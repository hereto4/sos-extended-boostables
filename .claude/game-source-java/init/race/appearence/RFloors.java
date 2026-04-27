package init.race.appearence;

import settlement.main.SETT;
import settlement.room.main.RoomBlueprint;
import settlement.tilemap.floor.Floors.Floor;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayListGrower;

public final class RFloors {

	private final LL[] override = new LL[SETT.ROOMS().AMOUNT_OF_BLUEPRINTS];
	
	RFloors(Json json){
		
		SETT.ROOMS().collection. new KJson("ROOM_FLOOR_OVERRIDE", json) {
			
			@Override
			protected void process(RoomBlueprint s, Json j, String key, boolean isWeak) {
				if (override[s.index()] == null)
					override[s.index()] = new LL();
				for (Floor f : SETT.FLOOR().map.readMany(key, j)){
					override[s.index()].add(f);
				}
			}
		};
		
	}
	
	public Floor get(RoomBlueprint b, int i, Floor backup) {
		LL li = override[b.index()];
		if (li == null)
			return backup;
		if (i >= li.size())
			return backup;
		return li.get(i);
		
	}
	
	private static class LL extends ArrayListGrower<Floor> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		
	}
	
}
