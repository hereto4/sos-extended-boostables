package settlement.path;

import static settlement.main.SETT.FLOOR;
import static settlement.main.SETT.ROOMS;
import static settlement.main.SETT.TAREA;
import static settlement.main.SETT.TERRAIN;

import settlement.main.SETT;
import settlement.path.components.SCOMPONENTS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.map.MAP_OBJECTE;

public final class AvailabilityMap implements MAP_OBJECTE<AVAILABILITY>{

	private final byte[] costs = new byte[TAREA];
	int state = 0;
	private final SCOMPONENTS comps;
	
	public AvailabilityMap(SCOMPONENTS comps) {
		this.comps = comps;
		for (int i = SETT.TWIDTH*SETT.TWIDTH-1; i >= 0; i--) {
			costs[i] = (byte) AVAILABILITY.NORMAL.ordinal();
		}
	}

	@Override
	public AVAILABILITY get(int tile) {
		return AVAILABILITY.values[costs[tile]];
	}

	@Override
	public AVAILABILITY get(int tx, int ty) {
		if (!SETT.IN_BOUNDS(tx, ty))
			return null;
		return get(tx+ty*SETT.TWIDTH);
	}

	@Override
	public void set(int tile, AVAILABILITY c) {
		AVAILABILITY old = get(tile);
		costs[tile] = (byte) c.ordinal();
		if (old.player == c.player) {
			int x = tile%SETT.TWIDTH;
			int y = tile/SETT.THEIGHT;
			if (SETT.MAINTENANCE().reservable.is(tile)) {
				
				comps.updateAvailability(x, y);
			}
			return;
		}
		
		state ++;
		int x = tile%SETT.TWIDTH;
		int y = tile/SETT.THEIGHT;
		comps.updateAvailability(x, y);
		boolean change = old.player*c.player < 0;
		AvailabilityListener.notify(x, y, c, old, change);
	}

	@Override
	public void set(int tx, int ty, AVAILABILITY object) {
		if (SETT.IN_BOUNDS(tx, ty))
			set(tx+ty*SETT.TWIDTH, object);
	}
	
	void init() {
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			costs[c.x()+c.y()*SETT.TWIDTH] = (byte) pget(c.x(), c.y()).ordinal();
		}
	}
	
	private AVAILABILITY pget(int tx, int ty) {
		AVAILABILITY a;
		
		a = TERRAIN().get(tx, ty).getAvailability(tx, ty);
		if (a != null) {
			return a;
		}
		a = ROOMS().getAvailability(tx, ty);
		if (a != null) {
			return a;
		}
		a = FLOOR().getAvailability(tx, ty);
		if (a != null) {
			return a;
		}
		return AVAILABILITY.NORMAL;
	}
	
	public void updateAvailability(int tx, int ty) {
		set(tx, ty, pget(tx, ty));
		
	}
	
	public int state() {
		return state;
	}

	public void updateService(int x, int y) {
		comps.updateService(x, y);
		
	}

	
}
