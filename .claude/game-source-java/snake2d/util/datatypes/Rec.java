package snake2d.util.datatypes;

import java.io.IOException;
import java.util.Iterator;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.iterators.RECIter;

public class Rec extends RecFacade implements SAVABLE{

	private static final long serialVersionUID = 1L;

	/**
	 * For temporary use only. May be used by everyone
	 */
	public final static Rec TEMP = new Rec();

	protected double x;
	protected double y;
	protected double width;
	protected double height;

	
	public Rec() {
	}

	public Rec(FileGetter f) throws IOException {
		load(f);
	}

	
	public Rec(double dim) {
		set(0, dim, 0, dim);
	}
	
	public Rec(double width, double height) {
		set(0, width, 0, height);
	}
	
	
	public Rec(double x1, double x2, double y1, double y2) {
		set(x1, x2, y1, y2);
	}

	/**
	 * 
	 * @param other
	 *            get copy of this
	 */
	public Rec(RECTANGLE other) {
		moveX1(other.x1());
		moveY1(other.y1());
		setWidth(other.width());
		setHeight(other.height());
	}

	@Override
	public Rec moveX1(double X1) {
		x = X1;
		return this;
	}

	@Override
	public Rec moveY1(double Y1) {
		y = Y1;
		return this;
	}

	@Override
	public Rec incr(double x, double y) {
		incrX(x);
		incrY(y);
		return this;
	}

	@Override
	public Rec incrX(double amount) {
		moveX1(x + amount);
		return this;
	}

	@Override
	public Rec incrY(double amount) {
		moveY1(y + amount);
		return this;
	}

	@Override
	public Rec incr(COORDINATE vector, double factor) {
		moveX1(x + vector.x() * factor);
		moveY1(y + vector.y() * factor);
		return this;
	}

	@Override
	public Rec incr(COORDINATE vector) {
		incrX(vector.x());
		incrY(vector.y());
		return this;
	}

	@Override
	public Rec moveX1Y1(double X, double Y) {
		moveX1(X);
		moveY1(Y);
		return this;
	}

	@Override
	public Rec moveX2(double X2) {
		moveX1(X2 - width);
		return this;
	}

	@Override
	public Rec moveY2(double Y2) {
		moveY1(Y2 - height);
		return this;
	}

	@Override
	public Rec moveX1Y1(COORDINATE vector) {
		moveX1Y1(vector.x(), vector.y());
		return this;
	}

	@Override
	public Rec setWidth(double width) {
		this.width = width;
		return this;
	}

	@Override
	public Rec setHeight(double height) {
		this.height = height;
		return this;
	}
	
	@Override
	public Rec setDim(double width, double height) {
		setWidth(width);
		setHeight(height);
		return this;
	}
	
	@Override
	public Rec setDim(double dim) {
		setWidth(dim);
		setHeight(dim);
		return this;
	}
	
	@Override
	public Rec setDim(DIMENSION other) {
		setWidth(other.width());
		setHeight(other.height());
		return this;
	}

	@Override
	public Rec scale(double Xmultiplier, double Ymultiplier) {
		setWidth(width * Xmultiplier);
		setHeight(height * Ymultiplier);
		return this;
	}

	@Override
	public Rec centerIn(RECTANGLE other) {
		centerX(other.x1(), other.x2());
		centerY(other.y1(), other.y2());
		return this;
	}

	@Override
	public Rec centerIn(double x1, double x2, double y1, double y2) {
		centerX(x1, x2);
		centerY(y1, y2);
		return this;
	}

	@Override
	public Rec centerX(double x1, double x2) {
		moveX1(x1 + ((x2 - x1) - width) / 2);
		return this;
	}

	@Override
	public Rec centerY(double y1, double y2) {
		moveY1(y1 + ((y2 - y1) - height) / 2);
		return this;
	}

	@Override
	public Rec centerX(RECTANGLE other) {
		centerX(other.x1(), other.x2());
		return this;
	}

