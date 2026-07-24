package your.mod.update;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import init.paths.ModInfo;
import init.paths.PATHS;
import snake2d.util.file.Json;
import snake2d.util.file.JsonE;
import util.gui.misc.GBox;
import view.main.VIEW;

/**
 * "Mod updated" event window. Shows a one-time popup of the recent changes the first time a player
 * enters a game after Extended Boostables' {@code _Info} VERSION changes.
 *
 * <p>All content is driven by a single file the modder edits each release,
 * {@code <modfolder>/V<major>/UpdateNotice.txt} (staged from
 * {@code src/main/resources/mod-files/UpdateNotice.txt}), which carries a per-release {@code ENABLED}
 * toggle, a {@code TITLE}, and a {@code CHANGES} string array. The popup fires only when BOTH the
 * VERSION differs from the last version already shown AND that file's {@code ENABLED} is true.
 *
 * <p>"Last shown version" is persisted per-install (not per-save) in
 * {@code %APPDATA%/songsofsyx/saves/profile/sos-extended-boostables-update.txt} via {@link PATHS#local()}
 * — this is what makes the popup fire once per update across every save, rather than once per savegame.
 * A disabled release is intentionally NOT recorded, so re-enabling it on a later load still shows it.
 *
 * <p>Every file/parse step is guarded to fail safe (no popup, logged) rather than break game load.
 */
public final class UpdateNotifier {

    /** Hard master switch: set false to disable the whole update-notice feature regardless of the file. */
    public static final boolean ENABLED = true;

    private static final String MOD_NAME = "Extended Boostables";
    private static final String CONFIG_FILE = "UpdateNotice.txt";
    /** Profile filename (no extension — PATHS.local().PROFILE appends ".txt"). */
    private static final String STATE_FILE = "sos-extended-boostables-update";
    private static final String LOG = "[sos-extended-boostables] update notice: ";

    private UpdateNotifier() {}

    /**
     * Evaluates the update-notice condition and shows the popup if warranted.
     *
     * @return {@code true} once a decision is finalized (popup shown, or determined no-show);
     *         {@code false} only when it must be retried next tick because the in-game
     *         {@link VIEW} is not ready yet (so the caller keeps calling until it returns true).
     */
    public static boolean showIfUpdated() {
        if (!ENABLED) return true;

        ModInfo self = findSelf();
        if (self == null) return true; // not loaded as an installed mod (e.g. IDE run) — nothing to do

        String current = self.version;
        if (current == null || current.isEmpty() || "???".equals(current)) {
            System.err.println(LOG + "could not resolve this mod's VERSION; skipping.");
            return true;
        }

        Config cfg = readConfig(self);
        if (cfg == null) return true;   // missing / malformed file — already logged, no-op
        if (!cfg.enabled) return true;  // per-release toggle off — do NOT record (can be enabled later)

        if (current.equals(readLastShown())) return true; // already shown for this version

        // The popup needs the in-game view; if it isn't up yet, retry on the next tick.
        if (!VIEW.existTemp()) return false;

        show(cfg.title, cfg.lines);
        writeLastShown(current);
        System.out.println(LOG + "showed update notice for version " + current + ".");
        return true;
    }

    /** Locates this mod's {@link ModInfo} among the currently-loaded mods, matched by NAME. */
    private static ModInfo findSelf() {
        try {
            for (ModInfo m : PATHS.currentMods()) {
                if (MOD_NAME.equals(m.name)) return m;
            }
        } catch (Throwable t) {
            System.err.println(LOG + "could not read currentMods: " + t);
        }
        return null;
    }

    private static final class Config {
        final boolean enabled;
        final String title;
        final String[] lines;
        Config(boolean enabled, String title, String[] lines) {
            this.enabled = enabled;
            this.title = title;
            this.lines = lines;
        }
    }

    /** Reads {@code <modfolder>/V<major>/UpdateNotice.txt} as game Json. Returns null on any problem. */
    private static Config readConfig(ModInfo self) {
        try {
            Path p = Paths.get(self.absolutePath, "V" + self.majorVersion, CONFIG_FILE);
            if (!Files.exists(p)) {
                System.out.println(LOG + "no " + CONFIG_FILE + " at " + p + " (skipping).");
                return null;
            }
            Json j = new Json(p);
            boolean enabled = j.bool("ENABLED", false);
            String title = j.text("TITLE", MOD_NAME + " Updated!");
            String[] lines = j.textsTry("CHANGES");
            return new Config(enabled, title, lines);
        } catch (Throwable t) {
            System.err.println(LOG + "failed to read " + CONFIG_FILE + ": " + t);
            return null;
        }
    }

    /** Version last shown to the player (empty string if none/unreadable). */
    private static String readLastShown() {
        try {
            if (PATHS.local().PROFILE.exists(STATE_FILE)) {
                Json j = new Json(PATHS.local().PROFILE.get(STATE_FILE));
                return j.text("VERSION", "");
            }
        } catch (Throwable t) {
            System.err.println(LOG + "failed to read state file: " + t);
        }
        return "";
    }

    /** Records {@code version} as the last version shown, so the popup never repeats for it. */
    private static void writeLastShown(String version) {
        try {
            JsonE j = new JsonE();
            j.addString("VERSION", version);
            Path p = PATHS.local().PROFILE.exists(STATE_FILE)
                    ? PATHS.local().PROFILE.get(STATE_FILE)
                    : PATHS.local().PROFILE.create(STATE_FILE);
            j.save(p);
        } catch (Throwable t) {
            System.err.println(LOG + "failed to write state file: " + t);
        }
    }

    /**
     * Builds the changelog {@link GBox} (title + one line per change; GBox auto-frames, titles and
     * scrolls) and shows it via the shared {@link view.interrupter.IPopup} — a centered framed panel
     * with a close (X) button that also dismisses on right-click / click-away.
     */
    private static void show(String title, String[] lines) {
        GBox box = new GBox();
        box.clear();
        box.maxWidth = 600;
        box.title(title);
        if (lines == null || lines.length == 0) {
            box.textLL("(no changes listed)");
            box.NL();
        } else {
            for (String line : lines) {
                box.textLL(line);
                box.NL();
            }
        }
        VIEW.inters().popup.show(box.asRenObj(), null);
    }
}
