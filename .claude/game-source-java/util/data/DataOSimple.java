package util.data;

import util.data.BOOLEANO.BOOLEAN_OE;
import util.data.DOUBLE_O.DOUBLE_OE;
import util.data.INT_O.INT_OE;
import util.data.LONG_O.LONG_OE;
import util.info.INFO;

public abstract class DataOSimple<T> {


	public DataOSimple(){
		
	}
	
	protected abstract long[] data(T t);
	
	public int longCount() {
		return countLong+1;
	}
	
	private int countLong = -1;
	
	private final Count cInt = new Count(32, null);
	private final Count cShort = new Count(16, cInt);
	private final Count cByte = new Count(8, cShort);
	private final Count cNibble = new Count(4, cByte);
	private final Count cCrumb = new Count(2, cNibble);
	private final Count cBit = new Count(1, cCrumb);
	
	private class Count {
		
		private final int size;
		private int pScroll = 0;
		private int longI;
		private int count = 1;
		private final Count next;
		
		Count(int size, Count next){
			this.size = size;
			this.next = next;
		}
		
		Count count() {
			if (next == null) {
				count++;
				if (count > 1) {
					countLong++;
					count = 0;
					longI = countLong;
				}
				
				return this;
			}
			
			
			count ++;
			if (count > 1) {
				next.count();
				pScroll = next.scroll();
				count = 0;
				longI = next.longI;
			}
			
			return this;
		}
		
		int scroll() {
			return pScroll + count*size;
		}
		
	}
	
	
	private class DataAbs implements INT_OE<T>{
		
		private final int iLong;
		private final int scroll;
		private final long mask;
		private final INFO info;
		
		public DataAbs(INFO info, Count c) {
			c.count();
			this.scroll = c.scroll();
			this.mask = ((1l << (c.size))-1);
			iLong = c.longI;
			this.info = info;
			
			
			long cc = mask;
			cc = cc << scroll;
			
		}
		
		@Override
		public INFO info() {
			return info;
		}

		@Override
		public int get(T t) {
			return (int) ((data(t)[iLong] >>> scroll) & mask);
		}

		@Override
		public int min(T t) {
			return 0;
		}

		@Override
		public int max(T t) {
			return (int) mask;
		}
		
		@Override
		public void set(T t, int s) {
			if (s < min(t) || s > max(t))
				throw new RuntimeException(s + " " + min(t) + " " + max(t));
			long c = mask;
			s &= mask;
			data(t)[iLong] &= ~(mask<<scroll);
			c = s & 0x0FFFFFFFFl;
			c = c << scroll;
			data(t)[iLong] |= c;
			
		}
		
	}
	
	public class DataBit extends DataAbs implements BOOLEAN_OE<T>{

		public DataBit(INFO info) {
			super(info, cBit);
		}
		
		public DataBit() {
			this(null);
		}
		
		public DataBit(CharSequence name, CharSequence desc) {
			this(new INFO(name, desc));
		}

		@Override
		public boolean is(T t) {
			return get(t) == 1;
		}

		@Override
		public BOOLEAN_OE<T> set(T t, boolean b) {
			set(t, b ? 1 : 0);
			return this;
		}
		
	}
	
//	public class DataCrumb extends DataAbs implements INT_OE<T>{
//		
//		public DataCrumb(INFO info) {
//			super(info, cCrumb);
//		}
//		
//		public DataCrumb() {
//			this(null);
//		}
//		
//		public DataCrumb(CharSequence name, CharSequence desc) {
//			this(new INFO(name, desc));
//		}
//		
//	}
	
	public class DataNibble extends DataAbs implements INT_OE<T>{
		
		private final int max;
		
		public DataNibble(INFO info, int max) {
			super(info, cNibble);
			this.max = max;
		}
		
		public DataNibble() {
			this(null, 0x0F);
		}
		
		public DataNibble(int max) {
			this(null, max);
		}
		
		public DataNibble(CharSequence name, CharSequence desc) {
			this(new INFO(name, desc), 0x0F);
		}
		
