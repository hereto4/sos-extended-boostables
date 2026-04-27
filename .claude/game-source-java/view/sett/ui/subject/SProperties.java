package view.sett.ui.subject;

import game.GAME;
import game.boosting.BHoverer;
import game.boosting.BOOSTABLES;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import game.time.TIME;
import init.settings.S;
import init.sprite.UI.UI;
import init.type.HTYPE;
import init.type.HTYPES;
import init.type.NEED;
import init.type.NEEDS;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.main.SETT;
import settlement.room.main.RoomInstance;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.colls.StatsNeeds.StatNeedNormal;
import settlement.stats.service.StatService;
import settlement.stats.stat.STAT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.colors.GCOLOR;
import util.colors.GCOLOR_UI;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GMeter;
import util.gui.misc.GMeter.GMeterCol;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;

final class SProperties {
	
	private final GuiSection section = new GuiSection();
	private final AInfo a;
	private static CharSequence ¤¤percPerDay = "¤Increase per day: {0}% / day";
	private static CharSequence ¤¤services = "¤Related Services";
	private static CharSequence ¤¤need = "¤Current Need";
	private static CharSequence ¤¤Tasks = "¤Tasks";
	private static CharSequence ¤¤Status = "¤Status";
	private static CharSequence ¤¤noModule = "¤This type of subject does not engage in this activities";
	private static CharSequence ¤¤serviceDesc = "¤When a subject has spare time, they want to consume services. This is how frequently they'll generally visit services that cater to this need per year.";
	
	private final int hi = 32;
	
	static {
		D.ts(SProperties.class);
	}
	
	SProperties(AInfo a, int height) {
		this.a = a;
		
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		rows.add(h(¤¤Tasks));
		makeModules(rows);
		
		rows.add(h(NEEDS.bCatE().name));
		rows.add(work());
		makeStats(rows);
		makeTraining(rows);
		makeProperties(rows);
	
		GScrollRows sc = new GScrollRows(rows, height-4, 0);
		section.add(sc.view());
		
	}


