package game.faction.royalty.opinion;

import game.boosting.BOOSTABLES;
import game.boosting.superb.SuperSpec.SuperSpecImp;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.diplomacy.DipStance;
import game.faction.npc.FactionNPC;
import game.faction.royalty.Royalty;
import game.faction.royalty.opinion.ROpper.ROpperDown;
import game.time.TIME;
import init.sprite.UI.UI;
import snake2d.util.misc.CLAMP;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import world.army.AD;
import world.battle.BattleListener;
import world.entity.army.WArmy;
import world.map.regions.Region;
import world.region.RD;

public final class OpsStance {

	private static CharSequence ¤¤WarDec = "War Declaration";
	private static CharSequence ¤¤WarDecD = "Wars that have been declared by you against this faction.";
	private static CharSequence ¤¤WarD = "Time spent at war with this faction.";
	private static CharSequence ¤¤agression = "Aggression";
	private static CharSequence ¤¤agressionD = "Aggression you have shown towards other factions by declaring war. The value is negated by the distance from you to the faction at the time of the declaration. Decreases with time.";
	
	private static CharSequence ¤¤chivalry = "Chivalry";
	private static CharSequence ¤¤chivalryD = "Based on your chivalrous actions after a successful battle.";
	private static CharSequence ¤¤cruelty = "Cruelty";
	private static CharSequence ¤¤crueltyD = "Based on your cruel actions after a successful battle.";
	
	private static CharSequence ¤¤joinW = "Joint Wars";
	private static CharSequence ¤¤joinWD = "Joint wars against a common enemy.";
	
	public static CharSequence ¤¤betrayal = "Betrayal";
	private static CharSequence ¤¤betrayalD = "Oaths, such as pacts or alliances that have been broken.";
	
	private static CharSequence ¤¤betrayalH = "When setting a new stance, the faction might feel betrayed. This happens when decreasing your current stance, especially if said stance is new. Declaring war on a faction you have a stance with is also frowned upon. You should break all agreements first. You could also sabotage their opinion of you, and have them break off the treaties for no penalty at all.";
	private static CharSequence ¤¤betrayalTime = "Current treaty freshness.";
	private static CharSequence ¤¤betrayalBase = "Base betrayal inflicted.";
	private static CharSequence ¤¤betrayalThis = "Betrayal inflicted on this faction.";
	private static CharSequence ¤¤betrayalOthers = "Betrayal inflicted on surrounding factions.";
	
	private static CharSequence ¤¤raidingD = "Raids, done by you on this faction's land.";
	private static CharSequence ¤¤peaceD = "From signing a reasonable peace agreement.";
	
	private static CharSequence ¤¤vassalP = "Failed Protection";
	private static CharSequence ¤¤vassalD = "Vassals you have failed to protect.";
	
	private static CharSequence ¤¤trespassing = "Trespassing";
	private static CharSequence ¤¤trespassingD = "By having your armies march in this faction's territory, unless you're allies.";
	
	private static CharSequence ¤¤fatique = "War Fatigue";
	private static CharSequence ¤¤fatiqueD = "Joint was with your allies. No one wants war forever. Aggressive rulers might be more forgiven for drawn-out wars.";
	
	static {
		D.ts(OpsStance.class);
	}
	
	private final ROpperDown agression;
	private final ROpperDown war;
	
	public final SuperSpecImp<Royalty> chivalry;
	public final ROpper cruelty;
	
	final ROpperDown joint;
	private final ROpperDown betrayal;
	private final ROpperDown raiding;
	private final ROpperDown trespassing;
	private final ROpperDown peace;
	private final ROpperDown vassal;
	
