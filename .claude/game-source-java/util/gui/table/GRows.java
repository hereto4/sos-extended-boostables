package util.gui.table;

import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;

public class GRows {

	private GuiSection s = null;
	private final LinkedList<RENDEROBJ> rows = new LinkedList<>();
	private int ii = 0;
	private final int max;
	private int pad = 0;
	private int minDist = 0;
	
	public GRows(int rowSize) {
		this.max = rowSize;
	}
	
	public GRows setPad(int pad) {
		this.pad = pad;
		return this;
	}
	
	public GRows setMin(int min) {
		this.minDist = min;
		return this;
	}
	
	public void add(RENDEROBJ obj) {
		
		if (ii % max == 0) {
			s = new GuiSection();
			rows.add(s);
			ii = 0;
		}
		int p = minDist-obj.body().width();
		if (pad > p)
			p = pad;
		s.addRight(p, obj);
		
		ii++;
	}
	
	public void nl() {
		if (s == null || s.elements().size() == 0)
			return;
		s = new GuiSection();
		rows.add(s);
		ii = 0;
	}
	
	public int height() {
		int h = 0;
		for (RENDEROBJ o : rows)
			h += o.body().height();
		return h;
	}
	
	public LIST<RENDEROBJ> rows(){
		for (RENDEROBJ rr : rows) {
			GuiSection s = (GuiSection) rr;
			if (s.getLast().width() < minDist) {
				s.body().incrW(minDist-s.getLast().width());
			}
		}
		return rows;
	}
	
	public LIST<RENDEROBJ> rowsCentered(int width){
		for (RENDEROBJ rr : rows()) {
			GuiSection s = (GuiSection) rr;
			if (s.body().width() < width)
				s.pad((width-s.body().width())/2, 0);
		}
		return rows;
	}
	
}
