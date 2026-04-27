package settlement.tilemap.generator;
import static settlement.main.SETT.GRID;
import static settlement.main.SETT.TERRAIN;
import static settlement.main.SETT.THEIGHT;
import static settlement.main.SETT.TWIDTH;

import settlement.main.CapitolArea;
import settlement.main.SETT;
import settlement.main.SettlementGrid;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.Polymap;
import snake2d.util.rnd.RND;
import util.GUTIL;
import world.WORLD;

class GeneratorLake {

	private final int PS = 4;
	private final Polymap polly;
	private final double radius;
	
	
	GeneratorLake(CapitolArea area, GeneratorUtil util) {
		
		radius = util.json.d("LAKE_SIZE", 0.1, 1.0)*200;
		
		polly = util.polly;
		polly.checkInit();
		
		for (int i = 0; i < GRID.tiles().size(); i++) {
			SettlementGrid.Tile t = GRID.tile(i);
			//WorldWater.AreaTileWater w = area.water(i);
			COORDINATE wt = area.ts().get(i);
			
			if (!WORLD.WATER().LAKE.is.is(wt))
				continue;
			for (DIR d : t.getDirs()) {
				if (WORLD.WATER().LAKE.is.is(wt, d)) {
					sink(t.cooInner(d));
				}
			}
		}
		
		/**
		 * refining the edges with smaller polly samples
		 */
		GUTIL.filler().init(this);
		for (int y = 0; y < SETT.TWIDTH; y++) {
			for (int x = 0; x < SETT.TWIDTH; x++) {
				
				if (util.polly.checker.is(x/PS, y/PS)) {
					GUTIL.filler().fill(x, y);
				}
				
				
			}
		}
		
		util.polly.checkInit();
		while(GUTIL.filler().hasMore()){
			
			COORDINATE t = GUTIL.filler().poll();
			util.polly.checker.set(t, true);
			/**
			 * Adding some scattered shite
			 */
			if (RND.oneIn(500)) {
				int x = (int) (t.x() + (RND.rBoolean() ? RND.rExpo()*20 : -RND.rExpo()*20)) ;
				int y =(int) (t.y() + (RND.rBoolean() ? RND.rExpo()*20 : -RND.rExpo()*20)) ;
				util.polly.checker.set(x, y, true);
			}
			
		}
		GUTIL.filler().done();
		
		int islands = (int) (util.json.d("LAKE_ISLANDS", 0.0, 1.0)*100);


		
		if (islands != 0) {
			
			for (int i = RND.rInt(islands); i > 0; i--) {
				int x = RND.rInt(TWIDTH);
				int y = RND.rInt(THEIGHT);
				util.polly.checker.set(x, y, false);
				for (int l = RND.rInt(20); l > 0; l--) {
					int x2 = x + RND.rInt0(30);
					int y2 = y + RND.rInt0(30);
					util.polly.checker.set(x2, y2, false);
				}
			}
		}


		for (COORDINATE c : SETT.TILE_BOUNDS) {
			if (polly.checker.is(c.x(), c.y())) {
				if (util.height.get(c) < 0.8) {
					TERRAIN().WATER.SHALLOW.placeRaw(c.x(), c.y());
					//GROUND().fertilityGet(0.5).placeRaw(c.x(), c.y());
				}
			}
		}
		
	}
	

	
	public void sink(int cx, int cy) {
		for (int y1 = (int) (-radius); y1 < radius; y1++) {
			int ty = y1 + cy;
			if (ty < 0 || ty >= SETT.TWIDTH)
				continue;
			for (int x1 = (int) (-radius); x1 < radius; x1++) {
				int tx = cx + x1;
				if (tx < 0 || tx >= SETT.TWIDTH)
					continue;
				double d = Math.sqrt(x1*x1 + y1*y1);
				if (d < radius) {
					
					polly.checker.set(tx/PS, ty/PS, true);
				}
				
				
			}
		}
		
	}

	
	public void sink(COORDINATE c) {
		sink(c.x(), c.y());
	}



}

