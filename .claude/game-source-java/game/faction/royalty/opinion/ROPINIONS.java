package game.faction.royalty.opinion;

import game.GAME;
import game.boosting.BOOSTABLES;
import game.boosting.superb.SuperBoostable;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.faction.royalty.Royalty;
import game.time.TIME;
import init.type.POP_CL;
import init.value.GVALUES;
import snake2d.util.misc.CLAMP;
import util.data.DOUBLE_O;
import util.text.D;
import world.region.RD;

public class ROPINIONS {

	static ROPINIONS self;
	public static CharSequence ¤¤name = "Opinion";
	public static CharSequence ¤¤desc = "The opinion of a royalty regarding you. High opinion help diplomacy, allows for higher stances, and prevents war.";
	
	public static CharSequence ¤¤rName = "Rivalry";
	public static CharSequence ¤¤rDesc = "Rivalry is based on your wealth against any factions wealth. High rivalry might cause the faction to attack you.";
	
	public static CharSequence ¤¤wname = "Attack Chance";
	public static CharSequence ¤¤wdesc = "As you grow, so will the rivalry increase between you and other factions. IF rivalry outweighs a faction's opinion of you, you are at the risk of being attacked.";
	
	static {
		D.ts(ROPINIONS.class);
	}
	
	

	
	private final OpsStance stance;
	private final OpsGifts gifts;
	private final OpsEmi emi;
	private final OpsOther other;
	
	public ROPINIONS(FACTIONS factions) {
		self = this;

		double year = TIME.secondsPerDay() * 16;
		stance = new OpsStance();
		gifts = new OpsGifts(year);
		emi = new OpsEmi();
		other = new OpsOther(year);
		
		GVALUES.FACTION.push("OPINION", ¤¤name, BOOSTABLES.CIVICS().bOpinion.icon, new DOUBLE_O<Faction>() {
			
			@Override
			public double getD(Faction t) {
				if (t instanceof FactionNPC) {
					return current((FactionNPC) t);
				}
				return 0;
			}
		});
	}

	public static SuperBoostable<Royalty> BOOST() {
		return GAME.BOOSTS().OPINION;
	}

	public static OpsStance STANCE() {
		return self.stance;
	}

	public static OpsGifts GIFTS() {
		return self.gifts;
	}

	public static OpsEmi EMMI() {
		return self.emi;
	}

	public static OpsOther OTHER() {
		return self.other;
	}

	public static double current(FactionNPC f) {
		if (f != null && f.court().king() != null)
			return current(f.court().king().roy());
		return 0;
	}

	public static double current(Royalty roy) {
		return BOOST().get(roy);
	}
	
	public static double peaceValue(FactionNPC f) {
		return current(f.court().king().roy())+rivalry(f);
	}
	
	static void setPeaceValue(FactionNPC f, ROpper op, double targetValue) {
		
		
		double v = getPeaceValue(f, op, targetValue);
		

		for (Royalty roy : f.court().all()) {
			op.value.setD(roy, roy == f.king() ? v : v*0.5);
		}
	}
	
	static double getPeaceValue(FactionNPC f, ROpper op, double targetValue) {
		
		Royalty k = f.king();
		double o = op.value.getD(k);
		op.value.setD(k, 0);
		
		if (peaceValue(f) > targetValue){
			op.value.setD(k, o);
			return 0;
			
		}

		double inc = 1.0;
		
		while(peaceValue(f) < targetValue) {
			double prev = peaceValue(f);
			op.value.incD(k, inc);
			if (prev == peaceValue(f))
				break;
			if (peaceValue(f) > targetValue) {
				op.value.incD(k, -inc);
				inc /= 2;
			}
			
		}
		
		double v = op.value.getD(k);
		op.value.setD(k, o);
		return v;
	}
	
	static void setOpinionValue(FactionNPC f, ROpper op, double targetValue) {
		
		double v = getOpinionValue(f, op, targetValue);
		for (Royalty roy : f.court().all()) {
			op.value.setD(roy, roy == f.king() ? v : v*0.5);
		}
	}
	
	static double getOpinionValue(FactionNPC f, ROpper op, double targetValue) {
		
		
		Royalty k = f.king();
		double o = op.value.getD(k);
		op.value.setD(k, 0);
		
		if (op.to() > 0) {
			if (current(f) > targetValue) {
				op.value.setD(k, o);
				return 0;
			}

			double inc = 1.0;
			
			while(current(f) < targetValue) {
				double prev = current(f);
				op.value.incD(k, inc);
				if (prev == current(f))
					break;
				if (current(f) > targetValue) {
					op.value.incD(k, -inc);
					inc /= 2;
				}
				
			}
		}else {
			if (current(f) < targetValue) {
				op.value.setD(k, o);
				return 0;
			}

			double inc = 1.0;
			
			while(current(f) > targetValue) {
				double prev = current(f);
				op.value.incD(k, inc);
				if (prev == current(f))
					break;
				if (current(f) < targetValue) {
					op.value.incD(k, -inc);
					inc /= 2;
				}
				
			}
		}
		
		double v = op.value.getD(k);
		op.value.setD(k, o);
		return v;
	}
	
	public static double peaceValue(Royalty roy) {
		if (roy == null)
			return 0;
		return current(roy)+rivalry(roy);
	}
	
	public static double rivalry(FactionNPC f) {
		if (f == null || DIP.OVERLORD().is(f)) {
			return 0;
		}else {
			double ff = FACTIONS.WORTH().faction(f);
			if (ff < 0)
				return 0;
			double d = 5.0*FACTIONS.WORTH().faction()/ff;
			
			if (DIP.VASSAL().is(f)) {
				d *= CLAMP.d(DIP.secondSinceStance(f)/(TIME.secondsPerDay()*16*8), 0, 1);
			}
			return -CLAMP.d(d/BOOSTABLES.CIVICS().PASIFISM.get(POP_CL.clP()), 0, 1000);
		}
	}
	
	public static double rivalry(Royalty roy) {
		return rivalry(roy.court.faction);
	}

	private static int upI = -1;
	private static double poison = 0;
	
	public static double poisonIncrease() {
		
		if (upI == GAME.updateI())
			return poison;
		upI = GAME.updateI();
		
		double love = 0;
		double hate = 0;
		
		for (int fi = 0; fi < FACTIONS.NPCs().size(); fi++) {
			FactionNPC f = FACTIONS.NPCs().get(fi);
			double op = poison(f)*RD.RACES().population.get(f.capitolRegion());
			if (op > 0)
				love += op;
			else
				hate -= op;
		}
		
		if (love <= 0)
			poison = 1;
		else
			poison = CLAMP.d(hate/love, 0, 1);
		return poison;
		
	}
	
	public static boolean isPoisoning(FactionNPC f) {
		return poison(f) < 0;
	}
	
	private static double poison(FactionNPC f) {
		return (current(f)-OTHER().poison.get(f.king())) +3;
	}
	
	public static double tradeCost(FactionNPC f) {
		return DIP.get(f).tarif;
	}
	
	public static void trade(FactionNPC f, int price) {
		
		
	}


	
}
