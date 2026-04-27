package snake2d.util.sprite;

import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIMENSION;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;

public abstract interface SPRITE extends DIMENSION{

	/**
	 * 
	 * @param r
	 * @param dt
	 * @param quad
	 * @return true if end of animation. Always false for sprites.
	 */
	public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2);
	
	public default void render(SPRITE_RENDERER r, RECTANGLE rec){
		render(r, rec.x1(), rec.x2(), rec.y1(), rec.y2());
	}
	
	/**
	 * 
	 * @param r
	 * @param dt
	 * @param X1
	 * @param Y1
	 * @return true if end of animation. Always false for sprites.
	 */
	public default void render(SPRITE_RENDERER r, int X1, int Y1){
		render(r, X1, X1+width(), Y1, Y1+height());
	}
	
	public default void renderC(SPRITE_RENDERER r, int cx, int cy){
		render(r, cx-width()/2, cx-width()/2+width(), cy-height()/2, cy-height()/2+height());
	}
	
	public default void renderCScaled(SPRITE_RENDERER r, int cx, int cy, int scale){
		render(r, cx-width()*scale/2, cx-width()*scale/2+width()*scale, cy-height()*scale/2, cy-height()*scale/2+height()*scale);
	}
	
	public default void renderC(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2){
		int cx = X1 + (X2-X1)/2;
		int cy = Y1 + (Y2-Y1)/2;
		renderC(r, cx, cy);
	}
	
	public default void renderCY(SPRITE_RENDERER r, int x1, int cy){
		render(r, x1, x1+width(), cy-height()/2, cy-height()/2+height());
	}
	
	public default void renderCX(SPRITE_RENDERER r, int cx, int y1){
		render(r, cx-width()/2, cx-width()/2+width(), y1, y1+height());
	}
	
	public default void renderCX(SPRITE_RENDERER r, int cx, int y1, int scale){
		render(r, cx-scale*width()/2, cx-scale*width()/2+scale*width(), y1, y1+scale*height());
	}
	
	public default void renderCXY2(SPRITE_RENDERER r, int cx, int y2){
		render(r, cx-width()/2, cx-width()/2+width(), y2-height(), y2);
	}
	
	public default void renderC(SPRITE_RENDERER r, RECTANGLE c){
		renderC(r, c.cX(), c.cY());
	}
	
	public default void renderC(SPRITE_RENDERER r, COORDINATE c){
		renderC(r, c.x(), c.y());
	}
	
	public default void renderScaled(SPRITE_RENDERER r, int X1, int Y1, int scale){
		render(r, X1, X1+width()*scale, Y1, Y1+height()*scale);
	}
	
	public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2);
	
	public default void renderTextured(TextureCoords texture, RECTANGLE rec){
		renderTextured(texture, rec.x1(), rec.x2(), rec.y1(), rec.y2());
	}
	
	public default void renderTextured(TextureCoords texture, int X1, int Y1){
		renderTextured(texture, X1, X1+width(), Y1, Y1+height());
	}
	
	public default TextureCoords texture() {
		return null;
	}
	
	public default SPRITE twin(SPRITE b, DIR align, int shadow) {
		SPRITE s = new SPRITE.Imp(width(), height()) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				SPRITE.this.render(r, X1, X2, Y1, Y2);
				int w = X2-X1;
				int h = Y2-Y1;
				
				double scaleX = (double)w/width();
				double scaleY = (double)h/height();
				
				int sw = (int) (b.width()*scaleX);
				int sh = (int) (b.height()*scaleY);
				
				int dx = (w-sw)/2;
				int dy = (h-sh)/2;
				
				int x1 = X1+dx + align.x()*dx;
				int y1 = Y1+dy + align.y()*dy;
				
				if (shadow > 0) {
					OPACITY.O75.bind();
					COLOR.BLACK.bind();
					int sx = (int) (shadow*scaleX);
					int sy = (int) (shadow*scaleY);
					b.render(r, x1+sx, x1+sw+sx, y1+sy, y1+sh+sy);
					OPACITY.unbind();
					COLOR.unbind();
				}
				
				b.render(r, x1, x1+sw, y1, y1+sh);
				
				
			}
		};
		
		return s;
	}
	
	public default SPRITE scaled(double scale) {
		return new SPRITE.Scaled(this, scale);
	}
	
	public default SPRITE resized(int size) {
		SPRITE o = this;
		return new SPRITE.Imp(size) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				int W = X2-X1;
				int H = Y2-Y1;
				
				
				
//				int w = Math.min(o.width(), W);
//				int h = Math.min(o.height(), H);
				int x1 = X1 + (W-W)/2;
				int y1 = Y1 + (H-H)/2;
				o.render(r, x1, x1+W, y1, y1+H);
			}
		};
	}
	
	public default SPRITE sized(int size) {
		SPRITE o = this;
		return new SPRITE.Imp(size) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				o.renderC(r, X1, X2, Y1, Y2);
			}
		};
	}
	
	public default SPRITE createColored(COLOR color) {
		return new SPRITE() {
			
			@Override
			public int width() {
				return SPRITE.this.width();
			}
			
			@Override
			public int height() {
				return SPRITE.this.height();
			}
			
			@Override
			public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				color.bind();
				SPRITE.this.render(r, X1, X2, Y1, Y2);
				COLOR.unbind();
				
			}
		};
		
	}
	
	public class Resized extends Imp{
		
		private final SPRITE other;
		
		public Resized(SPRITE other, int dim){
			super(dim);
			this.other = other;
		}

		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			other.render(r, X1, X2, Y1, Y2);
		}
		
	}
	
	public abstract class Imp implements SPRITE {
		
		protected int width,height;
		
		public Imp(){
		}
		
		public Imp(int d){
			this(d, d);
		}
		
		public Imp(int w, int h){
			this.width = w;
			this.height = h;
		}
		
		public void setDim(int w, int h) {
			this.width = w;
			this.height = h;
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
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public class Wrap implements SPRITE {
		
		protected int width,height;
		private final DIR align;
		private final SPRITE other;
		
		public Wrap(SPRITE other, int width, int height, DIR align){
			this.width = width;
			this.height = height;
			this.align = align;
			this.other = other;
		}
		
		public Wrap(SPRITE other, int width, int height){
			this(other, width, height, DIR.C);
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
			int w = X2-X1;
			int h = Y2-Y1;
			int dh = (h-other.height())/2;
			int dw = (h-other.width())/2;
			
			int cx = X1+w/2;
			int cy = Y1+h/2;
			cx += dw*align.x();
			cy += dh*align.y();
			other.renderC(r, cx, cy);
			
		}

		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public class Twin implements SPRITE {
		
		private final int w,h;
		private final SPRITE a,b;
		
		public Twin(SPRITE a, SPRITE b){
			this.a = a;
			this.b = b;
			w = a.width();
			h = a.height();
		}

		@Override
		public int width() {
			return w;
		}

		@Override
		public int height() {
			return h;
		}

		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {

		}

		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			a.render(r, X1, X2, Y1, Y2);
			int dx = (X2-X1)/2;
			int CX = X1+dx;
			dx *= (double)a.width()/b.width();
			
			int dy = (Y2-Y1)/2;
			int CY = Y1+dy;
			dy *= (double)a.height()/b.height();
			
			b.render(r, CX-dx, CX+dx, CY-dy, CY+dy);
		}
		
	}
	
	public class Scaled implements SPRITE {
		
		private final int w,h;
		private final SPRITE a;
		
		public Scaled(SPRITE a, double scale){
			this.a = a;
			w = (int) (a.width()*scale);
			h = (int) (a.height()*scale);
		}
		
		public Scaled(SPRITE a, int w, int h){
			this.a = a;
			this.w = w;
			this.h = h;
		}

		@Override
		public int width() {
			return w;
		}

		@Override
		public int height() {
			return h;
		}

		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {

		}

		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			a.render(r, X1, X2, Y1, Y2);
		}
		
	}
	
	public class SpriteImp extends TextureCoords implements SPRITE{
		

		
		protected final int gameWidth;
		protected final int gameHeight;
		
		public SpriteImp(int x1, int x2, int y1, int y2, int gameWidth, int gameHeight){
			this.x1 = (short) x1;
			this.x2 = (short) x2;
			this.y1 = (short) y1;
			this.y2 = (short) y2;
			this.gameWidth = gameWidth;
			this.gameHeight = gameHeight;
		}
		
		public SpriteImp(TextureCoords other, int w, int h){
			get(other);
			this.gameWidth = w;
			this.gameHeight = h;
		}

		@Override
		public int width() {
			return gameWidth;
		}

		@Override
		public int height() {
			return gameHeight;
		}

		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			r.renderSprite(X1, X2, Y1, Y2, 
					this);
		}

		@Override
		public void renderTextured(TextureCoords other, int X1, int X2, int Y1, int Y2) {
			CORE.renderer().renderTextured(X1, X2, Y1, Y2, 
					other, this);
		}

		@Override
		public TextureCoords texture() {
			return this;
		}

	}
	
	public class SpriteFromSheet implements SPRITE{
		
		private final int tile;
		private final TILE_SHEET sheet;
		
		public SpriteFromSheet(TILE_SHEET sheet, int tile){
			this.sheet = sheet;
			this.tile = tile;
		}

		@Override
		public int width() {
			return sheet.size();
		}

		@Override
		public int height() {
			return sheet.size();
		}

		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			sheet.render(r, tile, X1, X2, Y1, Y2);
		}

		@Override
		public void renderTextured(TextureCoords other, int X1, int X2, int Y1, int Y2) {
			sheet.renderTextured(other, tile, X1, Y1);
		}
		
		@Override
		public TextureCoords texture() {
			return sheet.getTexture(tile);
		}


	}
	
	

	
}
