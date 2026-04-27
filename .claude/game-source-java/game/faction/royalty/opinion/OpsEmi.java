package game.faction.royalty.opinion;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.player.emmi.EmiTypeRoy;
import game.faction.royalty.Royalty;
import game.time.TIME;
import init.sprite.UI.UI;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Str;
import util.text.D;
import view.ui.message.MessageSection;
import view.world.ui.faction.UIRoyalty;
import world.region.RD;

public final class OpsEmi {

	private static CharSequence ¤¤flattery = "Flattery";
	private static CharSequence ¤¤dflatteryD = "Flattery From your Emissaries.";
	
	private static CharSequence ¤¤sabotage = "Sabotage";
	private static CharSequence ¤¤sabotageD = "Sabotage from your Emissaries.";
	
	private static CharSequence ¤¤assasination = "Assassinations";
	private static CharSequence ¤¤assasinationD = "Assassinations of court members.";
	
	private static CharSequence ¤¤assasinated = "Assassinated!";
	private static CharSequence ¤¤assasinatedSucc = "Our emissaries report, their mission is done. The great lord of {FACTION} slipped last night on their nightdress, leading to a fall down the stone stairs of his bed chamber. Once down, a chandelier happened to fall on top of {NAME}'s head, crushing the skull completely. What a tragedy!";

	private static CharSequence ¤¤assasinatedFail = "Busted!";
	private static CharSequence ¤¤assasinatedFailD = "One of our emissaries serving in the court of {FACTION} was arrested and tortured. Unfortunately, our plans have been compromised. {NAME} knows this, and is not too happy about it. Our 'attempts' will continue, but it will be harder now.";
	
	static {
		D.ts(OpsEmi.class);
	}
	
	private final ROpper good;
	private final ROpper bad;
	private final ROpper assas;
	
	OpsEmi(){

		double year = 16*TIME.secondsPerDay();
		
		good = new ROpper("EMMI_GOOD", ¤¤flattery, ¤¤dflatteryD, UI.icons().s.gift,  10, false) {
			
			@Override
			public double increase(Royalty roy) {
				double v = value.getD(roy);
				
				double target = ptarget(roy, FACTIONS.player().emissaries.flatter);
				if (target > v) {
					return 1.0/(year*2);
					
				}else if (target < v){
					return - 1.0/(year*0.5);
				}
				return 0;
			}
			
			@Override
			public void update(Royalty bo, double time) {
				super.update(bo, time);
				double target = ptarget((Royalty)bo, FACTIONS.player().emissaries.flatter);
				if (value.getD(bo) > target)
					value.setD(bo, target);
			}
			
		};
		
		bad = new ROpper("EMMI_BAD", ¤¤sabotage, ¤¤sabotageD, UI.icons().s.gift, -10, false) {
			
			@Override
			public double increase(Royalty roy) {
				double v = value.getD(roy);
				
				double target = ptarget(roy, FACTIONS.player().emissaries.sabotage);
				if (target > v) {
					return 1.0/(year*2);
					
				}else if (target < v){
					return - 1.0/(year*0.5);
				}
				return 0;
			}
			
			@Override
			public void update(Royalty bo, double time) {
				super.update(bo, time);
				double target = ptarget(bo, FACTIONS.player().emissaries.sabotage);
				if (value.getD(bo) > target)
					value.setD(bo, target);
			}
			
		};
		
		assas = new ROpper.ROpperDown("EMMI_ASSES", ¤¤assasination, ¤¤assasinationD, UI.icons().s.death, -10, false, year*4) {
			
			@Override
			public void update(Royalty roy, double time) {
				
				double t = assasinationValue(roy);
				int a = (int) state.getD(roy);
				state.incD(roy, t*3.0*time/year);
				int n = (int) state.getD(roy);
				if (a != n) {
					long ran = STATS.RAN().getL(roy.induvidual, a%32);
					if ((ran & 0b11) == 0) {
						assasinate(roy, true);
						GAME.count().ROYALTIES_KILLED.inc(1);
					}else {
						assasinate(roy, false);
					}
				}
				super.update(roy, time);
			}
			
		};
		
		
	}
	
//	public void flatter(Royalty roy, double am) {
//		flattery.count.incD(roy, am);
//	}
//	
//	public double flattery(Royalty roy) {
//		return flattery.count.getD(roy)/flattery.max();
//	}
	
	public void assasinate(Royalty roy, boolean kill) {
		
		assas.value.incD(roy, 0.25);
		if (kill) {
			roy.kill(false);
			new Mess(¤¤assasinated, ¤¤assasinatedSucc, roy).send();
		}else {
			new Mess(¤¤assasinatedFail, ¤¤assasinatedFailD, roy).send();
		}
		
	}
	
	public double assasinationValue(Royalty roy) {
		double t = ptarget(roy, FACTIONS.player().emissaries.assasinate);
		t *= 0.1 + 1.0 - 0.2*assas.state.getD(roy);
		return t;
		
	}
	
	public double flatteryValue(Royalty roy) {
		return ptarget(roy, FACTIONS.player().emissaries.flatter)*10;
	}
	
	public double flatteryCurrent(Royalty roy) {
		return good.get(roy);
		
	}
	
	public double sabotageValue(Royalty roy) {
		return -ptarget(roy, FACTIONS.player().emissaries.sabotage)*10;
		
	}
	
	private double ptarget(Royalty roy, EmiTypeRoy t) {
		double target = t.getD(roy)*FACTIONS.player().emissaries.penaltyMul();
		double size = 1 + RD.RACES().population.faction().get(roy.court.faction);
		size = (RD.RACES().maxPop()*1.5) / size;
		target = CLAMP.d(target*size, 0, 1);
		
		
		return target;
	}
	
	public double sabotage(Royalty roy) {
		return bad.value.getD(roy);
	}

	private static class Mess extends MessageSection {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final String desc;
		private final Induvidual indu;
		private final String name;
		private final String fName;
		private int sI;
		
		public Mess(CharSequence title, CharSequence desc, Royalty roy) {
			super(title);
			this.desc = ""+desc;
			this.name = ""+roy.name();
			this.indu = roy.induvidual;
			sI = roy.successionI();
			fName = ""+roy.court.faction.name;
		}

		@Override
		protected void make(GuiSection section) {
			paragraph(Str.TMP.clear().add(desc).insert("NAME", name).insert("FACTION", fName));
			section.addRelBody(8, DIR.N, new UIRoyalty.PortraitAbs(4) {
				
				@Override
				protected int succ() {
					return sI;
				}
				
				@Override
				protected Induvidual indu() {
					return indu;
				}
			});
			
			
		}
	}

	
	
}
