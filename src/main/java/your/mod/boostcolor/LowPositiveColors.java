package your.mod.boostcolor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import snake2d.util.color.COLOR;
import util.colors.GCOLOR;

/**
 * Runtime support for "Low-Positive" boostable tooltip coloring.
 *
 * <p>The engine colors a boost line <b>green</b> when its modifier raises a boostable
 * (ADD &gt; 0 / MUL &gt; 1) and <b>red</b> when it lowers it (ADD &lt; 0 / MUL &lt; 1). That is correct
 * for the common case where a higher value is better for the player ("High-Positive").
 *
 * <p>Some effects are the inverse: a <em>lower</em> value is better ("Low-Positive"), e.g.
 * {@code PHYSICS_SOILING} (lower soiling = less filth). For those, the engine's mapping is backwards.
 * This helper flips green&lt;-&gt;red for the boostables listed in {@link #KEYS} while leaving the
 * displayed number untouched.
 *
 * <h2>How it is used</h2>
 * The engine code that picks the color is hardcoded ({@code BoosterAbs.hover}, {@code BoostSpecs.hover})
 * and has no modding seam, so {@link ColorAgent} rewrites those methods at runtime so that:
 * <ul>
 *   <li>each read of {@code GCOLOR.T().IGOOD} / {@code .IBAD} is routed through {@link #pick(COLOR, boolean)}, and</li>
 *   <li>the boostable key currently being rendered is made available via {@link #pushKey(String)} /
 *       {@link #popKey()} around each render scope.</li>
 * </ul>
 * If {@code pick} sees that the current boostable is Low-Positive it returns the opposite color; otherwise
 * it returns the color the engine chose. Nothing else about the tooltip changes.
 *
 * <p>All members are {@code public static} and the fully-qualified name is referenced from injected
 * bytecode — do not rename or move this class without updating {@link ColorAgent}.
 */
public final class LowPositiveColors {

    /**
     * Vanilla boostable keys whose tooltip color should be inverted because a <em>lower</em> value is
     * the positive outcome for the player. Edit this list to cover more effects. Keys are the engine
     * boostable keys (category prefix + name), e.g. {@code "PHYSICS_SOILING"}.
     */
    public static final Set<String> KEYS = Collections.synchronizedSet(new HashSet<String>());
    static {
        KEYS.add("PHYSICS_SOILING"); // "Soiling" — the rate at which a subject becomes dirty (lower is better)
    }

    /**
     * The boostable key currently being rendered, as a per-thread stack so nested tooltips behave.
     * Set/cleared by {@link #pushKey(String)} / {@link #popKey()} from injected engine bytecode.
     */
    private static final ThreadLocal<java.util.ArrayDeque<String>> STACK =
            new ThreadLocal<java.util.ArrayDeque<String>>() {
                @Override
                protected java.util.ArrayDeque<String> initialValue() {
                    return new java.util.ArrayDeque<String>();
                }
            };

    private LowPositiveColors() {}

    /** Marks {@code key} as the boostable being rendered for the current scope. */
    public static void pushKey(String key) {
        STACK.get().push(key == null ? "" : key);
    }

    /** Ends the scope opened by the matching {@link #pushKey(String)}. Safe to over-call. */
    public static void popKey() {
        java.util.ArrayDeque<String> s = STACK.get();
        if (!s.isEmpty())
            s.pop();
    }

    private static boolean currentIsInverted() {
        java.util.ArrayDeque<String> s = STACK.get();
        String k = s.isEmpty() ? null : s.peek();
        return k != null && KEYS.contains(k);
    }

    /**
     * Replacement for a read of {@code GCOLOR.T().IGOOD} ({@code good == true}) or {@code .IBAD}
     * ({@code good == false}). Returns the opposite theme color when the boostable currently being
     * rendered is Low-Positive, otherwise returns {@code original} unchanged.
     *
     * @param original the color the engine selected (the result of the original field read)
     * @param good     {@code true} if the engine read the "good"/green color, {@code false} for "bad"/red
     */
    public static COLOR pick(COLOR original, boolean good) {
        if (currentIsInverted())
            return good ? GCOLOR.T().IBAD : GCOLOR.T().IGOOD;
        return original;
    }
}