	@Override
	public Rec centerY(RECTANGLE other) {
		centerY(other.y1(), other.y2());
		return this;
	}

	@Override
	public Rec moveC(COORDINATE v) {
		moveC(v.x(), v.y());
		return this;
	}

	@Override
	public Rec moveC(double X, double Y) {
		moveX1(X - width / 2);
		moveY1(Y - height / 2);
		return this;
	}

	@Override
	public Rec moveCX(double X) {
		moveX1(X - width / 2);
		return this;
	}

	@Override
	public Rec moveCY(double Y) {
		moveY1(Y - height / 2);
		return this;
	}

	@Override
	public int x1() {
		return (int) x;
	}

	@Override
	public int x2() {
		return (int) (x + width);
	}

	@Override
	public int y1() {
		return (int) y;
	}

	@Override
	public int y2() {
		return (int) (y + height);
	}

	@Override
	public int height() {
		return (int) height;
	}

	@Override
	public int width() {
		return (int) width;
	}

	@Override
	public int cX() {
		return (int) (x + width / 2);
	}

	@Override
	public int cY() {
		return (int) (y + height / 2);
	}

	@Override
	public Rec scale(double scale) {
		scale(scale, scale);
		return this;
	}

	// @Override
	// public boolean holdsPoint(double X, double Y) {
	// return (X >= x && X < x+width) && (Y >= y && Y < y+height);
	// }
	//
	// @Override
	// public boolean holdsPoint(COORDINATE coo) {
	// return holdsPoint(coo.getX(), coo.getY());
	// }

	// @Override
	// public boolean isWithin(RECTANGLE other) {
	//
	// return (gX1() >= other.gX1() && gX2() <= other.gX2() &&
	// gY1() >= other.gY1() && gY2() <= other.gY2());
	//
	// }

	// @Override
	// public boolean touches(RECTANGLE other){
	// return ((x < other.gX2() && x+width > other.gX1())
	// && (y < other.gY2() && y+height > other.gY1()));
	// }

	public void unify(RECTANGLE o) {
		if (o.x1() < x) {
			width += x - o.x1();
			x = o.x1();
		}
		if (o.x2() > x2()) {
			width = o.x2() - x1();
		}
		if (o.y1() < y) {
			height += y - o.y1();
			y = o.y1();
		}
		if (o.y2() > y2()) {
			height = o.y2() - y1();
		}

	}
	
	public void unify(int xx, int yy) {
		if (width <= 0) {
			width = 1;
			x = xx;
		}
		if (height <= 0) {
			height = 1;
			y = yy;
		}
		
		if (xx < x) {
			width += x - xx;
			x = xx;
		}
		if (xx >= x2()) {
			width = xx - x1() +1;
		}
		if (yy < y) {
			height += y - yy;
			y = yy;
		}
		if (yy >= y2()) {
			height = yy - y1()+1;
		}

	}

	@Override
	public String toString() {
		return this.getClass().getName() + " x1:" + x1() + " x2:" + x2()
				+ " y1:" + y1() + " y2:" + y2();
	}

	@Override
	public Rec incrW(double dWidth) {
		setWidth(width += dWidth);
		return this;
	}

	@Override
	public void incrH(double dHeight) {
		setHeight(height + dHeight);
	}

	@Override
	public void save(FilePutter file) {
		file.d(x);
		file.d(y);
		file.d(width);
		file.d(height);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		x = file.d();
		y = file.d();
		width = file.d();
		height = file.d();
	}



	@Override
	public void clear() {
		x = -1;
		y = -1;
		width = 0;
		height = 0;
	}
	
	public double dx1() {
		return x;
	}
	
	public double dy1() {
		return x;
	}

	public static class RecThreadSafe extends Rec {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final RECIter iter = new RECIter(this);
		@Override
		public Iterator<COORDINATE> iterator() {
			return iter.init();
		}
		
	}

	public void pad(int w, int h) {
		x -= w;
		y -= h;
		width += w*2;
		height += h*2;
	};

}
