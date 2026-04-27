package game.event.actions;

import game.GAME;
import game.event.engine.EChoice;
import game.event.engine.Event;
import game.event.engine.EventCollection;
import snake2d.LOG;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;

public final class EventActions {

	private final KeyMap<EventActionConstructor> map = new KeyMap<EventActionConstructor>();
	private final EventCollection coll;
	public final _BOOST boosts;

	
	public EventActions(EventCollection coll) {
		
		register(new _PASTURE());
		register(new _ORCHARD());
		register(new _PARDON());
		register(new _INVASION());
		register(new _DESTRUCTION());
		register(new _OUTBREAK());
		register(new _RESOURCES());
		register(new _CREDITS());
		register(new _EARTHQUAKE());
		register(new _SUBJECTS_ADD());
		register(new _SUBJECTS_KILL());
		register(new _SOUND_AMBIENT());
		register(new _ALTER_SELECTION());
		register(new _COLOR());
		boosts = new _BOOST();
		register(boosts);
		register(new _EVENT());
		register(new _WEATHER());
		register(new _BOOST_PERM());
		register(new _BOOST_PERM_REMOVE());
		register(new _OPINION());
		register(new _OPINION_CLEAR());
		register(new _REGION_POP());
		this.coll = coll;
	}
	
	private void register(EventActionConstructor event) {
		map.put(event.key, event);
	}
	
	private static boolean hasWarned = false;
	
	public LIST<EventAction> get(Json[] jsons, Event parent, EChoice choice, LISTE<EventAction> all, boolean allow) {
		if (jsons == null)
			return new ArrayList<>(0);
		ArrayList<EventAction> res = new ArrayList<>(jsons.length);
		
		EventActionConstructor.Data data = new EventActionConstructor.Data();
		data.all = all;
		data.engine = coll;
		data.choice = choice;
		data.parent = parent;

		
		for (Json j : jsons) {
			String t = j.value("TYPE");
			if (!map.containsKey(t)) {
				String s = "There is no Action Type named " + t;
				if (!hasWarned){
					hasWarned = true;
					s += System.lineSeparator();
					s += "Available:";
					s += System.lineSeparator();
					s += map.keysString();
					GAME.Warn(j.errorGet(s, "TYPE"));
				}else
				LOG.err(j.errorGet(s, "TYPE"));
			}else {
				if (!allow && t.equals("EVENT"))
					j.error("spawning an event here is not allowed", t);
				
				boolean hideUI = j.bool("HIDE_UI", false);
				
				data.json = j;
				
				EventAction a = map.get(t).action(data);
				a.hideUI = hideUI;
				res.add(a);
			}
		}
		
		return res;
	}
	
	public void init() {
		_BOOST.init(coll);
		EventActionContext.check(coll.all);
	}
	

	
}
