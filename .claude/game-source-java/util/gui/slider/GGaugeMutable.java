package util.gui.slider;

import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Font;
import util.colors.GCOLOR;
import util.data.DOUBLE;
import util.data.DOUBLE.DOUBLE_MUTABLE;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.info.INFO;
import view.main.VIEW;

public class GGaugeMutable extends CLICKABLE.ClickableAbs{

	private static Font f = UI.FONT().M;
	private static GText text = new GText(UI.FONT().S, 100);
	private boolean hideInfo;
	private static Rec rTmp = new Rec();
	
	private final DOUBLE_MUTABLE d;
	private boolean clicked = false;
	private static final ColorImp col = new ColorImp();
	
	public GGaugeMutable(DOUBLE_MUTABLE d, int width) {
		if (f != UI.FONT().M )
			text = new GText(UI.FONT().S, 100);
		this.d = d;
		body.setDim(width, Icon.M);
		repetativeSet(true);
	}
	
	@Override
	protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
		
		clicked &= MButt.LEFT.isDown();
		
		
		if (isHovered) {
			GText t =  (VIEW.hoverBox()).text();
			t.setFont(UI.FONT().S);
			setInfo(d, t);
			if (t.length() > 0) {
				VIEW.hoverBox().add(t);
				VIEW.hoverBox().NL();
			}
		}
		
		rTmp.set(body());
		
		if (!hideInfo){
			text.clear();
			int w = setInfo(d, text);
			if (w > 0) {
				rTmp.moveX1(rTmp.x2()-w);
				rTmp.setWidth(w);
				renderBG(r, rTmp);
				text.adjustWidth();
				text.renderC(r, rTmp);
				rTmp.moveX1(body().x1());
				rTmp.setWidth(body().width()-w+1);
			}
		}
		
		
		
		boolean big = rTmp.width() > 100;
		if (isActive && big) {
			int w = rTmp.width();
			rTmp.set(rTmp.x1(), rTmp.x1()+16, rTmp.y1(), rTmp.y2());
			renderBG(r, rTmp);
			SPRITES.icons().s.minifier.renderC(r, rTmp);
			rTmp.set(rTmp.x1()+w-16, rTmp.x1()+w, rTmp.y1(), rTmp.y2());
			renderBG(r, rTmp);
			SPRITES.icons().s.magnifier.renderC(r, rTmp);
			rTmp.set(body());
			rTmp.setWidth(w-32);
			rTmp.incrX(16);
		}

		
		if (clicked) {
			set(rTmp);
		}
		
		renderBG(r, rTmp);
		

		if (isActive && !big && leftHovered(rTmp)) {
			renderColor(r, GCOLOR.UI().bgHov().hovered, rTmp.x1(), buttonX1(rTmp)+Icon.M/2);
			SPRITES.icons().s.minus.render(r, rTmp.x1(), rTmp.y1()+4);
		}else {
			bad2Good(col, d.getD());
			renderColor(r, col, rTmp.x1(), buttonX1(rTmp)+Icon.M/2);
		}
		
		if (isActive && !big && rightHovered(rTmp)) {
			renderColor(r, GCOLOR.UI().bgHov().hovered, buttonX1(rTmp)+Icon.M/2, rTmp.x2()-3);
			SPRITES.icons().s.plus.render(r, rTmp.x2()-Icon.S, rTmp.y1()+4);
		}else {
			
		}
		
		if (!isActive)
			return;
		
		int bx1 = buttonX1(rTmp);
		SPRITES.icons().m.circle_frame.render(r, bx1, body().y1());
		
		bad2Good(col, d.getD());
		

		
		if (buttonIsHovered(rTmp)) {
			col.shadeSelf(1.4);
		}
		
