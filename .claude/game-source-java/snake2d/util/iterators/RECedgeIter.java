package snake2d.util.iterators;

import java.util.Iterator;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.RECTANGLE;

public class RECedgeIter implements Iterator<COORDINATE>, Iterable<COORDINATE>{

	private int x1,x2,y1,y2;
	private int w;
	private int x,y;
	
	public static final RECedgeIter TMP = new RECedgeIter();
	private final Coo res = new Coo();
	
	public RECedgeIter init(RECTANGLE r) {
		return init(r.x1(), r.x2(), r.y1(), r.y2());
	}
	
	public RECedgeIter init(int x1, int x2, int y1, int y2) {
		x = x1;
		y = y1;
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
		w = x2-x1-1;
		if (w < 1)
			w = 1;
		return this;
	}
	
	@Override
	public boolean hasNext() {
		return x < x2 && y < y2;
	}

	@Override
	public COORDINATE next() {
		res.set(x,y);
		if (y == y1 || y == y2-1) {
			x++;
		}else {
			x += w;
		}
		
		if (x >= x2) {
			x = x1;
			y++;
		}
		return res;
	}

	@Override
	public Iterator<COORDINATE> iterator() {
		return this;
	}
	
	
}