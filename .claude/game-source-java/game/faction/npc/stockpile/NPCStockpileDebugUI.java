package game.faction.npc.stockpile;

import game.faction.npc.FactionNPC;
import game.faction.npc.stockpile.NPCStockpile.SRes;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import util.data.GETTER;
import util.data.INT.INTE;
import util.gui.misc.GButt;
import util.gui.misc.GInputInt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.slider.GSliderInt;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;

public class NPCStockpileDebugUI extends GuiSection {

	public int[] tamounts = new int[RESOURCES.ALL().size()];

	public NPCStockpileDebugUI(FactionNPC faction) {

		int HEIGHT = 800;

		add(table(faction, HEIGHT - 132));

		{
			GuiSection s = new GuiSection();
			
			s.addRightC(2, new GButt.ButtPanel("update") {
				@Override
				protected void clickA() {
					for (int i = 0; i < tamounts.length; i++) {
						faction.stockpile.res(i).offsetInc(tamounts[i]);
						tamounts[i] = 0;
					}
						
					faction.stockpile.update(faction, 0);
				}
			});

			s.addRightC(2, new GButt.ButtPanel("update day") {
				@Override
				protected void clickA() {
					for (int i = 0; i < tamounts.length; i++) {
						faction.stockpile.res(i).offsetInc(tamounts[i]);
						tamounts[i] = 0;
					}
					faction.stockpile.update(faction, TIME.secondsPerDay());
				}
			});

			s.addRightC(2, new GButt.ButtPanel("clear") {
				@Override
				protected void clickA() {
					faction.stockpile.saver().clear();
					faction.stockpile.update(faction, 0);
					for (int i = 0; i < tamounts.length; i++) {
						tamounts[i] = 0;
					}
				}
			});
			
			addRelBody(2, DIR.S, s);
		}
		
		{
			GuiSection s = new GuiSection();
			s.addRightC(2, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.perc(text, faction.stockpile.creditScore());
				}
			}.hh("score"));
			s.addRightC(120, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, (long) faction.credits().credits());
				}
			}.hh("cash"));
			
			INTE in = new INTE() {
				
				@Override
				public int min() {
					return 0;
				}
				
				@Override
				public int max() {
					return 200000000;
				}
				
				@Override
				public int get() {
					return (int) faction.credits().getD()+100000000;
				}
				
				@Override
				public void set(int t) {
					faction.credits().set(t-100000000);
				}
			};
			
			s.addRightC(120, new GSliderInt(in, 200, true));
			
			s.addRightC(120, new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.i(text, (long) faction.stockpile.credit());
				}
			}.hh("worth"));
			addRelBody(2, DIR.S, s);
			
			
		}

//		section.addRightC(2, new GButt.ButtPanel("r bonus") {
//			@Override
//			protected void clickA() {
//				faction.bonus().randomize();
//			}
//		});

	}

	private  GuiSection table(FactionNPC faction, int height) {
		
	GuiSection section = new GuiSection();
		
		GTableBuilder bu = new GTableBuilder() {
			
			@Override
			public int nrOFEntries() {
				return RESOURCES.ALL().size();
			}
		};
		
		bu.column("", 32, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new RENDEROBJ.RenderImp(24) {
					
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						RESOURCE re = RESOURCES.ALL().get(ier.get());
						re.icon().render(r, body);
					}
				};
			}
		});

		bu.column("rate", 100, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						SRes res = faction.stockpile.res(ier.get());
						GFORMAT.f(text, res.rate());
					}
				}.r(DIR.NW);
			}
		});
		
		bu.column("rateT", 100, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						SRes res = faction.stockpile.res(ier.get());
						GFORMAT.f(text, res.rateTot());
					}
				}.r(DIR.NW);
			}
		});
		
		bu.column("traded", 100, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						SRes res = faction.stockpile.res(ier.get());
						GFORMAT.f(text, res.offset());
					}
				}.r(DIR.NW);
			}
		});
		
		bu.column("amTarget", 100, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						SRes res = faction.stockpile.res(ier.get());
						GFORMAT.f(text, res.amTarget());
					}
				}.r(DIR.NW);
			}
		});
		
		bu.column("amTot", 100, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						SRes res = faction.stockpile.res(ier.get());
						GFORMAT.f(text, res.amTarget() + res.offset());
					}
				}.r(DIR.NW);
			}
		});
		
		bu.column("tradable", 100, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						SRes res = faction.stockpile.res(ier.get());
						GFORMAT.f(text, res.tradeAm());
					}
				}.r(DIR.NW);
			}
		});

		bu.column("priceMul", 100, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						SRes res = faction.stockpile.res(ier.get());
						GFORMAT.f(text, res.amMul(res.amTarget()+res.offset()));
					}
				}.r(DIR.NW);
			}
		});
		

		

		
		bu.column("priceB", 100, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						SRes res = faction.stockpile.res(ier.get());
						GFORMAT.f(text, res.priceBase());
					}
				}.r(DIR.NW);
			}
		});
		

		
		bu.column("price", 100, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						SRes res = faction.stockpile.res(ier.get());
						GFORMAT.f(text, res.price());
					}
				}.r(DIR.NW);
			}
		});
		
		bu.column("S-price", 100, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.f(text, faction.stockpile.priceSell(ier.get(), 1));
					}
				}.r(DIR.NW);
			}
		});
		
		bu.column("B-price", 100, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				return new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.f(text, faction.stockpile.priceBuy(ier.get(), 1));
					}
				}.r(DIR.NW);
			}
		});
		
		bu.column("trade", 100, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				INTE in = new INTE() {
					
					@Override
					public int min() {
						SRes res = faction.stockpile.res(ier.get());
						return -(int)res.tradeAm();
					}
					
					@Override
					public int max() {
						return 1000000;
					}
					
					@Override
					public int get() {
						return tamounts[ier.get()];
					}
					
					@Override
					public void set(int t) {
						tamounts[ier.get()] = t;
					}
				};
				
				return new GInputInt(in);
			}
		});
		
		

		
		section.add(bu.createHeight(height, true));
		faction.stockpile.update(faction, 0);
		return section;
	}

}
