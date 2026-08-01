package your.mod.boostformat;

import java.util.ArrayList;
import java.util.List;

import game.boosting.BOOSTING;
import game.boosting.BoostSpec;
import init.tech.TECH;
import init.tech.TECHS;
import snake2d.util.misc.ACTION;

/**
 * Per-key colour control for the effect list shown in a <b>tech node's</b> tooltip.
 *
 * <p>The engine picks that colour from the value alone, so any boostable where a LOWER value is the
 * better outcome reads backwards, and a genuinely neutral effect cannot be shown as neutral at all.
 * Where we can add a key we solve this with a high-positive front key (see
 * {@code MainScript.registerInverseFront}); where the effect is neutral, or the key is vanilla and
 * we do not want to add one, this layer re-colours the line instead.
 *
 * <h2>How</h2>
 * At the {@code BOOSTING.connecter} hook — which fires after every tech's {@code BoostSpecs} promise
 * list has been drained and populated — each tech's boost list is scanned and any spec whose boostable
 * matches a {@link #RULES} entry has its {@link game.boosting.Booster} swapped for a
 * {@link FormattedBooster}. That wrapper forwards every value method and overrides only
 * {@code format(GText,double)}.
 *
 * <h2>Scope and safety</h2>
 * <ul>
 *   <li><b>Display only.</b> A tech's {@code BoostSpecs} has {@code connect == false}
 *       ({@code TECH.java:122}), so mutating it never touches the target boostable's factor list. The
 *       gameplay effect is unchanged, and unchanged even in principle.</li>
 *   <li><b>Tech node only.</b> The Boosts browser and the boostable tooltips colour inline in
 *       {@code BoostSpecs.hover}/{@code BoosterAbs.hover}, which never call {@code format}. Those
 *       surfaces are unreachable without bytecode patching and stay vanilla.</li>
 *   <li><b>Order preserving.</b> The list is rebuilt in its original order (each spec is removed and
 *       pushed back in sequence), so a co-installed effect sorter is not disturbed.</li>
 *   <li><b>Idempotent.</b> Already-wrapped specs are left alone, so a second pass is harmless.</li>
 * </ul>
 *
 * <p>Install from {@code initBeforeGameInited()}, <b>after</b> {@code TargetFilters.install()} so that
 * this connecter is queued after the one that re-adds original specs for the UI — otherwise those
 * re-added specs would arrive unwrapped.
 */
public final class BoostFormats {

    /**
     * Colour rules, evaluated in order; the first match wins. A key ending in {@code *} matches by
     * prefix, otherwise the boostable key must match exactly.
     *
     * <ul>
     *   <li>{@code NEUTRAL} — always render this key's value in the engine's neutral colour. For
     *       effects that are neither good nor bad, which the engine has no way to express (its neutral
     *       colour is reserved for a literal no-op value).</li>
     *   <li>{@code INVERTED} — swap the good/bad colours (blue &harr; red), for a "lower is better"
     *       (low-positive) key: raising it is a cost, lowering it is a benefit.</li>
     * </ul>
     *
     * <p>{@code ACTIVITY_*} (Judgement / Mourning / Punishment / Social) are idle-activity desire
     * weights — how often a subject <em>wants</em> to do something while idle. They cost no work time
     * and are considered neutral, so they are rendered neutral (user decision, 2026-07-30).
     *
     * <p>The {@code RATES_*} entries are listed <b>individually rather than by prefix</b> on purpose:
     * the prefix is shared by keys that are genuinely high-positive and must NOT be inverted — this
     * mod's own {@code RATES_NATURE} (&gt;1 = loves nature) and, while they exist, the
     * {@code RATES_*} front keys (Satiety, Hydration, …). Every vanilla need rate is a need-GROWTH
     * rate ({@code init/type/NEED.java:59}), so on all of them a lower value is the better outcome.
     */
    private static final String[][] RULES = {
        { "ACTIVITY_*", "NEUTRAL" },

        // Low-positive: the rate at which a subject becomes dirty.
        { "PHYSICS_SOILING", "INVERTED" },

        // Low-positive: every vanilla need-growth rate. (NOT RATES_NATURE — ours, high-positive.)
        { "RATES_HUNGER", "INVERTED" },
        { "RATES_THIRST", "INVERTED" },
        { "RATES_SHOPPING", "INVERTED" },
        { "RATES_WELL", "INVERTED" },
        { "RATES_CONSTIPATION", "INVERTED" },
        { "RATES_BATH", "INVERTED" },
        { "RATES_HEARTH", "INVERTED" },
        { "RATES_ARENA", "INVERTED" },
        { "RATES_ARENAG", "INVERTED" },
        { "RATES_STAGE", "INVERTED" },
        { "RATES_SPEAKER", "INVERTED" },
        { "RATES_GROOMING", "INVERTED" },
        { "RATES_MASSAGE", "INVERTED" },
        { "RATES_DOCTOR", "INVERTED" },
        { "RATES_SKINNYDIP", "INVERTED" },
        { "RATES_STOCKS", "INVERTED" },
        { "RATES_COURT", "INVERTED" },
        { "RATES_SHRINE", "INVERTED" },
        { "RATES_TEMPLE", "INVERTED" },
    };

