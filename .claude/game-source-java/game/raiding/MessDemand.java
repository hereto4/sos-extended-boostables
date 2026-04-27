package game.raiding;

import java.io.Serializable;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.faction.FResources.RTYPE;
import game.faction.FSlaves;
import game.faction.FWorth;
import init.constant.C;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.STOCKPILE;
import init.resources.STOCKPILE.StockpileImp;
import init.sprite.UI.UI;
import init.type.HTYPES;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LinkedList;
import snake2d.util.sets.Tree;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.misc.GStat;
import util.gui.misc.GText;
import util.gui.misc.GTextR;
import util.gui.table.GRows;
import util.gui.table.GScrollRows;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import view.main.VIEW;
import view.ui.message.MessageSection;

final class MessDemand extends MessageSection{

	private static CharSequence ¤¤title = "Protection?";
	private static CharSequence ¤¤title2 = "Raider Returns";
	
	private static CharSequence ¤¤pay = "¤Pay Up";
	private static CharSequence ¤¤decline = "¤Decline Offer";
	
	private static CharSequence ¤¤proInfo = "¤Our scouts report these people have about {0} soldiers. Paying the sum is a safe bet. Declining might lead to trouble. We have 1 day to decide.";

	private static CharSequence ¤¤nolonger = "You do not have enough resources to meet their demands. ({0})";
	private static CharSequence ¤¤nocreds = "You do not have enough denari to meet their demands.";
	private static CharSequence ¤¤noslaves = "You do not have enough slaves to meet their demands. ({0})";
	
	static {
		D.ts(MessDemand.class);
	}
	
	private static final long serialVersionUID = 1L;
	private final Demand demand;
	
	private final Raider raider;
	private final String[] mess;
	
	public MessDemand(Raider raider) {
		super(raider.raids == 1 ? ¤¤title : ¤¤title2);
		this.raider = raider;
		
		demand = new Demand(raider);
		
		mess = new String[ raider.text.demandBody.size()];
		int mi = 0;
		for (String s : raider.text.demandBody)
			mess[mi++] = s;
	}

	@Override
	protected void make(GuiSection section) {
		
		
		section.addRelBody(32, DIR.N, new RaiderPortrait(4).set(raider));
		
		LinkedList<RENDEROBJ> rows = new LinkedList<>();
		
		for (String ss : mess) {
			for (CharSequence s : UI.FONT().M.getRows(ss, WIDTH)) {
				rows.add(new GTextR(UI.FONT().M, s));
			}
			rows.add(new RENDEROBJ.RenderDummy(10, 8));
		}

		
		section.addRelBody(8, DIR.S, new GScrollRows(rows, C.HEIGHT()-600).view());
		
		
		Str s = Str.TMP.clear();
		s.add(¤¤proInfo);
		s.insert(0, raider.army.men);
		
		
		
		section.addRelBody(16, DIR.S, new GText(UI.FONT().S, ""+s).lablifySub().setMaxWidth(WIDTH));
		
		section.addRelBody(16, DIR.S, demand.section(true));
	}
	
	
	
	static class Demand implements Serializable{
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private int[] resources = new int[RESOURCES.ALL().size()];
		private int[] slaves = new int[RESOURCES.ALL().size()];
		private int credits = 0;
		private boolean payed = false;
		private boolean declined = false;
		private final int iteration;
		private final Raider raider;
		
		public Demand(Raider raider) {
			this(raider, worth(raider));
		}
		
		private static int worth(Raider raider) {
			int wo = (int) FACTIONS.WORTH().raider();
			wo = (int) Math.max(raider.worth, wo /= 4);
			wo = (int) Math.min(wo, FACTIONS.WORTH().raider()/2);
			
			return wo;
		}
		
		private Demand(Raider raider, double cre){
			iteration = GAME.raiders().current.iterration;
			this.raider = raider;
			
			if (ee == null)
				ee = new DEntries();
			
			ee.set(this, (int) cre);
			
		}
		
