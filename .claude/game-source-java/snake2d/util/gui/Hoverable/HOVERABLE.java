package snake2d.util.gui.Hoverable;

import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIMENSION;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.datatypes.RecFacade;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;


public interface HOVERABLE extends RENDEROBJ{
	
	/**
	 * 
	 * @param mouseCoo
	 * @return true if hovered
	 */
	public boolean hover(COORDINATE mCoo);
	/**
	 * 
	 * @return hovered
	 */
	public boolean hoveredIs();
	/**
	 * 
	 * @param mouseCoo
	 * @return true if hovered
	 */
	public void hoverInfoGet(GUI_BOX text);

	@Override
	public HOVERABLE visableSet(boolean yes);
	
	/**
	 * 
	 * @param s
	 * @return
	 */
	public HOVERABLE hoverInfoSet(CharSequence s);
	
	public HOVERABLE hoverTitleSet(CharSequence s);
	
	public static abstract class HoverableAbs extends RENDEROBJ.RenderImp implements HOVERABLE{

		protected final Rec body = new Rec();
		protected boolean isHovered = false;
		private CharSequence hoverInfo = null;
		private CharSequence title = null;

		public HoverableAbs() {
			
		}
		
		public HoverableAbs(int width, int height) {
			body.setDim(width, height);
		}
		
		public HoverableAbs(int dim) {
			body.setDim(dim);
		}
		
		public HoverableAbs(DIMENSION dim) {
			body.setDim(dim.width(), dim.height());
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (!visableIs())
				return false;
			return isHovered = mCoo.isWithinRec(body);
		}

		@Override
		public boolean hoveredIs() {
			return isHovered;
		}

		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (hoverInfo != null){
				text.text(hoverInfo);
			}
			if (title != null)
				text.title(title);
		}

		@Override
		public RecFacade body() {
			return body;
		}

		@Override
		public HoverableAbs visableSet(boolean yes) {
			super.visableSet(yes);
			return this;
		}

		@Override
		public HOVERABLE hoverInfoSet(CharSequence s) {
			this.hoverInfo = s;
			return this;
		}
		
		@Override
		public final void render(SPRITE_RENDERER r, float ds) {
			if (visableIs())
				render(r, ds, isHovered);
			isHovered = false;
			
		}
		
		protected abstract void render(SPRITE_RENDERER r, float ds, boolean isHovered);
		
		public void hoveredSet(boolean h) {
			isHovered = h;
		}
		
		@Override
		public HOVERABLE hoverTitleSet(CharSequence s) {
			this.title = s;
			return this;
		}
		
	}
	
	public class Sprite extends HoverableAbs {

		protected SPRITE sprite;
		protected COLOR color = COLOR.WHITE100;
		private DIR align = DIR.NW;
		
		public Sprite() {
			
		}
		
		public Sprite(int dim) {
			body.setDim(dim);
		}
		
		public Sprite(SPRITE sprite) {
			setSprite(sprite);
		}
		
		public Sprite(SPRITE sprite, COLOR c) {
			setSprite(sprite);
			color = c;
		}
		
		public HOVERABLE.Sprite setAlign(DIR d) {
			this.align = d;
			return this;
		}
		
		public void setSprite(SPRITE sprite) {
			this.sprite = sprite;
			adjust();
			
		}
		
		protected void adjust() {
			if (body.width() != sprite.width() || body.height() != sprite.height()) {
				align.reposition(body, sprite.width(), sprite.height());
			}
		}
		
		public void replaceSprite(SPRITE newSprite, DIR d){
			this.sprite = newSprite;
			if (sprite == null){
				d.reposition(body, 0, 0);
			}else{
				d.reposition(body, newSprite.width(), newSprite.height());
			}
		}
		
//		
		public void setColor(COLOR color) {
			this.color = color;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			color.bind();
			adjust();
			sprite.render(r, body.x1(), body.y1());
			COLOR.unbind();
			OPACITY.unbind();
			
		}
		
		
	}
	
}


