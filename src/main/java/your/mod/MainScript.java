package your.mod;

import game.boosting.*;
import game.battle.div.Div;
import game.battle.thread.status.DivStatus;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import game.time.TIME;
import init.constant.Config;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.RES_AMOUNT;
import init.sprite.UI.UI;
import init.trade.TR;
import init.trade.TRADE_TYPE;
import init.type.CAUSE_ARRIVES;
// NOTE: HCLASS/HCLASSES are also used by registerCitizenRaceFractions (OFCLASS_F), not only by
// the disabled STAT_WORK_RETIREMENT feature — keep these imports live.
import init.type.HCLASS;
import init.type.HCLASSES;
import init.type.HCLASS_RACE;
import init.type.HTYPES;
import init.value.GVALUES;
import util.data.DOUBLE_O;
import script.SCRIPT;
// CIVIC_INDOCTRINATION SHELVED (2026-06-29): ROOM_UNIVERSITY used only by that feature.
// import settlement.room.knowledge.university.ROOM_UNIVERSITY;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
// STAT_WORK_RETIREMENT DISABLED (2026-06-27): STAT/STANDINGS/StatStanding used only by that feature.
// import settlement.stats.stat.STAT;
// import settlement.stats.standing.STANDINGS;
// import settlement.stats.standing.StatStanding;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import world.WORLD;
import world.army.AD;
import world.entity.army.WArmy;
import world.entity.caravan.Shipment;
import world.map.regions.Region;
import world.region.RD;

import java.io.IOException;
import java.io.Serializable;
// STAT_WORK_RETIREMENT DISABLED (2026-06-27): reflection Field used only by that feature.
// import java.lang.reflect.Field;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

@SuppressWarnings("unused")
public final class MainScript implements SCRIPT {

    private static final String SLAVER_KEY = "ROOM__SLAVER";
    private static final String CANNIBAL_KEY = "ROOM__CANNIBAL";
    private static final String PLUNDER_KEY = "CIVIC_PLUNDER";
    // CIVIC_INDOCTRINATION SHELVED (2026-06-29): the v71.40 game removed StatsEducation.policyIndoctor
    // (indoctrination policy is now StatsEducation.policy(HCLASS,Race)). Shelved for a later version;
    // re-enable by uncommenting this constant plus the four other CIVIC_INDOCTRINATION-marked blocks
    // and migrating the policy check in IndoctrinationBooster.factor to the v71.40 API.
    // private static final String INDOCTRINATION_KEY = "CIVIC_INDOCTRINATION";

    // Each ROOM_*_ALL umbrella key plus the child prefix it cascades to.
    private static final String MINE_ALL_KEY = "ROOM_MINE_ALL";
    private static final String WORKSHOP_ALL_KEY = "ROOM_WORKSHOP_ALL";
    private static final String FARM_ALL_KEY = "ROOM_FARM_ALL";
    private static final String REFINER_ALL_KEY = "ROOM_REFINER_ALL";

    /** Vanilla raid loots once every this many seconds of raiding (WArmyState.raiding accumulator). */
    private static final double RAID_PERIOD = 120.0;

    // BATTLE_FEAR: a per-division aura. A division projects fear equal to its soldiers' BATTLE_FEAR
    // (race-sourced, so the division value is that race's fear); enemy divisions within range take a
    // morale penalty that falls off linearly with distance and is capped/floored.
    private static final String FEAR_KEY = "BATTLE_FEAR";
    private static final double FEAR_AURA_RANGE = 8.0;    // tiles; fear falls to 0 at this distance
    private static final double FEAR_INTENSITY_SCALE = 1.0; // summed fear -> intensity (pre-clamp)
    private static final double FEAR_MORALE_FLOOR = 0.4;  // most fear can do: morale ×0.4
    private static final int FEAR_MAX_ENEMIES = 8;        // cap on nearby enemies considered

    private Boostable battleFear;

    // ===== STAT_WORK_RETIREMENT DISABLED (2026-06-27) =====================================
    // Temporarily removed because its reflective standing/denominator manipulation inflated
    // citizen happiness in v71 (-> runaway immigration). It runs every 2s regardless of techs,
    // and no shipped tech/data file boosts the key (F is always 1.0), so it currently provides
    // no benefit while pinning the v71 fulfillment denominators incorrectly. Re-enable only after
    // re-validating the reflection against the v71 StandingCitizen/StandingData internals.
    // To restore: uncomment this block plus the four other STAT_WORK_RETIREMENT-marked blocks.
    //
    // STAT_* prefix: keys that scale a race-stat value. First one: STAT_WORK_RETIREMENT, which
    // multiplies how much retirement contributes to subjects' fulfillment (a net boost: the
    // retirement weight scales, the fulfillment denominator is held at baseline).
    // private static final String STAT_RETIREMENT_KEY = "STAT_WORK_RETIREMENT";
    // private static final String RETIREMENT_STAT_KEY = "WORK_RETIREMENT";
    // private static final double STAT_REFRESH_SECONDS = 2.0;

