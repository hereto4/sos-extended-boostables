package snake2d;

import snake2d.KeyBoard.KeyEvent;
import snake2d.util.sets.LIST;


public abstract class CORE_STATE{

	public interface Constructor {

		public CORE_STATE getState();
		
		public default void doAfterSet() {
			
		}
		
	}

	/**
	 * updates the game!
	 * @param ds
	 */
	protected abstract void update(float ds, double slowTheFuckDown);

	/**
	 * 
	 * @param key
	 * @param scancode
	 * @param action Keyboard.RELEASE|PRESS|REPEAT
	 */
	protected abstract void keyPush(LIST<KeyEvent> keys, boolean hasCleared);
	
	/**
	 * this character has been pressed
	 * @param c
	 */
//	protected abstract void charPush(char c);
	/**
	 * A mouse Button has been pressed!
	 * @param button
	 */
	protected abstract void mouseClick(MButt button);
	
	protected abstract void render(Renderer r, float ds);

	protected void exit() {
		
	}

}
