package settlement.tilemap.generator;

import static settlement.main.SETT.THEIGHT;
import static settlement.main.SETT.TWIDTH;

import settlement.main.SETT;
import settlement.tilemap.floor.TGrowth.Grower;
import settlement.tilemap.ground.GroundType;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.HeightMap;
import util.GUTIL;

class GeneratorGrowth {

	private final GG[] map = new GG[128];

	GeneratorGrowth() {
		HeightMap ferMap = new HeightMap(TWIDTH, THEIGHT, 8, 2);

		add(SETT.TILE_MAP().growth.bush, 0, 0.1);
		add(SETT.TILE_MAP().growth.tree, 0.1, 0.25);
		add(SETT.TILE_MAP().growth.bush, 0.25, 0.35);
		fill(ferMap, SETT.GROUND().types.FOREST, 0.175, 1.0);
		
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			double f = ferMap.get(c);
			int i = (int) (f*(map.length-1));
			GG g = map[i];
			if (g != null) {
				double a = (f - g.from)/(g.to-g.from);
				g.g.set(c.x(), c.y(), a);
			}
			
		}
		
		ferMap = new HeightMap(TWIDTH, THEIGHT, 32, 2);
		fill(ferMap, SETT.GROUND().types.NORMAL, 1.0, 0.2);
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			
			double f = ferMap.get(c);
			
			if (f > 0.65) {
				double a = (f-0.65)/0.2;
				a = CLAMP.d(a, 0, 1);
				Grower g = SETT.TILE_MAP().growth.flower;
				g.set(c.x(), c.y(), a);
			}
			
			
		}
		
		ferMap = new HeightMap(TWIDTH, THEIGHT, 16, 1);
		fill(ferMap, SETT.GROUND().types.PASTURE, 1.0, 0.2);
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			
			double f = ferMap.get(c);
			
			if (f > 0.7) {
				double a = (f-0.7)/0.2;
				a = CLAMP.d(a, 0, 1);
				Grower g = SETT.TILE_MAP().growth.bush;
				g.set(c.x(), c.y(), a);
				
			}else {
				f = ferMap.get((c.x()+64)%SETT.TWIDTH, (c.y()+64)%SETT.THEIGHT);
				if (f > 0.8) {
					double a = (f-0.8)/0.15;
					a = CLAMP.d(a, 0, 1);
					Grower g = SETT.TILE_MAP().growth.mushroom;
					g.set(c.x(), c.y(), a);
				}
			}
			
			
		}
		
	}
	
	void add(Grower[] gs, Grower g, double amount) {
		int am = (int) Math.ceil(gs.length*amount);
		for (int i = 0; i < gs.length; i++) {
			if (gs[i] == null) {
				gs[i] = g;
				am--;
				if (am <= 0)
					return;
			}
		}
	}
	
	private void fill(HeightMap ferMap, GroundType type, double to, double strength) {
		
		GUTIL.flooder().init(this);
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			if (type.is(c)) {
				GUTIL.flooder().pushSloppy(c, 0);
			}
		}
		
		while(GUTIL.flooder().hasMore()){
			PathTile t = GUTIL.flooder().pollSmallest();
			
			double v = 1.0 - t.getValue() / 10.0;
			if (v < 0)
				break;
			
			double inc = to - ferMap.get(t);
			inc *= strength*v;
			ferMap.increment(t, inc);
			
			for (DIR d : DIR.ALL) {
				if (SETT.IN_BOUNDS(t, d)) {
					GUTIL.flooder().pushSloppy(t, d, t.getValue()+d.tileDistance());
				}
			}
			
		}
		GUTIL.flooder().done();
	}
	
	private void add(Grower g, double from, double to) {
		GG gg = new GG(g, from, to);
		int i = (int) (from*(map.length-1));
		int t = (int) (to*(map.length-1));
		for(; i < t; i++) {
			map[i] = gg;
		}
	}


	
	
	private static class GG{
		
		public final Grower g;
		public final double from;
		public final double to;
		
		GG(Grower g, double from, double to){
			this.g = g;
			this.from = from;
			this.to = to;
		}
		
	}

}