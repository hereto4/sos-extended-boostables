package your.mod.cannibal;

import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import game.boosting.BoosterValue;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import game.time.TIME;
import init.type.CAUSE_LEAVES;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.thing.ThingsCorpses;
import settlement.thing.ThingsCorpses.Corpse;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sprite.SPRITE;

import java.io.IOException;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * {@code ROOM__CANNIBAL_RESOURCE_<RESOURCE>} — per-subject keys that make a corpse, when butchered at a
 * Cannibal Room, also yield {@code <RESOURCE>}. The first (and so far only) one is
 * {@code ROOM__CANNIBAL_RESOURCE_KNOWLEDGE}.
 *
 * <p><b>Key naming.</b> Pushed as {@code __CANNIBAL_RESOURCE_KNOWLEDGE} into {@code BOOSTABLES.ROOMS()}
 * (prefix {@code "ROOM_"}); {@code BOOSTING.push} strips one leading {@code _}, giving
 * {@code ROOM__CANNIBAL_RESOURCE_KNOWLEDGE} — the same double underscore as {@code ROOM__CANNIBAL}, which
 * mirrors the room's own key {@code _CANNIBAL} ({@code ROOM_CANNIBAL.java}, {@code super(0, init,
 * "_CANNIBAL", cat)}).
 *
 * <p><b>Value.</b> Base 0, author with {@code >ADD}. The value is the amount of the resource yielded by
 * one whole, intact adult corpse. Vanilla butchering removes the corpse's meat in 0.25 steps
 * ({@code Corpse.resRemove}); each step yields the matching share, so a partial corpse (not intact 0.5,
 * a child 0.25 — {@code Corpse.init}) yields proportionally less, just as its vanilla {@code RESOURCES}
 * do.
 *
 * <p><b>Read per subject.</b> The key is read as {@code key.get(corpse.indu())} — the butchered subject
 * itself — so a {@code TARGET_RACE} tech filters it by the victim's race, and a race file's own
 * {@code BOOST} can grant it too. {@code TARGET_CLASS} is ignored
 * ({@code TargetFilters.markClassUnfilterable}): the only victims a Cannibal Room ever takes are caged
 * {@code HTYPES.PRISONER}s ({@code WorkCannibal.kill.victim}), which are the player's faction
 * ({@code Induvidual.faction()}) and always of class {@code HCLASSES.OTHER} ({@code HTYPES.PRISONER}),
 * so a class constraint could only be redundant ({@code OTHER}) or silently zero the yield.
 *
 * <p><b>Detecting a butcher step.</b> The engine has no event for it ({@code WorkCannibal.butcher2} is
 * sealed), so {@link #update} watches the corpses the engine keeps on its per-cause
 * {@code EXECUTED} list that lie inside a Cannibal Room, and compares {@code resLeft()} between polls.
 * {@code resLeft = res * (1 - decay)}: a butcher step drops {@code res} by 0.25 at once, while rot only
 * moves {@code decay} by at most 0.05 once every ~100 s ({@code ThingsCorpses} updater), so a drop of
 * at least {@link #STEP_THRESHOLD} in one poll is a butcher step. The last step removes the corpse in
 * the same call, so a tracked corpse that disappears is credited with whatever it had left. A corpse
 * that rots away instead has {@code resLeft <= 0} by then, so it yields nothing.
 *
 * <p><b>KNOWLEDGE is not a stockpile.</b> Knowledge is the {@code CIVIC_KNOWLEDGE} boostable: a standing
 * total that tech allocates against ({@code PTech.TechCurr.total()}). Libraries feed it from an
 * accumulated value that degrades over time ({@code AdminData}). Butchering does the same: yields go into
 * a pool that feeds {@code CIVIC_KNOWLEDGE} as a "Cannibalism" line, and the pool degrades by
 * {@link #LOSS_PER_YEAR} (5%) a year — deliberately slower than library knowledge, which loses ~20% a
 * year ({@code LIBRARY_NORMAL.txt VALUE_DEGRADE_PER_YEAR: 0.225}). A steady rate of butchering therefore holds a steady amount of knowledge instead of adding up forever.
 * The pool is saved with the game.
 */
public final class CannibalYield {

    public static final String KNOWLEDGE_KEY = "ROOM__CANNIBAL_RESOURCE_KNOWLEDGE";
    private static final String KNOWLEDGE_TARGET = "CIVIC_KNOWLEDGE";

    /** Fraction of the pool lost per in-game year (libraries lose ~20%). */
    private static final double LOSS_PER_YEAR = 0.05;
    /** {@link #LOSS_PER_YEAR} as a continuous rate: {@code exp(-rate * 1 year) == 1 - LOSS_PER_YEAR}. */
    private static final double DEGRADE_RATE = -Math.log(1.0 - LOSS_PER_YEAR);

    /** Smallest per-poll {@code resLeft} drop counted as a butcher step (a step is ~0.25). */
    private static final double STEP_THRESHOLD = 0.1;

    private static final int SAVE_MAGIC = 0x43414E4E; // "CANN"
    private static final int SAVE_VERSION = 1;
    private static final int SAVE_END = 0x454E4421;   // "END!"

    private static Boostable knowledgeKey;
    private static double knowledgePool;

    /** Corpses currently inside a Cannibal Room, with the subject and {@code resLeft} last seen. */
    private static final Map<Corpse, Tracked> tracked = new IdentityHashMap<>();
    private static int generation;

    private static final class Tracked {
        final Induvidual indu;
        double last;
        int seen;

        Tracked(Induvidual indu, double last) {
            this.indu = indu;
            this.last = last;
        }
    }

    private CannibalYield() {}

    /** Registers the keys and their resource sinks. Call from {@code initBeforeGameInited()}. */
    public static void register(BoostableCat cat, SPRITE icon) {
        if (BOOSTING.MAP().tryGet(KNOWLEDGE_KEY) == null) {
            BOOSTING.push("__CANNIBAL_RESOURCE_KNOWLEDGE", 0.0, "Cannibal: Knowledge",
                    "Knowledge gained for each corpse butchered at a Cannibal Room. It fades by 5% a year, "
                  + "much more slowly than knowledge from libraries.",
                    icon, cat, 0.0);
        }
        knowledgeKey = BOOSTING.MAP().tryGet(KNOWLEDGE_KEY);
        if (knowledgeKey == null) {
            System.err.println("[sos-extended-boostables] Could not register boostable: " + KNOWLEDGE_KEY);
            return;
        }
        // Every victim is a prisoner (class OTHER), so TARGET_CLASS could only be redundant or zero the
        // yield: ignore it and keep TARGET_RACE.
        your.mod.targetfilter.TargetFilters.markClassUnfilterable(knowledgeKey);

        Boostable knowledge = BOOSTING.MAP().tryGet(KNOWLEDGE_TARGET);
        if (knowledge == null) {
            System.err.println("[sos-extended-boostables] " + KNOWLEDGE_KEY + ": " + KNOWLEDGE_TARGET
                    + " not found; effect disabled (key still registered).");
            knowledgeKey = null;
            return;
        }

        // Same shape as AdminData's library booster: an additive BoosterValue whose BValue is the pool
        // normalised against a large ceiling, player-only (NPCs have no settlement to butcher in).
        final double max = 1_000_000.0;
        BValue v = new BValue.BValuePlayerOnly() {
            @Override public double vGet(Player f) { return knowledgePool / max; }
            @Override public double vGet(FactionNPC f) { return 0; }
        };
        new BoosterValue(v, new BSourceInfo("Cannibalism", icon), 0, max, false).add(knowledge);
        System.out.println("[sos-extended-boostables] " + KNOWLEDGE_KEY + " feeds " + KNOWLEDGE_TARGET + ".");
    }

    /** Per-frame poll. Cheap: walks only the engine's EXECUTED-corpse list. */
    public static void update(double ds) {
        if (knowledgeKey == null) return;

        if (ds > 0 && knowledgePool > 0) {
            knowledgePool *= Math.exp(-DEGRADE_RATE * ds / TIME.years().bitSeconds());
            if (knowledgePool < 1e-6) knowledgePool = 0;
        }

        int gen = ++generation;
        ThingsCorpses corpses = SETT.THINGS().corpses;
        for (Corpse c = corpses.getFirst(CAUSE_LEAVES.EXECUTED()); c != null; c = corpses.getNext(c)) {
            if (c.isRemoved()) continue;
            if (SETT.ROOMS().map.blueprint.get(c.ctx(), c.cty()) != SETT.ROOMS().CANNIBAL) continue;

            double now = Math.max(0, c.resLeft());
            Tracked t = tracked.get(c);
            if (t != null && t.indu != c.indu()) {
                // Engine reused this corpse slot: the previous occupant was removed between polls.
                grant(t.indu, t.last);
                t = null;
            }
            if (t == null) {
                t = new Tracked(c.indu(), now);
                tracked.put(c, t);
            } else {
                double drop = t.last - now;
                if (drop >= STEP_THRESHOLD) grant(t.indu, drop);
                t.last = now;
            }
            t.seen = gen;
        }

        Iterator<Map.Entry<Corpse, Tracked>> it = tracked.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Corpse, Tracked> e = it.next();
            Tracked t = e.getValue();
            if (t.seen == gen) continue;
            // Gone from the room. Removed = the final butcher step; otherwise it was moved out, so stop.
            if (e.getKey().isRemoved()) grant(t.indu, t.last);
            it.remove();
        }
    }

    private static void grant(Induvidual indu, double share) {
        if (share <= 0) return;
        double v = knowledgeKey.get(indu);
        if (v > 0) knowledgePool += v * share;
    }

    /** New game / before a load: no pool, nothing tracked. */
    public static void reset() {
        knowledgePool = 0;
        tracked.clear();
    }

    public static void save(FilePutter file) {
        file.i(SAVE_MAGIC);
        file.i(SAVE_VERSION);
        file.d(knowledgePool);
        // Trailing int: FileGetter.d() refuses to read a double from the last 8 bytes of a file.
        file.i(SAVE_END);
    }

    /** Bytes {@link #save} writes. */
    public static final int SAVE_BYTES = 4 + 4 + 8 + 4;

    /**
     * Reads what {@link #save} wrote, given {@code available} bytes left in this script's save chunk.
     * A save from before this feature has none, and anything unrecognised is ignored: both load as an
     * empty pool. The caller re-seeks to the chunk end afterwards, so a partial read here is harmless.
     */
    public static void load(FileGetter file, int available) throws IOException {
        reset();
        if (available < SAVE_BYTES) return;
        if (file.i() != SAVE_MAGIC || file.i() != SAVE_VERSION) return;
        knowledgePool = Math.max(0, file.d());
        file.i(); // SAVE_END
    }
}
