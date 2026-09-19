package your.mod.targetfilter;

import java.util.ArrayList;
import java.util.List;

import game.boosting.BOOSTABLE_O;
import game.boosting.BSourceInfo;
import game.boosting.BoostSpec;
import game.boosting.Boostable;
import game.boosting.Booster;
import game.faction.FACTIONS;
import init.race.RACES;
import init.race.Race;
import snake2d.util.sets.LIST;

/**
 * Stage 2 of the division-scope fix: makes a filtered tech visible to
 * {@code game.battle.util.Boosts}, the aggregator behind the <b>settlement regiments panel</b>
 * ({@code UIDivCardSett} / {@code UIDivCardBasic} → {@code UIDivStats.get(DIV_SPEC)}), army power
 * estimates and division formation weighting.
 *
 * <h2>Why this is needed at all</h2>
 *
 * {@code Boosts.Entry.get(DIV_SPEC)} never reads {@code Boostable.all}, so the
 * {@link TargetFilteredBooster} the applier installs is invisible to it. Its only two boost
 * sources are
 *
 * <pre>
 *     for (BoostSpec ss : div.race().all(bo))   // race traits   — race-keyed, faction-blind
 *     for (Booster  ss : bo.fGlobal)            // faction techs — faction-keyed, race-blind
 * </pre>
 *
 * and {@code fGlobal} is written only by {@code BoostCompound.exe()}, which reads
 * {@code tech.boosters} — the very list the applier pulls the spec out of. Hence the reported
 * symptom: the boost shows in a subject's properties panel but not in the regiments panel.
 *
 * <h2>How the two halves are paired</h2>
 *
 * Neither loop alone can express "player faction <b>and</b> this race", but they run in that order
 * inside one {@code Boosts.Entry.get} call, so together they can:
 *
 * <ul>
 *   <li>a {@link RaceProbe} — an identity ({@code ×1.0}) Booster reachable from
 *       {@code div.race().all(bo)} — records its own race in {@link #AMBIENT} when the race loop
 *       evaluates it;</li>
 *   <li>a {@link DivFactionBooster} in {@code bo.fGlobal} returns identity unless the target is
 *       {@code FACTIONS.player()}, then sums the filtered techs whose filter covers the recorded
 *       race — stacking MULs additively ({@code 1 + Σ(v-1)}, floored at 0) exactly as
 *       {@code BoostCompound.Boo} does.</li>
 * </ul>
 *
 * The ambient race is always fresh: a probe is injected into <em>every</em> race for every
 * boostable that gets an aggregate booster, so the race loop always runs one immediately before the
 * {@code fGlobal} loop reads it. The engine has exactly two {@code fGlobal} readers — {@code Boosts}
 * and {@code UIDivStats.hoverI} — and the latter calls {@code GAME.battle().boost(st, bo)} for the
 * same boostable first, so both are covered. A null ambient falls back to identity.
 *
 * <h2>Why the probe is invisible to everything else</h2>
 *
 * {@code Race.all(Boostable)} builds its private {@code bmap} lazily <b>once</b> and never rebuilds
 * it. So {@link #install()} pushes the probe into {@code race.boosts} (which is
 * {@code connect = false}, so it never reaches {@code Boostable.all}), calls {@code race.all(bo)} to
 * freeze {@code bmap} with the probe in it, then removes the spec from {@code race.boosts} again.
 * The probe survives only in the map {@code Boosts} reads. Everything that iterates
 * {@code race.boosts.all()} — the division editor's race-trait hover, the world-map population
 * panel, {@code Creator}, {@code RaceBoosts}' connecter and {@code setPrio}, and
 * {@code UIDivStats.hoverI}'s own race loop — sees nothing. No reflection is involved.
 *
 * <p>Ordering is what makes that safe: {@code BOOSTING.finishSetup()} drains {@code waiting} before
 * {@code connecters}, the race files' own specs arrive on {@code waiting} from the GAME constructor
 * (earlier than our applier, queued from {@code initBeforeGameInited}), and {@code RaceBoosts}'
 * connecter — the first thing that would otherwise build {@code bmap} — has not run yet. Each
 * injection is nevertheless <b>verified</b> by re-reading {@code race.all(bo)}; if any race comes
 * back without the probe, no aggregate booster is installed at all and behaviour stays exactly as it
 * was before this class existed.
 *
 * <h2>Why nothing double-applies</h2>
 *
 * {@code Boosts} reads {@code fGlobal} and never {@code Boostable.all}; {@code Boostable.get(Div)}
 * reads {@code all} and never {@code fGlobal}; {@code UIDivStats}' two tooltip branches are mutually
 * exclusive. The aggregate booster is added only to {@code fGlobal}, so {@code Boostable.get} and
 * {@code hoverDetailed} never see it.
 */
final class DivAggregate {

    /**
     * The race of the division currently being evaluated, set by {@link RaceProbe}. A ThreadLocal
     * rather than a plain static: the engine already assumes {@code Boosts.Entry} is not re-entered
     * concurrently (it keeps mutable {@code add}/{@code sub}/{@code mul} fields across the call),
     * but power estimates are driven from world/AI updates, and a stale read here would be a wrong
     * number on screen rather than a wrong persisted value. Cheap insurance.
     */
    private static final ThreadLocal<Race> AMBIENT = new ThreadLocal<>();

    /** One per (Boostable, isMul), rebuilt per game. */
    private static final List<Group> groups = new ArrayList<>();

    private DivAggregate() {}

    private static final class Group {
        final Boostable bo;
        final boolean isMul;
        final List<TargetFilteredBooster> entries = new ArrayList<>();
        Group(Boostable bo, boolean isMul) { this.bo = bo; this.isMul = isMul; }
    }

