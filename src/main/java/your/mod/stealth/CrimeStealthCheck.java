package your.mod.stealth;

import java.lang.reflect.Method;
import java.util.IdentityHashMap;
import java.util.Map;

import init.type.HTYPES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.main.SETT;
import settlement.room.law.guard.CrimeReporter;
import settlement.room.law.guard.ROOM_GUARD;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.rnd.RND;
import your.mod.los.TileLOS;

/**
 * The per-tick "Search hook" from {@code HANDOFF_SEARCH_STEALTH.md} §12 — a periodic sweep that
 * replaces the flat 50 % coin flip in {@code CrimeReporter.reportCriminal} with a proper contest
 * between the criminal's STEALTH and the best nearby guard's ALERTNESS.
 *
 * <h2>Why a sweep instead of intercepting the call</h2>
 * {@code CrimeReporter}/{@code AIModule_Crime} are sealed engine internals (final classes, package-
 * private constructors — see HANDOFF §10): nothing can hook the moment a crime is committed. The only
 * public seams are {@code reportCriminal(Humanoid)} (push a report) and {@code pollCriminal(...)}
 * (destructively pop one) — see §11. So this class independently re-derives "is anyone currently
 * gettable away with a crime?" via {@code AI.modules().isCriminal(Humanoid)} on a timer, and reinforces
 * the report itself rather than trying to suppress the engine's own internal roll.
 *
 * <p><b>Direct push, not a compounded coin-flip burst (2026-09-13 rework, review §2d Option B).</b>
 * An earlier version of this class called the public {@code reportCriminal(Humanoid)} up to 7 times to
 * compound past its internal flat 50 % coin flip (1 - 0.5^7 ≈ 99.2%). That was flagged in review as
 * actively harmful: each attempt that happens to pass the internal flip pushes a <em>duplicate</em>
 * crime id into the target guard house's 5-entry mailbox (no de-duplication), and a filled mailbox
 * de-registers that guard house from the finder for <b>all</b> crime reporting until it is rebuilt —
 * roughly a 1-in-4 chance per reinforcement of fully flooding an empty house. See
 * {@code HANDOFF_STEALTH_REVIEW.md} §2c/§2d.
 *
 * <p>This is fixed by {@link DirectReport}, which reflectively invokes {@code CrimeReporter}'s private
 * {@code report(int, int, int, int, int)} — the same method {@code reportCriminal} delegates to, minus
 * its internal coin flip — exactly once per won contest. This pushes <em>exactly one</em> entry, the
 * same as any single vanilla crime report, so our mod no longer amplifies the mailbox-flooding failure
 * mode at all (H1 is fully addressed, not just mitigated).
 *
 * <p><b>Why this reflection use is "fine", per the project's actual rule
 * ({@code CLAUDE.md} → "Reflection: there is no blanket ban — one narrow rule"):</b> it <i>reads</i> the
 * method handle once (cached in a static initializer, not re-resolved per call), it appends a report
 * rather than pinning any recomputed engine state, and it has a verbatim non-reflective fallback
 * ({@code reportCriminal(Humanoid)} itself) if the method can't be found/invoked on some future game
 * version. This is the same shape as {@code your.mod.targetfilter.TargetFilters.queueWaitingAction},
 * in production since 2026-07-27. There never was a project-wide "no reflection" policy; a stale
 * comment here previously claimed otherwise and has been removed.
 *
 * <p><b>Losing the contest just means "not reinforced this tick".</b> We deliberately do NOT try to
 * suppress the engine's own independent notify()/report() calls for loud crimes (Murder/Vandalism/
 * Flasher) — those still fire at their own vanilla rate regardless of this contest's outcome.
 *
 * <p><b>H2 — the sweep no longer treats raiders or decree-marked subjects as "criminals".</b>
 * {@code AI.modules().isCriminal(Humanoid)} is intentionally broader than "is committing a crime": it
 * also returns {@code true} for every hostile unit (raiders during a siege) and every subject
 * permanently marked by a PROSECUTION decree (a player choice, not an in-progress crime) — see
 * {@code AIModule_Crime.isCriminal}. Feeding either of those into this sweep means a single active siege
 * or decree turns hundreds of subjects into contest participants every sweep, each costing a ~101×101
 * proximity scan plus per-candidate LOS traces. {@link #sweep()} now excludes both cases explicitly
 * (via the same public {@code Induvidual.hostile()} / {@code STATS.MULTIPLIERS().PROSECUTION.markIs}
 * checks the engine itself uses), leaving only genuine "committed an actual crime, not yet caught"
 * subjects — restoring the intended scope and letting Stealth actually mean something (review §3, H2).
 */