    private double processedRatio = 0.0;

    /** CIVIC_PLUNDER, resolved at init; null until then. */
    private Boostable civicPlunder;

    // CIVIC_INDOCTRINATION SHELVED (2026-06-29) — see note on INDOCTRINATION_KEY.
    // /** CIVIC_INDOCTRINATION, resolved at init; null until then. */
    // private Boostable civicIndoctrination;

    /** Per-army raid accumulators, mirroring WArmy.stateFloat for player raiders only. */
    private final IdentityHashMap<WArmy, Double> raidTimers = new IdentityHashMap<>();
    private final Set<WArmy> seenRaiders = Collections.newSetFromMap(new IdentityHashMap<WArmy, Boolean>());

    // ===== STAT_WORK_RETIREMENT DISABLED (2026-06-27) — see note above; re-enable together =====
    // --- STAT_WORK_RETIREMENT state (all reflection guarded; on any failure we disable cleanly) ---
    // private Boostable statRetirement;     // the CIVIC-less STAT_WORK_RETIREMENT boostable
    // private STAT retirementStat;          // the engine WORK_RETIREMENT standing stat
    // private double statTimer = 0;
    // private boolean statInited = false;
    // private boolean statDisabled = false;
    // private double statLastF = Double.NaN;
    // private Field fMax, fFrom, fTo, fMaxes, fDefs;
    // private double[][] baseWeights;       // [race.index][hclass.index] baseline retirement weights
    // private double[] baseCitMaxes, baseCitDefs, baseSlaMaxes, baseSlaDefs;

    public MainScript() {}

    @Override
    public CharSequence name() {
        return "sos-extended-boostables";
    }

    @Override
    public CharSequence desc() {
        return "Adds extended boostable keys for room types not covered by vanilla.";
    }

    @Override
    public boolean forceInit() {
        return true;
    }

