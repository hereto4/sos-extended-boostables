package your.mod.racegroup;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import game.faction.Faction;
import init.paths.ModInfo;
import init.paths.PATHS;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.UI;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.value.GVALUES;
import settlement.stats.POP;
import snake2d.util.file.Json;
import snake2d.util.sprite.SPRITE;
import util.data.DOUBLE_O;

/**
 * Race groups: named sets of races that act as a single pseudo-race in the {@code POPULATION_*}
 * requirement values (faction GVALUES, i.e. what tech/event/room {@code REQUIRES:} blocks query).
 *
 * <p>A group is declared in a data file that any loaded mod may ship at
 * <pre>{@code <modfolder>/V<major>/RaceGroups.txt}</pre>
 * — the same "any mod that ships a file" idiom as {@code your.mod.update.UpdateNotifier}
 * ({@code UpdateNotice.txt}) and {@code your.mod.stealth.StealthAlertnessConfig}
 * ({@code StealthAlertness.txt}). Format (game Json):
 * <pre>{@code
 * GAQUATIC: {
 *     NAME: "Aquatic",
 *     RACES: [VERATHID, KHELIAN, THRYXARII, ONARI,],
 * },
 * }</pre>
 *
 * <p>For each group {@code <G>} this registers, mirroring the vanilla per-race keys in
 * {@code settlement.stats.SValues} plus this mod's {@code _OFCLASS_F} variant:
 * <pre>
 *   POPULATION_&lt;G&gt;__F                 sum POP.tot(*, r)  / POP.tot(*, *)        (whole population)
 *   POPULATION_&lt;G&gt;__I                 sum POP.tot(*, r)
 *   POPULATION_&lt;G&gt;_&lt;CLASS&gt;_F          sum POP.tot(cl, r) / POP.tot(*, *)        (vanilla v71 meaning)
 *   POPULATION_&lt;G&gt;_&lt;CLASS&gt;_I          sum POP.tot(cl, r)
 *   POPULATION_&lt;G&gt;_&lt;CLASS&gt;_OFCLASS_F  sum POP.pop(cl, r) / POP.pop(cl, *)       (fraction OF THE CLASS)
 *   POPULATION_&lt;G&gt;__MIN_I / __MAX_I     min / max over r of POP.tot(*, r)       (per-race extremes)
 *   POPULATION_&lt;G&gt;_&lt;CLASS&gt;_MIN_I / _MAX_I  min / max over r of POP.tot(cl, r)
 * </pre>
 * for every player class (NOBLE, CITIZEN, SLAVE) — 19 keys per group. The {@code _OFCLASS_F} form is
 * the exact expression of {@code MainScript.registerCitizenRaceFractions} with the numerator summed
 * over the group, so {@code POPULATION_GAQUATIC_CITIZEN_OFCLASS_F >= 1} means "every citizen is one of
 * the group's races, in any mix". The {@code _MIN_I}/{@code _MAX_I} pair quantifies over the
 * <em>installed</em> members: {@code CITIZEN_MIN_I >= 150} = every member race has at least 150
 * citizens, {@code CITIZEN_MAX_I >= 150} = at least one does; both read 0 when no member is installed.
 * The modifier sits after the class (like {@code _OFCLASS_F}) so the key can never coincide with the
 * plain {@code _<CLASS>_I} key of a race or group whose name ends in {@code _MIN}/{@code _MAX}.
 *
 * <p><b>Exactness.</b> Every value is computed as (integer sum of member head counts) / (integer
 * total) in a single division — never as a sum of per-race fractions. {@code COMPARATOR.GREATERE} is
 * an exact {@code a >= b}, and adding four rounded fractions can land on 0.9999… in a city that is
 * 100% the group, which would fail a {@code GREATERE: 1} requirement. Integer numerator == integer
 * denominator divides to exactly 1.0.
 *
 * <p><b>Merging.</b> Files from all loaded mods are merged: a group's member list is the union of
 * every file that declares it, and {@code NAME} is taken from the highest-priority mod that sets it —
 * the first in {@code PATHS.currentMods()}, which is the same mod whose file wins when two mods ship
 * the same asset ({@code VirtualFolder.getPossibleFile} searches mod folders in that order, vanilla
 * last). A member race that isn't loaded is skipped (logged), so a group never depends on every member
 * mod being installed — and its keys always exist, so tech files that use them never hit the engine's
 * "no FACTION named …" warning (which would also lock the tech forever: {@code Lockable.RPromise}
 * substitutes a value that is always 0).
 *
 * <p><b>Lifecycle.</b> {@link #install()} must run in {@code initBeforeGameInited()}, the same window
 * as {@code registerCitizenRaceFractions}: {@code GVALUES} has been cleared for the new game and
 * {@code RACES} / {@code SValues} are populated, but {@code init.finish()} has not yet resolved the
 * {@code REQUIRES} promises. It re-runs per game (the {@code GVALUES} map is cleared per game).
 *
 * <p>Phase 1 covers the {@code POPULATION_*} values only. Groups are not races, so {@code TARGET_RACE},
 * {@code FACTION_IS_*}, {@code LOYALTY_*} and race-specific boosts do not accept them (yet).
 */
