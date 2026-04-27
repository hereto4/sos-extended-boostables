package settlement.room.health.hospital;

import init.resources.RESOURCE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import util.data.GETTER;
import util.gui.misc.GButt;
import util.gui.misc.GGrid;
import util.text.D;
import view.sett.ui.room.UIRoomModule.UIRoomModuleImp;

class Gui extends UIRoomModuleImp<HospitalInstance, ROOM_HOSPITAL> {

	private static CharSequence ¤¤nn = "Fetch:";
	private static CharSequence ¤¤hov = "Not allowing this resource yields a recovery rate of 20% Allowing your nurses to use this resource increases recovery-rate to 50%";
	
	static {
		D.ts(Gui.class);
	}
	
	Gui(ROOM_HOSPITAL s) {
		super(s);
	}
	


	@Override
	protected void appendPanel(GuiSection section, GGrid grid, GETTER<HospitalInstance> getter, int x1, int y1) {
		
		RESOURCE res = blueprint.indus.get(0).ins().get(1).resource;
		
		GButt.ButtPanel b = new GButt.ButtPanel(res.icon()) {
			
			@Override
			protected void renAction() {
				selectedSet(getter.get().fetchOpium);
			}
			
			@Override
			protected void clickA() {
				getter.get().fetchOpium = !getter.get().fetchOpium;
			}
			
		};
		b.hoverTitleSet("" + ¤¤nn + " " + res.names);
		b.hoverInfoSet(¤¤hov);
		b.pad(16, 4);
		
		section.addRelBody(8, DIR.S, b);
		
		
	}

}
