package settlement.tilemap.generator;

import static settlement.main.SETT.GRID;
import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.TERRAIN;
import static settlement.main.SETT.THEIGHT;
import static settlement.main.SETT.TILE_MAP;
import static settlement.main.SETT.TWIDTH;
import static settlement.main.SettlementGrid.QUAD_SIZE;

import settlement.main.CapitolArea;
import settlement.main.SETT;
import settlement.main.SettlementGrid;
import settlement.tilemap.TileMap;
import snake2d.LOG;
import snake2d.PathGame;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.HeightMap;
import snake2d.util.rnd.Polymap;
import snake2d.util.sets.LinkedList;
import util.GUTIL;
import world.WORLD;

class GeneratorRiver {

	private final HeightMap height;
	private final TileMap m;
	private final int width;
	private final GeneratorUtil util;
	private final Polymap polly;
	
	private final PathGame.PathFancy p = new PathGame.PathFancy(5000);

	GeneratorRiver(CapitolArea area, GeneratorUtil util) {

		util.checker.init();
		
		this.util = util;
		polly = util.polly;
		height = util.height;
		this.m = TILE_MAP();
		width = util.json.i("RIVER_WIDTH", 1, 15);
		for (int i = 0; i < GRID.tiles().size(); i++) {
			SettlementGrid.Tile ut = GRID.tile(i);
			build(area.ts().get(i), ut);
		}
		
		GUTIL.flooder().init(this);
		
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			if (util.checker.is(c)) {
				GUTIL.flooder().pushSloppy(c, 0);
				double h = 1+util.fer.get(c);
				if (h < 0) {
					h = 0;
				}
					
				h = Math.sqrt(h/4.0);
				GUTIL.flooder().setValue2(c, h*20);
				//util.fer.set(c, 0);
				
			}
		}
		
		while (GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollSmallest();
			TERRAIN().WATER.SHALLOW.placeRaw(t.x(), t.y());
			
			
			double v = t.getValue();
			

			if (v >= t.getValue2()) {
				continue;
			}
			
			double h = 1*(1.0-Math.pow(v/t.getValue2(), 0.25));
			
			util.height.increment(t, h);

			for (int i = 0; i < DIR.ALL.size(); i++) {
				
				
				DIR d = DIR.ALL.get(i);
				int x = d.x() + t.x();
				int y = d.y() + t.y();
				
				if (!IN_BOUNDS(x, y))
					continue;
				
				v = d.tileDistance();
				v *= 1+CLAMP.d(Math.pow(height.get(x, y), 4), 0, 1)*1.5;
				v += t.getValue();

				if (v >= t.getValue2())
					continue;
				if (GUTIL.flooder().pushSmaller(x, y, (float) v) != null) {
					GUTIL.flooder().setValue2(x, y, t.getValue2());
				}

			}
		}
		
