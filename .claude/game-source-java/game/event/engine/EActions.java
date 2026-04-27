package game.event.engine;

import game.event.actions.EventAction;
import game.event.actions.EventActions;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

final class EActions {

	static LIST<EventAction> actions(){
		return new ArrayList<EventAction>(0);
	
	}
	
	static LIST<EventAction> actions(Event e, EventActions act, Json data){
		return get(e, null, act, data, true);
	
	}
	
	static LIST<EventAction> actions(Event e, EChoice c, EventActions act, Json data){
		return get(e, c, act, data, true);
	
	}
	
	static LIST<EventAction> actions(String key, Event e, EventActions act, Json data){
		return actions(key, e, null, act, data, true);
	}
	
	static LIST<EventAction> actions(String key, Event e, EChoice choice, EventActions act, Json data, boolean allowOther){
		if (!data.has(key)) {
			return new ArrayList<EventAction>(0);	
		}else {
			data = data.json(key);
			return get(e, choice, act, data, allowOther);
		}
		
	}
	
	private static LIST<EventAction> get(Event e, EChoice choice, EventActions act, Json data, boolean allow){
		if (data.has("ACTIONS"))
			return act.get(data.jsons("ACTIONS"), e, choice, e.allActions, allow);
		else
			return new ArrayList<EventAction>(0);
	}
	
}
