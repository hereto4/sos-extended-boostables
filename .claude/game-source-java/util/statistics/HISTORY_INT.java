package util.statistics;

import game.time.TIMECYCLE;
import snake2d.util.misc.CLAMP;
import util.data.GETTER;
import util.data.INT;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.info.INFO;

public interface HISTORY_INT extends INT, HISTORY{

	public abstract int get(int fromZero);
	
	@Override
	public default int get() {
		return get(0);
	}
	
	@Override
	default double getD() {
		return getD(0);
	}
	
	public default int getPeriod(int from, int to) {
		double am = 0;
		final int k = from-to;
		for (int i = 0; i < k; i++) {
			am += (i+1)*get(to+i);
		}
		double tot = k*(k+1)*0.5;
		am /= tot;
		return (int) Math.ceil(am);
	}
	
	public default int getPeriodSum(int from, int to) {
		int am = 0;
		from = CLAMP.i(from, -historyRecords(), 0);
		to = CLAMP.i(to, from, 0);
		from++;
		
		while(from <= to) {
			am += get(-from);
			
			from++;
		}
		return am;
	}
	
	
	public interface HISTORY_INTE extends HISTORY_INT, HISTORYE, INTE{
		
	}
	
	public interface HISTORY_INT_OBJECT<T> extends INT_O<T>, HISTORY_OBJECT<T>{

		@Override
		default int get(T t) {
			return get(t, 0);
		}
		@Override
		default double getD(T t) {
			return getD(t, 0);
		} 
		
		public abstract int get(T t, int fromZero);
		
		public default int getPeriod(T t, int from, int to) {
			double am = 0;
			final int k = from-to;
			for (int i = 0; i < k; i++) {
				am += (i+1)*get(t, to+i);
			}
			double tot = k*(k+1)*0.5;
			am /= tot;
			return (int) Math.ceil(am);
		}
		
	}
	
	public interface HISTORY_INT_OBJECTE<T> extends INT_OE<T>, HISTORY_OBJECTE<T>{

		public abstract int get(T t, int fromZero);
		
	}

	public static class HistoryIntObjectWrapper<T> implements HISTORY_INT{

		private final HISTORY_INT_OBJECT<T> t;
		private final GETTER<T> g;
		
		public HistoryIntObjectWrapper(HISTORY_INT_OBJECT<T> t, GETTER<T> g) {
			this.t = t;
			this.g = g;
		}
		
		@Override
		public int get() {
			return t.get(g.get());
		}

		@Override
		public int min() {
			return t.min(g.get());
		}

		@Override
		public int max() {
			return t.max(g.get());
		}

		@Override
		public TIMECYCLE time() {
			return t.time();
		}

		@Override
		public int historyRecords() {
			return t.historyRecords();
		}

		@Override
		public int get(int fromZero) {
			return (int) t.get(g.get(), fromZero);
		}
		
		@Override
		public INFO info() {
			return t.info();
		}

		@Override
		public double getD(int fromZero) {
			return t.getD(g.get(), fromZero);
		}
		
	}
	
	public static class HistoryIntWrapper<T> {

		private HISTORY_INT_OBJECT<T> t;
		private T g;
		
		public HISTORY_INT wrap(HISTORY_INT_OBJECT<T> t, T g) {
			this.t = t;
			this.g = g;
			return i;
		}
		
		private final HISTORY_INT i = new HISTORY_INT() {
			
			@Override
			public int get() {
				return t.get(g);
			}

			@Override
			public int min() {
				return t.min(g);
			}

			@Override
			public int max() {
				return t.max(g);
			}

			@Override
			public TIMECYCLE time() {
				return t.time();
			}

			@Override
			public int historyRecords() {
				return t.historyRecords();
			}

			@Override
			public int get(int fromZero) {
				return (int) t.get(g, fromZero);
			}
			
			@Override
			public INFO info() {
				return t.info();
			}

			@Override
			public double getD(int fromZero) {
				return t.getD(g, fromZero);
			}
			
			
		};
		
	}
}
