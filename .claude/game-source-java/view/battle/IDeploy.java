package view.battle;

import game.GAME;
import init.constant.C;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.text.Dic;
import view.interrupter.InterManager;
import view.interrupter.Interrupter;
import view.main.VIEW;

public class IDeploy extends Interrupter{

	private final GButt.ButtPanel butt = new GButt.ButtPanel(Dic.¤¤Start) {
		@Override
		protected void clickA() {
			VIEW.b().state().deploy();
			hide();
		};
	};
	
	public IDeploy(InterManager m) {
		butt.pad(50, 5);
		butt.body.centerX(C.DIM());
		butt.body.moveY1(0);
		pin();
		show(m);
	}
	
	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		return butt.hover(mCoo);
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.LEFT)
			butt.click();
	}

	@Override
	protected void hoverTimer(GBox text) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		butt.render(r, ds);
		return true;
	}

	@Override
	protected boolean update(float ds) {
		GAME.SPEED.tmpPause();
		return true;
	}

}
