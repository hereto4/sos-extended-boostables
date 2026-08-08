package your.mod.targetfilter;

import java.lang.reflect.Field;

import game.boosting.BOOSTING;
import game.boosting.Boostable;
import snake2d.util.misc.ACTION;

/**
 * Public entry point for the {@code TARGET_RACE} / {@code TARGET_CLASS} tech-filter
 * feature. This machinery was moved here from the standalone <em>target-race-tech</em>
 * mod and generalized to also support {@code TARGET_CLASS} (see {@link SubjectFilter}
 * for the matching rules; {@code EXSLAVE} means a freed slave — arrival cause
 * {@code EMANCIPATED}).
 *
 * <p>A tech declaring {@code TARGET_RACE: <race>} and/or {@code TARGET_CLASS: <class>}
 * has its {@code BOOST} effects restricted to matching subjects only; everyone else
 * (and every non-subject boost target) sees no effect.
 *
 * <p>Wiring from the host {@code SCRIPT}:
 * <ul>
 *   <li>{@link #scan()} from {@code initBeforeGameCreated()}</li>
 *   <li>{@link #install()} from {@code initBeforeGameInited()}</li>
 * </ul>
 */
public final class TargetFilters {

    private TargetFilters() {}

    /**
     * initBeforeGameCreated: pre-scan tech files, record flags, pre-suppress parse warnings, and
     * drop the previous game's {@link #markUnfilterable(Boostable)} registrations (boostables are
     * rebuilt per game — {@code new INIT()} lives in the GAME constructor).
     */
    public static void scan() {
        TargetFilterApplier.resetUnfilterable();
        TargetFilterRegistry.scanTechFiles();
    }

    /**
     * Declare that {@code bo} has no subject-scoped read-point, so a race/class filter can never
     * be evaluated against it. A flagged tech granting this key keeps the filter on all its
     * <em>other</em> boosts and applies this one unfiltered, through the vanilla tech path.
     *
     * <p>Without this, such a boost silently contributes nothing: the filtered booster is only
     * ever asked about a target it cannot test, so it returns identity at every tech level and the
     * effect tooltip drops the line entirely. The engine's own two cases ({@code CIVIC_OPINION},
     * {@code CIVIC_TRUST}) are built in; this is for keys a mod registers itself.
     *
     * <p>Call before {@code BOOSTING.finishSetup()} drains the waiting queue — anywhere in
     * {@code initBeforeGameInited()} works, including after {@link #install()}. Null-safe, so it
     * can be handed the result of a registration that may have failed.
     *
     * @param bo the boostable, read only at faction/region/division scope; null is ignored
     */
    public static void markUnfilterable(Boostable bo) {
        TargetFilterApplier.markUnfilterable(bo);
    }

    /**
     * initBeforeGameInited: scrub any leaked parse warning, then queue the applier
     * on {@code BOOSTING.waiting} (fires after BoostSpecs resolve but before
     * BoostCompound aggregates) and register a connecter that re-adds the original
     * BoostSpecs to {@code tech.boosters} for the tech-tree UI.
     *
     * <p>Ordering note: PTech's BoostCompound connecter is registered during the
     * GAME ctor, which runs before the host script's {@code initBeforeGameInited}.
     * So the re-add connecter we register here is queued <em>after</em> it and
     * runs once aggregation is done — exactly as intended.
     */
    public static void install() {
        // Safety net for the parse-warning suppression set up in scan(): by now
        // new TECHS() has parsed every tech file, so strip any leaked
        // "unknown key: TARGET_*" line before any UnhandledDump is written.
        ParseWarningSuppressor.scrubFromErrBuffer();

        queueWaitingAction(new ACTION() {
            @Override public void exe() { TargetFilterApplier.applyAll(); }
        });

        BOOSTING.connecter(new ACTION() {
            @Override public void exe() { TargetFilterApplier.reAddOriginalsForUI(); }
        });
    }

    /**
     * Add an action to {@code BOOSTING.waiting} reflectively. In v70 the field was
     * {@code public static}; v71 made it package-private. The waiting actions drain
     * at the very start of {@code BOOSTING.finishSetup()}, before any connecter —
     * the window where {@code tech.boosters} is populated but BoostCompound hasn't
     * aggregated yet. If reflection fails (further engine drift) we fall back to
     * running the action immediately; {@code applyAll()} already guards on an empty
     * flag set / empty tech.boosters.
     */
    private static void queueWaitingAction(ACTION a) {
        try {
            Field f = BOOSTING.class.getDeclaredField("waiting");
            f.setAccessible(true);
            Object waiting = f.get(null);
            java.lang.reflect.Method add = waiting.getClass().getMethod("add", Object.class);
            add.invoke(waiting, a);
            TargetFilterRegistry.announce("queued applier on BOOSTING.waiting (reflective; v71 made it package-private)");
        } catch (Throwable t) {
            TargetFilterRegistry.announce("could not access BOOSTING.waiting reflectively (" + t + "); running applier eagerly");
            a.exe();
        }
    }
}
