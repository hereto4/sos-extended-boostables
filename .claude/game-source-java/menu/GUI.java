package menu;

import init.constant.C;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.ColorShifting;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.Text;
import util.colors.GCOLOR;
import util.data.INT.INTE;
import util.gui.misc.GText;
import util.text.D;
import view.menu.MenuScreen;

abstract class GUI{
	
	
	static RECTANGLE bounds;
	static RECTANGLE right;
	static RECTANGLE left;
	static int bottomMarginX;
	static int bottomY;
	static int margin;

	static COLOR labelColor = new ColorImp(127,127,80);
	
	static RECTANGLE inner;
	static CharSequence ¤¤back = "¤< back";
	
	static {
		D.ts(GUI.class);
	}
	
	static void init(RECTANGLE bounds){
		GUI.bounds = bounds;
		D.t(GUI.class);
		float width = bounds.width()/4;
		float height = bounds.height()/1.4f;
		float dist = bounds.width()/16;
		bottomMarginX = bounds.width()/6;
		
		Rec l = new Rec();
		l.setWidth(width);
		l.setHeight(height);
		l.moveX1(bounds.x1() + width - dist/2);
		l.moveY1(bounds.y1() + (bounds.height() - height)/2);
		left = l;
		
		Rec r = new Rec(l);
		r.moveX1(l.x2() + dist);
		r.moveY1(bounds.y1() + (bounds.height() - height)/2);
		right = r;
		
		
		margin = getSmallText("aaaaaaaaaaaaaaaaa").width();
		
		r = new Rec(bounds);
		r.incrW(-200);
		r.incrH(-100);
		r.centerIn(bounds);
		
		bottomY = bounds.y2() + 30;
		
		inner = r;
	}
	
	static class COLORS{
		
		static COLOR normal = COLOR.WHITE100;
		static COLOR hover = new ColorShifting(new ColorImp(127,127,65),
				new ColorImp(110,90,45));
		static COLOR selected = new ColorImp(127,127,65);
		static COLOR hover_selected = COLOR.GREEN100;
		static COLOR inactive =new ColorImp(112, 87, 60); //COLOR.BROWN; //72, 58, 33
		static COLOR menu = new ColorImp(230,220,220);
		static COLOR unclickable = new ColorImp(127,127,80);
		static COLOR copper = new ColorImp(127,127,100);
		static COLOR hoverable = new ColorImp(115,95,55);
		static COLOR label = new ColorImp(127,127,80); //105,65,7
		static COLOR portrait = new ColorImp(220,220,220);
		static COLOR error = new ColorImp(127,90,90);
	}
	
	static class Button extends CLICKABLE.ClickableAbs {

		private final SPRITE s;

		Button(SPRITE s) {
			this.s = s;
			body.setWidth(s.width()).setHeight(s.height());
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive,
				boolean isSelected, boolean isHovered) {
			if (!isActive)
				COLORS.inactive.bind();
			else if (isHovered && isSelected)
				COLORS.hover_selected.bind();
			else if (isHovered)
				COLORS.hover.bind();
			else if (isSelected)
				COLORS.selected.bind();
			else
				COLORS.normal.bind();
			s.render(r, body);
			COLOR.unbind();

		}
	}
	
	static class TEXT{
		
		
		
	}
	
	
	static CLICKABLE getNavButt(CharSequence name){
		return new Button(UI.FONT().H1.getText(name));
	}
	

	
	static SPRITE getSmallText(CharSequence name){
		return UI.FONT().M.getText(name);
	}
	
	static Text getSmallText(int width){
		return UI.FONT().M.getText(width);
	}
	
	static SPRITE getBigTexts(CharSequence name) {
		return UI.FONT().H1.getText(name);
	}
	
	static HOVERABLE getBigText(CharSequence name) {
		return new HOVERABLE.Sprite(UI.FONT().H1.getText(name), COLORS.label);
	}
	
	static CLICKABLE.ClickableAbs getSmallButt(String name){
		return new Button(UI.FONT().M.getText(name));
	}
	
	static CLICKABLE getBackArrow(){
		Button b = new Button(UI.FONT().H1.getText(¤¤back));
		b.body().moveX2(bounds.x2()-80);
		b.body().moveY2(bounds.y1()-25);
		return b;
	}
	
