package snake2d.util.datatypes;

import java.io.Serializable;

import snake2d.UTIL;

public interface RECTANGLE extends DIMENSION, Serializable, Iterable<COORDINATE>{
	
	public int x1();
	public int x2();
	public int y1();
	public int y2();
	public int cX();
	public int cY();
	
	public default boolean holdsPoint(double x, double y){
		return (x >= x1() && x < x2()) && (y >= y1() && y < y2());
	}
	
	public default boolean holdsPoint(double x, double y, DIR d){
		return holdsPoint(x+d.x(), y+d.y());
	}
	
	public default boolean holdsPoint(COORDINATE coo){
		return holdsPoint(coo.x(),coo.y());
	}
	
	public default boolean holdsPoint(COORDINATE c, DIR d){
		return holdsPoint(c.x()+d.x(), c.y()+d.y());
	}
	
	public default boolean touches(double x, double y){
		return (x >= x1() && x <= x2()) && (y >= y1() && y <= y2());
	}
	
	public default boolean touches(BODY_HOLDER other){
		return touches(other.body());
	}
	
	public default boolean touches(RECTANGLE other){
		return ((x1() < other.x2() && x2() > other.x1())
				&& (y1() < other.y2() && y2() > other.y1()));
	}
	
	public default boolean touches(int x1, int x2, int y1, int y2){
		return ((x1() < x2 && x2() > x1)
				&& (y1() < y2 && y2() > y1));
	}
	
	public default boolean fitsIn(BODY_HOLDER other){
		return isWithin(other.body());
	}
	
	public default boolean isWithin(RECTANGLE other){
		return (x1() >= other.x1() && x2() <= other.x2() && 
				y1() >= other.y1() && y2() <= other.y2());
	}
	
	public default boolean isWithin(int x1, int x2, int y1, int y2){
		return (x1() >= x1 && x2() <= x2 && 
				y1() >= y1 && y2() <= y2);
	}
	
	public default boolean isSameAs(BODY_HOLDER other){
		return isSameAs(other.body());
	}
	
	public default boolean isSameAs(RECTANGLE other){
		return !(x1() != other.x1() || x2() != other.x2() ||
				y1() != other.y1() || y2() != other.y2());
	}
	
	public default int getDistance(RECTANGLE b){
		int x = Math.abs(cX() - b.cX());
		int y = Math.abs(cY()-b.cY());
		
		if (x > y){
			return (int) (UTIL.SQRT2*y) + x-y;
		}else if(x < y){
			return (int) (UTIL.SQRT2*x) + y-x;
		}else{
			return (int) (UTIL.SQRT2*x);
		}

	}
	

	public default boolean isOnEdge(int x, int y) {
		return holdsPoint(x, y) && (x == x1() || x == x2()-1 || y1() == y || y2()-1 == y);
	}

	
}



