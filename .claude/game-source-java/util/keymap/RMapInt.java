package util.keymap;

import java.io.IOException;
import java.util.Arrays;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.sets.ArrayList;
import util.data.GETTER_TRANS;
import util.data.INT_O.INT_OE;

public class RMapInt<T extends MAPPED> implements INT_OE<T>, SAVABLE{

	private final RMAPS<T> map;
	private final int min;
	private final int max;
	private final int[] data;
	private int total;
	
	public RMapInt(RMAPS<T> map){
		this(map, Integer.MIN_VALUE, Integer.MAX_VALUE);
	}
	
	public RMapInt(RMAPS<T> map, int min, int max){
		this.map = map;
		this.min = min;
		this.max = max;
		data = new int[map.all().size()];
	}
	
	@Override
	public void save(FilePutter file) {
		map.saver().save(data, file);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		map.loader().load(data, file, 0);
		for (int i : data)
			total += i;
	}

	@Override
	public void clear() {
		Arrays.fill(data, 0);
		total = 0;
	}

	@Override
	public int get(T t) {
		if (t == null)
			return total;
		return data[t.index()];
	}

	@Override
	public int min(T t) {
		return min;
	}

	@Override
	public int max(T t) {
		return max;
	}

	@Override
	public void set(T t, int i) {
		total -= data[t.index()];
		data[t.index()] = i;
		total += data[t.index()];
	}
	
	public void setAll(int v) {
		Arrays.fill(data, v);
		total = v*data.length;
	}
	
	public static class RMapIntTwo <A extends MAPPED, B extends MAPPED> implements GETTER_TRANS<A, RMapInt<B>>, SAVABLE{

		private final ArrayList< RMapInt<B>> all;
		private final RMAPS<A> map;
		private final SAVABLE[] ss;
		
		public RMapIntTwo(RMAPS<A> map, RMAPS<B> map2){
			this(map, map2, Integer.MIN_VALUE, Integer.MAX_VALUE);
		}
		
		public RMapIntTwo(RMAPS<A> map, RMAPS<B> map2, int min, int max){
			this.map = map;
			all = new ArrayList< RMapInt<B>>(map.all().size());
			ss = new SAVABLE[map.all().size()];
			for (int i = 0; i < ss.length; i++) {
				RMapInt<B> b = new RMapInt<B>(map2, min, max);
				ss[i] = b;
				all.add(b);
			}
		}
		
		@Override
		public void save(FilePutter file) {
			map.saver().save(ss, file);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			map.loader().load(ss, file);
		}

		@Override
		public void clear() {
			for (RMapInt<B> b : all) {
				b.clear();;
			}
		}

		@Override
		public RMapInt<B> get(A f) {
			return all.get(f.index());
		}
		
		public void setAll(int v) {
			for (RMapInt<B> b : all) {
				b.setAll(v);
			}
		}
		
	}
	
}
