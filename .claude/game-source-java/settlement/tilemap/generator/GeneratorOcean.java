package settlement.tilemap.generator;

import static settlement.main.SETT.GRID;
import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.TERRAIN;
import static settlement.main.SettlementGrid.QUAD_SIZE;

import settlement.main.CapitolArea;
import settlement.main.SETT;
import settlement.main.SettlementGrid;
import settlement.tilemap.generator.GeneratorUtil.FertilityTmp;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.HeightMap;
import snake2d.util.rnd.RND;
import util.GUTIL;
import world.WORLD;

class GeneratorOcean {

	private final HeightMap height;
	private final FertilityTmp fer;
	private final double table;

	private final int MARGIN = (int) (QUAD_SIZE / 2.2);

	private final double MAX_VALUE = MARGIN;
	private final double ferValue = -3;

	GeneratorOcean(CapitolArea area, GeneratorUtil util) {
		table = area.getWatertabe();
		TERRAIN().WATER.groundWaterSalt.clear();
		this.fer = util.fer;
		this.height = util.height;

		GUTIL.flooder().init(this);

		for (int i = 0; i < GRID.tiles().size(); i++) {
			COORDINATE c = area.ts().get(i);
			SettlementGrid.Tile ut = GRID.tile(i);
			if (WORLD.WATER().OCEAN.is.is(c)) {
				add(ut.coo(DIR.C).x(), ut.coo(DIR.C).y());
				for (DIR d : DIR.ORTHO) {
					if (WORLD.WATER().OCEAN.is.is(c, d)) {
						add(ut.coo(d).x(), ut.coo(d).y());
					}
				}
			}
		}

		boolean has = GUTIL.flooder().hasMore();
		
		generateWater(MAX_VALUE);

		GUTIL.flooder().done();

		if (has)
			smooth();
		
	}

	private void smooth() {
		
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			GUTIL.flooder().setValue2(c, 0);
		}
		
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			if (SETT.TERRAIN().WATER.SHALLOW.is(c) && SETT.TERRAIN().WATER.groundWaterSalt.is(c)) {
				smooth(c);
			}
		}
		
	}
	
	private void smooth(COORDINATE start) {
		
		if (GUTIL.flooder().getValue(start) != 0)
			return;
		
		GUTIL.flooder().init(this);
		GUTIL.flooder().pushSloppy(start, 0);
		
		int am = 0;
		
		while(GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollSmallest();
			if (!SETT.TERRAIN().WATER.is.is(t)) {
				GUTIL.flooder().done();
				return;
			}
			GUTIL.flooder().setValue2(t, 1);
			if (SETT.TERRAIN().WATER.SHALLOW.is(t) && SETT.TERRAIN().WATER.groundWaterSalt.is(t)) {
				am++;
				
				for (DIR d : DIR.ORTHO) {
					if (SETT.IN_BOUNDS(t, d)) {
						GUTIL.flooder().pushSmaller(t, d, t.getValue()+1);
					}
				}
				
			}
		}
		GUTIL.flooder().done();
		
		if (am > 100) {
			return;
		}
		
		GUTIL.flooder().init(this);
		GUTIL.flooder().pushSloppy(start, 0);
		
		while(GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollSmallest();
			if (SETT.TERRAIN().WATER.SHALLOW.is(t) && SETT.TERRAIN().WATER.groundWaterSalt.is(t)) {
				SETT.TERRAIN().WATER.DEEP.placeRaw(t.x(), t.y());
				
				for (DIR d : DIR.ORTHO) {
					if (SETT.IN_BOUNDS(t, d)) {
						GUTIL.flooder().pushSmaller(t, d, t.getValue()+1);
					}
				}
				
			}
		}
		
		GUTIL.flooder().done();
	}
	
	private void place(int x, int y) {
		TERRAIN().WATER.DEEP.placeRaw(x, y);
		TERRAIN().WATER.groundWaterSalt.set(x, y, true);
		fer.increment(x, y, ferValue);
		// height.set(x, y, 0);
	}

	private void add(int x, int y) {
		PathTile t = GUTIL.flooder().pushSloppy(x, y, 0);
		if (t != null)
			t.setValue2(0);

	}

	private void generateWater(double max) {

		Flooder f = GUTIL.flooder();

		while (f.hasMore()) {

			PathTile t = f.pollSmallest();
			if (t.getValue() >= max) {
				break;
			}
			if (t.getValue() > 4.5 * max / 5) {
				TERRAIN().WATER.SHALLOW.placeRaw(t.x(), t.y());
				TERRAIN().WATER.groundWaterSalt.set(t, true);
				fer.increment(t.x(), t.y(), ferValue);
			} else {
				place(t.x(), t.y());
			}

			for (int i = 0; i < DIR.ALL.size(); i++) {
				int x = t.x() + DIR.ALL.get(i).x();
				int y = t.y() + DIR.ALL.get(i).y();

				if (IN_BOUNDS(x, y)) {
					double d = DIR.ALL.get(i).tileDistance();
					double radius = t.getValue2() + d;
					float h = (float) height.get(x, y);
					double value = 0.8 * radius;
					value += max * h * h * h;
					if (value > max)
						value = max;
					if (h < table)
						value = 0;
					PathTile t2 = GUTIL.flooder().pushSloppy(x, y, (float) value);
					if (t2 != null)
						t2.setValue2((float) radius);
				}

			}
		}

		double delta = 15.0;
		max += delta;

		while (f.hasMore()) {

			PathTile t = f.pollSmallest();
			double v = t.getValue();
			if (v >= max) {
				break;
			}
			double dd = 1.0 - (v - max + delta) / delta;
			if (dd < 0)
				dd = 0;
			if (dd > 1)
				dd = 1;
			double fe = dd * ferValue * (0.7 + 0.3 * RND.rFloat());

			{
				double r = max - t.getValue2();
				if (r < 8)
					TERRAIN().WATER.groundWaterSalt.set(t, true);
			}

			// else if(fe > -0.4)
			//
			fer.increment(t.x(), t.y(), fe);
			if (fer.get(t.x(), t.y()) < 0.1)
				SETT.GROUND().types.SAND.placeFixed(t.x(), t.y());
			
			if (RND.oneIn(100-95*dd) && SETT.TERRAIN().NADA.is(t)) {
				SETT.TERRAIN().DECOR_BEACH.placeRaw(t.x(), t.y());
			}
			// if (fer.get(t.x(), t.y()) >)
			// TERRAIN().WATER.placeRaw(t.x(), t.y());

			
			
			for (int i = 0; i < DIR.ALL.size(); i++) {
				int x = t.x() + DIR.ALL.get(i).x();
				int y = t.y() + DIR.ALL.get(i).y();

				if (IN_BOUNDS(x, y)) {
					double d = DIR.ALL.get(i).tileDistance();
					double radius = t.getValue2() + d;
					float h = (float) height.get(x, y);
					double value = 0.8 * radius;
					value += max * h * h * h;
					if (value > max)
						value = max;
					PathTile t2 = GUTIL.flooder().pushSloppy(x, y, (float) value);
					if (t2 != null)
						t2.setValue2((float) radius);
				}

			}
		}

	}

}
