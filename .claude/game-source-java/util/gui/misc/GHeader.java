package util.gui.misc;

import init.constant.C;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Font;
import util.gui.common.TITLEABLE;
import util.info.INFO;

public class GHeader extends HOVERABLE.HoverableAbs{

	protected SPRITE text;
	
	public GHeader(CharSequence name) {
		this.text = new GText(UI.FONT().H2, name).lablify();
		body.setHeight(text.height());
		body.setWidth(text.width());
	}
	
	public GHeader(CharSequence name, Font f) {
		this.text = new GText(f, name).lablify();
		body.setHeight(text.height());
		body.setWidth(text.width());
	}
	
	public GHeader(CharSequence name, int max) {
		if (name.length() > max)
			name = (""+name).substring(0, max-1) + ".";
		this.text = new GText(UI.FONT().H2, name).lablify();
		body.setHeight(text.height());
		body.setWidth(text.width());
	}
	
	public GHeader(INFO info) {
		this.text = new GText(UI.FONT().H2, info.name).lablify();
		body.setHeight(text.height());
		body.setWidth(text.width());
		hoverTitleSet(info.name);
		hoverInfoSet(info.desc);
	}
	
//	public GHeader(CharSequence name, int w) {
//		this.text = new GText(UI.FONT().H2, name).lablify();
//		body.setHeight(text.height());
//		body.setWidth(w);
//	}
	
	public GHeader(SPRITE name) {
		this.text = name;
		body.setHeight(text.height());
		body.setWidth(text.width());
	}
	
	public GHeader subify() {
		((GText)text).lablifySub();
		return this;
	}
	
	public void setSprite(SPRITE name) {
		this.text = name;
	}
	
	@Override
	protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
		if (text instanceof GStat)
			((GStat) text).adjust();
		text.render(r, body.x1(), body.y1());
	}
	
	public HOVERABLE hoverInfoSet(INFO i) {
		hoverTitleSet(i.name);
		hoverInfoSet(i.desc);
		return this;
	}

	public static class HeaderVertical extends GHeader {

		private final SPRITE s;
		
		public HeaderVertical(CharSequence name, SPRITE s) {
			super(name, s.height() <= 16 ? UI.FONT().S : UI.FONT().H2);
			this.s = s;
			body.setHeight(text.height()+C.SG + s.height());
			body.setWidth(text.width() > s.width() ? text.width() : s.width());
		}
		
		public HeaderVertical(SPRITE name, SPRITE s) {
			super(name);
			this.s = s;
			body.setHeight(text.height()+C.SG + s.height());
			body.setWidth(text.width() > s.width() ? text.width() : s.width());
		}
		
		public HeaderVertical(CharSequence name, GStat s) {
			this(name, (SPRITE) s);
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			if (s instanceof GStat)
				((GStat) s).adjust();
			int cx = body.cX();
			body.setWidth(text.width() > s.width() ? text.width() : s.width());
			body.moveCX(cx);
			int dx = (body.width()-text.width())/2;
			text.render(r, body.x1()+dx, body.y1());

			dx = (body.width()-s.width())/2;
			s.render(r, body.x1()+dx, body.y1()+text.height()+C.SG);			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (s instanceof TITLEABLE)
				((TITLEABLE) s).hoverInfoGet((GBox)text);
			super.hoverInfoGet(text);
		}
	}
	
	public static class HeaderHorizontal extends GHeader {

		private final SPRITE s;
		private final int fixedWidth;
		

		public HeaderHorizontal(CharSequence name, SPRITE s) {
			super(name, s.height() <= 16 ? UI.FONT().S : UI.FONT().H2);
			this.s = s;
			body.setHeight(text.height() > s.height() ? text.height() : s.height());
			body.setWidth(text.width() + C.SG*6 + s.width());
			fixedWidth = -1;
		}
		
		public HeaderHorizontal(SPRITE name, SPRITE s) {
			super(name);
			this.s = s;
			body.setHeight(text.height() > s.height() ? text.height() : s.height());
			body.setWidth(text.width() + C.SG*6 + s.width());
			fixedWidth = -1;
		}

		public HeaderHorizontal(CharSequence name, SPRITE s, int width) {
			super(name, s.height() <= 16 ? UI.FONT().S : UI.FONT().H2);
			this.s = s;
			body.setHeight(text.height() > s.height() ? text.height() : s.height());
			body.setWidth(width+s.width());
			fixedWidth = width;
		}
		
		public HeaderHorizontal(SPRITE name, SPRITE s, int width) {
			super(name);
			this.s = s;
			body.setHeight(text.height() > s.height() ? text.height() : s.height());
			body.setWidth(width+32);
			fixedWidth = width;
		}
		
		public HeaderHorizontal(CharSequence name, GStat s) {
			this(s.statText.getFont() == UI.FONT().M ? (SPRITE) new GText(UI.FONT().H2, name).lablify() : (SPRITE) new GText(UI.FONT().H2, name).lablify(), s);
		}
		
		public HeaderHorizontal(CharSequence name, GStat s, int width) {
			this(s.statText.getFont() == UI.FONT().M ? (SPRITE) new GText(UI.FONT().H2, name).lablify() : (SPRITE) new GText(UI.FONT().H2, name).lablify(), s, width);
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			if (fixedWidth == -1)
				body.setWidth(text.width() + C.SG*6 + s.width());

			int dy = (body.height()-text.height())/2;
			text.render(r, body.x1(), body.y1()+dy);
			dy = (body.height()-s.height())/2;
			int x1 = body().x1();
			if (fixedWidth == -1) {
				body.setWidth(C.SG*6 + text.width() + s.width());
				x1 += C.SG*6 + text.width();
			}else {
				body.setWidth(fixedWidth + s.width());
				x1 += fixedWidth;
			}
			
			s.render(r, x1, body.y1()+dy);			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			if (s instanceof TITLEABLE)
				((TITLEABLE) s).hoverInfoGet((GBox)text);
			super.hoverInfoGet(text);
		}
		
		public HeaderHorizontal increaseWidth(int am) {
			body.incrW(am);
			return this;
		}

	}
	
	
	
	
}
