package view.ui.economy;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import util.gui.misc.GBox;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GStaples;
import util.info.GFORMAT;
import util.statistics.HistoryResource;

class RRowPriceDia extends GStaples {

	private final COLOR col;
	private final HistoryResource hres;
	private final RESOURCE res;
	private final int amount;
	
	private GStat tbuy = new GStat() {
		
		@Override
		public void update(GText text) {
			int b = hres.get(res);
			if (res == null)
				b /= RESOURCES.ALL().size();
			GFORMAT.i(text, b);
		}
	}.bg();
	
	private GStat tinc = new GStat() {
		
		@Override
		public void update(GText text) {
			double nn = 0;
			for (int i = 1; i < 5; i++)
				nn += hres.history(res).getD(i);
			nn/= 4.0;
			int b = hres.get(res)-(int) nn ;
			GFORMAT.iIncr(text, b);
		}
	}.bg();
	
	RRowPriceDia(RESOURCE res, COLOR color, HistoryResource hres, int height){
		super(hres.history(res).historyRecords(), false);
		this.amount = hres.history(res).historyRecords();
		this.col = color;
		this.res = res;
		this.hres = hres;
		body().setWidth(3*amount).setHeight(height);
		normalize(true);
		
	}
	
	@Override
	protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
		super.render(r, ds, hoveredIs());
		tbuy.render(r, body().x1()+4, body().y1()+ 4);
		tinc.render(r, body().x1()+4, body().y2()-18);
	}

	@Override
	protected double getValue(int stapleI) {
		return hres.history(res).get(amount-1-stapleI);
	}
	
	@Override
	protected void hover(GBox box, int stapleI) {

		
	}
	
	@Override
	protected void setColor(ColorImp c, int x, double value) {
		c.set(col);
		
	}
	

}