		GUTIL.flooder().done();

		
	}

	private void build(COORDINATE wtt, SettlementGrid.Tile ut) {
		
		
		if (!WORLD.WATER().isRivery.is(wtt))
			return;

		for (int i = 0; i < DIR.ORTHO.size(); i++) {
			DIR dStart = DIR.ORTHO.get(i);
			
			if (connected(wtt, dStart)) {
				
				int startX = ut.coo(dStart).x();
				int startY = ut.coo(dStart).y();

				boolean delta = WORLD.WATER().isDELTA.is(wtt);
				boolean connected = false;
				for (int j = 0; j < i; j++) {
					DIR dEnd = DIR.ORTHO.get(j);
					if (connected(wtt, dEnd)) {
						connected = true;
					}
				}
				for (int j = i + 1; j < DIR.ORTHO.size(); j++) {
					DIR dEnd = DIR.ORTHO.get(j);
					if (connected(wtt, dEnd)) {
						connected = true;
						int endX = ut.coo(dEnd).x();
						int endY = ut.coo(dEnd).y();
						boolean sDelta = WORLD.WATER().isDELTA.is(wtt, dStart);
						boolean eDelta = WORLD.WATER().isDELTA.is(wtt, dEnd);
						int sx = startX;
						int sy = startY;
							
						if (sDelta) {
							sx += dStart.x()*SettlementGrid.QUAD_HALF;
							sy += dStart.y()*SettlementGrid.QUAD_HALF;
						}
						
						if (eDelta) {
							endX += dEnd.x()*SettlementGrid.QUAD_HALF;
							endY += dEnd.y()*SettlementGrid.QUAD_HALF;
						}
						
						pave(positions(sx, sy, dStart, sDelta), positions(endX, endY, dEnd, eDelta), ut.bounds, sDelta ? 2.0 : 1.0, eDelta ? 2.0 : 1.0);
					}
				}
				if (!connected) {
					LinkedList<Coo> end = new LinkedList<>();
					end.add(new Coo(ut.coo(DIR.C).x(), ut.coo(DIR.C).y()));
					pave(
							positions(startX, startY, dStart, delta & !WORLD.WATER().RIVER.is(wtt, dStart)), 
							end, 
							ut.bounds, 1.0, 0);
				}
			}

		}

	}

	private boolean connected(COORDINATE wtt, DIR dStart) {
		if (WORLD.WATER().isRivery.is(wtt, dStart))
			return true;
		if (WORLD.WATER().isDELTA.is(wtt) && WORLD.WATER().isBig.is(wtt, dStart))
			return true;
		return false;
	}
	
	private LinkedList<Coo> positions(int x, int y, DIR d, boolean delta){

		
		if (x < 0) {
			x = 0;
		}
		if (x >= TWIDTH) {
			x = TWIDTH-1;
		}
		if (y < 0) {
			y = 0;
		}
		if (y >= THEIGHT) {
			y = THEIGHT-1;
		}
		
		int start = delta ? 16 : 0;
		int width = this.width;
		if (delta)
			width*=1.5;
		
		final LinkedList<Coo> res = new LinkedList<>();
		if (d.x() != 0) {
			for (int i = start; i < QUAD_SIZE*2 && res.size() < width; i++ ) {
				if (IN_BOUNDS(x, y+i) && polly.isEdge(x, y+i)) {
					res.add(new Coo(x, y+i));
				}
				if (IN_BOUNDS(x, y-i) && polly.isEdge(x, y-i)) {
					res.add(new Coo(x, y-i));
				}
			}
		}else {
			for (int i = start; i < QUAD_SIZE*2 && res.size() < width; i++ ) {
				if (IN_BOUNDS(x+i, y) && polly.isEdge(x+i, y)) {
					res.add(new Coo(x+i, y));
				}
				if (IN_BOUNDS(x-i, y) && polly.isEdge(x-i, y)) {
					res.add(new Coo(x-i, y));
				}
			}
		}
		return res;
		
		
	}
	
	private void pave(LinkedList<Coo> starts, LinkedList<Coo> ends,RECTANGLE bounds, double startWidth, double endWidth) {

		final PathGame.COST cm = new PathGame.COST() {

			@Override
			public double getCost(int fromX, int fromY, int toX, int toY) {

				if (!bounds.holdsPoint(toX, toY))
					return 50;

				if (m.topology.MOUNTAIN.is(toX, toY))
					return 20;

				if (m.topology.ROCK.is(toX, toY))
					return 20;

				if (!polly.isEdge(toX, toY))
					return 2.0;

				if (TERRAIN().WATER.SHALLOW.is(toX, toY))
					return 0.5;
				
				double mul = util.checker.is(toX, toY) ? 1.0 : 1.0;
				
				return mul; 

			}
		};
		
		LinkedList<Coo> flood = new LinkedList<>();
		
		while(!starts.isEmpty()) {
			
			Coo s = starts.removeFirst();
			Coo e = ends.removeFirst();
			ends.add(e);
			if (GUTIL.astar().getShortest(p, cm, s.x(), s.y(), e.x(), e.y())) {
//				double dW = (endWidth - startWidth) / p.getLength();
//				if (endWidth == startWidth)
//					dW = 0;
//				double w = startWidth;
				do {
					if (!util.checker.is(p)) {
						//util.fer.set(p, 1.0);
						util.checker.set(p, true);
					}else if (!TERRAIN().WATER.SHALLOW.is(p)){
						//util.fer.increment(p, w); 
					}
					//w += dW;
					
				} while (p.setNext());
			}else {
				LOG.ln("nono");
			}
			flood.add(s);
			
		}
		
		
		GUTIL.flooder().init(this);
		
		while(!flood.isEmpty()) {
			
			Coo s = flood.removeFirst();
			GUTIL.flooder().pushSloppy(s, 0);
			
		}

		while (GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollSmallest();
			TERRAIN().WATER.SHALLOW.placeRaw(t.x(), t.y());
			double v = t.getValue();

			if (v >= t.getValue2()) {
				continue;
			}

			for (int i = 0; i < DIR.ALL.size(); i++) {
				
				
				DIR d = DIR.ALL.get(i);
				int x = d.x() + t.x();
				int y = d.y() + t.y();
				
				if (!IN_BOUNDS(x, y))
					continue;
				
				if (util.checker.is(t, d)) {
					GUTIL.flooder().pushSmaller(x, y, 0);
				}

			}
		}
		
		GUTIL.flooder().done();
		

	}


}
