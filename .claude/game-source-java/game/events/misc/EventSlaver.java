package game.events.misc;

import java.io.IOException;

import game.boosting.BOOSTABLES;
import game.events.EVENTS.EventResource;
import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.faction.npc.NPCSlaves;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import init.type.HCLASSES;
import init.type.HTYPES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import util.data.DOUBLE;
import util.data.INT.INTE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GHeader;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.slider.GGaugeMutable;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.interrupter.IDebugPanel;
import view.main.VIEW;
import view.ui.message.MessageSection;

public final class EventSlaver extends EventResource{
	
	private static CharSequence ¤¤Slaver = "¤Slaver!";
	private static CharSequence ¤¤Question = "¤A slaver has arrived and is offering his goods. Would you like to make a purchase or a sale? He will stay for 4 days to await your payments.";
	private static CharSequence ¤¤Purchase = "¤Purchase";
	private static CharSequence ¤¤Expired = "¤Expired";
	

	private double messTime = 0;
	
	public EventSlaver(){
		super("SLAVER");
		D.t(this);
		clear();
		IDebugPanel.add("Slaver", new ACTION() {
			
			@Override
			public void exe() {
				random();
			}
		});
	}
	
	public void random() {
		


		
		SlaverMessage v = new SlaverMessage();
		for (Race r : RACES.all()) {
			int am = (int) (r.population().max * r.bvalue(BOOSTABLES.BEHAVIOUR().SUBMISSION)*500);
			if (!r.playable)
				am *= 0.25;
			am += 1;
			int price = (int) Math.ceil(NPCSlaves.BASE_PRICE(r));
			v.load(r, am, price);
		}
		
		v.send();
	}
	
	@Override
	protected void update(double ds) {
		messTime -= ds;
		if (messTime < 0) {
			if (FACTIONS.player().credits().getD() > 1000 || STATS.POP().POP.data(HCLASSES.SLAVE()).get(null) > 0)
				random();
			messTime = 4*TIME.years().bitSeconds() + RND.rFloat()*6*TIME.years().bitSeconds();
		}
	}
	
	@Override
	protected void save(FilePutter file) {
		file.d(messTime);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		messTime = file.d();
	}
	
	@Override
	protected void clear() {
		messTime = TIME.years().bitSeconds() + RND.rFloat()*3*TIME.years().bitSeconds();
	}
	
	private static class SlaverMessage extends MessageSection {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int[] amounts = new int[RACES.all().size()];
		private double[] prices = new double[RACES.all().size()];
		private int day = TIME.days().bitsSinceStart();
		
		SlaverMessage(){
			super(¤¤Slaver);
		}
		
		void load(Race race, int amount, int price) {
			amounts[race.index] = amount;
			prices[race.index] = price;
		}
		
		private int price(Race r, int am) {
			double h = prices[r.index];
			double lo = Math.ceil(prices[r.index] + prices[r.index]*am*0.001);
			int p = (int) Math.ceil((h+lo)/2.0);
			if (p < 0)
				return 0;
			return (int) (p*Math.abs(am));
		}
		
		private void buy(Race r, int am) {
			prices[r.index] += prices[r.index]*am*0.001;
		}
		
