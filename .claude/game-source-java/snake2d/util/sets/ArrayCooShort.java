package snake2d.util.sets;

import java.io.IOException;
import java.io.Serializable;

import snake2d.util.datatypes.COORDINATEE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;

public class ArrayCooShort implements Serializable{

	private static final long serialVersionUID = 1L;
	private final short[] coos;
	private final int size;
	private final Coord coo = new Coord();
	private int i;
	
	public ArrayCooShort(int size){
		this.size = size;
		this.coos = new short[size*2];
	}
	
	public final void save(FilePutter fp) {
		fp.ss(coos);
		fp.i(i);
	}
	
	public final void load(FileGetter fp) throws IOException {
		fp.ss(coos);
		i = fp.i();
		set(i);
	}
	
	public COORDINATEE get() {
		return coo;
	}
	
	public int getI() {
		return i;
	}
	
	public COORDINATEE set(int i) {
		if (i < 0 || i >= size)
			throw new RuntimeException(i + " " + size);
		this.i = i;
		return coo;
	}
	
	public boolean hasNext() {
		return i < size-1;
	}
	
	public COORDINATEE next() {
		return set(i+1);
	}
	
	public int size() {
		return size;
	}
	
	public void copy(ArrayCooShort other) {
		if (size != other.size)
			throw new RuntimeException();
		
		for (int i = 0; i < coos.length; i++) {
			coos[i] = other.coos[i];
		}
		set(0);
	}
	
	public int x(int i) {
		return coos[i];
	}
	
	public int y(int i) {
		return coos[i+size];
	}
	
	
	private class Coord extends COORDINATEE.Abs implements Serializable{

		private static final long serialVersionUID = 1L;

		@Override
		public int x() {
			return coos[i];
		}

		@Override
		public int y() {
			return coos[i+size];
		}

		@Override
		public void xSet(double x) {
			coos[i] = (short) x;
		}

		@Override
		public void ySet(double y) {
			coos[i+size] = (short) y;
		}
		
		@Override
		public String toString() {
			return "COORD " + x() + " " + y();
		}
		
	}

	public void swap(int i1, int i2) {
		short x = coos[i2];
		short y = coos[i2+size];
		
		coos[i2] = coos[i1];
		coos[i2+size] = coos[i1+size];
		
		coos[i1] = x;
		coos[i1+size] = y;
		
	}

	public void shuffle(int max) {
		for (int i = 0; i < max; i++) {
			swap(RND.rInt(max), RND.rInt(max));
		}
	}
	
	public void shuffle(int from, int to) {
		int d = to-from;
		for (int i = from; i < to; i++) {
			swap(from + RND.rInt(d), from + RND.rInt(d));
		}
	}

	public void inc() {
		i++;
		i%=(size);
		
	}
	
	public void dec() {
		i--;
		if (i <= 0)
			i = 0;
	}

}
