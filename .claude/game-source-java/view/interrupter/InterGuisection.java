package view.interrupter;

import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.ACTION;
import util.gui.misc.GBox;
import view.keyboard.KEYS;

public class InterGuisection extends Interrupter{

	private GuiSection section;
	private ACTION closeAction;
	private final InterManager m;
	
	public InterGuisection(InterManager m) {
		this.m = m;
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
		if (button == MButt.RIGHT) {
			deactivate();
			return true;
		}
		return false;
	}

	public GuiSection section() {
		return section;
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
		if (KEYS.MAIN().ESCAPE.consumeClick())
			hide();
		return true;
	}
	
	public void activate(GuiSection section) {
		this.section = section;
		show(m);
	}
	
	public void close() {
		hide();
	}
	
	public void setCloseAction(ACTION action) {
		this.closeAction = action;
	}
	
	public void deactivate() {
		hide();
	}
	
	@Override
	protected void deactivateAction() {
		if (closeAction != null)
			closeAction.exe();
		closeAction = null;
		super.deactivateAction();
	}

	public GuiSection current() {
		if (isActivated())
			return this.section;
		return null;
	}

}
