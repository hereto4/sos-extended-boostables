package settlement.path.finders;

import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.PATH;
import static settlement.main.SETT.TERRAIN;

import settlement.main.SETT;
import settlement.path.path.SPath;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import util.GUTIL;

public final class SFinderUnreachable {
	
	SFinderUnreachable() {
		
	}
	
	public boolean find(COORDINATE start, SPath path, int maxDistance) {
		
		if (TERRAIN().WATER.open.is(start.x(), start.y())){
			return findWater(start, path, maxDistance);
					
		}else if (PATH().connectivity.is(start)) {
			return false;
		}
		
		int tx = start.x();
		int ty = start.y();
		
		GUTIL.flooder().init(this);
		PathTile t = GUTIL.flooder().pushSloppy(tx, ty, 0);
		
		
		while(GUTIL.flooder().hasMore()) {
			t = GUTIL.flooder().pollSmallest();
			
			if (PATH().connectivity.is(t)) {
				GUTIL.flooder().done();
				path.setDirect(start.x(), start.y(), t.x(), t.y(), t, true);
				return true;
			}
			if (t.getValue() > maxDistance)
				continue;
			
			for (DIR d : DIR.ALL) {
				double v = t.getValue();
				tx = t.x()+d.x();
				ty = t.y()+d.y();
				
				if (isPassable(t.x(), t.y(), d)) {
					GUTIL.flooder().pushSloppy(tx, ty, v+d.tileDistance(), t);
				}
			}		
		}
		
		
		GUTIL.flooder().done();
		return false;
		
	}
	
	
	public boolean findWater(COORDINATE start, SPath path, int maxDistance) {
		
		int tx = start.x();
		int ty = start.y();
		if (isLand(tx, ty)) {
			path.request(tx, ty, tx, ty, false);
			return path.isSuccessful();
		}
		
		GUTIL.flooder().init(this);
		PathTile t = GUTIL.flooder().pushSloppy(tx, ty, 0);
		
		int bx = -1;
		int by = -1;
		
		PathTile backup = null;
		
		while(GUTIL.flooder().hasMore()) {
			t = GUTIL.flooder().pollSmallest();
			if (isLand(t.x(), t.y())) {
				if (PATH().connectivity.is(t)) {
					GUTIL.flooder().done();
					path.setDirect(start.x(), start.y(), t.x(), t.y(), t, true);
					return true;
				}else {
					bx = t.x();
					by = t.y();
				}
			}else if (!PATH().solidity.is(t) && backup == null)
				backup = t;
			if (t.getValue() > maxDistance)
				break;
			
			for (DIR d : DIR.ALL) {
				double v = t.getValue();
				tx = t.x()+d.x();
				ty = t.y()+d.y();
				
				if (isPassable(t.x(), t.y(), d)) {
					GUTIL.flooder().pushSloppy(tx, ty, v+d.tileDistance(), t);
				}
			}		
		}
		
		if (bx != -1) {
			t = GUTIL.flooder().get(bx, by);
			GUTIL.flooder().done();
			path.setDirect(start.x(), start.y(), t.x(), t.y(), t, true);
			return true;
		}
		GUTIL.flooder().done();
		if (backup != null) {
			path.setDirect(start.x(), start.y(), backup.x(), backup.y(), backup, true);
			return true;
		}
		
		
		return false;
		
	}
	
	private boolean isPassable(int tx, int ty, DIR d) {
		if (!IN_BOUNDS(tx+d.x(), ty) || SETT.PATH().availability.get(tx+d.x(), ty).tileCollide)
			return false;
		if (!IN_BOUNDS(tx, ty+d.y()) || SETT.PATH().availability.get(tx, ty+d.y()).tileCollide)
			return false;
		if (!IN_BOUNDS(tx+d.x(), ty+d.y()) || SETT.PATH().availability.get(tx+d.x(), ty+d.y()).tileCollide)
			return false;
		return true;
	}
	
	private boolean isLand(int tx, int ty) {
		return !isWater(tx, ty) && !PATH().solidity.is(tx, ty);
	}
	
	private boolean isWater(int tx, int ty) {
		return SETT.ENTITIES().submerged.is(tx, ty);
	}

}

