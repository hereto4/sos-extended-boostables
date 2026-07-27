package your.mod.targetfilter;

import java.lang.reflect.Field;

import snake2d.util.file.Json;

/**
 * Suppresses the engine's "unknown key: TARGET_RACE/TARGET_CLASS in object at
 * line: ..." stderr warning that {@code snake2d.util.file.Json.checkUnused()}
 * emits when the TECH constructor finishes parsing a tech node carrying one of
 * our custom keys.
 *
 * <p>Two points of attack (both verified against game source 71.19):
 * <ul>
 *   <li>{@link #suppressUnknownKeyWarnings()} flips {@code Json.untest} true
 *       before the engine parses tech files, so every {@code checkUnused()} call
 *       short-circuits and no unknown-key warning is written.</li>
 *   <li>{@link #scrubFromErrBuffer()} is a safety net that strips any leaked
 *       "unknown key: ...TARGET_*..." block from the 250KB {@code StringBuffer}
 *       behind {@code UnhandledDump.txt} ({@code snake2d.Errors}).</li>
 * </ul>
 *
 * <p>Generalized from the standalone target-race-tech mod's
 * {@code ParseWarningSuppressor} to cover both {@code TARGET_RACE} and
 * {@code TARGET_CLASS}. Cosmetic-only: any reflection failure is logged and
 * ignored.
 */
final class ParseWarningSuppressor {

    private static final String[] KEYS = { TargetFilterRegistry.KEY_RACE, TargetFilterRegistry.KEY_CLASS };

    private ParseWarningSuppressor() {}

    /**
     * Flip {@code Json.untest} true before the engine parses tech files (called
     * from {@code initBeforeGameCreated}, before {@code new TECHS()}). With it
     * set, every {@code checkUnused()} call short-circuits. Side effect: also
     * suppresses legitimate unknown-key warnings for the rest of the session —
     * but the engine flips the same flag itself on the first unknown key from
     * anywhere, so the warning is a one-shot regardless.
     */
    static void suppressUnknownKeyWarnings() {
        try {
            Field f = Json.class.getDeclaredField("untest");
            f.setAccessible(true);
            f.setBoolean(null, true);
            boolean readBack = f.getBoolean(null);
            TargetFilterRegistry.announce("suppressed Json.checkUnused() warnings (Json.untest=true; read-back=" + readBack + ")");
        } catch (Throwable t) {
            TargetFilterRegistry.announce("could not set Json.untest (reflection failed: " + t + "); relying on err-buffer scrub instead");
        }
    }

    /**
     * Safety net: strip any "unknown key: ... TARGET_RACE/TARGET_CLASS ..." block
     * that already made it into the stderr buffer before suppression took effect.
     * Called from {@code initBeforeGameInited}, after {@code new TECHS()} has
     * parsed every tech file. Only removes blocks that mention {@code "unknown
     * key:"} and one of our keys, so unrelated stderr content is untouched.
     */
    static void scrubFromErrBuffer() {
        try {
            Class<?> errorsClass = Class.forName("snake2d.Errors");
            Field iField = errorsClass.getDeclaredField("i");
            iField.setAccessible(true);
            Object errorsInstance = iField.get(null);
            if (errorsInstance == null) {
                TargetFilterRegistry.announce("scrub: Errors.i is null, skipping");
                return;
            }

            Field errField = errorsClass.getDeclaredField("err");
            errField.setAccessible(true);
            Object logger = errField.get(errorsInstance);

            Field dataField = logger.getClass().getDeclaredField("data");
            dataField.setAccessible(true);
            StringBuffer data = (StringBuffer) dataField.get(logger);

            String original = data.toString();
            String cleaned = original;
            for (String key : KEYS) cleaned = stripMatchingLines(cleaned, key);
            int removed = original.length() - cleaned.length();
            if (removed > 0) {
                data.setLength(0);
                data.append(cleaned);
                TargetFilterRegistry.announce("scrubbed " + removed + " char(s) of TARGET_* warning from stderr buffer");
            } else {
                TargetFilterRegistry.announce("scrub: no TARGET_* warning in stderr buffer (suppression worked, or none fired)");
            }
        } catch (Throwable t) {
            TargetFilterRegistry.announce("scrub failed (reflection): " + t);
        }
    }

    /**
     * Removes the ENTIRE warning block for one key: {@code Json.checkUnused()}
     * emits a header line ("unknown key: KEY in object at line: N. path"), an
     * "available:" line, a variable-length list of registered keys, then a
     * terminating blank line. Newline-agnostic (handles Windows \r\n) and quotes
     * the key so regex metachars can't break it.
     */
    private static String stripMatchingLines(String s, String key) {
        if (s.isEmpty()) return s;
        java.util.regex.Pattern block = java.util.regex.Pattern.compile(
                "(?m)^unknown key: [^\\r\\n]*" + java.util.regex.Pattern.quote(key) + "[^\\r\\n]*\\r?\\n" // header line
              + "available: ?\\r?\\n"          // "available: " line
              + "(?:[^\\r\\n]+\\r?\\n)*"        // zero or more non-empty key lines
              + "\\r?\\n");                     // terminating blank line
        return block.matcher(s).replaceAll("");
    }
}
