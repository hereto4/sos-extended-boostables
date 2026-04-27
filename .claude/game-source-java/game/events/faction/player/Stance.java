package game.events.faction.player;

import game.boosting.BOOSTABLES;
import game.events.faction.player.EventDiplomacy.EData;
import game.faction.FACTIONS;
import game.faction.diplomacy.DIP;
import game.faction.diplomacy.DipStance;
import game.faction.diplomacy.deal.Deal;
import game.faction.diplomacy.deal.DealBool;
import game.faction.diplomacy.deal.DealDrawfter;
import game.faction.npc.FactionNPC;
import game.faction.npc.stockpile.NPCStockpile;
import game.faction.royalty.opinion.ROPINIONS;
import game.time.TIME;
import init.race.KingMessages;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.text.D;
import view.ui.diplomacy.UIDipMess;
import view.ui.diplomacy.UIDipMessDeal;
import view.ui.message.MessageText;
import world.region.RD;

final class Stance {

	private static CharSequence ¤¤Welcome = "Welcome";
	private static CharSequence ¤¤AgreementCancelled = "¤Agreement Cancelled.";
	private static CharSequence ¤¤AgreementCancelledD = "¤This faction has gone from the stance of {0} to the stance of {1}.";
	
	private static CharSequence ¤¤Warning = "¤Relations Worsen.";
	private static CharSequence ¤¤WarningD = "¤This faction is currently your {0}. If their opinion is not raised in time, it is possible they'll cancel this agreement.";
	
	private static CharSequence ¤¤TradeCancelled = "¤Agreements Cancelled.";
	private static CharSequence ¤¤TradeCancelledD = "¤Since the faction of {0} is no longer reachable to us, all agreements have been annulled.";
	
	private static CharSequence ¤¤title = "Proposal: {0}";

	static {
		D.ts(Stance.class);
	}
	
	boolean process(FactionNPC fa, Induvidual king, EData data) {
		
		if (DIP.secondSinceStance(fa) < TIME.secondsPerDay()) {
			return false;
		}
		
		
		
		if (!RD.DIST().reachable(fa))
			return false;
		
		KingMessages m = king.race().kingMessage();
		
		if (DIP.get(fa).trades && !RD.DIST().reachable(fa)) {
			DIP.NEUTRAL().set(fa, FACTIONS.player());
			new MessageText(¤¤TradeCancelled, Str.TMP.clear().add(¤¤TradeCancelledD).insert(0, fa.name)).send();
			return true;
		}
		
		double opinion = ROPINIONS.current(fa);
		
		if (DIP.TRADE().is(fa)) {
			if (opinion < DIP.TRADE().minLoyalty*0.75) {
				return messDown(fa, DIP.NEUTRAL(), DIP.TRADE(), data);	
			}
			return false;
		}
		
		if (DIP.PACT().is(fa)) {
			if (opinion < DIP.PACT().minLoyalty*0.75) {
				return messDown(fa, DIP.TRADE(), DIP.PACT(), data);
			}
			return false;
		}
		
		if (DIP.ALLY().is(fa)) {
			if (opinion < DIP.ALLY().minLoyalty*0.75) {
				return messDown(fa, DIP.PACT(), DIP.ALLY(), data);
			}
		}
		
		if (!SETT.ROOMS().IMPORT.reqs.passes(FACTIONS.player()))
			return false;
		
		if (!data.welcomed && DIP.NEUTRAL().is(fa)) {
			
			if (!RND.oneIn(4))
				return false;
			
			if (ROPINIONS.peaceValue(fa) > 0.4) {
				
				Deal d = DIP.TMP();
				d.setFactionAndClear(fa);
				double max = giftWorth(fa);
				if (max > 0) {
					if (false) {
						//give away something more fitting, like raw materials, or maybe also slaves.
					}
					DealDrawfter.draft(d, max, false, false);
					if (d.hasDeal()) {
						data.welcomed = true;
						new UIDipMessDeal(¤¤Welcome, m.GREETING.get(fa), d, 0, -0.1).send();
						return true;
					}
					
				}
				
			}
			data.welcomed = true;
			return false;
			
		}
		boolean chance = RND.oneIn(32*(1+RD.DIST().neighs().size()));


		if (!chance)
			return false;
		
		if (DIP.NEUTRAL().is(fa)) {
			if (opinion > DIP.TRADE().minLoyalty + 0.5) {
				messUp(fa, DIP.TMP().bools.TRADE, DIP.TRADE());
				return true;
			}
		}
		
		if (DIP.TRADE().is(fa)) {
			if (opinion > DIP.PACT().minLoyalty + 0.5) {
				messUp(fa, DIP.TMP().bools.PACT, DIP.TRADE());
				return true;
			}
		}
		
		if (DIP.PACT().is(fa)) {
			if (opinion > DIP.ALLY().minLoyalty + 0.5) {
				messUp(fa, DIP.TMP().bools.ALLY, DIP.TRADE());
				return true;
			}
		}
		
		
		
		return false;
		
	}
	
	private static boolean messDown(FactionNPC fa, DipStance downTo, DipStance current, EData data) {
		if (fa.request.has())
			return false;
		KingMessages m = fa.court().king().roy().induvidual.race().kingMessage();
		if (data.stanceMess) {
			
			Str.TMP.clear().add(¤¤AgreementCancelledD);
			Str.TMP.insert(0, DIP.get(fa).name);
			Str.TMP.insert(1, downTo.name);
			new UIDipMess(¤¤AgreementCancelled, m.STANCE_DOWN.get(fa), Str.TMP, fa).send();
			downTo.set(fa);
			data.stanceMess = false;
		}else {
			Str.TMP.clear().add(¤¤WarningD);
			Str.TMP.insert(0, DIP.get(fa).name);
			
			double more = ROPINIONS.GIFTS().getGenerosityNeededForOpinion(fa, current.minLoyalty + 0.5);
			
			Deal d = DIP.TMP();
			d.setFactionAndClear(fa);
			double am = d.getWorthOfOpinion(more)*0.9;
			DealDrawfter.draft(d, -am, false, false);
			if (am > d.valueCredits())
				d.player.credits.i += am - d.valueCredits();
			
			data.stanceMess = true;
			
			new UIDipMessDeal(¤¤Warning, m.STANCE_WARNING.get(fa), d, more, 0).send();
		}
		return true;
	}
	
	private static void messUp(FactionNPC fa, DealBool bool, DipStance stance) {
		if (fa.request.has())
			return;
		Deal d = DIP.TMP();
		d.setFactionAndClear(fa);
		bool.set(true);
		double v = -d.valueCredits();
		double b = v*0.5+(0.5+RND.rFloat());
		DealDrawfter.draft(d, b, false, true);
		if (v < d.player.offerableWorth()) {
			KingMessages m = fa.court().king().roy().induvidual.race().kingMessage();
			new UIDipMessDeal(Str.TMP.clear().add(¤¤title).insert(0, stance.name), m.STANCE_UP.get(fa), d, 0, -0.1).send();
		}
	}
	
	private double giftWorth(FactionNPC fa) {
		Deal d = DIP.TMP();
		double min = NPCStockpile.AVERAGE_PRICE*5;
		double max = NPCStockpile.AVERAGE_PRICE*150;
		max = Math.min(max, d.npc.offerableWorth()*0.025);
		if (max > min) {
			
			return min + BOOSTABLES.NOBLE().PRIDE.get(fa.king().induvidual)*(max-min);
			
		}
		return 0;
	}
	
}
