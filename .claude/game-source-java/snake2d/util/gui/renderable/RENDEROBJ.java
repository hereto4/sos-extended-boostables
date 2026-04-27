package snake2d.util.gui.renderable;

import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.BODY_HOLDERE;
import snake2d.util.datatypes.DIMENSION;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.datatypes.RecFacade;
import snake2d.util.sprite.SPRITE;

public interface RENDEROBJ extends BODY_HOLDERE {

	public void render(SPRITE_RENDERER r, float ds);
	public boolean visableIs();
	public RENDEROBJ visableSet(boolean yes);
	
	public default SPRITE asSprite() {
		return new SPRITE.Imp(body().width(), body().height()) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				body().moveX1Y1(X1, Y1);
				RENDEROBJ.this.render(r, 0);
			}
		};
	}
	
	public abstract class RenderImp implements RENDEROBJ {
		
		protected final Rec body = new Rec();
		private boolean isVisable = true;
		
		public RenderImp(int width, int height){
			body.setDim(width, height);
		}
		
		public RenderImp() {
			
		}
		
		public RenderImp(int size) {
			this(size,size);
		}

		@Override
		public RecFacade body() {
			return body;
		}
		@Override
		public boolean visableIs() {
			return isVisable;
		}
		@Override
		public RenderImp visableSet(boolean yes) {
			isVisable = yes;
			return this;
		}
		
	}
	
	public final class RenderDummy implements RENDEROBJ {
		
		protected final Rec body = new Rec();
		
		public RenderDummy(int width, int height){
			body.setDim(width, height);
		}
		
		public RenderDummy(int size) {
			this(size,size);
		}

		@Override
		public RecFacade body() {
			return body;
		}
		@Override
		public boolean visableIs() {
			return true;
		}
		@Override
		public RenderDummy visableSet(boolean yes) {
			return this;
		}

		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	public class Sprite extends RenderImp implements DIMENSION{

		private SPRITE sprite;
		private COLOR color = COLOR.WHITE100;
		private DIR align = DIR.NW;
		
		public Sprite() {
			
		}
		
		public Sprite(int dim) {
			body.setDim(dim);
		}
		
		public Sprite(int width, int height) {
			body.setDim(width, height);
		}
		
		public Sprite(SPRITE sprite) {
			setSprite(sprite);
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			
			if (sprite != null) {
				adjust();
				color.bind();
				sprite.render(r, body.x1(), body.y1());
				COLOR.unbind();
			}
				
			
		}
		
		public Sprite setAlign(DIR d) {
			this.align = d;
			return this;
		}
		
		public RENDEROBJ setSprite(SPRITE sprite) {
			this.sprite = sprite;
			if (sprite != null)
				adjust();
			return this;
		}
		
		private void adjust() {
			if (body.width() != sprite.width() || body.height() != sprite.height()) {
				align.reposition(body, sprite.width(), sprite.height());
			}
		}
		
		public Sprite setColor(COLOR color) {
			this.color = color;
			return this;
		}

		@Override
		public int width() {
			return sprite.width();
		}

		@Override
		public int height() {
			return sprite.height();
		}
		
		@Override
		public Rec body() {
			return body;
		}

	}

	
}