		public DataNibble(CharSequence name, CharSequence desc, int max) {
			this(new INFO(name, desc), max);
		}
		
		@Override
		public int max(T t) {
			return max;
		}
		
	}
	
	public class DataByte extends DataAbs implements INT_OE<T>{
		
		private final int max;
		
		public DataByte(INFO info, int max){
			super(info, cByte);
			this.max = max;
		}
		
		public DataByte(INFO info){
			this(info, 255);
		}
		
		public DataByte(int max){
			this(null, max);
		}
		
		public DataByte() {
			this(null);
		}
		
		public DataByte(CharSequence name, CharSequence desc) {
			this(new INFO(name, desc));
		}
		
		@Override
		public int max(T t) {
			return max;
		}

		@Override
		public void set(T t, int s) {
			if (s < min(t) || s > max(t))
				throw new RuntimeException(""+s);
			super.set(t, s);
		}
		
	}
	
	public class DataShort extends DataAbs implements INT_OE<T>{

		private final int max;
		
		public DataShort(INFO info, int max) {
			super(info, cShort);
			this.max = max;
		}
		
		public DataShort(INFO info) {
			this(info, 0x0FFFF);
		}
		
		public DataShort() {
			this(null);
		}
		
		public DataShort(CharSequence name, CharSequence desc) {
			this(new INFO(name, desc));
		}
		
		public DataShort(CharSequence name, CharSequence desc, int max) {
			this(new INFO(name, desc), max);
		}

		@Override
		public int min(T t) {
			return 0;
		}

		@Override
		public int max(T t) {
			return max;
		}

		@Override
		public void set(T t, int s) {
			if (s < min(t) || s > max(t))
				throw new RuntimeException(""+s);
			super.set(t, s);
		}
		
	}
	
	public class DataInt extends DataAbs implements INT_OE<T>{
		
		private final int max;
		
		public DataInt(INFO info, int max) {
			super(info, cInt);
			this.max = max;
		}
		
		public DataInt() {
			this(null, Integer.MAX_VALUE);
		}
		
		public DataInt(INFO info) {
			this(info, Integer.MAX_VALUE);
		}
		
		public DataInt(CharSequence name, CharSequence desc) {
			this(new INFO(name, desc), Integer.MAX_VALUE);
		}
		@Override
		public int max(T t) {
			return max;
		}

		@Override
		public void set(T t, int s) {
			if (s < min(t) || s > max(t))
				throw new RuntimeException(""+s);
			super.set(t, s);
		}
		
	}
	
	public class DataFloat implements DOUBLE_OE<T>{
		
		private final DataInt dd = new DataInt();
		private INFO info;
		
		public DataFloat(INFO info) {
			this.info = info;
		}
		
		public DataFloat() {
			this(null);
		}

		@Override
		public double getD(T t) {
			return Float.intBitsToFloat(dd.get(t));
		}

		@Override
		public DOUBLE_OE<T> setD(T t, double d) {
			int i = Float.floatToIntBits((float) d);
			dd.set(t, i);
			return this;
		}
		
		@Override
		public INFO info() {
			return info;
		}
		
	}
	
	public class DataLong implements LONG_OE<T>{
		
		private final int longI;

		
		public DataLong() {
			countLong++;
			this.longI = countLong;
		}

		@Override
		public long get(T t) {
			return data(t)[longI];
		}

		@Override
		public void set(T t, long i) {
			data(t)[longI] = i;
		}
		
		
		
	}
	
	public class DataDouble implements DOUBLE_OE<T>{
		
		private final DataLong dd = new DataLong();
		private INFO info;
		
		public DataDouble(INFO info) {
			this.info = info;
		}
		
		public DataDouble() {
			this(null);
		}

		@Override
		public double getD(T t) {
			return Double.longBitsToDouble(dd.get(t));
		}

		@Override
		public DOUBLE_OE<T> setD(T t, double d) {
			long i = Double.doubleToLongBits(d);
			dd.set(t, i);
			return this;
		}
		
		@Override
		public INFO info() {
			return info;
		}
		
	}
	
	
}
