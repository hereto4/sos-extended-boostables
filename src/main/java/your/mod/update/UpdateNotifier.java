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
import snake2d.util.misc.CLAMP;
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
 * The changelog window: a large, fixed-size centered {@link GPanel} frame (with a close X), listing every
 * pending mod's notice as its own section (a title, a divider, then the change lines). One window, one
 * dismissal.
 *
 * <p><b>Manual layout.</b> The content is pre-laid-out into a flat list of {@link Row}s at construction —
 * each row is a single visual line of text (already word-wrapped by us), a divider, or a spacer, with an
 * explicit pixel height. {@link #render} draws each row at an explicit Y (with mouse-wheel scrolling and
 * clipping to the window). We do NOT use {@code GBox}: driving the layout ourselves is what fixed the
 * overlapping/ghosted text that GBox produced for long wrapped paragraphs.
 *
 * <p><b>Rendering returns {@code false}.</b> In {@code VIEW.render} the interrupter manager is drawn after
 * the terrain background but before the UI/foreground passes; returning {@code false} makes {@code VIEW}
 * stop there, so this window is the top-most thing drawn (as the engine's own {@code IPromtScreen} does).
 *
 * <p>Constructed {@code persistent + pinned} so it survives the interrupter churn during the
 * load→gameplay transition. It closes only on the X, ESC, or a right-click — deliberately NOT on a
 * left-click, so a stray click buffered during loading can't dismiss it before the player sees it.
 *
 * <p><b>CHANGES markup</b> (each array entry is one line): {@code "# Heading"} → heading (title-sized);
 * {@code "## Heading"} → smaller sub-heading; {@code "---"} (3+ dashes) → horizontal divider;
 * {@code ""} → blank spacer. Color is inline: {@code [tag]} switches color for the following text and
 * {@code [/]} reverts it, so {@code "kill [good]bonus[/] applied"} colors just one word while
 * {@code "[warn] whole line"} (unclosed) colors the rest. Tags: {@code good great bad worst warn error
 * label sub normal}; an unknown tag is left as literal text.
 */
final class UpdateNoticeWindow extends Interrupter {

    /** Window width, and preferred height — the actual height is clamped to the screen (see ctor). */
    private static final int INNER_W = 680;
    private static final int PREF_H = 760;
    private static final int V_MARGIN = 120;
    private static final int PAD = 20;
    private static final double SCALE = 1.0; // 1.0 = the game's natural font sizes
    private static final int LINE_GAP = 8;   // vertical gap after a full line (a real CHANGES entry)
    private static final int WRAP_GAP = 2;   // tighter gap between the wrapped rows of one paragraph
    private static final int HEADING_GAP = 12;
    private static final int SPACER_GAP = 16; // blank-line height
    private static final int DIVIDER_H = 14;  // divider-row height
    private static final int SCROLL_STEP = 40;

    private static final int T_TEXT = 0, T_DIVIDER = 1, T_SPACER = 2;

    private final int winW;
    private final int winH;
    private final List<Row> rows = new ArrayList<>();
    private final int totalH;
    private int scrollOffset = 0;

    /** Scrollbar geometry, refreshed each render() so mouseClick() can hit-test it. */
    private int barX1 = -1, barX2 = -1, barTop = 0, barVisibleH = 0, barMaxScroll = 0;
    /** Latest mouse position (from hover()), used for scrollbar click handling. */
    private int lastMouseX = 0, lastMouseY = 0;

    private final GPanel frame = new GPanel();
    /** Scratch text reused for width/height measurement (build) and drawing (render). */
    private final GText scratch = new GText(UI.FONT().S, 512);
    private final ACTION exit = new ACTION() {
        @Override public void exe() { hide(); }
    };

    /** One text segment (a run of one color) within a text row. */
    private static final class Seg {
        final String text; final Font font; final COLOR color;
        Seg(String text, Font font, COLOR color) { this.text = text; this.font = font; this.color = color; }
    }

    /** One laid-out row: a single visual line of text, a divider, or a blank spacer. */
    private static final class Row {
        final int type; final List<Seg> segs; final int height; final int gapAfter;
        Row(int type, List<Seg> segs, int height, int gapAfter) {
            this.type = type; this.segs = segs; this.height = height; this.gapAfter = gapAfter;
        }
    }

    /** A single word with its color, used while color-aware word-wrapping a line. */
    private static final class Tok {
        final String word; final COLOR color;
        Tok(String word, COLOR color) { this.word = word; this.color = color; }
    }

    UpdateNoticeWindow(List<UpdateNotifier.Notice> notices) {
        super(true, true); // persistent + pinned — survive load-time interrupter churn; stay until closed
        frame.setBig();

        // Tall, but never taller than the screen: clamp the preferred height to the available height.
        winW = Math.min(INNER_W, Math.max(360, C.WIDTH() - 80));
        winH = Math.max(360, Math.min(PREF_H, C.HEIGHT() - V_MARGIN));

        for (int i = 0; i < notices.size(); i++) {
            UpdateNotifier.Notice n = notices.get(i);
            if (i > 0) {                              // separate each mod's section
                rows.add(spacer());
                rows.add(divider());
                rows.add(spacer());
            }
            addLine(n.title, UI.FONT().H1, GCOLOR.T().H1, HEADING_GAP);
            rows.add(divider());
            buildContent(n.lines);
        }

        int h = 0;
        for (Row row : rows) h += row.height + row.gapAfter;
        totalH = h;
    }

    // ===== layout (build) =====

    private void buildContent(String[] lines) {
        if (lines == null || lines.length == 0) {
            addLine("(no changes listed)", UI.FONT().S, null, LINE_GAP);
            return;
        }
        for (String raw : lines) {
            String s = (raw == null ? "" : raw).trim();

            if (s.isEmpty()) { rows.add(spacer()); continue; }
            if (s.length() >= 3 && s.chars().allMatch(c -> c == '-')) { rows.add(divider()); continue; }

            Font font = UI.FONT().S;
            COLOR baseColor = null;
            int gap = LINE_GAP;
            if (s.startsWith("## ")) {
                font = UI.FONT().M; baseColor = GCOLOR.T().H2; s = s.substring(3); gap = HEADING_GAP;
            } else if (s.startsWith("# ")) {
                font = UI.FONT().H2; baseColor = GCOLOR.T().H1; s = s.substring(2); gap = HEADING_GAP;
            }

            addLine(s, font, baseColor, gap);
        }
    }

    /**
     * Parses one line's inline {@code [tag]}…{@code [/]} color markup into colored words, then greedily
     * word-wraps those to the content width — emitting one {@link Row} per visual line, where each row may
     * contain several colored {@link Seg}s. This wraps correctly whether or not the line uses inline color.
     */
    private void addLine(String s, Font font, COLOR baseColor, int gap) {
        // 1) split the line into (word, color) tokens
        List<Tok> toks = new ArrayList<>();
        COLOR cur = baseColor;
        StringBuilder run = new StringBuilder();
        int i = 0;
        while (i < s.length()) {
            char ch = s.charAt(i);
            if (ch == '[') {
                int end = s.indexOf(']', i);
                if (end > i) {
                    String tag = s.substring(i + 1, end).trim().toLowerCase();
                    if (tag.equals("/")) { addWords(toks, run.toString(), cur); run.setLength(0); cur = baseColor; i = end + 1; continue; }
                    COLOR c = colorFor(tag);
                    if (c != null) { addWords(toks, run.toString(), cur); run.setLength(0); cur = c; i = end + 1; continue; }
                }
                run.append(ch); i++;
            } else {
                run.append(ch); i++;
            }
        }
        addWords(toks, run.toString(), cur);

        if (toks.isEmpty()) {                       // nothing to draw
            rows.add(new Row(T_SPACER, null, gap, 0));
            return;
        }

        // 2) greedy-wrap the tokens to the content width, one Row per visual line
        int maxW = winW - PAD * 2;
        int lh = lineHeight(font);
        List<Tok> line = new ArrayList<>();
        StringBuilder lineText = new StringBuilder();
        for (Tok t : toks) {
            String piece = t.word + " ";
            if (!line.isEmpty() && measureWidth(lineText.toString() + piece, font) > maxW) {
                // a wrapped continuation follows → this row gets the tight WRAP_GAP
                rows.add(makeRow(line, font, lh, WRAP_GAP));
                line = new ArrayList<>();
                lineText.setLength(0);
            }
            line.add(t);
            lineText.append(piece);
        }
        // the paragraph's final visual line gets the full line-break gap
        if (!line.isEmpty()) rows.add(makeRow(line, font, lh, gap));
    }

    /** Splits {@code text} into words, tagging each with {@code color}. */
    private static void addWords(List<Tok> toks, String text, COLOR color) {
        for (String w : text.split("\\s+")) if (!w.isEmpty()) toks.add(new Tok(w, color));
    }

    /** Builds one visual-line row from its tokens, merging consecutive same-color words into segments. */
    private Row makeRow(List<Tok> line, Font font, int height, int gap) {
        List<Seg> segs = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        COLOR segColor = line.get(0).color;
        for (Tok t : line) {
            if (t.color != segColor) {              // color change → close the current segment
                segs.add(new Seg(sb.toString(), font, segColor));
                sb.setLength(0);
                segColor = t.color;
            }
            sb.append(t.word).append(' ');
        }
        if (sb.length() > 0) segs.add(new Seg(sb.toString(), font, segColor));
        return new Row(T_TEXT, segs, height, gap);
    }

    private Row divider() { return new Row(T_DIVIDER, null, DIVIDER_H, 0); }
    private Row spacer()  { return new Row(T_SPACER, null, SPACER_GAP, 0); }

    /** Single-line pixel width of {@code s} in {@code font} at {@link #SCALE} (no wrapping). */
    private int measureWidth(String s, Font font) {
        scratch.clear();
        scratch.setFont(font);
        scratch.setScale(SCALE);
        scratch.setMaxWidth(1_000_000);
        scratch.add(s);
        scratch.adjustWidth();
        return scratch.width();
    }

    /** Single-line pixel height of {@code font} at {@link #SCALE}. */
    private int lineHeight(Font font) {
        scratch.clear();
        scratch.setFont(font);
        scratch.setScale(SCALE);
        scratch.setMaxWidth(1_000_000);
        scratch.add("Ag");
        scratch.adjustWidth();
        return scratch.height();
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

    // ===== interrupter =====

    @Override
    protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
        lastMouseX = mCoo.x();
        lastMouseY = mCoo.y();
        return frame.hover(mCoo) || mCoo.isWithinRec(frame.body());
    }

    @Override
    protected void mouseClick(MButt button) {
        if (button == MButt.RIGHT) { hide(); return; }
        // Left-click on the scrollbar jumps the view to that position.
        if (button == MButt.LEFT && barMaxScroll > 0
                && lastMouseX >= barX1 && lastMouseX <= barX2
                && lastMouseY >= barTop && lastMouseY <= barTop + barVisibleH) {
            int rel = lastMouseY - barTop;
            scrollOffset = CLAMP.i((int) ((long) rel * barMaxScroll / Math.max(1, barVisibleH)), 0, barMaxScroll);
        }
    }

    @Override
    protected boolean otherClick(MButt button) {
        if (button == MButt.RIGHT) { hide(); return true; }
        return false;
    }

    @Override
    protected void hoverTimer(GBox text) {}

    @Override
    protected boolean render(Renderer r, float ds) {
        int x1 = C.WIDTH() / 2 - winW / 2;
        int y1 = C.HEIGHT() / 2 - winH / 2;

        Rec inner = frame.inner();
        inner.setWidth(winW);
        inner.setHeight(winH);
        inner.moveX1(x1);
        inner.moveY1(y1);
        frame.clickActionSet(exit); // draws the close (X) button and wires its click
        frame.render(r, ds);

        int left = x1 + PAD;
        int top = y1 + PAD;
        int visibleH = winH - PAD * 2;
        int bottom = top + visibleH;
        int maxScroll = Math.max(0, totalH - visibleH);

        // Mouse-wheel scroll (the window is modal, so consuming the wheel here is fine).
        double dv = MButt.clearWheelSpin();
        if (dv < 0) scrollOffset += SCROLL_STEP;
        else if (dv > 0) scrollOffset -= SCROLL_STEP;
        scrollOffset = CLAMP.i(scrollOffset, 0, maxScroll);

        int y = top - scrollOffset;
        for (Row row : rows) {
            if (y >= top && y + row.height <= bottom) { // only draw fully-visible rows
                if (row.type == T_TEXT) {
                    int sx = left;
                    for (Seg seg : row.segs) {
                        scratch.clear();
                        scratch.setFont(seg.font);
                        scratch.setScale(SCALE);
                        scratch.setMaxWidth(1_000_000); // segments are pre-wrapped; never wrap one internally
                        if (seg.color != null) scratch.color(seg.color); else scratch.normalify();
                        scratch.add(seg.text);
                        scratch.adjustWidth();
                        scratch.render(r, sx, sx + 1_000_000, y, y + row.height);
                        sx += scratch.width();
                    }
                } else if (row.type == T_DIVIDER) {
                    int dy = y + row.height / 2;
                    GCOLOR.UI().border().render(r, left, left + (winW - PAD * 2), dy, dy + 1);
                }
            }
            y += row.height + row.gapAfter;
        }

        // Scrollbar (only when the content overflows the window).
        barTop = top;
        barVisibleH = visibleH;
        barMaxScroll = maxScroll;
        if (maxScroll > 0) {
            barX2 = x1 + winW - 6;
            barX1 = barX2 - 8;
            COLOR.WHITE20.render(r, barX1, barX2, top, bottom); // track
            int thumbH = Math.max(24, (int) ((long) visibleH * visibleH / totalH));
            int thumbY = top + (int) ((long) (visibleH - thumbH) * scrollOffset / maxScroll);
            COLOR.WHITE150.render(r, barX1, barX2, thumbY, thumbY + thumbH); // thumb
        } else {
            barX1 = barX2 = -1;
        }
        return false; // top-most: stop VIEW from drawing the UI/foreground over us
    }

    @Override
    protected boolean update(float ds) {
        if (KEYS.MAIN().ESCAPE.consumeClick()) hide();
        return false;
    }
}
