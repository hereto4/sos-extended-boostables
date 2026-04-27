package view.tool;

import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;

public interface PLACABLE {

	static CharSequence E = "";

	/**
	 * 
	 * @return get an small image representing this tile
	 */
	public abstract SPRITE getIcon();

	/**
	 * 
	 * @return the name of the tile
	 */
	public abstract CharSequence name();
	
	public PLACABLE getUndo();
	public default LIST<CLICKABLE> getAdditionalButt(){
		return null;
	}

	
	public default void hoverDesc(GBox box) {
		box.text(name());
	}

}


