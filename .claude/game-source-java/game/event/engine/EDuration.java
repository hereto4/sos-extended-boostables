package game.event.engine;

import game.event.actions.EventAction;
import game.event.actions.EventActions;
import game.time.TIME;
import snake2d.util.file.Json;
import snake2d.util.sets.LIST;

public class EDuration {

	public final double seconds;
	public final LIST<EventAction> on_expire;
	
	EDuration(Json data, EventActions actions, Event parent){
		
		if (data.has("DURATION")) {
			data = data.json("DURATION");
			seconds = data.dTry("DAYS", 0, 1000000, 1)*TIME.secondsPerDay();
			on_expire = EActions.actions(parent, actions, data);
			data.checkUnused();
		}else {
			seconds = 0;
			on_expire = EActions.actions();
		}
		
		
		
	}
	
}
