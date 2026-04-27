package view.sett.ui.food;

import static settlement.main.SETT.ROOMS;

import game.faction.FACTIONS;
import game.faction.FResources;
import game.faction.FResources.RTYPE;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCES;
import init.resources.ResG;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.type.NEEDS;
import settlement.main.SETT;
import settlement.room.industry.module.IndustryResource;
import settlement.room.industry.module.RoomProduction;
import settlement.room.service.food.canteen.ROOM_CANTEEN;
import settlement.room.service.food.eatery.ROOM_EATERY;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LinkedList;
import snake2d.util.sets.Queue;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.gui.table.GStaples;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import util.text.DicTime;
import view.interrupter.ISidePanel;

public class UIFood extends ISidePanel{

	private static CharSequence ¤¤expl = "The amount of days we can feed the population for. Note that this is a rough estimate. Many factors, such as trade, and production can affect this amount.";
	
	static {
		D.ts(UIFood.class);
	}
	
	public UIFood() {
		
		titleSet(Dic.¤¤Food);
		
		
	
		section.addRelBody(16, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				
				double am = 0;
				for (ResG rr : RESOURCES.EDI().all()) {
					am += ROOMS().PROD.produced(rr.resource);
				}
				
				GFORMAT.f0(text, am);
				
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				double am = 0;
				for (ResG rr : RESOURCES.EDI().all()) {
					b.add(rr.resource.icon());
					b.textL(rr.resource.name);
					b.tab(7);
					double a = ROOMS().PROD.produced(rr.resource);
					
					b.add(GFORMAT.f0(b.text(), a));
					b.NL();
					am += a;
				}
				
				b.NL(8);
				
				b.textLL(Dic.¤¤Total);
				b.tab(7);
				
				b.add(GFORMAT.f0(b.text(), am));
				
			};
			
		}.hv(Dic.¤¤ProductionRate));
		
		section.addRelBody(16, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				
				double needed = 0;
				
				for (ResG res : RESOURCES.EDI().all()) {
					needed += SETT.ROOMS().PROD.consumed(res.resource);
					needed += SETT.MAINTENANCE().estimateGlobal(res.resource);
				}
				for (int ci = 0; ci < HCLASSES.ALL().size(); ci++) {
					HCLASS c = HCLASSES.ALL().get(ci);
					if (c.player) {
						for (int ri = 0; ri < RACES.all().size(); ri++) {
							Race r = RACES.all().get(ri);
							double n = NEEDS.TYPES().HUNGER.rate.get(c.get(r))*STATS.POP().POP.data(c).get(r, 0)*STATS.FOOD().FOOD.decree().get(c, r);
							needed += n;
							
						}
					}
				}
				
				GFORMAT.f0(text, -needed);
				
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				
				double needed = 0;
				for (int ci = 0; ci < HCLASSES.ALL().size(); ci++) {
					
					HCLASS c = HCLASSES.ALL().get(ci);
					if (c.player) {
						for (int ri = 0; ri < RACES.all().size(); ri++) {
							Race r = RACES.all().get(ri);
							double n = NEEDS.TYPES().HUNGER.rate.get(c.get(r))*STATS.POP().POP.data(c).get(r, 0)*STATS.FOOD().FOOD.decree().get(c, r);
							if (n > 0) {
								b.add(r.appearance().icon);
								b.textL(c.names);
								
								
								b.tab(7);
								b.add(GFORMAT.f0(b.text(), -n));
								b.NL();
							}
							needed += n;
							
						}
					}
				}
				
				
				b.sep();
				
				b.textLL(Dic.¤¤Total);
				b.tab(7);
				
				b.add(GFORMAT.f0(b.text(), -needed));
				
			};
			
		}.hv(Dic.¤¤ConsumptionRate));
		
		section.addRelBody(16, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				
				int a = 0;
				for (int ei = 0; ei < RESOURCES.EDI().all().size(); ei++) {
					ResG r = RESOURCES.EDI().all().get(ei);
					a += ROOMS().STOCKPILE.tally().amountTotal(r.resource);
				}
				
				for (int ri = 0; ri < SETT.ROOMS().EATERIES.size(); ri++) {
					ROOM_EATERY e = SETT.ROOMS().EATERIES.get(ri);
					a += e.totalFood();
				}
				
				for (int ri = 0; ri <  SETT.ROOMS().CANTEENS.size(); ri++) {
					ROOM_CANTEEN e = SETT.ROOMS().CANTEENS.get(ri);
					a += e.totalFood();
				}
				
				GFORMAT.f0(text, a);
				
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				
				
				int a = 0;
				for (int ei = 0; ei < RESOURCES.EDI().all().size(); ei++) {
					ResG r = RESOURCES.EDI().all().get(ei);
					a += ROOMS().STOCKPILE.tally().amountTotal(r.resource);
				}
				
				b.add(ROOMS().STOCKPILE.icon.small);
				b.textL(ROOMS().STOCKPILE.info.names);
				b.tab(7);
				b.add(GFORMAT.i(b.text(), a));
				b.NL();
				
				for (int ri = 0; ri < SETT.ROOMS().EATERIES.size(); ri++) {
					ROOM_EATERY e = SETT.ROOMS().EATERIES.get(ri);
					int am = (int) e.totalFood();
					a += am;
					

					b.add(e.icon.small);
					b.textL(e.info.names);
					b.tab(7);
					b.add(GFORMAT.i(b.text(), am));
					b.NL();
				}
				
				for (int ri = 0; ri <  SETT.ROOMS().CANTEENS.size(); ri++) {
					ROOM_CANTEEN e = SETT.ROOMS().CANTEENS.get(ri);
					int am = (int) e.totalFood();
					a += am;
					

					b.add(e.icon.small);
					b.textL(e.info.names);
					b.tab(7);
					b.add(GFORMAT.i(b.text(), am));
					b.NL();
				}

				b.NL(8);
				
				b.textLL(Dic.¤¤Total);
				b.tab(7);
				
				b.add(GFORMAT.i(b.text(), a));
				
			};
			
		}.hv(Dic.¤¤Stored));
		
		section.addRelBody(16, DIR.S, new GStat() {
			
			@Override
			public void update(GText text) {
				
				GFORMAT.f(text, STATS.FOOD().FOOD_DAYS.data().getD(null)*STATS.FOOD().FOOD_DAYS.dataDivider());
				
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				
				b.text(¤¤expl);
				
			};
			
		}.hv(STATS.FOOD().FOOD_DAYS.info().name));
		
		GStaples st = new GStaples(STATS.DAYS_SAVED/4) {
			
			@Override
			protected void hover(GBox box, int stapleI) {
				int ii = (STATS.DAYS_SAVED/4-stapleI -1);
				box.title(STATS.FOOD().FOOD_DAYS.info().name);
				
				{
					GText tt = box.text();
					DicTime.setDaysAgo(tt, ii*4);
					box.textLL(tt);
					box.NL(4);
				}
				
				{
					box.textLL(STATS.FOOD().FOOD_DAYS.info().name);
					box.tab(7);
					box.add(GFORMAT.f(box.text(), STATS.FOOD().FOOD_DAYS.data(null).getD(null, ii*4)*STATS.FOOD().FOOD_DAYS.dataDivider()));
					box.NL();
				}

				{
					box.textLL(Dic.¤¤Population);
					box.tab(7);
					box.add(GFORMAT.i(box.text(), STATS.POP().POP.data().get(null)));
					box.NL();
				}
				
				{
					box.textLL(Dic.¤¤Stored);
					box.tab(7);
					int st = 0;
					for (ResG rr : RESOURCES.EDI().all()) {
						st += SETT.ROOMS().STOCKPILE.tally().amountReservable.get(rr.resource);
					}
					box.add(GFORMAT.i(box.text(), st));
					box.NL();
				}
				
				box.sep();
				
				{
					int net = 0;
					
					for (RTYPE t : FResources.RTYPE.all) {
						box.text(t.name);
						box.tab(7);
						int in = 0;
						int out = 0;
						for (ResG rr : RESOURCES.EDI().all()) {
							in += FACTIONS.player().res().in(t).history(rr.resource).get(ii);
							out += FACTIONS.player().res().out(t).history(rr.resource).get(ii);
						}
						net += in;
						net -= out;
						box.add(GFORMAT.iIncr(box.text(), in));
						box.tab(9);
						box.add(GFORMAT.iIncr(box.text(), -out));
						box.NL();
					}
					
					box.textL(Dic.¤¤Net);
					box.tab(7);
					box.add(GFORMAT.iIncr(box.text(), net));
					box.NL();
				}
				
				
				
				
			}
			
			@Override
			protected double getValue(int stapleI) {
				int ii = (STATS.DAYS_SAVED/4-stapleI -1);
				return STATS.FOOD().FOOD_DAYS.data(null).getD(null, ii*4)*STATS.FOOD().FOOD_DAYS.dataDivider();
			}
		};
		
		st.normalize(true);
		
		st.body().setWidth(400).setHeight(80);
		
		section.addRelBody(4, DIR.S, st);
		
		
		Queue<ResG> all = new Queue<>(RESOURCES.EDI().all().size());
		
		for (ResG res : RESOURCES.EDI().all())
			all.push(res);
		
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		while(all.hasNext()) {
			
			GuiSection s = new GuiSection();
			
			for (int i = 0; i < 2 && all.hasNext(); i++) {
				
				s.addRightC(8, new RR(all.poll()));

				
			}
			
			rows.add(s);
		}
		
		section.addRelBody(16, DIR.S, new GScrollRows(rows, HEIGHT-section.body().height()-32).view());
		
		
	}
	
	
	private static class RR extends HOVERABLE.HoverableAbs{
		
		private final ResG res;
		private final GText t = new GText(UI.FONT().S, 8);
		
		RR(ResG res){
			super(Icon.M*2+200, Icon.M*2+12);
			this.res = res;
		}

		@Override
		protected void render(SPRITE_RENDERER ren, float ds, boolean isHovered) {
			GButt.ButtPanel.renderBG(ren, true, false, isHovered, body);
			res.resource.icon().renderScaled(ren, body.x1()+8, body.y1()+6, 2);
			t.clear();
			
			int am = totStored();
			
			GFORMAT.i(t, am);
			t.adjustWidth();
			t.renderCY(ren, body.x1()+120-t.width(), body.cY());
			
			t.clear();
			
			GFORMAT.f0(t, SETT.ROOMS().PROD.produced(res.resource)-SETT.ROOMS().PROD.consumed(res.resource));
			t.adjustWidth();
			t.renderCY(ren, body.x2()-8-t.width(), body.cY());
			
			GButt.ButtPanel.renderFrame(ren, body);
		}
		
		private int totStored() {
			int am = 0;
			am += ROOMS().STOCKPILE.tally().amountTotal(res.resource);
			
			for (int ri = 0; ri < SETT.ROOMS().EATERIES.size(); ri++) {
				ROOM_EATERY e = SETT.ROOMS().EATERIES.get(ri);
				am += e.amount(res);
			}
			
			for (int ri = 0; ri <  SETT.ROOMS().CANTEENS.size(); ri++) {
				ROOM_CANTEEN e = SETT.ROOMS().CANTEENS.get(ri);
				am += e.amount(res);
			}
			return am;
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(res.resource.names);
			
			b.textLL(Dic.¤¤Stored);
			b.NL();
			b.add(ROOMS().STOCKPILE.icon.small);
			b.textL(ROOMS().STOCKPILE.info.names);
			b.tab(7);
			b.add(GFORMAT.i(b.text(), ROOMS().STOCKPILE.tally().amountTotal(res.resource)));
			b.NL();
			
			for (int ri = 0; ri < SETT.ROOMS().EATERIES.size(); ri++) {
				ROOM_EATERY e = SETT.ROOMS().EATERIES.get(ri);
				b.NL();
				b.add(e.icon.small);
				b.textL(e.info.names);
				b.tab(7);
				b.add(GFORMAT.i(b.text(),  e.amount(res)));
				b.NL();
			}
			
			for (int ri = 0; ri <  SETT.ROOMS().CANTEENS.size(); ri++) {
				ROOM_CANTEEN e = SETT.ROOMS().CANTEENS.get(ri);
				b.NL();
				b.add(e.icon.small);
				b.textL(e.info.names);
				b.tab(7);
				b.add(GFORMAT.i(b.text(),  e.amount(res)));
				b.NL();
			}
			
			b.NL(4);
			
			b.textL(Dic.¤¤Total);
			b.tab(7);
			b.add(GFORMAT.i(b.text(), totStored()));
			
			b.sep();
			
			b.textLL(STATS.FOOD().FOOD_PREFFERENCE.info().name);
			b.NL();
			
			for (Race r : RACES.all()) {
				if (r.pref().foodMask.has(res.resource)) {
					b.add(r.appearance().icon);
					b.textL(r.info.names);
					b.NL();
				}
			}
			
			b.sep();
			
			b.textLL(Dic.¤¤Production);
			b.NL();
			
			for (RoomProduction.Source ii : SETT.ROOMS().PROD.producers(res.resource)) {
				b.add(ii.icon());
				b.textLL(ii.name());
				if (ii.thereAreMultipleIns() != null) {
					for (IndustryResource iii : ii.thereAreMultipleIns().ins()) {
						b.add(iii.resource.icon().small);
					}
				}
				
				b.tab(7);
				b.add(GFORMAT.f0(b.text(), ii.am()));
				b.NL();
			}
			
			b.NL(8);
			b.textLL(Dic.¤¤Consumed);
			b.NL();
			
			for (RoomProduction.Source ii : SETT.ROOMS().PROD.consumers(res.resource)) {
				b.add(ii.icon());
				b.textLL(ii.name());
				if (ii.thereAreMultipleIns() != null) {
					for (IndustryResource iii : ii.thereAreMultipleIns().ins()) {
						b.add(iii.resource.icon().small);
					}
				}
				
				b.tab(7);
				b.add(GFORMAT.f0(b.text(), -ii.am()));
				b.NL();
			}
		}
		
	}
	
}
