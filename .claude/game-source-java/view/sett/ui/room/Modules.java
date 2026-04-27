package view.sett.ui.room;

import settlement.room.main.RoomBlueprint;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LISTE;

final class Modules {

	private final ModuleMaker[] makers;
	
	Modules(Init init){
		
		makers = new ModuleMaker[] {
			
			new ModuleInstance(init),
			new ModuleEmployment(init),
			new ModuleRadius(init),
			new ModuleDegrade(init),
			new ModuleUpgradable(init),
			new ModulePumpable(init),
			new ModuleIrrigated(init),
			
			new ModuleService(init),
			new ModuleIndustry(init),
			new ModuleGrave(init),
			new ModuleConstructor(init),
		};
		
	}
	
	UIRoomModule[] get(RoomBlueprint p) {
		ArrayList<UIRoomModule> apps = new ArrayList<UIRoomModule>(32);
		
		for (ModuleMaker m : makers) {
			m.make(p, apps);
		}
		
		p.appendView(apps);
		UIRoomModule[] as = new UIRoomModule[apps.size()];
		for (int i = 0; i < apps.size(); i++)
			as[i] = apps.get(i);
		
		
		return as;
		
	}
	
	interface ModuleMaker {
		
		void make(RoomBlueprint p, LISTE<UIRoomModule> l);
		
	}
	
	
}
