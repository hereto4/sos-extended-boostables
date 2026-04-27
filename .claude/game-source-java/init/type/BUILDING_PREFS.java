package init.type;

import init.paths.PATHS;
import init.structure.STRUCTURES;
import init.structure.Structure;
import settlement.main.SETT;
import settlement.tilemap.terrain.TBuilding;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.keymap.RMAP;

public final class BUILDING_PREFS {

	private static BUILDING_PREFS self;

	
	private final BUILDING_PREF MOUNTAIN;
	private final BUILDING_PREF OUTDOORS;
	private final LIST<BUILDING_PREF> BUILDING;
	private final LIST<BUILDING_PREF> ALL;
	private final RMAP<BUILDING_PREF> map;
	
	BUILDING_PREFS() {
		self = this;
		LinkedList<BUILDING_PREF> all = new LinkedList<>();
		MOUNTAIN = new BUILDING_PREF("_MOUNTAIN", all) {
			@Override
			public SPRITE icon() {
				return SETT.TERRAIN().MOUNTAIN.getIcon();
			};
		};
		OUTDOORS = new BUILDING_PREF("_OUTDOORS", all) {
			@Override
			public SPRITE icon() {
				return CLIMATES.ALL().get(CLIMATES.ALL().size()/2).icon;
			};
		};
		LinkedList<String> keys = new LinkedList<>();
		keys.add("_MUD");
		keys.add(PATHS.INIT_SETTLEMENT().getFolder("structure").getFiles());
		ArrayList<BUILDING_PREF> buildings = new ArrayList<BUILDING_PREF>(keys.size());
		int in = 0;
		for (String k : keys) {
			final int ind = in++;
			buildings.add(new BUILDING_PREF(k, all) {
				
				@Override
				public SPRITE icon() {
					return SETT.TERRAIN().BUILDINGS.get(STRUCTURES.all().get(ind)).iconCombo;
				};
				
			});
		}
		this.BUILDING = buildings;
		this.ALL = new ArrayList<BUILDING_PREF>(all);
		
		map = new RMAP<BUILDING_PREF>("STRUCTURE", ALL);
		
		
	}
	
	public static BUILDING_PREF get(int tx, int ty) {
		if (SETT.TERRAIN().MOUNTAIN.isMountain(tx, ty))
			return self.MOUNTAIN;
		if (SETT.TERRAIN().get(tx, ty) instanceof TBuilding.BuildingComponent) {
			return self.BUILDING.get(((TBuilding.BuildingComponent)SETT.TERRAIN().get(tx, ty)).building().structure.index());
		}
		return self.OUTDOORS;
	}
	
	public static BUILDING_PREF get(Structure building) {
		return self.BUILDING.get(building.index());
	}
	
	public static RMAP<BUILDING_PREF> MAP(){
		return self.map;
	}
	
	public static LIST<BUILDING_PREF> ALL(){
		return self.ALL;
	}
	
}
