package util.gui.panel;

import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;
import util.colors.GCOLOR;

public class GFrame extends RENDEROBJ.RenderImp implements RENDEROBJ{

	public final static int MARGIN = 3;
	
	private Rec bounds = new Rec();

	public GFrame(RECTANGLE body){
		frame(body);

	}

	@Override
	public Rec body() {
		return bounds;
	}
	
	public void frame(RECTANGLE body) {
		this.bounds.setDim(body.width()+MARGIN*2, body.height()+MARGIN*2);
		this.bounds.centerIn(body);
	}

	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		render(r, ds, bounds);
	}
	
	public static void render(SPRITE_RENDERER r, float ds, RECTANGLE b) {
		render(r, b.x1(), b.x2(), b.y1(), b.y2());
	}
	
	public static void render(int MARGIN, SPRITE_RENDERER r, int x1, int x2, int y1, int y2) {
		
		x1-= MARGIN;
		x2+= MARGIN;
		y1-= MARGIN;
		y2+= MARGIN;
		GCOLOR.UI().borderH(r, x1, x2, y1, y2);
		
	}
	
	public static void render(SPRITE_RENDERER r, int x1, int x2, int y1, int y2) {
		
		render(MARGIN, r, x1, x2, y1, y2);
		
	}
	
	public static SPRITE separator(int width) {
		return new SPRITE() {
			
			@Override
			public int width() {
				return width;
			}
			
			@Override
			public int height() {
				return 16;
			}
			
			@Override
			public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				GCOLOR.UI().borderH(r, X1, X2, Y1+height()/2-1, Y1+height()/2+2);
			}
		};
	}
	
}