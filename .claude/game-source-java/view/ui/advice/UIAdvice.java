package view.ui.advice;

import java.util.ArrayList;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.npc.stockpile.NPCStockpile;
import game.faction.player.PBonusSetting;
import game.save.PROP;
import game.time.TIME;
import init.race.RACES;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import init.tech.TECHS;
import init.tech.TechCurrency;
import init.type.HCLASSES;
import settlement.main.SETT;
import settlement.room.industry.module.INDUSTRY_HASER;
import settlement.room.industry.module.IndustryResource;
import settlement.room.infra.admin.AdminData.ROOM_ADMIN_HOLDER;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.employment.RoomEmployment;
import settlement.stats.STATS;
import settlement.stats.service.StatServiceRoom;
import snake2d.CORE;
import snake2d.CORE_STATE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.text.D;
import view.main.VIEW;
import view.world.generator.WorldViewGenerator;

public class UIAdvice extends GuiSection{

	private static CharSequence ¤¤name = "Potential Problems";
	
	private static CharSequence ¤¤toggleOff = "Click to suppress this warning.";
	private static CharSequence ¤¤toggleOn = "Click to activate this warning";
	
	private static CharSequence ¤¤resettle = "resettle";
	private static CharSequence ¤¤resettleD = "If you find that your city is crumbling into dust, we can move to a different part of Syx and start over. We will retain our current level, and be able to unlock any new titles. We will also keep some of our resources.";
	private static CharSequence ¤¤resettleSure = "Are you sure you want to abandon your current city and try your luck somewhere else?";
	

	private static CharSequence ¤¤Oddjobbers = "While oddjobbers are handy as builders and lending a hand where possible, they are essentially freeloaders, only draining your realm of resources without giving anything back. Make sure they are either contributing to your needs, or producing something to export.";
	private static CharSequence ¤¤OddjobbersG = "The amount of odd-jobber you have is currently not a problem.";
	private static CharSequence ¤¤OddjobbersB = "You have a lot of oddjobbers compared to your workforce. Give them something to do.";
	
	private static CharSequence ¤¤Work = "Make sure that all rooms are making the most out of their employees. If they are not properly configured, the employees will idle and be no better than oddjobbers - a drain on your economy.";
	private static CharSequence ¤¤WorkG = "No rooms currently have severe problems with workload.";
	private static CharSequence ¤¤WorkB = "Some rooms have problems with workload.";
	
	private static CharSequence ¤¤Resources = "Resources are meant to be used. Only a few of them are beneficial to store, like food and weapons, but only in moderate amounts. Stored resources degrade and spoil, as well as attract unwanted attention to your city. If goods can't be consumed domestically, they should be traded for something that can.";
	private static CharSequence ¤¤ResourcesG = "You do not currently store any goods in excess.";
	private static CharSequence ¤¤ResourcesB = "Some of your goods are stored excessively, currently rotting away in storage for nothing.";
	
	private static CharSequence ¤¤Money = "Money itself can not be eaten or utilized. It is but a temporary credit to be traded into something that is actually beneficial to your citizens. And like with resources, if horded, you are only wasting the potential of your prosperity through inflation and attracting hostile attention to your city.";
	private static CharSequence ¤¤MoneyG = "You currently are not hording much wealth.";
	private static CharSequence ¤¤MoneyB = "Your treasury is full. Piled up denarii fills no stomach. You should use it.";
	
	private static CharSequence ¤¤Research = "Research and technology can be just as detrimental as beneficial. It's not simply about advancing. Since technology has a perpetual cost in one form or the other, it's important to ask yourself if a technology is actually benefitting your economy, if another allocation is better, or if the resources should be diverted elsewhere.";
	private static CharSequence ¤¤ResearchG = "You are not spending excessive resources on technology. But remember to make sure you are getting the most out of your allocations.";
	private static CharSequence ¤¤ResearchB = "You have a very high ratio of research workers compared to your industry workers. This could indicate that you'd be better off reassigning some research workers to industries instead.";
	
	private static CharSequence ¤¤Service = "Having services always cost you resources one way or the other. As such, it's important not to over-dimension them. Remember to also go for the most rewarding services first by measuring their cost vs their benefit.";
	private static CharSequence ¤¤ServiceG = "Your services are properly dimensioned.";
	private static CharSequence ¤¤ServiceB = "Some of your services have low usage and are thus over-dimensioned";
	
	static {
		D.ts(UIAdvice.class);
	}
	
	private final ArrayListGrower<Entry> all = new ArrayListGrower<Entry>();
	public static boolean resettlePossible = true;
	
