package view.world.ui.region;


import game.faction.FACTIONS;
import game.faction.FBanner;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import init.settings.S;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.clickable.CLICKABLE.ClickableAbs;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER.GETTER_IMP;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.slider.GSliderInt;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.interrupter.ISidePanel;
import view.interrupter.ISidePanels;
import view.main.VIEW;
import view.tool.ToolManager;
import world.WORLD;
import world.map.regions.Region;
import world.region.RD;

class Other extends ISidePanel implements RV {

	private static CharSequence ¤¤eCurrent = "Assigned envoys";
	private static CharSequence ¤¤eAvailable = "Available envoys";
	static {
		D.ts(Other.class);
	}
	
	public static final int width = 450;
	
	private GETTER_IMP<Region> g = new GETTER_IMP<>();
	private final RENDEROBJ mi = MiscMore.garrison(g, 288);
	public Other(ToolManager m, ISidePanels p) {
		titleSet(Dic.¤¤Region);
		section = new GuiSection() {
			
			@Override
			public void render(SPRITE_RENDERER r, float ds) {
				if (ff != g.get().faction())
					last().remove(Other.this);
				super.render(r, ds);
			}
			
		};
		section.addDown(0, banner(g, this, p));
		section.addDown(8, new Emiss(g));
		section.addRelBody(8, DIR.S, info(g));
		section.addRelBody(8, DIR.S, infoMore(g));
		section.addRelBody(8, DIR.S, buildings(g));
		section.addRelBody(8, DIR.S, more(g));
		
	}
	private Faction ff;

	
	@Override
	public ISidePanel get(Region reg) {
		g.set(reg);
		ff = reg.faction();
		return this;
	}
	
	
	
