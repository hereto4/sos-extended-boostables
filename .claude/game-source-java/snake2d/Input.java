package snake2d;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;

import snake2d.util.sprite.text.Str;

public class Input extends CORE_RESOURCE{

	private final Mouse mouse;
	private final KeyBoard keyboard;
	static int inputs;
	static long nanoNow;
	
	private boolean hasCleared;
	
	Input(GraphicContext window, SETTINGS sett){
		keyboard = new KeyBoard(window);
		mouse = new Mouse(window.getWindow());
		mouse.applySettings(sett);
		mouse.update();
		inputs = 0;
	}
	
	public Mouse getMouse(){
		return mouse;
	}
	
	public KeyBoard getKeyboard(){
		return keyboard;
	}
	
	public void clearAllInput(){
		hasCleared = true;
		CORE.getInput().keyboard.listener = null;
		mouse.clear();
		keyboard.clear();
		inputs = 0;
	}
	
	void poll(long nanoNow, boolean focused){
		MButt.wheelDy = 0;

		glfwPollEvents();
		if (!focused)
			clearAllInput();
		Input.nanoNow = nanoNow;
	}
	
	void poll (CORE_STATE current){
		

		mouse.poll(current);
		keyboard.poll(current, hasCleared);
		hasCleared = false;
	}
	
	@Override
	public void dis() {
//    	keyboard.release();
//    	mouse.release();
	}
	
	public static abstract class CHAR_LISTENER {
		
		private final Str text;
		
		public CHAR_LISTENER(int size) {
			text = new Str(size);
		}
		
		protected void acceptChar(char c) {
			if (text().spaceLeft() > 0 && listening()) {
				text().add(c);
				change();
			}
		}

		protected void enter() {
			
		}

		protected void backspace() {
			if (text().length() > 0 && listening()) {
				text().clearLast();
				change();
			}
		}
		

		public void del() {
			text().clear();
			change();
		}
		
		public void set(CharSequence name) {
			text().clear();
			text().add(name);
			change();
		}
		
		protected abstract void change();
		
		public Str text() {
			return text;
		}
		
		public void listen() {
			CORE.getInput().keyboard.listener = this;
		}
		
		public boolean listening() {
			return CORE.getInput().keyboard.listener == this;
		}

		public void left(boolean mod) {
			// TODO Auto-generated method stub
			
		}

		public void right(boolean mod) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
}