	static void addTitleText(GuiSection s, CharSequence title){
		
		HOVERABLE.Sprite r = new HOVERABLE.Sprite(UI.FONT().H1.getText((Object)title).toUpper(), COLORS.label);
		r.body().centerIn(bounds);
		r.body().moveY1((int) (left.y1() - r.body().height() - 10));
		s.add(r);
		
	}
	
	static abstract class OptionLine extends GuiSection{


		private final CLICKABLE left;
		private final CLICKABLE right;
		private GText value = new GText(UI.FONT().M, 16);
		
		OptionLine(INTE ii, CharSequence l) {
	
			body().setWidth(550);
			body().setHeight(1);
			
			
			GText label = new GText(UI.FONT().H2, l);
			label.color(COLORS.unclickable);
			left = new Button(getBigTexts("<<")) {
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
						boolean isHovered) {
					activeSet(ii.get() > ii.min());
					super.render(r, ds, activeIs(), isSelected, isHovered);
				}
			};
			left.clickActionSet(new ACTION() {
				@Override
				public void exe() {
					ii.inc(-1);
				}
			});
			//left.bodyM().moveX1(bodyM().gX2() + margin/6);
			add(left, 550/2-margin/20-left.body().width(), 0);
			
			right = new Button(getBigTexts(">>")) {
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
						boolean isHovered) {
					activeSet(ii.get() < ii.max());
					super.render(r, ds, activeIs(), isSelected, isHovered);
				}
			};
			right.clickActionSet(new ACTION() {
				@Override
				public void exe() {
					ii.inc(1);
				}
				
			});
			//right.bodyM().moveX1(bodyM().gX2() + 5);
			addRightC(margin/10, right);
			
			addCentredY(label, left.body().x1()-label.width()-7);
			
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			super.render(r, ds);
			
			Str.TMP.clear();
			value.clear();
			setValue(value);
			value.renderCY(r, right.body().x2() + 7, body().cY());
			COLORS.unclickable.bind();
			
			COLOR.unbind();
			
		}
	
		
		
		protected abstract void setValue(GText str);
		
	}
	
	static class CheckBox extends CLICKABLE.ClickableAbs {
		
		private final SPRITE ss;
		private final GText name;
		
		public CheckBox(CharSequence name) {
			this.name = new GText(UI.FONT().M, name);
			ss = new SPRITE.Imp(Icon.S + 8) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					
					boolean isActive = activeIs();
					isActive &= selectedIs() || hoveredIs();
					
					if (isActive)
						COLOR.WHITE100.renderFrame(r, X1, X2, Y1, Y2, 0, 2);
					else
						COLORS.inactive.renderFrame(r, X1, X2, Y1, Y2, 0, 2);
					int cx = X1+(X2-X1)/2;
					int cy = Y1+(Y2-Y1)/2;
					if (selectedIs()) {
						GCOLOR.UI().GOOD.hovered.bind();
						UI.icons().s.allRight.renderC(r, cx, cy);
					}else {
						GCOLOR.UI().BAD.hovered.bind();
						UI.icons().s.cancel.renderC(r,cx, cy);
					}
					COLOR.unbind();
				}
			};
			
			body.setDim(250, ss.height()+8);
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			ss.renderCY(r, body().x1(), body().cY());
			
			isActive &= isSelected || isHovered;
			
			if (!isActive)
				name.color(COLORS.inactive);
			else if (isHovered && isSelected)
				name.color(COLORS.hover_selected);
			else if (isHovered)
				name.color(COLORS.hover);
			else if (isSelected)
				name.color(COLORS.selected);
			else
				name.color(COLORS.normal);
			
			name.renderCY(r, body().x1()+8+ss.width(), body().cY());
			
		}
	}
	
	public static class Shadower extends GuiSection{

		private static final Rec shadow = new Rec(MenuScreen.bounds.width(), MenuScreen.bounds.height()-50).moveC(C.DIM().cX(), C.DIM().cY());
		public Shadower() {

			
		}
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			Background.shadow = shadow;
			super.render(r, ds);
		}
		
		public static void ren(SPRITE_RENDERER r, float ds) {
			Background.shadow = shadow;
		}
	}

	
}
