package settlement.stats.disease;

import game.boosting.BOOSTABLES;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.BoosterValue;
import game.faction.player.Player;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import snake2d.util.misc.CLAMP;
import util.text.D;
import util.text.Dic;

class BoostsHealth {

	private static CharSequence ¤¤entries = "New arrivals";
	
	static {
		D.ts(BoostsHealth.class);
	}
	
	public BoostsHealth() {
		{
			BSourceInfo s = new BSourceInfo(Dic.¤¤Population, UI.icons().s.human);
			BValue v = new BValue.BValueFaction(BOOSTABLES.PHYSICS().HEALTH) {
				
				@Override
				public double vGet(Player f) {
					double d = 1.0-5.0/(1+STATS.POP().POP.data().get(null));
					d = CLAMP.d(d, 0, 1);
					return d;
				}
			};
			new BoosterValue(v, s, 100, 0, true).add(BOOSTABLES.PHYSICS().HEALTH);
		}
		
		{
			BSourceInfo s = new BSourceInfo(¤¤entries, UI.icons().s.arrow_right);
			BValue v = new BValue.BValueFaction(BOOSTABLES.PHYSICS().HEALTH) {
				
				@Override
				public double vGet(Player f) {
					return CLAMP.d(STATS.POP().COUNT.newEntries(), 0, 1);
				}
			};
			new BoosterValue(v, s, 1, 0.5, true).add(BOOSTABLES.PHYSICS().HEALTH);
		}
		
	}
	
}
