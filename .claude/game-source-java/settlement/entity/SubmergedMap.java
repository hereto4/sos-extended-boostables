package settlement.entity;

import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.room.service.hygine.bath.ROOM_BATH;
import settlement.room.water.pool.ROOM_POOL;
import snake2d.util.map.MAP_BOOLEAN;

public class SubmergedMap implements MAP_BOOLEAN{

	
	
	@Override
	public boolean is(int tx, int ty) {
		
		if (ROOM_BATH.isPool(tx, ty))
			return true;

		if (SETT.TERRAIN().WATER.ice.is(tx, ty))
			return false;
		if (SETT.TERRAIN().WATER.open.is(tx, ty))
			return true;
		Room r = SETT.ROOMS().map.get(tx, ty);
		if (r != null && r.blueprint() instanceof ROOM_POOL)
			return true;
		return false;
	}
	
	@Override
	public boolean is(int tile) {
		throw new RuntimeException();
	}

}
