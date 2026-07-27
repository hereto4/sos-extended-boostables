package your.mod.targetfilter;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import init.paths.PATHS;
import snake2d.LOG;
import snake2d.util.file.Json;

/**
 * Pre-scans {@code assets/init/tech/*.txt} across every enabled mod (in load
 * order) and records which TECH nodes declare a {@code TARGET_RACE} and/or a
 * {@code TARGET_CLASS}.
 *
 * <ul>
 *   <li>Valid race names are enumerated dynamically by listing
 *       {@code assets/init/race/*.txt}, so race mods picked up by the engine are
 *       accepted here too.</li>
 *   <li>Valid class tokens are a fixed set ({@link #KNOWN_CLASSES}) — the engine's
 *       {@code HCLASSES} isn't constructed yet at {@code initBeforeGameCreated}
 *       time, and the player-facing classes never change. Tokens are resolved to
 *       live {@code HCLASS} instances later, in {@link TargetFilterApplier}.</li>
 * </ul>
 *
 * <p>Must run in {@code initBeforeGameCreated()} so the recorded flags are
 * available when {@link TargetFilterApplier} reaches in during
 * {@code initBeforeGameInited()}.
 *
 * <p>Generalized from the standalone target-race-tech mod's
 * {@code TargetRaceRegistry} to also carry the class token.
 */
final class TargetFilterRegistry {

    static final String KEY_RACE = "TARGET_RACE";
    static final String KEY_CLASS = "TARGET_CLASS";

    /**
     * Accepted {@code TARGET_CLASS} tokens. {@code CITIZEN/NOBLE/SLAVE/OTHER} map
     * to the engine {@code HCLASS} of the same key; {@code EXSLAVE} is synthetic
     * (arrival cause == EMANCIPATED — see {@link SubjectFilter}). Fixed set,
     * because the engine's {@code HCLASSES} is not yet constructed when the scan
     * runs (GAME ctor, before {@code new INIT()}).
     */
    static final Set<String> KNOWN_CLASSES = new LinkedHashSet<>(
            Arrays.asList("CITIZEN", "NOBLE", "SLAVE", "OTHER", "EXSLAVE"));

    /** TECH.key (== "${treeFileKey}_${nodeName}") -> the target spec for that tech. */
    static final Map<String, Spec> flags = new HashMap<>();

    /** Race names found in {@code assets/init/race/*.txt} (no extension), raw case. */
    static final Set<String> knownRaces = new HashSet<>();

    /** What a single tech wants to target. At least one of the two is non-null. */
    static final class Spec {
        String raceName;   // nullable — raw race key, e.g. "CRETONIAN"
        String classToken; // nullable — uppercased, one of KNOWN_CLASSES
    }

    private TargetFilterRegistry() {}

    static void scanTechFiles() {
        flags.clear();
        knownRaces.clear();
        announce("scan starting");

        scanRaceNames();
        if (knownRaces.isEmpty())
            announce("WARNING: no race files under assets/init/race; TARGET_RACE validation will reject everything");
        else
            announce("known races (" + knownRaces.size() + "): " + knownRaces);
        announce("known classes: " + KNOWN_CLASSES);

        PATHS.ResFolder techDir;
        try {
            techDir = new PATHS.ResFolder("tech", false);
        } catch (Throwable t) {
            announce("ABORT: PATHS not initialized: " + t);
            return;
        }

        String[] files = techDir.init.getFiles();
        int raceFlags = 0, classFlags = 0;
        for (String fileKey : files) {
            Path path = techDir.init.get(fileKey);
            Json fileJson = new Json(path);
            if (!fileJson.has("TECHS")) continue;
            Json techs = fileJson.json("TECHS");
            for (String nodeName : techs.keys()) {
                Json node = techs.json(nodeName);
                boolean hasRace = node.has(KEY_RACE);
                boolean hasClass = node.has(KEY_CLASS);
                if (!hasRace && !hasClass) continue;

                Spec spec = new Spec();

                if (hasRace) {
                    String raw = node.value(KEY_RACE);
                    String race = raw == null ? "" : raw.trim();
                    if (race.isEmpty()) {
                        announce("  ! " + path + " tech=" + nodeName + ": empty TARGET_RACE (ignored)");
                    } else if (!knownRaces.contains(race)) {
                        announce("  ! " + path + " tech=" + nodeName + ": TARGET_RACE='" + race
                                + "' not a known race (ignored)");
                    } else {
                        spec.raceName = race;
                    }
                }

                if (hasClass) {
                    String raw = node.value(KEY_CLASS);
                    String cl = raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
                    if (cl.isEmpty()) {
                        announce("  ! " + path + " tech=" + nodeName + ": empty TARGET_CLASS (ignored)");
                    } else if (!KNOWN_CLASSES.contains(cl)) {
                        announce("  ! " + path + " tech=" + nodeName + ": TARGET_CLASS='" + cl
                                + "' not one of " + KNOWN_CLASSES + " (ignored)");
                    } else {
                        spec.classToken = cl;
                    }
                }

                if (spec.raceName == null && spec.classToken == null) continue; // all constraints invalid

                String techKey = fileKey + "_" + nodeName;
                flags.put(techKey, spec);
                if (spec.raceName != null) raceFlags++;
                if (spec.classToken != null) classFlags++;
                announce("  + flagged tech=" + techKey
                        + (spec.raceName != null ? " TARGET_RACE=" + spec.raceName : "")
                        + (spec.classToken != null ? " TARGET_CLASS=" + spec.classToken : ""));
            }
        }
        announce("scan complete; " + flags.size() + " tech(s) flagged (race=" + raceFlags
                + ", class=" + classFlags + ") across " + files.length + " file(s)");

        if (!flags.isEmpty()) {
            // Runs in initBeforeGameCreated (GAME-ctor), before new TECHS() parses
            // tech files — so the engine's checkUnused() never emits an
            // "unknown key: TARGET_*" line to stderr. Only suppress when we
            // actually flagged a tech, to leave other mods' warnings intact when
            // neither key is in use.
            ParseWarningSuppressor.suppressUnknownKeyWarnings();
        }
    }

    /** Lists race files directly under assets/init/race/ (excludes subfolders like sprite/, home/, ...). */
    private static void scanRaceNames() {
        try {
            PATHS.ResFolder raceDir = new PATHS.ResFolder("race", false);
            for (String fileKey : raceDir.init.getFiles())
                knownRaces.add(fileKey);
        } catch (Throwable t) {
            announce("ABORT race scan: " + t);
        }
    }

    static void announce(String msg) {
        LOG.ln("[target-filter] " + msg);
    }
}
