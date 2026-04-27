package util.statistics;

import game.time.TIMECYCLE;
import util.data.DOUBLE;
import util.data.DOUBLE_O;
import util.data.DOUBLE_O.DOUBLE_OE;
import util.info.INFO;

public interface HISTORY extends DOUBLE{

	public abstract TIMECYCLE time();
	public abstract int historyRecords();
	public abstract double getD(int fromZero);
	

	
	@Override
	default double getD() {
		return getD(0);
	}
	
	public interface HISTORYE extends HISTORY, DOUBLE_MUTABLE{


	}
	
	public interface HISTORY_OBJECT<T> extends DOUBLE_O<T>{
		
		public abstract double getD(T t, int fromZero);
		public abstract TIMECYCLE time();
		public abstract int historyRecords();
		
		public default double getPeriodD(T t, int from, int to) {
			double am = 0;
			final int k = from-to;
			for (int i = 0; i < k; i++) {
				am += (i+1)*getD(t, to+i);
			}
			double tot = k*(k+1)*0.5;
			am /= tot;
			return am;
		}
		

		
	}
	
	public interface HISTORY_OBJECTE<T> extends HISTORY_OBJECT<T>, DOUBLE_OE<T>{

		
		
	}
	
	public abstract class HISTORYImp implements HISTORY {

		private final TIMECYCLE time;
		private final int rec;
		private final INFO info;
		
		public HISTORYImp(TIMECYCLE time, int rec) {
			this(null, null, time, rec);
		}
		
		public HISTORYImp(CharSequence name, CharSequence desc, TIMECYCLE time, int rec) {
			if (name == null)
				info = null;
			else
				info = new INFO(name, desc);
			this.rec = rec;
			this.time = time;
		}
		
		@Override
		public TIMECYCLE time() {
			return time;
		}

		@Override
		public int historyRecords() {
			return rec;
		}
		
		@Override
		public INFO info() {
			return info;
		}
		
	}
	
}
