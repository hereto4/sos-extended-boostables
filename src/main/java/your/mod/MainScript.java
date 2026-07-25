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
import init.type.NEEDS;
import init.value.GVALUES;
import util.data.DOUBLE_O;
import script.SCRIPT;
// CIVIC_INDOCTRINATION re-enabled 2026-07-05 (effect migrated to the v71.40 education-policy API).
import settlement.room.knowledge.university.ROOM_UNIVERSITY;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsEducation;
// STAT_WORK_RETIREMENT DISABLED (2026-06-27): STAT/STANDINGS/StatStanding used only by that feature.
// import settlement.stats.stat.STAT;
// import settlement.stats.standing.STANDINGS;
// import settlement.stats.standing.StatStanding;
import snake2d.util.datatypes.COORDINATE;
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
    private static final String PLUNDER_KEY = "WORLD_PLUNDER"; // renamed from CIVIC_PLUNDER 2026-07-04
    // CIVIC_INDOCTRINATION re-enabled 2026-07-05. The v70/v71.19 effect used the removed
    // StatsEducation.policyIndoctor; the effect now uses the v71.40 API StatsEducation.policy(HCLASS,Race)
    // == the INDOCTRINATION StatEducation (see IndoctrinationBooster.factor). CAC tech boosts this key.
    private static final String INDOCTRINATION_KEY = "CIVIC_INDOCTRINATION";

    // Each ROOM_*_ALL umbrella key plus the child prefix it cascades to.
    private static final String MINE_ALL_KEY = "ROOM_MINE_ALL";
    private static final String WORKSHOP_ALL_KEY = "ROOM_WORKSHOP_ALL";
    private static final String FARM_ALL_KEY = "ROOM_FARM_ALL";
    private static final String REFINER_ALL_KEY = "ROOM_REFINER_ALL";

    // WORLD_PRODUCTION_SLAVE_ALL: an umbrella over the per-race WORLD_PRODUCTION_SLAVE_<RACE> region-output
    // keys (same cascade pattern as ROOM_*_ALL). Excludes the hidden WORLD_PRODUCTION_SLAVE_<RACE>_YEARLY
    // display-derivative variants (per user, 2026-07-05).
    private static final String SLAVE_PRODUCTION_ALL_KEY = "WORLD_PRODUCTION_SLAVE_ALL";

    // ===== CLASS_<CLASS>[_<ROOMTYPE>] "Class Treatment" SHELVED (2026-07-04) =========================
    // Confirmed working in-game (CLASS_CITIZEN need-rates + CLASS_CITIZEN_MINE all-mine output), then
    // shelved for a later update per user request. To restore: uncomment all five CLASS_*-marked blocks —
    // this constants block, the registration block in initBeforeGameInited() (BoostableCat classCat …
    // registerClassKey …), registerClassKey(), registerClassTreatment(), and the ClassTreatmentBooster
    // nested class. No save-format impact (nothing is serialized). Full design: SPEC_CLASS_KEYS.md; player
    // doc: KEYS.md "Class keys"; project memory #8 + reference_rates_need_semantics.
    //
    // CLASS_<CLASS>[_<ROOMTYPE>] keys: a conditional multiplicative factor applied to selected per-subject
    // boostables, but ONLY for subjects of the matching population class (HCLASS); every other
    // subject/query gets the neutral 1.0 (same shape as the SLAVER/umbrella effects). The applied
    // multiplier is clamped to [CLASS_MIN, CLASS_MAX] so it can never reach 0. Every CLASS_<CLASS>* key
    // scales the three need-growth rates below; a room-typed CLASS_<CLASS>_<ROOMTYPE> also scales that
    // class's output across every room of the type via the ROOM_<ROOMTYPE>_ALL umbrella.
    // private static final double CLASS_MIN = 0.5;
    // private static final double CLASS_MAX = 1.5;
    // /** The three per-subject need-growth rates every CLASS_<CLASS>* key scales. */
    // private static final String[] CLASS_RATE_TARGETS = { "RATES_HUNGER", "RATES_THIRST", "RATES_SHOPPING" };

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

    // RATES_NATURE ("Piety (Nature)"): a per-subject multiplier centered at 1.0 meaning DESIRE for nature
    // (>1 seeks, <1 shuns, =1 neutral). NOT an engine need — the engine reads nothing; our per-tick loop
    // (Stage 1 passive reward + Stage 2 pilgrimage) is the only consumer. Registered under the vanilla
    // RATES_ "Service Needs" cat (NEEDS.bCat()) so it groups with the other need-rates. See
    // SPEC_NATURE_PIETY.md / reference_songsofsyx_behaviour_env_seams.md.
    private static final String NATURE_KEY = "RATES_NATURE";
    /** Hard floor on the computed value so >MUL:0 authoring never reaches 0 (engine x/0 guard). */
    private static final double NATURE_MIN = 0.01;
    private Boostable ratesNature;

    // --- Stage 1 nature-piety tuning knobs ---
    /** Seconds between reward passes (mirrors the engine's 16-tick StatsAccess cadence, in wall time). */
    private static final double NATURE_PERIOD = 1.0;
    /** Seconds between full rebuilds of the wild-tree proximity map (forests change slowly). */
    private static final double NATURE_TREE_REFRESH = 30.0;
    /** Tiles a wild tree spreads "nature" (linear Chebyshev falloff); matches the monument min radius. */
    private static final int NATURE_TREE_RADIUS = 5;
    /** Minimum natureValue (0..1) that counts as "near nature". */
    private static final double NATURE_THRESHOLD = 0.05;
    /** affinity × natureValue → shrine quality (0..1), pre-clamp. */
    private static final double NATURE_REWARD_SCALE = 1.0;

    // --- Stage 2 pilgrimage tuning knobs ---
    /**
     * Master switch for Stage 2 (active pilgrimage). Stage 1 (passive reward) is always on. Stage 2
     * commandeers idle citizens' AI to walk them to nature; it is the one fragile part (SPEC §7). Flip to
     * {@code false} to ship/run the proven passive core alone (recompile).
     */
    private static final boolean NATURE_PILGRIMAGE_ENABLED = true;
    /**
     * Minimum affinity to be eligible for a pilgrimage. {@code 0} means ANY nature-lover (RATES_NATURE > 1)
     * can pilgrimage — same gate as the Stage-1 passive reward, per the feature's intent. This only widens
     * the eligible pool; how many actually pilgrimage at once is bounded by {@link #NATURE_MAX_PILGRIMS}.
     * Raise it (e.g. 0.5 = value ≥ 1.5) if you want only strong lovers to leave their work to seek nature.
     */
    private static final double NATURE_PILGRIM_MIN_AFFINITY = 0.0;
    /**
     * Concurrent-pilgrim cap as a FRACTION of settlement population, so it scales with city growth instead
     * of a fixed number. Effective cap = clamp(pop × pct, MIN, ABS). Raise the ABS ceiling (or remove the
     * clamp) if you want it to keep scaling without bound.
     */
    private static final double NATURE_MAX_PILGRIMS_PCT = 0.02; // 2% of population
    /** Floor so small settlements still allow at least this many pilgrims. */
    private static final int NATURE_MAX_PILGRIMS_MIN = 1;
    /** Safety ceiling so we never commandeer an unreasonable number of citizens' AI at once. */
    private static final int NATURE_MAX_PILGRIMS_ABS = 30;
    /** Seconds a pilgrim lingers at the destination (int — STAND.activateTime takes int seconds). */
    private static final int NATURE_PILGRIM_STAND_SECS = 6;
    /** Watchdog: force-release a pilgrim after this many seconds no matter what (never pin a citizen). */
    private static final double NATURE_PILGRIM_MAX_EPISODE = 45.0;
    /** How far (tiles) to search outward for a nature destination when a pilgrimage triggers. */
    private static final int NATURE_PILGRIM_SEARCH_RADIUS = 40;
    /** A destination tile must have at least this natureValue (so the pilgrim arrives actually at nature). */
    private static final double NATURE_PILGRIM_PICK = 0.5;
    /** Chebyshev tiles from the destination that count as "arrived". */
    private static final int NATURE_PILGRIM_ARRIVE = 2;
    /**
     * Desire accrued per reward pass per unit affinity while a lover is away from nature. Reaching nature
     * resets desire to 0 (sated), so this + {@link #NATURE_DESIRE_TRIGGER} sets how often a citizen seeks
     * nature — the mod-side stand-in for the need-reset cadence a native engine need gets for free.
     */
    private static final double NATURE_DESIRE_GROWTH = 1.0;
    /** Accrued desire at which an idle lover sets out on a pilgrimage (affinity-1 lover: ~this many passes). */
    private static final double NATURE_DESIRE_TRIGGER = 60.0;

    /** Active pilgrimages, keyed by the commandeered citizen. Transient; never serialized. */
    private final IdentityHashMap<Humanoid, Pilgrim> pilgrims = new IdentityHashMap<>();

    /** Per-lover accumulated desire to seek nature (grows away from nature, resets on reaching it). Transient. */
    private final IdentityHashMap<Induvidual, Double> natureDesire = new IdentityHashMap<>();
    /** Scratch set of lovers seen in the current reward pass, used to prune dead entries from natureDesire. */
    private final Set<Induvidual> natureSeen = Collections.newSetFromMap(new IdentityHashMap<Induvidual, Boolean>());
    /** Settlement humanoid population counted on the last reward pass (drives the % pilgrim cap). */
    private int naturePop = 0;
    /** Effective concurrent-pilgrim cap for the current pass, derived from {@link #naturePop}. */
    private int maxPilgrims = NATURE_MAX_PILGRIMS_MIN;

    /** Per-pilgrim episode state (mod-owned; the engine AI is driven via overwrite/interrupt). */
    private static final class Pilgrim {
        final int tx, ty;      // destination tile
        boolean standing;      // false = walking to the destination, true = lingering there
        double episodeTime;    // seconds since this pilgrimage began (watchdog)
        double standLeft;      // seconds left to linger once standing
        Pilgrim(int tx, int ty) { this.tx = tx; this.ty = ty; }
    }

    // --- Stage 1 nature-piety runtime state (all transient; nothing serialized, rebuilt at runtime) ---
    /** MONUMENT_NATURE blueprint, resolved lazily once the settlement exists; null if none registered. */
    private settlement.room.infra.monument.ROOM_MONUMENT natureMon;
    /** True once we have scanned MONUMENTS.all for the blueprint (so we scan only once). */
    private boolean natureMonResolved = false;
    /** Per-tile wild-tree proximity, 0..255; length SETT.TAREA. Null until first refresh. */
    private byte[] treeProx;
    private double natureTimer = 0;
    private double treeProxTimer = 0;

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

    /** WORLD_PLUNDER, resolved at init; null until then. */
    private Boostable worldPlunder;

    /** CIVIC_INDOCTRINATION, resolved at init; null until then. */
    private Boostable civicIndoctrination;

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
    public void initBeforeGameCreated() {
        // TARGET_RACE / TARGET_CLASS tech-filter feature (moved here from the
        // standalone target-race-tech mod, generalized to also cover TARGET_CLASS).
        // Pre-scan tech files now — runs before new TECHS() parses them, so custom
        // keys never trip the engine's unknown-key warning. See your.mod.targetfilter.
        your.mod.targetfilter.TargetFilters.scan();
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

        // WORLD_PLUNDER: increases resources gained from raiding with your armies on enemy territory.
        // (Distinct from vanilla CIVIC_RAIDING / "Raid Security", which lowers the chance of being raided.)
        // Registered under the engine's world category (BoostableCat.ALL().WORLD — prefix "WORLD_",
        // TYPE_WORLD) so it groups with the other WORLD_* boostables in the World boost panel rather than
        // the city Civics panel. Renamed from CIVIC_PLUNDER 2026-07-04 (the effect is unchanged: read via
        // the player faction, type-agnostic).
        worldPlunder = ensureBoostable(PLUNDER_KEY, "PLUNDER", "Raid Plunder",
                "Multiplies the resources your armies plunder while raiding enemy territory.",
                UI.icons().s.sword, BoostableCat.ALL().WORLD);

        // CIVIC_INDOCTRINATION: increases the effectiveness (gain rate) of indoctrinating subjects.
        // Indoctrination is accumulated in the seam-less StatsEducation educate path, but a university's
        // learningSpeed = bonus().get(subject) * degrade * boosts (RoomEducationHelper.learningSpeed), so a
        // conditional factor on each university room's bonus() boostable scales the gain — for exactly the
        // subjects whose (class, race) education policy is INDOCTRINATION (see registerIndoctrinationEffect).
        civicIndoctrination = ensureBoostable(INDOCTRINATION_KEY, "INDOCTRINATION", "Indoctrination",
                "Multiplies how quickly subjects on the indoctrination policy are indoctrinated at universities.",
                UI.icons().s.admin, BOOSTABLES.CIVICS());
        if (civicIndoctrination != null) {
            registerIndoctrinationEffect(civicIndoctrination);
        }

        // ROOM_*_ALL umbrella keys: one tooltip line that cascades to every matching room boostable.
        registerUmbrellaCascade(MINE_ALL_KEY, "MINE_ALL", "Mines (All)", "ROOM_MINE_",
                "Affects every Mine room type at once.", BOOSTABLES.ROOMS(), null);
        registerUmbrellaCascade(WORKSHOP_ALL_KEY, "WORKSHOP_ALL", "Workshops (All)", "ROOM_WORKSHOP_",
                "Affects every Workshop room type at once.", BOOSTABLES.ROOMS(), null);
        registerUmbrellaCascade(FARM_ALL_KEY, "FARM_ALL", "Farms (All)", "ROOM_FARM_",
                "Affects every Farm room type at once.", BOOSTABLES.ROOMS(), null);
        registerUmbrellaCascade(REFINER_ALL_KEY, "REFINER_ALL", "Refineries (All)", "ROOM_REFINER_",
                "Affects every Refinery room type at once.", BOOSTABLES.ROOMS(), null);

        // WORLD_PRODUCTION_SLAVE_ALL umbrella: cascades to the per-race WORLD_PRODUCTION_SLAVE_<RACE>
        // region-output keys (dynamically discovered, so new races are auto-included). Registered under the
        // engine's World: Production category (prefix "WORLD_" -> key WORLD_PRODUCTION_SLAVE_ALL). The
        // hidden per-race "_YEARLY" display-derivative variants are excluded (per user).
        registerUmbrellaCascade(SLAVE_PRODUCTION_ALL_KEY, "PRODUCTION_SLAVE_ALL", "Production: Slaves (All)",
                "WORLD_PRODUCTION_SLAVE_", "Affects the production of slaves (all races).",
                BoostableCat.ALL().WORLD_PRODUCTION, "_YEARLY");

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

        // RATES_NATURE "Piety (Nature)": a per-subject nature-desire multiplier (>1 seeks nature, <1 shuns,
        // 1 neutral), registered in the vanilla RATES_ "Service Needs" category (NEEDS.bCat() — public
        // static, confirmed v71.40) so it groups with the other need-rates in the boostable UI. The 8-arg
        // ensureBoostable floors get() at NATURE_MIN (0.01) so a race authoring RATES_NATURE>MUL:0 can never
        // drive the value to 0. Base 1.0. It is NOT an engine need — nothing in the engine reads it; the
        // per-tick nature-piety routine in createInstance().update() is the sole consumer (SPEC §1/§4).
        ratesNature = ensureBoostable(NATURE_KEY, "NATURE", "Piety (Nature)",
                "The rate at which the need of Worship (Nature) increases daily. Subjects worship at Nature "
              + "monuments or natural trees to fulfill this service. Fulfilling this service grants "
              + "Piety (Shrine) fulfillment.",
                UI.icons().s.sprout, NEEDS.bCat(), 1.0, NATURE_MIN);

        // ===== CLASS_* "Class Treatment" registration SHELVED (2026-07-04) — see the constants block =====
        // New "CLASS_" BoostableCat (prefix applied by BOOSTING.push). In-game names follow
        // "<ClassName-plural> (<aspect>)" -> "Plebeians (Needs)", "Plebeians (Mining)". registerClassKey
        // wires each key to the three need-rates, plus — for a room-typed key — the ROOM_<ROOMTYPE>_ALL
        // umbrella (registered just above, so it already exists).
        // BoostableCat classCat = new BoostableCat("CLASS_", "Class Treatment", "",
        //         BoostableCat.TYPE_SETT, UI.icons().s.human);
        // registerClassKey(classCat, HCLASSES.CITIZEN(), null, null, null);                    // "Plebeians (Needs)"
        // registerClassKey(classCat, HCLASSES.CITIZEN(), "MINE", MINE_ALL_KEY, "Mining");       // "Plebeians (Mining)"
        // registerClassKey(classCat, HCLASSES.CITIZEN(), "FARM", FARM_ALL_KEY, "Farming");
        // registerClassKey(classCat, HCLASSES.CITIZEN(), "REFINER", REFINER_ALL_KEY, "Refining");
        // registerClassKey(classCat, HCLASSES.CITIZEN(), "WORKSHOP", WORKSHOP_ALL_KEY, "Crafting");

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

        // TARGET_RACE / TARGET_CLASS: queue the tech-boost rewriter on
        // BOOSTING.waiting and register the UI re-add connecter. Must run at this
        // hook (BoostSpecs resolve at the start of BOOSTING.finishSetup, which
        // INIT.finish() calls after this method returns). See your.mod.targetfilter.
        your.mod.targetfilter.TargetFilters.install();
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

    /**
     * 8-arg overload: like the 7-arg form but forwards a {@code minValue} to the 7-arg
     * {@link BOOSTING#push(String, double, CharSequence, CharSequence, SPRITE, BoostableCat, double)}
     * ({@code BOOSTING.java:109}), which floors the computed {@code get()} so multiplicative authoring
     * (e.g. {@code RATES_NATURE>MUL: 0}) can never drive the value to 0 and feed the engine's unguarded
     * x/0 tooltip math (see SPEC_CLASS_KEYS.md §6). Keeps the idempotent {@code tryGet(fullKey)} guard.
     */
    private static Boostable ensureBoostable(String fullKey, String pushKey, String name, String desc, SPRITE icon, BoostableCat cat, double baseValue, double minValue) {
        if (BOOSTING.MAP().tryGet(fullKey) == null) {
            BOOSTING.push(pushKey, baseValue, name, desc, icon, cat, minValue);
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

    /**
     * Installs the CIVIC_INDOCTRINATION effect. Indoctrination is accumulated through the education path,
     * and a university's learning speed is {@code bonus().get(subject) * degrade * boosts}
     * ({@code RoomEducationHelper.learningSpeed(ins, subject.indu())}), so adding a conditional
     * multiplicative {@link Booster} to each university room's {@code bonus()} boostable scales the
     * learning speed, and therefore the indoctrination gain. The factor is {@code 1.0} (no-op) except for
     * subjects whose (class, race) education policy is INDOCTRINATION, where it equals
     * {@code CIVIC_INDOCTRINATION.get(player)} — so subjects on the plain EDUCATION policy are untouched
     * and the boost only applies while a subject is actually being indoctrinated. Same shape as the
     * SLAVER/umbrella effects: a {@link Booster} added to an existing engine {@link Boostable}.
     *
     * <p><b>v71.40 API.</b> The old {@code StatsEducation.policyIndoctor.is(race)} check was removed;
     * policy is now per-{@code (HCLASS, Race)} via {@code StatsEducation.policy(HCLASS, Race)}, which
     * returns a {@link StatsEducation.StatEducation}. We resolve the INDOCTRINATION policy instance once
     * (by {@code total.key()}, falling back to the fixed engine order [EDUCATION, INDOCTRINATION]) and the
     * booster compares each subject's policy against it by identity.
     *
     * <p>Schools compute learning speed without a {@code bonus()} factor, so child indoctrination there is
     * unaffected; universities are the covered venue.
     */
    private void registerIndoctrinationEffect(Boostable civic) {
        StatsEducation edu = STATS.EDUCATION();
        StatsEducation.StatEducation indoctrination = null;
        for (StatsEducation.StatEducation e : edu.all) {
            if ("INDOCTRINATION".equals(e.total.key())) { indoctrination = e; break; }
        }
        if (indoctrination == null && edu.all.size() > 1) // engine order is [EDUCATION, INDOCTRINATION]
            indoctrination = edu.all.get(1);
        if (indoctrination == null) {
            System.err.println("[sos-extended-boostables] " + INDOCTRINATION_KEY
                    + ": INDOCTRINATION education policy not found; effect disabled (key still registered).");
            return;
        }

        BSourceInfo info = new BSourceInfo("Indoctrination", UI.icons().s.admin);
        int patched = 0;
        for (ROOM_UNIVERSITY u : SETT.ROOMS().UNIVERSITIES) {
            Boostable bonus = u.bonus();
            if (bonus == null) continue;
            new IndoctrinationBooster(civic, indoctrination, info).add(bonus);
            patched++;
        }
        System.out.println("[sos-extended-boostables] " + INDOCTRINATION_KEY
                + " scales learning speed on " + patched + " university room type(s).");
    }

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
     * Registers a single umbrella boostable and makes its value cascade onto every existing boostable whose
     * key starts with {@code childPrefix} (skipping any whose key ends with {@code excludeSuffix}, when
     * non-null). A tech that boosts the umbrella shows ONE line ("Mines (All) *1.5") yet every matching
     * child is multiplied. The umbrella resolves per query target via the engine's boosting, so the cascade
     * is correct whatever the child is queried with — the employee {@code Induvidual} for room production,
     * or the {@code Region} for world production.
     *
     * <p>Children must already be registered when this runs. That holds at {@code initBeforeGameInited} for
     * both settlement boostables ({@code SETT}, GAME.&lt;init&gt; line 142) and world boostables
     * ({@code WORLD}/{@code RD}, line 158) — both are constructed before this hook (line 172).
     *
     * @param cat           the umbrella's category; its {@code prefix} + {@code pushKey} forms the key
     * @param excludeSuffix if non-null, child keys ending with this are NOT cascaded (e.g. "_YEARLY")
     */
    private void registerUmbrellaCascade(String fullKey, String pushKey, String name, String childPrefix,
            String desc, BoostableCat cat, String excludeSuffix) {
        // Collect children BEFORE pushing the umbrella so the umbrella can never be its own child.
        snake2d.util.sets.ArrayListGrower<Boostable> children = new snake2d.util.sets.ArrayListGrower<>();
        SPRITE icon = UI.icons().s.house;
        for (Boostable b : BOOSTING.ALL()) {
            if (b.key != null && b.key.startsWith(childPrefix) && !b.key.equals(fullKey)
                    && (excludeSuffix == null || !b.key.endsWith(excludeSuffix))) {
                if (children.size() == 0) icon = b.nativeIcon;
                children.add(b);
            }
        }

        Boostable umbrella = ensureBoostable(fullKey, pushKey, name, desc, icon, cat);
        if (umbrella == null) return;

        BSourceInfo info = new BSourceInfo(name, icon);
        for (Boostable child : children) {
            new UmbrellaBooster(umbrella, info).add(child);
        }
        System.out.println("[sos-extended-boostables] " + fullKey + " cascades to " + children.size() + " boostable(s).");
    }

    // ===== CLASS_* "Class Treatment" helpers SHELVED (2026-07-04) — see the constants block ===========
    // /**
    //  * Registers one CLASS_<CLASS>[_<ROOMTYPE>] "Class Treatment" boostable and wires its effect. Every
    //  * key scales the three need-growth rates (CLASS_RATE_TARGETS) for hclass; when roomAllKey != null it
    //  * additionally scales that class's room output by also targeting the ROOM_<ROOMTYPE>_ALL umbrella,
    //  * whose cascade carries the class factor into every matching room's per-employee read. roomType/
    //  * roomAllKey/activity are null for the bare per-class key (need-rates only).
    //  */
    // private void registerClassKey(BoostableCat cat, HCLASS hclass, String roomType, String roomAllKey, String activity) {
    //     String pushKey = (roomType == null) ? hclass.key : hclass.key + "_" + roomType;
    //     String fullKey = "CLASS_" + pushKey;
    //     // In-game name: "<ClassName-plural> (<aspect>)" — e.g. "Plebeians (Mining)". The bare per-class
    //     // key (need-rates only) has no room activity, so it uses "(Needs)".
    //     String aspect = (activity == null) ? "Needs" : activity;
    //     String display = hclass.names + " (" + aspect + ")";
    //
    //     SPRITE icon = UI.icons().s.human;
    //     String[] targets = CLASS_RATE_TARGETS;
    //     String desc = "Multiplies the hunger/thirst/shopping need-growth rates of your " + hclass.names + ".";
    //     if (roomAllKey != null) {
    //         Boostable roomAll = BOOSTING.MAP().tryGet(roomAllKey);
    //         if (roomAll == null) {
    //             System.err.println("[sos-extended-boostables] " + fullKey + " room umbrella not found: " + roomAllKey);
    //         } else {
    //             if (roomAll.nativeIcon != null) icon = roomAll.nativeIcon;
    //             targets = new String[CLASS_RATE_TARGETS.length + 1];
    //             System.arraycopy(CLASS_RATE_TARGETS, 0, targets, 0, CLASS_RATE_TARGETS.length);
    //             targets[CLASS_RATE_TARGETS.length] = roomAllKey;
    //             desc = "Multiplies the hunger/thirst/shopping need-growth rates of your " + hclass.names
    //                     + ", and their " + activity + " output.";
    //         }
    //     }
    //
    //     Boostable key = ensureBoostable(fullKey, pushKey, display, desc, icon, cat);
    //     if (key != null) {
    //         registerClassTreatment(key, hclass, targets, display);
    //     }
    // }
    //
    // /**
    //  * Installs a CLASS_<CLASS> "Class Treatment" effect: attaches a conditional multiplicative
    //  * ClassTreatmentBooster to each named target boostable. The booster scales the target only for
    //  * subjects whose HCLASS matches hclass (per the employee/subject Induvidual the engine passes at each
    //  * read-point); every other subject and every non-Induvidual query returns the neutral 1.0. The
    //  * multiplier is classKey.get(player) clamped to [CLASS_MIN, CLASS_MAX]. Same shape as the
    //  * SLAVER/umbrella/(shelved)indoctrination effects — a Booster added to an existing engine Boostable.
    //  *
    //  * Zero-multiply safety-net: targets whose baseValue == 0 are skipped (a multiplicative booster there
    //  * is a no-op and would add a tooltip line that can hit the engine's unguarded x/0 progress math). And
    //  * because the clamped factor is always in [0.5, 1.5], it can never turn a value into 0 nor a 0 into
    //  * non-zero, so the booster never introduces a divide-by-zero of its own.
    //  */
    // private void registerClassTreatment(Boostable classKey, HCLASS hclass, String[] targetKeys, String sourceLabel) {
    //     BSourceInfo info = new BSourceInfo(sourceLabel, classKey.nativeIcon);
    //     int n = 0;
    //     for (String key : targetKeys) {
    //         Boostable target = BOOSTING.MAP().tryGet(key);
    //         if (target == null) {
    //             System.err.println("[sos-extended-boostables] " + classKey.key + " target not found: " + key);
    //             continue;
    //         }
    //         if (target.baseValue == 0) { // zero-multiply safety-net: skip zero-base boostables
    //             System.out.println("[sos-extended-boostables] " + classKey.key + " skips zero-base boostable: " + key);
    //             continue;
    //         }
    //         new ClassTreatmentBooster(classKey, hclass, info).add(target);
    //         n++;
    //     }
    //     System.out.println("[sos-extended-boostables] " + classKey.key + " attached to " + n + " boostable(s).");
    // }

    @Override
    public SCRIPT_INSTANCE createInstance() {
        return new SCRIPT_INSTANCE() {
            private double timer = 0;
            /** One-shot guard for the "mod updated" event window; keeps retrying until VIEW is ready. */
            private boolean updateChecked = false;

            @Override
            public void update(double ds) {
                // Show the one-time "mod updated" changelog window on the first in-game tick after an
                // _Info VERSION change (see your.mod.update.UpdateNotifier). showIfUpdated() returns
                // false until the in-game VIEW exists, so keep trying until it finalizes the decision.
                if (!updateChecked && your.mod.update.UpdateNotifier.showIfUpdated())
                    updateChecked = true;

                timer -= ds;
                if (timer <= 0) {
                    timer = 4.0;
                    recomputeRatio();
                }
                handleRaidPlunder(ds);
                handleNaturePiety(ds);

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
                // Nature-piety state is transient — force a fresh monument resolve + tree-map rebuild
                // against the just-loaded settlement on the next tick.
                natureMon = null;
                natureMonResolved = false;
                treeProx = null;
                natureTimer = 0;
                treeProxTimer = 0;
                pilgrims.clear(); // any in-flight pilgrimages are transient; the engine resumes normal AI
                natureDesire.clear();
                natureSeen.clear();
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
     * Delivers supplemental raid spoils for WORLD_PLUNDER. Vanilla still loots its full 100% inside
     * WArmyState.raiding; here we additionally ship {@code (plunder-1)} times the same per-tick loot for
     * each player army actually in the raiding state. This leaves all vanilla mechanics intact
     * (raiding(), UI, sprites, devastation, population loss) and touches only the resource spoils, which is
     * exactly "resources gained from raiding". Battle-victory and conquest spoils are untouched.
     */
    private void handleRaidPlunder(double ds) {
        if (worldPlunder == null) return;
        Player p = FACTIONS.player();
        if (p == null || p.capitolRegion() == null) {
            if (!raidTimers.isEmpty()) raidTimers.clear();
            return;
        }
        double plunder = worldPlunder.get(p);
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

    // ===== RATES_NATURE "Piety (Nature)" — Stage 1 passive core ==========================
    // Mod-owned per-tick routine (the WORLD_PLUNDER precedent), because the engine's need/service/AI-plan
    // system is sealed to mods (SPEC §2). Each pass: read each citizen's affinity = RATES_NATURE.get(indu)
    // − base(1.0); nature-lovers (affinity > 0) standing near nature gain shrine (private-devotion) piety
    // ∝ affinity × natureValue. "Near nature" = the MONUMENT_NATURE spread env (O(1)) OR a mod-maintained
    // wild-tree proximity map (O(1) read, rebuilt every NATURE_TREE_REFRESH s). Water is never read.
    //
    // Aversion polarity (SPEC §6/§12): for v1, averse citizens (affinity < 0) simply gain nothing near
    // nature (no write) — the safe minimal choice; symmetric piety loss is a later balance decision.
    // Reward interplay (SPEC §8): the vanilla shrine AI re-clears ACCESS/QUALITY for citizens who actively
    // seek a shrine and find none, so our write is durably sticky only for citizens without shrine access.

    private void handleNaturePiety(double ds) {
        if (ratesNature == null) return;

        treeProxTimer -= ds;
        if (treeProxTimer <= 0) {
            treeProxTimer = NATURE_TREE_REFRESH;
            refreshTreeProximity();
        }

        // Stage 2: advance any in-flight pilgrimages every tick (cheap — bounded by the % pilgrim cap).
        if (NATURE_PILGRIMAGE_ENABLED && !pilgrims.isEmpty()) updatePilgrims(ds);

        natureTimer -= ds;
        if (natureTimer > 0) return;
        natureTimer = NATURE_PERIOD;

        if (!natureMonResolved) resolveNatureMon();
        // Nothing to reward against if neither nature source exists yet.
        if (natureMon == null && treeProx == null) return;

        var rel = STATS.RELIGION();
        double base = ratesNature.baseValue; // 1.0
        // Population-scaled pilgrim cap, from the count observed last pass (population changes slowly).
        maxPilgrims = (int) CLAMP.d(naturePop * NATURE_MAX_PILGRIMS_PCT,
                NATURE_MAX_PILGRIMS_MIN, NATURE_MAX_PILGRIMS_ABS);
        int popCount = 0;
        if (NATURE_PILGRIMAGE_ENABLED) natureSeen.clear();
        for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
            if (!(e instanceof Humanoid)) continue;
            Humanoid h = (Humanoid) e;
            Induvidual indu = h.indu();
            if (indu == null) continue;
            popCount++;
            double affinity = ratesNature.get(indu) - base;
            if (affinity <= 0) continue; // Stage 1: only nature-lovers are rewarded
            if (NATURE_PILGRIMAGE_ENABLED) natureSeen.add(indu);
            COORDINATE c = h.tc();
            if (c == null) continue;
            double nv = natureValue(c.x(), c.y());
            if (nv > NATURE_THRESHOLD) {
                // Near nature: gain shrine (private-devotion) piety ∝ affinity × proximity, and the
                // desire to seek nature is fully sated (reset) — like a satisfied engine need.
                double q = CLAMP.d(affinity * nv * NATURE_REWARD_SCALE, 0.0, 1.0);
                if (q > 0) rel.SHRINE.cheatSetTotal(indu, q);
                if (NATURE_PILGRIMAGE_ENABLED) natureDesire.remove(indu);
            } else if (NATURE_PILGRIMAGE_ENABLED) {
                // Away from nature: desire builds ∝ affinity (stronger lovers build it faster). When it
                // crosses the trigger, an idle lover sets out on a pilgrimage; desire is held at the
                // trigger until they actually reach nature (above branch), so a blocked launch (cap/busy/
                // unreachable) simply retries next pass rather than losing progress. This self-paces how
                // often citizens pilgrimage — the accumulator substitutes for the need-reset cadence a
                // native engine need (e.g. skinny-dip) would get for free.
                double desire = natureDesire.getOrDefault(indu, 0.0) + affinity * NATURE_DESIRE_GROWTH;
                if (desire >= NATURE_DESIRE_TRIGGER) {
                    maybeStartPilgrimage(h, affinity, c);
                    desire = NATURE_DESIRE_TRIGGER;
                }
                natureDesire.put(indu, desire);
            }
        }
        // Prune desire entries for lovers who died / left this pass (mirrors the raidTimers retainAll).
        if (NATURE_PILGRIMAGE_ENABLED) natureDesire.keySet().retainAll(natureSeen);
        naturePop = popCount; // feeds next pass's population-scaled pilgrim cap
    }

    // ===== RATES_NATURE — Stage 2 active pilgrimage (the fragile part; SPEC §7) =========
    // Commandeer only genuinely idle nature-lovers whose accumulated desire has crossed the trigger, via
    // the PUBLIC AIManager surface: walk them to the nearest actual nature tile, let them linger, then
    // RELEASE (interrupt) so their real needs are never starved. Frequency is self-paced by the desire
    // accumulator (grows away from nature ∝ affinity, resets on arrival); a population-scaled cap (% of
    // pop) + a per-episode watchdog bound how many run at once. Uses overwrite(sub)+poll (re-issuing if idle
    // knocks it off) rather than a PLANRES plan, to stay within verified primitives. Master-switchable.

    /** Try to launch a pilgrimage for an idle nature-lover (desire-gated by the caller) away from nature. */
    private void maybeStartPilgrimage(Humanoid h, double affinity, COORDINATE c) {
        if (affinity < NATURE_PILGRIM_MIN_AFFINITY) return;
        if (pilgrims.size() >= maxPilgrims) return;
        if (pilgrims.containsKey(h) || h.isRemoved()) return;
        AIManager d = (AIManager) h.ai();
        if (!AI.modules().idle.is(h, d)) return; // only genuinely idle citizens
        int dest = findNearestNature(c.x(), c.y());
        if (dest < 0) return;
        int w = SETT.TWIDTH, tx = dest % w, ty = dest / w;
        AISUB.AISubActivation walk = AI.SUBS().walkTo.coo(h, d, tx, ty);
        if (walk == null) return; // unreachable — skip this citizen this round
        d.overwrite(h, walk);
        pilgrims.put(h, new Pilgrim(tx, ty));
    }

    /** Advance/finish every active pilgrimage. Runs every tick; releases on arrival-timeout or watchdog. */
    private void updatePilgrims(double ds) {
        java.util.Iterator<java.util.Map.Entry<Humanoid, Pilgrim>> it = pilgrims.entrySet().iterator();
        while (it.hasNext()) {
            java.util.Map.Entry<Humanoid, Pilgrim> en = it.next();
            Humanoid h = en.getKey();
            Pilgrim p = en.getValue();
            if (h.isRemoved() || h.indu() == null) { it.remove(); continue; }
            AIManager d = (AIManager) h.ai();
            p.episodeTime += ds;
            if (p.episodeTime > NATURE_PILGRIM_MAX_EPISODE) { // watchdog: never pin a citizen
                h.interrupt();
                it.remove();
                continue;
            }
            if (!p.standing) {
                COORDINATE c = h.tc();
                boolean near = c != null
                        && Math.max(Math.abs(c.x() - p.tx), Math.abs(c.y() - p.ty)) <= NATURE_PILGRIM_ARRIVE;
                if (near) {
                    d.overwrite(h, AI.SUBS().STAND.activateTime(h, d, NATURE_PILGRIM_STAND_SECS));
                    p.standing = true;
                    p.standLeft = NATURE_PILGRIM_STAND_SECS;
                } else if (!AI.SUBS().walkTo.isWalking(d)) {
                    // The idle module dropped our walk before arrival — re-issue to keep the pilgrim pinned.
                    AISUB.AISubActivation walk = AI.SUBS().walkTo.coo(h, d, p.tx, p.ty);
                    if (walk == null) { h.interrupt(); it.remove(); continue; }
                    d.overwrite(h, walk);
                }
                // else: still walking — let the engine run the sub to completion.
            } else {
                p.standLeft -= ds;
                if (p.standLeft <= 0) { h.interrupt(); it.remove(); }
            }
        }
    }

    /**
     * Nearest tile (as a {@code ty*TWIDTH+tx} index) with {@code natureValue > NATURE_PILGRIM_PICK} within
     * {@link #NATURE_PILGRIM_SEARCH_RADIUS}, searched ring by ring so the closest strong nature tile wins;
     * −1 if none. Only called on a pilgrimage trigger (≤ NATURE_MAX_PILGRIMS/period), so O(R²) is fine.
     */
    private int findNearestNature(int cx, int cy) {
        int w = SETT.TWIDTH, hgt = SETT.THEIGHT;
        for (int r = 1; r <= NATURE_PILGRIM_SEARCH_RADIUS; r++) {
            int best = -1;
            double bestv = NATURE_PILGRIM_PICK;
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    if (Math.max(Math.abs(dx), Math.abs(dy)) != r) continue; // perimeter of this ring only
                    int x = cx + dx, y = cy + dy;
                    if (x < 0 || y < 0 || x >= w || y >= hgt) continue;
                    double v = natureValue(x, y);
                    if (v > bestv) { bestv = v; best = y * w + x; }
                }
            }
            if (best >= 0) return best;
        }
        return -1;
    }

    /** Resolve (once) the MONUMENT_NATURE blueprint from the settlement's monument list; null if absent. */
    private void resolveNatureMon() {
        natureMonResolved = true;
        for (settlement.room.infra.monument.ROOM_MONUMENT m : SETT.ROOMS().MONUMENTS.all) {
            if ("MONUMENT_NATURE".equals(m.key())) {
                natureMon = m;
                return;
            }
        }
    }

    /**
     * Rebuild the wild-tree proximity map: mark every {@code TREES.isTree(x,y)} tile and stamp a
     * linear-falloff "nature" strength (0..255) out to {@link #NATURE_TREE_RADIUS} tiles, keeping the max.
     * Run infrequently ({@link #NATURE_TREE_REFRESH}); the per-citizen read ({@link #treeProximity}) is O(1).
     */
    private void refreshTreeProximity() {
        int w = SETT.TWIDTH, h = SETT.THEIGHT, area = SETT.TAREA;
        if (w <= 0 || h <= 0 || area <= 0) return; // settlement not ready
        if (treeProx == null || treeProx.length != area) treeProx = new byte[area];
        else java.util.Arrays.fill(treeProx, (byte) 0);

        var trees = SETT.TERRAIN().TREES;
        final int R = NATURE_TREE_RADIUS;
        for (int ty = 0; ty < h; ty++) {
            for (int tx = 0; tx < w; tx++) {
                if (!trees.isTree(tx, ty)) continue;
                int x0 = Math.max(0, tx - R), x1 = Math.min(w - 1, tx + R);
                int y0 = Math.max(0, ty - R), y1 = Math.min(h - 1, ty + R);
                for (int yy = y0; yy <= y1; yy++) {
                    for (int xx = x0; xx <= x1; xx++) {
                        int cheb = Math.max(Math.abs(xx - tx), Math.abs(yy - ty));
                        int strength = 255 - (cheb * 255) / (R + 1);
                        int idx = yy * w + xx;
                        if (strength > (treeProx[idx] & 0xFF)) treeProx[idx] = (byte) strength;
                    }
                }
            }
        }
    }

    /** Wild-tree proximity at a tile, 0..1 (0 if no map / out of bounds). */
    private double treeProximity(int x, int y) {
        if (treeProx == null) return 0;
        int w = SETT.TWIDTH, h = SETT.THEIGHT;
        if (x < 0 || y < 0 || x >= w || y >= h) return 0;
        return (treeProx[y * w + x] & 0xFF) / 255.0;
    }

    /** Max nature strength (0..1) at a tile: player-placed Nature Monument spread OR wild-tree proximity. */
    private double natureValue(int x, int y) {
        if (x < 0 || y < 0 || x >= SETT.TWIDTH || y >= SETT.THEIGHT) return 0;
        double v = treeProximity(x, y);
        if (natureMon != null) {
            double m = natureMon.envValue.get(x, y);
            if (m > v) v = m;
        }
        return v;
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

    // ===== CLASS_* "Class Treatment" booster SHELVED (2026-07-04) — see the constants block ===========
    // /**
    //  * A conditional multiplicative factor placed on a per-subject boostable (e.g. RATES_SHOPPING, or a
    //  * ROOM_<TYPE>_ALL umbrella). Its value is CLASS_<CLASS>.get(player) clamped to [CLASS_MIN, CLASS_MAX]
    //  * for a subject whose HCLASS matches hclass, and 1.0 (a no-op) for every other subject and every
    //  * non-Induvidual query — so only the per-subject read of the targeted class is scaled. getValue is
    //  * identity (like UmbrellaBooster), so the multiplier applies as-is; pget routes the query object into
    //  * the BValue.
    //  */
    // private static final class ClassTreatmentBooster extends Booster {
    //     private final Boostable classKey;   // the CLASS_<CLASS> boostable
    //     private final HCLASS hclass;        // the population class this booster applies to
    //     private final BValue value;
    //
    //     ClassTreatmentBooster(Boostable classKey, HCLASS hclass, BSourceInfo info) {
    //         super(info, true); // multiplicative
    //         this.classKey = classKey;
    //         this.hclass = hclass;
    //         this.value = new BValue() {
    //             @Override public double vGet(Induvidual indu) { return factor(indu); }
    //             @Override public double vGet(HCLASS_RACE reg) { return 1.0; }
    //             @Override public double vGet(Player f) { return 1.0; }
    //             @Override public double vGet(FactionNPC f) { return 1.0; }
    //             @Override public double vGet(Region reg) { return 1.0; }
    //             @Override public double vGet(Div div) { return 1.0; }
    //         };
    //     }
    //
    //     /** Clamped class multiplier for subjects in hclass; neutral 1.0 for everyone else. */
    //     private double factor(Induvidual indu) {
    //         if (indu == null || indu.clas() != hclass) return 1.0;
    //         return CLAMP.d(classKey.get(FACTIONS.player()), CLASS_MIN, CLASS_MAX);
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

    /**
     * A multiplicative factor placed on a university's learning-speed {@code bonus()} boostable. Its value
     * is {@code CIVIC_INDOCTRINATION.get(player)} for a subject whose {@code (class, race)} education
     * policy is INDOCTRINATION, and {@code 1.0} (a no-op) for everyone else — so learning speed (and hence
     * indoctrination gain) is scaled exactly while a subject is being indoctrinated, and plain education is
     * never affected. {@code getValue} is identity (like {@link UmbrellaBooster}), so the multiplier is
     * applied as-is rather than being clamped to [0,1] the way {@code BoosterValue} would. Only the
     * per-Induvidual query (the one the educate path uses) carries the factor; faction/population-class
     * queries return the neutral 1.0.
     */
    private static final class IndoctrinationBooster extends Booster {
        private final Boostable civic;
        private final StatsEducation.StatEducation indoctrination; // the INDOCTRINATION education policy
        private final BValue value;

        IndoctrinationBooster(Boostable civic, StatsEducation.StatEducation indoctrination, BSourceInfo info) {
            super(info, true); // multiplicative
            this.civic = civic;
            this.indoctrination = indoctrination;
            this.value = new BValue() {
                @Override public double vGet(Induvidual indu) { return factor(indu); }
                @Override public double vGet(HCLASS_RACE reg) { return 1.0; }
                @Override public double vGet(Player f) { return 1.0; }
                @Override public double vGet(FactionNPC f) { return 1.0; }
                @Override public double vGet(Region reg) { return 1.0; }
                @Override public double vGet(Div div) { return 1.0; }
            };
        }

        /** The multiplier: the boostable value if the subject's policy is INDOCTRINATION, else 1.0. */
        private double factor(Induvidual indu) {
            if (indu == null) return 1.0;
            Race r = indu.race();
            if (r == null) return 1.0;
            if (STATS.EDUCATION().policy(indu.clas(), r) == indoctrination)
                return civic.get(FACTIONS.player());
            return 1.0;
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
            return o.boostableValue(value);
        }
    }
}
