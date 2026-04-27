package world.map.terrain;

import static world.WORLD.MOUNTAIN;
import static world.WORLD.THEIGHT;
import static world.WORLD.TWIDTH;
import static world.WORLD.WATER;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.HeightMap;
import snake2d.util.rnd.RND;
import util.GUTIL;
import world.WORLD;

class GeneratorOcean {

	private double limit = 0.3;
	private final boolean[][] check = new boolean[WORLD.THEIGHT()][WORLD.TWIDTH()];

	GeneratorOcean(HeightMap height) {
		
		
		
		
		for (int y = 0; y < THEIGHT(); y++) {
			for (int x = 0; x < TWIDTH(); x++) {
				double v = height.get(x, y);
				if (v < limit && !MOUNTAIN().coversTile(x, y)) {
					if (v < limit*0.75)
						WATER().LAKE.deep.placeRaw(x, y);
					else
						WATER().LAKE.normal.placeRaw(x, y);
				}else {
					WATER().NOTHING.placeRaw(x, y);
				}
			}
		}
		
		
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (WORLD.WATER().LAKE.is.is(c) & !check[c.y()][c.x()]) {
				int size = check(c.x(), c.y());
				if (size > 150) {
					size -= 150;
					double ch = size /2500.0;
					if (RND.rFloat() < ch)
						makeOcean(c.x(), c.y());
				}
			}
			
		}
		
		
	}
	
	private int check(int tx, int ty) {
		GUTIL.filler().init(this);
		GUTIL.filler().fill(tx, ty);
		int size = 0;
		while(GUTIL.filler().hasMore()) {
			COORDINATE c = GUTIL.filler().poll();
			if (WORLD.WATER().LAKE.is.is(c)) {
				check[c.y()][c.x()] = true;
				size++;
			}else {
				continue;
			}
			for (DIR d : DIR.ORTHO) {
				if (WORLD.IN_BOUNDS(c, d))
					GUTIL.filler().fill(c, d);
			}
		}
		GUTIL.filler().done();
		return size;
		
	}
	
	private int makeOcean(int tx, int ty) {
		GUTIL.filler().init(this);
		GUTIL.filler().fill(tx, ty);
		int size = 0;
		while(GUTIL.filler().hasMore()) {
			COORDINATE c = GUTIL.filler().poll();
			if (WORLD.WATER().LAKE.is.is(c)) {
				if (WORLD.WATER().LAKE.deep.is(c))
					WORLD.WATER().OCEAN.deep.placeRaw(c.x(), c.y());
				else
					WORLD.WATER().OCEAN.normal.placeRaw(c.x(), c.y());
				size++;
			}else {
				continue;
			}
			for (DIR d : DIR.ORTHO) {
				if (WORLD.IN_BOUNDS(c, d))
					GUTIL.filler().fill(c, d);
			}
		}
		GUTIL.filler().done();
		return size;
		
	}


}
