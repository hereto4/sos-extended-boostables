package settlement.entity;

import java.io.IOException;

import init.constant.C;
import snake2d.util.datatypes.BODY_HOLDER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.VECTOR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;

public interface ESpeed extends VECTOR {

	public class Imp implements ESpeed {

		private double x = 0;
		private double y = -1;
		private double magnitude = 0;
		private double targetMagnitude = 0;
		private double acceleration = 10 * C.TILE_SIZE;
		private double topMagnitude = 10 * C.TILE_SIZE;
		// private final static int MAX = 31*C.TILE_SIZE;

		private DIR dir = DIR.N;
		private byte lastDir = (byte) dir.id();

		public Imp() {
		}

		public Imp accelerationInit(double a) {
			acceleration = a;
			return this;
		}

		public Imp magnitudeMaxInit(double topSpeed) {
			this.topMagnitude = topSpeed;
			return this;
		}

		public double magnitudeMax() {
			return topMagnitude;
		}

		public Imp magnitudeInit(double m) {
			this.magnitude = m;
			return this;
		}

		/**
		 * Sets direction, and magnitude
		 * 
		 * @param x
		 * @param y
		 * @return the length of x,y
		 */
		public void setRaw(double x, double y) {
			this.x = x;
			this.y = y;
			magnitude = normalize();
			setDir();
		}

		public void setRawNormalized(double x, double y, double magnitude) {
			this.x = x;
			this.y = y;
			this.magnitude = magnitude;
			setDir();
		}

		public void setRaw(DIR d, double magnitude) {
			this.x = d.xN();
			this.y = d.yN();
			this.magnitude = magnitude;
			setDirCurrent(d);
		}

		public Imp set(ESpeed.Imp master) {
			this.x = master.x;
			this.y = master.y;
			magnitude = master.magnitude;
			acceleration = master.acceleration;
			topMagnitude = master.topMagnitude;
			magnitudeTargetSetPrecise(master.targetMagnitude);
			setDirCurrent(master.dir);
			return this;
		}

		/**
		 * Sets direction, adjusts magnitude to simulate a turn.
		 * 
		 * @param x
		 * @param y
		 * @return
		 */
		public Imp turn2(double x, double y) {

			double ox = this.x;
			double oy = this.y;
			this.x = x;
			this.y = y;
			normalize();
			setDir();

			if (magnitude > 0) {
				double dot = ox * this.x + oy * this.y;
				dot += 1.0;
				dot /= 2.0;
				magnitude *= dot;
			}
			return this;
		}

		public Imp turn2(DIR d) {
			double ox = this.x;
			double oy = this.y;
			this.x = d.xN();
			this.y = d.yN();
			setDirCurrent(d);

			if (magnitude > 0) {
				double dot = ox * this.x + oy * this.y;
				dot += 1.0;
				dot /= 2.0;
				magnitude *= dot;
			}
			return this;
		}

		public Imp turn2(BODY_HOLDER h, double x, double y) {
			return turn2(h.body().cX(), h.body().cY(), x, y);
		}

		public Imp turn2(double aX, double aY, double bX, double bY) {
			return turn2(bX - aX, bY - aY);
		}

		public Imp turn2(COORDINATE a, COORDINATE b) {
			return turn2(b.x() - a.x(), b.y() - a.y());
		}

		public Imp turn2(RECTANGLE a, RECTANGLE b) {
			return turn2(a.cX(), a.cY(), b.cX(), b.cY());
		}

		public Imp turnRandom() {
			turn2Angle(RND.rFloat() * 2);
			return this;
		}

		public Imp turn2Angle(double angle) {
			double ox = this.x;
			double oy = this.y;
			angle *= Math.PI;
			x = Math.sin(angle);
			y = Math.cos(angle);
			setDir();
			if (magnitude > 0) {
				double dot = ox * this.x + oy * this.y;
				dot += 1.0;
				dot /= 2.0;
				magnitude *= dot;
			}
			return this;
		}

		public Imp turnWithAngel(double degrees) {
			double ox = this.x;
			double oy = this.y;
			double radians = Math.toRadians(degrees);
			double sin = Math.sin(radians);
			double cos = Math.cos(radians);
			double newX = x * cos - y * sin;
			double newY = x * sin + y * cos;
			x = newX;
			y = newY;
			setDir();
			if (magnitude > 0) {
				double dot = ox * this.x + oy * this.y;
				dot += 1.0;
				dot /= 2.0;
				magnitude *= dot;
			}
			return this;
		}

		public Imp turn90() {
			double newX = y;
			double newY = -x;
			x = newX;
			y = newY;
			if (magnitude > 0) {
				magnitude *= 0.5;
			}
			setDir();
			return this;
		}

		@Override
		public double magnitude() {
			return magnitude;
		}

