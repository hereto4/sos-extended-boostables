package snake2d.util.datatypes;

import java.io.IOException;
import java.util.Iterator;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.iterators.RECIter;

public class RecShort extends RecFacade implements SAVABLE{

	private static final long serialVersionUID = 1L;

	/**
	 * For temporary use only. May be used by everyone
	 */
	public final static RecShort TEMP = new RecShort();

	protected short x;
	protected short y;
	protected short width;
	protected short height;

	
	public RecShort() {
	}

	public RecShort(FileGetter f) throws IOException {
		load(f);
	}

	
	public RecShort(double dim) {
		set(0, dim, 0, dim);
	}
	
	public RecShort(double width, double height) {
		set(0, width, 0, height);
	}
	
	
	public RecShort(double x1, double x2, double y1, double y2) {
		set(x1, x2, y1, y2);
	}

	/**
	 * 
	 * @param other
	 *            get copy of this
	 */
	public RecShort(RECTANGLE other) {
		moveX1(other.x1());
		moveY1(other.y1());
		setWidth(other.width());
		setHeight(other.height());
	}

	@Override
	public RecShort moveX1(double X1) {
		x = (short) X1;
		return this;
	}

	@Override
	public RecShort moveY1(double Y1) {
		y = (short) Y1;
		return this;
	}

	@Override
	public RecShort incr(double x, double y) {
		incrX(x);
		incrY(y);
		return this;
	}

	@Override
	public RecShort incrX(double amount) {
		moveX1(x + amount);
		return this;
	}

	@Override
	public RecShort incrY(double amount) {
		moveY1(y + amount);
		return this;
	}

	@Override
	public RecShort incr(COORDINATE vector, double factor) {
		moveX1(x + vector.x() * factor);
		moveY1(y + vector.y() * factor);
		return this;
	}

	@Override
	public RecShort incr(COORDINATE vector) {
		incrX(vector.x());
		incrY(vector.y());
		return this;
	}

	@Override
	public RecShort moveX1Y1(double X, double Y) {
		moveX1(X);
		moveY1(Y);
		return this;
	}

	@Override
	public RecShort moveX2(double X2) {
		moveX1(X2 - width);
		return this;
	}

	@Override
	public RecShort moveY2(double Y2) {
		moveY1(Y2 - height);
		return this;
	}

	@Override
	public RecShort moveX1Y1(COORDINATE vector) {
		moveX1Y1(vector.x(), vector.y());
		return this;
	}

	@Override
	public RecShort setWidth(double width) {
		this.width = (short) width;
		return this;
	}

	@Override
	public RecShort setHeight(double height) {
		this.height = (short) height;
		return this;
	}
	
	@Override
	public RecShort setDim(double width, double height) {
		setWidth(width);
		setHeight(height);
		return this;
	}
	
	@Override
	public RecShort setDim(double dim) {
		setWidth(dim);
		setHeight(dim);
		return this;
	}
	
	@Override
	public RecShort setDim(DIMENSION other) {
		setWidth(other.width());
		setHeight(other.height());
		return this;
	}

	@Override
	public RecShort scale(double Xmultiplier, double Ymultiplier) {
		setWidth(width * Xmultiplier);
		setHeight(height * Ymultiplier);
		return this;
	}

	@Override
	public RecShort centerIn(RECTANGLE other) {
		centerX(other.x1(), other.x2());
		centerY(other.y1(), other.y2());
		return this;
	}

	@Override
	public RecShort centerIn(double x1, double x2, double y1, double y2) {
		centerX(x1, x2);
		centerY(y1, y2);
		return this;
	}

	@Override
	public RecShort centerX(double x1, double x2) {
		moveX1(x1 + ((x2 - x1) - width) / 2);
		return this;
	}

	@Override
	public RecShort centerY(double y1, double y2) {
		moveY1(y1 + ((y2 - y1) - height) / 2);
		return this;
	}

	@Override
	public RecShort centerX(RECTANGLE other) {
		centerX(other.x1(), other.x2());
		return this;
	}

	@Override
	public RecShort centerY(RECTANGLE other) {
		centerY(other.y1(), other.y2());
		return this;
	}

	@Override
	public RecShort moveC(COORDINATE v) {
		moveC(v.x(), v.y());
		return this;
	}

	@Override
	public RecShort moveC(double X, double Y) {
		moveX1(X - width / 2);
		moveY1(Y - height / 2);
		return this;
	}

	@Override
	public RecShort moveCX(double X) {
		moveX1(X - width / 2);
		return this;
	}

	@Override
	public RecShort moveCY(double Y) {
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
	public RecShort scale(double scale) {
		scale(scale, scale);
		return this;
	}



	@Override
	public String toString() {
		return this.getClass().getName() + " x1:" + x1() + " x2:" + x2()
				+ " y1:" + y1() + " y2:" + y2();
	}

	@Override
	public RecShort incrW(double dWidth) {
		setWidth(width += dWidth);
		return this;
	}

	@Override
	public void incrH(double dHeight) {
		setHeight(height + dHeight);
	}

	@Override
	public void save(FilePutter file) {
		file.s(x);
		file.s(y);
		file.s(width);
		file.s(height);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		x = file.s();
		y = file.s();
		width = file.s();
		height = file.s();
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

	public static class RecThreadSafe extends RecShort {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final RECIter iter = new RECIter(this);
		@Override
		public Iterator<COORDINATE> iterator() {
			return iter.init();
		}
		
	};

}
