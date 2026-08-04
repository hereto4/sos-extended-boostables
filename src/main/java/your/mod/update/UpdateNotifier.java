package your.mod.update;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import init.constant.C;
import init.paths.ModInfo;
import init.paths.PATHS;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.Json;
import snake2d.util.file.JsonE;
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
 * string array. A mod's notice fires only when BOTH its VERSION differs from the last version shown
 * for that mod AND its {@code ENABLED} is true. A disabled release is intentionally NOT recorded, so
 * re-enabling it on a later load still shows it.
 *
 * <p>All pending mods are shown together in a <b>single</b> window (one section per mod), so the player
 * dismisses one popup rather than a confusing stack.
 *
 * <p>"Last shown version" is persisted per-install (not per-save), one file per mod (keyed by the mod's
 * folder name) under {@code %APPDATA%/songsofsyx/saves/profile/<modfolder>-update.txt} — this is what
 * makes each notice fire once per update across every save. Every file/parse step is guarded to fail
 * safe (skip that mod, log) rather than break game load.
 */
public final class UpdateNotifier {

    /** Hard master switch: set false to disable the whole update-notice feature for all mods. */
    public static final boolean ENABLED = true;

    private static final String CONFIG_FILE = "UpdateNotice.txt";
    // NB: all diagnostics go to System.out, never System.err. In this engine snake2d.Errors tees
    // System.err into the crash/error dump and fires the game's error handler when it is non-empty
    // (see Errors.check()), so a user typo in an UpdateNotice.txt must not reach stderr.
    private static final String LOG = "[sos-extended-boostables] update notice: ";

    /** One-shot flag so the diagnostic log on the repeated "waiting" path doesn't spam. */
    private static boolean waitLogged = false;

    private UpdateNotifier() {}

    /**
     * Scans all loaded mods and, if any have a changed+enabled {@code UpdateNotice.txt}, shows them all
     * together in a single window and records each as shown.
     *
     * @return {@code true} once processed (window shown or nothing to do); {@code false} only while the
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

        List<Notice> pending = new ArrayList<>();
        try {
            for (ModInfo m : PATHS.currentMods()) {
                try {
                    Notice n = evaluate(m);
                    if (n != null) pending.add(n);
                } catch (Throwable t) {
                    System.out.println(LOG + "error processing mod '" + m.name + "': " + t);
                }
            }
        } catch (Throwable t) {
            System.out.println(LOG + "could not read currentMods: " + t);
        }

        if (!pending.isEmpty()) {
            try {
                new UpdateNoticeWindow(pending).open(VIEW.inters().manager);
                for (Notice n : pending) writeLastShown(n.stateKey, n.version);
                System.out.println(LOG + "showed " + pending.size() + " update notice(s).");
            } catch (Throwable t) {
                System.out.println(LOG + "failed to show window: " + t);
            }
        }
        return true;
    }

    /** Evaluates one mod's UpdateNotice.txt; returns a {@link Notice} to show, or null (with logging). */
    private static Notice evaluate(ModInfo m) {
        Path p = Paths.get(m.absolutePath, "V" + m.majorVersion, CONFIG_FILE);
        if (!Files.exists(p)) return null; // this mod didn't opt in

        Config cfg = readConfig(p, m.name);
        if (cfg == null) return null; // malformed — already logged
        if (!cfg.enabled) {
            System.out.println(LOG + "'" + m.name + "' config ENABLED=false; not showing.");
            return null; // per-release toggle off — do NOT record (can be enabled later)
        }

        String current = m.version;
        if (current == null || current.isEmpty() || "???".equals(current)) {
            System.out.println(LOG + "'" + m.name + "' has no resolvable VERSION; skipping.");
            return null;
        }

        String key = stateFile(m);
        if (current.equals(readLastShown(key))) {
            System.out.println(LOG + "'" + m.name + "' version " + current + " already shown; not showing.");
            return null;
        }

        System.out.println(LOG + "'" + m.name + "' has a new version " + current + " to show.");
        return new Notice(cfg.title, cfg.lines, key, current);
    }

    /** One mod's ready-to-show notice (package-private so {@link UpdateNoticeWindow} can render it). */
    static final class Notice {
        final String title;
        final String[] lines;
        final String stateKey;
        final String version;
        Notice(String title, String[] lines, String stateKey, String version) {
            this.title = title;
            this.lines = lines;
            this.stateKey = stateKey;
            this.version = version;
        }
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
            System.out.println(LOG + "failed to read " + p + ": " + t);
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
            System.out.println(LOG + "failed to read state file '" + key + "': " + t);
        }
        return "";
    }

    /** Records {@code version} as the last version shown for {@code key}, so its notice never repeats for it. */
    private static void writeLastShown(String key, String version) {
        try {
            JsonE j = new JsonE();
            j.addString("VERSION", version);
            Path p = PATHS.local().PROFILE.exists(key)
                    ? PATHS.local().PROFILE.get(key)
                    : PATHS.local().PROFILE.create(key);
            j.save(p);
        } catch (Throwable t) {
            System.out.println(LOG + "failed to write state file '" + key + "': " + t);
        }
    }
}

