package snake2d.util.gui;

import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Text;

public interface GUI_BOX {

	public GUI_BOX title(CharSequence title);
	
	public GUI_BOX NL();
	
	public GUI_BOX NL(int m);
	
	public GUI_BOX space();
	
	public Text text();
	
	public default GUI_BOX text(CharSequence text) {
		return add(text().set(text));
	}
	
	public default GUI_BOX text(CharSequence text, int maxChar) {
		Text t = text();
		t.set(text);
		t.setMaxChars(maxChar);
		return add(t);
	}

	public GUI_BOX add(SPRITE s);
	
	public GUI_BOX add(SPRITE s, int width);
	
	public GUI_BOX add(RENDEROBJ obj);
	
	public boolean emptyIs();
	
}
