package settlement.misc.util;

import static settlement.main.SETT.ROOMS;
import static settlement.main.SETT.THINGS;

import init.resources.RBIT;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import settlement.room.main.Room;
import settlement.thing.THINGS.Thing;
import settlement.thing.ThingsResources.ScatteredResource;

public interface RESOURCE_TILE extends FINDABLE{

	public RESOURCE resource();
	public void resourcePickup();
	public int reservable();
	public int amount();
	
	public default boolean isFindable() {
		return resource() != null && amount() > 0;
	}
	
	public boolean isStorage();
	public boolean isPrio();
	
	public default double spoilRate() {
		return 1.0;
	}
	
	public default boolean hasRoom() {
		return true;
	}
	
	public interface RESOURCE_TILE_HASER {
		public RESOURCE_TILE resourceTile(int tx, int ty);
		public double degradeRate();
	}
	
	public static final Getter GETTER = new Getter();

	public static final class Getter {
		
		private final RBITImp tmp = new RBITImp();
		
		private Getter() {
			
		}
		
		public int reserve(boolean stored, boolean fetch, RESOURCE r, int tx, int ty, int amount) {
			int am = 0;
			while(am < amount) {
				RESOURCE_TILE t = reservable(r, stored, fetch, tx, ty);
				if (t == null)
					return am;
				while(am < amount && t.findableReservedCanBe()) {
					t.findableReserve();
					am ++;
				}
			}
			return am;
		}
		
		public final void unreserve(RESOURCE r, int tx, int ty, int amount) {
			while(amount > 0) {
				RESOURCE_TILE t = RESOURCE_TILE.GETTER.reserved(r, tx, ty);
				if (t == null)
					return;
				while(amount > 0 && t.findableReservedIs()) {
					t.findableReserveCancel();
					amount --;
				}
			}
		}
		
		public final int pickup(RESOURCE r, int tx, int ty, int amount) {
			int am = 0;
			while(am < amount) {
				RESOURCE_TILE t = reserved(r, tx, ty);
				if (t == null)
					return am;
				while(am < amount && t.findableReservedIs()) {
					t.resourcePickup();
					am ++;
				}
			}
			return am;
		}
		
		public RESOURCE_TILE reservable(RBIT scattered, RBIT stored, RBIT fetch, int tx, int ty) {
			
			tmp.clear();
			tmp.or(scattered).or(stored).or(fetch);
			
			ScatteredResource sc = THINGS().resources.getReservable(tx, ty,tmp);
			if (sc != null && sc.findableReservedCanBe()) {
				return sc;
			}
			
			Room room = ROOMS().map.get(tx, ty);
			if (room == null)
				return null;
			RESOURCE_TILE res = room.resourceTile(tx, ty);
			if (res == null)
				return null;
			RESOURCE r = res.resource();
			if (r == null)
				return null;
			if (!res.findableReservedCanBe())
				return null;
			if (res.isPrio()) {
				if (fetch.has(r))
					return res;
				return null;
			}
			if (res.isStorage()) {
				if (stored.has(r))
					return res;
				return null;
			}
			if (scattered.has(r))
				return res;
			return null;
		}
		
		public RESOURCE_TILE reservable(RESOURCE r, boolean stored, boolean fetch, int tx, int ty) {
			
			
			ScatteredResource sc = THINGS().resources.getReservable(tx, ty,r.bit);
			if (sc != null) {
				return sc;
			}
			
			Room room = ROOMS().map.get(tx, ty);
			if (room == null)
				return null;
			RESOURCE_TILE res = room.resourceTile(tx, ty);
			if (res == null)
				return null;
			if (r != res.resource())
				return null;
			if (!res.findableReservedCanBe())
				return null;
			if (res.isPrio() && !fetch) {
				return null;
			}else if (res.isStorage() && !stored) {
				return null;
			}
			return res;
		}
		
		public RESOURCE_TILE reserved(RESOURCE resource, int tx, int ty) {
			Room room = ROOMS().map.get(tx, ty);
			if (room != null) {
				RESOURCE_TILE res = room.resourceTile(tx, ty);
				if (res != null && res.resource() == resource && res.findableReservedIs()) {
					return res;
				}
			}
			for (Thing t : THINGS().get(tx, ty)) {
				if (t instanceof ScatteredResource) {
					ScatteredResource sc = ((ScatteredResource) t);
					if(sc.findableReservedIs() && sc.resource() == resource) {
						return sc;
					}
				}
			}
			
			return null;
		}
	}
	
}
