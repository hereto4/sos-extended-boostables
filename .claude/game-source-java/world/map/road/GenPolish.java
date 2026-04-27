package world.map.road;

import static world.WORLD.IN_BOUNDS;
import static world.WORLD.REGIONS;
import static world.WORLD.TBOUNDS;
import static world.WORLD.WATER;

import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.map.MAP_DOUBLE;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.Polymap;
import util.GUTIL;
import world.WORLD;
import world.map.regions.Region;

final class GenPolish {

	public final Polymap polly = new Polymap(TBOUNDS(), 6, 1);
	
	public GenPolish(ACTION util, MAP_DOUBLE infra) {

		removeUnusedRoads();
		
		for (Region r : REGIONS().all()) {
			if (r.info.area() > 0) {
				randomRoad(r);
			}
		}

		
	}
	
	private void removeUnusedRoads() {

		
		for (COORDINATE c : TBOUNDS()) {
			if (WORLD.REGIONS().cTile.is(c))
				continue;
			boolean needed = false;
			boolean canBeRemoved = false;
			
			for (DIR d : DIR.ORTHO) {
				if (WORLD.ROADS().is(c, d) && WORLD.ROADS().is(c, d.next(2))) {
					if (!WORLD.ROADS().is(c, d.next(1)) || (WORLD.REGIONS().map.get(c) != null && WORLD.REGIONS().map.get(c) != WORLD.REGIONS().map.get(c, d))) {
						needed = true;
						break;
					}else
						canBeRemoved = true;
					
				}	
			}
			if (!needed && canBeRemoved) {
				WORLD.ROADS().set(c, false);
			}
		}
	}

	private void randomRoad(Region r) {

		GUTIL.flooder().init(this);
		GUTIL.flooder().pushSloppy(r.info.cx(), r.info.cy(), 0);
		
		int amount = (int) (r.info.area()*r.info.moisture());

		while (GUTIL.flooder().hasMore() && amount > 0) {
			PathTile t = GUTIL.flooder().pollSmallest();

			if (r != REGIONS().map.get(t)) {
				continue;
			}

			for (DIR d : DIR.ALL) {
				if (WORLD.REGIONS().map.get(t, d) != r)
					continue;
			}
			
			if (!WORLD.ROADS().is(t)) {
				WORLD.ROADS().set(t, true);
				WORLD.ROADS().minified.set(t, true);
				amount--;
			}
			
			
			
			for (DIR d : DIR.ORTHO) {
				int dx = t.x() + d.x();
				int dy = t.y() + d.y();
				if (IN_BOUNDS(dx, dy)) {
					
					if (WORLD.ROADS().is(dx, dy)) {
						GUTIL.flooder().pushSmaller(dx, dy, 0);
					}
					double v = 1;
					if (v >= 0 && polly.isEdge(dx, dy) && (!WATER().isBig.is(dx, dy))) {
						GUTIL.flooder().pushSmaller(dx, dy, t.getValue() + v * d.tileDistance(), t);
							
					}
				}
			}

		}

		GUTIL.flooder().done();

	}
	
}
