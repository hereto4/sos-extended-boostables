package snake2d.util.datatypes;

import java.util.Iterator;

import snake2d.util.iterators.RECIter;

public abstract class RecFacade implements RECTANGLEE {

	private static final long serialVersionUID = 1L;
	private final RECIter inter = new RECIter(this);
	public RecFacade() {
	}

	

	public RecFacade set(double x1, double x2, double y1, double y2) {
		
		moveX1(x1);
		setWidth(x2 - x1);
		moveY1(y1);
		setHeight(y2 - y1);
		return this;
	}
	
	public RecFacade makePositive() {
		int x1 = x1();
		int x2 = x2();
		int y1 = y1();
		int y2 = y2();
		if (x2 < x1) {
			int x = (int) x1;
			x1 = x2;
			x2 = x;
		}
		
		if (y2 < y1) {
			int y = (int) y1;
			y1 = y2;
			y2 = y ;
		}
		return set(x1, x2, y1, y2);
	}

	public RecFacade set(RECTANGLE other) {
		moveX1(other.x1());
		moveY1(other.y1());
		setWidth(other.width());
		setHeight(other.height());
		return this;
	}

	public RecFacade set(BODY_HOLDER other) {
		set(other.body());
		return this;
	}

	@Override
	public RecFacade incr(double x, double y) {
		incrX(x);
		incrY(y);
		return this;
	}

	@Override
	public RecFacade incrX(double amount) {
		moveX1(x1() + amount);
		return this;
	}

	@Override
	public RecFacade incrY(double amount) {
		moveY1(y1() + amount);
		return this;
	}

	@Override
	public RecFacade incr(COORDINATE vector, double factor) {
		moveX1(x1() + vector.x() * factor);
		moveY1(y1() + vector.y() * factor);
		return this;
	}

	@Override
	public RecFacade incr(COORDINATE vector) {
		incrX(vector.x());
		incrY(vector.y());
		return this;
	}

	@Override
	public RecFacade moveX1Y1(double X, double Y) {
		moveX1(X);
		moveY1(Y);
		return this;
	}

	@Override
	public RecFacade moveX2(double X2) {
		moveX1(X2 - width());
		return this;
	}

	@Override
	public RecFacade moveY2(double Y2) {
		moveY1(Y2 - height());
		return this;
	}

	@Override
	public RecFacade moveX1Y1(COORDINATE vector) {
		moveX1Y1(vector.x(), vector.y());
		return this;
	}

	public abstract RecFacade setWidth(double width);
	public abstract RecFacade setHeight(double height);
	
	public RecFacade setDim(double width, double height) {
		setWidth(width);
		setHeight(height);
		return this;
	}
	

	
	public RecFacade setDim(double dim) {
		setWidth(dim);
		setHeight(dim);
		return this;
	}
	
	public RecFacade setDim(DIMENSION other) {
		setWidth(other.width());
		setHeight(other.height());
		return this;
	}

	public RecFacade scale(double Xmultiplier, double Ymultiplier) {
		setWidth(width() * Xmultiplier);
		setHeight(height() * Ymultiplier);
		return this;
	}

	@Override
	public RecFacade centerIn(RECTANGLE other) {
		centerX(other.x1(), other.x2());
		centerY(other.y1(), other.y2());
		return this;
	}

	@Override
	public RecFacade centerIn(double x1, double x2, double y1, double y2) {
		centerX(x1, x2);
		centerY(y1, y2);
		return this;
	}

	@Override
	public RecFacade centerX(double x1, double x2) {
		moveX1(x1 + ((x2 - x1) - width()) / 2);
		return this;
	}

	@Override
	public RecFacade centerY(double y1, double y2) {
		moveY1(y1 + ((y2 - y1) - height()) / 2);
		return this;
	}

	@Override
	public RecFacade centerX(RECTANGLE other) {
		centerX(other.x1(), other.x2());
		return this;
	}

	@Override
	public RecFacade centerY(RECTANGLE other) {
		centerY(other.y1(), other.y2());
		return this;
	}

	@Override
	public RecFacade moveC(COORDINATE v) {
		moveC(v.x(), v.y());
		return this;
	}

	@Override
	public RecFacade moveC(double X, double Y) {
		moveX1(X - width() / 2);
		moveY1(Y - height() / 2);
		return this;
	}

	@Override
	public RecFacade moveCX(double X) {
		moveX1(X - width() / 2);
		return this;
	}

	@Override
	public RecFacade moveCY(double Y) {
		moveY1(Y - height() / 2);
		return this;
	}

	@Override
	public int x2() {
		// TODO Auto-generated method stub
		return x1() + width();
	}
	
	
	@Override
	public int y2() {
		// TODO Auto-generated method stub
		return y1() + height();
	}

	@Override
	public int cX() {
		return (int) (x1() + width() / 2);
	}

	@Override
	public int cY() {
		return (int) (y1() + height() / 2);
	}

	public RecFacade scale(double scale) {
		scale(scale, scale);
		return this;
	}


	@Override
	public String toString() {
		return this.getClass().getName() + " x1:" + x1() + " x2:" + x2()
				+ " y1:" + y1() + " y2:" + y2();
	}

	public RecFacade incrW(double dWidth) {
		setWidth(width() + dWidth);
		return this;
	}

	public void incrH(double dHeight) {
		setHeight(height() + dHeight);
	}
	

	@Override
	public Iterator<COORDINATE> iterator() {
		return inter.init();
	}

	// @Override
	// public boolean isSameAs(RECTANGLE other) {
	// return !(gX1() != other.gX1() || gX2() != other.gX2() ||
	// gY1() != other.gY1() || gY2() != other.gY2());
	// }

	public static abstract class RecFacadePoint implements RECTANGLE {

		private static final long serialVersionUID = 1L;
		private final RECIter inter = new RECIter(this);
		public RecFacadePoint() {
		}

		@Override
		public int x2() {
			return x1() + width();
		}
		
		
		@Override
		public int y2() {
			return y1() + height();
		}

		@Override
		public int cX() {
			return (int) (x1() + width() / 2);
		}

		@Override
		public int cY() {
			return (int) (y1() + height() / 2);
		}

		@Override
		public String toString() {
			return this.getClass().getName() + " x1:" + x1() + " x2:" + x2()
					+ " y1:" + y1() + " y2:" + y2();
		}


		@Override
		public Iterator<COORDINATE> iterator() {
			return inter.init();
		}

		// @Override
		// public boolean isSameAs(RECTANGLE other) {
		// return !(gX1() != other.gX1() || gX2() != other.gX2() ||
		// gY1() != other.gY1() || gY2() != other.gY2());
		// }

	}
	
}
