package game.faction.player;

import java.io.IOException;
import java.util.Arrays;

import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.faction.FSlaves;
import game.faction.Faction;
import init.race.RACES;
import init.race.Race;
import init.type.HTYPES;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public class PSlaves extends FSlaves{

	private int[] sold = new int[RACES.all().size()];
	private int[] leaving = new int[RACES.all().size()];
	
	@Override
	public int available(Race race) {
		return STATS.POP().pop(race, HTYPES.SLAVE())-sold[race.index];
	}

	@Override
	public void trade(Race race, int am, int credits) {
		if (am > 0) {
		}else {
			sold[race.index] -= am;
		}
		FACTIONS.player().credits().inc(credits, CTYPE.SLAVES);
	}

	@Override
	public int price(Race race, int am) {
		return BASE_PRICE(race);
	}

	@Override
	protected void save(FilePutter file) {
		RACES.map().saver().save(sold, file);
		RACES.map().saver().save(leaving, file);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		RACES.map().loader().load(sold, file, 0);
		RACES.map().loader().load(leaving, file, 0);
	}

	@Override
	protected void clear() {
		Arrays.fill(sold, 0);
		Arrays.fill(leaving, 0);
	}

	@Override
	protected void update(double ds, Faction f) {

	}
	
	public boolean shouldLeave(Humanoid h) {
		return sold[h.race().index()] - leaving[h.race().index()] > 0;
	}
	
	public boolean reserveLeave(Humanoid h) {
		if (shouldLeave(h)) {
			leaving[h.race().index()]++;
			return true;
		}
		return false;
	}
	
	public void reserveLeaveCancel(Humanoid h) {
		if (leaving[h.race().index()] > 0)
			leaving[h.race().index()]--;
	}
	
	public void leave(Humanoid h) {
		reserveLeaveCancel(h);
		if (sold[h.race().index()] > 0) {
			sold[h.race().index()]--;
		}
	}

}
