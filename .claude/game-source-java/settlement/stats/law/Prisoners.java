package settlement.stats.law;

import java.util.Arrays;

import game.GAME;
import init.race.RACES;
import init.race.Race;
import init.type.HTYPES;
import settlement.entity.EntityIterator;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.STATS;

public final class Prisoners {

	private int total;
	private final int[] types = new int[PRISONER_TYPE.ALL.size()];
	private final int[][] raceTypes = new int[RACES.all().size()][PRISONER_TYPE.ALL.size()];
	private int updateI = -1000;

	private final EntityIterator.Humans iter = new EntityIterator.Humans() {
		
		@Override
		protected boolean processAndShouldBreakH(Humanoid h, int ie) {
			if (h.indu().hType() == HTYPES.PRISONER()) {
				total ++;
				types[STATS.LAW().prisonerType.get(h.indu()).index()] ++;
				raceTypes[h.race().index()][STATS.LAW().prisonerType.get(h.indu()).index()] ++;
			}
			return false;
		}
	};
	
	public int amount() {
		return STATS.POP().pop(null, HTYPES.PRISONER());
	}
	
	public int amount(Race race) {
		return STATS.POP().pop(race, HTYPES.PRISONER());
	}
	
	public int amount(PRISONER_TYPE t) {
		update();
		if (t == null)
			return total;
		return types[t.index()];
	}
	
	public int amount(Race race, PRISONER_TYPE t) {
		update();
		if (race == null) {
			if (t == null)
				return total;
			return types[t.index()];
		}
		
		if (t == null)
			return amount(race);
		
		return raceTypes[race.index][t.index()];
	}
	
	private void update() {
		if (Math.abs(updateI-GAME.updateI()) < 120)
			return;
		updateI = GAME.updateI();
		total = 0;
		Arrays.fill(types, 0);
		for (int[] ii : raceTypes)
		Arrays.fill(ii, 0);
		iter.iterate();
	}

	
	
	
}