		public boolean impulseBreak(double power) {
			magnitude -= topMagnitude * power;
			if (magnitude < 0) {
				magnitude = 0;
				return true;
			}
			return magnitude == 0;
		}

		public ESpeed.Imp sprint(double power) {
			magnitude += topMagnitude * power;
			if (magnitude > topMagnitude)
				magnitude = topMagnitude;
			return this;
		}

		public double magintudeMax() {
			return topMagnitude;
		}

		public double magnitudeTarget() {
			return targetMagnitude;
		}

		public Imp magnitudeTargetSet(double scale) {
			magnitudeTargetSetPrecise(scale * topMagnitude);
			return this;
		}

		public Imp magnitudeTargetSetPrecise(double scale) {
			targetMagnitude = scale;
			return this;
		}

		public double magnitudeRelative() {
			return magnitude / topMagnitude;
		}

		@Override
		public double nX() {
			return x;
		}

		@Override
		public double nY() {
			return y;
		}

		@Override
		public DIR dir() {
			return dir;
		}

		private void setDir() {
			setDirCurrent(DIR.ALL.get(getDirNr()));
		}

		public Imp setDirCurrent(DIR d) {
			lastDir = (byte) dir.id();

			if (d == DIR.C)
				d = DIR.N;
			dir = d;
			return this;
		}

		public DIR getPrevDir() {
			return lastDir < 0 ? DIR.N : DIR.ALL.get(lastDir);
		}

		public void setPrevDir() {
			setDirCurrent(getPrevDir());
		}

		@Override
		public double x() {
			return x * magnitude;
		}

		@Override
		public double y() {
			return y * magnitude;
		}

		public Imp reverseX() {
			x = -x;
			setDir();
			return this;
		}

		public Imp reverseY() {
			y = -y;
			setDir();
			return this;
		}

		private double normalize() {
			double length = Math.sqrt(x * x + y * y);
			if (length == 0) {
				y = -1;
				x = 0;
				length = 1;
			} else {
				x /= length;
				y /= length;
			}
			return length;
		}

		private int getDirNr() {

			double abs = Math.abs(x);

			if (abs < 0.38) {
				if (y > 0)
					return 4;
				return 0;
			} else if (abs > 0.92) {
				if (x > 0)
					return 2;
				return 6;
			} else if (y > 0) {
				if (x > 0)
					return 3;
				return 5;
			} else {
				if (x > 0)
					return 1;
				return 7;
			}

		}

		@Override
		public String toString() {
			return "vector x: " + x + ", y:" + y + ", m:" + magnitude;
		}

		// public double getVelocityZ() {
		// return vz;
		// }
		//
		// public void jump(double seconds) {
		// vz += seconds * 5;
		// }
		//
		// public void setVelocityZ(double value) {
		// vz = value;
		// }

		public boolean magnitudeAdjust(double ds, double d, double bonus) {
			double min = topMagnitude*0.25;
			double target = targetMagnitude * bonus;
			if (target >= min && magnitude < min)
				magnitude = min;
			if (magnitude < target) {
				magnitude += acceleration * d * ds;
				if (magnitude > target) {
					magnitude = target;
					return true;
				}
				return false;
			} else if (magnitude > target) {
				magnitude -= acceleration * d * ds;
				if (magnitude < target) {
					magnitude = target;
					return true;
				}
				return false;
			}
			return true;
		}
		
		public void brake(double ds) {
//			double dec = ds*magnitude*0.25;
//			dec = CLAMP.d(dec, 8*ds*C.TILE_SIZE, magnitude);
			magnitude -= ds*(8*C.TILE_SIZE+magnitude*0.1);
			if (magnitude < 0)
				magnitude = 0;
		}
		

		protected static final double AIR_REDUCER = 0.0025;

		public void applyAirFriction(float ds) {
			double m = magnitude * AIR_REDUCER;
			magnitude -= m * ds;

		}

		public boolean isZero() {
			return magnitude == 0;
		}

		public void check() {
			if (!Double.isFinite(magnitude) || !Double.isFinite(x) || !Double.isFinite(y))
				throw new RuntimeException(magnitude + " " + x + " " + y);
		}

		public double dot(double norX, double norY) {
			return Math.abs(x * norX + y * norY);
		}

		public void save(FilePutter file) {
			file.d(x).d(y).d(magnitude).d(targetMagnitude).d(acceleration).d(topMagnitude);
			file.b((byte) dir.id());
			file.b(lastDir);
		}
		
		public void load(FileGetter file) throws IOException {
			x = file.d();
			y = file.d();
			magnitude = file.d();
			targetMagnitude = file.d();
			acceleration = file.d();
			topMagnitude = file.d();
			dir = DIR.ALL.get(file.b());
			lastDir = file.b();
		}
	}

}
