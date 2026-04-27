package view.world.ui.faction;

import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import game.faction.npc.FactionNPC;
import game.faction.npc.stockpile.NPCStockpileDebugUI;
import game.faction.royalty.opinion.ROPINIONS;
import game.faction.trade.TradeManager;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.settings.S;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.POP_CL;
import snake2d.LOG;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StringInputSprite;
import util.data.GETTER;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GChart;
import util.gui.misc.GInput;
import util.gui.misc.GMeter;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.table.GTableBuilder;
import util.gui.table.GTableBuilder.GRowBuilder;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import world.map.pathing.WRegFinder.RegDist;
import world.map.regions.Region;
import world.region.RD;

final class Goods extends GuiSection{

	private static CharSequence ¤¤productionD = "The rate at which this faction produces the resource.";

	private static CharSequence ¤¤priceSell = "The base buy price is proportional to the amount of the resource a faction has stored, and the money it has available.";
	private static CharSequence ¤¤priceBuy = "The base sell price is proportional to the amount of the resource a faction has stored, and the money it has available. If a faction has scant use for a resource, the buy price will be significantly lower than the sell price.";
	private static CharSequence ¤¤penaltyD1 = "Toll is the distance to this faction. This penalty can be decreased by building roads in your kingdom.";
	private static CharSequence ¤¤penaltyD2 = "The tariff penalty is based on the faction's opinion of you. Increase their opinion for better prices.";
	private static CharSequence ¤¤tradeAmount = "Tradable";
	private static CharSequence ¤¤tradeAmountD = "How much that can be traded fairly with this faction per day before it increases tariffs.";
	
	
	static {
		D.ts(Goods.class);
	}
	
	final GETTER<FactionNPC> f;
	
	private ArrayList<RESOURCE> ress = new ArrayList<>(RESOURCES.ALL().size());
	private final StringInputSprite filter = new StringInputSprite(12, UI.FONT().M).placeHolder(Dic.¤¤Search);
	
	Goods(GETTER<FactionNPC> f, int height){
		this.f = f;
		
		GChart chart = new GChart();
		chart.body().setDim(450, 64);
		
		{
			GuiSection s = new GuiSection();
			
			RENDEROBJ rr = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.f(text, TradeManager.toll(f.get()));
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(Dic.¤¤Toll);
					b.text(Dic.¤¤TollD);
					b.sep();
					
					b.textLL(Dic.¤¤Distance);
					b.tab(6);
					b.add(GFORMAT.i(b.text(), RD.DIST().distance(f.get())));
					b.NL();
					
					b.textLL(RD.DIST().boostable.name);
					b.tab(6);
					b.add(GFORMAT.f(b.text(), RD.DIST().boostable.get(POP_CL.clP())));
					b.NL();
					
					b.textLL(Dic.¤¤Total);
					b.tab(6);
					b.add(GFORMAT.f(b.text(), TradeManager.toll(f.get())));
					b.NL();
					
					b.sep();
					RD.DIST().boostable.hover(b, POP_CL.clP(), true);
				};
				
			}.hh(UI.icons().s.wheel);
			s.addRightC(84, rr);
			
			rr = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.percInc(text, f.get().stockpile.creditScore()-1.0);
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(Dic.¤¤CreditScore);
					b.text(Dic.¤¤CreditScoreD);
					b.sep();
					b.textLL(Dic.¤¤Current);
					b.tab(6);
					b.add(GFORMAT.percInc(b.text(), f.get().stockpile.creditScore()-1.0));
				};
				
			}.hh(UI.icons().s.money);
			s.addRightC(84, rr);
			
			rr = new GStat() {
				
				@Override
				public void update(GText text) {
					GFORMAT.percInv(text, ROPINIONS.tradeCost(f.get()));
				}
				
				@Override
				public void hoverInfoGet(GBox b) {
					b.title(Dic.¤¤Tariff);
					b.text(Dic.¤¤TariffD);
				};
				
			}.hh(UI.icons().s.angry);
			s.addRightC(84, rr);
			
			
