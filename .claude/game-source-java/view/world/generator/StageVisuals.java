package view.world.generator;

import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import util.gui.misc.GButt;
import util.text.D;
import util.text.Dic;
import view.ui.profile.UIProfile;

class StageVisuals extends GuiSection{

	static CharSequence ¤¤title = "Customize Faction";

	static {
		D.ts(StageVisuals.class);
	}
	
	StageVisuals(WorldViewGenerator stages){
	
		stages.reset();
		
		addRelBody(16, DIR.N, UIProfile.section(false));
		
		int p = 650-body().width();
		if (p > 0)
			pad(p/2, 0);
		
		addRelBody(16, DIR.S, new GButt.ButtPanel(Dic.¤¤confirm) {
			@Override
			protected void clickA() {
				stages.hasProfiled = true;
				stages.set();
			}
			
		}.hoverInfoSet(Dic.¤¤confirm));
		
		pad(0, 8);
		
		stages.dummy.add(this, ¤¤title);
		
	}
	
	
	
}