public final class CrimeStealthCheck {

    /** Seconds between full settlement sweeps. */
    private static final double PERIOD = 2.0;
    /** Matches the design brief's "50 tiles of unbroken LOS". */
    private static final int SEARCH_RADIUS = 50;
    /** Cooldown after a successful reinforcement, so one criminal doesn't spam the mailbox every tick. */
    private static final double REPORT_COOLDOWN = 15.0;

    private static double timer = 0;
    private static final Map<Humanoid, Double> cooldowns = new IdentityHashMap<>();

    private CrimeStealthCheck() {}

    /** Call once per frame from the script instance's update loop. No-op unless the feature is enabled. */
    public static void update(double ds) {
        if (!StealthAlertnessBoostables.isInstalled())
            return;
        if (!SETT.exists())
            return;

        timer -= ds;
        if (timer > 0)
            return;
        timer = PERIOD;

        tickCooldowns(PERIOD);
        sweep();
    }

    /** Call on game load: cooldowns are transient working state, same as the growth maps. */
    public static void clearOnLoad() {
        timer = 0;
        cooldowns.clear();
    }

    private static void tickCooldowns(double elapsed) {
        if (cooldowns.isEmpty())
            return;
        cooldowns.replaceAll((h, v) -> v - elapsed);
        cooldowns.values().removeIf(v -> v <= 0);
    }

    private static final String LOG = "[sos-extended-boostables] stealth/alertness: ";

