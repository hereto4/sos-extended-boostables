package game.faction.royalty.opinion;

import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.faction.royalty.Royalty;
import game.faction.royalty.opinion.ROpper.ROpperDown;
import game.time.TIME;
import game.tourism.TOURISM;
import init.race.Race;
import init.sprite.UI.UI;
import init.type.HCLASSES;
import settlement.stats.STATS;
import snake2d.util.misc.CLAMP;
import util.text.D;
import world.region.RD;
import world.region.pop.RDRace;

public final class OpsOther {

	private static CharSequence ¤¤liberation = "Liberation";
	private static CharSequence ¤¤liberationD = "Affection from previous liberation of this faction.";
	
	private static CharSequence ¤¤vassal = "Vassalage";
	private static CharSequence ¤¤vassalD = "Time spent as a vassal to our realm.";
	
	private static CharSequence ¤¤vassalT = "Vassal Tribute";
	private static CharSequence ¤¤vassalTD = "Based on the number of gifts that have been declined.";

	
	private static CharSequence ¤¤proximity = "Nearness";
	private static CharSequence ¤¤proximityD = "Based on the distance to this faction, capitol to capitol.";
	
	private static CharSequence ¤¤kinship = "Kinship";
	private static CharSequence ¤¤kinshipD = "Based on your race and this royalties race.";
	
	private static CharSequence ¤¤kinshipT = "Kin Treatment";
	private static CharSequence ¤¤kinshipTD = "How you are treating this royalty's race and its affiliated races.";
	
	private static CharSequence ¤¤poison = "Poison";
	private static CharSequence ¤¤PosionD = "Gained from someone spreading fake news about you to this faction. Someone that doesn't like you.";
	
	public final ROpperDown liberation;
	public final ROpper vassalage;
	public final ROpper proximity;
	public final ROpper vassalTribute;
	public final ROpper kinship;
	public final ROpper kintreatment;
	public final ROpper poison;
	
	static {
		D.ts(OpsOther.class);
	}
	
	OpsOther(double year){
		
		liberation = new ROpperDown("LIBERATION", ¤¤liberation, ¤¤liberationD, UI.icons().s.flags, 10, false, year*16*10);
		vassalage = new ROpper("VASSALAGE", ¤¤vassal, ¤¤vassalD, UI.icons().s.slave, -2, false) {

			@Override
			public double pget(Royalty roy) {
				if (DIP.overlord(roy.court.faction) == FACTIONS.player()) {
					double d = DIP.secondSinceStance(roy.court.faction)/(TIME.secondsPerDay()*16*20);
					d = CLAMP.d(d, 0, 1);
					return 1;
				}
				return 0;
			}
			
		};
		
		proximity = new ROpper("PROXI", ¤¤proximity, ¤¤proximityD, UI.icons().s.wheel, 1, false) {

			@Override
			public double pget(Royalty roy) {
				return 1.0-CLAMP.d(RD.DIST().capitolDist(roy.court.faction)/256.0, 0, 1);
			}
			
		};
		
		poison = new ROpper("POSION", ¤¤poison, ¤¤PosionD, UI.icons().s.death, -20, false) {

			@Override
			public double increase(Royalty roy) {
				double d = 1 - 0.5*BOOSTABLES.NOBLE().HONOUR.get(roy.induvidual);
				double sp = ROPINIONS.poisonIncrease()*d;
				
				if (sp > 0 && !DIP.WAR().is(roy.court.faction)) {
					return sp/(year*20.0);
				}else {
					return -1.0/(year*2);
				}

			}
			
			@Override
			public double getModifier(Royalty roy) {
				return 1 - 0.5*BOOSTABLES.NOBLE().HONOUR.get(roy.induvidual);
			}
		};
		
		vassalTribute = new ROpper("VASSAL_GIFT", ¤¤vassalT, ¤¤vassalTD, UI.icons().s.gift, 3, false) {
			
			
			@Override
			public double pget(Royalty bo) {
				Royalty roy = (Royalty) bo;
				if (DIP.overlord(roy.court.faction) == FACTIONS.player()) {
					return super.pget(bo);
				}
				return 0;
			}
		};
		
		kinship = new ROpper("KINSHIP", ¤¤kinship, ¤¤kinshipD, UI.icons().s.human, 0.75, true) {
			

			
			@Override
			public double getModifier(Royalty roy) {
				return BOOSTABLES.NOBLE().TOLERANCE.get(roy.induvidual);
			}

			@Override
			public double pget(Royalty roy) {
				double d = roy.induvidual.race().pref().race(FACTIONS.player().race());
				d = 1.0 - CLAMP.d(d, 0, 1);
				return d;
			}
		};
		
		kintreatment = new ROpper("KIN_TREATMENT", ¤¤kinshipT, ¤¤kinshipTD, UI.icons().s.human, 0.5, true) {
			
			@Override
			public double getModifier(Royalty roy) {
				return 1.0-0.5*BOOSTABLES.NOBLE().TOLERANCE.get(roy.induvidual);
			}

			@Override
			public double pget(Royalty roy) {
				double c = 0;
				Race ra = roy.induvidual.race();
				c += STATS.MULTIPLIERS().PROSECUTION.value(HCLASSES.CITIZEN(), ra, 0);
				c += 20*STATS.POP().POP.data(HCLASSES.SLAVE()).get(ra)/(1+STATS.POP().POP.data().get(null));
				RDRace rr = RD.RACE(ra);
				if (rr != null) {
					c += RD.RACES().edicts.sanction.realm(rr).getD(FACTIONS.player())*0.25;
					c += RD.RACES().edicts.exile.realm(rr).getD(FACTIONS.player())*0.5;
					c += RD.RACES().edicts.massacre.realm(rr).getD(FACTIONS.player());
				}
				
				if (!TOURISM.permit(ra)) {
					c += 0.1;
				}
				
				c = CLAMP.d(c, 0, 1);
				return c;
			}
		};
		
	}
	
	public void liberate(FactionNPC f) {
		for (Royalty r : f.court().all()) {
			liberation.value.setD(r, r.isKing() ? 1.0 : 0.5);
		}
	}
	
	public void acceptTribute(FactionNPC f, boolean accept) {
		
		double v = vassalTribute.value.getD(f.king()) + (accept ? -0.25 : 0.25);
		v = CLAMP.d(v, -1, 1);
		
		vassalTribute.value.setD(f.king(), v);
	}
	

	
}
