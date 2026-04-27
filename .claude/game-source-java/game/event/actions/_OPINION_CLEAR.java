package game.event.actions;

import game.boosting.superb.SuperSpec.SuperSpecImp;
import game.event.engine.EChoice;
import game.event.engine.EContext;
import game.event.engine.Event;
import game.faction.FACTIONS;
import game.faction.npc.FactionNPC;
import game.faction.royalty.Royalty;
import game.faction.royalty.opinion.ROPINIONS;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.file.Json;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LISTE;
import util.gui.misc.GBox;

final class _OPINION_CLEAR extends EventActionConstructor{

	
	_OPINION_CLEAR() {
		super("OPINION_REMOVE");
	}
	
	@Override
	public EventAction action(Data data) {
		return new Imp(key, data.parent, data.choice, data.json, data.all);
	}
	
	public final class Imp extends EventAction  {
		
		private final String[] keys;

		Imp(String key, Event parent, EChoice choice, Json data, LISTE<EventAction> all) {
			super(key, all);
			
			keys = data.texts("EVENTS");
			data.checkUnused();
		}

		@Override
		public void exe(Event e, EContext data) {
		
			KeyMap<ArrayListGrower<SuperSpecImp<Royalty>>> map = new KeyMap<ArrayListGrower<SuperSpecImp<Royalty>>>();
			
			for (SuperSpecImp<Royalty> s : ROPINIONS.BOOST().imps()) {
				
				if (!map.containsKey(s.key))
					map.put(key, new ArrayListGrower<SuperSpecImp<Royalty>>());
				map.get(s.key).add(s);

			}
			
			for (String k : keys) {
				if (!map.containsKey(k))
					continue;
				for (SuperSpecImp<Royalty> s : map.get(k)) {
					for (FactionNPC reg : FACTIONS.NPCs()) {
						for (Royalty r : reg.court().all()) {
							s.activate(r, false);
						}
					}
				}
				
			}
			
			super.exe(e, data);
		}
		
		
		@Override
		public void hover(GBox b, Event event, EContext context) {
			
		}
		
		@Override
		public void addToMessageBody(LISTE<RENDEROBJ> rows, Event event, EContext context, RECTANGLE messBody) {
			
			
		}
	}




	
}