	private final RENDEROBJ h(CharSequence name) {
		return new RENDEROBJ.RenderImp(10, hi) {
			GText t = new GText(UI.FONT().H2, name).lablify();
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				t.renderCY(r, body().x1()+16, body.cY());
			}
		};
	}
	
	private void makeModules(LinkedList<RENDEROBJ> rows) {
		final GText text = new GText(UI.FONT().S, 6);
		int w = 166;
		int cols = 3;
		
		GuiSection s = null;
		
		for (AIModule m : AI.modules().ALL()) {
			if (m == null)
				continue;
			
			boolean has = true;
			if (!S.get().developer) {
				has = false;
				for (HTYPE t : HTYPES.ALL())
					if (t.player && m.has(t))
						has = true;
				
			}
			if (!has)
				continue;
			
			RENDEROBJ o = new CLICKABLE.ClickableAbs(w, hi) {
				
				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected, boolean isHovered) {
					
					isSelected = m.is(a.a, (AIManager) a.a.ai());
					GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
					GButt.ButtPanel.renderFrame(r, body);
					
					int x1 = body().x1()+8;
					
					m.icon().renderCY(r, x1, body().cY());

					x1+= 20;
					
					
					text.clear();
					text.add(m.name);
					text.setMultipleLines(false);
					text.setMaxWidth(80);
					text.lablify();
					text.renderCY(r, x1, body().cY());
					x1+= 100;

					
					
					int p = m.has(a.a.indu().hType()) ? m.getPriority(a.a, (AIManager) a.a.ai()) : 0;
					
					text.clear();
					GFORMAT.iIncr(text, p);
					text.renderCY(r, x1, body().cY());
					
	
					if (!m.has(a.a.indu().hType())) {
						OPACITY op = OPACITY.O50;
						op.bind();
					
					COLOR.BLACK.render(r, body, -3);
					OPACITY.unbind();
					}

					
				}
				
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					GBox b = (GBox) text;
					b.title(m.name);
					if (m.desc != null)
						b.text(m.desc);
					if (!m.has(a.a.indu().hType())) {
						b.add(b.text().warnify().add(¤¤noModule));
					}
					
					super.hoverInfoGet(text);
				}
			};
			
			
			if (s == null || s.elements().size() >= cols) {
				s = new GuiSection();
				rows.add(s);
			}
			s.addRight(2, o);
			
			
		}
	}
	
	private int upI = -1;
	private double cc = 0;

	private double ni() {
		
		if (upI != GAME.updateI()) {
			upI = GAME.updateI();
			cc = 0;
			for (NEED n : NEEDS.ALLSIMPLE()) {
				cc += n.rate.get(a.a.indu());
			}
			
			cc = 0.5*TIME.servicePerDay()/cc;
		}
		
		return cc;
		
	}
	
	private void makeTraining(LinkedList<RENDEROBJ> rows) {
		rows.add(h(¤¤Status));
		
		rows.add(trainingStat(STATS.BATTLE().ENEMY_KILLS));
		for (StatTraining t : STATS.BATTLE().TRAINING_ALL) {
			rows.add(trainingStat(t.stat));
		}
		rows.add(trainingStat(STATS.EDUCATION().EDUCATION));
		rows.add(trainingStat(STATS.EDUCATION().INDOCTRINATION));
	}
	
	private RENDEROBJ trainingStat(STAT st) {
		GuiSection s = new GuiSection() {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				st.hover(text, a.a.indu());
			}
			
		};
		
		if (st.info().icon != null)
			s.add(st.info().icon, 0, 0);
		
		s.addRightC(4, new GText(UI.FONT().S, st.info().name).lablify());
		
		s.addRightCAbs(300, new GStat() {
			
			@Override
			public void update(GText text) {
				if (st.info().isInt()) {
					GFORMAT.i(text, st.indu().get(a.a.indu()));
				}else {
					GFORMAT.perc(text, st.indu().getD(a.a.indu()));
				}
			}
		});
		return s;
		
	}

	private void makeProperties(LinkedList<RENDEROBJ> rows) {
		
//		MapIndexed<Boostable> dis = new MapIndexed<>();
//		for (StatNeed s : STATS.NEEDS().SNEEDS) {
//			dis.add(s.need.rate);
//		}
//
//		
//		
		final GText text = new GText(UI.FONT().S, 6);
		
		
//		rows.add(new GText(UI.FONT().H2, NEEDS.bCat().name).lablify().r(DIR.E));
//		
		{
			int w = 125;
			int cols = 4;
			
			rows.add(h(NEEDS.bCat().name));
			GuiSection s = null;
			for (NEED n : NEEDS.ALLSIMPLE()) {
				
				HOVERABLE boo = new HOVERABLE.HoverableAbs(w, hi) {
					
					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
						GCOLOR.UI().border().render(r, body,-1);
						GCOLOR.UI().bg(true, false, isHovered).render(r, body,-2);
						double curr = TIME.years().bitConversion(TIME.days())*ni()*n.rate.get(a.a.indu());
						double m = TIME.years().bitConversion(TIME.days())*ni()*n.rate.baseValue;
						
						if (m > 0) {
							COLOR col = GCOLOR_UI.color(GCOLOR.UI().NEUTRAL.inactive, true, false, isHovered);
							double d = (curr/m-0.5);
							d = CLAMP.d(d, 0, 1);
							int w = (int) (d*(body().width()-6));
							col.render(r, body().x1()+3, body().x1()+3+w, body().y1()+3, body().y2()-3);
									
						}
						
						SPRITE ico = n.rate.icon;
						if (n == NEEDS.TYPES().SHRINE)
							ico = STATS.RELIGION().getter.get(a.a.indu()).religion.icon.small;
						else if (n == NEEDS.TYPES().TEMPLE)
							ico = STATS.RELIGION().getter.get(a.a.indu()).religion.icon.small;
						
						ico.renderCY(r, body().x1()+3, body().cY());
						
						text.clear();
						GFORMAT.fRel(text, curr, m);
						text.renderCY(r, body().x1()+23, body().cY());
						
						int x1 =  body().x1()+75;
						
						for (StatService s : STATS.SERVICE().perNeed(n)) {
							icon(r, s.access(a.a), x1);
							x1 += 12;
						}
						
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						b.title(n.rate.name);
						b.text(¤¤serviceDesc);
						text.NL(4);
						GFORMAT.f(b.text(), ni()*n.rate.get(a.a.indu()));
						b.NL(8);
		
						
						b.textLL(¤¤services);
						b.NL();
						
						for (StatService s : STATS.SERVICE().perNeed(n)) {
							hh(b, s.icon(a.a.indu()), s.name, s.access(a.a));
						}
						b.NL();
						b.sep();
						BHoverer.hover(b, n.rate.all(), a.a.indu(), Dic.¤¤Rate, n.rate.baseValue, true);
						
					}
					
					private void icon(SPRITE_RENDERER r, boolean access, int x1) {
						if (access) {
							GCOLOR.UI().GOOD.hovered.bind();
							UI.icons().s.allRight.renderCY(r, x1, body.cY());
						}else {
							GCOLOR.UI().BAD.hovered.bind();
							UI.icons().s.cancel.renderCY(r, x1, body.cY());
						}
					}
					
					private void hh(GBox b, SPRITE icon, CharSequence name, boolean access) {
						b.add(icon);
						b.textLL(name);
						b.tab(7);
						if (access) {
							b.add(UI.icons().s.allRight, GCOLOR.UI().GOOD.hovered);
						}else {
							b.add(UI.icons().s.cancel, GCOLOR.UI().BAD.hovered);
						}
						b.NL();
					}
				};
				
				if (s == null || s.elements().size() >= cols) {
					s = new GuiSection();
					rows.add(s);
				}
				s.addRight(2, boo);
				
			}
		}

		for (BoostableCat cat : BOOSTABLES.colls()) {
			
			GuiSection s = null;
			
		
			rows.add(h(cat.name));
			
			for (Boostable b : cat.all()) {
				
				Boo boo = new Boo(b);
				if (s == null || s.elements().size() >= 5) {
					s = new GuiSection();
					rows.add(s);
				}
				s.addRight(2, boo);
			}
		}
	
	}
	
	private class Boo extends HOVERABLE.HoverableAbs {

		private final Boostable boo;
		private final GText text = new GText(UI.FONT().S, 6);

		Boo(Boostable b){
			super(100, hi);
			this.boo = b;
		}
		
		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			GCOLOR.UI().border().render(r, body,-1);
			GCOLOR.UI().bg(true, false, isHovered).render(r, body,-2);
			double curr = boo.get(a.a.indu());
			double m = boo.max(Induvidual.class);
			
			if (m > 0) {
				COLOR col = GCOLOR_UI.color(GCOLOR.UI().NEUTRAL.inactive, true, false, isHovered);
				int w = (int) (curr/m*(body().width()-6));
				col.render(r, body().x1()+3, body().x1()+3+w, body().y1()+3, body().y2()-3);
						
			}
			
			
			boo.icon.renderCY(r, body().x1()+3, body().cY());
			
			text.clear();
			GFORMAT.fRel(text, curr, a.a.race().bvalue(boo));
			text.renderCY(r, body().x1()+23, body().cY());
			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {			
			hoverBoost(boo, text);
		}
		
		
	}
	
	void makeStats(LISTE<RENDEROBJ> rows){
	
		GText work = new GText(UI.FONT().S, 32);
		
		for (StatNeedNormal s : STATS.NEEDS().SNEEDS) {
			CLICKABLE c = makeNeed(s.need.rate.icon, s, s.stat(), work); 				
			rows.add(c);
		}
		rows.add(makeNeed(SETT.ROOMS().HOSPITAL.icon.small, null, STATS.NEEDS().INJURIES.COUNT, work));
		rows.add(makeNeed(UI.icons().s.heat, null, STATS.NEEDS().EXPOSURE.COUNT, work));
		rows.add(makeNeed(UI.icons().s.clock, null, STATS.NEEDS().EXHASTION, work));
	}
	
	CLICKABLE work() {
		return new CLICKABLE.ClickableAbs(500, hi) {
			GText work = new GText(UI.FONT().S, 32);
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				
				GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
				GButt.ButtPanel.renderFrame(r, body);
				
				int x1 = body().x1()+8;
				
				RoomInstance w = STATS.WORK().EMPLOYED.get(a.a);
				SPRITE ico = w != null ? w.blueprintI().icon.small : UI.icons().s.hammer; 
				ico.renderCY(r, x1, body().cY());

				x1+= 20;
				
				work.setFont(UI.FONT().S);
				work.clear();
				work.add(Dic.¤¤WorkVerb);
				work.setMultipleLines(false);
				work.setMaxWidth(200);
				work.lablify();
				work.renderCY(r, x1, body().cY());
				x1+= 230;
				
				int i = STATS.WORK().WORK_TIME.indu().max(a.a.indu()) - STATS.WORK().WORK_TIME.indu().get(a.a.indu());
				double m = STATS.WORK().WORK_TIME.indu().max(a.a.indu());
				double d = i/m;
				
				GMeterCol c = GMeter.C_GREEN;
				GMeter.render(r, c, 
						d, 
						x1, x1+75, body().y1()+8, body().y2()-8);

				
				x1 += 90;
				
				
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(Dic.¤¤WorkVerb);
				text.NL(4);
				GBox b = (GBox) text;
				b.textL(a.a.title());
				
				
				b.NL(8);
				b.textLL(Dic.¤¤WorkShift);
				b.tab(6);
				{
					GText t = b.text();
					int end = (a.a.getNewDayHour() + TIME.workHours())%24;
					t.add(a.a.getNewDayHour()).add(':').add('0').add('0').s().add('-').s().add(end).add(':').add('0').add('0');
					b.add(t);
				}
				b.NL();
				
				
				
			}
			
			@Override
			protected void clickA() {
				SDebugInput.activate(STATS.WORK().WORK_TIME.indu(), a.a);
			}
		};
	}
	
	CLICKABLE makeNeed(SPRITE icon, StatNeedNormal n, STAT s, GText work) {
		return new CLICKABLE.ClickableAbs(500, hi) {
			
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				
				GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body);
				GButt.ButtPanel.renderFrame(r, body);
				
				int x1 = body().x1()+8;
				
				if (n != null) {

					n.need.rate.icon.renderCY(r, x1, body().cY());
				}else {
					icon.renderCY(r, x1, body().cY());
				}
				x1+= 20;
				
				work.setFont(UI.FONT().S);
				work.clear();
				work.add(s.info().name);
				work.setMultipleLines(false);
				work.setMaxWidth(200);
				work.lablify();
				work.renderCY(r, x1, body().cY());
				x1+= 230;
				
				int i = s.indu().get(a.a.indu());
				double m = s.indu().max(a.a.indu());
				double d = i/m;
				
				GMeterCol c = GMeter.C_GREEN; 
				if (n == null || i > n.breakpoint()) {
					c = GMeter.C_RED;
				}
				GMeter.render(r, c, 
						d, 
						x1, x1+75, body().y1()+8, body().y2()-8);
				
				
				
				if (n != null) {
					int x = (int) (x1 + 75*(n.breakpoint()/m));
					GCOLOR.UI().border().render(r, x, x+1, body().y1()+8, body().y2()-8);
				}
				
				x1 += 90;
				
				work.clear();
				
				if (n != null){
					
					GFORMAT.percInc(work, n.need.rate.get(a.a.indu()), 0);
					work.renderCY(r, x1, body().cY());
					x1 += 45;
					
					for (StatService s : STATS.SERVICE().perNeed(n.need)) {
						boolean ok = access(s, n.need);
						
						if (ok) {
							GCOLOR.UI().GOOD.hovered.bind();
							UI.icons().s.allRight.renderCY(r, x1, body().cY());
						}else {
							GCOLOR.UI().BAD.hovered.bind();
							UI.icons().s.cancel.renderCY(r, x1, body().cY());
						}
						x1 += 12;
					}
					COLOR.unbind();
					
					
				}

			}
			

			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(s.info().name);
				text.text(s.info().desc);
				text.NL(4);
				GBox b = (GBox) text;
				b.textLL(¤¤need);
				b.tab(6);
				if (n != null) {
					double d = (double)s.indu().get(a.a.indu())/n.breakpoint();
					b.add(GFORMAT.perc(b.text(), d));
					b.NL();
				}
				
				
				
				
				if (n != null) {
					

					
					b.textLL(¤¤services);
					b.NL();
					
					for (StatService s : STATS.SERVICE().perNeed(n.need)) {
						b.add(s.icon(a.a.indu()));
						b.textLL(s.name(a.a.indu()));
						b.tab(6);
						if (access(s, n.need)) {
							b.add(UI.icons().s.allRight, GCOLOR.UI().GOOD.hovered);
						}else {
							b.add(UI.icons().s.cancel, GCOLOR.UI().BAD.hovered);
						}
						b.NL();
					}
					b.NL();
					b.sep();
					GText t = b.text();
					t.add(¤¤percPerDay).insert(0, (int)(100*n.need.rate.get(a.a.indu())));
					n.need.rate.hover(b, a.a.indu(), t, true);
				}
			}
			
			@Override
			protected void clickA() {
				SDebugInput.activate(s.indu(), a.a);
			}
		};
	}
	
	private boolean access(StatService s, NEED n) {
		return s.access(a.a);
		
	}
	
	private void hoverBoost(Boostable boo, GUI_BOX text) {
		
		text.title(boo.name);
		text.text(boo.desc);
		text.NL(8);
		boo.hover(text, a.a.indu(), true);
		
	}
	
	GuiSection activate() {
		return section;
	}


}