    /**
     * Drop the previous game's state. {@code new INIT()} runs inside the GAME constructor, so every
     * {@code Boostable} and every {@code Race} is a fresh instance per game and the old groups could
     * only keep a dead object graph alive.
     */
    static void reset() {
        groups.clear();
        AMBIENT.remove();
    }

    /**
     * Note a booster the applier has just installed, so {@link #install()} can give its boostable a
     * division-scope channel. Filters that constrain {@code TARGET_CLASS} are skipped — a division
     * carries no class, so they could only ever return identity here.
     */
    static void record(Boostable bo, TargetFilteredBooster b) {
        if (bo == null || b == null || !b.divDecidable()) return;
        for (Group g : groups) {
            if (g.bo == bo && g.isMul == b.isMul) { g.entries.add(b); return; }
        }
        Group g = new Group(bo, b.isMul);
        g.entries.add(b);
        groups.add(g);
    }

    /**
     * Inject the probes, verify them, and wire the aggregate boosters into {@code fGlobal}. Called
     * at the end of {@code TargetFilterApplier.applyAll()}, i.e. from the {@code BOOSTING.waiting}
     * action — before any connecter has had a chance to build a race's {@code bmap}.
     */
    static void install() {
        if (groups.isEmpty()) {
            TargetFilterRegistry.announce("div-scope: no div-decidable boosts; no aggregate boosters installed");
            return;
        }

        List<Boostable> bos = new ArrayList<>();
        for (Group g : groups) {
            if (!bos.contains(g.bo)) bos.add(g.bo);
        }

        int racesProbed = 0;
        String failure = null;

        for (Race r : RACES.all()) {
            List<BoostSpec> pushed = new ArrayList<>(bos.size());
            for (Boostable bo : bos) {
                pushed.add(r.boosts.push(new RaceProbe(r.boosts.info, r), bo));
            }
            // The first all() call materialises bmap from boosts.all(), probes included.
            for (int i = 0; i < bos.size() && failure == null; i++) {
                if (!reachable(r.all(bos.get(i)), pushed.get(i))) {
                    failure = "race=" + r.key + " boostable=" + bos.get(i).key
                            + " (bmap was already built before the applier ran)";
                }
            }
            // Take the specs back out of race.boosts; bmap keeps its own reference.
            for (BoostSpec s : pushed) r.boosts.remove(s);
            if (failure != null) break;
            racesProbed++;
        }

        if (failure != null) {
            groups.clear();
            TargetFilterRegistry.announce("div-scope: DISABLED — probe injection failed for " + failure
                    + "; division-scope readouts keep their pre-fix values");
            return;
        }

        for (Group g : groups) {
            g.bo.fGlobal.add(new DivFactionBooster(g.entries.get(0).info, g.isMul, g.entries));
        }

        TargetFilterRegistry.announce("div-scope: installed " + groups.size()
                + " aggregate booster(s) over " + bos.size() + " boostable(s); probes "
                + racesProbed + " race(s)");
    }

    /** Identity search — {@code BoostSpec} inherits Object equality, which is what we want. */
    private static boolean reachable(LIST<BoostSpec> specs, BoostSpec wanted) {
        if (specs == null) return false;
        for (BoostSpec s : specs) {
            if (s == wanted) return true;
        }
        return false;
    }

    /**
     * Identity booster whose only job is to tell {@link DivFactionBooster} which race's division is
     * being evaluated. {@code Boosts.Entry.get} calls {@code ss.booster.getValue(1.0)} on every spec
     * in {@code div.race().all(bo)}, which is where the record happens.
     */
    private static final class RaceProbe extends Booster {

        private final Race race;

        RaceProbe(BSourceInfo info, Race race) {
            super(info, true);
            this.race = race;
        }

        @Override public double from() { return 1.0; }

        @Override public double to() { return 1.0; }

        @Override
        public double getValue(double input) {
            AMBIENT.set(race);
            return 1.0;
        }

        @Override protected double pget(BOOSTABLE_O o) { return 1.0; }
    }

    /**
     * The {@code fGlobal} half. Follows the shape of {@code BoostCompound.Boo} and
     * {@code RaceBoosts.BV}: {@code getValue} is the identity and {@code pget} returns the finished
     * value.
     */
    private static final class DivFactionBooster extends Booster {

        private final List<TargetFilteredBooster> entries;
        private final double pFrom;
        private final double pTo;

        DivFactionBooster(BSourceInfo info, boolean isMul, List<TargetFilteredBooster> entries) {
            super(info, isMul);
            this.entries = entries;
            double lo = isMul ? 1.0 : 0.0;
            double hi = lo;
            for (TargetFilteredBooster b : entries) {
                lo = Math.min(lo, b.min());
                hi = Math.max(hi, b.max());
            }
            this.pFrom = lo;
            this.pTo = hi;
        }

        @Override public double from() { return pFrom; }

        @Override public double to() { return pTo; }

        @Override public double getValue(double input) { return input; }

        @Override
        protected double pget(BOOSTABLE_O o) {
            double identity = isMul ? 1.0 : 0.0;
            if (o != FACTIONS.player()) return identity;
            Race r = AMBIENT.get();
            if (r == null) return identity;

            if (isMul) {
                // Same additive stacking BoostCompound.Boo uses for multiple MUL specs.
                double acc = 1.0;
                for (int i = 0; i < entries.size(); i++) {
                    acc += entries.get(i).valueForRace(r) - 1.0;
                }
                return Math.max(0, acc);
            }
            double acc = 0;
            for (int i = 0; i < entries.size(); i++) {
                acc += entries.get(i).valueForRace(r);
            }
            return acc;
        }
    }
}
