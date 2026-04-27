package view.sett.ui.standing;

import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import game.time.TIME;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import init.type.HCLASSES;
import init.type.POP_CL;
import settlement.stats.STATS;
import settlement.stats.standing.STANDINGS;
import settlement.stats.standing.StandingCitizen;
import settlement.stats.stat.STAT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.data.INT;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GScrollRows;
import util.gui.table.GStaples;
import util.info.GFORMAT;
import util.text.Dic;
import view.main.VIEW;
import view.sett.ui.standing.Cats.Cat;
import view.sett.ui.standing.decree.UIDecreeButt;

final class CitizenMain extends GuiSection {

	static Race current;
	static int width = 220;
	private final INT.IntImp hov = new INT.IntImp();

	public CitizenMain(int HEIGHT, Cats cats) {

		add(infoButt(cats));
		addRelBody(8, DIR.S, mainHappiness(cats, hov));
		
		ArrayList<RENDEROBJ> rens = new ArrayList<>(STATS.COLLECTIONS().size());
		
		for (Cat c : cats.all) {
			rens.add(new CatButt(cats, c, HCLASSES.CITIZEN(), hov));
		}
		
		
		GScrollRows r = new GScrollRows(rens, HEIGHT -body().height()-4, 0);
		add(r.view(), body().x1(), body().y2()+4);

	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
//		if (current == null)
//			current = GAME.player().race();
		super.render(r, ds);
		hov.set(-1);
	}

	private static RENDEROBJ infoButt(Cats cats) {
		
		GuiSection ss = new GuiSection();
		
		GButt.ButtPanel s = new GButt.ButtPanel(SPRITES.icons().m.questionmark) {
			@Override
			protected void clickA() {
				VIEW.UI().wiki.showRace(current == null ? FACTIONS.player().race() : current);
			};
		};
		s.body.incrW(16);
		ss.add(s);
		
		{
			MenuProp pp = new MenuProp(HCLASSES.CITIZEN());
			GButt.ButtPanel sss = new GButt.ButtPanel(Dic.¤¤Properites) {
				@Override
				protected void clickA() {
					VIEW.s().panels.addDontRemove(VIEW.s().ui.standing, pp);
				}
				@Override
				protected void renAction() {
					selectedSet(VIEW.s().panels.added(pp));
				}
			};
			sss.body.incrW(16);
			ss.addRightC(2, sss);
		}
		
		{

			GETTER<Race> g = new GETTER<Race>() {

				@Override
				public Race get() {
					return current;
				}
				
			};
			GButt.ButtPanel sss = new UIDecreeButt(HCLASSES.CITIZEN(), g);
			ss.addRightC(2, sss);
		}
		
		ss.addRelBody(8, DIR.N, new GStat() {
			
			@Override
			public void update(GText text) {
				text.setFont(UI.FONT().H2);
				text.lablifySub();
				text.add(current == null ? Dic.¤¤All : current.info.names);
			}
		}.r(DIR.N));
		
		return ss;
	}

	private static RENDEROBJ mainHappiness(Cats cats, INT.IntImp hov) {
		GuiSection s = new GuiSection();
		StandingCitizen h = STANDINGS.CITIZEN();

		{
			GuiSection ss = new GuiSection() {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					super.hoverInfoGet(text);
					if (!text.emptyIs())
						return;
					
					GBox b = (GBox) text;
					b.title(h.info().name);
					b.text(h.info().desc);
					b.NL(8);
					
					b.textLL(Dic.¤¤Current);
					b.add(GFORMAT.perc(b.text(), h.loyalty.getD(current)));
					b.add(SPRITES.icons().s.arrow_right);

					b.textLL(Dic.¤¤Target);
					b.tab(6);
					b.add(GFORMAT.perc(b.text(), h.loyaltyTarget.getD(current)));
					b.NL(8);
					
					{
						b.textLL(h.happiness.info().name);
						b.tab(6);
						b.add(GFORMAT.perc(b.text(), h.happiness.getD(current, 0)));
						b.NL();
					}
					
					
					b.NL(8);
					
					BOOSTABLES.BEHAVIOUR().LOYALTY.hoverDetailed(b, POP_CL.clP(current, HCLASSES.CITIZEN()), Dic.¤¤Boosts, true);
				}
			};
			ss.add(new GHeader(h.info().name));
			ss.addRightCAbs(184, new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.perc(text, h.loyalty.getD(current));
				}
			});
