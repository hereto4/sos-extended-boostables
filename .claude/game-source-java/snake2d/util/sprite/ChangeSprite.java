package snake2d.util.sprite;

import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.RECTANGLE;

public class ChangeSprite implements SPRITE{

	private SPRITE s;
	private final ColorImp c = new ColorImp(COLOR.WHITE100);
	
	public ChangeSprite() {
		
	}
	
	public ChangeSprite(SPRITE s){
		set(s);
	}
	
	public ChangeSprite(SPRITE s, COLOR c){
		set(s);
		getColor().set(c);
	}
	
	public void set(SPRITE s){
		this.s = s;
	}
	
	public ColorImp getColor(){
		return c;
	}
	
	@Override
	public int width() {
		return s != null ? s.width() : 0;
	}

	@Override
	public int height() {
		return s != null ? s.height() : 0;
	}

	@Override
	public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
		if (s != null){
			c.bind();
			s.render(r, X1, X2, Y1, Y2);
			COLOR.unbind();
		}
		
	}

	@Override
	public void render(SPRITE_RENDERER r, RECTANGLE rec) {
		if (s != null){
			c.bind();
			s.render(r, rec);
			COLOR.unbind();
		}
		
	}

	@Override
	public void render(SPRITE_RENDERER r, int X1, int Y1) {
		if (s != null){
			c.bind();
			s.render(r, X1, Y1);
			COLOR.unbind();
		}
		
	}

	@Override
	public void renderTextured(TextureCoords texture, int X1, int X2, int Y1,
			int Y2) {
		s.renderTextured(texture, X1, X2, Y1, Y2);
		
	}

	
}
