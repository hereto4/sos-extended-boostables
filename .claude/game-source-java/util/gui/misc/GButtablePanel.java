package util.gui.misc;

import init.constant.C;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.clickable.CLICKABLE.ClickableAbs;
import util.gui.panel.GPanel;

public final class GButtablePanel extends ClickableAbs{

	private final GuiSection s = new GuiSection();
	private boolean visable = false;
	private GPanel box = new GPanel();
	private int buttI;
	public GButtablePanel() {
		box.setButt();
	}

	public void addTitle(CharSequence s) {
		box.setTitle(s);
		body.set(box.body());
	}
	
	public void addButton(CLICKABLE button, int margin) {
		if (buttI++ >= 10) {
			button.body().moveX1(s.body().x1()).moveY1(s.body().y2());
			s.add(button);
			buttI= 0;
		}else {
			s.addRight(margin, button);
		}
		visable = true;

		s.body().centerX(C.DIM());
		s.body().moveY1(120);
		box.inner().set(s);
		body.set(box.body());
	}
	
	public void nl() {
		buttI = 100;
	}
	
	public void addButton(CLICKABLE button) {
		addButton(button, 0);
		
	}
	
	public void addButtons(CLICKABLE... buttons) {
		for (CLICKABLE bu : buttons)
			addButton(bu);
	}


	public void clear() {
		s.clear();
		visable = false;
		buttI = 0;
		box.title().clear();
	}

	@Override
	protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
		if (visable) {
			box.render(r, ds);
			s.render(r, ds);
		}
	}
	
	@Override
	protected void clickA() {
		s.click();
	}
	
	@Override
	public boolean hover(COORDINATE mCoo) {
		s.hover(mCoo);
		return super.hover(mCoo);
	}
	
	@Override
	public void hoverInfoGet(GUI_BOX text) {
		s.hoverInfoGet(text);
	}

}
