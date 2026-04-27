package snake2d.util.sprite;

import snake2d.SPRITE_RENDERER;

public class SpriteTwin implements SPRITE {

	private SPRITE a;
	private SPRITE b;
	private int offX;
	private int offY;

	public SpriteTwin(){
		
	}
	
	public SpriteTwin(SPRITE a, SPRITE b){
		set(a, b);
	}
	
	public void set(SPRITE a, SPRITE b){
		this.a = a;
		this.b = b;
		offX = (a.width()-b.width())/2;
		offY = (a.height()-b.height())/2;
		
	}
	
	@Override
	public int width() {
		return a.width();
	}

	@Override
	public int height() {
		return a.height();
	}

	@Override
	public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
		if (a != null) {
			a.render(r, X1, X2, Y1, Y2);
			b.render(r, X1 + offX, X2 - offX, Y1 + offY, Y2 - offY);
		}
	}

	@Override
	public void renderTextured(TextureCoords other, int X1, int X2, int Y1, int Y2) {
		if (a != null) {
			a.renderTextured(other, X1, X2, Y1, Y2);
			b.renderTextured(other, X1 + offX, X2 - offX, Y1 + offY, Y2 - offY);
		}
		
	}

}