//			ss.addRightC(32, new RENDEROBJ.RenderImp(Icon.M) {
//
//				@Override
//				public void render(SPRITE_RENDERER r, float ds) {
//					double now = h.loyalty.getD(current);
//					double t = h.loyaltyTarget.getD(current);
//					int am = (int) CLAMP.d(Math.abs(now - t) * 10, 0, 1);
//					SPRITE a = SPRITES.icons().m.arrow_right;
//					GCOLOR.UI().goodFlash().bind();
//					if (now > t) {
//						GCOLOR.UI().badFlash().bind();
//						a = SPRITES.icons().m.arrow_left;
//					}
//					for (int i = 0; i < am; i++) {
//						a.render(r, body().x1() + i * Icon.M, body().y1());
//					}
//				}
//			});

			RENDEROBJ r = new RENDEROBJ.RenderImp(width, 24) {

				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					double now = h.loyalty.getD(current);
					double t = h.loyaltyTarget.getD(current);
					GMeter.renderDelta(r, now, t, body);
				}
			};

			ss.add(r, 0, ss.body().y2()+4);
			
			GStaples st = new GStaples(STATS.DAYS_SAVED) {

				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {

					isHovered = true;
					setHovered(hov.get());
					super.render(r, ds, isHovered);
				}

				@Override
				protected void hover(GBox box, int stapleI) {
					box.title(h.info().name);
					int fromZero = STATS.DAYS_SAVED - stapleI - 1;
					box.add(box.text().lablify().add(-fromZero).s().add(TIME.days().cycleName()));
					box.NL();
					
					box.textLL(Dic.¤¤Current);
					box.add(GFORMAT.perc(box.text(), h.loyalty.getD(current, fromZero)));
					box.add(SPRITES.icons().s.arrow_right);
					box.textLL(Dic.¤¤Target);
					box.tab(6);
					box.add(GFORMAT.perc(box.text(), h.loyaltyTarget.getD(current, fromZero)));
					box.NL(8);
					
					{
						box.textLL(h.happiness.info().name);
						box.tab(6);
						box.add(GFORMAT.f1(box.text(), h.happiness.getD(current, fromZero)));
						box.NL();
						
					}
					
					
					BOOSTABLES.BEHAVIOUR().LOYALTY.hoverDetailedHistoric(box, POP_CL.clP(current, HCLASSES.CITIZEN()), Dic.¤¤Boosts, true, fromZero);
					
				}

				@Override
				public boolean hover(COORDINATE mCoo) {
					if (super.hover(mCoo)) {
						hov.set(hoverI());
						return true;
					}
					return false;
				}
				
				@Override
				protected double getValue(int stapleI) {
					int fromZero = STATS.DAYS_SAVED - stapleI - 1;
					return h.loyalty.getD(current, fromZero);
				}

				@Override
				protected void setColor(ColorImp c, int stapleI, double value) {
					c.interpolate(GCOLOR.UI().BAD.hovered, GCOLOR.UI().GOOD2.hovered, value);
				}
			};
			st.normalize(false);
			st.body().setWidth(7*STATS.DAYS_SAVED);
			st.body().setHeight(64);
			st.body().centerY(ss);
			st.body().moveX1(width+8);
			ss.add(st);

			s.addDown(2, ss);


		}
		
		
		{
			GuiSection ss = new GuiSection() {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					super.hoverInfoGet(text);
					if (!text.emptyIs())
						return;
					
					GBox b = (GBox) text;
					b.title(h.happiness.info().name);
					b.text(h.happiness.info().desc);
					b.NL(8);
					
					b.textLL(Dic.¤¤Current);
					b.tab(7);
					b.add(GFORMAT.perc(b.text(), h.happiness.getD(current)));
					b.NL(8);
					
					
					
					b.textL(h.fullfillment.info().name);
					b.tab(7);
					b.add(GFORMAT.percBig(b.text(), h.fullfillment.getD(current)));
					b.NL();
					b.text(h.fullfillment.info().desc);
					b.NL(8);
					b.NL();
					b.textL(h.expectation.info().name);
					b.tab(7);
					b.add(GFORMAT.percBig(b.text(), h.expectation.getD(current)));
					b.NL();
					b.text(h.expectation.info().desc);
					b.sep();
					
					//UIDecreeButt.hover(b, HCLASS.CITIZEN, current);
					BOOSTABLES.BEHAVIOUR().HAPPI.hoverDetailed(b, POP_CL.clP(current, HCLASSES.CITIZEN()), Dic.¤¤Boosts, true);
					
				}
			};
			ss.add(new GHeader(h.happiness.info().name));
			ss.addRightCAbs(184, new GStat() {

				@Override
				public void update(GText text) {
					GFORMAT.perc(text, h.happiness.getD(current));
				}
			});

			RENDEROBJ r = new RENDEROBJ.RenderImp(width, 24) {

				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					double now = h.happiness.getD(current);
					GMeter.render(r, GMeter.C_REDGREEN, now, body);

				}
			};
			ss.add(r, 0, ss.body().y2()+4);

			GStaples st = new GStaples(STATS.DAYS_SAVED) {

				@Override
				protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {

					isHovered = true;
					setHovered(hov.get());
					super.render(r, ds, isHovered);
				}

				@Override
				protected void hover(GBox box, int stapleI) {
					int fromZero = STATS.DAYS_SAVED - stapleI - 1;
					box.add(box.text().lablify().add(-fromZero).s().add(TIME.days().cycleName()));
					box.NL();
					
					box.textLL(h.happiness.info().name);
					box.tab(7);
					box.add(GFORMAT.perc(box.text(), h.happiness.getD(current, fromZero)));
					
					box.sep();
					
					BOOSTABLES.BEHAVIOUR().HAPPI.hoverDetailedHistoric(box, POP_CL.clP(current, HCLASSES.CITIZEN()), Dic.¤¤Boosts, true, fromZero);
					
					box.sep();
					
					box.textLL(h.fullfillment.info().name);
					box.tab(7);
					box.add(GFORMAT.percBig(box.text(), h.fullfillment.getD(current, fromZero)));
					box.NL();
					for (Cat ca : cats.all) {
						int v1 = (int)(100*CatButt.Staples.value(stapleI, ca.cs, HCLASSES.CITIZEN()));
						int v2 = v1;
						if (stapleI > 0)
							v2 = (int)(100*CatButt.Staples.value(stapleI-1, ca.cs, HCLASSES.CITIZEN()));
						if (v1 != v2) {
							box.tab(1);
							box.textL(ca.cs[0].info.name);
							box.tab(7);
							double d = (v1-v2)/100.0;
							box.add(GFORMAT.f0(box.text(), d));
							box.NL();
						}
					}
					box.NL(16);
					
					
					box.textLL(h.expectation.info().name);
					box.tab(7);
					box.add(GFORMAT.percBig(box.text(), h.expectation.getD(current, fromZero)));
					if (fromZero < STATS.DAYS_SAVED-1) {
						box.NL();
						double d = h.expectation.getD(current, fromZero);
						double d2 = h.expectation.getD(current, fromZero+1);
						box.tab(7);
						double v = d/d2;
						if (v < 1) {
							box.add(GFORMAT.percInc(box.text(), (1-v)));
						}else if (v > 1) {
							box.add(GFORMAT.percInc(box.text(), -(v-1)));
						}
					}
					
					box.NL(8);
					

					
				}

				@Override
				public boolean hover(COORDINATE mCoo) {
					if (super.hover(mCoo)) {
						hov.set(hoverI());
						return true;
					}
					return false;
				}
				
				@Override
				protected double getValue(int stapleI) {
					int fromZero = STATS.DAYS_SAVED - stapleI - 1;
					return h.happiness.getD(current, fromZero);
				}

				@Override
				protected void setColor(ColorImp c, int stapleI, double value) {
					c.interpolate(GCOLOR.UI().BAD.hovered, GCOLOR.UI().GOOD2.hovered, value);
				}
			};
			st.normalize(false);
			st.body().setWidth(7*STATS.DAYS_SAVED);
			st.body().setHeight(64);
			st.body().centerY(ss);
			st.body().moveX1(width+8);
			ss.add(st);

			s.addDown(2, ss);

		}

		{
			GuiSection ss = new GuiSection();
			ss.add(new GHeader(h.fullfillment.info().name));
			ss.hoverTitleSet(h.fullfillment.info().name).hoverInfoSet(h.fullfillment.info().desc);
			ss.addRightC(16, new GStat() {
				
				@Override
				public void update(GText text) {
					double c = 0;
					double m = 0;
					for (STAT s : STATS.all()) {
						c += s.standing().get(HCLASSES.CITIZEN(), CitizenMain.current);
						m += s.standing().max(HCLASSES.CITIZEN(), CitizenMain.current) - s.standing().getDismiss(HCLASSES.CITIZEN(), CitizenMain.current);
					}
					GFORMAT.dofk(text, c, m);
				}
			});
			
			ss.addRightC(80, SPRITES.icons().m.arrow_right);
			
			ss.addRightC(4, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.percBig(text, h.fullfillment.getD(current, 0));
				}
			});
			
			s.addRelBody(8, DIR.S, ss);
		}
		


		return s;
	}

}
