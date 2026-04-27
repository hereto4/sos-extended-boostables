package view.battle.editor;

import snake2d.util.gui.GuiSection;
import util.data.GETTER.GETTER_IMP;
import util.data.INT.INTE;
import util.gui.slider.GTarget;
import world.army.AD;
import world.army.ADSupplies;
import world.army.ADSupplies.ADArtillery;

public class ArmyArtillery extends GuiSection{

	
	
	
	ArmyArtillery(GETTER_IMP<ArmySide> current){
		
		for (ADArtillery a : AD.supplies().arts()) {
			
			INTE ii = new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return ADSupplies.artilleryMax;
				}
				
				@Override
				public int get() {
					return current.get().artillery[a.index()];
				}
				
				@Override
				public void set(int t) {
					current.get().artillery[a.index()] = t;
				}
			};
			
			GuiSection s = new GuiSection();
			s.hoverInfoSet(a.art.info.names);
			add(a.art.icon, 0, 0);
			addRightC(8, new GTarget(48, true, true, ii));
			
			
		}
		
	}
	
}
