package settlement.room.service.module;

import static settlement.main.SETT.PATH;

import game.time.TIME;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.ROOMA;
import settlement.room.service.module.RoomServiceNeed.ROOM_SERVICE_NEED_HASER;
import snake2d.PathTile;
import snake2d.util.MATH;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayCooShort;
import util.GUTIL;

public abstract class ROOM_SPECTATOR {

	public static final double WORK_ENDSD = (double)2.0/TIME.hoursPerDay();
	public static final double WORK_STARTSD = (double)MATH.mod(3-TIME.workHours(), TIME.hoursPerDay())/TIME.hoursPerDay();
	
	
	private static final ArrayCooShort spots = new ArrayCooShort(20*35);
	
	public abstract boolean is(int sx, int sy);
	public abstract COORDINATE lookAt(int sx, int sy);
	public abstract RoomServiceAccess service();
	
	public boolean shouldCheer(int sx, int sy) {
		return false;
	}
	
	public boolean shouldBoo(int sx, int sy) {
		return false;
	}
	
	public interface ROOM_SPECTATOR_HASER extends ROOM_SERVICE_NEED_HASER{
		
		ROOM_SPECTATOR spec();
		
	}
	
	public COORDINATE getDestination(COORDINATE roomT) {
		ROOMA r = SETT.ROOMS().map.rooma.get(roomT);
		GUTIL.flooder().init(this);
		spots.set(0);
		for (COORDINATE c : r.body()) {
			if (r.is(c)) {
				GUTIL.flooder().pushSloppy(c.x(), c.y(), 5);
			}
		}
		
		while(GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollGreatest();
			if (isSpot(t.x(), t.y())) {
				spots.get().set(t.x(), t.y());
				spots.inc();
			}
			if (t.getValue() <= 0)
				continue;
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR dd = DIR.ALL.get(di);
				int dx = t.x() + dd.x();
				int dy = t.y() + dd.y();
				double c = SETT.PATH().coster.player.getCost(t.x(), t.y(), dx, dy);
				if (c > 0) {
					DIR old = dd;
					if (t.getParent() != null)
						old = DIR.get(t.getParent(), t);
					if (old != dd)
						c*= 2;
					c *= dd.tileDistance();
					GUTIL.flooder().pushGreater(dx, dy, t.getValue()-c);
				}
			}
		}
		
		GUTIL.flooder().done();
		
		int max = spots.getI();
		if (max == 0)
			return roomT;
		
		return spots.set(RND.rInt(max));
	}
	
	public boolean isSpot(int tx, int ty) {
		if (!SETT.IN_BOUNDS(tx, ty))
			return false;
		if (SETT.ROOMS().map.is(tx, ty))
			return false;
		AVAILABILITY av = PATH().availability.get(tx,ty);
		if (av.player >= 0 && av.player < AVAILABILITY.Penalty && av.from == 0) {
			return true;
		}
		return false;
	}
	
	public boolean isOpenNow() {
		return true;
	}
	
	public void doSomeThingExtraWhenAccess(Humanoid a) {
		
	}
	
}
