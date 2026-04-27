package view.keyboard;

import snake2d.KeyBoard.KeyEvent;
import snake2d.util.sets.LIST;

public interface KeyPoller {
	
	public void poll(LIST<KeyEvent> keys);
	
}