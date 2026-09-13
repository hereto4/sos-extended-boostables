package your.mod.stealth;

import java.util.IdentityHashMap;
import java.util.Map;

import game.boosting.BOOSTABLES;
import game.boosting.BOOSTABLE_O;
import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.Boostable;
import game.boosting.Booster;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import game.battle.div.Div;
import init.sprite.UI.UI;
import init.type.HCLASS_RACE;
import settlement.stats.Induvidual;
import snake2d.util.rnd.RND;
import world.map.regions.Region;

/**
 * Registers the {@code BEHAVIOUR_STEALTH} and {@code BEHAVIOUR_ALERTNESS} boostables and their
 * per-subject "use it and grow it" skill drift, gated entirely behind {@link StealthAlertnessConfig}.
 *
 * <p>Both keys are per-{@code Induvidual} traits (like the vanilla {@code BEHAVIOUR_LAWFULNESS}/
 * {@code BEHAVIOUR_SANITY} keys), base value {@code 1.0}, so race/tech content can grant them with
 * {@code BEHAVIOUR_STEALTH>ADD} / {@code >MUL}. On top of that authored value, an individual subject's
 * own STEALTH or ALERTNESS creeps up by a small random amount every time {@link #gainStealth(Induvidual)}
 * / {@link #gainAlertness(Induvidual)} is called (see {@link CrimeStealthCheck}) — "practice makes
 * perfect", capped so it can never run away unbounded.
 *
 * <p><b>Per-individual growth is intentionally NOT persisted across a save/load.</b> It lives in an
 * in-memory {@link IdentityHashMap} keyed by {@link Induvidual}, exactly like this mod's other
 * ephemeral per-subject working state ({@code natureDesire}/{@code natureSeen} in {@code MainScript}).
 * A reloaded save simply restarts everyone's drift from the authored baseline — acceptable for a
 * "small random increase" flavour mechanic, and it avoids inventing a new save-file format for a
 * mod-only concept the engine knows nothing about.
 *
 * <p><b>"Search" is just Alertness.</b> A hero's ability to actively spot hidden things (dungeon
 * traps, etc) is intentionally the SAME boostable as a guard's ability to notice crimes in progress
 * — both are "how observant/perceptive is this individual", so dependant mods (e.g. a Heroes-style
 * mod's trap-search system) simply read {@code BEHAVIOUR_ALERTNESS} via the generic boostable-key
 * lookup rather than any dedicated "search" key.
 *
 * <p><b>Innate trait-like bonus/malus.</b> Alongside the "practice" growth above, every individual
 * also gets a small fixed random bump (positive or negative) to their base STEALTH and ALERTNESS,
 * rolled once (lazily, on first query) and cached for that individual's lifetime — the same shape
 * and magnitude as the vanilla game's own data-driven {@code TRAIT} bonuses (e.g. {@code HONEST}'s
 * {@code NOBLE_HONOUR>ADD: 0.5}). We can't add genuine new {@code init.type.TRAIT} entries at
 * runtime (the vanilla trait table is a fixed-size array built once at boot from static data files,
 * and {@code TRAIT}'s constructor is package-private), so this reimplements the same "one persistent
 * per-individual random roll" idea as a {@link Booster}, exactly like {@code GrowthBooster} below.
 */
public final class StealthAlertnessBoostables {

    public static final String STEALTH_KEY = "BEHAVIOUR_STEALTH";
    public static final String ALERTNESS_KEY = "BEHAVIOUR_ALERTNESS";

    /** Upper bound of the per-use random growth roll (uniform 0..this). */
    private static final double GROWTH_STEP_MAX = 0.01;
    /** Hard cap on total accumulated growth per individual, so drift can't run away over a long game. */
    private static final double GROWTH_CAP = 3.0;

    /** Innate trait-like roll range: uniform in [-INNATE_SPREAD, +INNATE_SPREAD], same order of
     *  magnitude as vanilla trait ADD boosts (e.g. HONEST +0.5, CUNNING -0.5). */
    private static final double INNATE_SPREAD = 0.5;

    private static final String LOG = "[sos-extended-boostables] stealth/alertness: ";

    private static Boostable stealth;
    private static Boostable alertness;
    private static boolean installed = false;

    private static final Map<Induvidual, Double> stealthGrowth = new IdentityHashMap<>();
    private static final Map<Induvidual, Double> alertnessGrowth = new IdentityHashMap<>();
    private static final Map<Induvidual, Double> stealthInnate = new IdentityHashMap<>();
    private static final Map<Induvidual, Double> alertnessInnate = new IdentityHashMap<>();

    private StealthAlertnessBoostables() {}

    /** Call from {@code initBeforeGameInited()}. No-op (and no keys registered) unless config-enabled. */
    public static void install() {
        if (!StealthAlertnessConfig.isEnabled())
            return;

        stealth = ensure(STEALTH_KEY, "STEALTH", "Stealth",
                "A subject's ability to commit crimes unseen. Weighed against a nearby guard's "
              + "Alertness when a crime is at risk of being reported.");
        alertness = ensure(ALERTNESS_KEY, "ALERTNESS", "Alertness",
                "A guard's ability to notice crimes in progress, or a hero's ability to spot hidden "
              + "dangers such as dungeon traps. Weighed against a nearby criminal's Stealth when a "
              + "crime is at risk of being reported.");

        if (stealth == null || alertness == null) {
            System.out.println(LOG + "registration failed; feature disabled.");
            return;
        }

        new GrowthBooster(stealthGrowth).add(stealth);
        new GrowthBooster(alertnessGrowth).add(alertness);
        new InnateBooster(stealthInnate).add(stealth);
        new InnateBooster(alertnessInnate).add(alertness);
        installed = true;
        System.out.println(LOG + "enabled — " + STEALTH_KEY + " / " + ALERTNESS_KEY + " registered.");
    }

