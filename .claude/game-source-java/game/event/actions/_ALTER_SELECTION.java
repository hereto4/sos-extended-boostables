package game.event.actions;

import game.event.engine.EContext;
import game.event.engine.Event;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.file.Json;
import snake2d.util.sets.LISTE;

final class _ALTER_SELECTION extends EventActionConstructor{
	
	_ALTER_SELECTION() {
		super("ALTER_SELECTION");
	}
	
	@Override
	public EventAction action(Data data) {
		return new Imp(key, data.json, data.all);
	}
	
	public final class Imp extends EventAction  {

		private final String name;
		private final int gender;
		private double age;
		private double dirtiness;
		
		Imp(String key, Json data, LISTE<EventAction> all) {
			super(key, all);
			name = data.text("NAME", null);
			gender = data.i("GENDER", 0, Integer.MAX_VALUE, -1);
			dirtiness = data.dTry("DIRTINESS", 0, 1, -1);
			data.checkUnused();
		}


		
		@Override
		public void exe(Event event, EContext data) {
			

			ENTITY[] ee = SETT.ENTITIES().getAllEnts();
			
			for (int ie = 0; ie < ee.length; ie++) {
				ENTITY e = ee[ie];
				if (!(e instanceof Humanoid))
					continue;
				
				Humanoid a = (Humanoid) e;
				if (STATS.EVENT().has(a.indu())) {
					Induvidual indu = a.indu();
					if (name != null)
						STATS.APPEARANCE().customName(indu).clear().add("Hotam Greattusk");
					if (gender >= 0)
					STATS.APPEARANCE().gender.set(indu, Math.min(gender, STATS.APPEARANCE().gender.max(indu)));
					if (age >= 0)
						STATS.POP().age.dage.setD(indu, age);
					if (dirtiness >= 0)
						STATS.NEEDS().DIRTINESS.setD(indu, dirtiness);
				}
			}
			
			
		}
		
		
		
		
	}

	
}
