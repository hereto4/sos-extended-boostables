package util.statistics;

import java.io.IOException;
import java.util.Arrays;

import game.time.TIMECYCLE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import util.info.INFO;

public class HistoryInt implements HISTORY_INT.HISTORY_INTE, SAVABLE {

	private final int[] history;
	private int bitSinceStart = -1;
	private final TIMECYCLE c;
	private final boolean keep;
	private INFO info;
	private final int max;
	
	public HistoryInt(int size, TIMECYCLE c, boolean keep) {
		this(null, null, size, c, keep, Integer.MAX_VALUE);
	}
	
	public HistoryInt(CharSequence name, CharSequence desc, int size, TIMECYCLE c, boolean keep) {
		this(name, desc, size, c, keep, Integer.MAX_VALUE);
	}
	
	public HistoryInt(CharSequence name, CharSequence desc, int size, TIMECYCLE c, boolean keep, int max) {
		history = new int[size];
		bitSinceStart = c.bitsSinceStart();
		this.c = c;
		this.keep = keep;
		if (name != null)
			info = new INFO(name, desc);
		this.max = max;
	}

	@Override
	public int get(int fromZero) {
		update();
		int i = history.length-1-fromZero;
		i = CLAMP.i(i, 0, history.length-1);
		return history[i];
	}
	
	private void update() {
		if (bitSinceStart == c.bitsSinceStart())
			return;
		
		int d = Math.abs(c.bitsSinceStart()-bitSinceStart);
		
		int dd = 0;
		if (keep) {
			dd = history[history.length-1];
		}
		
		if (d >= history.length) {
			for (int i = 0; i < history.length; i++)
				history[i] = dd;
		}else {
			for (int i = 0; i+d < history.length; i++) {
				history[i] = history[i+d];
			}
			for (int i = history.length-d; i < history.length; i++)
				history[i] = dd;
		}
		bitSinceStart = c.bitsSinceStart();
	}

	@Override
	public final TIMECYCLE time() {
		return c;
	}
	
	@Override
	public void save(FilePutter file) {
		update();
		file.is(history);
		file.i(bitSinceStart);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		file.is(history);
		bitSinceStart = file.i();
	}

	@Override
	public void clear() {
		for (int i = 0; i < history.length; i++) {
			history[i] = 0;
		}
		bitSinceStart = c.bitsSinceStart();
	}

	public void randomize() {
		for (int i = 0; i < history.length; i++) {
			history[i] = RND.rInt(50000);
		}
	}

	public void add(HistoryInt other) {
		for (int i = 0; i < other.history.length && i < history.length; i++) {
			history[i] += other.history[i];
		}
	}
	
	@Override
	public final void set(int amount) {
		update();
		int old = history[history.length-1];
		history[history.length-1] = CLAMP.i(amount, min(), max());
		change(old, history[history.length-1]);
	}
	
	public void fill(int amount) {
		Arrays.fill(history, amount);
	}
	
	protected void change(int old, int current) {
		
	}

	@Override
	public int get() {
		return get(0);
	}

	@Override
	public int min() {
		return Integer.MIN_VALUE;
	}

	@Override
	public int max() {
		return max;
	}

	@Override
	public int historyRecords() {
		return history.length;
	}

	@Override
	public double getD(int fromZero) {
		return get(fromZero)/(double)(max());
	}
	
	@Override
	public INFO info() {
		return info;
	}

}