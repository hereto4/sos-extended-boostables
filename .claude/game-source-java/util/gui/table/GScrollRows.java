package util.gui.table;

import snake2d.MButt;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import util.data.INT.INTE;
import util.gui.slider.GSliderVer;

public class GScrollRows {

	private final RENDEROBJ[] rows;
	private final ArrayList<RENDEROBJ> current;
	private final GuiSection srows = new GuiSection();
	private final GuiSection section = new GuiSection() {
		
		@Override
		public void render(snake2d.SPRITE_RENDERER r, float ds) {

			if (hoveredIs()) {
				double d = MButt.clearWheelSpin();
				if (d > 0) {
					first--;
				}else if (d < 0) {
					first++;
				}
			}
			init();
			super.render(r, ds);
		};
	};
	private int first = 0;
	private int last;
	
	
	public GScrollRows(Iterable<? extends RENDEROBJ> rows, int height){
		this(convert(rows), height, 0);
	}
	
	public GScrollRows(Iterable<RENDEROBJ> rows, int height, int width){
		this(convert(rows), height, width-GSliderVer.WIDTH());
	}
	
	public GScrollRows(Iterable<RENDEROBJ> rows, int height, int width, boolean slide){
		this(convert(rows), height, width-GSliderVer.WIDTH(), slide);
	}
	
	public GScrollRows(RENDEROBJ[] renrows, int height, int width){
		this(renrows, height, width, true);
	}
	
	public GScrollRows(RENDEROBJ[] renrows, int height, int width, boolean slide){
		this.rows = renrows;
		this.current = new ArrayList<RENDEROBJ>(renrows.length);
		section.body().setHeight(height);
		int w = width;
		for (RENDEROBJ r : rows)
			if (r.body().width() > w)
				w = r.body().width();
		section.body().setWidth(w);
		if (slide) {
			GSliderVer slider = new GSliderVer(target, height);
			section.add(slider, section.body().x2(), section.body().y1());
		}
		srows.body().moveX1Y1(section.body());
		section.add(srows);
	}

	
	private static RENDEROBJ[] convert(Iterable<? extends RENDEROBJ> rows) {
		int size = 0;
		for (@SuppressWarnings("unused") RENDEROBJ r : rows)
			size++;
		RENDEROBJ[] rs = new RENDEROBJ[size];
		size = 0;
		for (RENDEROBJ r : rows)
			rs[size++] = r;
		return rs;
	}

	public void init() {
		current.clearSloppy();

		for (int i = 0; i < rows.length; i++) {
			if (passesFilter(i, rows[i])) {
				current.add(rows[i]);
			}
		}

		int h = 0;
		last = 0;
		for (int i = current.size()-1; i >= 0; i--) {
			h += current.get(i).body().height();
			if (h > section.body().height()) {
				last = i+1;
				break;
			}
		}
		
		first = CLAMP.i(first, 0, last);
		srows.clear();
		srows.body().moveX1Y1(section.body());
		
		for (int i = first; i < current.size(); i++) {
			RENDEROBJ rr = current.get(i);
			if (srows.body().height()+rr.body().height() > section.body().height())
				break;
			srows.add(rr, srows.body().x1(), srows.getLastY2());
		}
		
		
	}

	
	protected boolean passesFilter(int i, RENDEROBJ o) {
		return true;
	}
	
	public CLICKABLE view() {
		return section;
	}
	
	public final INTE target = new INTE() {
		
		@Override
		public int min() {
			return 0;
		}
		
		@Override
		public int max() {
			return last;
		}
		
		@Override
		public int get() {
			return first;
		}
		
		@Override
		public void set(int t) {
			first = t;
			init();
		}
	};

}
