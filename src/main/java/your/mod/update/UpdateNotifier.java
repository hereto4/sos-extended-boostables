package your.mod.update;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import init.constant.C;
import init.paths.ModInfo;
import init.paths.PATHS;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.Json;
import snake2d.util.file.JsonE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.text.Font;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.gui.panel.GPanel;
import view.interrupter.InterManager;
import view.interrupter.Interrupter;
import view.keyboard.KEYS;
import view.main.VIEW;

/**
 * Generic "mod updated" event-window service. On the first game a player enters after any loaded mod's
 * {@code _Info} VERSION changes, this shows a one-time popup of that mod's recent changes.
 *
 * <p><b>Opt-in per mod (no code required):</b> a mod participates simply by shipping a file at
 * {@code <modfolder>/V<major>/UpdateNotice.txt}. {@link #showPending()} scans every mod in
 * {@link PATHS#currentMods()} for that file. Because this code lives in Extended Boostables and runs
 * from its {@code MainScript}, any mod that has Extended Boostables loaded alongside it (e.g. as a
 * dependency) gets update windows for free — it only needs the data file.
 *
 * <p>The file carries a per-release {@code ENABLED} toggle, a {@code TITLE}, and a {@code CHANGES}
 * string array. A mod's window fires only when BOTH its VERSION differs from the last version shown
 * for that mod AND its {@code ENABLED} is true. A disabled release is intentionally NOT recorded, so
 * re-enabling it on a later load still shows it.
 *
 * <p>"Last shown version" is persisted per-install (not per-save), one file per mod (keyed by the mod's
 * folder name) under {@code %APPDATA%/songsofsyx/saves/profile/<modfolder>-update.txt} — this is what
 * makes each window fire once per update across every save. Every file/parse step is guarded to fail
 * safe (skip that mod, log) rather than break game load. Multiple pending windows stack and are shown
 * one after another (each is closed with X / ESC / right-click).
 */
public final class UpdateNotifier {

    /** Hard master switch: set false to disable the whole update-notice feature for all mods. */
    public static final boolean ENABLED = true;

    private static final String CONFIG_FILE = "UpdateNotice.txt";
    private static final String LOG = "[sos-extended-boostables] update notice: ";

    /** One-shot flag so the diagnostic log on the repeated "waiting" path doesn't spam. */
    private static boolean waitLogged = false;

    private UpdateNotifier() {}

    /**
     * Scans all loaded mods and shows an update window for each one whose VERSION changed and whose
     * {@code UpdateNotice.txt} is enabled.
     *
     * @return {@code true} once processed (windows shown or nothing to do); {@code false} only while the
     *         in-game {@link VIEW} isn't ready yet, so the caller retries next tick.
     */
    public static boolean showPending() {
        if (!ENABLED) {
            System.out.println(LOG + "master switch ENABLED=false; feature off.");
            return true;
        }

        // The popup needs the in-game view; if it isn't up yet, retry on the next tick.
        if (!VIEW.existTemp()) {
            if (!waitLogged) {
                System.out.println(LOG + "waiting for in-game VIEW before scanning mods...");
                waitLogged = true;
            }
            return false;
        }

        try {
            for (ModInfo m : PATHS.currentMods()) {
                try {
                    processMod(m);
                } catch (Throwable t) {
                    System.err.println(LOG + "error processing mod '" + m.name + "': " + t);
                }
            }
        } catch (Throwable t) {
            System.err.println(LOG + "could not read currentMods: " + t);
        }
        return true;
    }

    /** Evaluates one mod's UpdateNotice.txt and shows its window if the version changed and it's enabled. */
    private static void processMod(ModInfo m) {
        Path p = Paths.get(m.absolutePath, "V" + m.majorVersion, CONFIG_FILE);
        if (!Files.exists(p)) return; // this mod didn't opt in

        Config cfg = readConfig(p, m.name);
        if (cfg == null) return; // malformed — already logged
        if (!cfg.enabled) {
            System.out.println(LOG + "'" + m.name + "' config ENABLED=false; not showing.");
            return; // per-release toggle off — do NOT record (can be enabled later)
        }

        String current = m.version;
        if (current == null || current.isEmpty() || "???".equals(current)) {
            System.err.println(LOG + "'" + m.name + "' has no resolvable VERSION; skipping.");
            return;
        }

        String key = stateFile(m);
        if (current.equals(readLastShown(key))) {
            System.out.println(LOG + "'" + m.name + "' version " + current + " already shown; not showing.");
            return;
        }

        System.out.println(LOG + "showing '" + m.name + "' update window for version " + current
                + " (" + (cfg.lines == null ? 0 : cfg.lines.length) + " line(s)).");
        new UpdateNoticeWindow(cfg.title, cfg.lines).open(VIEW.inters().manager);
        writeLastShown(key, current);
        System.out.println(LOG + "showed '" + m.name + "' update notice for version " + current + ".");
    }

