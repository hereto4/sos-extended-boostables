package view.sett;

import util.gui.misc.GBox;

public interface SETT_HOVERABLE {
	
	public void hover(GBox text);
	public default boolean canBeClicked() {
		return false;
	}
	public default void click() {
		
	}
	
}