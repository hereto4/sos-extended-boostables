package view.ui.manage;

import init.constant.C;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.sprite.SPRITE;
import view.main.VIEW;

public class IFullView {
	
	public final static int TOP_HEIGHT = IManager.TOP_HEIGHT+8;
	public static final int WIDTH = C.WIDTH()-32;
	public static final int HEIGHT = C.HEIGHT()-TOP_HEIGHT-8;
	
	public final CharSequence title;
	public final SPRITE icon;
	protected GuiSection section = new GuiSection();
	
	public IFullView(CharSequence name, SPRITE icon){
		this.title = name;
		this.icon = icon;
	}
	
	
	

	
	public void activate() {
		VIEW.UI().manager.show(this);
	}

	public boolean back() {
		return false;
	}





	public void init() {
		
	}





	public void hoverInfoGet(GUI_BOX text) {
		text.title(title);
	}
	


	
}