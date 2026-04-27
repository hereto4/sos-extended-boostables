package view.interrupter;

import init.constant.C;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import util.gui.misc.GBox;
import util.gui.panel.GPanel;
import view.main.VIEW;

public final class IPopup{
	
	private final GuiSection s = new GuiSection();
	private final Inter inter = new Inter(s);
	private final InterManager m;
	private CLICKABLE trigger;
	
	
	public IPopup(InterManager manager){
		this.m = manager;
	}

	
	public void show(RENDEROBJ s, CLICKABLE trigger) {
		show(s, trigger, false);
		
	}
	
	public void show(RENDEROBJ s, CLICKABLE trigger, boolean centreAtMouse) {
		old = null;
		this.s.clear();
		this.s.add(s);
		this.trigger = trigger;
		if (trigger != null)
			showP(trigger.body().cX(), trigger.body().cY(), centreAtMouse);
		else {
			int x1 = C.WIDTH()/2 - s.body().width()/2;
			int y1 = C.HEIGHT()/2 - s.body().height()/2;
			showP(x1, y1, centreAtMouse);
		}
		
	}
	
	RENDEROBJ old;
	CLICKABLE oldC;
	
	public void push(RENDEROBJ s, CLICKABLE trigger) {
		RENDEROBJ old = null;
		oldC = this.trigger;
		if (inter.isActivated()) {
			old = this.s.elements().get(0);
		}
		show(s, trigger);
		this.old = old;
	}
	
	public void pop() {
		if (inter.isActivated() && old != null) {
			show(old, oldC);
		}else
			close();
		
	}
	
	public GuiSection section() {
		return s;
	}
	
	public void close() {
		inter.hide();
	}
	
	
	public boolean showing() {
		return inter.isActivated();
	}
	
	public RENDEROBJ current() {
		return inter.isActivated() ? s.elements().get(0) : null;
	}
	
	protected void showP(int x, int y, boolean centre) {
		
		int M = C.SG*32;
		
		if (centre) {
			s.body().moveC(VIEW.mouse());
			if (!inter.isActivated()) {
				m.add(inter);
			}
		}else {
			s.body().moveCX(x);
			if (y > C.HEIGHT()/2){
				s.body().moveY2(y-M);
			}else {
				s.body().moveY1(y+M);
			}
		}
		
		
		
		if (s.body().x2()+M >= C.WIDTH()) {
			s.body().moveX2(C.WIDTH()-M);
		}
		
		if (s.body().x1() - M < 0) {
			s.body().moveX1(x+M);
		}
		
		if (s.body().y2()+M >= C.HEIGHT()) {
			s.body().moveY2(C.HEIGHT()-M);
		}
		
		if (s.body().y1() - M < 0) {
			s.body().moveY1(M);
		}
		
		inter.hidden = true;
		
		if (!inter.isActivated()) {
			m.add(inter);
		}
	}

	private class Inter extends Interrupter {
		
		private boolean hidden = true;
		private final GPanel box;
		ACTION exit = new ACTION() {
			
			@Override
			public void exe() {
				if (hidden)
					return;
				hide();
			}
		};
		
		
		Inter(GuiSection s){
			box = new GPanel();
			box.setButt();
		}
		
		@Override
		protected void hoverTimer(GBox text) {
			s.hoverInfoGet(text);
		}

		@Override
		protected void mouseClick(MButt button) {
			if (button == MButt.RIGHT){
				hide();
			}else if(button == MButt.LEFT){
				if (!s.click())
					box.click();
			}
		}
		
		@Override
		public void hide() {
			if (old != null) {
				IPopup.this.show(old, oldC);
			}else
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
			return s.hover(mCoo) || box.hover(mCoo);
		}

		@Override
		protected boolean render(Renderer r, float ds) {
			hidden = false;
			box.inner().set(s);
			box.clickActionSet(exit);
			box.render(r, ds);
			//box.moveExit(exit);
			s.render(r, ds);
			if (trigger != null) {
				trigger.selectTmp();
			}
			return true;
		}

		@Override
		protected boolean update(float ds) {
//			if (KEY.anyPressed()) {
//				hide();
//			}
			return true;
		}
		
		
		
	}

}
