package your.mod.targetfilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import game.boosting.BoostSpec;
import game.boosting.Boostable;
import game.boosting.Booster;
import init.race.RACES;
import init.race.Race;
import init.tech.TECH;
import init.tech.TECHS;
import init.type.HCLASS;
import init.type.HCLASSES;

/**
 * For each tech the registry flagged with {@code TARGET_RACE} and/or
 * {@code TARGET_CLASS}:
 * <ul>
 *   <li>Removes every {@link BoostSpec} from {@code tech.boosters} so PTech's
 *       BoostCompound aggregator won't include it in the player's global tech
 *       bonus (which would otherwise apply the boost to <em>all</em> subjects
 *       regardless of race/class).</li>
 *   <li>Installs a {@link TargetFilteredBooster} directly on each affected
 *       {@link Boostable} (via {@code Boostable.addFactor}). That booster returns
 *       identity for any target that doesn't pass the tech's {@link SubjectFilter},
 *       so only the matching subjects see the boost.</li>
 * </ul>
 *
 * <p>Run from a {@code BOOSTING.waiting} action queued during
 * {@code initBeforeGameInited()}: {@code tech.boosters.all()} is only populated
 * once the BoostSpecs' PromiseList resolves at the start of
 * {@code BOOSTING.finishSetup()}, and BoostCompound's connecter that aggregates
 * from it runs immediately after. The waiting/connecter split gives us a window
 * where the BoostSpecs are live but not yet folded into the player's Boo.
 *
 * <p>Generalized from the standalone target-race-tech mod's
 * {@code TargetRaceApplier} to resolve a {@link SubjectFilter} (race and/or class)
 * per tech instead of a single race.
 */
final class TargetFilterApplier {

    /** (TECH, original BoostSpec) pairs pulled from tech.boosters during applyAll. */
    private static final List<Saved> saved = new ArrayList<>();

    private static final class Saved {
        final TECH tech;
        final BoostSpec original;
        Saved(TECH t, BoostSpec s) { this.tech = t; this.original = s; }
    }

    private TargetFilterApplier() {}

    static void applyAll() {
        saved.clear();
        if (TargetFilterRegistry.flags.isEmpty()) {
            TargetFilterRegistry.announce("apply: nothing flagged, nothing to do");
            return;
        }

        Map<String, Race> raceByName = new HashMap<>();
        for (Race r : RACES.all()) raceByName.put(r.key, r);
        Map<String, TECH> techByKey = new HashMap<>();
        for (TECH t : TECHS.ALL()) techByKey.put(t.key, t);

        int rewroteSpecs = 0;
        int unmatchedTechs = 0;
        int unresolvedFilters = 0;
        int techsWithNoBoosts = 0;

        for (Map.Entry<String, TargetFilterRegistry.Spec> e : TargetFilterRegistry.flags.entrySet()) {
            String techKey = e.getKey();
            TargetFilterRegistry.Spec spec = e.getValue();

            TECH tech = techByKey.get(techKey);
            if (tech == null) {
                unmatchedTechs++;
                TargetFilterRegistry.announce("  ! TECH key not loaded: " + techKey
                        + " (file scan vs. TECHS.ALL() mismatch — ignored)");
                continue;
            }

            SubjectFilter filter = resolve(spec, raceByName, techKey);
            if (filter == null) { unresolvedFilters++; continue; }

            // Snapshot before we mutate — tech.boosters.all() is a live view.
            List<BoostSpec> snapshot = new ArrayList<>();
            for (BoostSpec s : tech.boosters.all()) snapshot.add(s);
            TargetFilterRegistry.announce("  * tech=" + techKey + " " + describe(spec)
                    + " -> snapshot size=" + snapshot.size() + " (tech.levelMax=" + tech.levelMax + ")");
            if (snapshot.isEmpty()) {
                techsWithNoBoosts++;
                TargetFilterRegistry.announce("    (no BOOST entries — no-op)");
                continue;
            }

            for (BoostSpec original : snapshot) {
                Boostable bo = original.boostable;
                Booster src = original.booster;

                boolean removed = removeBoostSpec(tech, original);

                TargetFilteredBooster filt = new TargetFilteredBooster(
                        tech.boosters.info,
                        src.isMul,
                        src.from(),
                        src.to(),
                        filter,
                        tech);
                BoostSpec wrapped = new BoostSpec(filt, bo, null);
                bo.addFactor(wrapped);
                saved.add(new Saved(tech, original));
                rewroteSpecs++;
                TargetFilterRegistry.announce("    - rewrote boostable=" + bo.key
                        + " isMul=" + src.isMul + " from=" + src.from() + " to=" + src.to()
                        + " (removedFromTechBoosters=" + removed + ")");
            }
        }

        TargetFilterRegistry.announce("apply: rewrote " + rewroteSpecs + " BoostSpec(s) across "
                + TargetFilterRegistry.flags.size() + " flagged tech(s); unmatchedTechs="
                + unmatchedTechs + ", unresolvedFilters=" + unresolvedFilters
                + ", techsWithNoBoosts=" + techsWithNoBoosts);
    }

