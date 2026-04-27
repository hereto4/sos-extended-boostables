package game.event.actions;

import game.event.engine.EContext;
import game.event.engine.Event;
import snake2d.util.file.Json;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LISTE;
import view.main.VIEW;

final class _EARTHQUAKE extends EventActionConstructor{


	
	
	_EARTHQUAKE() {
		super("EARTHQUAKE");
	}
	
	@Override
	public EventAction action(Data data) {
		return new Imp(key, data.json, data.all);
	}

	
	public final class Imp extends EventAction  {
		
		Imp(String key, Json data, LISTE<EventAction> all) {
			super(key, all);
			data.checkUnused();
		}
		

		@Override
		public void setContext(Event event, EContext data) {
			acc = 0;
			vtime = 0;
		}
		

		double acc;
		double vtime = 0;
		
		@Override
		public void update(Event event, EContext e, double ds, double second) {
			if (VIEW.s().isActive() && VIEW.renderSecond() > vtime) {
				vtime = VIEW.renderSecond()+RND.rFloat()*0.2;
				if (ds > 0)
					VIEW.s().getWindow().centerer.set(VIEW.s().getWindow().pixels().cX()+RND.rInt0(20), VIEW.s().getWindow().pixels().cY()+RND.rInt0(20));
			}
			
			
		}
		
	}




	
}
