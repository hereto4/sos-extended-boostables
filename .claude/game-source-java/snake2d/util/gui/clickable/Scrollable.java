package snake2d.util.gui.clickable;

import snake2d.MButt;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;

public abstract class Scrollable {

	private final GuiSection section = new GuiSection() {
		
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			if (nrOfElements != nrOFEntries()) {
				move(itemTop);
			}
			
			super.render(r, ds);
		};
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			boolean ret = super.hover(mCoo);
			if (hoveredIs()) {
				double dw =  MButt.clearWheelSpin();
				if (nrOfElements != nrOFEntries() || (hoveredIs() && dw != 0)) {
					move(itemTop + (int) (-dw));
				}	
			}
			return ret;
		};
		
		
	};
	private final GuiSection elements = new GuiSection();
	private int itemTop = -1;
	private int nrOfElements = 0;
	private final int elementsY;
	private final int offY1;
	private final int width;
	private final ScrollRow[] rows;
	
	
	public Scrollable(RENDEROBJ title, ScrollRow...rows) {
		int h = 0;
		int w = 0;
		for (ScrollRow s : rows) {
			h += s.body().height();
			if (s.body().width() > w)
				w = s.body().width();
		}
		this.width = w;
		this.rows = rows;
		int height = h;

		//this.elementHeight = elementHeight;
		this.elementsY = rows.length;
		if (width <= 0 || height <= 0)
			throw new RuntimeException(width + " " + height + " " + rows.length);
		
		this.elements.body().setDim(width, height);
		section.add(this.elements);

		
		if (title != null) {
			title.body().centerX(section);
			title.body().moveY2(section.body().y1());
			section.add(title);
			offY1 = title.body().height();
		}else
			offY1 = 0;
		
		nrOfElements = -1;
		itemTop = 0;
		
		
	}
	
	private void move(int first) {
		if (first >= nrOFEntries()-elementsY)
			first = nrOFEntries()-elementsY;
		
		if (first < 0)
			first = 0;
		
		if (itemTop == first && nrOfElements == nrOFEntries())
			return;
		
		nrOfElements = nrOFEntries();
		itemTop = first;
		elements.clear();
		
		int real = itemTop;
		int virtual = 0;
		
		int y1 = elements.body().y1();
		
		while(real < nrOFEntries() && virtual < elementsY) {
			RENDEROBJ r = getElement(virtual, real);
			real ++;
			if (r != null) {
				r.body().moveX1(elements.body().x1());
				r.body().moveY1(y1);
				elements.add(r);
				y1 += r.body().height();
				virtual ++;
			}
		}
		elements.body().moveX1(section.body().x1());
		elements.body().moveY1(section.body().y1()+offY1);
	}
	
	public abstract int nrOFEntries();
	
	public final RENDEROBJ getElement(int virtual, int real) {
		rows[virtual].init(real);
		return rows[virtual];
	}
	
	public final GuiSection getView() {
		return section;
	}
	
	public interface ScrollRow extends RENDEROBJ {
		
		void init(int index);

		public static class ScrollRowImp extends GuiSection implements ScrollRow {
			
			@Override
			public void init(int index) {
				// TODO Auto-generated method stub
				
			}
			
		}
	}


	public int min() {
		return 0;
	}


	public int max() {
		return nrOFEntries()-rows.length;
	}


	public int get() {
		return itemTop;
	}


	public void set(int t) {
		move(t);
		nrOfElements = nrOFEntries();
	}
	
	
}