		boolean canRespond() {
			if (payed)
				return false;
			if (declined || !GAME.raiders().current.canPay(iteration))
				return false;
			return true;
		}
		
		boolean canPay() {
			if (payed)
				return false;
			if (declined || !GAME.raiders().current.canPay(iteration))
				return false;
			if (FACTIONS.player().credits().getD() < credits)
				return false;
			for (RESOURCE res : RESOURCES.ALL()) {
				if (resources[res.index()] > 0 && resources[res.index()] > SETT.ROOMS().STOCKPILE.tally().amountReservable.get(res)) {
					return false;
				}
			}
			for (Race r : RACES.all()) {
				if (slaves[r.index()] > 0 && FACTIONS.player().slaves().available(r) < slaves[r.index()])
					return false;
			}
			return true;
		}
		
		public void hoverInfoGet(GUI_BOX text) {
			GBox b = (GBox) text;
			if (FACTIONS.player().credits().getD() < credits) {
				b.error(¤¤nocreds);
			}
			for (RESOURCE res : RESOURCES.ALL()) {
				if (resources[res.index()] > 0 && resources[res.index()] > SETT.ROOMS().STOCKPILE.tally().amountReservable.get(res)) {
					Str.TMP.clear().add(¤¤nolonger).insert(0, res.names);
					b.error(Str.TMP);
					b.NL();
					break;
				}
			}
			
			for (Race r : RACES.all()) {
				if (slaves[r.index()] > 0 && FACTIONS.player().slaves().available(r) < slaves[r.index()]) {
					Str.TMP.clear().add(¤¤noslaves).insert(0, r.info.names);
					b.error(Str.TMP);
					b.NL();
					break;
				}
			}
		}
		
		public void pay() {
			FACTIONS.player().credits().inc(-credits, CTYPE.TRIBUTE);
			STOCKPILE.StockpileImp stock = new StockpileImp();
			for (RESOURCE res : RESOURCES.ALL())
				stock.set(res, resources[res.index()]);
			
			RESOURCE.remove(stock, RTYPE.SPOILS);
			
			for (Race r : RACES.all()) {
				int am = slaves[r.index()];
				if (am > 0) {
					FACTIONS.player().slaves().trade(r, -am, 0);
				}
			}
		}
		
		public RENDEROBJ section(boolean buttons) {
			
			GuiSection s = new GuiSection();
			
			GRows rr = new GRows(7);
			if (credits > 0) {
				rr.add(new GStat() {
					
					@Override
					public void update(GText text) {
						GFORMAT.i(text, credits);
						if (FACTIONS.player().credits().getD() < credits)
							text.errorify();
						else
							text.normalify();
					}
				}.hhw(UI.icons().m.coins, 64).hoverInfoSet(Dic.¤¤Curr));
			}
			

			
			for (RESOURCE res : RESOURCES.ALL()) {
				if (res.index() < resources.length && resources[res.index()] > 0) {
					rr.add(new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.i(text, resources[res.index()]);
							if (resources[res.index()] > SETT.ROOMS().STOCKPILE.tally().amountReservable.get(res)) {
								text.errorify();
							}else
								text.normalify();
						}
					}.hhw(res.icon(),64).hoverInfoSet(res.name));
				}
			}
			
			for (Race r : RACES.all()) {
				int am = slaves[r.index()];
				if (am > 0) {
					rr.add(new GStat() {
						
						@Override
						public void update(GText text) {
							GFORMAT.i(text, slaves[r.index()]);
							if (FACTIONS.player().slaves().available(r) < slaves[r.index()])
								text.errorify();
							else
								text.normalify();
						}
					}.hhw(r.appearance().icon ,64).hoverInfoSet(r.info.names + " (" + HTYPES.SLAVE().names + ")"));
				}
			}
			
			if (rr.rows().size() > 2) {
				GScrollRows sr = new GScrollRows(rr.rows(), rr.rows().get(0).body().height()*2);
				s.add(sr.view());
			}else {
				for (RENDEROBJ o : rr.rows())
					s.addRelBody(8, DIR.S, o);
				
			}
			
