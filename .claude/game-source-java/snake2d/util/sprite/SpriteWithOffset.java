package snake2d.util.sprite;

import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.Coo;

public class SpriteWithOffset extends Coo implements SPRITE {

	private static final long serialVersionUID = 1L;
	private final SPRITE a;

	public SpriteWithOffset(SPRITE s){
		a = s;
	}
	
	public SpriteWithOffset(SPRITE s, int x, int y){
		super(x, y);
		a = s;
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
		X1 += x();
		Y1 += y();
		X2 += x();
		Y2 += y();
		a.render(r, X1, X2, Y1, Y2);
		
	}

	@Override
	public void renderTextured(TextureCoords other, int X1, int X2, int Y1, int Y2) {
		X1 += x();
		Y1 += y();
		X2 += x();
		Y2 += y();
		a.renderTextured(other, X1, X2, Y1, Y2);
		
	}

}