//			SPRITE ss = new SPRITE.Imp(150, 14) {
//				
//				@Override
//				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
//					GMeter.render(r, GMeter.C_RED, TradeManager.toll(f.get(), RD.DIST().distance(f.get())), X1, X2, Y1, Y2);
//				}
//			};
//			GHeader h = new GHeader.HeaderHorizontal(UI.icons().s.wheel, ss) {
//				
//				@Override
//				public void hoverInfoGet(GUI_BOX text) {
//					GBox b = (GBox) text;
//					b.title(Dic.¤¤Toll);
//					b.text(Dic.¤¤TollD);
//					b.sep();
//					RD.DIST().boostable.hover(b, POP_CL.clP(), true);
//				}
//			};
//			
//
//			
//			s.addRightC(64, h);
//			
//			ss = new SPRITE.Imp(150, 14) {
//				
//				@Override
//				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
//					GMeter.render(r, GMeter.C_RED, ROPINIONS.TRADE().tradeCost(f.get()), X1, X2, Y1, Y2);
//				}
//			};
//			h = new GHeader.HeaderHorizontal(UI.icons().s.angry, ss) {
//				
//				@Override
//				public void hoverInfoGet(GUI_BOX text) {
//					GBox b = (GBox) text;
//					b.title(Dic.¤¤Tariff);
//					b.text(Dic.¤¤TariffD);
//					b.sep();
//					ROPINIONS.hover(b, f.get().court().king().roy(), true);
//				}
//			};
//			s.addRightC(64, h);
			
			s.addRightC(64, new GInput(filter));
			
			if (S.get().developer) {
				s.addRightC(64, new GButt.ButtPanel("debug") {
					@Override
					protected void clickA() {
						VIEW.inters().popup.show(new NPCStockpileDebugUI(f.get()), this, true);
					}
				});
				
				s.addRightC(64, new GButt.ButtPanel(UI.icons().s.wheel) {
					@Override
					protected void clickA() {
						for (RegDist d : RD.DIST().tradePartners(f.get())) {
							LOG.ln(d.reg.faction() + " " + d.distance);
							LOG.ln(d.reg.faction() + " " + TradeManager.totalFee(f.get(), d.reg.faction(), d.distance, RESOURCES.LIVESTOCK(), 1));
							
							
						}
					}
				});
			}
			
			addRelBody(8, DIR.S, s);
			
		}
		
		
		
		
		GTableBuilder builder = new GTableBuilder() {
			
			
			@Override
			public int nrOFEntries() {
				return ress.size();
			}

		};
		
		builder.column(Icon.M*2, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				
				return new HOVERABLE.Sprite(Icon.M) {
					
					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
						g(ier).icon().render(r, body());
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						text.title(g(ier).names);
					}
					
				};
			}
		}, DIR.C);
		
		int W = Icon.M*5+12;
		
		builder.column(Dic.¤¤Price, W+W/2, new GRowBuilder() {
			
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				GStat s = new GStat() {
					
					@Override
					public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
						RESOURCE res = ress.get(ier.get());
						// TODO Auto-generated method stub
						super.render(r, X1-80, X2-80, Y1, Y2);
						statText.clear();
						int p = (int) Math.round(f.get().stockpile.price(res.index(), 0));
						int i = (int) (p-FACTIONS.PRICE().get(res));
						GFORMAT.iIncrI(statText, i);
						statText.adjustWidth();
						statText.render(r, X2-statText.width(), Y1);
					}
					
					@Override
					public void update(GText text) {
						
						RESOURCE res = ress.get(ier.get());
						GFORMAT.i(text, Math.round(f.get().stockpile.price(res.index(), 0)));
					}
					
					@Override
					public void hoverInfoGet(GBox b) {
		
						RESOURCE res = ress.get(ier.get());
						
						int p = (int) Math.round(f.get().stockpile.price(res.index(), 0));
						
						b.title(Dic.¤¤Price);
						
						b.add(UI.icons().s.money);
						b.textL(Dic.¤¤basePrice);
						b.tab(6);
						b.add(GFORMAT.i(b.text(), p));
						b.NL();
						
						b.add(UI.icons().s.money);
						b.textL(Dic.¤¤avePrice);
						b.tab(6);
						b.add(GFORMAT.i(b.text(), FACTIONS.PRICE().get(g(ier))));
						b.NL();
						
						chart.clear();
						chart.add(f.get().stockpile.price.history(g(ier)));
						b.add(chart.sprite);
						
						b.sep();
						
						b.text(Dic.¤¤ProductionRate);
						b.tab(6);
						b.add(GFORMAT.f(b.text(), f.get().stockpile.prodRate(g(ier))));
						b.NL();
						b.text(¤¤productionD);
						
					};
					
				};
				return new Cell(W+W/2, s, DIR.E);
			}
			
		}, DIR.E);
		
		builder.column(Dic.¤¤Sell, W, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				GStat s = new GStat() {
					
					
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, f.get().seller().priceSellP(g(ier)));
					}
					
					@Override
					public void hoverInfoGet(GBox b) {
						int am =  Math.min(f.get().seller().forSale(g(ier))+1, 32);
						int p = f.get().seller().priceSell(g(ier), (int) am)/am;
						//f.get().stockpile.debug(g(ier));
						
						b.title(Dic.¤¤sellPrice);
						b.NL();
						b.text(¤¤priceSell);
						b.NL(8);
						
						b.add(UI.icons().s.money);
						b.textL(Dic.¤¤basePrice);
						b.tab(6);
						b.add(GFORMAT.i(b.text(), p));
						b.NL();
						
						double t = TradeManager.toll(f.get());
						double o = TradeManager.tarif(f.get(), FACTIONS.player(), g(ier), am);
						b.add(UI.icons().s.wheel);
						b.textL(Dic.¤¤Toll);
						b.tab(6);
						b.add(GFORMAT.f0(b.text(), t));
						b.NL();
						
						b.add(UI.icons().s.angry);
						b.textL(Dic.¤¤Tariff);
						b.tab(6);
						b.add(GFORMAT.f0(b.text(), o));
						b.NL();
						
						b.add(UI.icons().s.arrow_right);
						b.textLL(Dic.¤¤Total);
						b.tab(6);
						b.add(GFORMAT.i(b.text(), f.get().seller().priceSellP(g(ier))));
						
						b.NL(8);
						
						b.text(¤¤penaltyD1);
						b.NL(4);
						b.text(¤¤penaltyD2);
					};
					
				};
				return new Cell(W, s, DIR.E);
			}
			
			
			
		}, DIR.E);
		
		builder.column(Dic.¤¤Buy, W, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				GStat s = new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, f.get().seller().priceBuyP(g(ier)));
					}
					
					@Override
					public void hoverInfoGet(GBox b) {
						
						b.title(Dic.¤¤buyPrice);
						b.NL();
						b.text(¤¤priceBuy);
						b.NL(8);
						
						int p = (int) Math.ceil(f.get().buyer().buyPrice(g(ier), 32)/32.0);

						b.add(UI.icons().s.money);
						b.textL(Dic.¤¤basePrice);
						b.tab(6);
						b.add(GFORMAT.i(b.text(), p));
						b.NL();
						
						double t = TradeManager.toll(f.get());
						double o = TradeManager.tarif(FACTIONS.player(), f.get(), g(ier), 32);
						
						b.add(UI.icons().s.wheel);
						b.textL(Dic.¤¤Toll);
						b.tab(6);
						b.add(GFORMAT.f0(b.text(), -t));
						b.NL();
						
						b.add(UI.icons().s.angry);
						b.textL(Dic.¤¤Tariff);
						b.tab(6);
						b.add(GFORMAT.f0(b.text(), -o));
						b.NL();
						
						b.tab(6);
						b.add(UI.icons().s.arrow_right);
						b.textLL(Dic.¤¤Total);
						b.add(GFORMAT.i(b.text(), f.get().buyer().priceBuyP(g(ier))));
						
						b.NL(8);
						
						b.text(¤¤penaltyD1);
						b.NL(4);
						b.text(¤¤penaltyD2);
					};
				};
				return new Cell(W, s, DIR.E);
			}
		}, DIR.E);
		
		builder.column(¤¤tradeAmount, W*2, new GRowBuilder() {
			
			@Override
			public RENDEROBJ build(GETTER<Integer> ier) {
				
				
				GStat s = new GStat() {
					
					@Override
					public void update(GText text) {
						RESOURCE res = g(ier);
						GFORMAT.i(text, (int)f.get().stockpile.playerTradeLimit(res));
					}
					
					@Override
					public void hoverInfoGet(GBox b) {
						b.title(Dic.¤¤taxes);
						RESOURCE res = g(ier);
						for (int ri = 0; ri < f.get().realm().regions(); ri++) {
							Region reg = f.get().realm().region(ri);
							int out = RD.OUTPUT().get(res).getDelivery(reg);
							b.add(UI.icons().s.world);
							b.textL(reg.info.name());
							b.tab(7);
							b.add(GFORMAT.iIncr(b.text(), out));
							b.NL();
							
						}
						
						if (S.get().developer) {
							for (int i = 0; i < 3; i++) {
								b.add(GFORMAT.i(b.text(), f.get().res().in(RTYPE.TAX).history(res).get(i)));
								b.NL();
							}
						}
						
					};
					
				};
				
				SPRITE sp = new SPRITE.Imp(W*2, s.height()) {
					
					@Override
					public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
						s.adjust();
						s.render(r, X2-s.width(), Y1);
						RESOURCE res = g(ier);
						double now = f.get().stockpile.playerTraded(res);
						double max = f.get().stockpile.playerTradeLimit(res);
						
						now/= max;
						now -= 1;
						now *= 0.5;
						now += 0.5;
						GMeter.renderC(r, now, now, X1+4, X2-W-4, Y1, Y2);
						
					}
				};
				
				HOVERABLE h = new HOVERABLE.Sprite(sp) {
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						RESOURCE res = g(ier);
						b.title(¤¤tradeAmount);
						b.text(¤¤tradeAmountD);
						b.NL();
						
						b.textLL(Dic.¤¤Current);
						b.tab(6);
						b.add(GFORMAT.i(b.text(), (long) f.get().stockpile.playerTraded(res)));
						b.NL();
						
						b.textLL(Dic.¤¤limit);
						b.tab(6);
						b.add(GFORMAT.i(b.text(), (long) f.get().stockpile.playerTradeLimit(res)));
						b.NL();
						
						b.textLL(Dic.¤¤Tariff);
						b.tab(6);
						b.add(GFORMAT.f0(b.text(), f.get().stockpile.playerTarif(res, 1)));
						b.NL();
						
						super.hoverInfoGet(text);
					}
					
				};
				
				return h;
			}
		}, DIR.E);
		
