package settlement.stats.stat;

import java.io.IOException;
import java.util.Arrays;

import game.time.TIME;
import game.time.TIMECYCLE;
import init.race.RACES;
import init.race.Race;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import util.statistics.HISTORY_INT.HISTORY_INT_OBJECT;

public abstract class DataStat implements HISTORY_INT_OBJECT<Race>, SAVABLE{

	private int bitSinceStart = -1;
	private final int[][] data  = new int[STATS.DAYS_SAVED+1][RACES.all().size()];
	private final int[] total  = new int[STATS.DAYS_SAVED+1];
	
	public DataStat(String key, StatsInit init){
		init.savers.put(key, this);
		
	}
	
	DataStat(){

	}
	
	void set(Race r, int a) {
		init();
		if (r == null)
			total[0] = a;
		else
			data[0][r.index] = a;
	}
	
	public void incrFull(Induvidual i, int d) {
		init();
		data[0][i.race().index] += d;
		total[0] += d;
	}
	
	private void pushday() {
		for (int i = STATS.DAYS_SAVED-1; i > 0; i--) {
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				data[i][ri] = data[i-1][ri];
			}
			total[i] = total[i-1];
		}
	}
	
	private void init() {
		if (bitSinceStart == TIME.days().bitsSinceStart())
			return;
		
		int am = Math.abs(bitSinceStart-TIME.days().bitsSinceStart());
		if (am > 0) {
			for (int i = 0; i < am; i++)
				pushday();
		}
		bitSinceStart = TIME.days().bitsSinceStart();
	}
	
	@Override
	public void save(FilePutter file) {
		file.i(bitSinceStart);
		file.isE(data);
		file.isE(total);
	}
	
	@Override
	public void load(FileGetter file) throws IOException {
		bitSinceStart = file.i();
		file.isE(data);
		file.isE(total);
		total[0] = 0;
		Arrays.fill(data[0], 0);
		clear();
	}
	
	@Override
	public void clear() {
		for (int i = 0; i < data[0].length; i++)
			data[0][i] = 0;
		total[0] = 0;
	}

	@Override
	public int get(Race group, int daysBack) {
		init();
		if (group == null)
			return total[daysBack];
		return data[daysBack][group.index];
	}

	@Override
	public TIMECYCLE time() {
		return TIME.days();
	}

	@Override
	public int historyRecords() {
		return STATS.DAYS_SAVED;
	}
	
}
