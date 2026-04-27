package snake2d.util.sprite;

import snake2d.SPRITE_RENDERER;
import snake2d.util.sets.ArrayListResize;

/**
 * This disgusting class took me a day to make. I almost hit my wife because if it. I hate it.
 * @author mail__000
 *
 */
public class SpriteCollection implements SPRITE {

	private ArrayListResize<SpriteWCoo> sprites;
	private int width = 0;
	private int height = 0;
	
	public SpriteCollection(SPRITE s){
		sprites = new ArrayListResize<SpriteWCoo>(2, 500);
		add(s, 0, 0);
	}

	public void add(SPRITE s, int x, int y){

		
		if (y < 0){
			for (SpriteWCoo sc : sprites){
				sc.dY = sc.dY - y;
			}
			y = 0;
			
		}
		if (y + s.height() > height){
			height = y + s.height();
		}
		
		if (x < 0){
			for (SpriteWCoo sc : sprites){
				sc.dX = sc.dX - x;
			}
			x = 0;
			
		}
		if (x + s.width() > width){
			width = x + s.width();
		}
		
		sprites.add(new SpriteWCoo(s, x, y));
	}
	
	public SpriteCollection addRight(SPRITE s){
		add(
				s, 
				sprites.getLast().dX + sprites.getLast().s.width(),
				sprites.getLast().dY);
		return this;
	}
	
	public void addRightC(int m, SPRITE s){
		add(
				s, 
				sprites.getLast().dX + sprites.getLast().s.width() + m,
				sprites.getLast().dY + (sprites.getLast().s.height() - s.height())/2);
	}
	
	public void addRightCAbs(int abs, SPRITE s){
		add(
				s, 
				abs,
				sprites.getLast().dY + (sprites.getLast().s.height() - s.height())/2);
	}
	
	public void addDown(SPRITE s){
		add(
				s,
				sprites.getLast().dX,
				sprites.getLast().dY + sprites.getLast().s.height());
	}
	
	public void addLeft(SPRITE s){
		add(
				s, 
				(sprites.getLast().dX - s.width()),
				sprites.getLast().dY);
	}
	
	public void addUp(SPRITE s){
		add(
				s, 
				sprites.getLast().dX,
				sprites.getLast().dY - s.height());
	}
	
	public void addOnTopCentered(SPRITE s){
		
		int x1 = sprites.getLast().dX + (sprites.getLast().s.width() - s.width())/2;
		int y1 = sprites.getLast().dY + (sprites.getLast().s.height() - s.height())/2;
		add(s, x1, y1);
		
	}
	
	public int getLastX1(){
		return sprites.getLast().dX;
	}
	
	public int getLastX2(){
		return sprites.getLast().dX + sprites.getLast().s.width();
	}
	
	public int getLastY1(){
		return sprites.getLast().dY;
	}
	
	public int getLastY2(){
		return sprites.getLast().dY + sprites.getLast().s.height();
	}
	
	private class SpriteWCoo{
		
		private final SPRITE s;
		private int dY;
		private int dX;
		
		private SpriteWCoo(SPRITE s, int x, int y){
			this.s = s;
			dY = y;
			dX = x;
		}
		
	}

	@Override
	public int width() {
		return width;
	}

	@Override
	public int height() {
		return height;
	}

	@Override
	public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
		float xScale = (X2-X1)/width;
		float yScale = (Y2-Y1)/height;
		
		int x1;
		int x2;
		int y1;
		int y2;
		for (SpriteWCoo s : sprites){
			x1 = (int) (X1 + s.dX*xScale);
			y1 = (int) (Y1 + s.dY*yScale);
			x2 = (int) ((X1 + s.dX + s.s.width())*xScale);
			y2 = (int) ((Y1 + s.dY + s.s.height())*yScale);
			s.s.render(r, x1, x2, y1, y2);
		}
		
		
	}

	@Override
	public void render(SPRITE_RENDERER r, int X1, int Y1) {
		for (SpriteWCoo s : sprites){
			s.s.render(r, X1 + s.dX, X1 + s.dX + s.s.width(), Y1 + s.dY, Y1 + s.dY + s.s.height());
		}
		
	}

	@Override
	public void renderTextured(TextureCoords  other, int X1, int X2, int Y1, int Y2) {
		for (SpriteWCoo s : sprites){
			s.s.renderTextured(other, X1 + s.dX, X1 + s.dX + s.s.width(), Y1 + s.dY, Y1 + s.dY + s.s.height());
		}
		
	}
	
}
