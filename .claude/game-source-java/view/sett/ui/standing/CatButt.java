package view.sett.ui.standing;

import game.GAME;
import game.time.TIME;
import init.race.Race;
import init.type.HCLASS;
import init.type.HCLASSES;
import settlement.stats.STATS;
import settlement.stats.stat.STAT;
import settlement.stats.stat.StatCollection;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import util.colors.GCOLOR;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.gui.misc.GHeader;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GStaples;
import util.info.GFORMAT;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.main.VIEW;
import view.sett.ui.standing.Cats.Cat;

class CatButt extends GuiSection {
	
	static final int width = 220;
	private double last = 0;
	private double now = 0;
	private double max = 0;
	private final StatCollection[] cs;
	private final ISidePanel cat;
	private final HCLASS cl;

	CatButt(Cats cats, Cat cat, HCLASS cl, INTE hov) {
		this.cs = cat.cs;
		this.cat = cat;
		//body().setWidth(width - 10);
		//add(new GHeader(c.info.name));
		hoverInfoSet(cs[0].info.desc);

		
		add(new GHeader(cat.title()) {
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				OPACITY.O50.bind();
				COLOR.BLACK.render(r, body, 2);
				OPACITY.unbind();
				super.render(r, ds, isHovered);
			}
		}, 0, 0);
		

		
		addDown(2, new RENDEROBJ.RenderImp(width-20, 24) {

			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				double statMax = cats.getBiggest();
				if (max > 0)
					GMeter.renderDelta(r, last/max, now/max, body.x1(), (int) (body.x1()+body.width()*max/statMax), body.y1(), body.y2());
			}
		});
		
		addDown(2, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.f(text, now);
				text.add('/');
				GFORMAT.f(text, max);
				GFORMAT.colorInterInv(text, now, max);
			}
		});

		pad(4);
		
		GStaples staples = new Staples(cl, hov, cs);
		staples.body().moveX1(width+8);

		add(staples);
		moveLastToBack();
		pad(4);
		
		this.cl = cl;
	}

	@Override
	protected void clickA() {
		if (cl == HCLASSES.CITIZEN())
			VIEW.s().panels.addDontRemove(VIEW.s().ui.standing, cat);
		else
			VIEW.s().panels.addDontRemove(VIEW.s().ui.slaves, cat);
	}

	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		
		
		now = 0;
		max = 0;
		last = 0;
		
		for (StatCollection c : cs)
			for (STAT s : c.all()) {
				now += s.standing().get(cl, CitizenMain.current);
				last += s.standing().get(cl, CitizenMain.current, s.data(cl).getPeriodD(CitizenMain.current, 8, 0));
				max += s.standing().max(cl, CitizenMain.current);
			}
		GCOLOR.UI().border().render(r, body());
		GCOLOR.UI().bg(true, VIEW.s().panels.added(cat), hoveredIs()).render(r, body(), -1);
		
		
		
		super.render(r, ds);

	}
	
	@Override
	public void hoverInfoGet(GUI_BOX text) {
		GBox b = (GBox) text;
		b.title(cs[0].info.name);
		b.text(cs[0].info.desc);
		b.NL();
		super.hoverInfoGet(text);
	}
	
	static class Staples extends GStaples{
		
		private final HCLASS cl;
		private final INTE hov;
		private final StatCollection[] cs;
		private static final double div = 100;
		private Race old;
		
		private double vmin,vmax,max;
		private int vI = -1;
		
		Staples(HCLASS cl, INTE hov, StatCollection[] cs){
			super(STATS.DAYS_SAVED);
			this.cl = cl;
			this.hov = hov;
			this.cs = cs;
			border(false);
			background(true);
			body().setWidth(7*STATS.DAYS_SAVED);
			body().setHeight(70);
			normalize(false);
		}

		@Override
		protected void hover(GBox box, int stapleI) {
			box.title(cs[0].info.name);
			
			int fromZero = STATS.DAYS_SAVED - stapleI - 1;
			box.add(box.text().add(-fromZero).s().add(TIME.days().cycleName()));
			box.NL();
			double p = 0;
			double max = 0;
			double pprev = 0;
			for (StatCollection c : cs) {
				box.textLL(c.info.name);
				box.NL();
				for (STAT s : c.all()) {
					double m = s.standing().max(cl, CitizenMain.current, fromZero);
					if (m == 0)
						continue;
					
					double curr = s.standing().getHistoric(cl, CitizenMain.current, fromZero);
					box.textL(s.info().name);
					box.tab(6);
					box.add(GFORMAT.fofkInv(box.text(), curr, m));
					
					if (fromZero < STATS.DAYS_SAVED-1) {
						box.tab(9);
						int n = (int) (curr*div);
						double ppprev = s.standing().getHistoric(cl, CitizenMain.current, fromZero+1);
						pprev += ppprev;
						int prev = (int) (ppprev*div);
						double inc = (n-prev)/div;
						box.add(GFORMAT.f0(box.text(), inc));
					}
					p += s.standing().getHistoric(cl, CitizenMain.current, fromZero);
					max += m;
					box.NL();
				}
				
			}
			box.NL(8);
			box.textLL(Dic.¤¤Total);
			box.tab(6);
			box.add(GFORMAT.fofkInv(box.text(), p, max));
			box.tab(9);
			int n = (int) (p*div);
			int prev = (int) (pprev*div);
			double inc = (n-prev)/div;
			box.add(GFORMAT.f0(box.text(), inc));
			
//			box.NL();
//			box.add(GFORMAT.f(box.text(), getValue(stapleI)));
			
		}
		
		@Override
		protected double getValue(int stapleI) {
			if (old != CitizenMain.current || Math.abs(vI-GAME.updateI()) > 60) {
				vI = GAME.updateI();
				vmin = Double.MAX_VALUE;
				vmax = Double.MIN_VALUE;
				old = CitizenMain.current;
				max = 0;
//				for (StatCollection c : cs)
//					for (STAT s : c.all()) {
//						max += s.standing().max(cl, CitizenMain.current, i) - s.standing().getDismiss(cl, CitizenMain.current, i);
//					}
				for (int i = 0; i < STATS.DAYS_SAVED; i++) {
					double p = 0;
					double m = 0;
					for (StatCollection c : cs)
						for (STAT s : c.all()) {
							p += s.standing().getHistoric(cl, CitizenMain.current, i);
							m += s.standing().max(cl, CitizenMain.current, i) - s.standing().getDismiss(cl, CitizenMain.current, i);
						}
					
					vmin = Math.min(vmin, p);
					vmax = Math.max(vmax, p);
					max = Math.max(max, m);
				}
				
				if (max == 0 || vmax == 0)
					return 0;
				
				double dd = max*0.05;
				vmax = CLAMP.d(vmax+dd, 0, max);
				vmin = CLAMP.d(vmin-dd, 0, vmin);
				
			}
			
			
			
			double max = 0;
			int i = STATS.DAYS_SAVED-stapleI-1;
			double p = 0;
			for (StatCollection c : cs)
				for (STAT s : c.all()) {
					p += s.standing().getHistoric(cl, CitizenMain.current, i);
					max += s.standing().max(cl, CitizenMain.current, i) - s.standing().getDismiss(cl, CitizenMain.current, i);
				}
			if (max == 0)
				return 0;
			
			p-= vmin;
			p /= (vmax-vmin);
			
			return p;
		}
//		
//		private double v(int stapleI) {
//			return value(stapleI, cs, cl);
//			
//		}
		
		static double value(int stapleI, StatCollection[] cs, HCLASS cl) {
			double p = 0;
			int i = STATS.DAYS_SAVED-stapleI-1;
			for (StatCollection c : cs)
				for (STAT s : c.all()) {
					p += s.standing().getHistoric(cl, CitizenMain.current, i);
				}
			return p;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			
			if (hov != null) {
				isHovered = true;
				setHovered(hov.get());
			}
			
			super.render(r, ds, isHovered);
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				if (hov != null) {
					hov.set(hoverI());
				}
				return true;
			}
			return false;
		}
		
		@Override
		protected void renderExtra(SPRITE_RENDERER r, COLOR color, int stapleI, boolean hovered, double value,
				int x1, int x2, int y1, int y2) {
//			int v = (int) (v(stapleI)*100);
//			int p = (int) ((stapleI > 0 ? v(stapleI-1) : value)*100);
//			if (v == p)
//				return;
//			SPRITE s = p > v ? SPRITES.icons().s.arrowDown : SPRITES.icons().s.arrowUp;
//			color.bind();
//			s.renderC(r, x1 + (x2-x1)/2, y2 + s.height()/2);
		}
		
		@Override
		protected void setColor(ColorImp c, int stapleI, double value) {
//			double v = (int) (v(stapleI)*100);
//			double p = (int) ((stapleI > 0 ? v(stapleI-1) : value)*100);
//			if (p < v) {
//				c.interpolate(GCOLOR.UI().NEUTRAL.hovered, GCOLOR.UI().GOOD.hovered, p) set(GCOLOR.UI().GOOD.hovered);
//			}else if (p > v) {
//				c.set(GCOLOR.UI().BAD.hovered);
//			}else {
//				c.set(GCOLOR.UI().NEUTRAL.hovered);
//				
//			}
			if (max == 0)
				return;
			c.interpolate(GCOLOR.UI().BAD.hovered, GCOLOR.UI().GOOD.hovered, value);
		}
		
		@Override
		protected void setColorBg(ColorImp c, int stapleI, double value) {
			c.set((stapleI & 1) == 1 ? COLOR.WHITE20 : COLOR.WHITE15);
		}
		
	}

}