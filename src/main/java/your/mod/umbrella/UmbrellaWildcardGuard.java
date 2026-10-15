package your.mod.umbrella;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import game.boosting.BOOSTING;
import game.boosting.BoostSpec;
import game.boosting.BoostSpecs;
import game.boosting.Boostable;
import game.boosting.Booster;
import snake2d.util.misc.ACTION;

/**
 * Keeps content <b>wildcard</b> keys from landing on the {@code *_ALL} umbrella boostables, which would
 * otherwise apply every such boost twice.
 *
 * <p><b>The bug.</b> The engine expands a wildcard key ({@code ROOM_FARM*>MUL: 1.25}) by prefix-matching
 * every registered boostable ({@code RMAP.get}: {@code k.startsWith(beg)}), and it does so in
 * {@code BOOSTING.finishSetup()} — after {@code initBeforeGameInited}, where the umbrellas are registered.
 * So {@code ROOM_FARM*} also matches {@code ROOM_FARM_ALL}. The child farms get the ×1.25 directly, the
 * umbrella gets it too, and {@code UmbrellaBooster} then multiplies every child by the umbrella's value:
 * ×1.5625 in total. An {@code >ADD} wildcard is worse: it lifts the umbrella's 1.0 base, so a
 * {@code +0.15} becomes an extra ×1.15 on every child. Vanilla does this all over — eight of nine races,
 * religions (Athuri, Crator), noble traits, player titles, ~70 event entries, and the Overtime bonus
 * ({@code ROOM*>MUL: 1.25}) — with no tech involved.
 *
 * <p><b>The fix.</b> Every string-keyed {@link BoostSpecs} parks its unresolved keys in a private
 * {@code PromiseList} on {@code BOOSTING.waiting}; each {@code Promise} keeps the key text it was written
 * with and its {@link Booster}. When a promise resolves, the <em>same</em> booster instance is used for
 * every boostable its key matched ({@code PromiseList.add}: {@code new BoostSpec(p.factor, b, null)}). So
 * an umbrella spec is wildcard-made exactly when its booster is the factor of a promise whose key contains
 * {@code *}. An explicit {@code ROOM_FARM_ALL>MUL} has its own promise and its own booster and is never
 * touched. This runs as a waiting action queued after every content source has registered (they all parse
 * in the GAME constructor) and <em>before</em> any connecter — so every aggregator ({@code RaceBoosts},
 * {@code PTech}'s {@code BoostCompound}, religions, titles, nobles, …) builds from the cleaned lists — and
 * before the {@code TARGET_RACE}/{@code TARGET_CLASS} applier, so filtered techs are cleaned too.
 *
 * <p><b>Reflection.</b> Appends one action to {@code BOOSTING.waiting} (same as
 * {@code TargetFilters.queueWaitingAction}) and <em>reads</em> {@code PromiseList.coll/all} and
 * {@code Promise.key/factor}, once, at init. Removal goes through the public
 * {@link BoostSpecs#remove(BoostSpec)}, which also detaches the booster from the umbrella for connected
 * lists. If any of it fails the guard logs loudly and does nothing — the pre-fix behaviour.
 *
 * <p><b>Known gap.</b> {@code PBonusSetting.add} resolves wildcards itself via {@code BOOSTING.MAP().get}
 * without a promise, so it is not covered. No vanilla content reaches it with an umbrella-matching
 * wildcard. Boost lists another mod parses <em>after</em> this mod's {@code initBeforeGameInited} sit
 * behind our action in the queue and are also not covered; the count is logged.
 */
public final class UmbrellaWildcardGuard {

    private static final String TAG = "[sos-extended-boostables] umbrella wildcard guard: ";
    private static final String WILDCARD = "*";

    /** Umbrellas registered for the game currently being constructed; drained by {@link #install()}. */
    private static final Set<Boostable> pending = java.util.Collections.newSetFromMap(new IdentityHashMap<>());

    private UmbrellaWildcardGuard() {}

    /** Record an umbrella boostable. Call from initBeforeGameInited, before {@link #install()}. Null-safe. */
    public static void register(Boostable umbrella) {
        if (umbrella != null)
            pending.add(umbrella);
    }