	public static GButt.ButtPanel make() {
		UIAdvice a = new UIAdvice();
		resettlePossible = true;
		if (false) {
			//have wealth per year, and if you notice a big dip, then prompt about resettle
		}
		
		GButt.ButtPanel b = new GButt.ButtPanel(UI.icons().m.advice) {
			
			@Override
			protected void clickA() {
				VIEW.inters().popup.show(a, this);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				
				b.title(¤¤name);
				for (Entry e : a.all) {
					if (e.toggled()) {
						if (e.is()) {
							b.error(e.bad);
						}
					}
					
					
				}
			}
			
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				
				super.render(r, ds, isActive, isSelected, isHovered);
				for (Entry e : a.all) {
					if (e.toggled() && e.is()) {
						GCOLOR.T().IBAD.bind();
						UI.icons().s.alert.render(r, body.x1()+3, body.y1()+3);
						COLOR.unbind();
					}
				}
			}
			
			
		};
		
		
		b.hoverInfoSet(¤¤name);
		return b;
		
	}
	
	private UIAdvice() {

		
		
		new Entry(all, UI.icons().m.citizen, ¤¤Oddjobbers, ¤¤OddjobbersG, ¤¤OddjobbersB) {

			@Override
			boolean is() {
				double all = STATS.WORK().workforce();
				double emp = STATS.WORK().EMPLOYED.stat().data(HCLASSES.CITIZEN()).get(null);
				
				if (all > 100) {
					if (emp/all < 0.15)
						return true;
				}else if (all > 20) {
					if (emp/all < 0.5)
						return true;
				}
				
				return false;
			}
			
		};
		
		new Entry(all, UI.icons().m.workshop, ¤¤Work, ¤¤WorkG, ¤¤WorkB) {

			double ee = 0;
			double tot = 0;
			private final Count<RoomEmployment> count = new Count<RoomEmployment>(SETT.ROOMS().employment.ALL()) {

				@Override
				void count(RoomEmployment t) {
					ee += t.employed()*t.efficiency();
					tot += t.employed();
					
				}

				@Override
				boolean pis() {
					
					if (ee > 10) {
						if (ee/tot < 0.85) {
							return true;
						}
						
					}
					return false;
				}

				@Override
				void clear() {
					ee = 0;
					tot = 0;
				}
				
			};
			
			@Override
			boolean is() {
				return count.is();
			}
			
		};
		
		new Entry(all, UI.icons().m.storage_pull, ¤¤Resources, ¤¤ResourcesG, ¤¤ResourcesB) {

			double spoil = 0;
			private final Count<RESOURCE> count = new Count<RESOURCE>(RESOURCES.ALL()) {

				@Override
				void count(RESOURCE res) {
					double stored = SETT.ROOMS().STOCKPILE.tally().amountReservable.get(res);
					stored += SETT.ROOMS().HAULER.tally.amountReservable.getD(res);
					stored *= SETT.ROOMS().industries.vanillaRate(res)*res.degradeSpeed();
					stored /= 16.0;
					
					
					spoil += stored;
					
				}

				@Override
				boolean pis() {
					if (spoil == 0)
						return false;
					double tres = spoil/(STATS.WORK().workforce()+1);
					return tres > 0.25;
				}

				@Override
				void clear() {
					spoil = 0;
				}
				
			};
			
			@Override
			boolean is() {
				return count.is();
			}
			
		};
		
		new Entry(all, UI.icons().m.coins, ¤¤Money, ¤¤MoneyG, ¤¤MoneyB) {

			@Override
			boolean is() {
				return FACTIONS.player().credits().getD()/(NPCStockpile.AVERAGE_PRICE*4.0*STATS.POP().POP.data(HCLASSES.CITIZEN()).get(null) + STATS.POP().POP.data(HCLASSES.NOBLE()).get(null)) > 1;
			}
			
		};
		
		new Entry(all, UI.icons().m.admin, ¤¤Research, ¤¤ResearchG, ¤¤ResearchB) {
			
			private ArrayListGrower<RoomBlueprintIns<?>> all = new ArrayListGrower<RoomBlueprintIns<?>>();
			{
				for (RoomBlueprintIns<?> p : SETT.ROOMS().ins()) {
					if (p instanceof ROOM_ADMIN_HOLDER) {
						ROOM_ADMIN_HOLDER h = (ROOM_ADMIN_HOLDER) p;
						for (TechCurrency cc : TECHS.COSTS()) {
							if (cc.bo == h.admin().target) {
								all.add(p);
								break;
							}
						}
					}
				}
			}

			@Override
			boolean is() {

				double am = 0;
				for (RoomBlueprintIns<?> blue : all) {
					am += blue.employment().employed();
					if (blue instanceof INDUSTRY_HASER) {
						for (IndustryResource ii : ((INDUSTRY_HASER)blue).industries().get(0).ins()) {
							double a = ii.history().get(1)*(1-TIME.days().bitPartOf()) + ii.history().get(0)*TIME.days().bitPartOf();
							a /= SETT.ROOMS().industries.vanillaRate(ii.resource);
							am += a;
						}
						
					}
				}

				if (am > STATS.WORK().workforce()*0.2)
					return true;
				return false;
			}
			
		};
		
		new Entry(all, UI.icons().m.chainsFree, ¤¤Service, ¤¤ServiceG, ¤¤ServiceB) {

			private final Count<StatServiceRoom> count = new Count<StatServiceRoom>(STATS.SERVICE().ROOMS) {

				double low = 1.0;
				
				@Override
				void count(StatServiceRoom t) {
					low = Math.min(t.usage, low);
				}

				@Override
				boolean pis() {
					return low < 0.5;
				}

				@Override
				void clear() {
					low = 1;
				}
			};
			
			@Override
			boolean is() {
				return count.is();
			}
			
		};
		
		ArrayList<RENDEROBJ> rows = new ArrayList<RENDEROBJ>(all.size()+1);
		
		for (Entry e : all)
			rows.add(e);
		
		add(new GScrollRows(rows, 80*6).view());
		
		addRelBody(8, DIR.N, new GHeader(¤¤name));
		
		
		ACTION restart = new ACTION() {
			
			@Override
			public void exe() {
				
				String race = FACTIONS.player().race().key;
				PBonusSetting ss = FACTIONS.player().bonusesCustom;
				ss.startLevel = FACTIONS.player().level().current().index();
				
				for (RESOURCE res: RESOURCES.ALL()) {
					int am = SETT.ROOMS().STOCKPILE.tally().amountTotal(res)/4;
					ss.startResources.set(res, am);
				}
				
				boolean a = GAME.achieving();
				
				CORE_STATE.Constructor c = new CORE_STATE.Constructor() {

					@Override
					public CORE_STATE getState() {
						String[] sc = GAME.script().currentScripts();
						CORE_STATE s = GAME.create(sc);
						
						return s; 
					}
					
					@Override
					public void doAfterSet() {
						FACTIONS.player().bonusesCustom.copy(ss);
						GAME.achieve(a);
						WorldViewGenerator.setresettle(RACES.map().tryGet(race));
					}
					
				};
				
				CORE.setCurrentState(c);
				
			}
		};
		
		addRelBody(8, DIR.S, new GButt.ButtPanel(¤¤resettle) {
			
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.text(¤¤resettleD);
			}
			
			@Override
			protected void clickA() {
				VIEW.inters().yesNo.activate(¤¤resettleSure, restart, null, true);
			}
			
			@Override
			protected void renAction() {
				activeSet(resettlePossible);
			}
			
		});
		
		
		
	}
	
	private static abstract class Entry extends ClickableAbs{
		
		private final CharSequence sdesc;
		private final String key;
		private final SPRITE icon;
		
		private final GText good;
		private final GText bad;
		
		Entry(ArrayListGrower<Entry> all, SPRITE icon, CharSequence sdesc, CharSequence sgood, CharSequence sbad){
			super(600, 80);
			
			String key = "ADVICE_" + all.size();
			all.add(this);
			this.sdesc = sdesc;
			this.key = key;
			this.icon = icon;
			
			
			good = new GText(UI.FONT().S, sgood);
			good.normalify2();
			good.setMaxWidth(500);
			good.setMultipleLines(true);
			
			bad = new GText(UI.FONT().S, sbad);
			bad.errorify();
			bad.setMaxWidth(500);
			bad.setMultipleLines(true);
			
			
			
			
		}
		
		abstract boolean is();
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
			
			GButt.ButtPanel.renderBG(r, true, PROP.propI(key, 0) == 0, isHovered, body);
			
			icon.renderC(r, body.x1()+24, body.cY());
			
			GText t = is() ? bad : good;
			
			t.renderCY(r, body.x1()+48, body.cY());
			
			GButt.ButtPanel.renderFrame(r, body);
			
		}
		
		@Override
		protected void clickA() {
			PROP.propISet(key, (PROP.propI(key, 0) + 1) & 1);
		}
		
		public boolean toggled() {
			return PROP.propI(key, 0) == 0;
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.text(sdesc);
			b.sep();
			CharSequence t = PROP.propI(key, 0) == 0 ? ¤¤toggleOff : ¤¤toggleOn;
			b.warn(t);
			
			super.hoverInfoGet(text);
		}
		
	}
	
	
	private abstract static class Count<T> {
		
		boolean has = false;
		int ui = 0;
		private final LIST<T> all;
		
		Count(LIST<T> all){
			this.all = all;
		}
		
		boolean is() {
			
			if (GAME.updateI() == ui) {
				return has;
			}
			
			int start = ui;
			int end = GAME.updateI()&Integer.MAX_VALUE;
			if (end-start >= all.size()) {
				start = end-all.size();
			}
			ui = end;
			
			for (; start < end; start++) {
				int ei = start % all.size();
				if (ei == 0) {
					has = pis();
					clear();
				}
				T e = all.get(ei);
				count(e);
			}
			return has;
		}
		
		abstract void count(T t);
		abstract boolean pis();
		abstract void clear();
		
	}
	
}
