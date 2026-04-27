package view.ui.tech;

import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import util.data.INT.INTE;
import util.gui.slider.GSliderVer;
import view.main.VIEW;

final class Tree extends GuiSection{

	private final NodeCreator rows;
	private GuiSection content = new GuiSection();
	final INTE ii;
	

	final Prompt prompt = new Prompt();
	
	Tree(int height, int width){
		
		width -= 24;
		;
		
		rows = new NodeCreator(width);
		
		int lr = 0;
		int h = 0;

		int hi = rows.rows.get(0).body().height();
		height = hi*(height/hi);
		content.body().setWidth(width).setHeight(height);
		
		for (int i = rows.rows.size()-1; i >= 0; i--) {
			
			h += hi;
			if (h > height)
				break;
			lr = i;
		}
		
		add(content);
		final int last = lr;
		ii = new INTE() {
			
			int c = 0;
			
			@Override
			public int min() {
				return 0;
			}
			
			@Override
			public int max() {
				return last;
			}
			
			@Override
			public int get() {
				return c;
			}
			
			@Override
			public void set(int t) {
				c = CLAMP.i(t, 0, last);
				adjust(c);
			}
		};
		
		
		
		addRelBody(8, DIR.E, new GSliderVer(ii, height));
		
		adjust(0);
		
	}
	
	@Override
	protected void moveCallback() {
		if (ii != null)
			ii.inc(0);
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		if (body().holdsPoint(VIEW.mouse())) {
			double d = MButt.clearWheelSpin();
			if ( d != 0) {
				ii.inc(-(int)d);
			}
		}
		
		super.render(r, ds);

	}
	

	
	private void adjust(int fr){
		
		
		int x1 = content.body().x1();
		int y1 = content.body().y1();
		int w = content.body().width();
		int h = content.body().height();
		content.clear();
		content.body().setDim(w, h);
		content.body().moveX1Y1(x1, y1);
		int y = y1;
		
		{
			int dy = 0;
			for (int i = 0; i < fr; i++) {
				dy += rows.rows.get(i).body().height();
			}
			y -= dy;
		}

		
		for (RENDEROBJ rr : rows.rows) {

			int hi = rr.body().height();
			
			
			rr.body().moveX1(x1);
			rr.body().moveY1(y);
			
			if (y >= y1 && y + hi <= content.body().y2())
				content.add(rr);
			
			
			y+= hi;
		}
		
	}

	
}