    @Override
    public void initBeforeGameInited() {
        Boostable roomSlaver = ensureBoostable(SLAVER_KEY, "__SLAVER", "Slaver",
                "The effectiveness of your Slaver room. Higher values increase the submission of slaves processed through it.",
                UI.icons().s.slave, BOOSTABLES.ROOMS());
        if (roomSlaver != null) {
            registerSlaverEffect(roomSlaver);
        }

        Boostable roomCannibal = ensureBoostable(CANNIBAL_KEY, "__CANNIBAL", "Cannibal",
                "Multiplies the amount of resources gained when a corpse is butchered at a Cannibal Room.",
                SETT.ROOMS().CANNIBAL.icon, BOOSTABLES.ROOMS());
        if (roomCannibal != null) {
            registerCannibalEffect(roomCannibal);
        }

        // CIVIC_PLUNDER: increases resources gained from raiding with your armies on enemy territory.
        // (Distinct from vanilla CIVIC_RAIDING / "Raid Security", which lowers the chance of being raided.)
        civicPlunder = ensureBoostable(PLUNDER_KEY, "PLUNDER", "Raid Plunder",
                "Multiplies the resources your armies plunder while raiding enemy territory.",
                UI.icons().s.sword, BOOSTABLES.CIVICS());

        // CIVIC_INDOCTRINATION SHELVED (2026-06-29) — see note on INDOCTRINATION_KEY. Restore by
        // uncommenting this block (and migrating the v71.40 policy API in IndoctrinationBooster.factor).
        // CIVIC_INDOCTRINATION: increases the effectiveness (gain rate) of indoctrinating subjects.
        // Indoctrination is accumulated in the seam-less StatsEducation.educate(), but a university's
        // learningSpeed = learningSpeed*(1-degrade)*quality*bonus().get(subject), so a conditional factor
        // on each university room's bonus() boostable scales the gain — for exactly the races on the
        // indoctrination policy (see registerIndoctrinationEffect).
        // civicIndoctrination = ensureBoostable(INDOCTRINATION_KEY, "INDOCTRINATION", "Indoctrination",
        //         "Multiplies how quickly subjects on the indoctrination policy are indoctrinated at universities.",
        //         UI.icons().s.admin, BOOSTABLES.CIVICS());
        // if (civicIndoctrination != null) {
        //     registerIndoctrinationEffect(civicIndoctrination);
        // }

        // ROOM_*_ALL umbrella keys: one tooltip line that cascades to every matching room boostable.
        registerRoomAllCascade(MINE_ALL_KEY, "MINE_ALL", "Mines (All)", "ROOM_MINE_",
                "Affects every Mine room type at once.");
        registerRoomAllCascade(WORKSHOP_ALL_KEY, "WORKSHOP_ALL", "Workshops (All)", "ROOM_WORKSHOP_",
                "Affects every Workshop room type at once.");
        registerRoomAllCascade(FARM_ALL_KEY, "FARM_ALL", "Farms (All)", "ROOM_FARM_",
                "Affects every Farm room type at once.");
        registerRoomAllCascade(REFINER_ALL_KEY, "REFINER_ALL", "Refineries (All)", "ROOM_REFINER_",
                "Affects every Refinery room type at once.");

        // ===== STAT_WORK_RETIREMENT DISABLED (2026-06-27) — see note on the constants block =====
        // STAT_* prefix. New BoostableCat whose prefix is "STAT_"; push key "WORK_RETIREMENT"
        // -> "STAT_WORK_RETIREMENT". Effect applied at runtime via handleStatRetirement().
        // BoostableCat statCat = new BoostableCat("STAT_", "Stats", "", BoostableCat.TYPE_SETT, UI.icons().s.human);
        // statRetirement = ensureBoostable(STAT_RETIREMENT_KEY, "WORK_RETIREMENT", "Retirement Desire",
        //         "Multiplies how much retirement contributes to your subjects' fulfillment.",
        //         UI.icons().s.human, statCat);
        // retirementStat = findStat(RETIREMENT_STAT_KEY);
        // if (retirementStat == null) {
        //     System.err.println("[sos-extended-boostables] STAT_WORK_RETIREMENT: stat '" + RETIREMENT_STAT_KEY + "' not found; disabling.");
        //     statDisabled = true;
        // }

        // BATTLE_FEAR: register in the BATTLE category (base 0 — no fear by default; races add via
        // BATTLE_FEAR>ADD). Then install the morale aura. Registered here so it exists when race
        // BOOST promises resolve at finishSetup.
        battleFear = ensureBoostable(FEAR_KEY, "FEAR", "Fear",
                "Soldiers who project fear lower the BATTLE_MORALE of nearby enemy divisions. Grant via a race's BATTLE_FEAR>ADD.",
                UI.icons().s.crazy, BOOSTABLES.BATTLE(), 0.0);
        if (battleFear != null) {
            registerFearAura(battleFear);
        }

        // POPULATION_<RACE>_<CLASS>_OFCLASS_F GVALUEs: restore the v70 per-class race-fraction meaning
        // that v71 silently changed, so dependent tech REQUIRES (e.g. CaC monorace techs) resolve.
        // MUST run here in initBeforeGameInited — the GAME ctor calls this immediately before
        // init.finish() resolves the tech promises. (Previously this call was misplaced inside the
        // per-tick handleRaidPlunder, which early-returns when plunder<=1.0, so it never ran.)
        registerCitizenRaceFractions();

        // Low-Positive tooltip recolor: flip the engine's green/red for boostables where a LOWER value
        // is the positive outcome (e.g. PHYSICS_SOILING). The color decision is hardcoded in the engine
        // with no seam, so this self-attaches a Java agent that rewrites those methods. Fully guarded:
        // if the JVM blocks self-attach it logs the -javaagent fallback and leaves tooltips as vanilla.
        // See your.mod.boostcolor.LowPositiveColors / ColorAgent.
        your.mod.boostcolor.ColorAgent.install();
    }

    // ===== STAT_WORK_RETIREMENT DISABLED (2026-06-27) — helper used only by that feature =====
    // private static STAT findStat(String key) {
    //     for (STAT s : STATS.all()) {
    //         if (key.equals(s.key()))
    //             return s;
    //     }
    //     return null;
    // }

    private static Boostable ensureBoostable(String fullKey, String pushKey, String name, String desc, SPRITE icon, BoostableCat cat) {
        return ensureBoostable(fullKey, pushKey, name, desc, icon, cat, 1.0);
    }

    private static Boostable ensureBoostable(String fullKey, String pushKey, String name, String desc, SPRITE icon, BoostableCat cat, double baseValue) {
        if (BOOSTING.MAP().tryGet(fullKey) == null) {
            BOOSTING.push(pushKey, baseValue, name, desc, icon, cat);
        }
        Boostable b = BOOSTING.MAP().tryGet(fullKey);
        if (b == null) {
            System.err.println("[sos-extended-boostables] Could not register boostable: " + fullKey);
        }
        return b;
    }

