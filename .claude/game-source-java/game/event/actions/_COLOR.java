package game.event.actions;

import game.event.engine.EContext;
import game.event.engine.Event;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sets.LISTE;

final class _COLOR extends EventActionConstructor{
	
	_COLOR() {
		super("COLOR");
	}
	
	@Override
	public EventAction action(Data data) {
		return new Imp(key, data.json, data.all);
	}
	
	public final class Imp extends EventAction  {

		public final ColorImp color;
		private final boolean useSelection;
		
		Imp(String key, Json data, LISTE<EventAction> all) {
			super(key, all);
			color = new ColorImp(data);
			useSelection = data.bool("USE_SELECTION", false);
			data.checkUnused();
		}
		

		@Override
		public void exe(Event event, EContext data) {
			if (useSelection)
				data.colorIndu = color;
			else	
				data.colorinduAll = color;
		}
		
	}

	
}
