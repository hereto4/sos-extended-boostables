package settlement.maintenance;

import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.PATH;
import static settlement.main.SETT.ROOMS;

import game.GAME;
import game.faction.FACTIONS;
import init.resources.RESOURCE;
import settlement.path.AVAILABILITY;
import settlement.room.main.Room;
import snake2d.util.bit.Bits;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;

final class MRoom extends MType{
	
	private static double MIN_JOBS = 4;
	private static final Bits extraBit =		new Bits(0b1000_0000_0000_0000_0000_0000_0000_0000);
	private static final Bits jobsPlaced = 		new Bits(0b0111_1111_0000_0000_0000_0000_0000_0000);
	private static final Bits secret = 			new Bits(0b0000_0000_0000_0000_0000_0000_0000_1111);
	private static final Bits tot = 			new Bits(0b0000_0000_1111_1111_1111_1111_1111_0000);
	private static final int jobSize = (int) (tot.mask/(3));
	
	MRoom(){

	}
	
	
	
	@Override
	public boolean validate(int tx, int ty) {
		Room room = ROOMS().map.get(tx, ty);
		if (room != null) {
			ROOM_DEGRADER deg = room.degrader(tx, ty);
			if (deg == null || isBlocked(tx, ty, room)) {
				return false;
			}	
			return true;
		}
		return false;
	}
	
	private boolean isBlocked(int tx, int ty, Room room) {
		if (room.isBadMaintenanceTile(tx, ty))
			return true;
		for (DIR d : DIR.ORTHO) {
			int dx = tx + d.x();
			int dy = ty + d.y();
			if (!IN_BOUNDS(dx, dy)) {
				continue;
			}
			if (room.isSame(tx, ty, dx, dy)) {
				if (PATH().availability.get(dx, dy).player <= AVAILABILITY.ROOM.player && PATH().availability.get(dx, dy).player > 0)
					return false;
			}else if (PATH().availability.get(dx, dy).player > 0)
				return false;
			
		}
			
		return true;

	}
	
	@Override
	public boolean degrade(int tx, int ty, int tile, double rate) {
		Room room = ROOMS().map.get(tx, ty);
		if (room == null)
			return false;
		
		ROOM_DEGRADER deg = room.degrader(tx, ty);
		if (deg == null)
			return true;
		
		double r = deg.rate(rate);
		if (locked(room, tx, ty))
			r*= 3;
		inc(tx, ty, r/room.area(tx, ty));
		return true;
	}

	
	@Override
	public void vandalize(int tx, int ty) {
		inc(tx, ty, MIN_JOBS);
	}
	
	private static double min = tot.mask >> 2;
	private static double II = 1.0/(tot.mask -min);
	
	public static double degrade(int data) {
		double w = tot.get(data);
		if (w > min) {
			w -= min;
			w*= II;
			
			return w;
		}
		return 0;
	}
	
	private void inc(int tx, int ty, double am) {
		Room room = ROOMS().map.get(tx, ty);
		ROOM_DEGRADER deg = room.degrader(tx, ty);
		
		int jz = jobSize(room.area(tx, ty));
		
		double d = jz*am;
		int dd = (int) d;
		if (d > 0) {
			if (RND.rFloat() < dd-d)
				dd++;
		}else {
			if (RND.rFloat() < -(dd-d))
				dd--;
		}
		
		
		
		
		if (dd != 0) {
			
			int data = deg.getData();
			
			int a = tot.get(data) + dd + secret.get(data)*jz;
			int s = CLAMP.i(a/jz, 0, 4);
			a -= s*jz;
			
			a = CLAMP.i(a, 0, tot.mask);
			
			data = tot.set(data, a);
			data = secret.set(data, s);
			set(deg, data);
		}
		
		
		
		
	}

	private static int jobSize(int area) {
		return (int) Math.ceil((jobSize)/area);
	}

