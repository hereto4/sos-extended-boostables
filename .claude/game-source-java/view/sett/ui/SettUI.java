package view.sett.ui;

import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomInstance;
import view.interrupter.InterManager;
import view.sett.ui.bottom.UIBuildPanel;
import view.sett.ui.room.construction.UIRoomPlacer;
import view.sett.ui.room.copy.UICopier;
import view.sett.ui.room.prints.UISavedPrints;

public final class SettUI {

	public final UIRoomPlacer placer = new UIRoomPlacer();
	public final UISavedPrints prints = new UISavedPrints();
	public final UICopier copier = new UICopier();
	public final UIBuildPanel bottom; 
	
	public SettUI(InterManager m){
		
		bottom = new UIBuildPanel(placer, m);
	}
	
	public void reconstruct(RoomInstance r) {
		placer.init(r);
	}
	
	public void reconstruct(int tx, int ty) {
		placer.init(tx, ty);
	}
	
	public void reconstruct(RoomBlueprintImp b) {
		placer.init(b, -1,-1);
	}
	
}
