package game.event.actions;

import game.audio.AUDIO;
import game.event.engine.EContext;
import game.event.engine.Event;
import snake2d.SoundStream;
import snake2d.util.file.Json;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import view.main.VIEW;

final class _SOUND_AMBIENT extends EventActionConstructor{
	
	
	
	_SOUND_AMBIENT() {
		super("SOUND_AMBIENT");
		
	}
	
	@Override
	public EventAction action(Data data) {
		return new Imp(key, data.json, data.all);
	}
	
	public final class Imp extends EventAction  {

		private final LIST<SoundStream> stream;
		private final boolean city;
		private final boolean world;
		
		Imp(String key, Json data, LISTE<EventAction> all) {
			super(key, all);
			stream = AUDIO.AMBI().factory.read(data);
			city = data.bool("CITY", true);
			world = data.bool("WORLD", false);
			data.checkUnused();
			
		}

		@Override
		public void update(Event event, EContext e, double ds, double second) {
			
			if (!city && VIEW.s().isActive() || VIEW.s().battle.isActive())
				return;
			
			if (!world && VIEW.world().isActive())
				return;
			
			for (SoundStream s : stream)
				s.play();
			
			super.update(event, e, ds, second);
		}
		
	}
	
}
