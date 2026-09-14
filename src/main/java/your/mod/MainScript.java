package your.mod;

import game.boosting.*;
import game.GAME;
import game.nobility.Noble;
import game.nobility.NobleOffice;
import game.nobility.NOBLES;
import settlement.stats.colls.StatsNeeds;
import game.battle.div.Div;
import game.battle.thread.status.DivStatus;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import game.faction.royalty.Royalty;
import game.faction.royalty.opinion.ROPINION;
import game.boosting.superb.SuperBoostable;
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
// RATES_NATURE pilgrimage: SComponent + DIR back the O(1) reachability pre-check that keeps us from
// claiming a citizen for an unreachable nature tile (see THE CRASH TRAP by maybeStartPilgrimage).
import settlement.path.components.SComponent;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsEducation;
// STAT_WORK_RETIREMENT DISABLED (2026-06-27): STAT/STANDINGS/StatStanding used only by that feature.
// import settlement.stats.stat.STAT;
// import settlement.stats.standing.STANDINGS;
// import settlement.stats.standing.StatStanding;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
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
import java.util.LinkedHashMap;
import java.util.Map;
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

    /**
     * Full-key prefix of every consumption boostable the engine registers — both the per-room form
     * {@code ROOM_CONSUMPTION_<ROOMKEY>} ({@code RoomConsumption.java:84}) and the per-recipe form
     * {@code ROOM_CONSUMPTION_<ROOMKEY>_<i>} ({@code Industry.java:279}). The category is
     * {@code BOOSTABLES.CONSUMPTION()}, which *is* {@code BOOSTABLES.ROOMS()} (prefix {@code ROOM}), so the
     * push key we hand {@code BOOSTING.push} is everything after {@code ROOM_}.
     */
    private static final String CONSUMPTION_PREFIX = "ROOM_CONSUMPTION_";

    // WORLD_PRODUCTION_SLAVE_ALL: an umbrella over the per-race WORLD_PRODUCTION_SLAVE_<RACE> region-output
    // keys (same cascade pattern as ROOM_*_ALL). Excludes the hidden WORLD_PRODUCTION_SLAVE_<RACE>_YEARLY
    // display-derivative variants (per user, 2026-07-05).
    // NOTE: renamed 2026-08-02 from SLAVE_PRODUCTION_ALL_KEY to match its own value — the engine has a
    // SEPARATE "SLAVE_PRODUCTION_" family (see CAPTIVE_ALL_KEY below) and the old constant name pointed at
    // the wrong one of the two.
    private static final String WORLD_PRODUCTION_SLAVE_ALL_KEY = "WORLD_PRODUCTION_SLAVE_ALL";

    // SLAVE_PRODUCTION_ALL: an umbrella over the per-race SLAVE_PRODUCTION_<RACE> keys — the engine's
    // "Captives" family (Recipes.boostsSlave, prefix "SLAVE_PRODUCTION_", TYPE_WORLD), i.e. a region's
    // ability/target for producing captives of that race. This is a DIFFERENT family from
    // WORLD_PRODUCTION_SLAVE_<RACE> above (daily/yearly slave *production* output); the game ships both and
    // they are easy to confuse. Children are pushed one per RACES.all() with base 1
    // (Creator.java:109-132), so the umbrella is multiplicative like the ROOM_*_ALL ones and there are no
    // "_YEARLY" derivatives to exclude.
    private static final String CAPTIVE_ALL_KEY = "SLAVE_PRODUCTION_ALL";

    // CLASS_<CLASS>[_<ROOMTYPE>] "Class Treatment" keys (un-shelved 2026-07-11; CITIZEN + SLAVE live).
    // A conditional multiplicative factor applied to selected per-subject boostables, but ONLY for
    // subjects of the matching population class (HCLASS); every other subject/query gets the neutral 1.0
    // (same shape as the SLAVER/umbrella effects). The applied multiplier is clamped to
    // [CLASS_MIN, CLASS_MAX] so it can never reach 0. Every CLASS_<CLASS>* key scales the three
    // need-growth rates below; a room-typed CLASS_<CLASS>_<ROOMTYPE> also scales that class's output
    // across every room of the type via the ROOM_<ROOMTYPE>_ALL umbrella. Full design: SPEC_CLASS_KEYS.md;
    // player doc: KEYS.md "Class keys"; project memory #8 + reference_rates_need_semantics.
    private static final double CLASS_MIN = 0.5;
    private static final double CLASS_MAX = 1.5;
    /** The three per-subject need-growth rates every CLASS_<CLASS>* key scales. */
    private static final String[] CLASS_RATE_TARGETS = { "RATES_HUNGER", "RATES_THIRST", "RATES_SHOPPING" };

    // CLASS_NOBLE_<CATEGORY> / CLASS_NOBLE_ALL "noble office" keys (2026-07-29). Nobles don't work rooms;
    // they hold OFFICES (game.nobility.NobleOffice). Each office adds a contribution to a target boostable
    // (a room's bonus() worker-skill, or CIVIC_GOV for the Governor). These keys scale that office's OWN
    // contribution by [CLASS_MIN, CLASS_MAX]: CLASS_NOBLE_<CATEGORY> scales the offices of one category,
    // CLASS_NOBLE_ALL scales every office (both stack multiplicatively, each clamped). See registerNobleOffices.
    private static final String CLASS_NOBLE_ALL_KEY = "CLASS_NOBLE_ALL";
    // A noble's office effect scales with their own need-satisfaction ("contentment"): fully-satisfied =
    // ×1.0, fully-discontent = ×NOBLE_CONTENT_FLOOR, linear between. This is what makes lowering
    // CLASS_NOBLE ("Contentment", the need-rate key) a real cost — needier nobles run their offices worse.
    private static final double NOBLE_CONTENT_FLOOR = 0.5;

    // CIVIC_VASSAL_OPINION ("Vassal Loyalty"): additive opinion points that apply only to factions where
    // DIP.overlord(...) == the player. Base 0 (inert until content grants it), authored with >ADD. The key
    // is only a carrier — your.mod.vassal.VassalOpinionSpec, installed per game, is what reads it and feeds
    // it into the per-royalty opinion pipeline. See SPEC_VASSAL_OPINION.md / KEYS.md.
    private static final String VASSAL_OPINION_KEY = "CIVIC_VASSAL_OPINION";

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

    // NOTE (2026-07-30): PHYSICS_CLEANLINESS and 19 RATES_* "front" keys used to be registered here —
    // high-positive keys that divided a low-positive vanilla key so the tooltip coloured correctly.
    // They were REMOVED once your.mod.boostformat proved it can recolour a tech-node effect line
    // directly (BoostFormats.RULES -> INVERTED), which needs no new keys at all. Content boosts the
    // vanilla key again (PHYSICS_SOILING, RATES_HUNGER, ...). Recover from git history if the
    // format-override approach ever has to be abandoned; note it only covers the tech node, whereas
    // front keys read correctly on every surface.

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

    /** CIVIC_VASSAL_OPINION, resolved at init; null until then. */
    private Boostable vassalOpinion;

    /**
     * The {@code SuperBoostable<Royalty>} our vassal-opinion spec is currently installed on. Identity
     * guard only: {@code SuperBoostables} is rebuilt in the GAME constructor ({@code GAME.java:139}), so
     * every new game / load hands us a fresh object and we install again; if a future engine version ever
     * reuses the instance across {@code createInstance()} calls, this stops the spec stacking and doubling
     * the bonus. See {@code SPEC_VASSAL_OPINION.md} §6.1.
     */
    private SuperBoostable<Royalty> vassalOpinionInstalledOn;

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
        // Read only as roomSlaver.get(FACTIONS.player()) (inside registerSlaverEffect's per-subject
        // BValue), never with the subject itself — so a TARGET_RACE/TARGET_CLASS tech cannot filter it.
        your.mod.targetfilter.TargetFilters.markUnfilterable(roomSlaver);

        Boostable roomCannibal = ensureBoostable(CANNIBAL_KEY, "__CANNIBAL", "Cannibal",
                "Multiplies the amount of resources gained when a corpse is butchered at a Cannibal Room.",
                SETT.ROOMS().CANNIBAL.icon, BOOSTABLES.ROOMS());
        if (roomCannibal != null) {
            registerCannibalEffect(roomCannibal);
        }
        // Read only as boost.get(FACTIONS.player()) in BoostedResAmount.amount() — butchering yield is
        // a per-corpse resource amount with no subject attached, so there is nothing to filter on.
        your.mod.targetfilter.TargetFilters.markUnfilterable(roomCannibal);

        // WORLD_PLUNDER: increases resources gained from raiding with your armies on enemy territory.
        // (Distinct from vanilla CIVIC_RAIDING / "Raid Security", which lowers the chance of being raided.)
        // Registered under the engine's world category (BoostableCat.ALL().WORLD — prefix "WORLD_",
        // TYPE_WORLD) so it groups with the other WORLD_* boostables in the World boost panel rather than
        // the city Civics panel. Renamed from CIVIC_PLUNDER 2026-07-04 (the effect is unchanged: read via
        // the player faction, type-agnostic).
        worldPlunder = ensureBoostable(PLUNDER_KEY, "PLUNDER", "Raid Plunder",
                "Multiplies the resources your armies plunder while raiding enemy territory.",
                UI.icons().s.sword, BoostableCat.ALL().WORLD);
        // Read only as worldPlunder.get(player) in handleRaidPlunder — faction-scoped, no subject.
        your.mod.targetfilter.TargetFilters.markUnfilterable(worldPlunder);

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
        // IndoctrinationBooster already does the per-subject test itself (policy == INDOCTRINATION) and
        // reads the key at civic.get(FACTIONS.player()), so the key never sees an Induvidual.
        your.mod.targetfilter.TargetFilters.markUnfilterable(civicIndoctrination);

        // ROOM_*_ALL umbrella keys: one tooltip line that cascades to every matching room boostable.
        registerUmbrellaCascade(MINE_ALL_KEY, "MINE_ALL", "Mines (All)", "ROOM_MINE_",
                "Affects every Mine room type at once.", BOOSTABLES.ROOMS(), null);
        registerUmbrellaCascade(WORKSHOP_ALL_KEY, "WORKSHOP_ALL", "Workshops (All)", "ROOM_WORKSHOP_",
                "Affects every Workshop room type at once.", BOOSTABLES.ROOMS(), null);
        registerUmbrellaCascade(FARM_ALL_KEY, "FARM_ALL", "Farms (All)", "ROOM_FARM_",
                "Affects every Farm room type at once.", BOOSTABLES.ROOMS(), null);
        registerUmbrellaCascade(REFINER_ALL_KEY, "REFINER_ALL", "Refineries (All)", "ROOM_REFINER_",
                "Affects every Refinery room type at once.", BOOSTABLES.ROOMS(), null);

        // ROOM_CONSUMPTION_<ROOMKEY>_ALL umbrellas: one key per multi-recipe room, standing in for that
        // room's whole per-recipe consumption set. Discovered from the engine's own keys (see the method).
        registerConsumptionUmbrellas();

        // WORLD_PRODUCTION_SLAVE_ALL umbrella: cascades to the per-race WORLD_PRODUCTION_SLAVE_<RACE>
        // region-output keys (dynamically discovered, so new races are auto-included). Registered under the
        // engine's World: Production category (prefix "WORLD_" -> key WORLD_PRODUCTION_SLAVE_ALL). The
        // hidden per-race "_YEARLY" display-derivative variants are excluded (per user).
        registerUmbrellaCascade(WORLD_PRODUCTION_SLAVE_ALL_KEY, "PRODUCTION_SLAVE_ALL", "Production: Slaves (All)",
                "WORLD_PRODUCTION_SLAVE_", "Affects the production of slaves (all races).",
                BoostableCat.ALL().WORLD_PRODUCTION, "_YEARLY");

        // SLAVE_PRODUCTION_ALL umbrella: cascades to the per-race SLAVE_PRODUCTION_<RACE> "Captives" keys.
        // Registered into the engine's OWN category for that family (SETT.RECIPES().boostsSlave, whose
        // prefix is "SLAVE_PRODUCTION_"), so the push key is just "ALL" -> full key SLAVE_PRODUCTION_ALL,
        // and the umbrella groups with its children under "Captives" in the World boost panel instead of
        // creating a near-duplicate category. Children are discovered dynamically, so races added by other
        // mods are auto-included. No excludeSuffix: unlike WORLD_PRODUCTION_SLAVE_*, this family has no
        // "_YEARLY" display derivatives — every key under the prefix is a real per-race child.
        // Safe at this hook: SETT (and its Recipes field, which pushes the children) is constructed in the
        // GAME ctor at GAME.java:143, before initBeforeGameInited() at GAME.java:173.
        registerUmbrellaCascade(CAPTIVE_ALL_KEY, "ALL", "Captives (All)",
                "SLAVE_PRODUCTION_", "Affects the ability to produce captives of every race at once.",
                SETT.RECIPES().boostsSlave, null);

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

        // CIVIC_VASSAL_OPINION ("Vassal Loyalty"): additive opinion points applied ONLY to factions that
        // are currently the player's vassals. Base 0 so it is a pure no-op until content grants it; author
        // with >ADD (>MUL is meaningless against base 0). The key itself is inert — the consumer is the
        // per-game VassalOpinionSpec installed from createInstance() (see installVassalOpinion).
        // Magnitude: CIVIC_OPINION's base is 1.5 and the vassal stance's reference threshold is 6, so 2-5
        // is the plausible authoring band. Full design: SPEC_VASSAL_OPINION.md.
        vassalOpinion = ensureBoostable(VASSAL_OPINION_KEY, "VASSAL_OPINION", "Vassal Loyalty",
                "Raises the opinion of factions that are your vassals, keeping their trust above the point "
              + "where they would turn on you.",
                UI.icons().s.noble, BOOSTABLES.CIVICS(), 0.0);
        // Same shape as vanilla CIVIC_OPINION: VassalOpinionSpec.get(Royalty) reads the key at
        // key.get(FACTIONS.player()), so no race/class filter can ever be evaluated against it.
        your.mod.targetfilter.TargetFilters.markUnfilterable(vassalOpinion);

        // CLASS_* "Class Treatment" registration (un-shelved 2026-07-11). New "CLASS_" BoostableCat
        // (prefix applied by BOOSTING.push). In-game names follow "<ClassName-plural> (<aspect>)" ->
        // "Plebeians (Needs)", "Plebeians (Mining)", "Slaves (Needs)", "Slaves (Mining)". registerClassKey
        // wires each key to the three need-rates, plus — for a room-typed key — the ROOM_<ROOMTYPE>_ALL
        // umbrella (registered just above, so it already exists).
        BoostableCat classCat = new BoostableCat("CLASS_", "Class Treatment", "",
                BoostableCat.TYPE_SETT, UI.icons().s.human);
        // CITIZEN (Plebeians): contentment key + the full room-output set (2026-08-04).
        registerClassKey(classCat, HCLASSES.CITIZEN(), null, null, null);                    // "Plebeians (Contentment)"
        registerClassKey(classCat, HCLASSES.CITIZEN(), "MINE", MINE_ALL_KEY, "Mining");
        registerClassKey(classCat, HCLASSES.CITIZEN(), "FARM", FARM_ALL_KEY, "Farming");
        registerClassKey(classCat, HCLASSES.CITIZEN(), "REFINER", REFINER_ALL_KEY, "Refining");
        registerClassKey(classCat, HCLASSES.CITIZEN(), "WORKSHOP", WORKSHOP_ALL_KEY, "Crafting");
        registerClassAllKey(classCat, HCLASSES.CITIZEN(), "All Work",
                MINE_ALL_KEY, FARM_ALL_KEY, REFINER_ALL_KEY, WORKSHOP_ALL_KEY);              // "Plebeians (All Work)"
        // SLAVE (Slaves): same full set (2026-08-04).
        registerClassKey(classCat, HCLASSES.SLAVE(), null, null, null);                      // "Slaves (Contentment)"
        registerClassKey(classCat, HCLASSES.SLAVE(), "MINE", MINE_ALL_KEY, "Mining");
        registerClassKey(classCat, HCLASSES.SLAVE(), "FARM", FARM_ALL_KEY, "Farming");
        registerClassKey(classCat, HCLASSES.SLAVE(), "REFINER", REFINER_ALL_KEY, "Refining");
        registerClassKey(classCat, HCLASSES.SLAVE(), "WORKSHOP", WORKSHOP_ALL_KEY, "Crafting");
        registerClassAllKey(classCat, HCLASSES.SLAVE(), "All Work",
                MINE_ALL_KEY, FARM_ALL_KEY, REFINER_ALL_KEY, WORKSHOP_ALL_KEY);              // "Slaves (All Work)"
        // NOBLE (Nobles): the bare needs key (no per-room variants — nobles don't work rooms). Added
        // 2026-07-29; display "Nobles (Needs)".
        registerClassKey(classCat, HCLASSES.NOBLE(), null, null, null);
        // NOBLE office keys: instead of per-room-type, nobles get per-OFFICE-category keys +
        // CLASS_NOBLE_ALL, each scaling the noble office's own contribution (2026-07-29).
        registerNobleOffices(classCat);

        // POPULATION_<RACE>_<CLASS>_OFCLASS_F GVALUEs: restore the v70 per-class race-fraction meaning
        // that v71 silently changed, so dependent tech REQUIRES (e.g. CaC monorace techs) resolve.
        // MUST run here in initBeforeGameInited — the GAME ctor calls this immediately before
        // init.finish() resolves the tech promises. (Previously this call was misplaced inside the
        // per-tick handleRaidPlunder, which early-returns when plunder<=1.0, so it never ran.)
        registerCitizenRaceFractions();

        // NOTE (2026-07-29): the your.mod.boostcolor recolor agent used to self-attach here. It was
        // removed — self-attach is impossible on the game's bundled JRE (no jdk.attach module), and the
        // Low-Positive coloring problem is solved instead by your.mod.boostformat, which re-colours the
        // tech-node effect line directly. Recover the agent from git history if it is ever revived.

        // TARGET_RACE / TARGET_CLASS: queue the tech-boost rewriter on
        // BOOSTING.waiting and register the UI re-add connecter. Must run at this
        // hook (BoostSpecs resolve at the start of BOOSTING.finishSetup, which
        // INIT.finish() calls after this method returns). See your.mod.targetfilter.
        your.mod.targetfilter.TargetFilters.install();

        // Tech-tooltip colour overrides (ACTIVITY_* -> neutral). The tech node renders each effect via
        // booster.format(..) — a virtual call on the booster — so swapping in a display-only wrapper
        // controls that line's colour. MUST be installed AFTER TargetFilters so our connecter is queued
        // after the one that re-adds original specs for the UI (otherwise those arrive unwrapped).
        // See your.mod.boostformat.BoostFormats.
        your.mod.boostformat.BoostFormats.install();

        // Tech effect-list ordering (benefits, then costs, then neutral). Ported here 2026-07-31 from
        // the standalone tech-boost-sort jar because it must agree with the polarity table above — a
        // low-positive key raised above its neutral point is a COST and has to sort with the negatives.
        // Registered last so it runs after the colour swap. NOTE: the old tech-boost-sort.jar must not
        // be loaded alongside this — both reorder the same list and whichever connecter runs last wins.
        your.mod.boostformat.BoostOrder.install();

        // BEHAVIOUR_STEALTH / BEHAVIOUR_ALERTNESS: opt-in only (see your.mod.stealth.StealthAlertnessConfig
        // — a hosting mod like a Heroes-style dependant must ship V<major>/StealthAlertness.txt with
        // ENABLED: true). No-op, and no keys registered, unless that flag is found. See KEYS.md.
        your.mod.stealth.StealthAlertnessBoostables.install();
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

    /**
     * Registers one {@code ROOM_CONSUMPTION_<ROOMKEY>_ALL} umbrella for <b>every</b> room whose consumption
     * the engine registered per recipe, and cascades it to that room's whole
     * {@code ROOM_CONSUMPTION_<ROOMKEY>_<i>} set. Authoring the umbrella once is then equivalent to writing
     * every numbered child — {@code ROOM_CONSUMPTION_WORKSHOP_RATION_ALL>MUL: 1.5} covers Rationmaker
     * recipes I–VI — but it is a single key, so a tech shows <b>one</b> effect line instead of six.
     *
     * <p><b>Rooms with only one recipe get an umbrella too</b> (user decision 2026-08-05). It is redundant
     * on the day it is registered, but the cascade is rebuilt from the live key set every load, so it
     * silently starts covering a second recipe the moment a patch or another mod adds one. That makes
     * {@code ROOM_CONSUMPTION_<ROOM>_ALL} a safe uniform target for content across every industry room,
     * with no per-room "does this one have an umbrella?" question and nothing to revisit later.
     *
     * <p><b>Where the children come from.</b> An industry room declares an {@code INDUSTRIES} array; for
     * each entry with both inputs and outputs the engine pushes
     * {@code CONSUMPTION_<blue.key>_<i>} into {@code BOOSTABLES.CONSUMPTION()}
     * ({@code Industry.createIndustries}, {@code settlement/room/industry/module/Industry.java:279}), named
     * <i>"&lt;Room&gt; Input: &lt;numeral&gt;"</i>. Rooms with a plain {@code CONSUMPTION} block instead get a
     * single unnumbered {@code ROOM_CONSUMPTION_<blue.key>} ({@code RoomConsumption.java:84}) — Administrations,
     * Laboratories, Libraries, Embassies. Those already are one key per room, so they get no umbrella.
     *
     * <p><b>Discovery, not a hardcoded list.</b> We group the existing keys by stripping the trailing
     * {@code _<digits>}, so a room added by another mod gets its own umbrella automatically. Vanilla v71.44
     * yields <b>15</b>: six multi-recipe — Bakery (2), Brewery (2), Carpenter (5), Rationmaker (6),
     * Smithy (5), Tailor (3) — and nine single-recipe (Charcoaler, Smelter, Weaver, Bowyer, Jeweller,
     * Masonry, Mechanic, Papermaker, Pottery).
     *
     * <p>Not covered, by design: the four rooms whose consumption comes from a plain {@code CONSUMPTION}
     * block rather than {@code INDUSTRIES} ({@code ROOM_CONSUMPTION_ADMIN_NORMAL},
     * {@code _LABORATORY_NORMAL}, {@code _LIBRARY_NORMAL}, {@code ROOM_CONSUMPTION__EMBASSY}). Those keys
     * carry no recipe index, and the engine never numbers them — a recipe list would come from the separate
     * {@code Industry} path and push its own {@code _<i>} keys, which this method would then pick up.
     *
     * <p><b>Polarity.</b> These keys are <b>high-positive</b>: the engine <i>divides</i> by them
     * ({@code IndustryUtil.calcConsumptionRate}, {@code IndustryUtil.java:42,51}), so a higher value means
     * less input consumed for the same output. The umbrella inherits that direction unchanged, which is why
     * it needs no {@code BoostFormats} colour rule (see the polarity table there).
     *
     * <p>Runs at {@code initBeforeGameInited}, after {@code SETT} (and therefore every room blueprint and its
     * industries) has been constructed in the GAME constructor — the same guarantee the {@code ROOM_*_ALL}
     * umbrellas rely on.
     */
    private void registerConsumptionUmbrellas() {
        // Pass 1: group the per-recipe children by room, keyed on the full key minus its trailing "_<i>".
        // Counting is enough to decide; we also keep the first child's display name to derive the
        // umbrella's (localisation-safe — we reuse whatever text the engine already produced).
        Map<String, Integer> counts = new LinkedHashMap<>();
        Map<String, String> firstChildName = new LinkedHashMap<>();
        for (Boostable b : BOOSTING.ALL()) {
            if (b.key == null || !b.key.startsWith(CONSUMPTION_PREFIX)) continue;
            int cut = b.key.lastIndexOf('_');
            if (cut < CONSUMPTION_PREFIX.length() + 1) continue;   // no room key between prefix and index
            if (!isRecipeIndex(b.key, cut + 1)) continue;          // unnumbered => per-room key, not a child
            String group = b.key.substring(0, cut);
            Integer n = counts.get(group);
            if (n == null) {
                counts.put(group, 1);
                firstChildName.put(group, "" + b.name);
            } else {
                counts.put(group, n + 1);
            }
        }

        // Pass 2: one umbrella per room, INCLUDING rooms that currently have only a single recipe (user
        // decision 2026-08-05). A single-recipe room's umbrella is redundant today — it cascades to that
        // one "_0" key — but the cascade is rebuilt from the live key set on every load, so the moment a
        // game update or another mod gives that room a second recipe the existing umbrella covers it with
        // no content change. Content can therefore author ROOM_CONSUMPTION_<ROOM>_ALL uniformly for every
        // industry room and never revisit it. The cost is one extra neutral x1.0 line on that room's
        // production breakdown while the umbrella is unboosted, same as the other umbrellas.
        int made = 0, single = 0;
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            String group = e.getKey();                                    // ROOM_CONSUMPTION_WORKSHOP_RATION
            int n = e.getValue();
            String roomKey = group.substring(CONSUMPTION_PREFIX.length()); // WORKSHOP_RATION
            String stem = nameStem(firstChildName.get(group));             // "Rationmaker Input"
            String desc = (n > 1)
                    ? "Affects all " + n + " of this room's input recipes at once, in place of the " + n
                            + " numbered \"" + stem + "\" keys."
                    : "Affects every input recipe of this room at once. This room currently has only one, "
                            + "but the key also covers any recipe a game update or another mod adds later.";
            registerUmbrellaCascade(group + "_ALL", "CONSUMPTION_" + roomKey + "_ALL", stem + ": All",
                    group + "_",
                    desc + " Higher means less resource consumed per unit produced; output is unaffected.",
                    BOOSTABLES.CONSUMPTION(), null);
            made++;
            if (n == 1) single++;
        }
        System.out.println("[sos-extended-boostables] ROOM_CONSUMPTION_*_ALL: registered " + made
                + " umbrella(s) — " + (made - single) + " multi-recipe, " + single
                + " single-recipe (future-proofed).");
    }

    /** True if {@code key} from {@code from} to its end is a non-empty run of digits (a recipe index). */
    private static boolean isRecipeIndex(String key, int from) {
        if (from >= key.length()) return false;
        for (int i = from; i < key.length(); i++) {
            if (key.charAt(i) < '0' || key.charAt(i) > '9') return false;
        }
        return true;
    }

    /**
     * The shared stem of a per-recipe consumption key's display name — everything before the trailing
     * {@code ": <numeral>"} the engine appends. {@code "Rationmaker Input: I"} → {@code "Rationmaker Input"}.
     * Taking the LAST colon keeps this correct even if a room's own name contains one; if the engine ever
     * stops appending a numeral, the whole name is used as the stem rather than mangling it.
     */
    private static String nameStem(String childName) {
        if (childName == null) return "Input";
        int c = childName.lastIndexOf(':');
        return c > 0 ? childName.substring(0, c) : childName;
    }

    /**
     * The display label for a class in CLASS_* key names/descriptions. Defaults to the vanilla plural
     * {@code hclass.names}, overridden where that reads awkwardly — NOBLE's vanilla plural is "Nobilities",
     * shown here as "Nobles".
     */
    private static String classDisplayName(HCLASS hclass) {
        if (hclass == HCLASSES.NOBLE()) return "Nobles";
        return "" + hclass.names;
    }

    /**
     * Registers one CLASS_&lt;CLASS&gt;[_&lt;ROOMTYPE&gt;] "Class Treatment" boostable and wires its
     * effect. Every key scales the three need-growth rates ({@link #CLASS_RATE_TARGETS}) for
     * {@code hclass}; when {@code roomAllKey != null} it additionally scales that class's room output by
     * also targeting the ROOM_&lt;ROOMTYPE&gt;_ALL umbrella, whose cascade carries the class factor into
     * every matching room's per-employee read. {@code roomType}/{@code roomAllKey}/{@code activity} are
     * {@code null} for the bare per-class key (need-rates only).
     */
    private void registerClassKey(BoostableCat cat, HCLASS hclass, String roomType, String roomAllKey, String activity) {
        String pushKey = (roomType == null) ? hclass.key : hclass.key + "_" + roomType;
        String fullKey = "CLASS_" + pushKey;
        // In-game name: "<ClassName-plural> (<aspect>)" — e.g. "Plebeians (Mining)". The bare per-class
        // key has no room activity, so it uses "(Contentment)" — see the polarity note below.
        String aspect = (activity == null) ? "Contentment" : activity;
        String label = classDisplayName(hclass);
        String display = label + " (" + aspect + ")";

        // Target set differs by key kind:
        //  - bare per-class key (roomAllKey == null): the three need-growth rates only.
        //  - room-typed key (roomAllKey != null): ONLY that class's room output (the ROOM_<TYPE>_ALL
        //    umbrella) — NOT the need rates. Room-typed keys are a pure skill/output boost; the neediness
        //    trade-off lives solely on the bare per-class key.
        // POLARITY (inverted in place 2026-07-30, user decision). The bare per-class key targets ONLY the
        // three need-growth rates, so before this change raising it made that class NEEDIER — a pure cost
        // that the engine nonetheless colored as a benefit (it colors from the number alone). The key now
        // DIVIDES those rates: higher = calmer = genuinely better, matching the color. Room-typed keys
        // are untouched (they were already high-positive: more output). Key strings are unchanged, so no
        // cross-mod reference breaks; only the direction of the effect flipped. (The vanilla low-positive
        // keys are handled by re-colouring instead — see your.mod.boostformat.BoostFormats.)
        SPRITE icon = UI.icons().s.human;
        String[] targets = CLASS_RATE_TARGETS;
        boolean invert = true; // bare per-class key: divide the need rates
        String desc = "How content your " + label + " are. Higher is better: it divides their "
                + "hunger/thirst/shopping need-growth rates, so x2 means those needs grow half as fast.";
        if (roomAllKey != null) {
            Boostable roomAll = BOOSTING.MAP().tryGet(roomAllKey);
            if (roomAll == null) {
                System.err.println("[sos-extended-boostables] " + fullKey + " room umbrella not found: " + roomAllKey);
                targets = new String[0];
            } else {
                if (roomAll.nativeIcon != null) icon = roomAll.nativeIcon;
                targets = new String[] { roomAllKey };
                invert = false; // room-typed key: already high-positive (more output)
                desc = "Multiplies your " + label + "' " + activity + " output.";
            }
        }

        // Max-cap policy: the bare CLASS_NOBLE (needs) key is UNCAPPED on the high end (user, 2026-07-29);
        // every other Class-Treatment key keeps the [CLASS_MIN, CLASS_MAX] band. Min floor is retained.
        double maxCap = (roomType == null && hclass == HCLASSES.NOBLE()) ? Double.MAX_VALUE : CLASS_MAX;

        Boostable key = ensureBoostable(fullKey, pushKey, display, desc, icon, cat);
        if (key != null) {
            registerClassTreatment(key, hclass, targets, display, maxCap, invert);
        }
    }

    /**
     * Registers a {@code CLASS_<CLASS>_ALL} room-output key that scales this class's output across MANY room
     * umbrellas at once (the citizen/slave analogue of {@code CLASS_NOBLE_ALL}) — e.g. all Mines + Farms +
     * Refineries + Workshops. Output-only (no need rates), capped like the other room-typed keys. Each
     * {@code roomAllKeys} entry is a {@code ROOM_<TYPE>_ALL} umbrella; missing/zero-base ones are skipped by
     * {@code registerClassTreatment}.
     */
    private void registerClassAllKey(BoostableCat cat, HCLASS hclass, String activity, String... roomAllKeys) {
        String pushKey = hclass.key + "_ALL";
        String fullKey = "CLASS_" + pushKey;
        String label = classDisplayName(hclass);
        String display = label + " (" + activity + ")";
        String desc = "Multiplies your " + label + "' output across all Mines, Farms, Refineries, and Workshops.";
        Boostable key = ensureBoostable(fullKey, pushKey, display, desc, UI.icons().s.human, cat);
        if (key != null) {
            registerClassTreatment(key, hclass, roomAllKeys, display, CLASS_MAX, false); // output-only, not inverted
        }
    }

    /**
     * Installs a CLASS_&lt;CLASS&gt; "Class Treatment" effect: attaches a conditional multiplicative
     * {@link ClassTreatmentBooster} to each named target boostable. The booster scales the target only for
     * subjects whose {@link HCLASS} matches {@code hclass} (per the employee/subject {@code Induvidual} the
     * engine passes at each read-point); every other subject and every non-Induvidual query returns the
     * neutral {@code 1.0}. The multiplier is {@code classKey.get(subject)} clamped to
     * [{@link #CLASS_MIN}, {@code maxCap}] — read with the subject's {@code Induvidual} so
     * {@code TARGET_RACE}/{@code TARGET_CLASS}-filtered grants are respected (the filter matches the
     * subject). Same shape as the SLAVER/umbrella effects — a {@link Booster} added to an existing engine
     * {@link Boostable}. {@code maxCap} is {@link #CLASS_MAX} for every key except the bare
     * {@code CLASS_NOBLE} needs key, which is uncapped ({@code Double.MAX_VALUE}) per user request.
     *
     * <p><b>Zero-multiply safety-net.</b> Targets whose {@code baseValue == 0} are skipped (a
     * multiplicative booster there is a no-op and would add a tooltip line that can hit the engine's
     * unguarded {@code x/0} progress math). And because the applied factor is floored at
     * {@link #CLASS_MIN} (0.5), it can never turn a value into 0 nor a 0 into non-zero, so the booster
     * never introduces a divide-by-zero of its own.
     */
    private void registerClassTreatment(Boostable classKey, HCLASS hclass, String[] targetKeys, String sourceLabel, double maxCap, boolean invert) {
        BSourceInfo info = new BSourceInfo(sourceLabel, classKey.nativeIcon);
        int n = 0;
        for (String key : targetKeys) {
            Boostable target = BOOSTING.MAP().tryGet(key);
            if (target == null) {
                System.err.println("[sos-extended-boostables] " + classKey.key + " target not found: " + key);
                continue;
            }
            if (target.baseValue == 0) { // zero-multiply safety-net: skip zero-base boostables
                System.out.println("[sos-extended-boostables] " + classKey.key + " skips zero-base boostable: " + key);
                continue;
            }
            new ClassTreatmentBooster(classKey, hclass, maxCap, invert, info).add(target);
            n++;
        }
        System.out.println("[sos-extended-boostables] " + classKey.key + " attached to " + n + " boostable(s)"
                + (invert ? " (inverted: divides the target)" : "")
                + (maxCap == Double.MAX_VALUE ? " (max cap removed)." : "."));
    }

    /**
     * Registers the noble-office "Class Treatment" keys: one {@code CLASS_NOBLE_<CATEGORY>} per office
     * category plus a global {@code CLASS_NOBLE_ALL}. Nobles hold {@link NobleOffice}s (not room jobs);
     * each office adds a contribution to a target boostable (a room's {@code bonus()} worker-skill, or
     * {@code CIVIC_GOV} for the Governor) equal to {@code office.add * clamp(office.value(allocations),0,1)}.
     * For every office we attach a {@link NobleOfficeBooster} to that target that adds the <em>supplement</em>
     * {@code contribution * (factor − 1)}, where {@code factor = clamp(CLASS_NOBLE_<CAT>) * clamp(CLASS_NOBLE_ALL)}
     * — so the net office contribution becomes {@code contribution * factor}, scaling ONLY the office's own
     * part (base skill, tech, and race stats are untouched). Both keys clamp to [{@link #CLASS_MIN},
     * {@link #CLASS_MAX}] and stack multiplicatively.
     *
     * <p>Offices are grouped into categories by their target boostable key (see {@link #nobleOfficeCategory}).
     * {@code GAME.NOBLE().OFFICES} already exists at this hook (NOBLES is constructed at GAME.&lt;init&gt;
     * line 167, before {@code initBeforeGameInited} at 173).
     *
     * <p><b>Accuracy note.</b> The supplement is additive, so it is exact for the buff case (factor ≥ 1,
     * the normal {@code >MUL} usage); for deflation (factor &lt; 1) combined with a multiplicative booster
     * on the same room bonus it is a close approximation (the reduction lands in the engine's additive
     * {@code sub} pool, which is not multiplied). Same replicate-and-scale trade-off as {@code WORLD_PLUNDER}.
     */
    private void registerNobleOffices(BoostableCat classCat) {
        if (GAME.NOBLE() == null) {
            System.err.println("[sos-extended-boostables] CLASS_NOBLE offices: GAME.NOBLE() unavailable; skipped.");
            return;
        }
        String nobles = "" + classDisplayName(HCLASSES.NOBLE());
        Boostable allKey = ensureBoostable(CLASS_NOBLE_ALL_KEY, "NOBLE_ALL", nobles + " (All Offices)",
                "Multiplies the effect of every office your " + nobles + " hold.", UI.icons().s.noble, classCat);

        Map<String, Boostable> catKeys = new LinkedHashMap<>();  // token -> CLASS_NOBLE_<token> boostable
        int offices = 0, attached = 0;
        for (NobleOffice office : GAME.NOBLE().OFFICES) {
            offices++;
            if (office == null || office.target == null) continue;
            String[] cat = nobleOfficeCategory(office);   // { token, aspectLabel }
            String token = cat[0];
            Boostable catKey = catKeys.get(token);
            if (catKey == null) {
                SPRITE icon = office.target.nativeIcon != null ? office.target.nativeIcon : UI.icons().s.noble;
                catKey = ensureBoostable("CLASS_NOBLE_" + token, "NOBLE_" + token, nobles + " (" + cat[1] + ")",
                        "Multiplies the effect of your " + nobles + "' " + cat[1] + " offices.", icon, classCat);
                catKeys.put(token, catKey);
            }
            if (catKey == null || allKey == null) continue;
            // Two additive display lines on the room's production breakdown: the contentment penalty and the
            // CLASS_NOBLE tech boost, itemised separately (they sum to the net office scaling).
            new NobleOfficeBooster(office, catKey, allKey, true).add(office.target);   // "Noble Contentment"
            new NobleOfficeBooster(office, catKey, allKey, false).add(office.target);  // "Noble Class Boost"
            attached++;
        }
        System.out.println("[sos-extended-boostables] CLASS_NOBLE offices: " + catKeys.size()
                + " category key(s) + CLASS_NOBLE_ALL, attached to " + attached + " of " + offices + " office(s).");
    }

    /**
     * Maps a {@link NobleOffice} to a {@code { keyToken, aspectLabel }} pair by its target boostable key.
     * The token becomes the {@code CLASS_NOBLE_<token>} suffix; the label is the in-game "(aspect)" text.
     * Unknown targets fall back to a generic {@code OFFICE / "Offices"} bucket so nothing is silently
     * dropped (they still get a category key and are covered by {@code CLASS_NOBLE_ALL}).
     */
    private static String[] nobleOfficeCategory(NobleOffice office) {
        String k = office.target.key;
        if (k == null) return new String[] { "OFFICE", "Offices" };
        if (k.equals("CIVIC_GOV"))            return new String[] { "GOVERNOR",   "Governing" };
        if (k.startsWith("ROOM_MINE_"))       return new String[] { "MINE",       "Mining" };
        if (k.startsWith("ROOM_FARM_"))       return new String[] { "FARM",       "Farming" };
        if (k.startsWith("ROOM_REFINER_"))    return new String[] { "REFINER",    "Refining" };
        if (k.startsWith("ROOM_WORKSHOP_"))   return new String[] { "WORKSHOP",   "Crafting" };
        if (k.startsWith("ROOM_ORCHARD"))     return new String[] { "ORCHARD",    "Orchards" };
        if (k.startsWith("ROOM_PASTURE"))     return new String[] { "PASTURE",    "Pastures" };
        if (k.startsWith("ROOM_FISHERY"))     return new String[] { "FISHERY",    "Fishing" };
        if (k.startsWith("ROOM_WOODCUTTER"))  return new String[] { "WOOD",       "Woodcutting" };
        if (k.startsWith("ROOM_EMBASSY"))     return new String[] { "EMBASSY",    "Diplomacy" };
        if (k.startsWith("ROOM_LIBRARY"))     return new String[] { "LIBRARY",    "Libraries" };
        if (k.startsWith("ROOM_LABORATORY"))  return new String[] { "LABORATORY", "Laboratories" };
        if (k.startsWith("ROOM_ADMIN"))       return new String[] { "ADMIN",      "Administration" };
        System.out.println("[sos-extended-boostables] CLASS_NOBLE: unmapped office target '" + k
                + "' -> generic OFFICE category.");
        return new String[] { "OFFICE", "Offices" };
    }

    /**
     * Installs the {@code CIVIC_VASSAL_OPINION} contribution onto this game's per-royalty opinion
     * pipeline. Must run from {@link #createInstance()} — "called once when leaving the main menu, and
     * once for every game load" ({@code script/SCRIPT.java:46-51}) — and NOT from
     * {@code initBeforeGameInited()}: {@code SuperBoostables} is constructed inside the GAME constructor
     * ({@code GAME.java:139}), so a spec installed at init time would attach to an object that is about to
     * be thrown away.
     *
     * <p>Constructing the spec IS the registration ({@code self.all.add(this)},
     * {@code SuperSpec.java:32}); the identity guard is what stops a second install stacking a second
     * copy. See {@code SPEC_VASSAL_OPINION.md} §6.
     */
    private void installVassalOpinion() {
        if (vassalOpinion == null) return;                 // key registration failed; stay inert
        SuperBoostable<Royalty> target = ROPINION.BOOST(); // == GAME.BOOSTS().OPINION, fresh per GAME
        if (target == null || target == vassalOpinionInstalledOn) return;
        vassalOpinionInstalledOn = target;
        new your.mod.vassal.VassalOpinionSpec(target, vassalOpinion,
                new BSourceInfo("Vassal Loyalty", UI.icons().s.noble));
    }

    @Override
    public SCRIPT_INSTANCE createInstance() {
        installVassalOpinion();
        return new SCRIPT_INSTANCE() {
            private double timer = 0;
            /** One-shot guard for the "mod updated" event window; keeps retrying until VIEW is ready. */
            private boolean updateChecked = false;
            /** Let the view settle for a few in-game frames before showing the update window. */
            private int updateSettle = 0;

            @Override
            public void update(double ds) {
                // Show one-time "mod updated" changelog windows shortly after entering a game, for THIS mod
                // and any other loaded mod that ships a V<major>/UpdateNotice.txt (see
                // your.mod.update.UpdateNotifier — a generic, opt-in-by-data service). We wait a few update
                // ticks so the load->gameplay transition is finished, then keep retrying until showPending()
                // finalizes (it returns false only while the in-game VIEW isn't ready yet).
                if (!updateChecked) {
                    if (updateSettle < 10) {
                        if (updateSettle == 0)
                            System.out.println("[sos-extended-boostables] update notice: instance update() ticking; settling before scan.");
                        updateSettle++;
                    } else if (your.mod.update.UpdateNotifier.showPending()) {
                        updateChecked = true;
                    }
                }

                timer -= ds;
                if (timer <= 0) {
                    timer = 4.0;
                    recomputeRatio();
                }
                handleRaidPlunder(ds);
                handleNaturePiety(ds);

                // Stealth/Alertness crime-detection contest (no-op unless config-enabled — see
                // your.mod.stealth.StealthAlertnessBoostables.install()).
                your.mod.stealth.CrimeStealthCheck.update(ds);

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
                // Stealth/Alertness growth + report cooldowns are ephemeral working state (see
                // StealthAlertnessBoostables class javadoc) — reset cleanly on every load.
                your.mod.stealth.StealthAlertnessBoostables.clearGrowthOnLoad();
                your.mod.stealth.CrimeStealthCheck.clearOnLoad();
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
    // pop) + a per-episode watchdog bound how many run at once. Uses overwrite(sub) rather than a mod-owned
    // AIPLAN.PLANRES, so nothing mod-defined is ever written into a save. Master-switchable.
    //
    // ⚠️⚠️ THE CRASH TRAP — read before touching anything below. (Fixed 2026-08-29; this was a CTD for
    // every player who unlocked a RATES_NATURE tech and then played long enough for a lover's desire to
    // cross the trigger.)
    //
    // AISUB_walkTo.coo() is NOT a query. It mutates the humanoid's SHARED AIManager.path *before* it knows
    // whether a route exists, and never restores it on failure:
    //     coo():            d.path.request(a.physics.tileC(), dx, dy);
    //                       if (d.path.isSuccessful()) return vanilla.activate(a, d);
    //                       return null;                                   // <- path left POISONED
    //     SPath.request():  this.successful = false;   ...search...   return successful;
    // So a failed coo() hands back null AND leaves d.path with successful == false. If that humanoid's
    // currently active AISUB is an engine PathWalker already mid-route, the next AIManager.update does:
    //     AIManager.update -> setNextState -> PathWalker.resume -> PathWalker$next.res -> path.setNext()
    //     SPath.setNext():  if (!successful) throw new RuntimeException();   // CTD @ SPath.java:357
    // The old gate (AI.modules().idle.is) did not protect against this: the idle module walks constantly —
    // it strolls (SubMove), steps out of the way (getOutofWay + walkTo.pathFull) and walks to benches
    // (walkTo.serviceInclude) — and every one of those is a PathWalker. Diagnosis verified line-for-line
    // against .claude/game-source-v71.44 (every frame of the reported stack matches exactly).
    //
    // THE RULE: never touch d.path — i.e. never call walkTo.coo() or overwrite() — on a humanoid the engine
    // still owns. h.interrupt() (= AIManager.overwrite(a, AI.plans().NOP)) is the seam that transfers
    // ownership: it cancels the live sub (running its abort(), which also releases any bench/service/
    // resource reservation that sub was holding — the old code leaked those), restores and cancels a
    // pending interruption, cancels the plan, and parks the citizen on NOP, whose sub is STAND and never
    // reads d.path. Only after that is d.path ours to clobber, and a failed request is then harmless
    // because nothing is consuming it.

    /** Try to launch a pilgrimage for an idle nature-lover (desire-gated by the caller) away from nature. */
    private void maybeStartPilgrimage(Humanoid h, double affinity, COORDINATE c) {
        if (affinity < NATURE_PILGRIM_MIN_AFFINITY) return;
        if (pilgrims.size() >= maxPilgrims) return;
        if (pilgrims.containsKey(h) || h.isRemoved()) return;
        AIManager d = (AIManager) h.ai();
        // Half-built or mid-interruption AI (AIManager.interrupt nulls sub until the interrupter installs
        // its own): not ours to touch — h.interrupt() would NPE on sub.cancel().
        if (d.plansub() == null || d.plan() == null) return;
        if (!AI.modules().idle.is(h, d)) return; // only genuinely idle citizens
        // Resolve a destination the pathfinder can plausibly reach BEFORE claiming the citizen, so a
        // hopeless target (a tree inside a solid forest blob, across a wall, another landmass) costs
        // nothing. A residual failure below is safe, this just keeps it rare.
        int dest = findNearestNature(c.x(), c.y());
        if (dest < 0) return;
        int w = SETT.TWIDTH, tx = dest % w, ty = dest / w;
        h.interrupt();               // take ownership FIRST — see THE CRASH TRAP. Must precede coo().
        AISUB.AISubActivation walk = AI.SUBS().walkTo.coo(h, d, tx, ty);
        if (walk == null) return;    // no route after all: d.path is dead but unowned, so harmless
        d.overwrite(h, walk);
        pilgrims.put(h, new Pilgrim(tx, ty));
    }

    /**
     * True while the citizen is still parked on the NOP plan we claimed them with. Once the engine has
     * handed them a real plan again (walk finished, sub failed, interruption, module switch) the episode is
     * over and we must not interrupt or re-drive them — they are doing something of their own again.
     */
    private static boolean pilgrimStillOurs(AIManager d) {
        return d.plan() == AI.plans().NOP;
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
            // Mid-interruption / half-built AI: h.interrupt() would NPE on sub.cancel(). Drop the episode.
            if (d.plansub() == null || d.plan() == null) { it.remove(); continue; }
            p.episodeTime += ds;
            if (p.episodeTime > NATURE_PILGRIM_MAX_EPISODE) { // watchdog: never pin a citizen
                if (pilgrimStillOurs(d)) h.interrupt();
                it.remove();
                continue;
            }
            if (!p.standing) {
                COORDINATE c = h.tc();
                boolean near = c != null
                        && Math.max(Math.abs(c.x() - p.tx), Math.abs(c.y() - p.ty)) <= NATURE_PILGRIM_ARRIVE;
                if (near) {
                    // Reached nature. If the engine has already reclaimed them, leave them alone — they are
                    // standing in nature either way, so the Stage-1 proximity pass still pays the piety.
                    if (!pilgrimStillOurs(d)) { it.remove(); continue; }
                    // Still ours: re-claim before swapping the sub, because our walk sub may have ended or
                    // been interrupted and we must never overwrite a sub in place without cancelling it.
                    h.interrupt();
                    d.overwrite(h, AI.SUBS().STAND.activateTime(h, d, NATURE_PILGRIM_STAND_SECS));
                    p.standing = true;
                    p.standLeft = NATURE_PILGRIM_STAND_SECS;
                } else if (!pilgrimStillOurs(d) || !AI.SUBS().walkTo.isWalking(d)) {
                    // The engine took the citizen back (walk failed, interruption, new plan) before they
                    // arrived. Do NOT re-issue the walk: they are engine-owned again, and coo() would
                    // poison d.path underneath whatever sub is now running (THE CRASH TRAP). End the
                    // episode; desire is still parked at the trigger, so the next reward pass simply
                    // re-launches through maybeStartPilgrimage, which claims them properly.
                    it.remove();
                    continue;
                }
                // else: still walking under our NOP claim — let the engine run the sub to completion.
            } else {
                p.standLeft -= ds;
                boolean ours = pilgrimStillOurs(d);
                if (p.standLeft <= 0 || !ours) {
                    // Worshipped long enough (or the engine reclaimed them). Hand control back cleanly;
                    // AIManager picks a fresh plan next tick, so real needs are never starved.
                    if (ours) h.interrupt();
                    it.remove();
                }
            }
        }
    }

    /**
     * Nearest tile (as a {@code ty*TWIDTH+tx} index) with {@code natureValue > NATURE_PILGRIM_PICK} within
     * {@link #NATURE_PILGRIM_SEARCH_RADIUS} that the citizen can actually path to, searched ring by ring so
     * the closest strong nature tile wins; −1 if none. Only called on a pilgrimage trigger
     * (≤ NATURE_MAX_PILGRIMS/period), so O(R²) is fine.
     */
    private int findNearestNature(int cx, int cy) {
        int w = SETT.TWIDTH, hgt = SETT.THEIGHT;
        SComponent from = SETT.PATH().comps.superComp.get(cx, cy);
        if (from == null) return -1; // walker is on an unwalkable/unconnected tile — nowhere to send them
        for (int r = 1; r <= NATURE_PILGRIM_SEARCH_RADIUS; r++) {
            int best = -1;
            double bestv = NATURE_PILGRIM_PICK;
            for (int dx = -r; dx <= r; dx++) {
                for (int dy = -r; dy <= r; dy++) {
                    if (Math.max(Math.abs(dx), Math.abs(dy)) != r) continue; // perimeter of this ring only
                    int x = cx + dx, y = cy + dy;
                    if (x < 0 || y < 0 || x >= w || y >= hgt) continue;
                    double v = natureValue(x, y);
                    if (v > bestv && pathReachable(from, x, y)) { bestv = v; best = y * w + x; }
                }
            }
            if (best >= 0) return best;
        }
        return -1;
    }

    /**
     * O(1) pre-check that {@code walkTo.coo(x,y)} has a realistic chance of finding a route. {@code coo}
     * requests a NON-full path, which may end on a tile orthogonally adjacent to the destination, so the
     * destination itself may be solid (a tree) as long as a neighbour is walkable and in the SAME path
     * super-component as the walker. Mirrors the engine's own guard in {@code AISUB_walkTo.room}. Not a
     * guarantee — but it stops us claiming a citizen (h.interrupt()) for a target nothing can reach.
     */
    private static boolean pathReachable(SComponent from, int tx, int ty) {
        var comps = SETT.PATH().comps.superComp;
        if (comps.get(tx, ty) == from) return true;
        for (int i = 0; i < DIR.ORTHO.size(); i++) {
            DIR dd = DIR.ORTHO.get(i);
            int x = tx + dd.x(), y = ty + dd.y();
            if (SETT.IN_BOUNDS(x, y) && comps.get(x, y) == from) return true;
        }
        return false;
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

    /**
     * A conditional multiplicative factor placed on a per-subject boostable (e.g. {@code RATES_SHOPPING},
     * or a {@code ROOM_<TYPE>_ALL} umbrella). Its value is {@code CLASS_<CLASS>.get(player)} clamped to
     * [{@link #CLASS_MIN}, {@link #CLASS_MAX}] for a subject whose {@link HCLASS} matches {@code hclass},
     * and {@code 1.0} (a no-op) for every other subject and every non-Induvidual query — so only the
     * per-subject read of the targeted class is scaled. {@code getValue} is identity (like
     * {@link UmbrellaBooster}), so the multiplier applies as-is; {@code pget} routes the query object into
     * the {@link BValue}.
     */
    private static final class ClassTreatmentBooster extends Booster {
        private final Boostable classKey;   // the CLASS_<CLASS> boostable
        private final HCLASS hclass;        // the population class this booster applies to
        private final double maxCap;        // upper clamp on the applied factor (Double.MAX_VALUE = uncapped)
        private final boolean invert;       // true = apply 1/value (need-rate targets; see registerClassKey)
        private final BValue value;

        ClassTreatmentBooster(Boostable classKey, HCLASS hclass, double maxCap, boolean invert, BSourceInfo info) {
            super(info, true); // multiplicative
            this.classKey = classKey;
            this.hclass = hclass;
            this.maxCap = maxCap;
            this.invert = invert;
            this.value = new BValue() {
                @Override public double vGet(Induvidual indu) { return factor(indu); }
                @Override public double vGet(HCLASS_RACE reg) { return 1.0; }
                @Override public double vGet(Player f) { return 1.0; }
                @Override public double vGet(FactionNPC f) { return 1.0; }
                @Override public double vGet(Region reg) { return 1.0; }
                @Override public double vGet(Div div) { return 1.0; }
            };
        }

        /**
         * Clamped class multiplier for subjects in {@code hclass}; neutral 1.0 for everyone else. Reads the
         * key with the SUBJECT'S {@code Induvidual} (not the player faction) so a {@code TARGET_RACE} /
         * {@code TARGET_CLASS}-filtered grant is respected — the filter matches this very subject; for an
         * unfiltered grant the per-Induvidual read resolves to the same faction/tech value as before.
         */
        private double factor(Induvidual indu) {
            if (indu == null || indu.clas() != hclass) return 1.0;
            double f = CLAMP.d(classKey.get(indu), CLASS_MIN, maxCap);
            // Inverted targets (the need-growth rates): raising the key must LOWER the rate, so apply
            // 1/f. f is floored at CLASS_MIN (0.5) so this can never divide by zero. Note the uncapped
            // bare CLASS_NOBLE key means its inverted factor tends toward 0 as the key grows — i.e.
            // noble needs can be driven arbitrarily close to nothing.
            return invert ? 1.0 / f : f;
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

    /**
     * An <b>additive</b> factor placed on a noble office's target boostable (a room's {@code bonus()}, or
     * {@code CIVIC_GOV} for the Governor) that scales ONLY that office's own contribution. The office adds
     * {@code C = office.add * clamp(office.value(allocations),0,1)} to the target. Each office gets TWO of
     * these boosters (a {@code contentmentPart} flag), so the room's production breakdown shows the effect
     * as two labelled additive lines — "Noble Contentment" = {@code C*(avgContent−1)} and "Noble Class Boost"
     * = {@code C*(avgEff−avgContent)} — that sum to the true net {@code C*(avgEff−1)}. {@code avgContent} and
     * {@code avgEff} are the allocation-weighted means over the office's holders of {@code contentment} and of
     * {@code techBoost × contentment}:
     * <ul>
     *   <li>{@code techBoost = clamp(catKey,[MIN,MAX]) * clamp(allKey,[MIN,MAX])} — the CLASS_NOBLE_&lt;CAT&gt;
     *       and CLASS_NOBLE_ALL tech multipliers.</li>
     *   <li>{@code contentment} — the noble's own need-satisfaction in [{@link #NOBLE_CONTENT_FLOOR}, 1]. So a
     *       discontent noble runs their office worse, and lowering {@code CLASS_NOBLE} (which speeds their
     *       needs) is a real cost. This is what nobles have instead of a happiness/loyalty stat.</li>
     * </ul>
     * Player-faction only; neutral {@code 0} for NPC factions (so only the player's offices are scaled). Uses
     * {@link BValue.BValueFaction} exactly like the engine's own office booster, so the supplement reaches the
     * per-worker reads, and it is memoised per update-tick ({@link #supplement}). The office's
     * {@code value()}/{@code allocations} read employment/noble state, {@code contentment} reads need STATs, and
     * the CLASS keys are distinct boostables from the target — so evaluating this inside the target's read is
     * re-entrancy-safe. {@code from()/to()} are 0 so the booster stays out of the displayed min/max range.
     *
     * <p><b>TARGET_RACE / TARGET_CLASS.</b> The scaling factor is evaluated against the OFFICE-HOLDING
     * NOBLE(S), not the room's workers — so a race/class-filtered grant only lifts the office effect of
     * nobles who pass the filter. The two CLASS keys are read with each holder's {@code Induvidual}
     * (so a {@code TargetFilteredBooster} matches that noble); the per-office factor is the
     * allocation-weighted average across the office's holders. Off-map holders (a Governor who left the
     * map, {@code subject()==null}) can't be race-tested, so they fall back to the unfiltered faction
     * value — meaning a race-filtered grant does not reach an off-map Governor.
     */
    private static final class NobleOfficeBooster extends Booster {
        private final NobleOffice office;
        private final Boostable catKey;   // CLASS_NOBLE_<CATEGORY>
        private final Boostable allKey;   // CLASS_NOBLE_ALL
        private final boolean contentmentPart; // true = "Noble Contentment" line; false = "Noble Class Boost" line
        private final BValue value;
        private int cachedTick = Integer.MIN_VALUE;   // per-update-tick memo (like the engine's own Boo)
        private double cachedSupp = 0.0;

        NobleOfficeBooster(NobleOffice office, Boostable catKey, Boostable allKey, boolean contentmentPart) {
            super(new BSourceInfo(contentmentPart ? "Noble Contentment" : "Noble Class Boost",
                    contentmentPart ? UI.icons().s.human : UI.icons().s.noble), false); // additive, display line
            this.office = office;
            this.catKey = catKey;
            this.allKey = allKey;
            this.contentmentPart = contentmentPart;
            this.value = new BValue.BValueFaction(office.target) {
                @Override public double vGet(Player f) { return supplement(); }
                @Override public double vGet(FactionNPC f) { return 0.0; }
            };
        }

        /** Per-update-tick memo of {@link #computeSupplement} — this booster sits on a hot per-worker
         *  read, so recompute at most once per game update (mirrors {@code BoostCompound.Boo}'s caching). */
        private double supplement() {
            int tick = GAME.updateI();
            if (tick != cachedTick) {
                cachedTick = tick;
                cachedSupp = computeSupplement();
            }
            return cachedSupp;
        }

        /**
         * {@code officeContribution * (avgFactor − 1)} for the player — the amount to ADD to the target,
         * so the net office contribution becomes {@code officeContribution * avgFactor}. {@code avgFactor}
         * is the allocation-weighted mean of each holding noble's {@link #nobleEffFactor} (tech boost ×
         * that noble's contentment), so race/class filters and per-noble discontent both scale the office
         * proportionally.
         */
        private double computeSupplement() {
            NOBLES nob = GAME.NOBLE();
            if (nob == null) return 0.0;
            int totalSlots = nob.allocations(office);
            if (totalSlots <= 0) return 0.0;
            double contribution = office.add * CLAMP.d(office.value(totalSlots), 0.0, 1.0);
            if (contribution == 0.0) return 0.0;

            double wContent = 0.0;   // Σ slots·content(n)
            double wEff = 0.0;       // Σ slots·tech(n)·content(n)
            int counted = 0;         // Σ slots(n)  (== totalSlots barring cache lag)
            for (Noble n : nob.active()) {
                if (n.office() != office) continue;
                int slots = 1 + NOBLES.RANK_INCREASE * n.rank();
                double tech = nobleTech(n);
                double content = nobleContent(n);
                wContent += slots * content;
                wEff += slots * tech * content;
                counted += slots;
            }
            if (counted <= 0) return 0.0;
            double avgContent = wContent / counted;   // allocation-weighted mean contentment
            double avgEff = wEff / counted;            // allocation-weighted mean (tech × contentment)
            // Two additive display lines whose sum is the true net C*(avgEff-1):
            //   Contentment line = C*(avgContent-1)      (the pure contentment penalty, tech-independent)
            //   Class-boost line = C*(avgEff-avgContent) (the CLASS_NOBLE tech boost, on top)
            return contribution * (contentmentPart ? (avgContent - 1.0) : (avgEff - avgContent));
        }

        /**
         * Tech-boost factor for one holder = {@code clamp(catKey) * clamp(allKey)}, read with the noble's
         * {@code Induvidual} so TARGET_RACE/TARGET_CLASS filters match the noble. Off-map holders (a Governor
         * who left the map) fall back to the unfiltered player value.
         */
        private double nobleTech(Noble n) {
            Humanoid h = n.subject();
            BOOSTABLE_O ctx = (h != null) ? h.indu() : FACTIONS.player();
            return CLAMP.d(catKey.get(ctx), CLASS_MIN, CLASS_MAX)
                    * CLAMP.d(allKey.get(ctx), CLASS_MIN, CLASS_MAX);
        }

        /** Contentment for one holder in [{@link #NOBLE_CONTENT_FLOOR}, 1]; off-map holders (whose needs
         *  aren't simulated) are treated as fully content. */
        private static double nobleContent(Noble n) {
            Humanoid h = n.subject();
            return (h != null) ? contentment(h.indu()) : 1.0;
        }

        /**
         * A noble's contentment in [{@link #NOBLE_CONTENT_FLOOR}, 1], from their satisfaction of the three
         * needs {@code CLASS_NOBLE} governs (hunger/thirst/shopping): each need's satisfaction is
         * {@code 1 - level/max}; the mean maps linearly onto [floor, 1]. Nobles have no engine
         * happiness/loyalty stat, so their unmet-need levels (computed per-subject by {@code StatsNeeds})
         * are the contentment signal — and the same axis {@code CLASS_NOBLE} moves via the need rates.
         */
        private static double contentment(Induvidual ind) {
            StatsNeeds needs = STATS.NEEDS();
            if (needs == null) return 1.0;
            double satSum = 0.0;
            int n = 0;
            for (StatsNeeds.StatNeedNormal sn : needs.SNEEDS) {
                if (!isClassRateKey(sn.need.rate.key)) continue;   // only the CLASS_NOBLE need axis
                int max = sn.stat().indu().max(ind);
                if (max <= 0) continue;
                double unmet = CLAMP.d((double) sn.stat().indu().get(ind) / max, 0.0, 1.0);
                satSum += 1.0 - unmet;
                n++;
            }
            double avgSat = (n > 0) ? satSum / n : 1.0;   // no needs found → treat as content
            return NOBLE_CONTENT_FLOOR + (1.0 - NOBLE_CONTENT_FLOOR) * avgSat;
        }

        /** True if {@code key} is one of {@link #CLASS_RATE_TARGETS} (the hunger/thirst/shopping rates). */
        private static boolean isClassRateKey(String key) {
            for (String k : CLASS_RATE_TARGETS) if (k.equals(key)) return true;
            return false;
        }

        @Override
        public double from() {
            return 0.0;
        }

        @Override
        public double to() {
            return 0.0;
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
