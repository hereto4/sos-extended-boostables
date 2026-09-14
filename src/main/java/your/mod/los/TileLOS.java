package your.mod.los;

import settlement.main.SETT;

/**
 * A small, reusable straight-line-of-sight utility over the settlement tile grid.
 *
 * <p>Songs of Syx has no built-in "line of sight" concept for gameplay purposes (see the Search/
 * Stealth feasibility research, {@code HANDOFF_SEARCH_STEALTH.md} — the closest engine thing is
 * {@code settlement.thing.pointlight.LOS}, which is a per-tile point-light occlusion flag, not a
 * two-point ray trace). This class walks a Bresenham-style line between two tiles and asks
 * {@link SETT#PATH()}{@code .solidity} (the same wall/blocking map the engine's own pathfinding
 * uses) whether any tile strictly between the two endpoints blocks movement. It deliberately does
 * NOT test the two endpoint tiles themselves (a criminal or a guard standing right next to a wall
 * tile shouldn't be blinded by their own tile).
 *
 * <p>Public and stateless so any mod that depends on this one (e.g. a Heroes-style mod using it for
 * dungeon trap detection) can reuse the exact same algorithm rather than re-implementing it.
 *
 * <p><b>L1 (review §3):</b> {@code SETT.PATH().solidity} is walkability, not visibility
 * ({@code PATHING.java} — {@code availability.player < 0}, "solid from the players viewpoint"). Water,
 * cliffs and unclaimed terrain block line of sight here even though nothing would truly block a real
 * sightline over them; workable proxy, but this is not a literal vision-occlusion map.
 *
 * <p><b>L2 (review §3):</b> the {@code maxRange} gate below is Chebyshev ({@code max(dx,dy)}), NOT the
 * same metric as {@code snake2d.util.datatypes.COORDINATE.tileDistance} (which is octile:
 * {@code sqrt2*min + (max-min)}). Callers that also apply {@code COORDINATE.tileDistance} as a second
 * range filter (as {@code CrimeStealthCheck} does) get an effective radius of {@code maxRange} tiles
 * orthogonally but only {@code maxRange/sqrt2} (~71 %) diagonally — the two checks disagree at the
 * corners. Documented here rather than "fixed" because the tighter, LOS-side Chebyshev gate is the one
 * that matters for correctness (it is the loop bound below); the outer caller's octile check is simply
 * redundant/stricter on the diagonals, not wrong.
 */
public final class TileLOS {

    private TileLOS() {}

    /**
     * @return true if a straight line from (x0,y0) to (x1,y1) is unobstructed by solid
     *         (wall/blocking) tiles, AND the two points are within {@code maxRange} tiles of each
     *         other under Chebyshev distance ({@code max(|dx|,|dy|)} — see class javadoc L2 for how
     *         this differs from {@code COORDINATE.tileDistance}). Returns false if either endpoint is
     *         out of bounds.
     */
    public static boolean hasLineOfSight(int x0, int y0, int x1, int y1, int maxRange) {
        if (!SETT.IN_BOUNDS(x0, y0) || !SETT.IN_BOUNDS(x1, y1))
            return false;

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        if (Math.max(dx, dy) > maxRange)
            return false;

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        int x = x0, y = y0;
        while (x != x1 || y != y1) {
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x += sx; }
            if (e2 < dx)  { err += dx; y += sy; }

            if (x == x1 && y == y1)
                break; // reached the destination tile itself -- never tested for blocking

            if (SETT.PATH().solidity.is(x, y))
                return false;
        }
        return true;
    }
}