//		builder.column(Dic.¤¤ForSale, W, new GRowBuilder() {
//			
//			@Override
//			public RENDEROBJ build(GETTER<Integer> ier) {
//				GStat s = new GStat() {
//					
//					@Override
//					public void update(GText text) {
//						GFORMAT.i(text, f.get().seller().forSale(g(ier)));
//					}
//					
//					@Override
//					public void hoverInfoGet(GBox b) {
//						b.title(Dic.¤¤ForSale);
//						RESOURCE res = g(ier);
//						chart.clear();
//						chart.add(f.get().stockpile.price.history(g(ier)));
//						b.add(chart.sprite);
//						b.sep();
//						for (RTYPE t : RTYPE.all) {
//							b.textL(t.name);
//							b.tab(6);
//							b.add(GFORMAT.iIncr(b.text(), f.get().res().in(t).get(res)));
//							b.tab(8);
//							b.add(GFORMAT.iIncr(b.text(), -f.get().res().out(t).get(res)));
//							b.NL();
//							
//
//						}
//						
//
//						
//					};
//					
//					
//					
//				};
//				return new Cell(W, s, DIR.E);
//			}
//		}, DIR.E);
		
		addRelBody(8, DIR.S, builder.createHeight(height-16-body().height(), true));
		

	}

	private RESOURCE g(GETTER<Integer> ier) {
		return ress.get(ier.get());
	}
	
	@Override
	public void render(SPRITE_RENDERER r, float ds) {
		ress.clearSloppy();
		if (filter.text().length() == 0) {
			ress.add(RESOURCES.ALL());
		}else {
			for (RESOURCE res : RESOURCES.ALL()) {
				if (Str.containsText(res.name, filter.text()) || Str.containsText(res.names, filter.text()))
					ress.add(res);
			}
		}
		
		super.render(r, ds);
	}
	
	private class Cell extends HOVERABLE.HoverableAbs{
		
		private final GStat st;
		private final DIR d;
		Cell(int width, GStat st, DIR d){
			this.st = st;
			this.d = d;
			body.setDim(width, Icon.M);
		}

		@Override
		protected void render(SPRITE_RENDERER r, float ds, boolean isHovered) {
			if (hoveredIs())
				COLOR.WHITE50.render(r, body);
			st.adjust();
			int dx = (body.width()-st.width())/2;
			int dy = (body.height()-st.height())/2;
			st.renderC(r, body.cX()+ dx*d.x(), body.cY()+ dy*d.y());
		}
		
		@Override
		public void hoverInfoGet(GUI_BOX text) {
			st.hoverInfoGet((GBox)text);
		}
		
	}
	

//	public double toll() {
//		return TradeManager.tollPlayer(RD.DIST().distance(f.get()));
//	}

}
