package view.world.ui.faction;

import game.faction.FACTIONS;
import game.faction.FWorth.WINT;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.faction.royalty.opinion.ROPINIONS;
import game.faction.trade.TradeManager;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.SPRITE;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GHeader;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import world.region.RD;
import world.region.pop.RDRace;

class Hoverer {

	GuiSection s = new GuiSection();
	private final SPRITE ss;
	private FactionNPC f;
	
	private static CharSequence ¤¤powerD = "The power of this faction compared to you. The power is a mix of military might and production capabilities. High powered factions are harder to please.";
	private static CharSequence ¤¤poisioning = "This faction hates you to a degree that it is poisoning all other factions against you. You should try to appease this faction with a gift.";
	
	static {
		D.ts(Hoverer.class);
	}
	
	public Hoverer() {
		s.add(new GStat(UI.FONT().S) {

			@Override
			public void update(GText text) {

				text.lablifySub().add(f.nameIntro);

			}
		}.r(DIR.N), 0, 0);
		s.addDownC(2, new GStat(UI.FONT().H2) {

			@Override
			public void update(GText text) {

				text.lablify().add(f.name);

			}
		}.r(DIR.N));

		s.addRelBody(110, DIR.W, new SPRITE.Imp(Icon.L) {

			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				f.banner().BIG.render(r, X1, X2, Y1, Y2);
			}
		});

