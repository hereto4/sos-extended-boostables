package view.interrupter;

import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.gui.GuiSection;
import util.gui.misc.GBox;

public abstract class InterGui extends Interrupter{

	protected GuiSection section = new GuiSection();
	
	public InterGui() {

	}
	
	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		return section.hover(mCoo) || mCoo.isWithinRec(section.body());
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.LEFT)
			section.click();
		else if (button == MButt.RIGHT && back()) {
			deactivate();
		}
	}
	
	protected boolean back() {
		return true;
	}
	
	@Override
	protected boolean otherClick(MButt button) {
		if (button == MButt.RIGHT && !pinned()) {
			deactivate();	
			return true;
		}
		return false;
	}

	@Override
	protected void hoverTimer(GBox text) {
		section.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		section.render(r, ds);
		return true;
	}

	@Override
	protected boolean update(float ds) {
		return true;
	}
	
	public void deactivate() {
		hide();
	}
	
	public RECTANGLE body() {
		return section.body();
	}

}
