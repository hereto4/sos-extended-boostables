package util.gui.common;

import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import util.gui.misc.GHeader;
import util.gui.misc.GText;
import util.info.INFO;

public interface TITLEABLE extends SPRITE{

	public default HOVERABLE hv(CharSequence name) {
		return new GHeader.HeaderVertical(name, this);
	}
	
	public default HOVERABLE hv(CharSequence name, CharSequence desc) {
		return new GHeader.HeaderVertical(name, this).hoverInfoSet(desc);
	}
	
	public default HOVERABLE hv(INFO info) {
		return new GHeader.HeaderVertical(info.name, this).hoverTitleSet(info.name).hoverInfoSet(info.desc);
	}
	
	public default HOVERABLE hv(SPRITE name) {
		return new GHeader.HeaderVertical(name, this);
	}
	
	public default HOVERABLE hh(CharSequence name) {
		return new GHeader.HeaderHorizontal(name, this);
	}
	
	public default HOVERABLE hhw(CharSequence name, int trail) {
		GHeader.HeaderHorizontal h = new GHeader.HeaderHorizontal(name, this);
		h.body().incrW(trail);
		return h;
	}
	
	public default HOVERABLE hhw(SPRITE name, int trail) {
		GHeader.HeaderHorizontal h = new GHeader.HeaderHorizontal(name, this);
		h.body().incrW(trail);
		return h;
	}
	
	public default HOVERABLE hh(INFO info) {
		return new GHeader.HeaderHorizontal(info.name, this).hoverInfoSet(info.desc);
	}
	
	public default HOVERABLE hh(SPRITE name) {
		return new GHeader.HeaderHorizontal(name, this);
	}
	
	public default HOVERABLE hh(SPRITE name, int width) {
		return new GHeader.HeaderHorizontal(name, this, width);
	}
	
	public default  GHeader.HeaderHorizontal hh(CharSequence name, int width) {

		return new GHeader.HeaderHorizontal(name, this, width);
	}
	
	public default  GHeader.HeaderHorizontal hh(SPRITE icon, CharSequence name, int width) {
		
		final GText t = new GText(UI.FONT().S, name).lablify();
		final SPRITE ii = icon.resized(t.height());
		SPRITE sp = new SPRITE.Imp(ii.width() + 4 + t.width(), t.height()) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				icon.render(r, X1, Y1);
				t.render(r, X1+t.height()+4, Y1);
				
			}
		};
		return new GHeader.HeaderHorizontal(sp, this, width);
	}
	
	public default GHeader.HeaderHorizontal hh(CharSequence name, CharSequence desc, int width) {
		GHeader.HeaderHorizontal h = new GHeader.HeaderHorizontal(name, this, width);
		h.hoverTitleSet(name);
		h.hoverInfoSet(desc);
		return h;
	}
	
	public default GHeader.HeaderHorizontal hh(CharSequence name, CharSequence desc) {
		
		GHeader.HeaderHorizontal h = new GHeader.HeaderHorizontal(name, this);
		h.hoverTitleSet(name);
		h.hoverInfoSet(desc);
		return h;
	}
	
	public default void hoverInfoGet(GBox b) {
		
	}
	
}
