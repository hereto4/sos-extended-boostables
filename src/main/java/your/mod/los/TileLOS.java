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
 */
public final class TileLOS {

    private TileLOS() {}

    /**
     * @return true if a straight line from (x0,y0) to (x1,y1) is unobstructed by solid
     *         (wall/blocking) tiles, AND the two points are within {@code maxRange} tiles of each
     *         other (Chebyshev-ish straight-line distance, matching {@code COORDINATE.tileDistance}
     *         semantics used elsewhere in this codebase). Returns false if either endpoint is out of
     *         bounds.
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
