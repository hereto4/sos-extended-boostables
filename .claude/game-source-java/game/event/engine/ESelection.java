package game.event.engine;

import game.event.actions.EventAction;
import game.event.actions.EventActions;
import game.faction.Faction;
import game.faction.royalty.Royalty;
import init.value.GVALUES;
import init.value.GValueCat;
import init.value.Lockable;
import settlement.stats.Induvidual;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import world.map.regions.Region;

public final class ESelection {

	public LIST<EventAction> onFail = EActions.actions();
	public ESelectionType<Induvidual> indu = new ESelectionType<Induvidual>();
	public ESelectionType<Region> reg = new ESelectionType<Region>();
	public ESelectionType<Faction> faction = new ESelectionType<Faction>();
	public ESelectionType<Royalty> royalty = new ESelectionType<Royalty>();
	
	ESelection(Event e, EventActions act, Json data){
		if (data.has("SELECTION")) {
			data = data.json("SELECTION");
			onFail = EActions.actions("ON_FAIL", e, act, data);

			indu.read(data, "SUBJECTS", GVALUES.INDU);
			reg.read(data, "REGIONS", GVALUES.REGION);
			faction.read(data, "FACTIONS", GVALUES.FACTION);
			royalty.read(data, "ROYALTIES", GVALUES.ROYALTY);
			data.checkUnused();
		}
	}

	
	public static final class ESelectionType<T> {
		public ArrayListGrower<Lockable<T>> filters = new ArrayListGrower<Lockable<T>>();
		EAmount min;
		EAmount max;
		public boolean useAsIcon = false;
		public ESelectionMark mark = new ESelectionMark();
		
		public ESelectionType() {
			min = new EAmount(0);
			max = new EAmount(Integer.MAX_VALUE);
		}
		
		void read(Json data, String key, GValueCat<T> ll) {
			if (data.has(key)) {
				Json dd = data.json(key);
				min = am(dd, "MIN_AMOUNT", 0);
				max = am(dd, "MAX_AMOUNT", Integer.MAX_VALUE);
				if (dd.has("FILTERS")) {
					for (Json j : dd.jsons("FILTERS")) {
						Lockable<T> lock = ll.LOCK.push();
						lock.pushPush(j);
						filters.add(lock);
					}
				}
				useAsIcon = dd.bool("USE_AS_ICON", false);
				mark.read(dd);
				dd.checkUnused();
			}
		}
		
		private EAmount am(Json data, String key, int fallback) {
			
			if (data.has(key)) {
				Json json = data.json(key);
				return new EAmount(json, 0);
			}else
				return new EAmount(fallback);
			
		}

		
	}
	
	static final class ESelectionMark {
		
		public boolean mark = false;
		public String clear = null;
		public String filter = null;
		
		void read(Json json) {
			if (json.has("MARK")) {
				json = json.json("MARK");
				mark = json.bool("MARK_WITH_EVENT_KEY", false);
				clear = json.value("CLEAR_EVENT_KEY", null);
				filter = json.value("ALLOW_ONLY_MARK", null);
				json.checkUnused();
			}
		}
		
	}
	
}
