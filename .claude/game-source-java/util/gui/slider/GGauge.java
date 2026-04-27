package util.gui.slider;

import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.sprite.SPRITE;
import util.data.DOUBLE;
import util.gui.common.TITLEABLE;
import util.gui.misc.GMeter;

public abstract class GGauge extends SPRITE.Imp implements TITLEABLE, DOUBLE{

	private final GMeter.GMeterCol col;
	
	public GGauge(int width, int height){
		this(width, height, GMeter.C_REDGREEN);
		setDim(width, height);
	}
	
	public GGauge(){
		this(48, 16, GMeter.C_REDGREEN);
	}
	
	public GGauge(int width, int height, GMeter.GMeterCol col){
		this.col = col;
		setDim(width, height);
	}

	@Override
	public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
		
		GMeter.render(r, col, getD(), X1, X2, Y1, Y2);
		
		
	}
	
	void render(SPRITE_RENDERER r, COLOR col, int x1, int w, int y1, int h, int dx, int dy) {
		if (w > 2*dx && h > 2*dy)
			col.render(r, x1+dx, x1+w-dx, y1+dy, y1+h-dy);
		
	}
	
}