    private BoostFormats() {}

    public static void install() {
        BOOSTING.connecter(new ACTION() {
            @Override
            public void exe() {
                applyAll();
            }
        });
    }

    private static void applyAll() {
        int wrapped = 0;
        int techsTouched = 0;

        for (TECH tech : TECHS.ALL()) {
            // tech.boosters.all() is a live view — snapshot before mutating.
            List<BoostSpec> snapshot = new ArrayList<>();
            for (BoostSpec s : tech.boosters.all())
                snapshot.add(s);
            if (snapshot.isEmpty())
                continue;

            boolean any = false;
            for (BoostSpec s : snapshot) {
                if (modeFor(s) != null && !(s.booster instanceof FormattedBooster)) {
                    any = true;
                    break;
                }
            }
            if (!any)
                continue;

            // Rebuild in place: remove each spec and push it (or its replacement) straight back. Because
            // we walk the snapshot in order and always push to the end, the list ends in its original
            // order. push/remove are no-ops on the boostable itself here (connect == false).
            for (BoostSpec s : snapshot) {
                tech.boosters.remove(s);
                FormattedBooster.Mode mode = modeFor(s);
                if (mode == null || s.booster instanceof FormattedBooster) {
                    tech.boosters.push(s);
                    continue;
                }
                tech.boosters.push(new BoostSpec(new FormattedBooster(s.booster, mode), s.boostable, appendOf(s)));
                wrapped++;
            }
            techsTouched++;
        }

        System.out.println("[sos-extended-boostables] tech-tooltip colour override: re-coloured "
                + wrapped + " effect line(s) across " + techsTouched + " tech(s).");
    }

    /** The rule that applies to this spec's boostable, or {@code null} if none does. */
    private static FormattedBooster.Mode modeFor(BoostSpec s) {
        return s.boostable == null ? null : modeForKey(s.boostable.key);
    }

    /**
     * The colour rule that applies to a boostable key, or {@code null} if none does — i.e. the key's
     * polarity as this mod understands it: {@code INVERTED} = lower is better, {@code NEUTRAL} = neither
     * good nor bad, {@code null} = ordinary (higher is better).
     *
     * <p>Public because it is the single source of truth for polarity: {@link BoostOrder} uses it to
     * decide where a boost line belongs in a tech's effect list, so the ordering and the colouring can
     * never disagree.
     */
    public static FormattedBooster.Mode modeForKey(String key) {
        if (key == null)
            return null;
        for (String[] rule : RULES) {
            String pattern = rule[0];
            boolean hit = pattern.endsWith("*")
                    ? key.startsWith(pattern.substring(0, pattern.length() - 1))
                    : key.equals(pattern);
            if (hit)
                return "NEUTRAL".equals(rule[1]) ? FormattedBooster.Mode.NEUTRAL : FormattedBooster.Mode.INVERTED;
        }
        return null;
    }

    /**
     * Recovers the {@code append} a {@link BoostSpec} was built with, so the replacement keeps the same
     * {@code tName} (used as the row label in {@code BoostSpecs.hover}). {@code BoostSpec} stores only
     * the composed {@code "<name> (<append>)"}, so it is read back off the end; {@code null} when the
     * spec carried no append.
     */
    private static CharSequence appendOf(BoostSpec s) {
        if (s.tName == null || s.boostable == null || s.boostable.name == null)
            return null;
        String tName = s.tName.toString();
        String name = s.boostable.name.toString();
        String wrap = name + " (";
        if (tName.length() > wrap.length() && tName.startsWith(wrap) && tName.endsWith(")"))
            return tName.substring(wrap.length(), tName.length() - 1);
        return null;
    }
}