    /** Profile filename (no extension) for a mod's "last shown version" — keyed by its unique folder name. */
    private static String stateFile(ModInfo m) {
        return m.path + "-update";
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

    /** Reads an UpdateNotice.txt as game Json. Returns null on any problem (logged). */
    private static Config readConfig(Path p, String modName) {
        try {
            Json j = new Json(p);
            boolean enabled = j.bool("ENABLED", false);
            String title = j.text("TITLE", modName + " Updated!");
            String[] lines = j.textsTry("CHANGES");
            return new Config(enabled, title, lines);
        } catch (Throwable t) {
            System.err.println(LOG + "failed to read " + p + ": " + t);
            return null;
        }
    }

    /** Version last shown for the given state key (empty string if none/unreadable). */
    private static String readLastShown(String key) {
        try {
            if (PATHS.local().PROFILE.exists(key)) {
                Json j = new Json(PATHS.local().PROFILE.get(key));
                return j.text("VERSION", "");
            }
        } catch (Throwable t) {
            System.err.println(LOG + "failed to read state file '" + key + "': " + t);
        }
        return "";
    }

    /** Records {@code version} as the last version shown for {@code key}, so its window never repeats for it. */
    private static void writeLastShown(String key, String version) {
        try {
            JsonE j = new JsonE();
            j.addString("VERSION", version);
            Path p = PATHS.local().PROFILE.exists(key)
                    ? PATHS.local().PROFILE.get(key)
                    : PATHS.local().PROFILE.create(key);
            j.save(p);
        } catch (Throwable t) {
            System.err.println(LOG + "failed to write state file '" + key + "': " + t);
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
 * exactly what the engine's own {@code IPromtScreen} does — a version that returned {@code true} got
 * painted over by the UI/foreground, hence "showed in the log but invisible").
 *
 * <p>Constructed {@code persistent + pinned} so it survives the interrupter churn during the
 * load→gameplay transition. It closes only on the X, ESC, or a right-click — deliberately NOT on a
 * left-click, so a stray click buffered during loading can't dismiss it before the player sees it.
 *
 * <p><b>CHANGES markup</b> (each array entry is one line): <ul>
 * <li>{@code "# Heading"} → heading (title-sized); {@code "## Heading"} → smaller sub-heading.</li>
 * <li>{@code "---"} (3+ dashes) → a horizontal divider line.</li>
 * <li>{@code ""} (empty) → a blank spacer line.</li>
 * <li>{@code "[tag] text"} → colored text. Tags: {@code good great bad worst warn error label sub
 *     normal}. An unknown tag is left as literal text.</li></ul>
 * A color tag may precede a heading marker (e.g. {@code "[warn]# Title"}). Plain lines render as body text.
 */
final class UpdateNoticeWindow extends Interrupter {

    /** Extra vertical gap after a blank-line spacer / after a heading. */
    private static final int SPACER_GAP = 10;
    private static final int HEADING_GAP = 4;

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
        buildContent(lines);
        ren = content.asRenObj();
    }

    /** Renders the CHANGES lines using the lightweight markup documented on this class. */
    private void buildContent(String[] lines) {
        if (lines == null || lines.length == 0) {
            content.textLL("(no changes listed)");
            content.NL();
            return;
        }
        for (String raw : lines) {
            String s = (raw == null ? "" : raw).trim();

            if (s.isEmpty()) {                                          // blank spacer
                content.NL(SPACER_GAP);
                continue;
            }
            if (s.length() >= 3 && s.chars().allMatch(c -> c == '-')) { // divider
                content.sep();
                continue;
            }

            // Optional leading color tag: [tag]
            COLOR color = null;
            if (s.startsWith("[")) {
                int end = s.indexOf(']');
                if (end > 1) {
                    COLOR c = colorFor(s.substring(1, end).trim().toLowerCase());
                    if (c != null) {
                        color = c;
                        s = s.substring(end + 1);
                        if (s.startsWith(" ")) s = s.substring(1);
                    }
                }
            }

            // Optional heading marker: ## or #. Sized so a heading never exceeds the window title (H2):
            // "#" -> H2 (title-sized) with the strong H1 heading color, "##" -> M (smaller) with H2 color.
            Font font = UI.FONT().S;
            COLOR headingColor = null;
            if (s.startsWith("## ")) {
                font = UI.FONT().M; headingColor = GCOLOR.T().H2; s = s.substring(3);
            } else if (s.startsWith("# ")) {
                font = UI.FONT().H2; headingColor = GCOLOR.T().H1; s = s.substring(2);
            }

            GText t = content.text();
            t.setFont(font);
            if (color != null) t.color(color);
            else if (headingColor != null) t.color(headingColor);
            else t.normalify();
            t.add(s);
            content.add(t);
            content.NL(headingColor != null ? HEADING_GAP : 0);
        }
    }

    /** Maps a {@code [tag]} to a themed text color, or {@code null} if unknown (left as literal text). */
    private static COLOR colorFor(String tag) {
        switch (tag) {
            case "good":   return GCOLOR.T().IGOOD;
            case "great":  return GCOLOR.T().IGREAT;
            case "bad":    return GCOLOR.T().IBAD;
            case "worst":  return GCOLOR.T().IWORST;
            case "warn":   return GCOLOR.T().WARNING;
            case "error":  return GCOLOR.T().ERROR;
            case "label":  return GCOLOR.T().H1;
            case "sub":    return GCOLOR.T().H2;
            case "normal": return GCOLOR.T().NORMAL;
            default:       return null;
        }
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
