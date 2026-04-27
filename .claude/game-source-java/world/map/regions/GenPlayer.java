package world.map.regions;

import game.faction.FACTIONS;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.rnd.RND;
import util.GUTIL;
import world.WORLD;
import world.map.regions.centre.WCentre;
import world.map.regions.centre.WorldCentrePlacablity;
import world.map.road.WTRAV;

class GenPlayer {

	public static boolean gen() {
		int pcx = -1;
		int pcy = -1;
		
		if (WORLD.REGIONS().player.is(WORLD.REGIONS().player.cx(), WORLD.REGIONS().player.cy())) {
			pcx = WORLD.REGIONS().player.cx();
			pcy = WORLD.REGIONS().player.cy();
		}else if (WORLD.GEN().playerX != -1) {
			pcx = WORLD.GEN().playerX;
			pcy = WORLD.GEN().playerY;
		}else {
			for (int i = 0; i < 1000; i++) {
				int x = RND.rInt(WORLD.TWIDTH());
				int y = RND.rInt(WORLD.THEIGHT());
				
				if (WorldCentrePlacablity.terrainC(x, y) == null) {
					pcx = x;
					pcy = y;
					break;
				}
			}
			if (pcx == -1) {
				for (COORDINATE c : WORLD.TBOUNDS()) {
					if (WorldCentrePlacablity.terrainC(c.x(), c.y()) == null) {
						pcx = c.x();
						pcy = c.y();
						break;
					}
				}
				
			}
		}
		
		if (pcx == -1)
			return false;
		
		fixWays(pcx, pcy);

		Rec pp = new Rec(WCentre.TILE_DIM);
		pp.moveC(pcx+1, pcy+1);
		boolean pharbour = false;
		
		for (COORDINATE c : pp) {
			WORLD.REGIONS().pmap.set(c, WORLD.REGIONS().player);
			pharbour |= WORLD.WATER().isBig.is(c);
		}
		
		pharbour = false;
		
		if (pharbour) {
			
			GUTIL.flooder().init(GenPlayer.class);
			if (WTRAV.isGoodLandTile(pcx, pcy)) {
				GUTIL.flooder().pushSloppy(pcx, pcy, 0);
			}
			if (WORLD.WATER().isBig.is(pcx, pcy)) {
				for (int di = 0; di < DIR.ORTHO.size(); di++) {
					DIR d = DIR.ORTHO.get(di);
					int dx = pcx + d.x();
					int dy = pcy + d.y();
					if (WTRAV.isGoodLandTile(dx, dy) && WTRAV.canLand(pcx, pcy, d, false)) {
						GUTIL.flooder().pushSloppy(dx, dy, 0);
					}
				}
			}
			Rec cc = new Rec(WCentre.TILE_DIM+2);
			cc.moveC(pp.cX(), pp.cY());
			while(GUTIL.flooder().hasMore()) {
				PathTile t = GUTIL.flooder().pollSmallest();
				if (!pp.holdsPoint(t) && WTRAV.isHarbour(t.x(), t.y())) {

					
					int px = t.x();
					int py = t.y();
					while(t != null) {
						WORLD.REGIONS().pmap.set(t, WORLD.REGIONS().player);
						t = t.getParent();
					}
					GUTIL.flooder().done();
					GUTIL.flooder().init(GenPlayer.class);
					GUTIL.flooder().pushSloppy(px, py, 0);
					while(GUTIL.flooder().hasMore()) {
						t = GUTIL.flooder().pollSmallest();
						if (t.getValue() > 2)
							continue;
						WORLD.REGIONS().pmap.set(t, WORLD.REGIONS().player);
						for (DIR d : DIR.ORTHO) {
							if (WORLD.WATER().isBig.is(t.x(), t.y(), d))
								GUTIL.flooder().pushSmaller(t, d, t.getValue()+1, t);
						}
					}
					
					
					break;
				}
				
				for (DIR d : DIR.ORTHO) {
					if (cc.holdsPoint(t, d) && WTRAV.canLand(t.x(), t.y(), d, false))
						GUTIL.flooder().pushSmaller(t, d, t.getValue()+1, t);
				}
			}
			
			GUTIL.flooder().done();
		}
		
		WORLD.REGIONS().player.info.centreSet(pcx, pcy);

		WORLD.REGIONS().player.info.name().clear().add(FACTIONS.player().name);
		return true;
	}
	
	private static void fixWays(int px, int py) {
		WORLD.TERRAIN().secretFixWays();
	}

	
}