    private void registerSlaverEffect(Boostable roomSlaver) {
        Boostable submission = BOOSTABLES.BEHAVIOUR().SUBMISSION;
        BSourceInfo info = new BSourceInfo("Slaver Training", UI.icons().s.slave);

        BValue bv = new BValue() {
            @Override
            public double vGet(Induvidual indu) {
                if (indu.hType() != HTYPES.SLAVE()) return 0;
                if (STATS.POP().COUNT.arrive.get(indu) == CAUSE_ARRIVES.PAROLE())
                    return roomSlaver.get(FACTIONS.player()) - roomSlaver.baseValue;
                return 0;
            }

            @Override
            public double vGet(Player f) {
                return (roomSlaver.get(f) - roomSlaver.baseValue) * processedRatio;
            }

            // v71: BValue.PopTime was removed and replaced by the per-population-class
            // query vGet(HCLASS_RACE). Mirror the old PopTime aggregate value here.
            @Override
            public double vGet(HCLASS_RACE reg) {
                return (roomSlaver.get(FACTIONS.player()) - roomSlaver.baseValue) * processedRatio;
            }

            @Override public double vGet(FactionNPC f) { return 0; }
            @Override public double vGet(Region reg) { return 0; }
            @Override public double vGet(Div div) { return 0; }
        };

        new BoosterValue(bv, info, 2.0, false).add(submission);
    }

    private void registerCannibalEffect(Boostable roomCannibal) {
        int patched = 0;
        for (Race race : RACES.all()) {
            LIST<RES_AMOUNT> list = race.resources();
            if (!(list instanceof ArrayList)) continue;
            @SuppressWarnings("unchecked")
            ArrayList<RES_AMOUNT> al = (ArrayList<RES_AMOUNT>) list;
            for (int i = 0; i < al.size(); i++) {
                RES_AMOUNT a = al.get(i);
                if (a instanceof BoostedResAmount) continue;
                al.replace(i, new BoostedResAmount(a.resource(), a.amount(), roomCannibal));
                patched++;
            }
        }
        System.out.println("[sos-extended-boostables] Wrapped " + patched + " race RESOURCES entries with " + CANNIBAL_KEY + " multiplier.");
    }

    // CIVIC_INDOCTRINATION SHELVED (2026-06-29) — see note on INDOCTRINATION_KEY.
    // /**
    //  * Installs the CIVIC_INDOCTRINATION effect. Indoctrination is accumulated in
    //  * {@code StatsEducation.educate(indu, speed)}, which has no boostable seam, but a university's
    //  * learning speed is {@code learningSpeed*(1-degrade)*quality*bonus().get(subject)} — so adding a
    //  * conditional multiplicative {@link Booster} to each university room's {@code bonus()} boostable
    //  * scales the learning speed, and therefore the indoctrination gain. The factor is {@code 1.0}
    //  * (no-op) except for subjects whose race is on the indoctrination policy, where it equals
    //  * {@code CIVIC_INDOCTRINATION.get(player)} — so education-only races are untouched and the boost
    //  * only applies while a race is actually being indoctrinated. Same shape as the SLAVER/umbrella
    //  * effects: a {@link Booster} added to an existing engine {@link Boostable}.
    //  *
    //  * <p>Schools ({@code ROOM_SCHOOL}) compute learning speed without a {@code bonus()} factor (they
    //  * never register one — {@code bonus()} is null), so child indoctrination in schools is unaffected;
    //  * universities are the covered venue.
    //  */
    // private void registerIndoctrinationEffect(Boostable civic) {
    //     BSourceInfo info = new BSourceInfo("Indoctrination", UI.icons().s.admin);
    //     int patched = 0;
    //     for (ROOM_UNIVERSITY u : SETT.ROOMS().UNIVERSITIES) {
    //         Boostable bonus = u.bonus();
    //         if (bonus == null) continue;
    //         new IndoctrinationBooster(civic, info).add(bonus);
    //         patched++;
    //     }
    //     System.out.println("[sos-extended-boostables] " + INDOCTRINATION_KEY
    //             + " scales learning speed on " + patched + " university room type(s).");
    // }

    /**
     * Installs the BATTLE_FEAR morale aura: a multiplicative factor on BATTLE_MORALE whose intensity,
     * for a division, comes from nearby <em>enemy</em> divisions that project fear. Mirrors how the
     * engine's own morale factors (Situation/Surrounded) work — it reads the per-division nearby-enemy
     * list the engine already maintains in {@link DivStatus}, so there is no spatial work of our own
     * and no per-soldier cost. Morale is a division-level boostable, queried ~once/second/division.
     *
     * <p>Span [1, FLOOR] with isMul: intensity 0 → ×1 (no effect, incl. all non-Div queries), intensity
     * 1 → ×FLOOR. Intensity = clamp(Σ enemyFear × linearFalloff, 0, 1), so multiple nearby fearful
     * enemies stack but are capped, and the floor bounds the worst case.
     */
    private void registerFearAura(Boostable fear) {
        Boostable morale = BOOSTABLES.BATTLE().MORALE;
        BSourceInfo info = new BSourceInfo("Fear", UI.icons().s.crazy);

        BValue bv = new BValue() {
            @Override public double vGet(Div d) { return fearIntensity(d, fear); }
            @Override public double vGet(Induvidual indu) { return 0; }
            @Override public double vGet(Player f) { return 0; }
            @Override public double vGet(FactionNPC f) { return 0; }
            @Override public double vGet(Region reg) { return 0; }
            @Override public double vGet(HCLASS_RACE reg) { return 0; }
        };

        new BoosterValue(bv, info, 1.0, FEAR_MORALE_FLOOR, true).add(morale);
    }

