package game.event.engine;

import game.event.actions.EventAction;
import game.event.actions.EventActions;
import game.faction.Faction;
import init.value.GVALUES;
import init.value.Lockable;
import snake2d.util.file.Json;
import snake2d.util.sets.LIST;

public class EChoice {

	public final CharSequence name;
	public final Lockable<Faction> request = GVALUES.FACTION.LOCK.push();
	public final int index;
	public final LIST<EventAction> actions;
	
	EChoice(Event e, int index, EventActions act, Json data, CharSequence name){
		
		this.name = name;
		request.push("REQUIRES", data);
		this.index = index;
		actions = EActions.actions(e, this, act, data);
		data.checkUnused();
	}
	
}
