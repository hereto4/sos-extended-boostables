package util.gui.misc;

import init.constant.C;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.SPRITE;

public class GGrid {
	
	private final ArrayList<RENDEROBJ> row;
	private int maxHeight = 0;
	
	private int startX;
	private int startY;
	private final int elements;
	private int width;
	private int marginY = 0;
	private int count;

	private DIR align = DIR.NW;
	public final GuiSection section;

	
	public GGrid(GuiSection section, int width, int elements, int x1, int y1) {
		this(section, width, elements, x1, y1, 0);
	}
	
	public GGrid(GuiSection section, int elements) {
		this(section, section.body().width(), elements, section.body().x1(), section.body().y1(), 0);
	}
	
	public GGrid(GuiSection section, int elements, int y1) {
		this(section, section.body().width(), elements, section.body().x1(), y1, 0);
	}
	
	public GGrid(GuiSection section, int width, int elements, int x1, int y1, int marginX) {
		this.startX = x1 + marginX;
		this.startY = y1;
		this.width = width-marginX*2;
		this.elements = elements;
		this.section = section;
		this.row = new ArrayList<>(elements);
	}
	
	public GGrid setStartY(int y1) {
		this.startY = y1;
		return this;
	}
	
	public GGrid setAlignment(DIR align) {
		this.align = align;
		return this;
	}
	
	public void setTile(RENDEROBJ o) {
		o.body().moveY2(startY-2*C.SG);
		o.body().moveCX(startX+width/2);
		section.add(o);
	}
	
	public void centered(RENDEROBJ o) {
		NL(8);
		o.body().moveY1(startY+4*C.SG);
		o.body().moveCX(startX+width/2);
		section.add(o);
		count = 0;
		row.clear();
		maxHeight = 0;
		this.startY = section.getLastY2();
	}
	
	public void add(RENDEROBJ o) {
		
		if (o.body().height() > maxHeight) {
			maxHeight = o.body().height();
			int x = 0;
			for (RENDEROBJ r : row) {
				if (r != null) {
					align(x++, r);
				}
			}
		}
		
		align(count, o);
		row.add(o);
		section.add(o);
		
		count++;
		if (count == row.max())
			NL();
	}
	
	private void align(int row, RENDEROBJ o) {
		int dw = width/elements;
		int x1 = startX + (row % elements) * dw;
		int y1 = startY;
		int dx = (int) Math.ceil((dw-o.body().width())/2.0);
		int dy = (maxHeight - o.body().height())/2;
		
		int cx = x1+dw/2;
		int cy = y1+maxHeight/2;
		
		cx += align.x()*dx;
		cy += align.y()*dy;
		
		o.body().moveC(cx, cy);
	}
	
	public void skip() {
		if (count == 0)
			return;
		count ++;
		if (count == row.max())
			NL();
	}
	
	public void add(SPRITE s) {
		add(new RENDEROBJ.Sprite(s));
	}
	
	public int sx(int i) {
		int dw = width/elements;
		return startX + (i % elements) * dw;
	}

	public void NL() {
		
		startY += maxHeight + marginY;
		count = 0;
		row.clear();
		maxHeight = 0;
		
	}
	
	public void NL(int margin) {
		
		startY += maxHeight + margin;
		count = 0;
		row.clear();
		maxHeight = 0;
		
	}
	
	public GGrid widthSet(int width) {
		this.width = width;
		return this;
	}
	
	public GGrid setMarginY(int y) {
		this.marginY = y;
		return this;
	}
	
	public GGrid incStartX(int x) {
		this.startX += x;
		return this;
	}
	
}
