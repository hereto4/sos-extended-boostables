package util.gui.misc;

import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.sprite.TextureCoords;
import snake2d.util.sprite.text.Font;
import util.gui.common.TITLEABLE;

public abstract class GStat implements TITLEABLE{

	protected final GText statText;
	private boolean bg = false;
	
	public GStat() {
		this(64);
	}
	
	public GStat(Font f) {
		this(new GText(f, 64));
	}
	
	public GStat(int size) {
		this(new GText(UI.FONT().S, size));
	}
	
	public GStat increase() {
		statText.setFont(UI.FONT().M);
		return this;
	}
	
	public GStat decrease() {
		statText.setFont(UI.FONT().S);
		return this;
	}
	
	public GStat(GText text) {
		this.statText = text;
	}
	
	@Override
	public int width() {
		return statText.width();
	}

	@Override
	public int height() {
		return statText.height();
	}

	public abstract void update(GText text);
	
	public GStat bg() {
		this.bg = true;
		return this;
	}
	
	public void adjust() {
		statText.clear();
		update(statText);
		statText.adjustWidth();
	}
	
	public GStat setFont(Font f) {
		statText.setFont(f);
		return this;
	}
	
	@Override
	public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
		
		
		adjust();
		if (bg) {
			OPACITY.O50.bind();
			COLOR.BLACK.render(r, X1-1, X2+1, Y1-1, Y2+1);
			OPACITY.unbind();
		}
		statText.render(r, X1, X1+statText.width(), Y1, Y2);
		
	}

	@Override
	public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
		throw new RuntimeException();
	}
	
	public final HOVERABLE r(DIR alignment) {
		HOV h = new HOV();
		h.setSprite(this);
		h.setAlign(alignment);
		return h;
	}
	
	public final HOVERABLE r() {
		return r(DIR.NW);
	}
	
//	public final HOVERABLE hv(CharSequence name) {
//		if (statText.getFont() == UI.FONT().L)
//			return new GHeader.HeaderVertical((SPRITE) new GText(UI.FONT().H2, name).lablify(), this);
//		return new GHeader.HeaderVertical(name, this);
//	}
//	
//	public final HOVERABLE hv(CharSequence name, CharSequence desc) {
//		if (statText.getFont() == UI.FONT().L)
//			return new GHeader.HeaderVertical((SPRITE) new GText(UI.FONT().H2, name).lablify(), this).hoverInfoSet(desc);
//		return new GHeader.HeaderVertical(name, this).hoverInfoSet(desc);
//	}
//	
//	public final HOVERABLE hv(INFO info) {
//		if (statText.getFont() == UI.FONT().L)
//			return new GHeader.HeaderVertical((SPRITE) new GText(UI.FONT().H2, info.name).lablify(), this).hoverTitleSet(info.name).hoverInfoSet(info.desc);;
//		return new GHeader.HeaderVertical(info.name, this).hoverTitleSet(info.name).hoverInfoSet(info.desc);
//	}
//	
//	public final HOVERABLE hv(SPRITE name) {
//		return new GHeader.HeaderVertical(name, this);
//	}
//	
//	public final HOVERABLE hh(CharSequence name) {
//		if (statText.getFont() == UI.FONT().L)
//			return new GHeader.HeaderHorizontal((SPRITE) new GText(UI.FONT().H2, name).lablify(), this);
//		return new GHeader.HeaderHorizontal(name, this);
//	}
//	
//	public final HOVERABLE hh(INFO info) {
//		if (statText.getFont() == UI.FONT().L)
//			return new GHeader.HeaderHorizontal((SPRITE) new GText(UI.FONT().H2, info.name).lablify(), this).hoverTitleSet(info.name).hoverInfoSet(info.desc);;
//		return new GHeader.HeaderHorizontal(info.name, this).hoverInfoSet(info.desc);
//	}
//	
//	public final HOVERABLE hh(SPRITE name) {
//		return new GHeader.HeaderHorizontal(name, this);
//	}
//	
//	public final HOVERABLE hh(SPRITE name, int width) {
//		return new GHeader.HeaderHorizontal(name, this, width);
//	}
//	
//	public final  GHeader.HeaderHorizontal hh(CharSequence name, int width) {
//		if (statText.getFont() == UI.FONT().L)
//			return new GHeader.HeaderHorizontal((SPRITE) new GText(UI.FONT().H2, name).lablify(), this, width);
//		
//		return new GHeader.HeaderHorizontal(name, this, width);
//	}
//	
//	public final HOVERABLE hh(CharSequence name, CharSequence desc, int width) {
//		if (statText.getFont() == UI.FONT().L)
//			return new GHeader.HeaderHorizontal((SPRITE) new GText(UI.FONT().H2, name).lablify(), this, width);
//		
//		return new GHeader.HeaderHorizontal(name, this, width).hoverInfoSet(desc);
//	}
//	
//	public final HOVERABLE hh(CharSequence name, CharSequence desc) {
//		if (statText.getFont() == UI.FONT().L)
//			return new GHeader.HeaderHorizontal((SPRITE) new GText(UI.FONT().H2, name).lablify(), this);
//		
//		return new GHeader.HeaderHorizontal(name, this).hoverInfoSet(desc);
//	}
	
	private static class HOV extends HOVERABLE.Sprite {
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			((GStat) sprite).adjust();
			super.render(r, ds, isHovered);
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			((GStat) sprite).hoverInfoGet((GBox)text);
			super.hoverInfoGet(text);
		}
		
	}
	

}
