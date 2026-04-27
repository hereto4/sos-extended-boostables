package view.main;

import init.sprite.UI.UI;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.sprite.SPRITE;

public final class Mouse implements COORDINATE{

	private final Coo coo = new Coo();
	private final SPRITE sprites;
	private boolean hidden = false;
	private SPRITE overlay = null; 
	
	Mouse(){
		sprites = UI.decor().mouse;
	}
	
	void render(Renderer r, float ds){
		
		if (hidden) {
			hidden = false;
			return;
		}
		
		if (overlay == null && CLICKABLE.ClickableAbs.clickableHovered) {
			UI.decor().mouseHov.render(r, coo.x(), coo.y());
		}else
			sprites.render(r, coo.x(), coo.y());
		if (overlay != null){
			overlay.render(r, coo.x()+10, coo.y());
		}
		overlay = null;
		CLICKABLE.ClickableAbs.clickableHovered = false;
	}
	
	public void setReplacement(SPRITE o){
		overlay = o;
	}

	public void hide(boolean hide){
		hidden = hide;
	}

	@Override
	public int x() {
		return coo.x();
	}

	@Override
	public int y() {
		return coo.y();
	}

	@Override
	public boolean isWithinRec(RECTANGLE shape) {
		return coo.isWithinRec(shape);
	}

	Coo getCoo() {
		return coo;
	}

}
