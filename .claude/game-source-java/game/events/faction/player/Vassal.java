package game.events.faction.player;

import game.events.faction.player.EventDiplomacy.EData;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.faction.royalty.opinion.ROPINIONS;
import game.time.TIME;
import util.text.D;
import view.ui.diplomacy.UIDipMess;
import view.ui.diplomacy.UIDipMessAction;

final class Vassal {

	private static CharSequence ¤¤breakTitle = "¤Freedom request";
	private static CharSequence ¤¤breakBody = "¤This faction asks that you release them from their bounds. The faction will become your colleague, and the faction will be very grateful should you accept.";

	private static CharSequence ¤¤joinTitle = "¤Freedom!";
	private static CharSequence ¤¤joinBody = "¤This former vassal has has severed all diplomatic relationships with you";
	
	static {
		D.ts(Vassal.class);
	}
	
	boolean process(FactionNPC fa, EData data) {
		
		if (!DIP.VASSAL().is(fa)) {
			return false;
		}
		
		if (DIP.secondSinceStance(fa) < TIME.secondsPerDay())
			return false;
		
		double opinion = ROPINIONS.current(fa);
		
		if (opinion > 4) {
			if (data.vassal && opinion > 1) {
				data.vassal = false;
			}
			return false;
		}
		
		if (!data.vassal) {
			new Release(fa, 2, -1).send();
			data.vassal = true;
			return true;
		}

		new UIDipMess(¤¤joinTitle, fa.race().kingMessage().VASSAL_BREAK.get(fa), ¤¤joinBody, fa).send();
		DIP.NEUTRAL().set(fa);
		return true;
		
	}
	
	static class Release extends UIDipMessAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Release(FactionNPC f, double happiness, double decline) {
			super(¤¤breakTitle, f.king().induvidual.race().kingMessage().VASSAL_BREAK_REQ.get(f), ¤¤breakBody, f, f, happiness, decline);
		}

		@Override
		protected void accept(FactionNPC f, FactionNPC o) {
			DIP.PACT().set(f);
			ROPINIONS.OTHER().liberate(f);
		}

		@Override
		protected boolean valid(FactionNPC f, FactionNPC o) {
			return DIP.VASSAL().is(f);
		}
		
		
	}
	
}
