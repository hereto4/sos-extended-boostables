package util.gui.slider;

import init.sprite.UI.UI;
import snake2d.CORE;
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

public class GSliderVer extends GuiSection{

	private final INTE target;
	private final int size;
	private final SPRITE c,cc;
	
	private GSliderVer(SPRITE b1, SPRITE c, SPRITE cc, SPRITE b2, INTE target, int width) {
		this.target = target;


		this.size = b1.width();
		this.c = c;
		this.cc = cc;
		
		
		GButt.Glow b;
		
		b = new GButt.Glow(b1) {
			
			@Override
			protected void renAction() {
				activeSet(target.get() > target.min());
			}
			
			@Override
			protected void clickA() {
				if (target.get() > target.min())
					target.inc(-1);
			}
			
		};
		b.body.setDim(b1.width());
		b.repetativeSet(true);
		add(b);
		
		if (width < 3*this.size)
			width = 3*this.size;
		
		width -= 2*this.size;
		
		addDownC(0,new Mid(width));
		
		b = new GButt.Glow(b2) {
			
			@Override
			protected void renAction() {
				activeSet(target.get() < target.max());
			}
			
			@Override
			protected void clickA() {
				if (target.get() < target.max())
					target.inc(1);
			}
			
		};
		b.body.setDim(b1.width());
		b.repetativeSet(true);
		addDownC(0, b);
		
	}
	
	public GSliderVer(INTE target, int size) {
		this(UI.decor().slider.makeSprite(4),
				UI.decor().slider.makeSprite(5), 
				UI.decor().slider.makeSprite(6), 
				UI.decor().slider.makeSprite(7), target, size);
	}
	
	public static int WIDTH() {
		return UI.decor().slider.size();
	}
	
	private class Mid extends CLICKABLE.ClickableAbs{

		private boolean dragging;
		
		Mid(int s){
			body.setHeight(s);
			body.setWidth(size);
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			dragging &= MButt.LEFT.isDown();
			
			if (dragging) {
				double d = (CORE.getInput().getMouse().getCoo().y()-body.y1())/(double)body.height();
				int k = (int) CLAMP.d(Math.round(d*target.max()), target.min(), target.max());
				target.set(k);
			}
			
			isActive &= target.min() != target.max();
			
			if (!isActive)
				GCOLOR.T().INACTIVE.bind();
			else if(isHovered || dragging)
				COLOR.WHITE100.bind();
			else
				COLOR.WHITE65.bind();
			
			int mids = body.height()/size;
			for (int i = 0; i < mids; i++) {
				c.render(r, body.x1(), body.y1()+i*size);
			}
			int left = body.height()%size;
			if (left != 0) {
				int y1 = size*mids-size+left;
				c.render(r, body.x1(), body.y1()+y1);
			}
			if (target.max() > 0) {
				int y1 = (body().height()-size)*target.get()/(target.max());
				cc.render(r, body.x1(), body.y1()+y1);
			}
			
			
			COLOR.unbind();
		}
		
		@Override
		protected void renAction() {
			activeSet(target.get() > target.min() || target.get() < target.max());
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			
			dragging &= MButt.LEFT.isDown();
			
			if (dragging) {
				double d = (CORE.getInput().getMouse().getCoo().y()-body.y1())/(double)body.height();
				int k = (int) CLAMP.d(d*target.max(), target.min(), target.max());
				target.set(k);
			}
			return super.hover(mCoo);
			
		}
		
		@Override
		protected void clickA() {
			dragging = true;
			super.clickA();
		}
		
		
	}
	
}
