package menu;

import snake2d.CORE;
import snake2d.KeyBoard.KeyEvent;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;

interface SC{
	
	
	public boolean hover(COORDINATE mCoo);
	public boolean click();
	public default void renderBackground(Background back, float ds, COORDINATE mCoo) {
		back.render(CORE.renderer(), ds);
	}
	public void render(SPRITE_RENDERER r, float ds);
	public boolean back(Menu menu);
	
	public default void poll(KeyEvent e) {
		
	}
}
