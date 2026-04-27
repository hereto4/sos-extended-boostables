package settlement.stats.law;

import java.io.IOException;

import game.time.TIME;
import init.race.Race;
import settlement.stats.STATS;
import settlement.stats.law.PRISONER_TYPE.CRIME;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import util.statistics.HISTORY_COLLECTION;
import util.statistics.HISTORY_INT;
import util.statistics.HistoryInt;
import util.statistics.HistoryRace;

public final class Crimes {

	private final HistoryInt total = new HistoryInt(STATS.DAYS_SAVED, TIME.days(), false);
	private final HistoryInt[] crimes = new HistoryInt[PRISONER_TYPE.CRIMES.size()];
	private final HistoryRace perRace = new HistoryRace(STATS.DAYS_SAVED, TIME.days(), false);
	
	private double upD = 0;
	
	Crimes() {
		for (int i = 0; i < crimes.length; i++)
			crimes[i] = new HistoryInt(STATS.DAYS_SAVED, TIME.days(), false);
	}
	
	public HISTORY_INT crimes(CRIME c) {
		if (c == null)
			return total;
		return crimes[c.crimeI];
	}
	
	public HISTORY_COLLECTION<Race> perRace() {
		return perRace;
	}
	
	public void register(Race race, CRIME c) {
		total.inc(1);
		crimes[c.crimeI].inc(1);
		perRace.inc(race, 1);
	}
	
	void update(double ds) {
		upD -= ds;
		if (upD < 0) {
			upD += 5;
		}
	}


	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			total.save(file);
			for (HistoryInt i : crimes)
				i.save(file);
			perRace.save(file);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			total.load(file);
			for (HistoryInt i : crimes)
				i.load(file);
			perRace.load(file);
		}
		
		@Override
		public void clear() {
			total.clear();
			for (HistoryInt i : crimes)
				i.clear();
			perRace.clear();
		}
	};
	
	
}