	OpsStance(){
		
		double year = 16*TIME.secondsPerDay();
		agression = new ROpperDown("AGRESSION", ¤¤agression, ¤¤agressionD, UI.icons().s.shield, -1, false, year*4) {
			@Override
			public double getModifier(Royalty roy) {
				return 1.0/(0.25+BOOSTABLES.NOBLE().AGRESSION.get(roy.induvidual));
			}
		};
		war = new ROpperDown("WAR_DECLARATION", ¤¤WarDec, ¤¤WarDecD, UI.icons().s.sword, -2, false, year*5);
		
		vassal = new ROpperDown("VASSAL_FAIL", ¤¤vassalP, ¤¤vassalD, UI.icons().s.slave, -20, false, year*4*20) {

			@Override
			public double pget(Royalty roy) {
				if (DIP.VASSAL().is(roy.court.faction)){
					return super.pget(roy);
				}
				return 0;
			}
		};
		
		new ROpper("WAR_FAT", ¤¤fatique, ¤¤fatiqueD, UI.icons().s.time, -15, false) {

			@Override
			public double increase(Royalty roy) {
				if (DIP.get(roy.court.faction).ally && DIP.WAR().all(FACTIONS.player()).size() > 0) {
					return 1.0/(year*10);
				}if (DIP.get(roy.court.faction).trades && DIP.WAR().all(FACTIONS.player()).size() > 0) {
					return 0.25/(year*10);
				}else {
					return - 1.0/(year*2);
				}
			}
			
			@Override
			public double getModifier(Royalty roy) {
				return 1.0/(0.25+BOOSTABLES.NOBLE().AGRESSION.get(roy.induvidual));
			}
		};
		
		new ROpper("WAR", Dic.¤¤War, ¤¤WarD, UI.icons().s.time, -4, false) {

			@Override
			public double increase(Royalty roy) {
				if (DIP.WAR().is(roy.court.faction))
					return 1.0/(year*2);
				else
					return -1.0/(year*4);
			}			
		};
		chivalry = new ROpper("CHIVALRY", ¤¤chivalry, ¤¤chivalryD, UI.icons().s.law, 1, false) {
			
			@Override
			public double getModifier(Royalty roy) {
				return CLAMP.d(BOOSTABLES.NOBLE().MERCY.get(roy.induvidual)-1, 0, 1);
			}
			@Override
			public double pget(Royalty roy) {
				double d = AD.stats().mercy().getD(FACTIONS.player());
				if (d > 0)
					return CLAMP.d(d, 0, 1);
				return 0;
			}
			
		};
		cruelty = new ROpper("CRUELTY", ¤¤cruelty, ¤¤crueltyD, UI.icons().s.law, 1, false) {
			
			@Override
			public double getModifier(Royalty roy) {
				return CLAMP.d(1 - BOOSTABLES.NOBLE().MERCY.get(roy.induvidual), 0, 1);
			}

			@Override
			public double pget(Royalty roy) {
				double d = -AD.stats().mercy().getD(FACTIONS.player());
				if (d > 0)
					return CLAMP.d(d, 0, 1);
				return 0;
			}
		};
		
		joint = new ROpperDown("MUTUAL_WAR", ¤¤joinW, ¤¤joinWD, UI.icons().s.sword, 3, false, year*16) {
			@Override
			public double getModifier(Royalty roy) {
				return BOOSTABLES.NOBLE().AGRESSION.get(roy.induvidual);
			}
			
		};

		peace = new ROpperDown("PEACE", Dic.¤¤peace, ¤¤peaceD, UI.icons().s.sprout, 100, false, year*4*100);
		
		betrayal = new ROpperDown("BETRAYAL", ¤¤betrayal, ¤¤betrayalD, UI.icons().s.sword, -10, false, year*10*2) {
			@Override
			public double getModifier(Royalty roy) {
				return BOOSTABLES.NOBLE().HONOUR.get(roy.induvidual);
			}
		};
		
		raiding = new ROpperDown("RAIDING", Dic.¤¤Raiding, ¤¤raidingD, UI.icons().s.sword, -8, false, year*24);
		
		trespassing = new ROpperDown("TRESPASS", ¤¤trespassing, ¤¤trespassingD, UI.icons().s.sword, -4, false, year*4);
		
		new BattleListener() {
			
			@Override
			public void siege(Faction attacker, Region reg) {
				if (reg.faction() instanceof FactionNPC) {
					FactionNPC ff = (FactionNPC) reg.faction();
					if (DIP.VASSAL().is(ff)) {
						Royalty r = ff.court().king().roy();
						vassal.value.incD(r, 0.25);
					}
				}
				
			}
			
			@Override
			public void siege(WArmy attacker, Region reg) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void battle(Faction a, boolean victory, int losses, int kills, Faction against) {
				if (a != FACTIONS.player())
					return;
				if (!(against instanceof FactionNPC))
					return;
				FactionNPC f = (FactionNPC) against;
				for (Faction o : DIP.WAR().all(f)) {
					if (o != FACTIONS.player()) {
						Royalty r = ((FactionNPC)o).court().king().roy();
						joint.value.incD(r, (double)kills/AD.men(null).faction(f));
					}
					
				}
				
			}

			@Override
			public void battle(WArmy a, boolean victory, int losses, int kills, Faction against) {

			}
		};

	}
	