		@Override
		protected void make(GuiSection section) {
			paragraph(¤¤Question);
			section.add(new GHeader(Dic.¤¤Buy), section.body().x1()+24, section.body().y2()+4);
			section.add(new GHeader(Dic.¤¤Sell), section.body().x1()+460, section.getLastY1());
			for (Race r : RACES.playable()) {
				GuiSection s = new GuiSection() {
					
					@Override
					public void render(SPRITE_RENDERER r, float ds) {
						activeSet(TIME.days().bitsSinceStart()-day < 4);
						super.render(r, ds);
					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						GBox b = (GBox) text;
						if (TIME.days().bitsSinceStart()-day >= 4)
							b.error(¤¤Expired);
						else {
							b.title(r.info.name);
							b.text(Dic.¤¤buyPrice);
							b.tab(6);
							b.add(GFORMAT.i(b.text(), price(r, 1)));
							b.NL();
							b.text(Dic.¤¤sellPrice);
							b.tab(6);
							b.add(GFORMAT.i(b.text(), (int)(price(r, 1)*0.5)));
							b.NL();
						}
						
					}
					
				};
				
				s.add(r.appearance().icon, 0, 0);
				
				{
					
					
					s.addRightCAbs(32, new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.i(text, price(r, 1));
						}
					});
					
					INTE in = new INTE() {
						
						int i = 1;
						
						@Override
						public int min() {
							return 0;
						}
						
						@Override
						public int max() {
							return amounts[r.index];
						}
						
						@Override
						public int get() {
							return CLAMP.i(i, 0, max());
						}
						
						@Override
						public void set(int t) {
							i = t;
						}
					};
					
					s.addRightCAbs(48, new GGaugeMutable(in, 150) {
						@Override
						protected int setInfo(DOUBLE d, GText text) {
							GFORMAT.i(text, in.get());
							return 48;
						}
					});
					
					s.addRightCAbs(170, new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.i(text, -price(r, in.get()));
							if (price(r, in.get()) > FACTIONS.player().credits().credits())
								text.errorify();
						}
					});
					
					s.addRightCAbs(64, new GButt.Glow(¤¤Purchase) {
						
						@Override
						protected void renAction() {
							activeSet(active());
						}
						
						private boolean active() {
							return in.get() > 0 &&  TIME.days().bitsSinceStart()-day < 4 && price(r, in.get()) <= FACTIONS.player().credits().credits();
						}
						
						@Override
						protected void clickA() {
							if (active()) {
								
								FACTIONS.player().credits().inc(-price(r, in.get()), CTYPE.MISC);
								buy(r, in.get());
								GBox b = VIEW.timeBox();
								b.add(GFORMAT.i(b.text(), in.get()));
								b.text(r.info.names);
								b.text(Dic.¤¤Bought);
								
								SETT.ENTRY().add(r, HTYPES.SLAVE(), in.get());
								amounts[r.index] -= in.get();
								
								
							}
						}
						
					});
				}
				
				{
					
					INTE in = new INTE() {
						
						int i = 0;
						
						@Override
						public int min() {
							return 0;
						}
						
						@Override
						public int max() {
							return FACTIONS.player().slaves().available(r);
						}
						
						@Override
						public int get() {
							return CLAMP.i(i, 0, max());
						}
						
						@Override
						public void set(int t) {
							i = t;
						}
					};
					
					s.addRightCAbs(180, new GGaugeMutable(in, 150) {
						@Override
						protected int setInfo(DOUBLE d, GText text) {
							GFORMAT.i(text, in.get());
							return 48;
						}
					});
					
					s.addRightCAbs(170, new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.i(text, (long) (price(r, -in.get())*0.5));
						}
					});
					
					s.addRightCAbs(64, new GButt.Glow(Dic.¤¤Sell) {
						
						@Override
						protected void renAction() {
							activeSet(active());
						}
						
						private boolean active() {
							return in.get() > 0 &&  TIME.days().bitsSinceStart()-day < 4 && in.max() > 0;
						}
						
						@Override
						protected void clickA() {
							if (active()) {
								int am = in.get();
								buy(r, -am);
								FACTIONS.player().credits().inc((int) (price(r, -in.get())*0.5), CTYPE.MISC);
								
								for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
									if (am <= 0)
										break;
									if (e instanceof Humanoid) {
										Humanoid h = (Humanoid) e;
										if (h.race() == r && h.indu().hType() == HTYPES.SLAVE()) {
											h.helloMyNameIsInigoMontoyaYouKilledMyFatherPrepareToDie();
											am--;
										}
									}
								}
								
								
								GBox b = VIEW.timeBox();
								b.add(GFORMAT.i(b.text(), am));
								b.text(r.info.names);
								b.text(Dic.¤¤Sold);
								
								
								
								
								
								amounts[r.index] += in.get();
								
								
							}
						}
						
					});
				}
				
	
				
				
				
				
				section.addRelBody(4, DIR.S, s);
			}
			
			
			
			
		}
		
		
	}
	
}
