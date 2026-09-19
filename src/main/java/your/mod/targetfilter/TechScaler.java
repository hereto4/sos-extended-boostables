package your.mod.targetfilter;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.player.PTech;
import game.faction.player.PTech.TechCurr;
import init.tech.TECH;
import init.tech.TechCost;
import snake2d.util.misc.CLAMP;

/**
 * "How far along is this tech, for the player" — the scaler a {@link TargetFilteredBooster}
 * feeds into {@code getValue}.
 *
 * <p>Mirrors what PTech's own BoostCompound does for an unfiltered tech
 * ({@code PTech.java:169-172}):
 *
 * <pre>    (1.0 - penalties[t.index()]) * level(t) / t.levelMax</pre>
 *
 * <p>The earlier implementation used only {@code level/levelMax}, so a filtered tech kept full
 * strength while an unfiltered one was throttled by a currency shortfall — a silent divergence
 * from vanilla. {@code penalties} is private, but the array is exactly reconstructible from
 * public API: PTech fills it as a cost-weighted mean of the per-currency penalties
 * ({@code PTech.java:299-307}), and {@code TECH.costs} / {@code TECH.costTotal} /
 * {@code PTech.currs()} / {@code TechCurr.penalty()} are all public. {@code TechCurr.cu} is
 * matched by identity rather than by index, so nothing depends on the engine's own indexing.
 *
 * <p>Calling the public {@link PTech#isPenaltyLocked(TECH)} first is what forces
 * {@code setPenalty()} when the ledger is dirty, so the per-currency figures read below are
 * current rather than one update stale.
 *
 * <p>The result is cached per {@code GAME.updateI()}. That is the engine's own idiom for exactly
 * this situation ({@code BoostCompound.Boo.vGet(Player)}, {@code BoostCompound.java:116-133}) and
 * it matters here: a per-subject booster on a damage boostable is consulted once per attack
 * resolution, not once per frame.
 */
final class TechScaler {

    private final TECH tech;
    private final int levelMax;

    private int cacheI = Integer.MIN_VALUE;
    private double cache;

    TechScaler(TECH tech) {
        this.tech = tech;
        this.levelMax = Math.max(1, tech.levelMax);
    }

    /** 0 when the tech is untaken; 1 at full level with no penalty. */
    double progress() {
        int ci = GAME.updateI();
        if (ci != cacheI) {
            cacheI = ci;
            cache = compute();
        }
        return cache;
    }

    private double compute() {
        PTech t = FACTIONS.player().tech;
        int level = t.level(tech);
        if (level <= 0) return 0.0;
        return CLAMP.d((1.0 - penalty(t)) * level / levelMax, 0, 1);
    }

    private double penalty(PTech t) {
        // Forces PTech.setPenalty() when dirty; the boolean itself is not interesting to us.
        t.isPenaltyLocked(tech);
        if (tech.costTotal <= 0) return 0.0;
        double p = 0;
        for (TechCost c : tech.costs) {
            for (TechCurr cu : t.currs()) {
                if (cu.cu == c.cu) {
                    p += cu.penalty() * c.amount / tech.costTotal;
                    break;
                }
            }
        }
        return CLAMP.d(p, 0, 1);
    }
}
