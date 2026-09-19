package your.mod.targetfilter;

import game.battle.div.Div;
import game.boosting.BOOSTABLE_O;
import game.boosting.BSourceInfo;
import game.boosting.Booster;
import game.faction.FACTIONS;
import init.race.Race;
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
 *   <li>{@link Div} — a division, the target of every division-scope read-point
 *       ({@code FORMATION.get(a.division())} in live combat, {@code MORALE.get(div)},
 *       {@code SPEED.get(div)}, the in-battle regiment card). A division names one race
 *       but no class; see {@link SubjectFilter#matches(Div)}.</li>
 * </ul>
 *
 * <p><b>Faction gate.</b> Every branch first requires the target to belong to
 * {@code FACTIONS.player()}. Vanilla gets this for free — a tech's aggregate Boo is a
 * {@code BValue.BValueFaction}, which routes an NPC to {@code f.bonus.getD(bo)} rather than to
 * the player's tech. Without the gate a filtered tech leaked to hostiles, because
 * {@code Induvidual.faction()} returns {@code FACTIONS.otherFaction()} — a real {@code FactionNPC} —
 * for every enemy subject, and the race filter alone happily matched an invader of the target race.
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
 * <p>The scaler for matching subjects is the player's current tech progress, penalty
 * included — see {@link TechScaler}. It is read on every {@code pget} call, so level
 * changes apply the next time a stat is queried.
 *
 * <p>This is the generalization of the standalone target-race-tech mod's
 * {@code RaceFilteredBooster}; the only change is swapping the fixed race check
 * for an arbitrary {@link SubjectFilter}.
 */
final class TargetFilteredBooster extends Booster {

    private final double pFrom;
    private final double pTo;
    private final SubjectFilter filter;
    private final TechScaler scaler;

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
        this.scaler = new TechScaler(tech);
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
            Induvidual ind = (Induvidual) o;
            if (ind.faction() != FACTIONS.player()) return 0.0;
            if (!filter.matches(ind)) return 0.0;
        } else if (o instanceof Div) {
            Div div = (Div) o;
            if (div.faction() != FACTIONS.player()) return 0.0;
            if (!filter.matches(div)) return 0.0;
        } else if (o instanceof HCLASS_RACE) {
            // Population buckets are settlement-side and therefore the player's by construction;
            // there is no faction on one to gate against.
            if (!filter.matches((HCLASS_RACE) o)) return 0.0;
        } else {
            return 0.0;
        }
        return scaler.progress();
    }

    /**
     * The value this booster contributes to a whole division of {@code r}, or identity when the
     * filter does not cover that race. Used by {@link DivAggregate}, which reaches the engine's
     * {@code Boostable.fGlobal} channel — a channel that is handed a {@code Faction}, never a
     * {@code Div}, so it resolves the race separately and asks here for the value.
     */
    double valueForRace(Race r) {
        if (!filter.matchesRace(r)) return isMul ? 1.0 : 0.0;
        return getValue(scaler.progress());
    }

    /** @see SubjectFilter#divDecidable() */
    boolean divDecidable() {
        return filter.divDecidable();
    }
}
