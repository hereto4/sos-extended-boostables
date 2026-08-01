package your.mod.boostformat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import game.boosting.BOOSTING;
import game.boosting.BoostSpec;
import init.tech.TECH;
import init.tech.TECHS;
import snake2d.util.misc.ACTION;

/**
 * Re-orders the effect list shown in a tech node's tooltip, by usefulness rather than alphabetically.
 *
 * <p>The engine populates each {@code tech.boosters} list and sorts it alphabetically while draining
 * {@code BOOSTING.waiting} at the start of {@code BOOSTING.finishSetup()}; the tooltip
 * ({@code view.ui.tech.Node.hoverInfoGet}) then simply iterates that list. So re-ordering the list is
 * the whole job — no UI code is touched, and (since a {@code TECH}'s {@code BoostSpecs} is built with
 * {@code connect == false}) nothing here can affect gameplay.
 *
 * <h2>Order</h2>
 * <ol>
 *   <li><b>Additive benefits</b> — e.g. {@code >ADD} on an ordinary key.</li>
 *   <li><b>Multiplicative benefits</b> — strongest first.</li>
 *   <li><b>Costs</b> — every effect that makes the player worse off, whichever operator produced it.</li>
 *   <li><b>Neutral</b> — keys marked {@code NEUTRAL} in {@link BoostFormats}, at the very bottom.</li>
 * </ol>
 *
 * <p><b>Polarity-aware.</b> "Benefit" and "cost" are judged after applying the key's polarity from
 * {@link BoostFormats#modeForKey}, the same table that drives the colouring — so a line's position and
 * its colour can never disagree. On a low-positive ({@code INVERTED}) key such as {@code RATES_HUNGER}
 * or {@code PHYSICS_SOILING}, a value <em>above</em> the neutral point is a cost and sorts down with the
 * negatives, while a value below it is a benefit and sorts up with the other gains.
 *
 * <p>Within a tier, entries are ordered by <b>benefit magnitude, strongest first</b> — measured after
 * polarity, so an inverted {@code x0.5} correctly outranks an inverted {@code x0.9}. In the cost tier
 * that same rule puts the mildest cost first, matching how the original sorter ordered its negatives.
 *
 * <p>Ported into this mod 2026-07-31 from the standalone {@code tech-boost-sort} jar, which had no
 * access to the polarity table. <b>That jar must not be loaded alongside this one</b> — both register a
 * {@code BOOSTING.connecter} that reorders the same list, and whichever runs last wins.
 */
public final class BoostOrder {

    private BoostOrder() {}

    public static void install() {
        BOOSTING.connecter(new ACTION() {
            @Override
            public void exe() {
                sortAll();
            }
        });
    }

    private static final Comparator<BoostSpec> BY_USEFULNESS = new Comparator<BoostSpec>() {
        @Override
        public int compare(BoostSpec a, BoostSpec b) {
            int ta = tier(a);
            int tb = tier(b);
            if (ta != tb)
                return Integer.compare(ta, tb);
            return Double.compare(strength(b), strength(a)); // strongest benefit first
        }
    };

    /** 0 = additive benefit, 1 = multiplicative benefit, 2 = cost, 3 = neutral (bottom). */
    private static int tier(BoostSpec s) {
        if (BoostFormats.modeForKey(s.boostable.key) == FormattedBooster.Mode.NEUTRAL)
            return 3;
        if (strength(s) <= 0)
            return 2; // a cost, or an exactly-neutral value: below every real gain
        return s.booster.isMul ? 1 : 0;
    }

    /**
     * How much this effect helps the player: positive = benefit, negative = cost, 0 = no change.
     * Measured as the distance from the operator's neutral point ({@code 1} for {@code >MUL},
     * {@code 0} for {@code >ADD}), with the sign flipped for a low-positive key.
     */
    private static double strength(BoostSpec s) {
        double v = s.booster.to();
        double delta = s.booster.isMul ? v - 1.0 : v;
        return BoostFormats.modeForKey(s.boostable.key) == FormattedBooster.Mode.INVERTED ? -delta : delta;
    }

    private static void sortAll() {
        int reordered = 0;
        for (TECH t : TECHS.ALL()) {
            // tech.boosters.all() is a live view — snapshot before mutating.
            List<BoostSpec> sorted = new ArrayList<>();
            for (BoostSpec s : t.boosters.all())
                sorted.add(s);
            if (sorted.size() < 2)
                continue;

            // Stable sort, so equal-ranked entries keep the engine's alphabetical order.
            Collections.sort(sorted, BY_USEFULNESS);

            // Rebuild via the public API: removing each spec and pushing it straight back appends it, so
            // walking the sorted list leaves the list in sorted order. push/remove are no-ops on the
            // boostable itself here (a tech's BoostSpecs has connect == false).
            for (BoostSpec s : sorted) {
                t.boosters.remove(s);
                t.boosters.push(s);
            }
            reordered++;
        }
        System.out.println("[sos-extended-boostables] tech effect lists reordered for " + reordered + " tech(s).");
    }
}