    public static boolean isInstalled() {
        return installed;
    }

    private static Boostable ensure(String fullKey, String pushKey, String name, String desc) {
        if (BOOSTING.MAP().tryGet(fullKey) == null) {
            // NOTE: no obviously-matching vanilla icon ("sneak"/"eye"/"search" don't exist in UI.icons().s);
            // .human is already used by several of this mod's other BEHAVIOUR-style keys.
            BOOSTING.push(pushKey, 1.0, name, desc, UI.icons().s.human, BOOSTABLES.BEHAVIOUR());
        }
        Boostable b = BOOSTING.MAP().tryGet(fullKey);
        if (b == null)
            System.out.println(LOG + "could not register boostable: " + fullKey);
        return b;
    }

    /** The value used in the crime-detection contest for this subject: authored value + growth. */
    public static double effectiveStealth(Induvidual indu) {
        if (!installed || indu == null) return 0;
        return Math.max(0, stealth.get(indu));
    }

    public static double effectiveAlertness(Induvidual indu) {
        if (!installed || indu == null) return 0;
        return Math.max(0, alertness.get(indu));
    }

    /** Small random practice bump to this subject's Stealth. Safe to call every time it's exercised. */
    public static void gainStealth(Induvidual indu) {
        gain(stealthGrowth, indu);
    }

    /** Small random practice bump to this guard's Alertness. Safe to call every time it's exercised. */
    public static void gainAlertness(Induvidual indu) {
        gain(alertnessGrowth, indu);
    }

    private static void gain(Map<Induvidual, Double> growth, Induvidual indu) {
        if (!installed || indu == null) return;
        double cur = growth.getOrDefault(indu, 0.0);
        if (cur >= GROWTH_CAP) return;
        double next = Math.min(GROWTH_CAP, cur + RND.rFloat(GROWTH_STEP_MAX));
        growth.put(indu, next);
    }

    /** Ephemeral by design — see class javadoc. Call on game load. */
    public static void clearGrowthOnLoad() {
        stealthGrowth.clear();
        alertnessGrowth.clear();
        // Innate rolls are equally ephemeral, for the same reason (Induvidual identity does not
        // survive a save/load in this mod's bookkeeping) -- a reload simply re-rolls everyone's
        // innate predisposition, same tradeoff already accepted for growth.
        stealthInnate.clear();
        alertnessInnate.clear();
    }

    /**
     * A plain additive per-{@code Induvidual} factor sourced from a growth map — identical shape to
     * {@code MainScript.IndoctrinationBooster} (raw pass-through {@code getValue}, so no 0..1 clamp is
     * imposed the way {@code BoosterValue} would). Every other query target contributes {@code 0}
     * (a no-op additive term).
     */
    private static final class GrowthBooster extends Booster {
        private final BValue value;

        GrowthBooster(Map<Induvidual, Double> growth) {
            super(new BSourceInfo("Practice", UI.icons().s.human), false); // additive
            this.value = new BValue() {
                @Override public double vGet(Induvidual indu) { return growth.getOrDefault(indu, 0.0); }
                @Override public double vGet(HCLASS_RACE reg) { return 0; }
                @Override public double vGet(Player f) { return 0; }
                @Override public double vGet(FactionNPC f) { return 0; }
                @Override public double vGet(Region reg) { return 0; }
                @Override public double vGet(Div div) { return 0; }
            };
        }

        @Override public double from() { return 0; }
        @Override public double to() { return 0; }
        @Override public double getValue(double input) { return input; }
        @Override protected double pget(BOOSTABLE_O o) { return o.boostableValue(value); }
    }

    /**
     * A trait-like additive per-{@code Induvidual} factor: uniformly random in
     * {@code [-INNATE_SPREAD, +INNATE_SPREAD]}, rolled once (lazily on first query for that
     * individual) and cached forever after — "some folk are just naturally sneakier/more watchful
     * than others", the same flavour as a vanilla {@code TRAIT}'s fixed {@code BOOST} entry, just
     * generated in code instead of authored in a data file (see class javadoc for why). Every other
     * query target contributes {@code 0} (a no-op additive term) — this is a purely per-Induvidual
     * flavour stat, not a faction/region/div-wide one.
     */
    private static final class InnateBooster extends Booster {
        private final BValue value;

        InnateBooster(Map<Induvidual, Double> innate) {
            super(new BSourceInfo("Disposition", UI.icons().s.human), false); // additive
            this.value = new BValue() {
                @Override public double vGet(Induvidual indu) { return roll(innate, indu); }
                @Override public double vGet(HCLASS_RACE reg) { return 0; }
                @Override public double vGet(Player f) { return 0; }
                @Override public double vGet(FactionNPC f) { return 0; }
                @Override public double vGet(Region reg) { return 0; }
                @Override public double vGet(Div div) { return 0; }
            };
        }

        private static double roll(Map<Induvidual, Double> innate, Induvidual indu) {
            if (indu == null) return 0;
            Double cached = innate.get(indu);
            if (cached != null) return cached;
            double v = (RND.rFloat() * 2 - 1) * INNATE_SPREAD;
            innate.put(indu, v);
            return v;
        }

        @Override public double from() { return -INNATE_SPREAD; }
        @Override public double to() { return INNATE_SPREAD; }
        @Override public double getValue(double input) { return input; }
        @Override protected double pget(BOOSTABLE_O o) { return o.boostableValue(value); }
    }
}
