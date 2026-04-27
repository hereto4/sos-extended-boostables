package util.data;

import snake2d.util.bit.Bits;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;

public interface INT_O<T> extends DOUBLE_O<T> {

	public int get(T t);
	public int min(T t);
	public int max(T t);
	@Override
	public default double getD(T t) {
		if (max(t) == 0)
			return 0;
		return get(t)/(double)max(t);
	}
	
	public default boolean isMax(T t) {
		return get(t) == max(t);
	}

	public interface INT_OE<T> extends INT_O<T>, DOUBLE_OE<T>{

		
		public void set(T t, int i);
		@Override
		public default DOUBLE_OE<T> setD(T t, double d) {
			set(t, (int)(max(t)*d));
			return this;
		}
		@Override
		public default DOUBLE_OE<T> incD(T t, double d) {
			int i = (int)(max(t)*d);
			if (i == 0)
				if (d < 0)
					i = -1;
				else
					i = 1;
			inc(t, i);
			return this;
		}
		

		
		public default void inc(T t, int i) {
			set(t, CLAMP.i(get(t)+i, min(t), max(t)));
		}
		
		public default void incFraction(T t, double d) {
			int am = (int) d;
			if (am != d) {
				if (d < 0 && -d-am > RND.rFloat())
					am--;
				else if (d > 0 && d-am > RND.rFloat())
					am++;
			}
			set(t, CLAMP.i(get(t)+am, min(t), max(t)));
		}
		
		public default DOUBLE_OE<T> moveTo(T t, double d, int target) {
			int am = (int) d;
			if (am != d) {
				if (d < 0 && -(d+am) > RND.rFloat())
					am--;
				else if (d > 0 && d-am > RND.rFloat())
					am++;
			}
			int c = get(t);
			if (c < target) {
				c += am;
				if (c > target)
					c = target;
			}else {
				c -= am;
				if (c < target)
					c = target;
			}

			set(t, CLAMP.i(c, min(t), max(t)));
			return this;
		}
		
		
		public default void andSet(T t, int i) {
			set(t, get(t) & i);
		}
		
		public default void orSet(T t, int i) {
			set(t, get(t) | i);
		}
		
		public default INT.INTE createInt(T t) {
			return new INT.INTE() {
				
				@Override
				public int min() {
					return INT_OE.this.min(t);
				}
				
				@Override
				public int max() {
					return INT_OE.this.max(t);
				}
				
				@Override
				public int get() {
					return INT_OE.this.get(t);
				}
				
				@Override
				public void set(int k) {
					INT_OE.this.set(t, k);
				}
			};
		}
		
		public default INT.INTE createIntInverted(T t) {
			return new INT.INTE() {
				
				@Override
				public int min() {
					return INT_OE.this.min(t);
				}
				
				@Override
				public int max() {
					return INT_OE.this.max(t);
				}
				
				@Override
				public int get() {
					return max()-INT_OE.this.get(t);
				}
				
				@Override
				public void set(int k) {
					INT_OE.this.set(t, max()-k);
				}
			};
		}
	}
	
	public static class INTWRAP<T> implements INT_OE<T> {

		private final Bits bits;
		private final INT_OE<T> data;
		
		public INTWRAP(int mask, INT_OE<T> data){
			this.bits = new Bits(mask);
			this.data = data;
		}

		@Override
		public int get(T t) {
			return bits.get(data.get(t));
		}

		@Override
		public int min(T t) {
			return 0;
		}

		@Override
		public int max(T t) {
			return bits.mask;
		}

		@Override
		public void set(T t, int i) {
			int d = data.get(t);
			d = bits.set(d, i);
			data.set(t, d);
		}
		
		
	}
	
	
	
}
