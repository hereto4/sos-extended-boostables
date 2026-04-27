package game.event.actions;

import game.event.engine.EContext;
import game.event.engine.Event;
import init.type.DISEASE;
import init.type.DISEASES;
import settlement.stats.STATS;
import snake2d.util.file.Json;
import snake2d.util.sets.LISTE;

final class _OUTBREAK extends EventActionConstructor{
	
	
	_OUTBREAK() {
		super("OUTBREAK");
		
	}
	
	@Override
	public EventAction action(Data data) {
		return new Imp(key, data.json, data.all);
	}
	
	public final class Imp extends EventAction  {

		private final double amount;
		public final DISEASE disease;
		private CInt am = new CInt("AFFLICTED");
		
		Imp(String key, Json data, LISTE<EventAction> all) {
			super(key, all);
			amount = data.d("AMOUNT", 0, 1);
			disease = DISEASES.map().readTry(data);
			data.checkUnused();
			
		}

		@Override
		public void exe(Event event, EContext data) {
			if (disease != null) {
				int am = STATS.DISEASE().incubating().data().get(null) + STATS.DISEASE().sick().data().get(null);
				STATS.DISEASE().outbreak(amount, disease);
				am = STATS.DISEASE().incubating().data().get(null) + STATS.DISEASE().sick().data().get(null)-am;
				this.am.set(event, data, am);
			}
		}
		
	}
	
}
