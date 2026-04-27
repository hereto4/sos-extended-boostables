package snake2d.util.datatypes;

import java.io.Serializable;

public interface COORDINATEE extends COORDINATE {

	public boolean set(double x, double y);

	public void xSet(double x);

	public void ySet(double y);

	public default boolean set(COORDINATE other) {
		return this.set(other.x(), other.y());
	}

	public void xIncrement(double amount);

	public void yIncrement(double amount);

	public void increment(COORDINATE other);

	public void increment(double x, double y);

	public void increment(COORDINATE other, double factor);

	public void xInvert();

	public void yInvert();

	public void xMakePos();

	public void xMakeNeg();

	public void yMakePos();

	public void yMakeNeg();

	public void scale(double xScale, double yScale);

	/**
	 * the coordinates will be reduced by the factors
	 * 
	 * @param factorX
	 * @param factorY
	 */
	public void deScale(double factorX, double factorY);

	public boolean isZero();

	public abstract class Abs implements COORDINATEE {

		@Override
		public boolean set(double x, double y) {
			if (x == x() && y == y()) {
				xSet(x);
				ySet(y);
				return false;
			}
			xSet(x);
			ySet(y);
			return true;
		}

		@Override
		public void xIncrement(double amount) {
			xSet(x()+amount);
		}

		@Override
		public void yIncrement(double amount) {
			ySet(y()+amount);
		}

		@Override
		public void increment(COORDINATE other) {
			increment(other.x(), other.y());
		}

		@Override
		public void increment(double x, double y) {
			xIncrement(x);
			yIncrement(y);
		}

		@Override
		public void increment(COORDINATE other, double factor) {
			increment(other.x()*factor, other.y()*factor);
		}

		@Override
		public void xInvert() {
			xSet(-x());
		}

		@Override
		public void yInvert() {
			ySet(-y());
		}

		@Override
		public void xMakePos() {
			if (x() < 0)
				xInvert();
		}

		@Override
		public void xMakeNeg() {
			if (x() > 0)
				xInvert();
		}

		@Override
		public void yMakePos() {
			if (y() < 0)
				yInvert();
		}

		@Override
		public void yMakeNeg() {
			if (y() > 0)
				yInvert();
		}

		@Override
		public void scale(double xScale, double yScale) {
			set(x()*xScale, y()*yScale);
		}

		@Override
		public void deScale(double factorX, double factorY) {
			increment(-x()*factorX, -y()*factorY);
		}

		@Override
		public boolean isZero() {
			return x() == 0 && y() == 0;
		}
		
	}
	
	public class Imp implements Serializable, COORDINATEE {

		private static final long serialVersionUID = 1L;
		private double X;
		private double Y;

		public Imp() {
			this(0, 0);
		}

		public Imp(double x, double y) {
			X = x;
			Y = y;
		}

		@Override
		public boolean set(double x, double y) {
			boolean ret = x != x() || y != y();
			X = x;
			Y = y;
			return ret;
		}

		@Override
		public void xSet(double x) {
			set(x, Y);
		}

		@Override
		public void ySet(double y) {
			set(X, y);
		}

		@Override
		public boolean set(COORDINATE other) {
			return this.set(other.x(), other.y());
		}

		@Override
		public void xIncrement(double amount) {
			xSet(X + amount);
		}

		@Override
		public void yIncrement(double amount) {
			ySet(Y + amount);
		}

		@Override
		public void increment(COORDINATE other) {
			increment(other.x(), other.y());
		}

		@Override
		public void increment(double x, double y) {
			xSet(X + x);
			ySet(Y + y);
		}

		@Override
		public void increment(COORDINATE other, double factor) {
			increment(other.x() * factor, other.y() * factor);
		}

		@Override
		public void xInvert() {
			xSet(-X);
		}

		@Override
		public void yInvert() {
			ySet(-Y);
		}

		@Override
		public void xMakePos() {
			if (X < 0)
				xSet(-X);
		}

		@Override
		public void xMakeNeg() {
			if (X > 0)
				xSet(-X);
		}

		@Override
		public void yMakePos() {
			if (Y < 0)
				ySet(-Y);
		}

		@Override
		public void yMakeNeg() {
			if (Y > 0)
				ySet(-Y);
		}

		@Override
		public void scale(double xScale, double yScale) {
			set(X*xScale, Y*yScale);
		}

		@Override
		public void deScale(double factorX, double factorY) {
			set(X-X*factorX, Y-Y*factorY);
		}

		@Override
		public int x() {
			return (int) X;
		}

		@Override
		public int y() {
			return (int) Y;
		}

		@Override
		public String toString() {
			return getClass().getName() + " x:" + X + " y:" + Y;
		}

		@Override
		public boolean isZero() {
			return x() == 0 && y() == 0;
		}

	}

}
