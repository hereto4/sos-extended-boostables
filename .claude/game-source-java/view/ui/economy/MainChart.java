package view.ui.economy;

import game.GAME;
import game.faction.player.PCredits.CredHistory;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.misc.CLAMP;
import util.colors.GCOLOR;
import util.data.INT.IntImp;
import util.gui.misc.GBox;
import util.gui.table.GStaples;
import util.info.GFORMAT;

final class MainChart extends GuiSection{

	private IntImp hi;
	private final int w ;
	private int am = GAME.player().credits().creditsH().historyRecords();
	
	private int loCredits;
	private double maxin, maxout;

	
	
	MainChart(int height, IntImp hi, int sw){
		this.hi = hi;
		this.w = sw;
		addRelBody(4, DIR.S, amount());
		addRelBody(4, DIR.S, new Profits());
		addRelBody(8, DIR.S, new Losses());
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		loCredits = Integer.MAX_VALUE;
		maxin = 0;
		maxout = 0;
		for (int i = 0; i < am; i++) {
			loCredits = Math.min(loCredits, (int)GAME.player().credits().creditsH().get(i));
			int m = 0;
			int o = 0;
			
			for (CredHistory h : GAME.player().credits().all()) {
				m += h.IN.get(i);
				o += h.OUT.get(i);
			}
			
			maxin = Math.max(maxin, m);
			maxout = Math.max(o, maxout);
		}
		if (loCredits > 1)
			loCredits --;
		super.render(r, ds);
		
	}
	
	private GStaples amount() {
		
		GStaples s = new GStaples(am) {
			
			@Override
			protected void hover(GBox box, int stapleI) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				if (hi.get() >= 0) {
					setHovered(hi.get());
				}
				super.render(r, ds, hoveredIs());
			}
			
			@Override
			public boolean hover(COORDINATE mCoo) {
				if (super.hover(mCoo)) {
					hi.set(hoverI());
					return true;
				}
				return false;
			}
			
			@Override
			protected double getValue(int stapleI) {
				return CLAMP.d(GAME.player().credits().creditsH().get(am -stapleI-1), 0, Integer.MAX_VALUE);
			}
			
			@Override
			protected void setColor(ColorImp c, int stapleI, double value) {
				c.set(COLOR.YELLOW100).saturateSelf(0.5);
			}
		};
		s.normalize(true);
		s.body().setWidth(w*am);
		s.body().setHeight(78);
		return s;
		
	}
	
	@Override
	public void hoverInfoGet(GUI_BOX text) {
		if (hi.get() >= 0) {
			GBox b = (GBox) text;
			int si = am-hi.get()-1;
			
			{
				int ri = 0;
				for (RESOURCE res : RESOURCES.ALL()) {
					int a = GAME.player().trade.inExported.history(res).get(si) - GAME.player().trade.outImported.history(res).get(si);
					if (a != 0) {
						b.add(res.icon());
						b.add(GFORMAT.iIncr(b.text(), a));
						b.space();
						ri++;
						if (ri >= 4) {
							ri = 0;
							b.NL(8);
						}
							
						
					}
					
				}
					
				
			}
			
		}else
			super.hoverInfoGet(text);
	}

	
	private final class Profits extends HOVERABLE.HoverableAbs{
		
		Profits(){
			body.setWidth(w*am);
			body().setHeight(112);
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			
			GCOLOR.UI().border().render(r, body(), 1);
			
			for (int x = 0; x < am; x++) {
				
				int x1 = body().x1()+w*x;

				
				
				if (x != hi.get()) {
					GCOLOR.UI().bg().render(r, x1, x1+w, body().y1(), body().y2());
				}
				
				if (maxin == 0)
					continue;
				
				int si = am - x-1;
				
				int y2 = body().y2();
				for (CredHistory h : GAME.player().credits().all()) {
					
					double d = h.IN.get(si)/maxin;
					int hig = (int) Math.ceil(body().height()*d);
					ColorImp.TMP.set(COLOR.UNIQUE.getC(h.type.ordinal()));
					if (x == hi.get()) {
						ColorImp.TMP.shadeSelf(1.5);
					}else {
						ColorImp.TMP.shadeSelf(0.5);
					}
					ColorImp.TMP.render(r, x1, x1+w, y2-hig, y2);
					
					if (hig > 1)
						COLOR.UNIQUE.getC(h.type.ordinal()).render(r, x1+1, x1+w-1, y2-hig+1, y2);
					if (hig > 0)
						hig--;
					y2-= hig;
					
				}
			}
			
			
			
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				int ii = ((mCoo.x()-body().x1())/w);
				if (ii < am)
					hi.set(ii);
				return true;
			}
			return false;
		}
		
	}
	
	private final class Losses extends HOVERABLE.HoverableAbs{
		
		Losses(){
			body.setWidth(w*am);
			body().setHeight(112);
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			
			GCOLOR.UI().border().render(r, body(), 1);
			
			for (int x = 0; x < am; x++) {
				
				int x1 = body().x1()+w*x;

				
				
				if (x != hi.get()) {
					GCOLOR.UI().bg().render(r, x1, x1+w, body().y1(), body().y2());
				}
				
				if (maxout == 0)
					continue;
				
				int si = am - x-1;
				
				int y1 = body().y1();
				for (CredHistory h : GAME.player().credits().all()) {
					
					double d = h.OUT.get(si)/maxout;
					int hig = (int) Math.ceil(body().height()*d);
					ColorImp.TMP.set(COLOR.UNIQUE.getC(h.type.ordinal()));
					if (x == hi.get()) {
						ColorImp.TMP.shadeSelf(1.5);
					}else {
						ColorImp.TMP.shadeSelf(0.5);
					}
					ColorImp.TMP.render(r, x1, x1+w, y1, y1+hig);
					
					if (hig > 1)
						COLOR.UNIQUE.getC(h.type.ordinal()).render(r, x1+1, x1+w-1, y1-1, y1+hig);
					if (hig > 0)
						hig--;
					y1+= hig;
					
				}
			}
			
			
			
		}
		
		@Override
		public boolean hover(COORDINATE mCoo) {
			if (super.hover(mCoo)) {
				int ii = ((mCoo.x()-body().x1())/w);
				if (ii < am)
					hi.set(ii);
				return true;
			}
			return false;
		}
		
	}
	
}
