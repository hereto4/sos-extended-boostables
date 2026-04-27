package util.gui.slider;

import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.INT.INTE;
import util.gui.misc.GButt;
import view.main.VIEW;

public class GSliderHor extends GuiSection{

	
	private final int size;
	private final SPRITE c,cc;
	private final INTE target;
	
	private GSliderHor(SPRITE b1, SPRITE c, SPRITE cc, SPRITE b2, INTE target, int size) {
		this.target = target;

		this.size = b1.width();
		this.c = c;
		this.cc = cc;
		
		CLICKABLE b;
		
		b = new GButt.Glow(b1) {
			
			@Override
			protected void renAction() {
				activeSet(target().get() > target().min());
			}
			
			@Override
			protected void clickA() {
				if (target().get() > target().min())
					target().inc(-1);
			}
			
		}.repetativeSet(true);
		add(b);
		
		if (size < 3*this.size)
			size = 3*this.size;
		
		size -= 2*this.size;
		
		b = new Mid(size);
		addRightC(0, b);
		
		b = new GButt.Glow(b2) {
			
			@Override
			protected void renAction() {
				activeSet(target().get() < target().max());
			}
			
			@Override
			protected void clickA() {
				if (target().get() < target().max())
					target().inc(1);
			}
			
		}.repetativeSet(true);
		addRightC(0, b);
		
	}
	
	protected INTE target() {
		return target;
	}
	
	public GSliderHor(INTE target, int size) {
		this(UI.decor().slider.makeSprite(0),
				UI.decor().slider.makeSprite(1), 
				UI.decor().slider.makeSprite(2), 
				UI.decor().slider.makeSprite(3), target, size);
		
	}
	
	private class Mid extends CLICKABLE.ClickableAbs{

		private boolean dragging;
		
		Mid(int s){
			body.setWidth(s);
			body.setHeight(size);
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			dragging &= MButt.LEFT.isDown();
			
			if (dragging) {
				double sx = body.x1()+cc.width()/2;
				double ex = body.x2()-cc.width()/2;
				double w = ex-sx;
				double d = (VIEW.mouse().x()-sx)/w;
				int k = (int) CLAMP.d(Math.round(d*target.max()), target().min(), target().max());
				target().set(k);
			}
			
			isActive &= target().min() != target().max();
			
			if (!isActive)
				GCOLOR.T().INACTIVE.bind();
			else if(isHovered || dragging)
				COLOR.WHITE100.bind();
			else
				COLOR.WHITE85.bind();
			
			int mids = body.width()/size;
			for (int i = 0; i < mids; i++) {
				c.render(r, body.x1()+i*size, body.y1());
			}
			int left = body.width()%size;
			if (left != 0) {
				int x1 = size*mids-size+left;
				c.render(r, body.x1()+x1, body.y1());
			}
			
			if (target().max() != 0) {
				double d = target().get()/(double)(target().max());
				int x1 = (int)((body().width()-size)*d);
				
				cc.render(r, body.x1()+x1, body.y1());
			}
			
			COLOR.unbind();
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			

			return super.hover(mCoo);
			
		}
		
		@Override
		protected void renAction() {
			activeSet(target.get() > target.min() || target.get() < target.max());
		}
		
		@Override
		protected void clickA() {
			dragging = true;
			super.clickA();
		}
		
		
	}
	
}