	public double betrayal(FactionNPC npc, DipStance newStance) {
		return betrayal(npc, DIP.get(npc), newStance);	
	}
	
	public double betrayal(FactionNPC npc, DipStance old, DipStance newStance) {
		
		return betrayalD(npc, old, newStance)*betrayal.to()*betrayal.getModifier(npc.court().king().roy());
		
	}
	
	private double betrayalD(FactionNPC npc, DipStance old, DipStance newStance) {
		
		double dp = newStance.loyalty-old.loyalty;
		if (dp > 0) {
			return dp;
		}
		
		if (newStance == DIP.WAR()) {
			dp*= 2;
			dp -= 1;
		}
		
		double time = DIP.secondSinceStance(npc)/(TIME.secondsPerDay()*16.0*8);
		time = 1.0-time;
		time = CLAMP.d(time, 0.01, 1.0);
		dp*= time;
		
		return dp;
		
	}
	
	public void setNewStance(FactionNPC npc, DipStance newStance, boolean playerDidIt) {
		
		
		DipStance old = DIP.get(npc, FACTIONS.player());
		
		if (old == newStance)
			return;
		
		double betray = -betrayalD(npc, old, newStance);
		if (betray > 0) {
			for (FactionNPC o : FACTIONS.NPCs()) {
				double v = 0.25*CLAMP.d(128.0/RD.DIST().distance(o), 0, 1);
				if (o == npc)
					v = 1.0;
				for (Royalty r : o.court().all()) {
					betrayal.value.incD(r, r.isKing() ? betray*v : betray*0.25*v);
				}
			}
		}
		
		if (old == DIP.WAR()) {
			signPeace();
		}
		
		newStance.set(npc);
		
		

		if (newStance == DIP.WAR()) {
			for (Royalty r : npc.court().all()) {
				war.value.incD(r, r.isKing() ? 1.0 : 0.25);
				joint.value.setD(r, 0);
				peace.value.setD(r, 0);
			}
			for (FactionNPC o : FACTIONS.NPCs()) {
				if (o != npc) {
					double v = 0.5*CLAMP.d(1 - RD.DIST().distance(npc)/256.0, 0, 1);
					for (Royalty r : o.court().all()) {
						
						agression.value.incD(r, r.isKing() ? v : 0.25*v);
					}
				}
			}
			
		}
		
	}
	
	private void signPeace() {
		
		for (Faction f : DIP.WAR().all(FACTIONS.player())) {
			ROPINIONS.setPeaceValue((FactionNPC)f, peace, 0.5);
		}

	}
	
	public void raid(FactionNPC f, double time) {
		
		double inc = 0.1 + time*TIME.secondsPerDayI();
		
		for (Royalty r : f.court().all()) {
			raiding.value.incD(r, r.isKing() ? inc : inc*0.5);
			
		}
	}
	
	public void tresPass(FactionNPC f, double time) {
		
		if (DIP.get(f).ally)
			return;
		
		double inc = 0.1 + time*TIME.secondsPerDayI();
		
		for (Royalty r : f.court().all()) {
			trespassing.value.incD(r, r.isKing() ? inc : inc*0.5);
			
		}
	}
	
	public double trustWorthyness(FactionNPC f) {
		return CLAMP.d(1.0-betrayal.value.getD(f.king()), 0, 1);
	}

	public void betrayalHover(GBox box, FactionNPC npc, DipStance stance) {
		box.title(¤¤betrayal);
		box.text(¤¤betrayalH);
		box.sep();
		box.textLL(¤¤betrayalTime);
		box.NL();
		double time = DIP.secondSinceStance(npc)/(TIME.secondsPerDay()*16.0*8);
		time = 1.0-time;
		time = CLAMP.d(time, 0, 1);
		box.add(GFORMAT.perc(box.text(), time));
		box.sep();
		
		box.textLL(¤¤betrayalBase);
		box.NL();
		box.add(GFORMAT.f0(box.text(), betrayal(npc, stance)));
		box.sep();
		
		box.textLL(¤¤betrayalThis);
		box.NL();
		double m = betrayal(npc, stance)*betrayal.getModifier(npc.king());
		box.add(GFORMAT.f0(box.text(), m));
		box.sep();
		
		box.textLL(¤¤betrayalOthers);
		box.NL();
		m = betrayal(npc, stance)*0.25;
		box.add(GFORMAT.f0(box.text(), m));
		box.sep();
		
	}


	
}
