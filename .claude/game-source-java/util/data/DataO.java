package util.data;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.save.Savable;
import snake2d.LOG;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import util.data.BOOLEANO.BOOLEAN_OE;
import util.data.DOUBLE_O.DOUBLE_OE;
import util.data.INT_O.INT_OE;
import util.data.LONG_O.LONG_OE;
import util.info.INFO;

public abstract class DataO<T> {

	public DataO(String key) {
		GAME.saver().addSpecialSaver(new Savable(key) {

			@Override
			public void save(FilePutter file) {
				saver = new DataOSaver(file);

			}

			@Override
			public void load(FileGetter file) throws IOException {
				loader = new DataOLoader(file);
			}

		});
	};
	
	private DataO(boolean hack) {
		
	};

	protected abstract long[] data(T t);

	public int longCount() {
		return countLong + 1;
	}

	public INT_OE<T> create(String key, int max) {
		if (max <= 0b01) {
			return new DataBit(key);
		} else if (max <= 0b0000_0000_0000_0000_0000_0000_0000_0011) {
			return new DataCrumb(key);
		} else if (max <= 0b0000_0000_0000_0000_0000_0000_0000_1111) {
			return new DataNibble(key, max);
		} else if (max <= 0b0000_0000_0000_0000_0000_0000_0001_1111) {
			return new DataNibble1(key, max);
		} else if (max <= 0b0000_0000_0000_0000_0000_0000_1111_1111) {
			return new DataByte(key, max);
		} else if (max <= 0b0000_0000_0000_0000_1111_1111_1111_1111) {
			return new DataShort(key, null, max);
		} else if (max <= 0b0000_0000_1111_1111_1111_1111_1111_1111) {
			return new DataShortE(key, max);
		} else if (max <= 0x0FFFFFFFF) {
			return new DataInt(key, null, max);
		}
		throw new RuntimeException("" + max);
	}

	private int countLong = -1;

	private final Entries entries = new Entries();
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

		Count(int size, Count next) {
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

			count++;
			if (count > 1) {
				next.count();
				pScroll = next.scroll();
				count = 0;
				longI = next.longI;
			}

			return this;
		}