public final class RaceGroups {

    static final String CONFIG_FILE = "RaceGroups.txt";
    // NB: all diagnostics go to System.out, never System.err — the engine tees System.err into its
    // error dump/handler (see UpdateNotifier), and a typo in a RaceGroups.txt must not trigger that.
    private static final String LOG = "[sos-extended-boostables] race groups: ";
    /** Group keys are spliced into GVALUE keys, so keep them to the engine's key alphabet. */
    private static final Pattern KEY_FORMAT = Pattern.compile("[A-Z][A-Z0-9_]*[A-Z0-9]");

    private RaceGroups() {}

    /** One group, merged across every loaded mod's {@code RaceGroups.txt}. */
    static final class Def {
        final String key;
        String name;                       // highest-priority (first-listed) mod that sets NAME wins
        final LinkedHashSet<String> members = new LinkedHashSet<>();
        final List<String> definedBy = new ArrayList<>();

        Def(String key) {
            this.key = key;
        }
    }

    /** Reads every loaded mod's {@code RaceGroups.txt} and registers each group's GVALUEs. */
    public static void install() {
        Map<String, Def> defs = readAll();
        if (defs.isEmpty()) {
            System.out.println(LOG + "no loaded mod ships a V<major>/" + CONFIG_FILE + "; none registered.");
            return;
        }

        Map<String, Race> raceByKey = new HashMap<>();
        for (Race r : RACES.all())
            raceByKey.put(r.key, r);

        int total = 0;
        for (Def d : defs.values()) {
            try {
                total += register(d, raceByKey);
            } catch (Throwable t) {
                System.out.println(LOG + "failed to register group " + d.key + ": " + t);
            }
        }
        System.out.println(LOG + "registered " + total + " POPULATION_<GROUP>_* values for "
                + defs.size() + " group(s).");
    }

    // ---------------------------------------------------------------------------------------------
    // Reading
    // ---------------------------------------------------------------------------------------------

    private static Map<String, Def> readAll() {
        Map<String, Def> defs = new LinkedHashMap<>();
        try {
            for (ModInfo m : PATHS.currentMods()) {
                Path p = Paths.get(m.absolutePath, "V" + m.majorVersion, CONFIG_FILE);
                if (!Files.exists(p))
                    continue;
                Json j;
                try {
                    j = new Json(p); // parses the whole file; a syntax error throws before any merge
                } catch (Throwable t) {
                    System.out.println(LOG + "failed to read " + p + " (whole file ignored): " + t);
                    continue;
                }
                readFile(j, m.name, defs);
            }
        } catch (Throwable t) {
            System.out.println(LOG + "could not read currentMods: " + t);
        }
        return defs;
    }

