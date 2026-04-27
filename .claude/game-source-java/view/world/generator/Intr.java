package view.world.generator;

import init.constant.C;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GuiSection;
import util.gui.misc.GBox;
import util.gui.panel.GPanel;
import view.interrupter.Interrupter;

final class Intr extends Interrupter{

	private GuiSection s;
	
	Intr(WorldViewGenerator v){
		pin();
		v.uiManager.add(this);
	}
	
	public void add(GuiSection s, CharSequence title) {
		add(s, title, true);
	}
	
	public void add(GuiSection s, CharSequence title, boolean panel) {
		this.s = s;
		if (s != null && panel) {
			
			s.body().moveCY(C.HEIGHT()/2);
			s.body().moveCX(C.WIDTH()/2);
			
			GPanel pan = new GPanel();
			pan.setBig();
			pan.inner().setDim(s.body().width(), s.body().height());
			pan.body.centerIn(s);
			pan.setTitle(title);
			s.add(pan);
			s.moveLastToBack();
			
		}
	}
	
	
	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		if (s != null) {
			return s.hover(mCoo);
		}
		return false;
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.LEFT && s != null)
			s.click();
	}

	@Override
	protected void hoverTimer(GBox text) {
		if (s != null)
			s.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		if (s != null)
			s.render(r, ds);
		return true;
	}

	@Override
	protected boolean update(float ds) {
		
		return false;
	}
	
	@Override
	public boolean canSave() {
		return true;
	}
	

}