			if (buttons) {
				GuiSection bb = new GuiSection();
				
				bb.add(new GButt.ButtPanel(¤¤pay) {

					@Override
					protected void renAction() {
						activeSet(!expired());
						
					}

					private boolean expired() {
						return declined || payed || !GAME.raiders().current.canPay(iteration);
					}
					
					@Override
					protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
							boolean isHovered) {
						

						super.render(r, ds, isActive, isSelected, isHovered);
						if(!expired() && !canPay()){
							OPACITY.O50.bind();
							COLOR.RED100.render(r, body);
							OPACITY.unbind();
						}
					}
					
					@Override
					protected void clickA() {
						if (canPay()) {
							payed = true;
							pay();
							GAME.raiders().current.clear();
							VIEW.messages().hide();
							
							new MessDemandTY(raider).send();
						}

					}
					
					@Override
					public void hoverInfoGet(GUI_BOX text) {
						Demand.this.hoverInfoGet(text);
					}

				});

				bb.addRightC(0, new GButt.ButtPanel(¤¤decline) {

					@Override
					protected void renAction() {
						if (declined)
							selectedSet(true);
						else
							activeSet(!payed && GAME.raiders().current.canPay(iteration));
					}

					@Override
					protected void clickA() {
						declined = true;
						VIEW.messages().hide();
					}

				});
				
				s.addRelBody(16, DIR.S, bb);
			}
			
			
			return s;
			
			
		}
		
	}
	
	private static DEntries ee;
	
	private static class DEntries {
		
		private final ArrayListGrower<DEntry> all = new ArrayListGrower<>();
		private final Tree<DEntry> rtree;
		
		
		DEntries(){
			for (RESOURCE res : RESOURCES.ALL()) {
				all.add(new DEntry() {
					
					@Override
					int max() {
						return (int) (SETT.ROOMS().STOCKPILE.tally().amountReservable.get(res)*0.8);
					}
					
					@Override
					void add(Demand d, int am) {
						d.resources[res.index()] += am;
					}

					@Override
					double value() {
						return FWorth.worthResource(res, 1);
					}
				});
			}
			
			for (Race r : RACES.all()) {
				all.add(new DEntry() {
					
					@Override
					int max() {
						return FACTIONS.player().slaves().available(r)-5;
					}
					
					@Override
					void add(Demand d, int am) {
						d.slaves[r.index()] += am;
					}

					@Override
					double value() {
						return FSlaves.BASE_PRICE(r);
						
					}
				});
			}
			
			rtree = new Tree<DEntry>(all.size()) {
				
				@Override
				protected boolean isGreaterThan(DEntry current, DEntry cmp) {
					return current.value > cmp.value;
				}
			};
		}
		
		void set(Demand d, int credits) {
			
			rtree.clear();
			for (DEntry e : all) {
				e.value = (int) (e.value()*e.max()*RND.rFloat1(0.5));
				if (e.value > 0)
					rtree.add(e);
			}
			
			if (FACTIONS.player().credits().getD() > 0) {
				int cre = (int) (FACTIONS.player().credits().getD()*RND.rFloat());
				if (cre > credits)
					cre = (int) credits;
				d.credits += cre;
				credits -= cre;
			}
			
			while (credits > 0 && rtree.hasMore()) {
				DEntry e = rtree.pollGreatest();
				
				int am = e.max();
				int v = (int) e.value();
				int w = (int) (e.max()*v);
				if (w > credits)
					am = (int) Math.ceil((double)credits/v);
				e.add(d, am);
				credits -= am*v;
			}
			
			if (credits > 0) {
				int am = (int) (FACTIONS.player().credits().getD() - d.credits);
				if (am > credits)
					am = credits;
				if (am > 0)
					d.credits += am;
			}
			
			
		}
		
	}
	
	private static abstract class DEntry {
		
		int value;
		int current;
		
		abstract void add(Demand d, int am);
		abstract int max();
		abstract double value();
		
	}
	
}
