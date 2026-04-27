package snake2d.util.sets;

import java.io.IOException;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;

public class ArrayInt implements SAVABLE {

	private final int[] data;
	
	public ArrayInt(int size){
		this.data = new int[size];
	}
	
	public ArrayInt(LIST<?> li){
		this.data = new int[li.size()];
	}
	
	@Override
	public void save(FilePutter file) {
		file.isE(data);
	}
	
	@Override
	public void load(FileGetter file) throws IOException {
		file.isE(data);
	}
	
	@Override
	public void clear() {
		setAll(0);
	}
	
	public int get(int i) {
		return data[i];
	}
	
	public int get(INDEXED i) {
		return get(i.index());
	}
	
	public ArrayInt setAll(int v) {
		for (int i = 0; i < data.length; i++)
			data[i] = v;
		return this;
	}
	
	public ArrayInt set(int i, int v) {
		data[i] = v;
		return this;
	}
	
	public ArrayInt set(INDEXED i, int v) {
		return set(i.index(), v);
	}
	
	public ArrayInt inc(int i, int d) {
		data[i] += d;
		return this;
	}
	
	public ArrayInt inc(INDEXED i, int d) {
		return inc(i.index(), d);
	}
	
	public static class ArrayInt2D implements SAVABLE{
		
		private final ArrayInt[] ints;
		
		public ArrayInt2D(int h, int w){
			ints = new ArrayInt[h];
			for (int i = 0; i < h; i++) {
				ints[i] = new ArrayInt(w);
			}
		}

		@Override
		public void save(FilePutter file) {
			for (ArrayInt i : ints)
				i.save(file);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			for (ArrayInt i : ints)
				i.load(file);
		}

		@Override
		public void clear() {
			for (ArrayInt i : ints)
				i.clear();
		}
		
		public ArrayInt2D setAll(int v) {
			for (ArrayInt i : ints)
				i.setAll(v);
			return this;
		}
		
		public ArrayInt get(int i) {
			return ints[i];
		}
		
		public ArrayInt get(INDEXED i) {
			return ints[i.index()];
		}
		
	}
	
}
