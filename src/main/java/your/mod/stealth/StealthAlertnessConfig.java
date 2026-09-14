package your.mod.stealth;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import init.paths.ModInfo;
import init.paths.PATHS;
import snake2d.util.file.Json;

/**
 * Opt-in switch for the STEALTH / ALERTNESS boostables and the crime-detection contest that reads
 * them (see {@link StealthAlertnessBoostables} and {@link CrimeStealthCheck}).
 *
 * <p>Per the session brief: these two boostables must NOT be created unless a hosting mod (e.g. a
 * Heroes-style mod that depends on this one) ships a small opt-in data file. This mirrors the
 * existing {@code your.mod.update.UpdateNotifier} "any mod that ships a marker file" idiom — no code
 * from the hosting mod is required, only a data file at:
 *
 * <pre>{@code <modfolder>/V<major>/StealthAlertness.txt}</pre>
 *
 * containing (game {@code Json} syntax, same as {@code UpdateNotice.txt}):
 * <pre>{@code
 * ENABLED: true,
 * }</pre>
 *
 * <p>Scanned once, lazily, the first time {@link #isEnabled()} is asked (registration happens at
 * {@code initBeforeGameInited()}, which runs once per game boot before any save is loaded — the mod
 * list itself does not change per-save, so a single scan is sufficient for the whole process
 * lifetime, same reasoning as {@code UpdateNotifier}).
 */
public final class StealthAlertnessConfig {

    private static final String CONFIG_FILE = "StealthAlertness.txt";
    private static final String LOG = "[sos-extended-boostables] stealth/alertness: ";

    private static Boolean cached = null;

    private StealthAlertnessConfig() {}

    /** @return true if ANY currently loaded mod ships an enabled {@code V<major>/StealthAlertness.txt}. */
    public static boolean isEnabled() {
        if (cached != null)
            return cached;

        boolean found = false;
        try {
            for (ModInfo m : PATHS.currentMods()) {
                try {
                    if (checkMod(m)) {
                        found = true;
                        System.out.println(LOG + "enabled by mod '" + m.name + "'.");
                        break;
                    }
                } catch (Throwable t) {
                    System.out.println(LOG + "error scanning mod '" + m.name + "': " + t);
                }
            }
        } catch (Throwable t) {
            System.out.println(LOG + "could not read currentMods: " + t);
        }

        if (!found)
            System.out.println(LOG + "no loaded mod enabled it; STEALTH/ALERTNESS boostables not created.");
        cached = found;
        return found;
    }

    private static boolean checkMod(ModInfo m) {
        Path p = Paths.get(m.absolutePath, "V" + m.majorVersion, CONFIG_FILE);
        if (!Files.exists(p))
            return false;
        try {
            Json j = new Json(p);
            return j.bool("ENABLED", false);
        } catch (Throwable t) {
            System.out.println(LOG + "failed to read " + p + ": " + t);
            return false;
        }
    }

    /** Test-only / reload hook: forces the next {@link #isEnabled()} call to re-scan. */
    static void resetCacheForTests() {
        cached = null;
    }
}
