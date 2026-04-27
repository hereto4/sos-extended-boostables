package view.world.ui.faction;

import game.boosting.superb.SuperSpec;
import game.faction.FACTIONS;
import game.faction.player.emmi.EmiTypeRoy;
import game.faction.player.emmi.Emissaries;
import game.faction.royalty.Royalty;
import game.faction.royalty.opinion.ROPINIONS;
import init.race.appearence.RPortrait;
import init.settings.S;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.value.GVALUES;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
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
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.data.GETTER;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.slider.GSliderInt;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import view.ui.util.UIValues;

public class UIRoyalty {

	private final GuiSection sec = new GuiSection() {
		@Override
		public void render(SPRITE_RENDERER r, float ds) {
			if (STATS.APPEARANCE().dead.indu().get(roy.induvidual) == 1)
				VIEW.inters().popup.close();
			super.render(r, ds);
		};
	};
	private Royalty roy;
	
	private static CharSequence ¤¤Personality = "Personal Importance:";
	
	static {
		D.ts(UIRoyalty.class);
		
	}
	
	UIRoyalty(){
		GETTER<Royalty> g = new GETTER<Royalty>() {
			@Override
			public Royalty get() {
				return roy;
			}
		};
		
		{
			GuiSection s = new GuiSection();
			s.addRight(4, new GStat(new GText(UI.FONT().H2, 32)) {
				
				@Override
				public void update(GText text) {
					text.lablify().add(roy.name());
				}
			}.r(DIR.NW));
			
			s.addDown(4, new GStat(new GText(UI.FONT().M, 32)) {
				
				@Override
				public void update(GText text) {
					text.lablifySub().add(roy.nameSucc(Str.TMP.clear()));
				}
			}.r(DIR.NW));
			
			for (int i = 0; i < 3; i++) {
				final int k = i;
				s.addDown(i == 0 ? 8 : 0, new HOVERABLE.HoverableAbs(200, 32) {
					
					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
						if (k < roy.traits.size()) {
							GButt.ButtPanel.renderBG(r, true, false, isHovered, body);
							GCOLOR.T().H1.bind();
							UI.FONT().S.renderC(r, body.cX(), body.cY(), roy.traits.get(k).info.name);
							GButt.ButtPanel.renderFrame(r, body);
						}
						
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						if (k < roy.traits.size())
							text.text(roy.traits.get(k).info.desc);
					}
				});
			}
			
			s.addRelBody(16, DIR.W, new Portrait(4, g));
			
			if (S.get().developer) {
				
				s.addRelBody(64, DIR.E, UIValues.butt(GVALUES.ROYALTY, g));
				
			}
			
			sec.add(s);
		}
		
		
		
		
		