/**
 * The changelog window: a large, fixed-size centered {@link GPanel} frame (with a close X via
 * {@code clickActionSet}) wrapping a scrollable {@link GBox} that lists every pending mod's notice as
 * its own section (a scaled title, a divider, then the change lines). One window, one dismissal.
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
 * <p><b>CHANGES markup</b> (each array entry is one line): {@code "# Heading"} → heading (title-sized);
 * {@code "## Heading"} → smaller sub-heading; {@code "---"} (3+ dashes) → horizontal divider;
 * {@code ""} → blank spacer; {@code "[tag] text"} → colored text (tags: {@code good great bad worst
 * warn error label sub normal}; unknown tag is left literal). A color tag may precede a heading.
 */
final class UpdateNoticeWindow extends Interrupter {

    /** Fixed inner size of the window (px, in the game's virtual UI resolution). Deliberately large. */
    private static final int INNER_W = 900;
    private static final int INNER_H = 560;
    /** Inner padding between the frame and the text, and scale applied to all text. */
    private static final int PAD = 20;
    private static final double SCALE = 1.5;
    /** Extra vertical gap after a blank-line spacer / after a heading. */
    private static final int SPACER_GAP = 14;
    private static final int HEADING_GAP = 6;

    private final GBox content = new GBox();
    private final GPanel frame = new GPanel();
    private final ACTION exit = new ACTION() {
        @Override public void exe() { hide(); }
    };

    UpdateNoticeWindow(List<UpdateNotifier.Notice> notices) {
        super(true, true); // persistent + pinned — survive load-time interrupter churn; stay until closed
        frame.setBig();
        content.clear();
        content.maxWidth = INNER_W - PAD * 2;   // wrap within the window
        content.maxHeight = INNER_H - PAD * 2;  // scroll (mouse wheel) when taller than this

        for (int i = 0; i < notices.size(); i++) {
            UpdateNotifier.Notice n = notices.get(i);
            if (i > 0) {                       // separate each mod's section
                content.NL(SPACER_GAP);
                content.sep();
                content.NL(SPACER_GAP);
            }
            addLine(n.title, UI.FONT().H1, GCOLOR.T().H1, SCALE + 0.4, HEADING_GAP);
            content.sep();
            buildContent(n.lines);
        }
    }

    /** Adds one styled, scaled line to the content box, followed by a newline with {@code gap} extra px. */
    private void addLine(String text, Font font, COLOR color, double scale, int gap) {
        GText t = content.text();
        t.setFont(font);
        t.setScale(scale);
        if (color != null) t.color(color); else t.normalify();
        t.add(text);
        content.add(t);
        content.NL(gap);
    }

    /** Renders one mod's CHANGES lines using the lightweight markup documented on this class. */
    private void buildContent(String[] lines) {
        if (lines == null || lines.length == 0) {
            addLine("(no changes listed)", UI.FONT().S, null, SCALE, 0);
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

            // Optional heading marker: ## or #. Sized under the title: "#" -> H2 (strong H1 color),
            // "##" -> M (H2 color). Plain lines are body text (Small).
            Font font = UI.FONT().S;
            COLOR headingColor = null;
            int gap = 0;
            if (s.startsWith("## ")) {
                font = UI.FONT().M; headingColor = GCOLOR.T().H2; s = s.substring(3); gap = HEADING_GAP;
            } else if (s.startsWith("# ")) {
                font = UI.FONT().H2; headingColor = GCOLOR.T().H1; s = s.substring(2); gap = HEADING_GAP;
            }

            addLine(s, font, color != null ? color : headingColor, SCALE, gap);
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

    @Override
    protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
        // frame.hover consumes the X close click; keep hover over the whole window so it stays interactive
        // (and the mouse wheel scrolls the content) and inside-clicks don't fall through to the game.
        return frame.hover(mCoo) || mCoo.isWithinRec(frame.body());
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
        int x1 = C.WIDTH() / 2 - INNER_W / 2;
        int y1 = C.HEIGHT() / 2 - INNER_H / 2;

        Rec inner = frame.inner();
        inner.setWidth(INNER_W);
        inner.setHeight(INNER_H);
        inner.moveX1(x1);
        inner.moveY1(y1);

        frame.clickActionSet(exit); // draws the close (X) button and wires its click
        frame.render(r, ds);
        content.renderWithout(r, x1 + PAD, y1 + PAD); // content only (no nested panel), scrolls if tall
        return false; // top-most: stop VIEW from drawing the UI/foreground over us
    }

    @Override
    protected boolean update(float ds) {
        if (KEYS.MAIN().ESCAPE.consumeClick()) hide();
        return false;
    }
}
