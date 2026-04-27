package util.data;

import java.io.IOException;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.CLAMP;

public interface INT extends DOUBLE{

	public int get();
	public int min();
	public int max();
	@Override
	public default double getD() {
		if (get() < 0) {
			if (min() == 0)
				return 0;
			return get()/(double)min();
		}else if (get() > 0) {
			if (max() == 0)
				return 0;
			return get()/(double)max();
		}else
			return 0;
	}

	public interface INTE extends INT, DOUBLE_MUTABLE{

		
		public void set(int t);
		public default void inc(int i) {
			set(CLAMP.i(get()+i, min(), max()));
		}
		
		@Override
		public default INTE incD(double d) {
			int i = (int)(max()*d);
			if (i == 0)
				if (d < 0)
					i = -1;
				else
					i = 1;
			inc(i);
			return this;
		}
		
		@Override
		default DOUBLE_MUTABLE setD(double d) {
			set((int) Math.ceil(d*max()));
			return this;
		}
		
	}
	
	public static class IntImp implements INTE, SAVABLE {

		public int i;
		public int min,max;
		
		public IntImp() {
			this(Integer.MIN_VALUE, Integer.MAX_VALUE);
		}
		
		public IntImp(int min, int max) {
			this.min = min;
			this.max = max;
			i = CLAMP.i(i, min, max);
		}
		
		public IntImp(int i, int min, int max) {
			this.min = min;
			this.max = max;
			i = CLAMP.i(i, min, max);
			this.i = i;
		}
		
		
		@Override
		public int get() {
			return i;
		}

		@Override
		public int min() {
			return min;
		}

		@Override
		public int max() {
			return max;
		}

		@Override
		public void set(int t) {
			i = CLAMP.i(t, min(), max());
		}

		@Override
		public void save(FilePutter file) {
			file.i(i);
			
		}

		@Override
		public void load(FileGetter file) throws IOException {
			i = file.i();
		}

		@Override
		public void clear() {
			i = 0;
		}
		
		
	}
	
}
