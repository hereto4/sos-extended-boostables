package init.type;

import game.boosting.BoostableCat;
import init.paths.PATHS.ResFolder;
import settlement.stats.STATS;
import settlement.stats.colls.StatsNeeds.StatNeedNormal;
import snake2d.util.sets.LISTE;

public class NEED_E extends NEED{

	private final int indexE;
	
	NEED_E(String key, ResFolder f, LISTE<NEED> all, LISTE<NEED_E> alle, BoostableCat cat) {
		super(key, f, all, cat, null, true);
		indexE = alle.add(this);
	}

	public StatNeedNormal stat() {
		return STATS.NEEDS().SNEEDS.get(indexE);
	}

}
