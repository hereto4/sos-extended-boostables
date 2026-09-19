package your.mod.targetfilter;

import game.battle.div.Div;
import init.race.Race;
import init.type.CAUSE_ARRIVES;
import init.type.HCLASS;
import init.type.HCLASS_RACE;
import settlement.stats.Induvidual;
import settlement.stats.STATS;

/**
 * A resolved predicate over a subject {@link Induvidual}, built from a tech's
 * {@code TARGET_RACE} and/or {@code TARGET_CLASS} declaration. A subject matches
 * only when it satisfies <em>every</em> constraint that was set:
 * <ul>
 *   <li><b>race</b>  — {@code ind.race() == race}  (when a race was named)</li>
 *   <li><b>class</b> — the class matcher passes     (when a class token was named)</li>
 * </ul>
 * A tech that names both must satisfy both (logical AND).
 *
 * <p><b>Class matching:</b>
 * <ul>
 *   <li>{@code CITIZEN} / {@code NOBLE} / {@code SLAVE} / {@code OTHER} —
 *       {@code ind.clas() == } the matching {@link HCLASS}.</li>
 *   <li>{@code EXSLAVE} — <em>synthetic</em>: there is no EXSLAVE HCLASS in the
 *       engine. A freed slave becomes a CITIZEN-class subject tagged with the
 *       arrival cause {@code CAUSE_ARRIVES.EMANCIPATED} ("Subjects that are freed
 *       slaves"; set in {@code settlement.stats.muls.StatsMultipliers} when a
 *       slave is freed). So EXSLAVE matches any subject whose arrival cause is
 *       EMANCIPATED, regardless of its current class. (This is distinct from
 *       {@code PAROLE}, which the engine documents as pardoned <em>prisoners</em>,
 *       not freed slaves.)</li>
 * </ul>
 */
final class SubjectFilter {

    /** null = no race constraint. */
    private final Race race;
    /** null = no HCLASS constraint (also null when {@link #exslave}). */
    private final HCLASS hclass;
    /** true = match freed slaves (arrival cause == EMANCIPATED) instead of an HCLASS. */
    private final boolean exslave;

    SubjectFilter(Race race, HCLASS hclass, boolean exslave) {
        this.race = race;
        this.hclass = hclass;
        this.exslave = exslave;
    }

    boolean matches(Induvidual ind) {
        if (ind == null) return false;
        if (race != null && ind.race() != race) return false;
        if (exslave) {
            return STATS.POP().COUNT.arrive.get(ind) == CAUSE_ARRIVES.EMANCIPATED();
        }
        if (hclass != null && ind.clas() != hclass) return false;
        return true;
    }

    /**
     * Match against a <b>population aggregate</b> — the engine's race × class bucket
     * ({@link HCLASS_RACE}), used at read-points that ask what a boostable is worth for a
     * <em>slice</em> of the population rather than for one subject. Two live examples:
     * {@code Immigration.getRate} reads {@code IMMIGRATION.get(HCLASS_RACE.clP(race, CITIZEN))}
     * and {@code StatsReproduction} projects births with {@code clP(r, cl)}.
     *
     * <p>A bucket answers a coarser question than an {@link Induvidual}, so a constraint can come
     * back <em>undecidable</em> rather than true/false. A bucket with {@code race == null} means
     * "all races" (that is what {@code HCLASS_RACE.clP()} — the citywide readout target — is), and
     * a bucket records no arrival cause, so {@code EXSLAVE} can never be tested against one.
     *
     * <p>This returns true only when <b>every</b> constraint is definitively satisfied. Both
     * "definitely a different race/class" and "too coarse to tell" collapse to false, which leaves
     * the booster at identity — the same value it returned before this branch existed. So the
     * citywide readouts are unchanged and only the genuinely race-named buckets start filtering.
     */
    boolean matches(HCLASS_RACE pop) {
        if (pop == null) return false;
        // pop.race == null is the "all races" bucket, so != race covers both "wrong race" and
        // "undecidable" in one test. Same for pop.cl below.
        if (race != null && pop.race != race) return false;
        if (exslave) return false;
        if (hclass != null && pop.cl != hclass) return false;
        return true;
    }

    /**
     * Match against a <b>division</b> — the target the engine hands to every division-scope
     * read-point ({@code BATTLE().FORMATION.get(a.division())} in live combat,
     * {@code MORALE.get(div)} in {@code DivFactors}, {@code PHYSICS().SPEED.get(div)} for march
     * speed, and the in-battle regiment card).
     *
     * <p>Same <b>definite-match</b> semantics as {@link #matches(HCLASS_RACE)}: a division names
     * exactly one race ({@code Div.race()}) so a race constraint is decidable, but it carries no
     * class and no arrival cause, so a {@code TARGET_CLASS} constraint — {@code EXSLAVE} included —
     * is undecidable and collapses to false. False leaves the booster at identity, which is exactly
     * the value it returned before this branch existed.
     */
    boolean matches(Div div) {
        return div != null && matchesRace(div.race());
    }

    /**
     * The race half of {@link #matches(Div)}, exposed for the division-scope aggregate booster in
     * {@link DivAggregate}, which is handed a bare {@link Race} rather than a {@code Div}.
     */
    boolean matchesRace(Race r) {
        if (exslave) return false;    // divisions carry no arrival cause
        if (hclass != null) return false; // divisions carry no class
        if (race == null) return true;    // unconstrained; the registry should never produce this
        return r == race;
    }

    /**
     * True when this filter can be decided for a division at all, i.e. when {@link #matchesRace}
     * can return true for some race. A class-constrained filter never can, so {@link DivAggregate}
     * skips it rather than installing machinery that could only ever return identity.
     */
    boolean divDecidable() {
        return !exslave && hclass == null;
    }
}
