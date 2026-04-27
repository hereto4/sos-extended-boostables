package view.sett.ui.room;

import settlement.room.main.RoomInstance;
import util.gui.misc.GBox;

public abstract class UIRoomBulkApplier {
	
	protected final CharSequence name;
	
	public UIRoomBulkApplier(CharSequence name) {
		this.name = name;
	}
	
	protected abstract void apply(RoomInstance t);
	
	protected void hover(GBox b) {
		
	}
	
}