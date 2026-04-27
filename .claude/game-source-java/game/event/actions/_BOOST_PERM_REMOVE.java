package game.event.actions;

import game.GAME;
import game.boosting.tmp.TmpBoostSpec;
import game.event.engine.EContext;
import game.event.engine.Event;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.type.POP_CL;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LISTE;
import util.gui.misc.GBox;
import world.WORLD;
import world.map.regions.Region;

final class _BOOST_PERM_REMOVE extends EventActionConstructor{

	
	_BOOST_PERM_REMOVE() {
		super("BOOST_PERM_REMOVE");
	}
	
	@Override
	public EventAction action(Data data) {
		return new Imp(key, data);
	}
	
	public final class Imp extends EventAction  {
		
		private final String[] keys;

		Imp(String key, Data data) {
			super(key, data.all);
			
			if (data.json.has("EVENTS")) {
				keys = data.json.values("EVENTS");
				
				for (int i = 0; i < keys.length; i++) {
					String k = keys[i];
					Event e = data.engine.read(data.parent, k, data.json, "EVENTS");
					if (e != null) {
						k = "EVENT_" + e.key;
					}
					keys[i] = k;
				}
			}else {
				keys = null;
			}
			
			
			
			data.json.checkUnused();
		}

		@Override
		public void exe(Event e, EContext data) {
		
			if (keys != null) {
				KeyMap<ArrayListGrower<TmpBoostSpec>> map = new KeyMap<ArrayListGrower<TmpBoostSpec>>();
				
				for (TmpBoostSpec s : GAME.BOOST().specs()) {
					
					if (!map.containsKey(s.key))
						map.put(s.key, new ArrayListGrower<TmpBoostSpec>());
					
					map.get(s.key).add(s);
					
				}
				
				for (String k : keys) {
					if (!map.containsKey(k))
						continue;
					for (TmpBoostSpec s : map.get(k)) {
						for (Region reg : WORLD.REGIONS().all()) {
							GAME.BOOST().regions.set(reg, s, false);
						}
						for (POP_CL cl : POP_CL.ALL()) {
							GAME.BOOST().popcl.set(cl, s, false);
						}
						for (Faction reg : FACTIONS.all()) {
							GAME.BOOST().factions.set(reg, s, false);
						}
					}
					
				}
			}else {
				for (Region reg : WORLD.REGIONS().all()) {
					GAME.BOOST().regions.clear(reg);
				}
				for (POP_CL cl : POP_CL.ALL()) {
					GAME.BOOST().popcl.clear(cl);
				}
				for (Faction reg : FACTIONS.all()) {
					GAME.BOOST().factions.clear(reg);
				}
			}
			
			
			
			super.exe(e, data);
		}
		
		
		@Override
		public void hover(GBox b, Event event, EContext context) {
			
		}
		
		@Override
		public void addToMessageBody(LISTE<RENDEROBJ> rows, Event event, EContext data, RECTANGLE messBody) {

			
		}
	}




	
}
