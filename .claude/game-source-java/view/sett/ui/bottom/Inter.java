package view.sett.ui.bottom;

import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import util.gui.misc.GBox;
import view.interrupter.Interrupter;
import view.main.VIEW;

final class Inter extends Interrupter{

	private CLICKABLE panel;
	public CLICKABLE exp;
	
	private CLICKABLE DUMMY = new GuiSection();
	private CLICKABLE trigger;
	
	
	Inter(){
		
	}
	
	void set(CLICKABLE trigger, CLICKABLE panel) {
		this.trigger = trigger;
		this.panel = panel;
		this.panel.body().moveY2(trigger.body().y1());
		this.panel.body().moveCX(trigger.body().cX());
		exp(trigger, DUMMY);
		show(VIEW.s().uiManager);
	}
	
	void exp(CLICKABLE exbutt, CLICKABLE panel) {
		if (exp == panel)
			return;
		exp = panel;
		exp.body().moveY1(this.panel.body().y1());
		exp.body().moveX1(this.panel.body().x2());
		
	}
	
	@Override
	protected void hoverTimer(GBox text) {
		panel.hoverInfoGet(text);
		exp.hoverInfoGet(text);
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.RIGHT){
			hide();
		}else if(button == MButt.LEFT){
			panel.click();
			exp.click();
			
		}
	}
	
	@Override
	public void hide() {
		super.hide();
	}
	
	@Override
	protected boolean otherClick(MButt butt) {
		hide();
		if (butt == MButt.RIGHT)
			return true;
		return false;
	}
	
	@Override
	protected void otherAdd(Interrupter other) {
		hide();
	}

	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		boolean ret = panel.hover(mCoo);
		if (exp.hover(mCoo)) {
			ret = true;
		}
//		if (!ret)
//			exp(DUMMY, DUMMY);
		return ret;
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		trigger.selectTmp();
		panel.render(r, ds);
		exp.render(r, ds);
		return true;
	}

	@Override
	protected boolean update(float ds) {
		return true;
	}
}