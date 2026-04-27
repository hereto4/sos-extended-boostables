package game.event.actions;

import game.event.engine.EChoice;
import game.event.engine.Event;
import game.event.engine.EventCollection;
import snake2d.util.file.Json;
import snake2d.util.sets.LISTE;

abstract class EventActionConstructor {

	public final String key;
	
	EventActionConstructor(String key){
		this.key = key;
	}
	
	public abstract EventAction action(Data data);

	static class Data {
		Json json;
		Event parent;
		EChoice choice;
		LISTE<EventAction> all;
		EventCollection engine;
	}
}
