package your.mod.update;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import init.constant.C;
import init.paths.ModInfo;
import init.paths.PATHS;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.Json;
import snake2d.util.file.JsonE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import util.gui.misc.GBox;
import util.gui.panel.GPanel;
import view.interrupter.InterManager;
import view.interrupter.Interrupter;
import view.keyboard.KEYS;
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

    /** One-shot flag so the diagnostic log on the repeated "waiting" path doesn't spam. */
    private static boolean waitLogged = false;

    private UpdateNotifier() {}

    /**
     * Evaluates the update-notice condition and shows the popup if warranted.
     *
     * @return {@code true} once a decision is finalized (popup shown, or determined no-show);
     *         {@code false} only when it must be retried next tick because the in-game
     *         {@link VIEW} is not ready yet (so the caller keeps calling until it returns true).
     */
    public static boolean showIfUpdated() {
        if (!ENABLED) {
            System.out.println(LOG + "master switch ENABLED=false; feature off.");
            return true;
        }

        ModInfo self = findSelf();
        if (self == null) {
            System.out.println(LOG + "this mod not found in PATHS.currentMods(); skipping.");
            return true; // not loaded as an installed mod (e.g. IDE run) — nothing to do
        }

        String current = self.version;
        if (current == null || current.isEmpty() || "???".equals(current)) {
            System.err.println(LOG + "could not resolve this mod's VERSION; skipping.");
            return true;
        }

        Config cfg = readConfig(self);
        if (cfg == null) return true;   // missing / malformed file — already logged, no-op
        if (!cfg.enabled) {
            System.out.println(LOG + "config ENABLED=false; not showing (version " + current + ").");
            return true;  // per-release toggle off — do NOT record (can be enabled later)
        }

        String last = readLastShown();
        if (current.equals(last)) {
            System.out.println(LOG + "version " + current + " already shown; not showing.");
            return true;
        }

        // The popup needs the in-game view; if it isn't up yet, retry on the next tick.
        if (!VIEW.existTemp()) {
            if (!waitLogged) {
                System.out.println(LOG + "version " + current + " is new (last=\"" + last
                        + "\"); waiting for in-game VIEW before showing...");
                waitLogged = true;
            }
            return false;
        }

        System.out.println(LOG + "VIEW ready; showing update window for version " + current
                + " (" + (cfg.lines == null ? 0 : cfg.lines.length) + " line(s)).");
        new UpdateNoticeWindow(cfg.title, cfg.lines).open(VIEW.inters().manager);
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
}

/**
 * The changelog window: a centered {@link GPanel} frame (with a close X via {@code clickActionSet})
 * wrapping a scrollable {@link GBox} of the title + change lines.
 *
 * <p><b>Rendering:</b> {@link #render} returns {@code false}. In {@code VIEW.render} the interrupter
 * manager is drawn after the terrain background but before the UI/foreground passes; returning
 * {@code false} makes {@code VIEW} stop there, so this window is the top-most thing drawn (this is
 * exactly what the engine's own {@code IPromtScreen} does — an earlier version returned {@code true}
 * and got painted over by the UI/foreground, hence "showed in the log but invisible").
 *
 * <p>Constructed {@code persistent + pinned} so it survives the interrupter churn during the
 * load→gameplay transition. It closes only on the X, ESC, or a right-click — deliberately NOT on a
 * left-click, so a stray click buffered during loading can't dismiss it before the player sees it.
 */
final class UpdateNoticeWindow extends Interrupter {

    private final GBox content = new GBox();
    private final GPanel frame = new GPanel();
    private final RENDEROBJ ren;
    private final ACTION exit = new ACTION() {
        @Override public void exe() { hide(); }
    };

    UpdateNoticeWindow(String title, String[] lines) {
        super(true, true); // persistent + pinned — survive load-time interrupter churn; stay until closed
        frame.setBig();
        content.clear();
        content.maxWidth = 600;
        content.title(title);
        if (lines == null || lines.length == 0) {
            content.textLL("(no changes listed)");
            content.NL();
        } else {
            for (String line : lines) {
                content.textLL(line);
                content.NL();
            }
        }
        ren = content.asRenObj();
    }

    /** Public entry so {@link UpdateNotifier} can add us to the manager (show() is protected). */
    void open(InterManager m) {
        show(m);
    }

    private void centre() {
        ren.body().moveX1(C.WIDTH() / 2 - ren.body().width() / 2);
        ren.body().moveY1(C.HEIGHT() / 2 - ren.body().height() / 2);
    }

    @Override
    protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
        // frame.hover consumes the X close click; keep hover within the window so inside-clicks don't fall through.
        return frame.hover(mCoo) || mCoo.isWithinRec(frame.body()) || mCoo.isWithinRec(ren.body());
    }

    @Override
    protected void mouseClick(MButt button) {
        if (button == MButt.RIGHT) hide();
        // left-clicks inside the window do nothing (the X is handled in hover()).
    }

    @Override
    protected boolean otherClick(MButt button) {
        // Only a right-click outside closes it; ignore stray left-clicks (e.g. buffered during loading).
        if (button == MButt.RIGHT) {
            hide();
            return true;
        }
        return false;
    }

    @Override
    protected void hoverTimer(GBox text) {}

    @Override
    protected boolean render(Renderer r, float ds) {
        centre();
        frame.inner().set(ren.body());
        frame.clickActionSet(exit); // draws the close (X) button and wires its click
        frame.render(r, ds);
        ren.render(r, ds);
        return false; // top-most: stop VIEW from drawing the UI/foreground over us
    }

    @Override
    protected boolean update(float ds) {
        if (KEYS.MAIN().ESCAPE.consumeClick()) hide();
        return false;
    }
}
