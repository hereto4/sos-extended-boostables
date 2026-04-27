package view.ui.goods;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.FResources;
import game.faction.FResources.RTYPE;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import settlement.main.SETT;
import settlement.room.industry.module.RoomProduction.Source;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.data.INT;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GStaples;
import util.info.GFORMAT;
import util.text.Dic;
import util.text.DicTime;
import view.main.VIEW;
import world.map.regions.Region;
import world.region.RD;

final class Row extends GuiSection {

	private static final int w = 5;
	private static int amount = FACTIONS.player().res().total().history(RESOURCES.ALL().get(0)).historyRecords();
	private static final int height = 60;
	
	private final GStaples[] dias;
	private int hi;
	private final GETTER<RESOURCE> res;
	
	Row(GETTER<RESOURCE> r, Pop pop) {
		this.res = r;
		INTE im = new INT.IntImp();
		im.set(-1);
		
		add(new HOVERABLE.HoverableAbs(Icon.M*2) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
				SPRITE s = res.get() == null ? SPRITES.icons().m.urn.big : res.get().icon();
				s.render(r, body);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				if (res.get() != null)
					res.get().hoverDetailed(text);
				else
					text.title(Dic.¤¤Total);
			}
			
		});
		
		dias = new GStaples[] {
			 new StorageDiagram(r),
			 new ProductionDiagram(r),
		};
		
		for (GStaples ss : dias) {
			addRelBody(4, DIR.E, ss);
		}
		addRelBody(4, DIR.E, prod(r, pop));
		pad(2, 6);
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		hi = -1;
		for (GStaples ss : dias) {
			if (ss.hoveredIs()) {
				hi = ss.hoverI();
			}
		}
		if (hi >= 0) {
			for (GStaples ss : dias) {
				ss.setHovered(hi);
			}
		}
		
		super.render(r, ds);
	}
	
	@Override
	public void hoverInfoGet(GUI_BOX text) {
		if (hi < 0) {
			super.hoverInfoGet(text);
			return;
		}
		GBox b = (GBox) text;
		
		int si = amount-hi-1;
		
		{
			GText t = b.text();
			t.lablify();
			DicTime.setAgo(t, si*GAME.player().res().time.bitSeconds());
			b.add(t);
			b.NL(4);
		}
		
		RESOURCE res = this.res.get();
		
		{
			b.textL(Dic.¤¤Stored);
			b.tab(6);
			b.add(GFORMAT.i(b.text(), SETT.ROOMS().STOCKPILE.tally().amountsDay().history(res).get(si)));
			b.NL(8);
		}
		{
			FResources rr = FACTIONS.player().res();
			for (RTYPE t : RTYPE.all) {
				b.add(b.text().normalify().add(t.name));
				b.tab(6);
				b.add(GFORMAT.iIncr(b.text(), rr.in(t).history(res).get(si)));
				b.tab(8);
				b.add(GFORMAT.iIncr(b.text(), -rr.out(t).history(res).get(si)));
				b.NL();
			}
			
			b.NL(4);
			b.textL(Dic.¤¤Net);
			b.tab(6);
			b.add(GFORMAT.iIncr(b.text(), GAME.player().res().total().history(res).get(si)));
		}
		
		{
			b.NL(8);
			si = amount-hi-1;
			b.text(Dic.¤¤buyPrice);
			b.tab(6);
			b.add(GFORMAT.i(b.text(), FACTIONS.player().trade.pricesBuy.history(res).get(si)));
			b.NL();
			b.text(Dic.¤¤sellPrice);
			b.tab(6);
			b.add(GFORMAT.i(b.text(), FACTIONS.player().trade.pricesSell.history(res).get(si)));
			b.NL();
			if (res != null) {
				b.text(Dic.¤¤avePrice);
				b.tab(6);
				b.add(GFORMAT.i(b.text(), si == 0 ? FACTIONS.PRICE().get(res) : FACTIONS.player().trade.pricesAve.history(res).get(si)));
				b.NL();
			}
			
			
			b.textL(Dic.¤¤Earnings);
			b.tab(6);
			b.add(GFORMAT.iIncr(b.text(), GAME.player().trade.inExported.history(res).get(si)-GAME.player().trade.outImported.history(res).get(si)));
			b.text(Dic.¤¤Curr);
		}
		
		

	}
	
	private static class StorageDiagram extends GStaples {

		private final GETTER<RESOURCE> res;
		private GStat t = new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofkNoColor(text, SETT.ROOMS().STOCKPILE.tally().amountTotal(res.get()), SETT.ROOMS().STOCKPILE.tally().space.total(res.get()));
			}
		}.bg();
		
		StorageDiagram(GETTER<RESOURCE> res){
			super(amount);
			this.res = res;
			body().setWidth(w*amount).setHeight(height);
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {			
			
			super.render(r, ds, hoveredIs());
			t.render(r, body().x1()+w, body().y1()+ w/2);		
		}

		@Override
		protected double getValue(int stapleI) {
			double c = SETT.ROOMS().STOCKPILE.tally().space.total(res.get());
			double d = SETT.ROOMS().STOCKPILE.tally().amountsDay().history(res.get()).get(amount-1-stapleI);
			if (c == 0)
				d = d > 0 ? 1 : 0;
			else
				d /= c;
			
			d = CLAMP.d(d, 0, 1);
			return d;
		}

		@Override
		protected void hover(GBox box, int stapleI) {

			
		}
		
		@Override
		protected void setColor(ColorImp c, int x, double value) {
			c.set(GCOLOR.UI().SOSO.normal);
		}
	}
	
	private static class ProductionDiagram extends GStaples {

		private final GETTER<RESOURCE> res;
		private GStat t = new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iIncr(text, GAME.player().res().total().history(res.get()).get(1));
			}
		}.bg();
		
		ProductionDiagram(GETTER<RESOURCE> res){
			super(amount, false);
			this.res = res;
			body().setWidth(w*amount).setHeight(height);
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			
			super.render(r, ds, hoveredIs());
			t.render(r, body().x1()+w, body().y1()+ w/2);			
		}

		@Override
		protected double getValue(int stapleI) {
			return Math.abs(GAME.player().res().total().history(res.get()).get(amount-1-stapleI));
		}

		@Override
		protected void hover(GBox box, int stapleI) {

			
		}
		
		@Override
		protected void setColor(ColorImp c, int x, double value) {
			if (GAME.player().res().total().history(res.get()).get(amount-1-x) < 0)
				c.set(GCOLOR.UI().BAD.normal);
			else
				c.set(GCOLOR.UI().GOOD.normal);
			
		}
	}
	
	
	private static RENDEROBJ prod(GETTER<RESOURCE> gres, Pop pop) {

		GStat s = new GStat() {
			
			@Override
			public void update(GText text) {
				
				double tot = 0;
				{
					RESOURCE res = gres.get();
					for (Source rr : SETT.ROOMS().PROD.producers(res)) {
						if (rr.am() == 0)
							continue;
						tot += rr.am();
					}
					for (Source rr : SETT.ROOMS().PROD.consumers(res)) {
						if (rr.am() == 0)
							continue;
						tot -= rr.am();
					}
					
					
				}
				
				GFORMAT.iIncr(text, (long) tot);
			}
		};
		
		ClickableAbs c = new ClickableAbs() {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
				if (gres.get() == null)
					return;
				GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
				GButt.ButtPanel.renderFrame(r, body);
				s.renderC(r, body);
				for (int i = 0; i < FACTIONS.player().realm().regions(); i++) {
					Region re = FACTIONS.player().realm().region(i);
					if (RD.OUTPUT().get(gres.get()).getDelivery(re) > 0) {
						return;
					}
				}
				OPACITY.O50.bind();
				COLOR.BLACK.render(r, body);
				OPACITY.unbind();
			}
			
			@Override
			protected void clickA() {
				if (gres.get() == null)
					return;
				for (int i = 0; i < FACTIONS.player().realm().regions(); i++) {
					Region re = FACTIONS.player().realm().region(i);
					if (RD.OUTPUT().get(gres.get()).getDelivery(re) > 0) {
						pop.res = gres.get();
						VIEW.inters().popup.show(pop, this);
						return;
					}
				}
				super.clickA();
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				if (gres.get() == null)
					return;
				gres.get().hoverDetailed(text);
			}
			
			
		};
		c.body.setDim(64, 62);
		
		
		return c;
		
	}
	


}
