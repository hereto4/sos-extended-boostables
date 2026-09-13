package your.mod.stealth;

import init.type.HTYPES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.rnd.RND;
import your.mod.los.TileLOS;

import java.util.IdentityHashMap;
import java.util.Map;

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
 * <p><b>Reinforcing, not replacing, the internal flip.</b> {@code reportCriminal} still re-rolls its
 * own flat 50 % internally every time it's called and there is no public "guaranteed report" call.
 * When our contest decides a crime SHOULD be caught, we call {@code reportCriminal} {@link #REPORT_ATTEMPTS}
 * times in the same tick — independent 50 % misses compound multiplicatively
 * (1 - 0.5^{@value #REPORT_ATTEMPTS} ≈ {@code 99.2%}), which is as close to "the contest decided it" as
 * the public API allows without reflection.
 *
 * <p><b>Losing the contest just means "not reinforced this tick".</b> We deliberately do NOT try to
 * suppress the engine's own independent notify()/report() calls for loud crimes (Murder/Vandalism/
 * Flasher) — there is no way to intercept those without reflection, and this feature chose not to use
 * any.
 *
 * <p><b>That was a self-imposed choice, not a project rule (corrected 2026-09-13).</b> The original
 * wording here — "per project policy this mod does not use reflection" — was wrong: no such policy has
 * ever existed. Extended Boostables already reflects in {@code your.mod.targetfilter}
 * ({@code TargetFilters.queueWaitingAction} reaches the package-private {@code BOOSTING.waiting};
 * {@code ParseWarningSuppressor} reaches {@code Json.untest}), and the upstream template docs recommend
 * reflection outright. The project's actual rule is narrower: <b>never reflectively write or pin engine
 * state the engine recomputes</b> (the {@code STAT_WORK_RETIREMENT} denominator-pinning bug), while
 * reflecting to read — or to reach a member whose visibility moved between game versions — is fine when
 * it runs once at init with a non-reflective fallback. See {@code .claude/memory/feedback_reflection_policy.md}.
 *
 * <p>So the {@link #REPORT_ATTEMPTS} hack below is not the only option available. A single reflective
 * push into the sealed {@code CrimeReporter} would be a read-mostly, one-shot, fallback-able use that
 * sits on the permitted side of that line — and it would avoid this workaround's real cost: each
 * {@code reportCriminal} call that passes the engine's internal coin flip pushes a duplicate into a
 * guard house's <b>5-entry</b> mailbox, which can fill it and de-register that guard house from the
 * finder, starving unrelated crimes nearby. Revisit if this is ever tuned.
 *
 * <p>A high-Stealth criminal therefore still benefits (their contest loss simply
 * fails to add extra report attempts on top of the vanilla 50 %), while a high-Stealth criminal near NO
 * guard at all never gets reinforced regardless of the roll (see {@link #findBestGuard}).
 */
public final class CrimeStealthCheck {

    /** Seconds between full settlement sweeps. */
    private static final double PERIOD = 2.0;
    /** Matches the design brief's "50 tiles of unbroken LOS". */
    private static final int SEARCH_RADIUS = 50;
    /** Extra reportCriminal() calls fired when the guard wins the contest (see class javadoc). */
    private static final int REPORT_ATTEMPTS = 7;
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

    private static void sweep() {
        for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
            if (!(e instanceof Humanoid))
                continue;
            Humanoid criminal = (Humanoid) e;
            if (criminal.isRemoved())
                continue;
            if (!AI.modules().isCriminal(criminal))
                continue;
            if (cooldowns.containsKey(criminal))
                continue;

            resolve(criminal);
        }
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
            for (int i = 0; i < REPORT_ATTEMPTS; i++)
                SETT.ROOMS().GUARD.reporter.reportCriminal(criminal);
            cooldowns.put(criminal, REPORT_COOLDOWN);
            System.out.println("[sos-extended-boostables] stealth/alertness: crime reinforced-reported"
                    + " (guard=" + guardRoll + " vs perp=" + perpRoll
                    + ", stealth=" + stealthValue + " alertness=" + alertnessValue + ").");
        }
    }

    /** Best (highest-alertness) HTYPES.GUARD() humanoid within {@link #SEARCH_RADIUS} tiles of unbroken LOS. */
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
