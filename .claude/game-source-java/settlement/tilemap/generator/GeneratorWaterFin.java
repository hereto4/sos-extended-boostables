package settlement.tilemap.generator;

import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.TERRAIN;

import settlement.main.CapitolArea;
import settlement.main.SETT;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import util.GUTIL;

class GeneratorWaterFin {



	GeneratorWaterFin(CapitolArea area, GeneratorUtil util) {

		addDepth(area, util);
		
	}
	
	private void addDepth(CapitolArea area, GeneratorUtil util) {
		GUTIL.flooder().init(this);
		
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			if (!TERRAIN().WATER.SHALLOW.is(c)) {
				GUTIL.flooder().pushSloppy(c, 0);
				GUTIL.flooder().setValue2(c, 1+ RND.rExpo()*50);
			}
		}
		
		while(GUTIL.flooder().hasMore()) {
			
			PathTile t = GUTIL.flooder().pollSmallest();
			double v2 = t.getValue();
			
			if (TERRAIN().WATER.SHALLOW.is(t)) {
				if (v2 <  4) {
					TERRAIN().NADA.placeRaw(t.x(), t.y());
				}else {
					double v = v2*(Math.pow(CLAMP.d(util.height.get(t), 0, 1), 1.7));
					if (v > 4)
						TERRAIN().WATER.DEEP.placeRaw(t.x(), t.y());
				}
				
			}
			
			for (DIR d : DIR.ALL) {
				
				int x = t.x() + d.x();
				int y = t.y() + d.y();
				if (IN_BOUNDS(x, y)) {
					double v = v2;
					if (v2 >= 4) {
						v += d.tileDistance();
					}else {
						v += d.tileDistance()*t.getValue2();
					}
					if (v > 0) {
						if (GUTIL.flooder().pushSmaller(x, y, v) != null) {
							GUTIL.flooder().setValue2(x, y, t.getValue2());
						}
					}
					
					
				}
				
			}
			
		}
		
		GUTIL.flooder().done();
		
		//fixWays(area, util);
		
		SETT.TERRAIN().WATER.groundWater.clear();
		GUTIL.flooder().init(this);
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			if (TERRAIN().WATER.SHALLOW.is(c)) {
				GUTIL.flooder().pushSloppy(c, 0);
			}
		}
		while(GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollSmallest();
			if (t.getValue() > 8)
				break;
			SETT.TERRAIN().WATER.groundWater.set(t, true);
			for (DIR d : DIR.ALL) {
				if (SETT.IN_BOUNDS(t, d))
					GUTIL.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
			}
		}
		
		GUTIL.flooder().done();
	}




}
