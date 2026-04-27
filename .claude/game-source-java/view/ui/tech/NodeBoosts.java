package view.ui.tech;

import game.boosting.Boostable;
import settlement.main.SETT;
import settlement.room.industry.module.Industry;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.employment.RoomEmployment;
import settlement.room.main.employment.RoomEquip;
import snake2d.util.sets.KeyMap;

public class NodeBoosts {

	public final KeyMap<upEntry> upgradeBoost = new KeyMap<upEntry>();
	public final KeyMap<tEntry> tools = new KeyMap<tEntry>();
	NodeBoosts() {

		for (Industry ins : SETT.ROOMS().industries.all) {
			for (int i = 1; i <= ins.blue.upgrades().max(); i++) {
				String k = ins.blue.upgrades().reqs.get(i-1).key;
				double v = ins.blue.upgrades().boost(i)-ins.blue.upgrades().boost(i-1);
				if (!upgradeBoost.containsKey(k)) {
					upEntry e = new upEntry();
					e.blue = ins.blue;
					e.bo = ins.bonus();
					e.value = v;
					upgradeBoost.put(k, e);
				}
			}
			
		}
		
		for (RoomEmployment emp : SETT.ROOMS().employment.ALL()) {
			for (RoomEquip t : emp.tools()) {
				tEntry e = new tEntry();
				e.blue = emp.blueprint();
				e.value = t;
				e.bo = e.blue.bonus();
				if (!tools.containsKey(t.target(emp).boost().key))
					tools.put(t.target(emp).boost().key, e);
				
			}
		}
		
	}
	
	public static class upEntry {
		
		public double value;
		public RoomBlueprintImp blue;
		public Boostable bo;
		
	}
	
	public static class tEntry {
		
		public RoomEquip value;
		public RoomBlueprintImp blue;
		public Boostable bo;
		
	}
	
}
