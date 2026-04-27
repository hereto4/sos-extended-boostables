package util.keymap;

import java.io.IOException;
import java.util.Arrays;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.sets.ArrayList;
import util.data.DOUBLE_O.DOUBLE_OE;
import util.data.GETTER_TRANS;

public class RMapD<T extends MAPPED> implements DOUBLE_OE<T>, SAVABLE{

	private final RMAPS<T> map;
	private final double[] data;
	
	public RMapD(RMAPS<T> map){
		this(map, Double.MIN_VALUE, Double.MAX_VALUE);
	}
	
	public RMapD(RMAPS<T> map, double min, double max){
		this.map = map;
		data = new double[map.all().size()];
	}
	
	@Override
	public void save(FilePutter file) {
		map.saver().save(data, file);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		map.loader().load(data, file, 0);
	}

	@Override
	public void clear() {
		Arrays.fill(data, 0);
	}

	@Override
	public double getD(T t) {
		return data[t.index()];
	}
	
	@Override
	public DOUBLE_OE<T> setD(T t, double d) {
		data[t.index()] = d;
		return this;
	}
	
	public static class RMapDTwo <A extends MAPPED, B extends MAPPED> implements GETTER_TRANS<A, RMapD<B>>, SAVABLE{

		private final ArrayList< RMapD<B>> all;
		private final RMAPS<A> map;
		private final SAVABLE[] ss;
		
		public RMapDTwo(RMAPS<A> map, RMAPS<B> map2){
			this(map, map2, Integer.MIN_VALUE, Integer.MAX_VALUE);
		}
		
		public RMapDTwo(RMAPS<A> map, RMAPS<B> map2, int min, int max){
			this.map = map;
			all = new ArrayList< RMapD<B>>(map.all().size());
			ss = new SAVABLE[map.all().size()];
			for (int i = 0; i < ss.length; i++) {
				RMapD<B> b = new RMapD<B>(map2);
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
			// TODO Auto-generated method stub
			
		}

		@Override
		public RMapD<B> get(A f) {
			return all.get(f.index());
		}
		
	}
	
}