    /** Total fear intensity (0..1) bearing on division {@code d} from nearby enemy divisions. */
    private static double fearIntensity(Div d, Boostable fear) {
        if (d == null) return 0;
        DivStatus st = d.status();
        if (st == null) return 0;
        int n = st.enemiesClosest();
        if (n <= 0) return 0;

        ArrayList<Div> buf = new ArrayList<>(FEAR_MAX_ENEMIES);
        st.enemiesClosest(buf);

        double sum = 0;
        int c = Math.min(buf.size(), FEAR_MAX_ENEMIES);
        for (int i = 0; i < c; i++) {
            Div e = buf.get(i);
            if (e == null || e.men() == 0) continue;
            double ef = fear.get(e);
            if (ef <= 0) continue;
            double falloff = 1.0 - st.enemyClosestDist(i) / FEAR_AURA_RANGE;
            if (falloff <= 0) continue;
            sum += ef * falloff;
        }
        return CLAMP.d(sum * FEAR_INTENSITY_SCALE, 0, 1);
    }

    /**
     * Registers a single ROOM_*_ALL umbrella boostable and makes its value cascade onto every existing
     * boostable whose key starts with {@code childPrefix}. A tech that boosts the umbrella shows ONE line
     * ("Mines (All) *1.5") yet every matching room is multiplied. The umbrella resolves tech boosts per
     * target via the engine's BValueFaction, so the cascade is correct for any query target (the employee
     * Induvidual that room production passes in).
     */
    private void registerRoomAllCascade(String fullKey, String pushKey, String name, String childPrefix, String desc) {
        // Collect children BEFORE pushing the umbrella so the umbrella can never be its own child.
        snake2d.util.sets.ArrayListGrower<Boostable> children = new snake2d.util.sets.ArrayListGrower<>();
        SPRITE icon = UI.icons().s.house;
        for (Boostable b : BOOSTING.ALL()) {
            if (b.key != null && b.key.startsWith(childPrefix) && !b.key.equals(fullKey)) {
                if (children.size() == 0) icon = b.nativeIcon;
                children.add(b);
            }
        }

        Boostable umbrella = ensureBoostable(fullKey, pushKey, name, desc, icon, BOOSTABLES.ROOMS());
        if (umbrella == null) return;

        BSourceInfo info = new BSourceInfo(name, icon);
        for (Boostable child : children) {
            new UmbrellaBooster(umbrella, info).add(child);
        }
        System.out.println("[sos-extended-boostables] " + fullKey + " cascades to " + children.size() + " room boostables.");
    }

    @Override
    public SCRIPT_INSTANCE createInstance() {
        return new SCRIPT_INSTANCE() {
            private double timer = 0;

            @Override
            public void update(double ds) {
                timer -= ds;
                if (timer <= 0) {
                    timer = 4.0;
                    recomputeRatio();
                }
                handleRaidPlunder(ds);

                // ===== STAT_WORK_RETIREMENT DISABLED (2026-06-27) — see note on the constants block =====
                // statTimer -= ds;
                // if (statTimer <= 0) {
                //     statTimer = STAT_REFRESH_SECONDS;
                //     handleStatRetirement();
                // }
            }

            @Override
            public void save(FilePutter file) {}

            @Override
            public void load(FileGetter file) throws IOException {
                recomputeRatio();
                raidTimers.clear();
                // ===== STAT_WORK_RETIREMENT DISABLED (2026-06-27) =====
                // statTimer = 0; // reassert STAT scaling promptly after load (engine setAll ran during load)
            }
        };
    }