		{
			final ArrayList<SuperSpec<Royalty>> specs = new ArrayList<SuperSpec<Royalty>>(ROPINIONS.BOOST().all().size());
			
			GuiSection s = new GuiSection() {
				
				@Override
				public void render(SPRITE_RENDERER r, float ds) {
					specs.clearSloppy();
					for (SuperSpec<Royalty> bb : ROPINIONS.BOOST().all()) {
						if (!bb.hidden || bb.get(roy) != bb.from()) {
							specs.add(bb);
						}
					}
					super.render(r, ds);
				}
				
			};

			GTableBuilder bu = new GTableBuilder() {
				
				@Override
				public int nrOFEntries() {
					return specs.size();
				}
			};
			
			bu.column(null, 760, new GRowBuilder() {
				
				@Override
				public RENDEROBJ build(GETTER<Integer> ier) {
					GButt.BSection ss = new GButt.BSection() {
						@Override
						public void hoverInfoGet(GUI_BOX text) {
							GBox b = (GBox) text;
							if (ier.get() >= specs.size() || specs.get(ier.get()).desc == null)
								return;
							b.text(specs.get(ier.get()).desc);
							b.sep();
							b.textLL(Dic.¤¤Value);
							b.tab(8);
							if (specs.get(ier.get()).isMul) {

								b.add(GFORMAT.f1(b.text(), specs.get(ier.get()).get(roy)));
							}else
								b.add(GFORMAT.f0(b.text(), specs.get(ier.get()).get(roy)));
							b.NL();
							b.textLL(¤¤Personality);
							b.tab(8);
							b.add(GFORMAT.perc(b.text(), specs.get(ier.get()).getModifier(roy)));
						}
					};
					ss.add(new GStat() {

						@Override
						public void update(GText text) {
							text.add(specs.get(ier.get()).info.name);
							text.lablify();
						}
						
					},0,0);
					ss.addRightCAbs(400, new GStat() {
						
						@Override
						public void update(GText text) {
							if (specs.get(ier.get()).isMul) {
								text.add('*');
								GFORMAT.f1(text, specs.get(ier.get()).get(roy));
							}else
								GFORMAT.f0(text, specs.get(ier.get()).get(roy));
						}
					});
					
					ss.addRightC(100, new GStat() {
						
						@Override
						public void update(GText text) {
							text.add('(');
							GFORMAT.f(text, specs.get(ier.get()).from(), 2);
							text.s();
							text.add('<').add('-').add('>');
							text.s();
							GFORMAT.f(text, specs.get(ier.get()).to(), 2);
							text.add(')');
						}
					});
					
					ss.addRightC(160, new GStat() {
						
						@Override
						public void update(GText text) {
							double m = specs.get(ier.get()).getModifier(roy);
							GFORMAT.perc(text, m);
							if (m < 1)
								text.errorify();
							else if (m > 1)
								text.normalify2();
							else
								text.color(COLOR.WHITE65);
						}
					});
					
					ss.body().incrW(100);
					ss.pad(6);
					return ss;
				}
			});
			

			
			
			
			

			
			
			s.add(bu.create(8, false));
			s.addRelBody(4, DIR.N, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.f0(text, ROPINIONS.current(roy));
				}
			}.hh(ROPINIONS.¤¤name).hoverInfoSet(ROPINIONS.¤¤desc));
			s.addRelBody(4, DIR.N, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.f0(text, ROPINIONS.rivalry(roy));
				}
			}.hh(ROPINIONS.¤¤rName).hoverInfoSet(ROPINIONS.¤¤rDesc));
			
			
			sec.addDown(8, s);
			
		}
		
		{
			GuiSection emm = new GuiSection();
			
			emm.add(new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, FACTIONS.player().emissaries.available());
				}
			}.hh(Emissaries.¤¤name));
			
			LinkedList<RENDEROBJ> rows = new LinkedList<>();
			
			for (EmiTypeRoy m : FACTIONS.player().emissaries.roys) {
				
				INTE ii = new INTE() {
					
					@Override
					public int min() {
						return 0;
					}
					
					@Override
					public int max() {
						return m.max(roy);
					}
					
					@Override
					public int get() {
						return m.get(roy);
					}
					
					@Override
					public void set(int t) {
						m.set(roy, t);
					}
				};
				
				GuiSection s = new GuiSection() {
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						super.hoverInfoGet(text);
						if (text.emptyIs()) {
							GBox b = (GBox) text;
							b.title(m.name);
							b.text(m.desc);
						}
					
					}
					
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						if (roy == null || roy.successionI() < 0)
							return;
						super.render(r, ds);
						
						GButt.ButtPanel.renderFrame(r, body());
					}
					
				};
				
				s.add(m.icon, 0, 0);
				s.addRightC(4, new GHeader(m.name));
				
				s.addRightCAbs(220, new GStat() {
					
					@Override
					public void update(GText text) {
						m.formatValue(text, roy);
					}
				});
				
				s.addRightCAbs(220+100, new GSliderInt(ii, 128, true));
				s.pad(4, 4);
				rows.add(s);
				
				emm.addRelBody(2, DIR.S, s);
				
			}
			
			emm.pad(16, 16);
			
			sec.addRelBody(4, DIR.S, emm);
		}
		
		if (S.get().developer) {
			GuiSection s = new GuiSection();
			s.add(new GButt.ButtPanel("assasinate") {
				
				@Override
				protected void clickA() {
					ROPINIONS.EMMI().assasinate(roy, RND.rBoolean());
				}
				
			});
			sec.addRelBody(4, DIR.S, s);
		}
		
	}
	
	public GuiSection get(Royalty roy) {
		this.roy = roy;
		return sec;
		

		
	}
	

	
	static class Portrait extends PortraitAbs{

		
		final GETTER<Royalty> g;
		final int scale;
		final int bar;
		public Portrait(int scale, GETTER<Royalty> g) {
			super(scale);
			this.scale = scale;
			this.g = g;
			bar = (int) (8*Math.ceil(scale/2.0));
		}
		
		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			super.render(r, X1, X2, Y1, Y2);
			int x1 = X1;
			Y1 += 4;
			for (EmiTypeRoy m : FACTIONS.player().emissaries.roys) {
				int am = m.get(g.get());
				if (am > 0) {
					
					OPACITY.O35.bind();
					COLOR.BLACK.render(r, x1, x1+64, Y1, Y1+16);
					OPACITY.unbind();
					m.icon.render(r, x1, Y1);
					Str.TMP.clear();
					GFORMAT.formatI(Str.TMP, am);
					UI.FONT().S.render(r, Str.TMP, x1+18, Y1);
					Y1 += 18;
				}
				
			}
		}

		@Override
		protected Induvidual indu() {
			return g.get().induvidual;
		}

		@Override
		protected int succ() {
			return g.get().successionI();
		}
		
		
	}
	
	public static abstract class PortraitAbs extends SPRITE.Imp{

		private static final ArrayList<COLOR> cols = new ArrayList<COLOR>(
				new ColorImp(127, 127, 50),
				new ColorImp(100, 100, 100),
				new ColorImp(88, 75, 62)
				);

		
		final int scale;
		final int bar;
		public PortraitAbs(int scale) {
			super(RPortrait.P_WIDTH*scale, RPortrait.P_HEIGHT*scale+6*scale);
			this.scale = scale;
			bar = (int) (8*Math.ceil(scale/2.0));
		}
		
		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			Induvidual ro = indu();
			if (ro == null)
				return;
			
			int y = Y1;
			
			STATS.APPEARANCE().portraitRender(r, ro, X1, y, scale);
			
			if (succ() == 0)
				ro.race().appearance().crown.crowns().get(0).renderScaled(r, X1, y+8*scale, scale);
			else {
				cols.getC(succ()-1).bind();
				int w = scale/2;
				w = CLAMP.i(w, 1, 2);
				UI.icons().s.star.render(r, X2-Icon.S*w-4, X2-4, Y1+4, Y1+4+w*Icon.S);
				COLOR.unbind();
			}
		}
		
		protected abstract Induvidual indu();
		protected abstract int succ();
		
	}
	
}
