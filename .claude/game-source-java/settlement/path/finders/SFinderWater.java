package settlement.path.finders;

import static settlement.main.SETT.PATH;
import static settlement.main.SETT.TERRAIN;

import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.path.path.SPath;
import settlement.room.main.Room;
import settlement.room.water.pool.ROOM_POOL;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import util.GUTIL;

public final class SFinderWater extends SFinderFindable{
	

	SFinderWater() {
		super("water");
		new TestPath(name, this);
	}
	
	public boolean findLand(COORDINATE start, SPath path, int maxDistance) {
		
		int tx = start.x();
		int ty = start.y();
		if (isLand(tx, ty)) {
			path.request(tx, ty, tx, ty, false);
			return path.isSuccessful();
		}
		
		GUTIL.flooder().init(this);
		PathTile t = GUTIL.flooder().pushSloppy(tx, ty, 0);
		
		
		while(GUTIL.flooder().hasMore()) {
			t = GUTIL.flooder().pollSmallest();
			if (isLand(t.x(), t.y())) {
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
				
				if (isLand(tx,ty) && PATH().coster.player.getCost(t.x(), t.y(), tx, ty) > 0) {
					if (PATH().connectivity.is(tx,ty)) {
						GUTIL.flooder().pushSloppy(tx, ty, v+1, t);
					}else {
						GUTIL.flooder().pushSloppy(tx, ty, v+100, t);
					}
				}else if(isWater(tx, ty)) {
					GUTIL.flooder().pushSloppy(tx, ty, v+1, t);
				}
			}		
		}
		
		
		GUTIL.flooder().done();
		return false;
		
	}
	
	private boolean isLand(int tx, int ty) {
		return !isWater(tx, ty) && !PATH().solidity.is(tx, ty);
	}
	
	private boolean isWater(int tx, int ty) {
		return SETT.ENTITIES().submerged.is(tx, ty);
	}

	public FINDABLE get(int tx, int ty) {
		FINDABLE s = TERRAIN().WATER.service.get(tx, ty);
		if (s != null)
			return s;
		Room r = SETT.ROOMS().map.get(tx, ty);
		if (r != null && r.blueprint() instanceof ROOM_POOL) {
			FINDABLE f = ((ROOM_POOL) r.blueprint()).fservice(tx, ty);
			if (f != null)
				return f;
		}
		return null;
	}
	
	@Override
	public FINDABLE getReservable(int tx, int ty) {
		FINDABLE f = get(tx, ty);
		if (f != null && f.findableReservedCanBe())
			return f;
		return null;
	}

	@Override
	public FINDABLE getReserved(int tx, int ty) {
		FINDABLE f = get(tx, ty);
		if (f != null && f.findableReservedIs())
			return f;
		return null;
	}

}