    private static void readFile(Json j, String modName, Map<String, Def> defs) {
        for (String gk : j.keys()) {
            try {
                if (!KEY_FORMAT.matcher(gk).matches()) {
                    System.out.println(LOG + "mod '" + modName + "': group key '" + gk
                            + "' is not an UPPER_CASE key - skipped.");
                    continue;
                }
                Json g = j.json(gk);
                String[] races = raceList(g);
                String name = g.text("NAME", null);

                Def d = defs.get(gk);
                if (d == null) {
                    d = new Def(gk);
                    defs.put(gk, d);
                }
                if (name != null && d.name == null)
                    d.name = name; // mods are read in priority order, so the first NAME wins
                for (String r : races)
                    d.members.add(r);
                d.definedBy.add(modName);
            } catch (Throwable t) {
                System.out.println(LOG + "mod '" + modName + "': group '" + gk + "' is malformed - skipped: " + t);
            }
        }
    }

    /** {@code RACES} as bare keys ({@code [VERATHID, ...]}) or, leniently, quoted strings. */
    private static String[] raceList(Json g) {
        try {
            return g.values("RACES");
        } catch (RuntimeException notBare) {
            return g.texts("RACES");
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Registration
    // ---------------------------------------------------------------------------------------------

    private static int register(Def d, Map<String, Race> raceByKey) {
        if (raceByKey.containsKey(d.key)) {
            System.out.println(LOG + "group " + d.key + " has the same key as a loaded race - skipped.");
            return 0;
        }

        List<Race> present = new ArrayList<>();
        List<String> missing = new ArrayList<>();
        for (String k : d.members) {
            Race r = raceByKey.get(k);
            if (r != null)
                present.add(r);
            else
                missing.add(k);
        }
        final Race[] rs = present.toArray(new Race[0]);
        final String baseName = d.name != null ? d.name : d.key;

        // Display name, e.g. "Aquatic (Verathids, Khelians, Thryxarii, Onari)". Only loaded members
        // are listed, so the requirement tooltip tells the player exactly which races count.
        StringBuilder label = new StringBuilder(baseName);
        if (rs.length > 0) {
            label.append(" (");
            for (int i = 0; i < rs.length; i++) {
                if (i > 0)
                    label.append(", ");
                label.append(rs[i].info.names);
            }
            label.append(')');
        }
        final String name = label.toString();
        // MIN/MAX label (user-chosen 2026-11-13): "<NAME> (per race)" with no member list, e.g.
        // "Plebeians: Aquatic (per race)   >=   150   (42)". The current value shown is the smallest
        // (MIN) or largest (MAX) single-race count, not which race it belongs to.
        final String perRace = baseName + " (per race)";

        String k = "POPULATION_" + d.key + "_";
        int n = 0;

        // Whole-group totals (vanilla POPULATION_<RACE>__F / __I analogues).
        n += push(k + "_F", name, UI.icons().s.human, new DOUBLE_O<Faction>() {
            @Override
            public double getD(Faction o) {
                double div = POP.tot(null, null);
                if (div == 0)
                    return 0;
                return sumTot(null, rs) / div;
            }
        }, true);
        n += push(k + "_I", name, UI.icons().s.human, new DOUBLE_O<Faction>() {
            @Override
            public double getD(Faction o) {
                return sumTot(null, rs);
            }
        }, false);
        // Per-race extremes over the whole population: POPULATION_<G>__MIN_I / __MAX_I.
        n += push(k + "_MIN_I", perRace, UI.icons().s.human, new DOUBLE_O<Faction>() {
            @Override
            public double getD(Faction o) {
                return minTot(null, rs);
            }
        }, false);
        n += push(k + "_MAX_I", perRace, UI.icons().s.human, new DOUBLE_O<Faction>() {
            @Override
            public double getD(Faction o) {
                return maxTot(null, rs);
            }
        }, false);

        for (HCLASS cl : HCLASSES.ALL()) {
            if (!cl.player)
                continue;
            final HCLASS fcl = cl;
            String clName = cl.names + ": " + name;
            String clPerRace = cl.names + ": " + perRace;

            // Vanilla v71 meaning: share of the WHOLE population.
            n += push(k + cl.key + "_F", clName, cl.icon(), new DOUBLE_O<Faction>() {
                @Override
                public double getD(Faction o) {
                    double div = POP.tot(null, null);
                    if (div == 0)
                        return 0;
                    return sumTot(fcl, rs) / div;
                }
            }, true);
            n += push(k + cl.key + "_I", clName, cl.icon(), new DOUBLE_O<Faction>() {
                @Override
                public double getD(Faction o) {
                    return sumTot(fcl, rs);
                }
            }, false);
            // This mod's OFCLASS meaning: share OF THE CLASS (same terms as registerCitizenRaceFractions).
            n += push(k + cl.key + "_OFCLASS_F", clName, cl.icon(), new DOUBLE_O<Faction>() {
                @Override
                public double getD(Faction o) {
                    double div = POP.pop(fcl, null);
                    if (div == 0)
                        return 0;
                    return sumPop(fcl, rs) / div;
                }
            }, true);
            // Per-race extremes of this class. MIN >= N <=> every installed member race has >= N;
            // MAX >= N <=> at least one does. Same head-count terms as _I (POP.tot).
            n += push(k + cl.key + "_MIN_I", clPerRace, cl.icon(), new DOUBLE_O<Faction>() {
                @Override
                public double getD(Faction o) {
                    return minTot(fcl, rs);
                }
            }, false);
            n += push(k + cl.key + "_MAX_I", clPerRace, cl.icon(), new DOUBLE_O<Faction>() {
                @Override
                public double getD(Faction o) {
                    return maxTot(fcl, rs);
                }
            }, false);
        }

        System.out.println(LOG + d.key + " \"" + name + "\": " + rs.length + "/" + d.members.size()
                + " member races loaded" + (missing.isEmpty() ? "" : " (not loaded: " + missing + ")")
                + "; " + n + " keys; defined by " + d.definedBy + ".");
        return n;
    }

    private static int push(String key, CharSequence name, SPRITE icon, DOUBLE_O<Faction> value, boolean percentage) {
        if (GVALUES.FACTION.get(key) != null) {
            // GValueCat.push throws on a duplicate key, which would abort the game's init.
            System.out.println(LOG + key + " already exists - skipped.");
            return 0;
        }
        GVALUES.FACTION.push(key, name, icon, value, percentage);
        return 1;
    }

    /**
     * Integer head count in vanilla's {@code POP.tot} terms — {@code POP.pop} plus, for CITIZEN (and
     * the null/all class), children, rioters, deranged, criminals and field armies; for SLAVE, slave
     * children and slave criminals. Same terms the vanilla per-race {@code _F}/{@code _I} keys use.
     */
    private static int sumTot(HCLASS cl, Race[] rs) {
        int s = 0;
        for (Race r : rs)
            s += POP.tot(cl, r);
        return s;
    }

    /** Integer head count, {@code POP.pop} terms ({@code STATS.POP().POP.data(cl).get(r)}). */
    private static int sumPop(HCLASS cl, Race[] rs) {
        int s = 0;
        for (Race r : rs)
            s += POP.pop(cl, r);
        return s;
    }

    /**
     * Smallest single-member head count ({@code POP.tot} terms). A group with no installed member
     * reads 0 (user decision 2026-11-13), so a {@code GREATERE} requirement on it stays locked rather
     * than passing vacuously.
     */
    private static int minTot(HCLASS cl, Race[] rs) {
        if (rs.length == 0)
            return 0;
        int m = Integer.MAX_VALUE;
        for (Race r : rs)
            m = Math.min(m, POP.tot(cl, r));
        return m;
    }

    /** Largest single-member head count ({@code POP.tot} terms); 0 when no member is installed. */
    private static int maxTot(HCLASS cl, Race[] rs) {
        int m = 0;
        for (Race r : rs)
            m = Math.max(m, POP.tot(cl, r));
        return m;
    }
}