		int scroll() {
			return pScroll + count * size;
		}

	}

	public class DataAbs implements INT_OE<T> {

		private final int iLong;
		private final int scroll;
		private final long mask;
		private final INFO info;
		public final String key;

		private DataAbs(String key, INFO info, Count c) {
			c.count();
			this.key = key;
			this.scroll = c.scroll();
			this.mask = ((1l << (c.size)) - 1);
			iLong = c.longI;
			this.info = info;

			long cc = mask;
			cc = cc << scroll;
			entries.push(key, c.size, new LONG_OE<T>() {

				@Override
				public long get(T t) {
					return DataAbs.this.get(t);
				}

				@Override
				public void set(T t, long i) {
					DataAbs.this.set(t, (int) i);
				}

			});

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
			data(t)[iLong] &= ~(mask << scroll);
			c = s & 0x0FFFFFFFFl;
			c = c << scroll;
			data(t)[iLong] |= c;

		}

	}

	public class DataBit extends DataAbs implements BOOLEAN_OE<T> {

		public DataBit(String key, INFO info) {
			super(key, info, cBit);
		}

		public DataBit(String key) {
			this(key, null);
		}

		public DataBit(String key, CharSequence name, CharSequence desc) {
			this(key, new INFO(name, desc));
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

	public class DataCrumb extends DataAbs implements INT_OE<T> {

		public DataCrumb(String key, INFO info) {
			super(key, info, cCrumb);
		}

		public DataCrumb(String key) {
			this(key, null);
		}

		public DataCrumb(String key, CharSequence name, CharSequence desc) {
			this(key, new INFO(name, desc));
		}

	}

	public class DataNibble extends DataAbs implements INT_OE<T> {

		private final int max;

		public DataNibble(String key, INFO info, int max) {
			super(key, info, cNibble);
			this.max = max;
		}

		public DataNibble(String key) {
			this(key, null, 0x0F);
		}

		public DataNibble(String key, int max) {
			this(key, null, max);
		}

		public DataNibble(String key, CharSequence name, CharSequence desc) {
			this(key, new INFO(name, desc), 0x0F);
		}

		public DataNibble(String key, CharSequence name, CharSequence desc, int max) {
			this(key, new INFO(name, desc), max);
		}

		@Override
		public int max(T t) {
			return max;
		}

	}

	public class DataNibble1 implements INT_OE<T> {

		private final DataBit bit;
		private final DataNibble nibble;
		private final int max;

		public DataNibble1(String key) {
			this(key, 0b011111);
		}

		public DataNibble1(String key, int max) {
			bit = new DataBit(key);
			nibble = new DataNibble(key);
			this.max = max;
		}

		@Override
		public int get(T t) {
			return (bit.get(t) << 4) + nibble.get(t);
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
		public void set(T t, int v) {
			int b = (v >> 4) & 1;
			bit.set(t, b);
			nibble.set(t, (v & 0xF));
		}

	}

	public class DataByte extends DataAbs implements INT_OE<T> {

		private final int max;

		public DataByte(String key, INFO info, int max) {
			super(key, info, cByte);
			this.max = max;
		}

		public DataByte(String key, INFO info) {
			this(key, info, 255);
		}

		public DataByte(String key, int max) {
			this(key, null, max);
		}

		public DataByte(String key) {
			this(key, null);
		}

		public DataByte(String key, CharSequence name, CharSequence desc) {
			this(key, new INFO(name, desc));
		}

		@Override
		public int max(T t) {
			return max;
		}

		@Override
		public void set(T t, int s) {
			if (s < min(t) || s > max(t))
				throw new RuntimeException("" + s);
			super.set(t, s);
		}

	}

	public class DataShort extends DataAbs implements INT_OE<T> {

		private final int max;

		public DataShort(String key, INFO info, int max) {
			super(key, info, cShort);
			this.max = max;
		}

		public DataShort(String key, INFO info) {
			this(key, info, 0x0FFFF);
		}

		public DataShort(String key) {
			this(key, null);
		}

		public DataShort(String key, CharSequence name, CharSequence desc) {
			this(key, new INFO(name, desc));
		}

		public DataShort(String key, CharSequence name, CharSequence desc, int max) {
			this(key, new INFO(name, desc), max);
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
				throw new RuntimeException("" + s);
			super.set(t, s);
		}

	}

	public class DataShortE implements INT_OE<T> {

		private final DataByte by;
		private final DataShort sh;
		private final int max;

		public DataShortE(String key) {
			this(key, 0x0FFFFFF);
		}

		public DataShortE(String key, int max) {
			by = new DataByte(key);
			sh = new DataShort(key);
			this.max = max;
		}

		@Override
		public int get(T t) {
			return (by.get(t) << 16) + sh.get(t);
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
		public void set(T t, int v) {
			int b = (v >> 16) & 0x0FF;
			by.set(t, b);
			sh.set(t, (v & 0xFFFF));
		}

	}

	public class DataInt extends DataAbs implements INT_OE<T> {

		private final int max;

		public DataInt(String key, INFO info, int max) {
			super(key, info, cInt);
			this.max = max;
		}

		public DataInt(String key) {
			this(key, null, Integer.MAX_VALUE);
		}

		public DataInt(String key, INFO info) {
			this(key, info, Integer.MAX_VALUE);
		}

		public DataInt(String key, CharSequence name, CharSequence desc) {
			this(key, new INFO(name, desc), Integer.MAX_VALUE);
		}

		@Override
		public int max(T t) {
			return max;
		}

		@Override
		public void set(T t, int s) {
			if (s < min(t) || s > max(t))
				throw new RuntimeException("" + s);
			super.set(t, s);
		}

	}

	public class DataFloat implements DOUBLE_OE<T> {

		private final DataInt dd;
		private INFO info;

		public DataFloat(String key, INFO info) {
			this.info = info;
			dd = new DataInt(key) {
				@Override
				public int min(T t) {
					return Integer.MIN_VALUE;
				};
			};
		}

		public DataFloat(String key) {
			this(key, null);
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

	public class DataLong implements LONG_OE<T> {

		private final int longI;

		public DataLong(String key) {
			countLong++;
			this.longI = countLong;
			entries.push(key, 64, this);
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

	public class DataDouble implements DOUBLE_OE<T> {

		private final DataLong dd;
		private INFO info;

		public DataDouble(String key, INFO info) {
			this.info = info;
			dd = new DataLong(key);
		}

		public DataDouble(String key) {
			this(key, null);
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

	private DataOSaver saver;
	private DataOLoader loader;

	public DataOSaver saver() {
		return saver;
	}

	public DataOLoader loader() throws IOException {
		return loader;
	}

	public final class DataOSaver {

		private DataOSaver(FilePutter f) {
			f.i(longCount());
			f.i(entries.entries.size());

			for (Entries.Entry e : entries.entries) {
				f.chars(e.key);
				f.i(e.type);
			}
		}

		public void save(T t, FilePutter f) {
			f.ls(data(t));
		}

		public void save(T[] tt, FilePutter f) {
			f.i(tt.length);
			for (T t : tt)
				save(t, f);
		}

		public void save(LIST<T> tt, FilePutter f) {
			f.i(tt.size());
			for (T t : tt)
				save(t, f);
		}

	}

	public final class DataOLoader {

		DataO<T> old = null;
		private long[] tmp;
		private final int longCount;

		private DataOLoader(FileGetter f) throws IOException {
			boolean isSame = true;
			longCount = f.i();
			int kk = f.i();
			isSame = isSame & longCount == longCount();

			old = new DataO<T>(false) {

				@Override
				protected long[] data(T t) {
					return tmp;
				}

			};

			for (int i = 0; i < kk; i++) {
				String k = f.chars();
				int t = f.i();
				switch (t) {
				case 1:
					old.new DataBit(k);
					break;
				case 2:
					old.new DataCrumb(k);
					break;
				case 4:
					old.new DataNibble(k);
					break;
				case 8:
					old.new DataByte(k);
					break;
				case 16:
					old.new DataShort(k);
					break;
				case 32:
					old.new DataInt(k);
					break;
				case 64:
					old.new DataLong(k);
					break;
				}
			}

			isSame = old.entries.entries.size() == entries.entries.size();

			for (int i = 0; i < entries.entries.size() && isSame; i++) {

				if (!old.entries.entries.get(i).mkey.equals(entries.entries.get(i).mkey)) {
					LOG.ln(old.entries.entries.get(i).mkey + " " + entries.entries.get(i).mkey);
					isSame = false;
				}
			}

			if (isSame)
				old = null;
			else
				LOG.ln("" + DataO.this);

		}

		public void wash(T t, long[] oldData) {
			if (old == null) {
				for (int i = 0; i < oldData.length; i++)
				data(t)[i] = oldData[i];
				return;
			}

			tmp = oldData;
			Arrays.fill(data(t), 0);
			for (Entries.Entry e : old.entries.entries) {
				Entries.Entry o = entries.map.get(e.mkey);
				if (o != null && e.type == o.type) {
					o.ii.set(t, e.ii.get(t));
				}

			}
		}
		
		public void load(T t, FileGetter f) {

			if (old == null) {
				f.ls(data(t));
				return;
			}

			
			
			tmp = new long[longCount];
			f.ls(tmp);
			
			
			Arrays.fill(data(t), 0);

			for (Entries.Entry e : old.entries.entries) {
				Entries.Entry o = entries.map.get(e.mkey);
				if (o != null && e.type == o.type) {
					o.ii.set(t, e.ii.get(t));
				}

			}
		}

		public void load(T[] tt, FileGetter f) throws IOException {
			int am = f.i();

			for (int i = 0; i < am; i++) {
				if (i > tt.length) {
					long[] ll = new long[longCount];
					f.ls(ll);
				} else {
					load(tt[i], f);
				}
			}
			for (int i = am; i < tt.length; i++) {
				Arrays.fill(data(tt[i]), 0);
			}

		}

		public void load(LIST<T> tt, FileGetter f) throws IOException {
			int am = f.i();
			for (int i = 0; i < am; i++) {
				if (i > tt.size()) {
					long[] ll = new long[longCount];
					f.ls(ll);
				} else {
					load(tt.get(i), f);
				}
			}
			for (int i = am; i < tt.size(); i++) {
				Arrays.fill(data(tt.get(i)), 0);
			}

		}

	}

	private class Entries {

		private KeyMap<Entry> map = new KeyMap<Entry>();
		private final ArrayListGrower<Entry> entries = new ArrayListGrower<>();

		String push(String key, int cc, LONG_OE<T> ii) {
			String mkey = key + cc;
			if (map.containsKey(mkey)) {
				map.get(mkey).ee.printStackTrace();
				throw new RuntimeException(mkey);
			}
			Entry e = new Entry();
			e.key = key;
			e.mkey = mkey;
			e.type = cc;
			e.ii = ii;
			map.put(mkey, e);
			entries.add(e);
			e.ee = new RuntimeException();
			return mkey;
		}

		private class Entry {
			public String key;
			public String mkey;
			public int type;
			public LONG_OE<T> ii;
			public RuntimeException ee;
		}

	}

//	public static void main(String[] args) {
//		DataOL<TT> data = new DataOL<DataOL.TT>() {
//
//			@Override
//			protected long[] data(TT t) {
//				return t.data;
//			}
//		
//		};
//		
//		
//		
//		
//		
//		DataOL<TT>.DataFloat l1 = data. new DataFloat();
//		DataOL<TT>.DataFloat b1 = data. new DataFloat();
//		DataOL<TT>.DataFloat b2 = data. new DataFloat();
//		DataOL<TT>.DataDouble b3 = data. new DataDouble();
//		DataOL<TT>.DataDouble l2 = data. new DataDouble();
//		DataOL<TT>.DataDouble l3 = data. new DataDouble();
//		
//		
//		
//		TT t = new TT();
//		t.data = new long[ data.longCount()];
//		
//		l1.setD(t, 1);
//		l2.setD(t, 2);
//		l3.setD(t, 3);
//		
//
//		
//		

//		
//	}
//	
//	private static class TT {
//		
//		long[] data;
//		
//	}

}
