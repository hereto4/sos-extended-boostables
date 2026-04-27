package util.statistics;

import java.io.IOException;

import game.time.TIMECYCLE;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import util.data.INT_O.INT_OE;
import util.info.INFO;

public class HistoryResource implements HISTORY_COLLECTION<RESOURCE>, INT_OE<RESOURCE>, SAVABLE {

	private final HistoryInt total;
	private final HistoryInt[] histories = new HistoryInt[RESOURCES.ALL().size()];
	private final INFO info;

	public HistoryResource(int size, TIMECYCLE time, boolean keep) {
		this(null, size, time, keep);
	}
	
	public HistoryResource(INFO info, int size, TIMECYCLE time, boolean keep) {
		total = new HistoryInt(size, time, keep);
		for (int i = 0; i < RESOURCES.ALL().size(); i++) {
			histories[i] = new H(RESOURCES.ALL().get(i), size, time, keep);
		}
		this.info = info;
	}

	@Override
	public INFO info() {
		return info;
	}
	
	@Override
	public HISTORY_INT total() {
		return total;
	}

	@Override
	public void save(FilePutter file) {
		RESOURCES.map().saver().save(histories, file);
		total.save(file);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		RESOURCES.map().loader().load(histories, file);
		total.load(file);
		total.set(0);
		for (HistoryInt ii : histories)
			total.inc(ii.get());
	}

	@Override
	public void clear() {
		for (RESOURCE r : RESOURCES.ALL())
			histories[r.bIndex()].clear();
		total.clear();
	}

	public HISTORY_INT.HISTORY_INTE get(int rI) {
		return histories[rI];
	}
	
	@Override
	public HISTORY_INT.HISTORY_INTE history(RESOURCE r) {
		if (r == null)
			return total;
		return get(r.bIndex());
	}
	
	protected void change(RESOURCE r, int old, int current) {
		
	}
	
	private class H extends HistoryInt {

		final RESOURCE r;
		
		public H(RESOURCE r, int size, TIMECYCLE c, boolean keep) {
			super(size, c, keep);
			this.r = r;
		}
		@Override
		protected void change(int old, int current) {
			total.inc(-old);
			total.inc(current);
			HistoryResource.this.change(r, old, current);
		}
	}

	@Override
	public int get(RESOURCE t) {
		return history(t).get();
	}

	@Override
	public void set(RESOURCE t, int i) {
		history(t).set(i);
	}

	@Override
	public int min(RESOURCE t) {
		return Integer.MIN_VALUE;
	}

	@Override
	public int max(RESOURCE t) {
		return Integer.MAX_VALUE;
	}

}