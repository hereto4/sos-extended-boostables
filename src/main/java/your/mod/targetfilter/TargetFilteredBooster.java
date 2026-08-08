package your.mod.targetfilter;

import game.boosting.BOOSTABLE_O;
import game.boosting.BSourceInfo;
import game.boosting.Booster;
import game.faction.FACTIONS;
import init.tech.TECH;
import init.type.HCLASS_RACE;
import settlement.stats.Induvidual;
import snake2d.util.misc.CLAMP;

/**
 * A Booster that mirrors the math of a vanilla tech BoosterValue but only applies
 * its scaled effect to targets that pass a {@link SubjectFilter} (race and/or class).
 * Two kinds of target can be tested:
 * <ul>
 *   <li>{@link Induvidual} — one subject; the filter decides exactly.</li>
 *   <li>{@link HCLASS_RACE} — a race × class population bucket, used by the engine's
 *       aggregate read-points (per-race immigration pull, the per-race reproduction
 *       projection, ...). Filterable only when the bucket names the race/class the
 *       filter constrains; see {@link SubjectFilter#matches(HCLASS_RACE)}.</li>
 * </ul>
 * For anything else — a target that fails the filter, or a {@link BOOSTABLE_O} with no
 * subject in it at all (regions, divisions, factions) — it returns the identity scaler
 * so {@code getValue(scaler) == from()} (i.e. 1.0 for MUL, 0.0 for ADD: no effect).
 *
 * <p><b>Identity is indistinguishable from "not applied".</b> A boostable whose only
 * read-point is faction- or region-scoped can therefore never be reached through this
 * booster — it would silently contribute nothing at every tech level. Those boostables
 * are not wrapped at all: {@link TargetFilterApplier} leaves their BoostSpec on the
 * vanilla tech path instead. See {@code TargetFilterApplier#isUnfilterable}.
 *
 * <p>The scaler for matching subjects is the player's current tech progress
 * ({@code level / levelMax}). It is read on every {@code pget} call, so level
 * changes apply the next time a stat is queried — no cache to invalidate.
 *
 * <p>This is the generalization of the standalone target-race-tech mod's
 * {@code RaceFilteredBooster}; the only change is swapping the fixed race check
 * for an arbitrary {@link SubjectFilter}.
 */
final class TargetFilteredBooster extends Booster {

    private final double pFrom;
    private final double pTo;
    private final SubjectFilter filter;
    private final TECH tech;
    private final int levelMaxCached;

    TargetFilteredBooster(BSourceInfo info, boolean isMul,
                          double origFrom, double origTo,
                          SubjectFilter filter, TECH tech) {
        super(info, isMul);
        // Vanilla scaling (mirrors PTech$5.bos(TECH)): every tech level applies
        // the full per-level delta. For MUL with origTo=0.9 levelMax=3, the
        // effect at level N is 1 + N*(origTo-1). We bake that by stretching the
        // booster's `to` over the full level range so getValue(N/levelMax)
        // produces the same result the vanilla aggregator would.
        int lm = Math.max(1, tech.levelMax);
        this.pFrom = origFrom;
        this.pTo = isMul ? (origTo - 1.0) * lm + 1.0 : origTo * lm;
        this.filter = filter;
        this.tech = tech;
        this.levelMaxCached = lm;
    }

    @Override
    public double from() { return pFrom; }

    @Override
    public double to() { return pTo; }

    @Override
    public double getValue(double scaler) {
        double s = CLAMP.d(scaler, 0, 1);
        return pFrom + s * (pTo - pFrom);
    }

    @Override
    protected double pget(BOOSTABLE_O o) {
        if (o instanceof Induvidual) {
            if (!filter.matches((Induvidual) o)) return 0.0;
        } else if (o instanceof HCLASS_RACE) {
            if (!filter.matches((HCLASS_RACE) o)) return 0.0;
        } else {
            return 0.0;
        }
        int level = FACTIONS.player().tech.level(tech);
        if (level <= 0) return 0.0;
        return (double) level / levelMaxCached;
    }
}