	private static RENDEROBJ banner(GETTER_IMP<Region> g, ISidePanel panel, ISidePanels p) {
		
		GuiSection s = new GuiSection();
		s.add(new ClickableAbs(Icon.HUGE+16, Icon.HUGE+16) {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				VIEW.world().UI.factions.hover(text, g.get().faction());
				super.hoverInfoGet(text);
			}

			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				Region reg = g.get();
				if (reg.faction() == null) {
					FBanner.rebel.HUGE.renderC(r, body().cX(), body().cY());
				}else {
					GButt.ButtPanel.renderBG(r, isActive, isSelected, isHovered, body());
					reg.faction().banner().HUGE.renderC(r, body().cX(), body().cY());
					if (DIP.WAR().is(FACTIONS.player(), reg.faction())) {
						COLOR.REDISH.bind();
						UI.icons().s.sword.render(r, body().x2()-16, body().y1());
					}
					GButt.ButtPanel.renderFrame(r, body());
				}
				
			}
			
			@Override
			protected void clickA() {
				if (g.get().faction() instanceof FactionNPC) {
					VIEW.world().UI.factions.open((FactionNPC) g.get().faction());
				}
			}
		});
		
		s.add(new GStat(UI.FONT().H2) {

			@Override
			public void update(GText text) {
				text.lablify();
				text.add(g.get().info.name());
			}
			
		}.r(DIR.NW), s.getLastX2()+16, s.getLastY1()+8);
		
		
		if (S.get().developer) {
			s.add(new GButt.ButtPanel(UI.icons().s.cog) {
				@Override
				protected void clickA() {
					p.add(VIEW.world().UI.regions.player.get(g.get()), true);
				}
			}, s.getLastX1(), s.getLastY2()+4);
		}
		
		return s;
	}

	
	private static class Emiss extends GuiSection{
		private final GETTER_IMP<Region> g;
		
		Emiss(GETTER_IMP<Region> g){
			this.g = g;
			
			add(UI.icons().m.flag, 0, 0);
			
			int wi = 150;
			
			RENDEROBJ r = new RENDEROBJ.RenderImp(wi, 18) {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					GMeter.render(r, GMeter.C_REDGREEN, RD.OWNER().affiliation.getD(g.get()), body());
				}
			};
			
			addRightC(16, r);
			
			INTE ii = new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return FACTIONS.player().emissaries.assimilate.max(g.get());
				}
				
				@Override
				public int get() {
					return FACTIONS.player().emissaries.assimilate.get(g.get());
				}

				@Override
				public void set(int t) {
					FACTIONS.player().emissaries.assimilate.set(g.get(), t);
				}
			};
			
			GSliderInt sl = new GSliderInt(ii, wi/2, true);
			
			addRightC(16, sl);
			
			
			
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			b.title(RD.OWNER().affiliation.info().name);
			b.text(RD.OWNER().affiliation.info().desc);
			b.NL(8);
			b.textLL(¤¤eCurrent);
			b.tab(7);
			b.add(GFORMAT.i(b.text(),  FACTIONS.player().emissaries.assimilate.get(g.get())));
			b.NL();
			b.textLL(¤¤eAvailable);
			b.tab(7);
			b.add(GFORMAT.i(b.text(), FACTIONS.player().emissaries.available()));
			b.NL();
		}
		
	}
	
	private static RENDEROBJ info(GETTER_IMP<Region> g) {
		return MiscBasics.info(g);
	}

	
	private static RENDEROBJ infoMore(GETTER_IMP<Region> g) {
		
		GuiSection s = new GuiSection();
		s.add(MiscBasics.rel(g));
		s.addDownC(2, MiscBasics.prospect(g));
		return new Mystery(g, s, 0.1);
	}
	
	private static RENDEROBJ more(GETTER_IMP<Region> g) {
		
		GuiSection ss = new GuiSection();
		ss.body().incrW(64);
		ss.body().incrH(1);
		
		
		ss.addRightC(0, new GStat() {
			
			@Override
			public void update(GText text) {
				GFORMAT.iofkInv(text, RD.MILITARY().garrison.get(g.get()), (int)RD.MILITARY().garrisonTarget(g.get()));
			}
			
			@Override
			public void hoverInfoGet(GBox b) {
				b.title(Dic.¤¤garrison);
			};
			
		}.hv(UI.icons().m.shield));
		ss.addRightC(64, MiscMore.garrison(g, width - ss.body().width()-64 ));

		return new Mystery(g, ss, 0.5);
	}
	
	private static RENDEROBJ buildings(GETTER_IMP<Region> g) {
		RENDEROBJ ii = MiscMore.buildings(g);
		return new Mystery(g, ii, 0.25);
	}
	
	private static class Mystery extends CLICKABLE.ClickWrap{
		
//		private final CLICKABLE place;
//		
		final RENDEROBJ obj;
//		private final double reveal;
//		final GETTER_IMP<Region> g;
		Mystery(GETTER_IMP<Region> g, RENDEROBJ obj, double reveal){
			super(obj);
//			place = new ClickableAbs(obj.body().width(), obj.body().height()) {
//
//				@Override
//				protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
//						boolean isHovered) {
//					GButt.ButtPanel.renderBG(r, true, false, hoveredIs(), body);
//					SPRITES.icons().m.questionmark.renderC(r, body);
//					GButt.ButtPanel.renderFrame(r, body);
//					
//				}
//				
//				@Override
//				public void hoverInfoGet(GUI_BOX text) {
//					GBox b = (GBox) text;
//					GText t = b.text();
//					t.add(¤¤reveal);
//					t.insert(0, (int) (100*reveal));
//					b.text(t);				
//				}
//				
//				@Override
//				protected void clickA() {
//					if (S.get().developer)
//						RD.OWNER().affiliation.set(g.get(), (int)Math.ceil(reveal*RD.OWNER().affiliation.max(g.get())));
//				}
//			};
			
//			this.g = g;
			this.obj = obj;
//			this.reveal = reveal;
		}


		@Override
		protected RENDEROBJ pget() {
//			if (RD.OWNER().affiliation.getD(g.get()) < reveal)
//				return place;
			return obj;
			
		}
		
	}

	private final OtherHov hov = new OtherHov();
	
	@Override
	public void hover(GBox box, Region reg) {
		hov.hover(reg, box);
	}
	
	


	@Override
	public boolean added(ISidePanels pans, Region reg) {
		return pans.added(this) && reg == g.get();
	}
	
	@Override
	protected void update(float ds) {
		WORLD.OVERLAY().hover(g.get());
	};
	
	@Override
	public void hoverGarrison(GBox box, Region reg) {
		box.title(reg.info.name());
		g.set(reg);
		box.add(mi);
	}
	
	
}
