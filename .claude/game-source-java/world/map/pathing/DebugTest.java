package world.map.pathing;

import snake2d.LOG;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayCooShort;
import util.GUTIL;
import world.WORLD;
import world.map.regions.Region;
import world.map.road.WTRAV;

class DebugTest {

	public DebugTest() {
		ArrayCooShort coos = new ArrayCooShort(WORLD.TAREA());
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (WORLD.PATH().map.is.is(c)) {
				coos.get().set(c);
				coos.inc();
			}

		}

		int max = coos.getI();
		test("mixed", coos, max);

		coos.set(0);
		for (COORDINATE c : WORLD.TBOUNDS()) {
			Region r = WORLD.REGIONS().map.get(c);
			if (r != null && WORLD.PATH().map.is.is(c) && c.isSameAs(r.cx(), r.cy())) {
				coos.get().set(c);
				coos.inc();
			}
		}
		
		test("capitols", coos, max);

	}
	
	private void test(String title, ArrayCooShort coos, int max) {
		LOG.ln(title);
		test("simple", simple, coos, max, 1000);
		test("normal", make, coos, max, 1000);
		test("fancy", makeFancy, coos, max, 1000);
	}
	
	private void test(String title, TEST test, ArrayCooShort coos, int max, int amount) {
		long now = System.currentTimeMillis();
		int fails = 0;
		int diff1 = 0;
		int diff2 = 0;
		for (int i = 0; i < amount; i++) {

			coos.set(RND.rInt(max));
			int sx = coos.get().x();
			int sy = coos.get().y();

			coos.set(RND.rInt(max));
			int dx = coos.get().x();
			int dy = coos.get().y();

			PathTile t = test.make(WORLD.WATER().is(sx, sy), sx, sy, dx, dy);
			if (t == null) {
				fails++;
			}else {
				if (!t.isSameAs(dx, dy))
					diff1++;
				
				while(t.getParent() != null) {
					t = t.getParent();
				}
				
				if (!t.isSameAs(sx, sy))
					diff2++;
			}
		}
		LOG.ln(title + " " + (System.currentTimeMillis() - now) + " " + fails + " " + diff1 + " " + diff2);
	}

	interface TEST {
		PathTile make(boolean isShip, int fromX, int fromY, int tox, int toy);
	}

	public final TEST simple = new TEST() {

		@Override
		public PathTile make(boolean isShip, int fromX, int fromY, int tox, int toy) {

			if (!WORLD.PATH().map.is.is(fromX, fromY) || !WORLD.PATH().map.is.is(tox, toy)) {
				return null;
			}	

			GUTIL.flooder().init(WPATHING.class);
			GUTIL.flooder().pushSloppy(fromX, fromY, 0);
			while (GUTIL.flooder().hasMore()) {

				PathTile t = GUTIL.flooder().pollSmallest();
				if (t.isSameAs(tox, toy)) {
					GUTIL.flooder().done();
					return t;
				}
				for (int di = 0; di < DIR.ALL.size(); di++) {
					DIR d = DIR.ALL.get(di);
					int dx = t.x() + d.x();
					int dy = t.y() + d.y();
					if (WORLD.PATH().map.is.is(dx, dy))
						GUTIL.flooder().pushSmaller(dx, dy, t.getValue() + d.tileDistance()*WPATHING.cost(t.x(), t.y(), d), t);
				}

			}
			GUTIL.flooder().done();
			return null;
		}
	};
	
	public final TEST make = new TEST() {


		@Override
		public PathTile make(boolean isShip, int fromX, int fromY, int tox, int toy) {

			if (!WORLD.PATH().map.is.is(fromX, fromY) || !WORLD.PATH().map.is.is(tox, toy))
				return null;

			GUTIL.flooder().init(WPATHING.class);
			
			GUTIL.flooder().pushSloppy(fromX, fromY, 0);
			while (GUTIL.flooder().hasMore()) {

				PathTile t = GUTIL.flooder().pollSmallest();
				if (t.isSameAs(tox, toy)) {
					GUTIL.flooder().done();
					return t;
				}
				process(t);

			}

			GUTIL.flooder().done();
			return null;
		}
		
		private void process(PathTile t) {
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				int dx = t.x() + d.x();
				int dy = t.y() + d.y();
				if (WORLD.PATH().map.is.is(dx, dy)) {
					if (WORLD.WATER().isBig.is(t)) {
						if (WORLD.WATER().canTravelToByBoat(t.x(), t.y(), d) || WORLD.PATH().map.is.is(dx, dy, d)) {
							GUTIL.flooder().pushSmaller(dx, dy, t.getValue()+d.tileDistance()*WPATHING.cost(t.x(), t.y(), d), t);
						}
					}else {
						if (WORLD.WATER().isBig.is(dx, dy)) {
							if (WORLD.PATH().map.is.is(dx, dy))
								GUTIL.flooder().pushSmaller(dx, dy, t.getValue()+d.tileDistance()+WTRAV.PORT_PENALTY, t);
						}else {
							GUTIL.flooder().pushSmaller(dx, dy, t.getValue()+d.tileDistance()*WPATHING.cost(t.x(), t.y(), d), t);
						}
					}
				}
			}
			
			
		}
		
		
	};
	
	public final TEST makeFancy = new TEST() {
		
		@Override
		public PathTile make(boolean isShip, int fromX, int fromY, int tox, int toy) {

			PathTile t = WORLD.PATH().path(fromX, fromY, tox, toy);
			if (t == null) {
				LOG.ln(fromX + " " + fromY + " " + tox + " " + toy);
			}
			return t;
			
		}
		
		
	};

}
