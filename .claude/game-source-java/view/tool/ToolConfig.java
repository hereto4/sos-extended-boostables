package view.tool;

import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;

public interface ToolConfig {

	public default void addUI(LISTE<RENDEROBJ> uis){
		
	}
	
	public default void activateAction() {
		
	}
	
	public default void deactivateAction() {
		
	}
	
	public default boolean back() {
		return true;
	}
	
	public default void update(boolean UIHovered) {
		
	}
	
}