		col.bind();
		SPRITES.icons().m.circle_inner.render(r, bx1, body().y1());
		COLOR.unbind();
		
	}
	
	public static void bad2Good(ColorImp c, double d) {
		if (d < 0)
			d = 0;
		if (d > 1)
			d = 1;
		double r = (d > 0.5) ? (1.0-(d-0.5)*2) : 1;
		double g = (d < 0.5) ? d*2 : 1;
		c.set(30+(int)(70*r), 30+(int)(70*g), 30);
	}
	
	
	private void renderBG(SPRITE_RENDERER r, RECTANGLE rec) {
		GCOLOR.UI().border().render(r, rec, -1);
		GCOLOR.UI().bg().render(r, rec, -2);
	}
	
	private void renderColor(SPRITE_RENDERER r, COLOR c, int x1, int x2) {
		ColorImp.TMP.set(c);
		col.set(ColorImp.TMP);
		col.shadeSelf(0.5);
		col.render(r, x1+2, x2, body().y1()+3, body().y2()-3);
		col.set(ColorImp.TMP);
		col.render(r, x1+2, x2, body().y1()+4, body().y2()-4);
	}
	
	int adjustWidth(int width, DOUBLE d){
		return (int) (Icon.M/2 + (width-Icon.M)*d.getD());
	}
	
	protected void setColor(DOUBLE d, ColorImp imp, boolean hovered) {
		bad2Good(imp, d.getD());
		if (hovered)
			imp.shadeSelf(1.4);
	}
	
	@Override
	public boolean hover(COORDINATE mCoo) {
		return super.hover(mCoo);
	}
	
	private int buttonX1(RECTANGLE body) {
		int w = body.width()-Icon.M;
		return (int) (body.x1() + d.getD()*w);
	}
	
	private boolean leftHovered(RECTANGLE body) {
		COORDINATE c = VIEW.mouse();
		if (c.isWithinRec(body)) {
			return c.x() < buttonX1(body);
		}
		return false;
	}
	
	private boolean rightHovered(RECTANGLE body) {
		COORDINATE c = VIEW.mouse();
		if (c.isWithinRec(body)) {
			return c.x() > buttonX1(body)+Icon.M;
		}
		return false;
	}
	
	private boolean buttonIsHovered(RECTANGLE body) {
		COORDINATE c = VIEW.mouse();
		if (c.isWithinRec(body)) {
			int x1 =  buttonX1(body);
			return c.x() > x1 && c.x() < x1+Icon.M;
		}
		return false;
	}
	
	@Override
	protected final void clickA() {
		rTmp.set(body());
		if (!hideInfo) {
			rTmp.incrW(-setInfo(d, text));
		}
		if (rTmp.width() > 100) {
			rTmp.incrX(16);
			rTmp.incrW(-32);
		}

		if (leftHovered(rTmp) || VIEW.mouse().x() < rTmp.x1()) {
			d.incD(-Double.MIN_VALUE);
			
		}else if (rightHovered(rTmp) || VIEW.mouse().x() >= rTmp.x2()) {
			d.incD(Double.MIN_VALUE);
			
		}else {
			if (VIEW.mouse().isWithinRec(rTmp)) {
				clicked = true;
				
				set(rTmp);
			}
		}
	}
	
	private void set(RECTANGLE body) {
		COORDINATE c = VIEW.mouse();
		double w = body.width()-Icon.M;
		double de = c.x()-body.x1()-Icon.M/2;
		de = CLAMP.d(de/w, 0, 1);
		d.setD(de);
	}

	protected int setInfo(DOUBLE d, GText text) {
		GFORMAT.perc(text, d.getD());
		return text.getFont().height()*3;
	}
	
	public GGaugeMutable hideInfo() {
		hideInfo = true;
		return this;
	}
	
	@Override
	public void hoverInfoGet(GUI_BOX text) {
		
		super.hoverInfoGet(text);
	}

	public RENDEROBJ hoverInfoSet(INFO info) {
		hoverInfoSet(info.desc);
		hoverTitleSet(info.name);
		return this;
	}

}