    /** Resolve a scan {@link TargetFilterRegistry.Spec} into a live {@link SubjectFilter}; null if unsatisfiable. */
    private static SubjectFilter resolve(TargetFilterRegistry.Spec spec, Map<String, Race> raceByName, String techKey) {
        Race race = null;
        if (spec.raceName != null) {
            race = raceByName.get(spec.raceName);
            if (race == null) {
                TargetFilterRegistry.announce("  ! Race not in RACES.all(): " + spec.raceName
                        + " for tech " + techKey + " (ignored)");
                return null;
            }
        }

        HCLASS hclass = null;
        boolean exslave = false;
        if (spec.classToken != null) {
            if ("EXSLAVE".equals(spec.classToken)) {
                exslave = true;
            } else {
                hclass = classFor(spec.classToken);
                if (hclass == null) {
                    TargetFilterRegistry.announce("  ! Unknown TARGET_CLASS token: " + spec.classToken
                            + " for tech " + techKey + " (ignored)");
                    return null;
                }
            }
        }

        return new SubjectFilter(race, hclass, exslave);
    }

    private static HCLASS classFor(String token) {
        switch (token) {
            case "CITIZEN": return HCLASSES.CITIZEN();
            case "NOBLE":   return HCLASSES.NOBLE();
            case "SLAVE":   return HCLASSES.SLAVE();
            case "OTHER":   return HCLASSES.OTHER();
            default:        return null;
        }
    }

    private static String describe(TargetFilterRegistry.Spec spec) {
        StringBuilder sb = new StringBuilder();
        if (spec.raceName != null) sb.append("race=").append(spec.raceName);
        if (spec.classToken != null) {
            if (sb.length() > 0) sb.append(' ');
            sb.append("class=").append(spec.classToken);
        }
        return sb.toString();
    }

    /** BoostSpecs.remove returns void; we check via size delta. */
    private static boolean removeBoostSpec(TECH tech, BoostSpec spec) {
        int before = tech.boosters.all().size();
        tech.boosters.remove(spec);
        return tech.boosters.all().size() < before;
    }

    /**
     * After PTech's BoostCompound has aggregated, the per-Boostable Boos are
     * built and cached — they don't re-read tech.boosters again. So we can safely
     * put the originals back into tech.boosters at that point: the tech-tree UI
     * iterates tech.boosters to render the effect list, but the runtime boost
     * pipeline no longer cares.
     */
    static void reAddOriginalsForUI() {
        if (saved.isEmpty()) {
            TargetFilterRegistry.announce("re-add: nothing to do");
            return;
        }
        for (Saved s : saved) s.tech.boosters.push(s.original);
        TargetFilterRegistry.announce("re-added " + saved.size()
                + " BoostSpec(s) to tech.boosters for tech-tree UI display");
    }
}
