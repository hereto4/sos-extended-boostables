package view.menu;

import init.constant.C;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Font;
import util.colors.GCOLOR;
import util.text.D;

public abstract class MenuScreen extends GuiSection{

	static CharSequence ¤¤back = "¤< back";
	static {
		D.ts(MenuScreen.class);
	}
	public static final RECTANGLE bounds = new Rec(1200, 600);
	public static RECTANGLE inner = new Rec(bounds.width()-50, bounds.height()-32).moveC(C.DIM().cX(), C.DIM().cY());
	
	private final GuiSection bottombutts = new GuiSection();
	
	public MenuScreen(CharSequence title, COLOR color) {
	
		body().set(bounds);
		body().centerIn(C.DIM());
		
		
		RENDEROBJ s = UI.decor().frame(this.body(), color);
		s.body().centerIn(this.body());
		add(s);
		
		
		
		s = UI.decor().decorate(title, color);
		s.body().centerIn(C.DIM());
		s.body().moveY2(getLastY1());
		add(s);
		
		
		
		ScreenButton b = new ScreenButton(UI.FONT().H1.getText(¤¤back)) {
			
			@Override
			protected void clickA() {
				MenuScreen.this.back();
			}
			
		};
		b.body().moveX2(body().x2()-20);
		b.body().moveY1(body().y1());
		add(b);
		addRelBody(14, DIR.S, bottombutts);
	}
	
	public void addButt(RENDEROBJ obj) {
		bottombutts.addRightC(24, obj);
		bottombutts.body().centerX(this);
	}
	
	protected abstract void back();
	
	public static class ScreenButton extends CLICKABLE.ClickableAbs {

		private final SPRITE s;
		
		public ScreenButton(CharSequence name){
			this((SPRITE) UI.FONT().H1.getText(name));
		}
		
		public ScreenButton(CharSequence name, Font f){
			this((SPRITE)f.getText(name));
		}
		
		public ScreenButton(SPRITE s) {
			this.s = s;
			body.setWidth(s.width()).setHeight(s.height());
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive,
				boolean isSelected, boolean isHovered) {
			if (!isActive)
				GCOLOR.T().INACTIVE.bind();
			else if (isHovered && isSelected)
				GCOLOR.T().HOVER_SELECTED.bind();
			else if (isHovered)
				GCOLOR.T().HOVERED.bind();
			else if (isSelected)
				GCOLOR.T().SELECTED.bind();
			else
				GCOLOR.T().CLICKABLE.bind();
			s.render(r, body);
			COLOR.unbind();

		}
	}
	
}
