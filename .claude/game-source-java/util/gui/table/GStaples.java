package util.gui.table;

import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.Hoverable.HOVERABLE.HoverableAbs;
import snake2d.util.misc.CLAMP;
import util.colors.GCOLOR;
import util.gui.misc.GBox;

public abstract class GStaples extends HoverableAbs{

	private final int amount;
	private int hoveredI = -1;
	private final ColorImp color = new ColorImp();
	private boolean negative;
	private boolean border = true;
	private boolean backGround = true;
	private boolean normalize = true;
	private boolean normalizePlus = false;
	public GStaples(int amount){
		this(amount, false);
	}
	
	public GStaples(int amount, boolean negative){
		this.amount = amount;
		this.negative = negative;
	}
	
	public void border(boolean border) {
		this.border = border;
	}
	
	public void background(boolean border) {
		this.backGround = border;
	}
	
	public void normalize(boolean n) {
		normalize = n;
	}
	
	public void normalizePlus(boolean n) {
		normalizePlus = n;
	}
	
	
	@Override
	protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
		
		int w = sw();
		if (w < 3)
			return;
		if (body().height()<3)
			return;
		
		
		int x1 = body().x1()+(body().width()-amount*sw())/2;
		int y1 = body().y1();
		int y2 = body().y2();
		
		if (border)
			GCOLOR.UI().border().render(r, x1-1, x1+amount*sw()+1, y1-1, y2+1);
		
		
		double max = 0;
		double min = 0;
		double mid = 0;
		if (negative) {
			min = -1;
		}
		
		if (normalizePlus) {
			min = Double.MAX_VALUE;
			max = -Double.MAX_VALUE;
			for (int i = 0; i < amount; i++) {
				double v = getValue(i);
				max = Math.max(max, v);
				min = Math.min(min, v);
			}
			
			if (max == 0)
				max = 1;
		}else if (normalize) {
			min = Double.MAX_VALUE;
			max = -Double.MAX_VALUE;
			for (int i = 0; i < amount; i++) {
				double v = getValue(i);
				max = Math.max(max, v);
				min = Math.min(min, v);
			}
			min /= 2;
			if (max == 0)
				max = 1;
		}else {
			max = 1;
			min = 0;
		}
		
		if (negative) {
			int cy = body().cY();
			
			for (int i = 0; i < amount; i++) {
				int x = x1 + i*w;
				double v = getValue(i);
				setColorBg(color, i, v);
				if (backGround && (!isHovered || !hovered(i)))
					color.render(r, x, x+w, y1, y2);
				
				
				v = (mid + v-min)/(max-min);
				v = CLAMP.d(v, -1 ,1);
				int h = (int) Math.ceil(Math.abs(v)*(body().height()/2));
				if (h > 0) {
					
					setColor(color, i, v);
					if (isHovered && hovered(i))
						color.shadeSelf(1.5);
					else
						color.shadeSelf(0.5);
					
					if (v < 0) {
						int y22 = cy+h;
						color.render(r, x, x+w, cy, y22);
						setColor(color, i, v);
						color.render(r, x+1, x+w-1, cy, y22-1);
					}else {
						int y11 = cy-h;
						color.render(r, x, x+w, y11, cy);
						setColor(color, i, v);
						color.render(r, x+1, x+w-1, y11+1, cy);
					}
				}
			}
			color.set(GCOLOR.UI().border()).shadeSelf(0.75);
			color.render(r, body().x1(), body().x2(), cy, cy+1);
			
		}else {
			for (int i = 0; i < amount; i++) {
				int x = x1 + i*w;
				double v = getValue(i);
				setColorBg(color, i, v);
				if (backGround && (!isHovered || !hovered(i)))
					color.render(r, x, x+w, y1, y2);
				
				v = (mid + v-min)/(max-min);
				
				v = CLAMP.d(v, 0 ,1);
				int h = (int) Math.ceil(v*(body().height()));
				if (h > 0) {
					int y11 = y2-h;
					setColor(color, i, v);
					if (isHovered && hovered(i))
						color.shadeSelf(1.5).render(r, x, x+w, y11, y2);
					else
						color.shadeSelf(0.5).render(r, x, x+w, y11, y2);
					setColor(color, i, v);
					color.render(r, x+1, x+w-1, y11+1, y2);
					renderExtra(r, color, i, isHovered && hovered(i), v, x, x+w, y11, y2);
				}
				
				
			}
		}
		
		
		
	}
	
	protected void renderExtra(SPRITE_RENDERER r, COLOR color, int stapleI, boolean hovered, double value, int x1, int x2, int y1, int y2) {
		
	}
	
	protected abstract double getValue(int stapleI);

	protected abstract void hover(GBox box, int stapleI);
	
	public void setHovered(int i) {
		hoveredI = i;
		hoveredSet(hoveredI >= 0);
	}
	
	public int hoverI() {
		return hoveredI;
	}
	
	protected boolean hovered(int ii) {
		return ii == hoveredI;
	}

	protected void setColor(ColorImp c, int stapleI, double value) {
		c.set(GCOLOR.UI().SOSO.normal);
	}
	
	protected void setColorBg(ColorImp c, int stapleI, double value) {
		c.set(GCOLOR.UI().bg());
	}
	
	@Override
	public boolean hover(COORDINATE mCoo) {
		if (super.hover(mCoo)) {
			int x1 = body().x1()+(body().width()-amount*sw())/2;
			int x = mCoo.x()-x1;
			hoveredI = CLAMP.i(x/sw(), -1, amount-1);
			return true;
		}
		return false;
	}
	
	@Override
	public void hoverInfoGet(GUI_BOX text) {
		if (hoveredI != -1) {
			hover((GBox) text, hoveredI);
		}
		super.hoverInfoGet(text);
	}
	
	private int sw() {
		return (body().width()/amount);
	}
	
}
