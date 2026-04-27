package game.battle.formation;

import game.battle.Army;
import init.constant.C;
import init.race.Race;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import snake2d.util.datatypes.DIR;

public final class DivPlacability{

	private static final DIR[] dirIter = new DIR[] { DIR.NW, DIR.NE, DIR.SE, DIR.SW };
	
	public static boolean pixelIsBlocked(int x1, int y1, Race race, Army defender) {
		int tileSize = race.physics.hitBoxsize()/2;
		for (DIR d : dirIter) {
			int tx = (x1 + d.x() * (tileSize)) >> C.T_SCROLL;
			int ty = (y1 + d.y() * (tileSize)) >> C.T_SCROLL;
			if (!tileIsOK(tx, ty, defender)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean pixelIsBlocked(int x1, int y1, int dist, Army defender) {
		int tileSize = dist/2;
		for (DIR d : dirIter) {
			int tx = (x1 + d.x() * (tileSize)) >> C.T_SCROLL;
			int ty = (y1 + d.y() * (tileSize)) >> C.T_SCROLL;
			if (!tileIsOK(tx, ty, defender)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean tileIsOK(int tx, int ty, Army defender) {
		AVAILABILITY a = SETT.PATH().availability.get(tx, ty);
		if (a == null)
			return false;
		return !a.isSolid(defender);
	}
	
	public static boolean checkPixelStep(int fx, int fy, int tox, int toy, Race tz, Army defender) {
		if (pixelIsBlocked(fx, fy, tz, defender))
			return false;
		if (pixelIsBlocked(tox, toy, tz, defender))
			return false;
		if (pixelIsBlocked(fx, toy, tz, defender))
			return false;
		if (pixelIsBlocked(tox, fy, tz, defender))
			return false;
		return true;
	}
	
	public static boolean checkStep(int fx, int fy, int tx, int ty, Army defender) {
		if (!tileIsOK(tx, ty, defender))
			return false;
		
		if (tx != fx || ty != fy)
			if (!tileIsOK(tx, fy, defender) || ! tileIsOK(fx, ty, defender))
				return false;
		
		return true;
	}

}
