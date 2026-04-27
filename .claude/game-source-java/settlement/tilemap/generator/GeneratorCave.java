package settlement.tilemap.generator;

import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.TERRAIN;
import static settlement.main.SETT.THEIGHT;
import static settlement.main.SETT.TWIDTH;

import settlement.main.CapitolArea;
import settlement.main.SETT;
import snake2d.PathGame;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LinkedList;
import util.GUTIL;

class GeneratorCave {

	
	private final PathGame.PathFancy p = new PathGame.PathFancy(5000);
	private final int caveSize;
	private final double tunnels;
	
	GeneratorCave(CapitolArea area, GeneratorUtil util, LinkedList<COORDINATE> caves){
		
		int amount = (int) (util.json.d("CAVE_AMOUNT", 0, 1.0)*300);
		caveSize = (int) (util.json.d("CAVE_SIZE", 0, 1.0)*30);
		tunnels = util.json.d("CAVE_TUNNELS", 0, 1);
		
		for (int i = 0; i < amount; i++) {
			int x = RND.rInt(TWIDTH);
			int y = RND.rInt(THEIGHT);
			if (cave(area, util, x, y)) {
				caves.add(new Coo(x, y));
			}
			
		}
		
		
		GUTIL.flooder().init(this);
		for (int y = 0; y < SETT.TWIDTH; y++) {
			for (int x = 0; x < SETT.TWIDTH; x++) {
				if (TERRAIN().CAVE.is(x, y))
					GUTIL.flooder().pushSloppy(x, y, 0);
			}
		}
		
		while(GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollSmallest();
			TERRAIN().CAVE.placeRaw(t.x(), t.y());
			
			for (DIR d : DIR.ORTHO) {
				if (!IN_BOUNDS(t, d))
					continue;
				if (TERRAIN().MOUNTAIN.is(t, d)) {
					if (RND.oneIn(3)) {
						GUTIL.flooder().pushSmaller(t, d, 1);
					}else {
						GUTIL.flooder().close(t, d, 0);
					}
				}
				
			}
		}
		GUTIL.flooder().done();
		
	}
	
	private boolean cave(CapitolArea area, GeneratorUtil util, int x, int y) {
		
		GUTIL.flooder().init(this);
		LinkedList<Coo> coos = new LinkedList<>();
		
		util.polly.checkInit();
		util.polly.checker.set(x, y, true);
		
		GUTIL.flooder().pushSloppy(x, y, 0);
		
		for (int i = RND.rInt(caveSize); i > 0; i--) {
			int x2 = x + RND.rInt0(10+caveSize);
			int y2 = y + RND.rInt0(10+caveSize);
			if (SETT.IN_BOUNDS(x2, y2)) {
				if (!util.polly.checker.is(x2, y2)) {
					util.polly.checker.set(x2, y2, true);
					GUTIL.flooder().pushSloppy(x2, y2, 0);
					coos.add(new Coo(x2, y2));
				}
				
			}
		}
		
		while(GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollSmallest();
			if (!TERRAIN().MOUNTAIN.is(t)) {
				GUTIL.flooder().done();
				return false;
			}
			
			for (DIR d : DIR.ORTHO) {
				if (!IN_BOUNDS(t, d))
					continue;
				if (!util.polly.checker.is(t, d)) {
					if (t.getValue() < 5)
						GUTIL.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
				}else {
					GUTIL.flooder().pushSmaller(t, d, 0);
				}
				
			}
		}
		GUTIL.flooder().done();
		GUTIL.flooder().init(this);
		for (Coo c : coos) {
			GUTIL.flooder().pushSloppy(c, 0);
		}
		
		while(GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollSmallest();
			if (TERRAIN().MOUNTAIN.is(t)) {
				TERRAIN().CAVE.placeRaw(t.x(), t.y());
			}
			
			for (DIR d : DIR.ORTHO) {
				if (!IN_BOUNDS(t, d))
					continue;
				if (util.polly.checker.is(t, d))
					GUTIL.flooder().pushSmaller(t, d, 0);
			}
		}
		GUTIL.flooder().done();
//		if (RND.oneIn(2))
//			tunnel(area, util,x, y);
		
		int a = RND.rInt((int) (1 + coos.size()*tunnels*10));
		
		for (int i = 0; i < a; i++) {
			Coo c = coos.removeFirst();
			tunnel(area, util,c.x(), c.y());
			coos.add(c);
		}
			
		
		
		return true;
		
	}
	
	private void tunnel (CapitolArea area, GeneratorUtil util, int startX, int startY) {
		
		final PathGame.COST cm = new PathGame.COST() {

			@Override
			public double getCost(int fromX, int fromY, int toX, int toY) {

				if (!IN_BOUNDS(toX, toY))
					return -1;

				if (!util.polly.isEdge(toX, toY)) {
					return 5;
				}
				
				if (TERRAIN().MOUNTAIN.is(toX, toY))
					return 1;
				
				if (TERRAIN().CAVE.is(toX, toY))
					return 2;
				
				return 0;

			}
		};
		
		final PathGame.DEST dm = new PathGame.DEST() {
			@Override
			protected boolean isDest(int x, int y) {
				if (TERRAIN().CAVE.is(x, y)) {
					int d = Math.abs(x-startX);
					d += Math.abs(y-startY);
					return d > 60;
				}
				return !TERRAIN().MOUNTAIN.is(x, y);
			}
			
			@Override
			protected float getOptDistance(int x, int y) {
				// TODO Auto-generated method stub
				return 0;
			}
		};

		if (!GUTIL.astar().getNearest(p, cm, dm, startX, startY)) {
			return;
		}

		GUTIL.flooder().init(this);

		int max = 60 + RND.rInt(60);
		
		do {
			int x = p.x();
			int y = p.y();
			GUTIL.flooder().pushSmaller(x, y, 0);
		} while (p.setNext() && max-- >= 0);

		while (GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollGreatest();
			if (TERRAIN().MOUNTAIN.is(t))
			TERRAIN().CAVE.placeRaw(t.x(), t.y());
			if (TERRAIN().MOUNTAIN.is(t, DIR.E))
				TERRAIN().CAVE.placeRaw(t.x()+1, t.y());
			
			
			
		}
		
		GUTIL.flooder().done();
		
	}
	
}