    private static void sweep() {
        for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
            if (!(e instanceof Humanoid))
                continue;
            Humanoid criminal = (Humanoid) e;
            if (criminal.isRemoved())
                continue;
            if (!isGenuineCrimeSubject(criminal))
                continue;
            if (cooldowns.containsKey(criminal))
                continue;

            resolve(criminal);
        }
    }

    /**
     * H2 fix: {@code AI.modules().isCriminal(Humanoid)} is deliberately broader than "committing a
     * crime" — see class javadoc. This re-narrows it to genuine in-progress crimes only, using the
     * same public checks the engine itself uses to build that broader flag, so a siege or a
     * PROSECUTION decree no longer drags the whole settlement through this sweep.
     */
    private static boolean isGenuineCrimeSubject(Humanoid a) {
        if (!AI.modules().isCriminal(a))
            return false;
        if (a.indu().hostile())
            return false; // every hostile unit during a siege -- not "a crime", a war
        if (STATS.MULTIPLIERS().PROSECUTION.markIs(a))
            return false; // a permanent player decree mark, not an in-progress crime
        return true;
    }

    private static void resolve(Humanoid criminal) {
        Induvidual perpIndu = criminal.indu();
        double stealthValue = StealthAlertnessBoostables.effectiveStealth(perpIndu);

        Humanoid bestGuard = findBestGuard(criminal);
        if (bestGuard == null)
            return; // nobody around to contest this tick -- matches vanilla's "no guard house nearby" gap

        Induvidual guardIndu = bestGuard.indu();
        double alertnessValue = StealthAlertnessBoostables.effectiveAlertness(guardIndu);

        double perpRoll = RND.rFloat(Math.max(0.0001, stealthValue));
        double guardRoll = RND.rFloat(Math.max(0.0001, alertnessValue));

        // Item 3: using the skill -- win or lose -- grows it a little.
        StealthAlertnessBoostables.gainStealth(perpIndu);
        StealthAlertnessBoostables.gainAlertness(guardIndu);

        if (guardRoll > perpRoll) {
            DirectReport.push(criminal);
            cooldowns.put(criminal, REPORT_COOLDOWN);
            System.out.println(LOG + "crime reinforced-reported via " + DirectReport.modeDescription()
                    + " (guard=" + guardRoll + " vs perp=" + perpRoll
                    + ", stealth=" + stealthValue + " alertness=" + alertnessValue + ").");
        } else {
            // Review §6 item 2: log losses too, so a clean success-only log isn't mistaken for
            // "nothing else happened". Not on cooldown, so this can be noisy; keep it terse.
            System.out.println(LOG + "crime evaded"
                    + " (guard=" + guardRoll + " vs perp=" + perpRoll
                    + ", stealth=" + stealthValue + " alertness=" + alertnessValue + ").");
        }
    }

    /**
     * Option B from review §2d: a single, one-shot, fallback-able reflective call into
     * {@code CrimeReporter}'s private {@code report(int, int, int, int, int)} — the method
     * {@code reportCriminal(Humanoid)} itself delegates to, minus its internal 50 % coin flip.
     * Pushes exactly one entry per won contest, eliminating the mailbox-flooding amplification
     * described in review §2c (H1). See class javadoc for why this reflection use is in-policy.
     */
    private static final class DirectReport {

        /** {@code CrimeReporter}'s private {@code static int tCrime = 0} report-type stride index. */
        private static final int TCRIME = 0;

        private static final Method REPORT;

        static {
            Method m;
            try {
                m = CrimeReporter.class.getDeclaredMethod(
                        "report", int.class, int.class, int.class, int.class, int.class);
                m.setAccessible(true);
            } catch (Throwable t) {
                System.out.println(LOG + "reflective CrimeReporter.report() unavailable (" + t
                        + "); will fall back to public reportCriminal() (with its internal 50% flip).");
                m = null;
            }
            REPORT = m;
        }

        private DirectReport() {}

        static void push(Humanoid criminal) {
            if (REPORT != null) {
                try {
                    REPORT.invoke(SETT.ROOMS().GUARD.reporter, TCRIME,
                            criminal.tc().x(), criminal.tc().y(), ROOM_GUARD.maxRadius, criminal.id());
                    return;
                } catch (Throwable t) {
                    System.out.println(LOG + "reflective report() invoke failed (" + t
                            + "); falling back to reportCriminal() for this call.");
                }
            }
            SETT.ROOMS().GUARD.reporter.reportCriminal(criminal);
        }

        static String modeDescription() {
            return REPORT != null ? "direct-push" : "fallback-coinflip";
        }
    }

    /** Best (highest-alertness) HTYPES.GUARD() humanoid within {@link #SEARCH_RADIUS} tiles of unbroken LOS.
     *
     *  <p><b>L6 (review §3):</b> {@code getInProximity} returns a shared mutable {@code temp} list owned
     *  by {@code ENTETIES}. We iterate it while calling {@code Boostable.get()}, which is safe today
     *  because this feature's two boosters (Growth/Innate) never re-enter a proximity query mid-iteration
     *  — but a future dependent mod that does would clobber this loop. Not copying it out here to avoid
     *  a per-sweep-per-criminal allocation; flagged so nobody "fixes" the underlying sharing without
     *  checking this call site first. */
    private static Humanoid findBestGuard(Humanoid criminal) {
        int cx = criminal.tc().x();
        int cy = criminal.tc().y();

        Humanoid best = null;
        double bestAlertness = -1;

        for (ENTITY e : SETT.ENTITIES().getInProximity(criminal, SEARCH_RADIUS)) {
            if (!(e instanceof Humanoid))
                continue;
            Humanoid g = (Humanoid) e;
            if (g == criminal || g.isRemoved())
                continue;
            if (g.indu().hType() != HTYPES.GUARD())
                continue;

            int gx = g.tc().x();
            int gy = g.tc().y();
            if (COORDINATE.tileDistance(cx, cy, gx, gy) > SEARCH_RADIUS)
                continue;
            if (!TileLOS.hasLineOfSight(cx, cy, gx, gy, SEARCH_RADIUS))
                continue;

            double alert = StealthAlertnessBoostables.effectiveAlertness(g.indu());
            if (alert > bestAlertness) {
                bestAlertness = alert;
                best = g;
            }
        }
        return best;
    }
}
