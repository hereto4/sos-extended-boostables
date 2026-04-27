package settlement.main;

import settlement.misc.util.TILE_STORAGE;
import settlement.room.main.Room;
import snake2d.util.map.MAP_OBJECT;

public final class SettMaps {


	
	SettMaps(){

	}
	
	public final MAP_OBJECT<TILE_STORAGE> STORAGE = new MAP_OBJECT<TILE_STORAGE>() {
		
		@Override
		public TILE_STORAGE get(int tx, int ty) {
			Room r = SETT.ROOMS().map.get(tx, ty);
			if (r != null)
				return r.storage(tx, ty);
			return null;
		}
		
		@Override
		public TILE_STORAGE get(int tile) {
			int x = tile%SETT.TWIDTH;
			int y = tile/SETT.TWIDTH;
			return get(x,y);
		}
	};
	

	
}
