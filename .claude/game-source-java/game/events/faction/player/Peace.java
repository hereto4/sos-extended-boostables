package game.events.faction.player;

import game.faction.diplomacy.DIP;
import game.faction.diplomacy.deal.Deal;
import game.faction.diplomacy.deal.DealDrawfter;
import game.faction.npc.FactionNPC;
import init.race.KingMessages;
import settlement.main.SETT;
import snake2d.util.rnd.RND;
import util.text.Dic;
import view.ui.diplomacy.UIDipMessDeal;
import world.army.AD;

class Peace {
	
	boolean update() {
		if (SETT.INVADOR().invading())
			return false;
		
		
		if (!RND.oneIn(6))
			return true;
		
		FactionNPC f = null;
		
		for (FactionNPC ff : DIP.WAR().player()) {
			if (f == null || AD.power().get(ff) > AD.power().get(f))
				f = ff;
		}
		
		if (f != null && !f.request.has()) {
			KingMessages m = f.court().king().roy().induvidual.race().kingMessage();
			Deal d = DIP.TMP();
			d.setFactionAndClear(f);
			d.bools.PEACE.set(true);
			CharSequence desc = m.PEACE.get(f);
			double credits = d.valueCredits();
			if (credits > 0) {
				desc = m.PEACE_GOOD.get(f);
				
			}
			else if (credits < 0) {
				desc = m.PEACE_BAD.get(f);
			}
			credits += (1 + RND.rFloat()*0.5)*Math.abs(credits);
			DealDrawfter.draft(d, credits, true, true);
			new UIDipMessDeal(Dic.¤¤peace, desc, d, 0.5, -0.5).send();
			return true;
		}
		return false;
	}
	
}
