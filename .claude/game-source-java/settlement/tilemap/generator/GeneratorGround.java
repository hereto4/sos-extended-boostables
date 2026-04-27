package settlement.tilemap.generator;

import static settlement.main.SETT.GRID;
import static settlement.main.SETT.TERRAIN;
import static settlement.main.SETT.THEIGHT;
import static settlement.main.SETT.TWIDTH;

import init.type.CLIMATES;
import settlement.main.CapitolArea;
import settlement.main.SETT;
import settlement.main.SettlementGrid;
import settlement.tilemap.ground.GroundType;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.HeightMap;
import snake2d.util.rnd.RND;
import util.GUTIL;
import world.WORLD;

public class GeneratorGround {

	GeneratorGround(CapitolArea area, GeneratorUtil util) {
		
		forest(area, util);
		
		final HeightMap h = new HeightMap(TWIDTH, THEIGHT, 128, 4);
		
		double v = 0;
		int am = 0;
		for (COORDINATE c : area.tiles()) {
			v+= WORLD.GROUND().getter.get(c).moisture();
			am++;
		}
		v /= am;
		
		
		v = 0.5 + v*0.15; 
		
		GUTIL.flooder().init(this);
		
		GroundType worst = area.climate() == CLIMATES.HOT() ? SETT.GROUND().types.SAND : SETT.GROUND().types.INFERTILE;
		
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			if (SETT.GROUND().types.NORMAL.is(c)) {
				if (h.get(c) < v/2) {
					worst.placeFixed(c.x(), c.y());
				}else if (h.get(c) < v) {
					SETT.GROUND().types.PASTURE.placeFixed(c.x(), c.y());
				}
			}
			if (worst.is(c)) {
				GUTIL.flooder().pushSmaller(c, 0);
			}
			
		}
		
		while(GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollSmallest();
			double vv = (4.0 - t.getValue())/6.0;
			if (vv < 0)
				continue;
			util.fer.increment(t, -vv);
			for (DIR d : DIR.ALL) {
				if (SETT.IN_BOUNDS(t, d)) {
					GUTIL.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
				}
			}
		}
		
		GUTIL.flooder().done();
		
		for (COORDINATE c : new Rec(SETT.TILE_BOUNDS)) {
			double vv = util.fer.get(c.x(), c.y());			
			SETT.GROUND().MOISTURE_BASE.set(c, vv);
			
		}
		
		SETT.GROUND().init();

	}

	private void forest(CapitolArea area, GeneratorUtil util) {
		double value = util.json.d("FOREST_AMOUNT", 0, 1);

		final HeightMap ma = new HeightMap(SETT.TWIDTH, SETT.THEIGHT, 32, 4);
		
		if (area.isBattle) {
			value *= 0.75;
		}
		
		util.polly.checkInit();
		
		GUTIL.flooder().init(this);

		for (int i = 0; i < GRID.tiles().size(); i++) {
			
			SettlementGrid.Tile ut = GRID.tile(i);
			double wf = WORLD.FOREST().amount.get(area.ts().get(i));
			int a = (int) Math.ceil(value * wf * 10);
			while (a-- > 0) {
				int sx = ut.coo(DIR.W).x() + RND.rInt(SettlementGrid.QUAD_SIZE);
				int sy = ut.coo(DIR.N).y() + RND.rInt(SettlementGrid.QUAD_SIZE);
				
				int mm = (int) (5 + value*RND.rInt(10));
				while (mm-- > 0) {
					int x = sx + RND.rInt0(40);
					x = CLAMP.i(x, 0, SETT.TWIDTH);
					int y = sy + RND.rInt0(40);
					y = CLAMP.i(y, 0, SETT.THEIGHT);
					util.polly.checker.set(x, y, true);
					GUTIL.flooder().pushSloppy(x, y, 0);
				}
				
				
				
			}

		}

		while (GUTIL.flooder().hasMore()) {

			PathTile t = GUTIL.flooder().pollSmallest();
			if (!TERRAIN().NADA.is(t))
				continue;

			SETT.GROUND().types.FOREST.placeFixed(t.x(), t.y());

			double v = t.getValue();
			
			if (util.polly.checker.is(t)) {
				v = 0;
			}
			
			if (v/40.0 + ma.get(t) > 1) {
				continue;
			}
			


			

			for (DIR d : DIR.ALL) {
				if (SETT.IN_BOUNDS(t, d))
					GUTIL.flooder().pushSmaller(t, d, v + d.tileDistance());
			}

		}

		GUTIL.flooder().done();
	}
	
}
