package game.event.actions;

import game.GAME;
import game.event.engine.EContext;
import game.event.engine.Event;
import settlement.main.SETT;
import settlement.room.food.pasture.PastureInstance;
import settlement.room.food.pasture.ROOM_PASTURE;
import settlement.room.main.RoomBlueprint;
import snake2d.util.file.Json;
import snake2d.util.sets.LISTE;

final class _PASTURE extends EventActionConstructor{

	_PASTURE() {
		super("PASTURE");
		
	}
	
	@Override
	public EventAction action(Data data) {
		return new Imp(key, data.json, data.all);
	}

	
	public final class Imp extends EventAction  {

		private final ROOM_PASTURE room;
		private final double amount;
		
		Imp(String key, Json data, LISTE<EventAction> all) {
			super(key, all);
			RoomBlueprint b = SETT.ROOMS().collection.getWarn(data.value("ROOM"), data);
			
			if (b != null && b instanceof ROOM_PASTURE)
				this.room = (ROOM_PASTURE) b;
			else {
				this.room = null;
				GAME.Warn(data.errorGet("no pasture named: " + data.value("ROOM"), "ROOM"));
			}
			amount = data.d("ANIMALS_KILLED", 0, 1);
			data.checkUnused();
		}
		

		@Override
		public void exe(Event event, EContext data) {
			if (room == null)
				return;
			
			ROOM_PASTURE p = room;
			
			if (p.instancesSize() == 0)
				return;
			
			double death = amount;
			
			double am = 0;
			int tot = 0;
			
			for (int i = 0; i < p.instancesSize(); i++) {
				PastureInstance ins = p.getInstance(i);
				data.coo.set(ins.body().cX(), ins.body().cY());
				tot += ins.animalsCurrent();
				int d = (int) Math.ceil(Math.ceil(ins.animalsCurrent()*death)); 
				ins.kill(d);
				am += d;

			}
			
			double per = am / tot;
			data.actionAmount = per;
			
		}

	}




	
}