    private void recomputeRatio() {
        int total = 0;
        int processed = 0;
        for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
            if (!(e instanceof Humanoid)) continue;
            Humanoid h = (Humanoid) e;
            if (h.indu().hType() != HTYPES.SLAVE()) continue;
            total++;
            if (STATS.POP().COUNT.arrive.get(h.indu()) == CAUSE_ARRIVES.PAROLE())
                processed++;
        }
        processedRatio = total == 0 ? 0.0 : (double) processed / total;
    }

    /**
     * Delivers supplemental raid spoils for CIVIC_PLUNDER. Vanilla still loots its full 100% inside
     * WArmyState.raiding; here we additionally ship {@code (plunder-1)} times the same per-tick loot for
     * each player army actually in the raiding state. This leaves all vanilla mechanics intact
     * (raiding(), UI, sprites, devastation, population loss) and touches only the resource spoils, which is
     * exactly "resources gained from raiding". Battle-victory and conquest spoils are untouched.
     */
    private void handleRaidPlunder(double ds) {
        if (civicPlunder == null) return;
        Player p = FACTIONS.player();
        if (p == null || p.capitolRegion() == null) {
            if (!raidTimers.isEmpty()) raidTimers.clear();
            return;
        }
        double plunder = civicPlunder.get(p);
        if (plunder <= 1.0) {
            if (!raidTimers.isEmpty()) raidTimers.clear();
            return;
        }
        double bonus = plunder - 1.0;

        seenRaiders.clear();
        for (WArmy a : p.armies().all()) {
            if (!a.raiding()) continue;
            Region reg = a.region();
            if (reg == null) continue;
            seenRaiders.add(a);

            Double prev = raidTimers.get(a);
            double acc = (prev == null ? 0.0 : prev) + ds;
            if (acc >= RAID_PERIOD) {
                acc -= RAID_PERIOD;
                emitRaidBonus(a, reg, p, bonus);
            }
            raidTimers.put(a, acc);
        }

        // Drop accumulators for armies that are no longer raiding / no longer exist.
        if (raidTimers.size() != seenRaiders.size()) {
            raidTimers.keySet().retainAll(seenRaiders);
        }
    }

    /**
     * Re-registers the per-class population fraction that vanilla used through v70 and silently changed
     * in v71. The vanilla {@code POPULATION_<RACE>_<CLASS>_F} GVALUE (in {@code settlement.stats.SValues})
     * changed its denominator from "this class" to "the entire population":
     * <pre>
     *   v70:  POP(class, race) / POP(class, *)      // fraction of that class which is this race
     *   v71:  POP(class, race) / POP(*,    *)        // fraction of the whole population
     * </pre>
     * The v71 form means any noble — even a noble of the same race, since nobles are class NOBLE, not
     * CITIZEN — inflates the denominator without touching the numerator, so {@code EQUAL: 1.0} "monorace"
     * tech requirements can no longer be satisfied once a single noble is appointed.
     *
     * <p>This registers a parallel key {@code POPULATION_<RACE>_<CLASS>_OFCLASS_F} carrying the old v70
     * expression verbatim ({@code STATS.POP().POP.data(class).get(race) / STATS.POP().POP.data(class).get(null)}),
     * for every race × player class. Tech files that want the original behaviour point {@code REQUIRES.EQUAL} at this key
     * instead of the vanilla one. The value is computed live per query, so it needs nothing from
     * {@code SValues}; it only has to exist in the {@code GVALUES.FACTION} map before {@code init.finish()}
     * resolves the tech requirement promises — which is exactly what {@code initBeforeGameInited} guarantees
     * (the per-game {@code GVALUES} clear has already run by then, and the lock resolution has not).
     */
    private void registerCitizenRaceFractions() {
        int n = 0;
        for (Race r : RACES.all()) {
            final Race fr = r;
            for (HCLASS cl : HCLASSES.ALL()) {
                if (!cl.player) continue;
                final HCLASS fcl = cl;
                String key = "POPULATION_" + r.key + "_" + cl.key + "_OFCLASS_F";
                if (GVALUES.FACTION.get(key) != null) continue; // already present this session
                GVALUES.FACTION.push(key, cl.names + ": " + r.info.names, cl.icon(),
                        new DOUBLE_O<Faction>() {
                            @Override
                            public double getD(Faction o) {
                                // Exact v70 expression: fraction of the class population that is this
                                // race. v71 replaced both terms (POP.data -> POP.tot, and the per-class
                                // denominator -> whole-population), which is what broke the requirement.
                                double div = STATS.POP().POP.data(fcl).get(null);
                                if (div == 0) return 0;
                                return STATS.POP().POP.data(fcl).get(fr) / div;
                            }
                        }, true);
                n++;
            }
        }
        System.out.println("[sos-extended-boostables] Registered " + n
                + " POPULATION_*_*_OFCLASS_F values (v70 per-class race fractions).");
    }

    /** Replicates the per-tick loot of WArmyState.raiding, scaled by {@code bonus} (= plunder-1). */
    private void emitRaidBonus(WArmy a, Region reg, Player p, double bonus) {
        // Once the region is fully devastated vanilla loots nothing, so neither do we.
        if (RD.DEVASTATION().current.get(reg) >= RD.DEVASTATION().current.max(reg))
            return;

        double rd = RD.RACES().popSize(reg);
        if (rd <= 0) return;
        double ad = (double) AD.men(null).get(a) / Config.battle().MEN_PER_ARMY;
        double dd = ad / rd;
        double d = 180 * dd / (TIME.secondsPerDay() * 4);
        if (d <= 0) return;

        Shipment s = null;
        for (RESOURCE res : RESOURCES.ALL()) {
            int baseAm = (int) Math.ceil(RD.OUTPUT().get(TR.get(res)).loot(reg) * d * 10.0);
            int am = (int) Math.round(baseAm * bonus);
            if (am > 0) {
                if (s == null) {
                    s = WORLD.ENTITIES().caravans.create(a.ctx(), a.cty(), p.capitolRegion(), TRADE_TYPE.spoils);
                    if (s == null) return;
                }
                s.loadAndReserve(TR.get(res), am);
            }
        }
    }

    // ===== STAT_WORK_RETIREMENT DISABLED (2026-06-27) ====================================
    // The three methods below (handleStatRetirement / initStatReflection / restoreDenominators)
    // are commented out together with the constants, fields, registration and update-tick blocks
    // marked elsewhere. Reflectively pinning StandingCitizen.maxes/defs inflated v71 happiness.
    // Uncomment all STAT_WORK_RETIREMENT-marked blocks to restore (after re-validating v71 fields).
    //
    // /**
    //  * Applies STAT_WORK_RETIREMENT. The engine has no boostable seam in the standing system, so we
    //  * reflectively scale the WORK_RETIREMENT per-class standing weight (a {@code final} field) by
    //  * F = STAT_WORK_RETIREMENT.get(player) for every race, and hold the cached fulfillment
    //  * denominators ({@code StandingCitizen.maxes/defs}) at their baseline so the result is a *net*
    //  * boost to retirement fulfillment rather than a reweighting. All writes are absolute (idempotent)
    //  * and fully guarded — any reflection failure disables the feature without affecting the game.
    //  * The standing computation itself is untouched, so there is no per-subject runtime cost; this
    //  * runs only every STAT_REFRESH_SECONDS s and the weight rescale is skipped when F is
    //  * unchanged.
    //  */
    // private void handleStatRetirement() {
    //     if (statDisabled || statRetirement == null || retirementStat == null) return;
    //     Player p = FACTIONS.player();
    //     if (p == null) return;
    //     try {
    //         if (!statInited) initStatReflection();
    //
    //         double f = statRetirement.get(p);
    //         if (f != statLastF) {
    //             for (Race r : RACES.all()) {
    //                 StatStanding.StandingDef def = r.stats().def(retirementStat.standing());
    //                 if (def == null) continue;
    //                 double[] bw = baseWeights[r.index];
    //                 for (HCLASS c : HCLASSES.ALL()) {
    //                     StatStanding.StandingDef.StandingData sd = def.get(c);
    //                     double v = bw[c.index()] * f;
    //                     fMax.setDouble(sd, v);
    //                     if (def.inverted) { fFrom.setDouble(sd, v); fTo.setDouble(sd, 0.0); }
    //                     else { fFrom.setDouble(sd, 0.0); fTo.setDouble(sd, v); }
    //                 }
    //             }
    //             statLastF = f;
    //         }
    //
    //         // Hold the fulfillment denominators at baseline so scaled retirement is a net gain, not a
    //         // reweight. The engine only writes these in setAll() (init/load), so reasserting here also
    //         // repairs them after a load.
    //         restoreDenominators(STANDINGS.CITIZEN(), baseCitMaxes, baseCitDefs);
    //         restoreDenominators(STANDINGS.SLAVE(), baseSlaMaxes, baseSlaDefs);
    //     } catch (Throwable t) {
    //         statDisabled = true;
    //         System.err.println("[sos-extended-boostables] STAT_WORK_RETIREMENT disabled (reflection failed): " + t);
    //     }
    // }
    //
    // private void initStatReflection() throws Exception {
    //     StatStanding.StandingDef sample = RACES.all().get(0).stats().def(retirementStat.standing());
    //     Class<?> sdClass = sample.get(HCLASSES.ALL().get(0)).getClass();
    //     fMax = sdClass.getDeclaredField("max");   fMax.setAccessible(true);
    //     fFrom = sdClass.getDeclaredField("from");  fFrom.setAccessible(true);
    //     fTo = sdClass.getDeclaredField("to");      fTo.setAccessible(true);
    //
    //     Class<?> scClass = STANDINGS.CITIZEN().getClass();
    //     fMaxes = scClass.getDeclaredField("maxes"); fMaxes.setAccessible(true);
    //     fDefs = scClass.getDeclaredField("defs");   fDefs.setAccessible(true);
    //
    //     // Capture baseline per-class weights (read happens before any scaling -> true baseline).
    //     baseWeights = new double[RACES.all().size()][];
    //     for (Race r : RACES.all()) {
    //         StatStanding.StandingDef def = r.stats().def(retirementStat.standing());
    //         double[] bw = new double[HCLASSES.ALL().size()];
    //         for (HCLASS c : HCLASSES.ALL())
    //             bw[c.index()] = def.get(c).max;
    //         baseWeights[r.index] = bw;
    //     }
    //
    //     baseCitMaxes = ((double[]) fMaxes.get(STANDINGS.CITIZEN())).clone();
    //     baseCitDefs  = ((double[]) fDefs.get(STANDINGS.CITIZEN())).clone();
    //     baseSlaMaxes = ((double[]) fMaxes.get(STANDINGS.SLAVE())).clone();
    //     baseSlaDefs  = ((double[]) fDefs.get(STANDINGS.SLAVE())).clone();
    //
    //     statInited = true;
    // }
    //
    // private void restoreDenominators(Object standingCitizen, double[] baseMaxes, double[] baseDefs) throws Exception {
    //     double[] m = (double[]) fMaxes.get(standingCitizen);
    //     double[] d = (double[]) fDefs.get(standingCitizen);
    //     System.arraycopy(baseMaxes, 0, m, 0, Math.min(baseMaxes.length, m.length));
    //     System.arraycopy(baseDefs, 0, d, 0, Math.min(baseDefs.length, d.length));
    // }

    private static final class BoostedResAmount implements RES_AMOUNT, Serializable {
        private static final long serialVersionUID = 1L;
        private final byte cIndex;
        private final int baseAmount;
        private final Boostable boost;

        BoostedResAmount(RESOURCE resource, int baseAmount, Boostable boost) {
            this.cIndex = resource.bIndex();
            this.baseAmount = baseAmount;
            this.boost = boost;
        }

        @Override
        public RESOURCE resource() {
            return RESOURCES.ALL().get(cIndex);
        }

        @Override
        public int amount() {
            double mult = boost.get(FACTIONS.player());
            if (mult <= 0) return 0;
            return (int) Math.round(baseAmount * mult);
        }
    }

    /**
     * A multiplicative factor placed on each ROOM_*_X boostable whose value mirrors the live aggregated
     * value of an umbrella ROOM_*_ALL boostable for the same query target. getValue is identity, so
     * {@code get(o) == umbrella.get(o)}. No deadlock: the umbrella never references its children.
     */
    private static final class UmbrellaBooster extends Booster {
        private final Boostable umbrella;

        UmbrellaBooster(Boostable umbrella, BSourceInfo info) {
            super(info, true); // multiplicative
            this.umbrella = umbrella;
        }

        @Override
        public double from() {
            return 1.0;
        }

        @Override
        public double to() {
            return 1.0;
        }

        @Override
        public double getValue(double input) {
            return input;
        }

        @Override
        protected double pget(BOOSTABLE_O o) {
            return umbrella.get(o);
        }
    }

    // CIVIC_INDOCTRINATION SHELVED (2026-06-29) — see note on INDOCTRINATION_KEY. The factor() body
    // references StatsEducation.policyIndoctor, removed in v71.40; migrate to policy(HCLASS,Race) on
    // re-enable (compare the active StatEducation against the "INDOCTRINATION" one).
    // /**
    //  * A multiplicative factor placed on a university's learning-speed {@code bonus()} boostable. Its value
    //  * is {@code CIVIC_INDOCTRINATION.get(player)} for a subject whose race is on the indoctrination policy
    //  * and {@code 1.0} (a no-op) for everyone else — so learning speed (and hence indoctrination gain) is
    //  * scaled exactly while a race is being indoctrinated, and education is never affected. getValue is
    //  * identity (like {@link UmbrellaBooster}), so the multiplier is applied as-is rather than being clamped
    //  * to [0,1] the way {@code BoosterValue} would. Only the per-Induvidual query (the one the educate path
    //  * uses) carries the factor; faction/population-class queries return the neutral 1.0.
    //  */
    // private static final class IndoctrinationBooster extends Booster {
    //     private final Boostable civic;
    //     private final BValue value;
    //
    //     IndoctrinationBooster(Boostable civic, BSourceInfo info) {
    //         super(info, true); // multiplicative
    //         this.civic = civic;
    //         this.value = new BValue() {
    //             @Override public double vGet(Induvidual indu) { return factor(indu.race()); }
    //             @Override public double vGet(HCLASS_RACE reg) { return 1.0; }
    //             @Override public double vGet(Player f) { return 1.0; }
    //             @Override public double vGet(FactionNPC f) { return 1.0; }
    //             @Override public double vGet(Region reg) { return 1.0; }
    //             @Override public double vGet(Div div) { return 1.0; }
    //         };
    //     }
    //
    //     /** The multiplier to apply: the boostable value if {@code r} is being indoctrinated, else 1.0. */
    //     private double factor(Race r) {
    //         if (r != null && STATS.EDUCATION().policyIndoctor.is(r))
    //             return civic.get(FACTIONS.player());
    //         return 1.0;
    //     }
    //
    //     @Override
    //     public double from() {
    //         return 1.0;
    //     }
    //
    //     @Override
    //     public double to() {
    //         return 1.0;
    //     }
    //
    //     @Override
    //     public double getValue(double input) {
    //         return input;
    //     }
    //
    //     @Override
    //     protected double pget(BOOSTABLE_O o) {
    //         return o.boostableValue(value);
    //     }
    // }
}
