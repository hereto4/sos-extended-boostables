package game.event.actions;

import game.GAME;
import game.event.engine.EContext;
import game.event.engine.Event;
import settlement.main.SETT;
import settlement.room.food.orchard.ROOM_ORCHARD;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomInstance;
import snake2d.util.file.Json;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LISTE;

final class _ORCHARD extends EventActionConstructor{

	
	
	_ORCHARD() {
		super("ORCHARD");
		
	}
	
	@Override
	public EventAction action(Data data) {
		return new Imp(key, data.json, data.all);
	}

	
	public final class Imp extends EventAction  {

		private final ROOM_ORCHARD room;
		private final double amount;
		
		Imp(String key, Json data, LISTE<EventAction> all) {
			super(key, all);
			RoomBlueprint b = SETT.ROOMS().collection.getWarn(data.value("ROOM"), data);
			
			if (b != null && b instanceof ROOM_ORCHARD)
				this.room = (ROOM_ORCHARD) b;
			else {
				this.room = null;
				GAME.Warn(data.errorGet("no orchard room named: " + data.value("ROOM"), "ROOM"));
			}
			amount = data.d("AREA_AFFECTED", 0, 1);
			data.checkUnused();
		}
		

		@Override
		public void exe(Event event, EContext data) {
			int area = (int) (this.amount*room.totalArea());
			double aa = 0;
			int r = RND.rInt()&Integer.MAX_VALUE;
			boolean first = true;
			

			for (int i = 0; i < room.instancesSize(); i++) {
				RoomInstance ro = room.getInstance((i+r)%room.instancesSize());
				if (first || ro.area() <= area) {
					room.event(ro.mX(), ro.mY(), 1.0);
					data.coo.set(ro.body().cX(), ro.body().cY());
					area -= ro.area();
					first = false;
					aa += ro.area();
				}
			}
			
			double per = aa / room.totalArea();
			data.actionAmount = per;
			
		}

	}




	
}
