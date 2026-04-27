package world.map.terrain;

import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.sets.Bitmap2D;
import util.GUTIL;
import util.text.D;
import world.WORLD;
import world.WORLD.WorldError;
import world.map.regions.centre.WorldCentrePlacablity;
import world.map.road.WTRAV;

final class GeneratorValidator extends Bitmap2D{

	private final Bitmap2D tested = new Bitmap2D(WORLD.TBOUNDS(), false);
	
	private static CharSequence ¤¤noRoom = "There is no room for a single settlement on the map. Make sure there is at least one 3x3 area where a city can be.";
	private static CharSequence ¤¤notConnected = "This area is isolated by the terrain. Make sure there is an open path to this place.";
	
	static {
		D.ts(GeneratorValidator.class);
	}
	
	public GeneratorValidator(WorldError error) {
		super(WORLD.TBOUNDS(), false);
		Rec tBound = new Rec(WORLD.TBOUNDS());
	
		boolean hasOne = false;
		
		for (COORDINATE c : tBound) {
			if (WorldCentrePlacablity.terrainC(c.x(), c.y()) == null) {
				fill(c.x(), c.y());
				hasOne = true;
				
				break;
			}
		}
		
		
		
		if (!hasOne) {
			if (error != null) {
				error.coo.set(WORLD.TWIDTH()/2, WORLD.THEIGHT()/2);
				error.problem = ¤¤noRoom;
				return;
			}
			
			GUTIL.flooder().init(this);
			PathTile t = GUTIL.flooder().pushSloppy(WORLD.TWIDTH()/2, WORLD.THEIGHT()/2, 0);
			hasOne = true;
			t = GUTIL.flooder().pushSmaller(WORLD.TWIDTH()/2+1, WORLD.THEIGHT()/2+1, 0, t);
			GUTIL.flooder().done();

			while(t != null) {
				WORLD.MOUNTAIN().pClear(t.x(), t.y());
				WORLD.WATER().NOTHING.placeRaw(t.x(), t.y());
				t = t.getParent();
			}
			fill(WORLD.TWIDTH()/2, WORLD.THEIGHT()/2);
			
			if (!tested.is(WORLD.TWIDTH()/2, WORLD.THEIGHT()/2)) {
				throw new RuntimeException("WTF " + WORLD.MOUNTAIN().is(WORLD.TWIDTH()/2, WORLD.THEIGHT()/2) + " " + WORLD.WATER().isBig.is(WORLD.TWIDTH()/2, WORLD.THEIGHT()/2));
			}
		}
		
		
		
		for (COORDINATE c : tBound) {
			if (!tested.is(c) && WorldCentrePlacablity.terrainC(c.x(), c.y()) == null) {
				if (error != null) {
					error.coo.set(c);
					error.problem = ¤¤notConnected;
					return;
				}
				connect(c);
				fill(c.x(), c.y());
				
			}
		}
	}

	private void connect(COORDINATE c) {
		
		GUTIL.flooder().init(this);
		GUTIL.flooder().pushSloppy(c, 0);
		
		while(GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollSmallest();
			if (tested.is(t)) {
				
				GUTIL.flooder().done();
				fix(t);
				GUTIL.flooder().reverse(t);
				fix(t);
				return;
			}
			

			
			for (DIR d : DIR.ORTHO) {
				if (WORLD.IN_BOUNDS(t, d)) {
					if (WTRAV.can(t.x(), t.y(), d, false))
						GUTIL.flooder().pushSmaller(t, d, t.getValue()+1, t);
					else
						GUTIL.flooder().pushSmaller(t, d, t.getValue()+15, t);
				}
			}
			
		}
		
		throw new RuntimeException();
		
		
	}
	

	

	
	
	private void fill(int sx, int sy) {
		GUTIL.flooder().init(this);
		GUTIL.flooder().pushSloppy(sx, sy, 0);
		
//		for (DIR d : DIR.ORTHO) {
//			if (WTRAV.isGoodLandTile(sx+d.x(), sy+d.y()) && WTRAV.can(sx, sy, d, false)) {
//				RES.flooder().pushSloppy(sx, sy, d, 0);
//			}
//		}
		
		while(GUTIL.flooder().hasMore()) {
			PathTile c = GUTIL.flooder().pollSmallest();
			
			tested.set(c, true);
			
			
			for (DIR d : DIR.ORTHO) {
				if (WORLD.IN_BOUNDS(c, d) && WTRAV.can(c.x(), c.y(), d, false))
					GUTIL.flooder().pushSmaller(c, d, c.getValue()+1);
			}
			
		}
		GUTIL.filler().done();
		
	}
	
	private void fix(PathTile t) {
		
		if (t.getParent() == null) {
			WORLD.MOUNTAIN().pClear(t.x(), t.y());
			WORLD.WATER().NOTHING.placeRaw(t.x(), t.y());
			return;
		}
		
		PathTile from = t;
		t = t.getParent();
		
		while(t != null) {
			DIR d = DIR.get(from, t);
			if (!WTRAV.can(from.x(), from.y(), d, false)) {
				WORLD.MOUNTAIN().pClear(from.x(), from.y());
				WORLD.WATER().NOTHING.placeRaw(from.x(), from.y());
				WORLD.MOUNTAIN().pClear(t.x(), t.y());
				WORLD.WATER().NOTHING.placeRaw(t.x(), t.y());
			}
			from = t;
			t = t.getParent();
		}
	}
	
}
