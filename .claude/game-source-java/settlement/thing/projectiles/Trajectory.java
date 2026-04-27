package settlement.thing.projectiles;

import java.io.Serializable;

import init.constant.C;
import snake2d.LOG;

public class Trajectory implements Serializable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static final double G = 10*C.TILE_SIZE;
	public static final double FRICTION = C.TILE_SIZE*0.25;
	static final double ANGLE45 = 45.0/360.0;
	private static final double ANGLE75 = 75.0/360;
	static final double ANGLEMIN60 = -60.0/360.0;
	static final double EMARGIN2 = C.TILE_SIZEH*C.TILE_SIZEH;	
	
	public static final int HIT_HEIGHT = C.TILE_SIZE*2;
	public static final int RELEASE_HEIGHT = C.TILE_SIZE*2 + 5;
	private double vz;
	private double vx;
	private double vy;

	private static final int TFAR = 1;
	private static final int TSHORT = -1;
	private static final int THIT = 0;
	
	
	public double vx() {
		return vx;
	}
	
	public double vy() {
		return vy;
	}
	
	public double vz() {
		return vz;
	}
	
	public static double range(int h, double maxAngle, double velocity) {
		
		if (maxAngle > 45) {
			maxAngle = ANGLE45;
		}else if (maxAngle <= 0){
			return 0;
		}else {
			maxAngle = maxAngle/360;
		}
		
		return length(h, maxAngle, velocity);
	}
	
	public void set(double vx, double vy, double vz) {
		this.vx = vx;
		this.vy = vy;
		this.vz = vz;
	}
	
//	public boolean testRange(int height, int startX, int startY, int destX, int destY, double maxAngle, double velocity) {
//		final double dx = destX-startX;
//		final double dy = destY-startY;
//		final double L2 = dx*dx + dy*dy;
//	
//		return test(L2, height, ANGLE45, velocity) != TSHORT && test(L2, height, ANGLEMIN60, velocity) != TFAR;
//	}
	
	public boolean calcLow(int height, int startX, int startY, int destX, int destY, double maxAngle, double velocity) {
		
		
		double minAngle = ANGLEMIN60;
		if (maxAngle > 45) {
			maxAngle = ANGLE45;
		}else if (maxAngle <= 0){
			return false;
		}else {
			maxAngle = maxAngle/360;
		}
		
		final double dx = destX-startX;
		final double dy = destY-startY;
		final double L2 = dx*dx + dy*dy;
	
		int direction = test(L2, height, maxAngle, velocity);
		switch (direction) {
			case THIT: set(dx, dy, maxAngle, velocity); return true; // direct hit, lets go
			case TFAR: break; // too far, this is good
			case TSHORT: return false; //too short, can't reach
		}
		

		
		double delta = maxAngle-minAngle;
		delta /= 2;
		
		//just adjust them a little for rounding errors
		minAngle -= 0.005;
		
		//binary search lets go!
		
		//we are going to aim lower initially
		double angle = minAngle + delta;
		//the result of the shot
		direction = test(L2, height, angle, velocity);
		//our next delta angle
		delta /= 2;
		
		//just an emergency fail safe.
		int am = 0;
		while(am++ < 500) {
			int newDirection = test(L2, height, angle, velocity);
			
			
			
			if (newDirection == THIT) {
				set(dx, dy, angle, velocity);
				if (vz < 0 && height < 0)
					vz = -vz;
				return true;
			}else if (newDirection == TFAR) {
				if (angle < minAngle)
					return false;
			}else if (newDirection == TSHORT) {
				if (angle > maxAngle)
					return false;
			}
			
			if (newDirection != direction) {
				direction = newDirection;
				delta /= 2;
			}
			
			angle -= direction*delta;
			
			if (angle < minAngle || angle > maxAngle) {
				return false;
			}
		}
		return false;
	}
	
	public boolean calcHigh(int height, int startX, int startY, int destX, int destY, double maxAngle, double velocity) {
		
		if (maxAngle <= 45) {
			return false;
		}else if (maxAngle > 75){
			maxAngle = ANGLE75;
			
		}else {
			maxAngle = maxAngle/360;
		}
		double minAngle = ANGLE45;
		
		final double dx = destX-startX;
		final double dy = destY-startY;
		final double L2 = dx*dx + dy*dy;
	
		switch (test(L2, height, ANGLE45, velocity)) {
			case THIT: set(dx, dy, ANGLE45, velocity); return true; // direct hit, lets go
			case TFAR: break; // too far, this is good
			case TSHORT: return false; //too short, can't go high
		}
		
		switch (test(L2, height, maxAngle, velocity)) {
			case THIT: set(dx, dy, maxAngle, velocity); return true; // direct hit, lets go
			case TFAR: return false; // too far, this is bad
			case TSHORT: break; //too short, good
		}
		
		double delta = maxAngle-minAngle;
		delta /= 2;
		
		//just adjust them a little for rounding errors
		minAngle -= 0.005;
		maxAngle += 0.005;
		
		//binary search lets go!
		
		//we are going to aim higher initially
		double angle = minAngle + delta;
		//the result of the shot
		int direction = test(L2, height, angle, velocity);
		//our next delta angle
		delta /= 2;
		
		
		int am = 0;
		while(am++ < 500) {
			int newDirection = test(L2, height, angle, velocity);
			
			if (newDirection == THIT) {
				set(dx, dy, angle, velocity);
				if (vz < 0 && height < 0)
					vz = -vz;
				return true;
			}else if (newDirection == TFAR) {
				if (angle < minAngle)
					return false;
			}else if (newDirection == TSHORT) {
				if (angle > maxAngle)
					return false;
			}
			
			if (newDirection != direction) {
				direction = newDirection;
				delta /= 2;
			}
			
			angle += direction*delta;
		
		}
		return false;
	}
	
	private int test(double L2, double height, double angle, double velocity) {
		double l = length(height, angle, velocity);
		double m = L2-l*l;
		if (m < -EMARGIN2) {
			return TFAR;
		}else if(m > EMARGIN2) {
			return TSHORT;
		}else {
			return THIT;
		}
	}
	
	private void set(double dx, double dy, double angle, double velocity) {
		vz = Math.sin(angle*2*Math.PI)*velocity;
		double v = Math.cos(angle*2*Math.PI)*velocity;
		double l = Math.sqrt(dx*dx+dy*dy);
		vx = v*dx/l;
		vy = v*dy/l;
	}

	
	private static double length(double height, double angle, double velocity) {
		
		double vz = Math.sin(angle*2*Math.PI)*velocity;
		double v = Math.cos(angle*2*Math.PI)*velocity;
		double t = getTime(height, vz);
		
		return getLength(v, t);
	}
	
	public double getTime(double height) {
		return getTime(height, vz);
	}
	
