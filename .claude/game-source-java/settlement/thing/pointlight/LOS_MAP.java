package settlement.thing.pointlight;

import settlement.main.SETT;
import settlement.room.main.RoomBlueprint;
import snake2d.util.map.MAP_OBJECT;

public final class LOS_MAP implements MAP_OBJECT<LOS>{

	LOS_MAP() {
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public LOS get(int tile) {
		return get(tile%SETT.TWIDTH, tile/SETT.TWIDTH);
	}

	@Override
	public LOS get(int tx, int ty) {
		RoomBlueprint p = SETT.ROOMS().map.blueprint.get(tx, ty);
		if (p != null) {
			return p.LOS(tx, ty);
		}
		
		return SETT.TILE_MAP().LOS(tx, ty);
	}
	
}
