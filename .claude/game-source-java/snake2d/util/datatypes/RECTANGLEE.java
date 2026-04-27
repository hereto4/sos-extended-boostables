package snake2d.util.datatypes;


public interface RECTANGLEE extends RECTANGLE{

	public RECTANGLEE incr(double x, double y);
	public RECTANGLEE incrX(double amount);
	public RECTANGLEE incrY(double amount);
	public RECTANGLEE incr(COORDINATE vector);
	public RECTANGLEE incr(COORDINATE vector, double factor);
	
	public RECTANGLEE moveX1Y1(double X, double Y);
	public RECTANGLEE moveX1Y1(COORDINATE vector);
	public default RECTANGLEE moveX1Y1(RECTANGLE other){
		moveX1Y1(other.x1(), other.y1());
		return this;
	}
	public RECTANGLEE moveX1(double X1);
	public RECTANGLEE moveX2(double X2);
	public RECTANGLEE moveY1(double Y1);
	public RECTANGLEE moveY2(double Y2);
	//public RECTANGLEE scale(double scale);
	
	public RECTANGLEE moveC(COORDINATE c);
	public RECTANGLEE moveC(double X, double Y);
	public RECTANGLEE moveCX(double X);
	public RECTANGLEE moveCY(double Y);
	public default RECTANGLEE centerIn(BODY_HOLDER b){
		return centerIn(b.body());
	}
	public RECTANGLEE centerIn(RECTANGLE other);
	public RECTANGLEE centerIn(double x1, double x2, double y1, double y2);
	public RECTANGLEE centerX(double x1, double x2);
	public default RECTANGLEE centerX(BODY_HOLDER b){
		return centerX(b.body());
	}
	public RECTANGLEE centerX(RECTANGLE other);
	public RECTANGLEE centerY(double y1, double y2);
	public default RECTANGLEE centerY(BODY_HOLDER b){
		return centerY(b.body());
	}
	public RECTANGLEE centerY(RECTANGLE other);

	public default RECTANGLEE fitIn(RECTANGLE other) {
		if (x1() < other.x1())
			moveX1(other.x1());
		if (y1() < other.y1())
			moveY1(other.y1());
		if (x2() > other.x2())
			moveX2(other.x2());
		if (y2() > other.y2())
			moveY2(other.y2());
		return this;
	}
	
}