		s.addRelBody(110, DIR.E, new SPRITE.Imp(Icon.L) {

			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				f.banner().BIG.render(r, X1, X2, Y1, Y2);
			}
		});

		s.addRelBody(4, DIR.S, new GStat() {

			@Override
			public void update(GText text) {
				if (!RD.DIST().reachable(f)) {
					if (RD.DIST().factionCanAttackPlayerAllies(f))
						text.add(Dic.¤¤FactionBorder);
					else
						text.add(Dic.¤¤Distant);
				}
					
				else
					text.add(DIP.get(f).name);

			}
		}.r(DIR.N));

		GETTER<FactionNPC> g = new GETTER<FactionNPC>() {

			@Override
			public FactionNPC get() {
				return f;
			}

		};


		s.add(facts(g, 3, 100), s.body().x1(), s.body().y2());

		ss = s.asSprite();

	}

	static GuiSection facts(GETTER<FactionNPC> f, int cols, int M) {

		GuiSection s = new GuiSection() {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				if (f.get() == null)
					return;
				super.render(r, ds);
			}
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				if (f.get() == null)
					return;
				super.hoverInfoGet(text);
			}
			
		};
		int i = 0;

		
		{
			GuiSection ss = new GuiSection() {
				@Override
				public void hoverInfoGet(GUI_BOX text) {
					if (f.get() == null || f.get().court().king() == null)
						return;
					text.title(f.get().court().king().roy().induvidual.race().info.namePosessive);
				}
			};
			ss.add(UI.icons().s.crown, 0, 0);
			ss.addRightC(4, new RENDEROBJ.RenderImp(Icon.M) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					f.get().court().king().roy().induvidual.race().appearance().icon.render(r, body);
				}
			});
			
			s.addGridD(ss, i++, cols, M, 20, DIR.W);
		}
		
		s.addGridD(new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.f0(text, (int)(100*ROPINIONS.current(f.get()))/100.0);
			}

			@Override
			public void hoverInfoGet(GBox b) {
				b.title(ROPINIONS.¤¤name);
				b.text(ROPINIONS.¤¤desc);
				b.sep();
				ROPINIONS.BOOST().hoverDetailed(b, f.get().court().king().roy());
			
			};

		}.hh(UI.icons().s.happy), i++, cols, M, 20, DIR.W);
		
		s.addGridD(new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.f0(text, ROPINIONS.peaceValue(f.get()));
			}

			@Override
			public void hoverInfoGet(GBox b) {
				if (f.get() == null)
					return;
				
				b.title(ROPINIONS.¤¤wname);
				b.text(ROPINIONS.¤¤wdesc);
				b.sep();
				
				b.textLL(ROPINIONS.¤¤name);
				b.tab(6); 
				b.add(GFORMAT.f0(b.text(), ROPINIONS.current(f.get())));
				b.NL();
				
				b.textLL(ROPINIONS.¤¤rName);
				b.tab(6); 
				b.add(GFORMAT.f0(b.text(), ROPINIONS.rivalry(f.get())));
				b.NL();
				
				b.textLL(Dic.¤¤Total);
				b.tab(6); 
				b.add(GFORMAT.f0(b.text(), ROPINIONS.peaceValue(f.get())));
				b.NL();
			
			};

		}.hh(UI.icons().s.alert), i++, cols, M, 20, DIR.W);
		
		s.addGridD(new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.i(text, RD.DIST().distance(f.get()));
			}

			@Override
			public void hoverInfoGet(GBox b) {
				b.add(RD.DIST().distance().info());
				b.NL();
				
				if (f.get() == null)
					return;
				
				b.textLL(Dic.¤¤Toll);
				b.tab(6);
				b.add(GFORMAT.f(b.text(), TradeManager.toll(f.get())));
				b.NL();
				
				b.textLL(Dic.¤¤Tariff);
				b.tab(6);
				b.add(GFORMAT.f(b.text(), ROPINIONS.tradeCost(f.get())));
				b.NL();
				
			};

		}.hh(UI.icons().s.wheel), i++, cols, M, 20, DIR.W);
		
		s.addGridD(new GStat() {

			@Override
			public void update(GText text) {
				//GFORMAT.i(text,(long) f.get().power());
				GFORMAT.f1(text, (FACTIONS.player().offensivePower() + 1.0) / (f.get().offensivePower()+1));
				text.clear();
				GFORMAT.f(text, (f.get().offensivePower()+1) / (FACTIONS.player().offensivePower() + 1.0), 1);
			}

			@Override
			public void hoverInfoGet(GBox b) {
				b.title(Dic.¤¤Power);
				b.text(¤¤powerD);
			};

		}.hh(UI.icons().s.fist), i++, cols, M, 20, DIR.W);
		
		s.addGridD(new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.i(text, (int) FACTIONS.WORTH().faction(f.get()));
			}

			@Override
			public void hoverInfoGet(GBox b) {
				b.title(Dic.¤¤NetWorth);
				for (WINT d : FACTIONS.WORTH().faction) {
					b.add(d.icon);
					b.textL(d.info.name);
					b.tab(6);
					b.add(GFORMAT.iIncr(b.text(), d.get(f.get())));
					b.NL();
					b.text(d.info.desc);
					b.NL(5);
				}
				
				
			};

		}.hh(UI.icons().s.money), i++, cols, M, 20, DIR.W);


		
		
		s.addGridD(new GStat() {

			@Override
			public void update(GText text) {
				GFORMAT.i(text, RD.RACES().population.faction().get(f.get()));
			}

			@Override
			public void hoverInfoGet(GBox b) {
				b.title(Dic.¤¤Subject);
				for (RDRace rr : RD.RACES().all) {
					b.add(rr.race.appearance().icon);
					b.text(rr.race.info.names);
					b.tab(7);
					b.add(GFORMAT.i(b.text(), rr.pop.faction().get(f.get())));
					b.NL();
				}
			};

		}.hh(UI.icons().s.human), i++, cols, M, 20, DIR.W);
		
		SPRITE ss = new SPRITE.Imp(140, Icon.S) {

			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				int am = DIP.WAR().all(f.get()).size();
				
				if (am == 0)
					return;
				
				int dx = (width()-24)/am;
				
				dx = CLAMP.i(dx, 1, 24);
				
				double x1 = X1;
				for (Faction fa : DIP.WAR().all(f.get())) {
					fa.banner().MEDIUM.render(r, (int) x1, Y1);
					x1 += dx;
					if (x1 > X2)
						break;
				}
			}
		};

		s.addGridD(new GHeader.HeaderHorizontal(UI.icons().s.sword, ss) {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				GBox b = (GBox) text;
				b.title(Dic.¤¤Enemies);
				for (Faction fa : FACTIONS.all()) {
					if (fa.isActive() && DIP.WAR().is(fa, f.get())) {
						b.add(fa.banner().BIG);
						b.text(fa.name);
						b.NL();
					}
				}

			}
			
		}, i++, cols, M, 20, DIR.W);
		
		s.body().incrW(Math.max(M-s.body().width()-20, 0));
		return s;

	}




	void hover(GUI_BOX box, Faction f) {
		GBox b = (GBox) box;

		if (f == null) {
			b.title(Dic.¤¤NoRuler);
		} else if (f instanceof FactionNPC) {
			hoverFF(b, (FactionNPC) f);
		} else {
			box.title(f.name);
		}
	}

	void hoverFF(GBox b, FactionNPC f) {
		this.f = f;
		b.add(ss);
		
		if (ROPINIONS.isPoisoning(f)) {
			b.NL();
			b.error(¤¤poisioning);
			b.NL();
		}

	}

}
