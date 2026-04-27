package view.interrupter;

import init.constant.C;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import util.gui.misc.GBox;
import util.gui.panel.GPanel;
import view.keyboard.KEYS;
import view.main.VIEW;

public class IPopCurrent extends Interrupter{

	public final GuiSection expansion = new GuiSection();
	private CLICKABLE trigger;
	private final GPanel panel = new GPanel();
	
	public IPopCurrent() {
		super();
		panel.setCloseAction(new ACTION() {
			
			@Override
			public void exe() {
				hide();
			}
		});
	}

	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		boolean ret = panel.hover(mCoo) | expansion.hover(mCoo);

		return ret;
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.LEFT) {
			expansion.click();
			panel.click();
		}
		if (panel.hoveredIs())
			MButt.clearWheelSpin();
		else if(button == MButt.RIGHT)
			hide();
	}

	public void show(CLICKABLE trigger) {
		if (isActivated())
			hide();
		
		panel.inner().set(expansion);
		
		panel.body().moveC(trigger.body().cX(), 0);
		panel.body().moveY1(trigger.body().y2());
		if (panel.body().y2() > C.HEIGHT())
			panel.body().moveY2(trigger.body().y2());
		if (panel.body.x2() > C.WIDTH()-32)
			panel.body.moveX2(C.WIDTH()-32);
		if (panel.body.x1() < 32)
			panel.body.moveX1(32);
		
		expansion.body().centerIn(panel.inner());
		super.show(VIEW.current().uiManager);
	}
	
	@Override
	protected boolean otherClick(MButt button) {
		
		hide();
		return false;
	}
	
	@Override
	public void hide() {
		super.hide();
	}
	
	@Override
	protected void hoverTimer(GBox text) {
		expansion.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		panel.inner().set(expansion);
		panel.body.centerIn(expansion);
		panel.render(r, ds);
		expansion.render(r, ds);
		if (trigger != null)
			trigger.selectTmp();
		return true;
	}

	@Override
	protected boolean update(float ds) {
		if (KEYS.anyDown())
			hide();
		if (panel.hoveredIs())
			MButt.clearWheelSpin();
		return true;
	}
	
	@Override
	protected void otherAdd(Interrupter other) {
		hide();
	}
	
	
}
