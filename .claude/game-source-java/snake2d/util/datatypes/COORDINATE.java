package snake2d.util.datatypes;

import snake2d.UTIL;

public interface COORDINATE {

	public int x();
	public int y();
	
	public default boolean isWithinRec(BODY_HOLDER shape){
		return isWithinRec(shape.body());
	}
	public default boolean isWithinRec(RECTANGLE shape){
		if (x() <= shape.x1() || x() > shape.x2() || y() <= shape.y1() || y() > shape.y2())
			return false;
		return true;
	}
	public default boolean isWithin(int x1, int x2, int y1, int y2){
		if (x() <= x1 || x() > x2 || y() <= y1 || y() > y2)
			return false;
		return true;
	}
	
	public default boolean touchesRec(BODY_HOLDER h) {
		return touchesRec(h.body());
	}
	
	public default boolean touchesRec(RECTANGLE shape) {
		if (x() < shape.x1() || x() > shape.x2() || y() < shape.y1() || y() > shape.y2())
			return false;
		return true;
	}
	
	public default double tileDistance(){
		return tileDistance(x(), y(), 0, 0);
	}
	
	public default double tileDistanceTo(COORDINATE b){
		return tileDistance(x(), y(), b.x(), b.y());
	}
	
	public default double tileDistanceTo(double bx, double by){
		return tileDistance(x(), y(), bx, by);
	}
	
	public default double distance(double bx, double by) {
		double dx = bx-x();
		double dy = by-y();
		return Math.sqrt(dx*dx + dy*dy);
	}
	
	public default double distance(COORDINATE c) {
		double dx = c.x()-x();
		double dy = c.y()-y();
		return Math.sqrt(dx*dx + dy*dy);
	}
	
	public static double tileDistance(double x1, double y1, double x2, double y2){
		
		double x = Math.abs(x1-x2);
		double y = Math.abs(y1-y2);
		
		if (x > y){
			return UTIL.SQRT2*y + x-y;
		}else if(x < y){
			return UTIL.SQRT2*x + y-x;
		}else{
			return UTIL.SQRT2*x;
		}
	}
	
	public static double tileDistance(COORDINATE a, COORDINATE b){
		return tileDistance(a.x(),  a.y(), b.x(), b.y());
	}
	
	public static double tileDistance(COORDINATE a, double x2, double y2){
		return tileDistance(a.x(),  a.y(), x2, y2);
	}
	
	public static double tileDistance(double x1, double y1, COORDINATE b){
		return tileDistance(x1,  y1, b.x(), b.y());
	}
	
	public static double properDistance(double x1, double y1, double x2, double y2) {
		double x = x1-x2;
		double y = y1-y2;
		return Math.sqrt(x*x+y*y);
	}
	
	public default double absSum(){
		return Math.abs(x()) + Math.abs(y());
	}
	
	public default boolean isSameAs(COORDINATE other){
		if (other == null)
			return false;
		return other.x() == x() && other.y() == y();
	}
	
	public default boolean isSameAs(double x, double y){
		return x == x() && y == y();
	}
	

	
}
