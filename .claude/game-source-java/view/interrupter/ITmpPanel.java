package view.interrupter;

import init.constant.C;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GuiSection;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.panel.GPanel;

public final class ITmpPanel extends Interrupter{

	private boolean visable = false;
	private final GuiSection section = new GuiSection();
	private GPanel box = new GPanel();
	private int buttI;

	
	public ITmpPanel(InterManager manager) {
		
		pin();
		
		
		box.setButt();
		show(manager);
	}

	public void addTitle(CharSequence s) {
		box.setTitle(s);

	}
	
	public void addButton(GButt.Panel button) {
		if (buttI++ > 10) {
			button.body.moveX1(section.body().x1()).moveY1(section.body().y2());
			section.add(button);
			buttI= 0;
		}else {
			section.addRight(0, button);
		}
		
		
		visable = true;

		section.body().centerX(C.DIM());
		section.body().moveY1(120);
		
	}
	
	public void addButtons(GButt.Panel... buttons) {
		for (GButt.Panel bu : buttons)
			addButton(bu);
	}
	
	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		if (!visable)
			return false;
		return section.hover(mCoo);
	}

	@Override
	protected void mouseClick(MButt button) {
		if (MButt.LEFT == button)
			section.click();
	}

	@Override
	protected void hoverTimer(GBox text) {
		section.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		
	
		if (visable) {
			box.inner().set(section);
			box.render(r, ds);
			section.render(r, ds);
		}
		
		visable = false;
		buttI = 0;
		box.title().clear();
		section.clear();
		return true;
	}

	@Override
	protected boolean update(float ds) {
		return true;
	}
	

}
