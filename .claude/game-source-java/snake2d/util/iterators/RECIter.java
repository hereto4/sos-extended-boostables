package snake2d.util.iterators;

import java.io.Serializable;
import java.util.Iterator;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.RECTANGLE;

public class RECIter implements Iterator<COORDINATE>, Iterable<COORDINATE>, COORDINATE, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final RECTANGLE rec;
	private int ix,iy;
	
	public RECIter (RECTANGLE rec) {
		this.rec = rec;
	}
	
	public RECIter init() {
		ix = rec.x1()-1;
		iy = rec.y1();
		return this;
	}
	
	@Override
	public int x() {
		return ix;
	}

	@Override
	public int y() {
		return iy;
	}

	@Override
	public boolean hasNext() {
		return ix < rec.x2()-1 || iy < rec.y2()-1;
	}

	@Override
	public COORDINATE next() {
		ix ++;
		if (ix >= rec.x2()) {
			if (iy >= rec.y2())
				throw new RuntimeException();
			iy++;
			ix = rec.x1();
		}
		return this;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " " + ix + " " + iy;
	}

	@Override
	public Iterator<COORDINATE> iterator() {
		ix = rec.x1()-1;
		iy = rec.y1();
		return this;
	}
	
}