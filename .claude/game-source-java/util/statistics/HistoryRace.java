package util.statistics;

import java.io.IOException;

import game.time.TIMECYCLE;
import init.race.RACES;
import init.race.Race;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import util.data.INT_O.INT_OE;
import util.info.INFO;

public class HistoryRace implements HISTORY_COLLECTION<Race>, INT_OE<Race>, SAVABLE {

	private final HistoryInt total;
	private final HistoryInt[] histories = new HistoryInt[RACES.all().size()];
	private final INFO info;

	public HistoryRace(int size, TIMECYCLE time, boolean keep) {
		total = new HistoryInt(size, time, keep);
		for (int i = 0; i < RACES.all().size(); i++) {
			Race r = RACES.all().get(i);
			histories[i] = new HistoryInt(size, time, keep) {
				@Override
				protected void change(int old, int current) {
					total.inc(-old);
					total.inc(current);
					HistoryRace.this.change(r, old, current);
				}
				@Override
				public int max() {
					return HistoryRace.this.max(r);
				}
				@Override
				public int min() {
					return HistoryRace.this.min(r);
				}
				
				
			};
		}
		info = null;
	}
	
	public HistoryRace(int size, TIMECYCLE time, boolean keep, CharSequence name, CharSequence desc) {
		total = new HistoryInt(size, time, keep);
		for (int i = 0; i < RACES.all().size(); i++) {
			Race r = RACES.all().get(i);
			histories[i] = new HistoryInt(size, time, keep) {
				@Override
				protected void change(int old, int current) {
					total.inc(-old);
					total.inc(current);
					HistoryRace.this.change(r, old, current);
				}
			};
		}
		info = new INFO(name, desc);
	}

	@Override
	public HISTORY_INT.HISTORY_INTE history(Race r) {
		if (r == null)
			return total;
		return histories[r.index];
	}

	@Override
	public HISTORY_INT.HISTORY_INTE total() {
		return total;
	}

	@Override
	public void save(FilePutter file) {
		RACES.map().saver().save(histories, file);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		RACES.map().loader().load(histories, file);
		total.clear();
		for (Race r : RACES.all())
			total.add(histories[r.index]);
	}

	@Override
	public void clear() {
		for (Race r : RACES.all())
			histories[r.index].clear();
		total.clear();
	}

	@Override
	public int get(Race t) {
		if (t == null)
			return total.get();
		return history(t).get();
	}
	
	protected void change(Race r, int old, int current) {

	}
	
	@Override
	public INFO info() {
		return info;
	}

	@Override
	public int min(Race t) {
		return 0;
	}

	@Override
	public int max(Race t) {
		return Integer.MAX_VALUE;
	}

	@Override
	public void set(Race t, int i) {
		history(t).set(i);;
	}
	
}