	@Override
	public int shouldPlaceResource(int tx, int ty) {
		Room room = ROOMS().map.get(tx, ty);
		if (room == null)
			return 0;
		ROOM_DEGRADER deg = room.degrader(tx, ty);
		if (deg == null)
			return 0;
		
		if (deg.resSize() == 0)
			return 0;
		
		double am = 0;
		for (int ri = 0; ri < deg.resSize(); ri++) {
			am += deg.resAmount(ri);
		}
		
		double rr = ROOM_DEGRADER.rate(1, 1, 1, am, room.area(tx, ty));
		double rrw = rr-ROOM_DEGRADER.rate(1, 1, 1, 0, room.area(tx, ty));

		if (rrw > rr*RND.rFloat()) {
			double lim = am*RND.rFloat();
			am = 0;
			for (int ri = 0; ri < deg.resSize(); ri++) {
				int a = deg.resAmount(ri);
				if (a > 0) {
					am += deg.resAmount(ri);
					if (am >= lim && a > 0) {
						return ri+1;
					}
				}
				
			}
		}
		return 0;
	}
	
	@Override
	public double resRate(int tx, int ty, int ri) {
		if (ri == 0 )
			return 0;
		ri--;
		
		Room room = ROOMS().map.get(tx, ty);
		if (room != null) {
			ROOM_DEGRADER deg = room.degrader(tx, ty);
			if (deg != null) {
				if (ri >=  deg.resSize())
					return 0;
				return ROOM_DEGRADER.rateResource(1, deg.base(), room.isolation(tx, ty), deg.resAmount(ri))/room.area(tx, ty);
			}	
		}
		return 0;
	}
	
	@Override
	public void maintain(int tx, int ty) {
		Room room = ROOMS().map.get(tx, ty);

		ROOM_DEGRADER deg = room.degrader(tx, ty);
		if (deg == null) {
			GAME.Notify("MAINTENANCE" + tx + " " + ty);
			return;
		}

		
		
		inc(tx, ty, -1);
		set(deg, jobsPlaced.inc(deg.getData(), -1));
	}

	@Override
	public RESOURCE res(int tx, int ty, int ri) {
		if (ri == 0)
			return null;
		ri-= 1;
		Room room = ROOMS().map.get(tx, ty);
		if (room != null) {
			if (room.constructor() != null && room.constructor().resources() > 0)
				return room.constructor().resource(ri%room.constructor().resources());
			
		}
		return null;
	}

	@Override
	public boolean shouldPlace(int tx, int ty, boolean was) {
		Room room = ROOMS().map.get(tx, ty);
		ROOM_DEGRADER deg = room.degrader(tx, ty);
		int data = deg.getData();
		
		if (isBlocked(tx, ty, room)) {
			return false;
		}
		
		
		if (locked(room, tx, ty))
			return false;

		if (jobsPlaced.isMaximum(data))
			return false;

		int jobs = (tot.get(data))/jobSize(room.area(tx, ty));
		
		if (was) {
			jobs += secret.get(data);
		}
		
		if (jobsPlaced.get(data) >= jobs) {
			return false;
		}
		
		
		data = jobsPlaced.inc(data, 1);
		set(deg, data);

		return true;
	}
	
	private boolean locked(Room room, int tx, int ty) {
		if (!room.constructor().blue().reqs.passes(FACTIONS.player()))
			return true;
		if (room.upgrade(tx, ty) > 0 && !room.constructor().blue().upgrades().requires(room.upgrade(tx, ty)).passes(FACTIONS.player()))
			return true;
		return false;
	}
	
	public static int jobs(int data, int area) {
		return  secret.get(data) + (tot.get(data))/jobSize(area);
	}
	
	public static void initRoom(Room room, int rx, int ry) {
		ROOM_DEGRADER deg = room.degrader(rx, ry);
		if (deg == null)
			return;
		int data = deg.getData();
		data = jobsPlaced.set(data, 0);
		set(deg, data);
	}

	@Override
	public double degrade(int tx, int ty) {
		Room room = ROOMS().map.get(tx, ty);
		ROOM_DEGRADER deg = room.degrader(tx, ty);
		return deg.get();
	}

	private static void set(ROOM_DEGRADER deg, int data) {
		double d = degrade(data);
		boolean changed = false;
		if (d > 0.5) {
			if (extraBit.get(data) == 0) {
				data = extraBit.set(data, 1);
				changed = true;
			}
		}else if (d == 0) {
			if (extraBit.get(data) == 1) {
				data = extraBit.set(data, 0);
				changed = true;
			}
		}
		deg.setData(data, changed);
	}



	public static boolean degradeReal(int data) {
		return extraBit.get(data) == 1;
	}
	
	public static double secretDegrade(int data) {
		return (double)tot.get(data)/tot.mask;
	}



}