//	static double getTime(double height, double vz) {
//		double p = vz*vz + 2*G*height;
//		if (height < 0)
//			return (vz - Math.sqrt(-p))/G;
//		return (vz + Math.sqrt(p))/G;
//	}
	
	public static double getTime(double height, double vz) {
        // quadratic: 0.5·G·t² - vz·t - height = 0
        double a = 0.5 * G;
        double b = -vz;
        double c = -height;

        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0) return Double.NaN;               // never hits

        double sqrtD = Math.sqrt(discriminant);

        // two possible roots
        double t1 = ( -b + sqrtD ) / (2 * a);
        double t2 = ( -b - sqrtD ) / (2 * a);

        // we want the later (positive) time
        if (t1 >= 0 && t2 >= 0) return Math.max(t1, t2);
        if (t1 >= 0)            return t1;
        if (t2 >= 0)            return t2;
        return Double.NaN; // both negative → already below ground
    }
	
	public static double getRange(double v, double time, double FRICTION) {
	    if (v <= 0) return 0.0;
	    return v * time - 0.5 * FRICTION * time * time;
	}

	static double getLength(double v, double time) {
		

		return time*v - 0.5*FRICTION*time*time;
	}
	
	@SuppressWarnings("unused")
	private static void debug(int i, double L2, double height, double angle, double velocity) {
		double vz = Math.sin(angle*2*Math.PI)*velocity;
		double v = Math.cos(angle*2*Math.PI)*velocity;
		double t = getTime(height, vz);
		double l = getLength(v, t);
		LOG.ln(i + " " + (int)(angle*360) + " " + v + " " + t + " " + l + " " + (L2-l));
		
	}

	
}
