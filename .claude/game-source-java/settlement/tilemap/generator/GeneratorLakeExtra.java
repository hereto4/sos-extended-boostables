package settlement.tilemap.generator;
import static settlement.main.SETT.TERRAIN;

import settlement.main.CapitolArea;
import settlement.main.SETT;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.Polymap;
import snake2d.util.rnd.RND;
import util.GUTIL;

class GeneratorLakeExtra {

	private final Polymap polly;
	
	
	GeneratorLakeExtra(CapitolArea area, GeneratorUtil util) {

		polly = util.polly;
		polly.checkInit();


		extraLake(area, util);
		
		
		
		
	}
	
	private void extraLake(CapitolArea area, GeneratorUtil util) {
		if (area.isBattle)
			return;
		int am = 0;
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			if (SETT.TERRAIN().WATER.is.is(c) && SETT.TERRAIN().WATER.groundWater.is(c.x(), c.y())) {
				am++;
			}
		}
		if (am > 100)
			return;
		
		util.polly.checkInit();
		
		GUTIL.flooder().init(this);
		
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			if (SETT.TILE_BOUNDS.isOnEdge(c.x(), c.y()) || TERRAIN().MOUNTAIN.is(c) || ((TERRAIN().WATER.is.is(c)))) {
				GUTIL.flooder().pushSloppy(c, 0);
				GUTIL.flooder().setValue2(c, 1);
			}
		}
		
		while(GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollSmallest();
			if (t.getValue() > 48)
				break;
			for (DIR d : DIR.ALL) {
				if (SETT.IN_BOUNDS(t, d))
					GUTIL.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
			}
		}
		
		
		
		
		
		double b = 0;
		int tx = SETT.TWIDTH/2;
		int ty = SETT.THEIGHT/2;
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			if (!GUTIL.flooder().hasBeenPushed(c.x(), c.y())) {
				double d = RND.rFloat();
				if (d >= b) {
					b = d;
					tx = c.x();
					ty = c.y();
				}
			}
		}

		GUTIL.flooder().done();
		
		int radius = 12;
		for (int y1 = (int) (-radius); y1 < radius; y1++) {
			int y = y1 + ty;
			if (y < 0 || y >= SETT.TWIDTH)
				continue;
			for (int x1 = (int) (-radius); x1 < radius; x1++) {
				int x = tx + x1;
				if (x < 0 || x >= SETT.TWIDTH)
					continue;
				double d = Math.sqrt(x1*x1 + y1*y1);
				if (d < radius) {
					
					polly.checker.set(x, y, true);
				}
				
				
			}
		}
		
		
		
		
		
		GUTIL.flooder().init(this);
		
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			if (polly.checker.is(c.x(), c.y())) {
				if (util.height.get(c) < 0.8) {
					TERRAIN().WATER.SHALLOW.placeRaw(c.x(), c.y());
					//GROUND().fertilityGet(0.5).placeRaw(c.x(), c.y());
					GUTIL.flooder().pushSloppy(c, 0);
				}
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