    /**
     * Queue the strip pass on {@code BOOSTING.waiting}. Call once from initBeforeGameInited, after every
     * umbrella is registered and <b>before</b> {@code TargetFilters.install()}, so the strip runs ahead of
     * the target-filter applier in the same queue.
     */
    public static void install() {
        final Set<Boostable> umbrellas = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        umbrellas.addAll(pending);
        pending.clear(); // boostables are rebuilt per game; the next game registers its own

        if (umbrellas.isEmpty()) {
            System.out.println(TAG + "no umbrellas registered; nothing to guard.");
            return;
        }

        final Object waiting;
        try {
            Field f = BOOSTING.class.getDeclaredField("waiting");
            f.setAccessible(true);
            waiting = f.get(null);
        } catch (Throwable t) {
            fail("could not access BOOSTING.waiting (" + t + ")");
            return;
        }

        ACTION strip = new ACTION() {
            @Override
            public void exe() {
                try {
                    strip(waiting, umbrellas, this);
                } catch (Throwable t) {
                    fail("strip pass failed (" + t + ")");
                }
            }
        };

        try {
            waiting.getClass().getMethod("add", Object.class).invoke(waiting, strip);
        } catch (Throwable t) {
            fail("could not queue on BOOSTING.waiting (" + t + ")");
        }
    }

    private static void strip(Object waiting, Set<Boostable> umbrellas, ACTION self) throws ReflectiveOperationException {
        Field fColl = null, fAll = null, fKey = null, fFactor = null;

        Map<String, Integer> perKey = new TreeMap<>();
        int removed = 0, sources = 0, late = 0;
        boolean afterSelf = false;

        for (Object action : (Iterable<?>) waiting) {
            if (action == self) {
                afterSelf = true;
                continue;
            }
            if (!action.getClass().getName().equals("game.boosting.BoostSpecs$PromiseList"))
                continue;
            if (afterSelf) { // not resolved yet when we run — see "Known gap"
                late++;
                continue;
            }

            if (fColl == null) {
                fColl = field(action.getClass(), "coll");
                fAll = field(action.getClass(), "all");
            }
            BoostSpecs coll = (BoostSpecs) fColl.get(action);
            Iterable<?> promises = (Iterable<?>) fAll.get(action);

            // Boosters that came from a wildcard key in this source.
            Set<Booster> wild = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
            for (Object p : promises) {
                if (fKey == null) {
                    fKey = field(p.getClass(), "key");
                    fFactor = field(p.getClass(), "factor");
                }
                String key = (String) fKey.get(p);
                if (key != null && key.contains(WILDCARD))
                    wild.add((Booster) fFactor.get(p));
            }
            if (wild.isEmpty())
                continue;

            java.util.ArrayList<BoostSpec> hits = new java.util.ArrayList<>();
            for (BoostSpec s : coll.all()) {
                if (umbrellas.contains(s.boostable) && wild.contains(s.booster))
                    hits.add(s);
            }
            if (hits.isEmpty())
                continue;

            sources++;
            for (BoostSpec s : hits) {
                coll.remove(s);
                removed++;
                perKey.merge(s.boostable.key, 1, Integer::sum);
            }
        }

        StringBuilder sb = new StringBuilder(TAG)
                .append("stripped ").append(removed).append(" wildcard-made umbrella boost(s) from ")
                .append(sources).append(" source(s)");
        if (!perKey.isEmpty())
            sb.append(": ").append(perKey);
        if (late > 0)
            sb.append("; ").append(late).append(" boost list(s) queued after the guard were not checked");
        System.out.println(sb.toString());
    }

    private static Field field(Class<?> c, String name) throws NoSuchFieldException {
        Field f = c.getDeclaredField(name);
        f.setAccessible(true);
        return f;
    }

    private static void fail(String why) {
        System.err.println(TAG + "DISABLED — " + why
                + ". Wildcard keys (e.g. a race's ROOM_FARM*) will apply twice through the *_ALL umbrellas.");
    }
}
