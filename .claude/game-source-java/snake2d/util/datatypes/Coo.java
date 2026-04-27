package snake2d.util.datatypes;
import java.io.IOException;
import java.io.Serializable;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;


public class Coo implements Serializable, COORDINATEE, SAVABLE{

	private static final long serialVersionUID = 1L;
	public static final Coo TMP = new Coo();
	public static final Coo TMP2 = new Coo();
	
	double X;
	double Y;
	
	public Coo(){
		this(0,0);
	}
	
	public Coo(COORDINATE c) {
		this(c.x(), c.y());
	}
	
	public Coo(double x, double y){
		X = x;
		Y = y;
	}
	
	@Override
	public boolean set(double x, double y){
		boolean ret = x != X || y != Y;
		this.X = x;
		this.Y = y;
		return ret;
	}
	@Override
	public void xSet(double x){X = x;}
	@Override
	public void ySet(double y){Y = y;}
	@Override
	public boolean set(COORDINATE other){
		return this.set(other.x(), other.y());
	}
	@Override
	public void xIncrement(double amount){X += amount;}
	@Override
	public void yIncrement(double amount){Y += amount;}
	@Override
	public void increment(COORDINATE other) {
		X += other.x();
		Y += other.y();
	}
	@Override
	public void increment(double x, double y) {
		X += x;
		Y += y;
	}
	@Override
	public void increment(COORDINATE other, double factor) {
		X += other.x()*factor;
		Y += other.y()*factor;
	}
	
	@Override
	public void xInvert(){X *= -1f;}
	@Override
	public void yInvert(){Y *= -1f;}
	@Override
	public void xMakePos(){if (X < 0) X = -X;}
	@Override
	public void xMakeNeg(){if (X > 0) X = -X;}
	@Override
	public void yMakePos(){if (Y < 0) Y = -Y;}
	@Override
	public void yMakeNeg(){if (Y > 0) Y = -Y;}
	
	@Override
	public void scale(double xScale, double yScale){
		X *= xScale;
		Y *= yScale;
	}
	
	/**
	 * the coordinates will be reduced by the factors
	 * @param factorX
	 * @param factorY
	 */
	@Override
	public void deScale(double factorX, double factorY){
		X -= X*factorX;
		Y -= Y*factorY;
	}
	
	@Override
	public int x(){return (int) X;}
	@Override
	public int y(){return (int) Y;}
	
	@Override
	public String toString() {
		return getClass().getSimpleName() + " --> (" + X + ", " + Y + ")";
	}
	
	public void decrease(double amountX, double amountY){
		if(X < -amountX){
			X += amountX;
		}else if(X > amountX){
			X -= amountX;
		}else{
			X = 0;
		}
		if(Y < -amountY){
			Y += amountY;
		}else if(Y > amountY){
			Y -= amountY;
		}else{
			Y = 0;
		}
	}
	
	@Override
	public boolean isZero(){
		return x() == 0 && y() == 0;
	}

	@Override
	public void save(FilePutter file) {
		file.d(X);
		file.d(Y);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		X = file.d();
		Y = file.d();
	}

	@Override
	public void clear() {
		X = -1;
		Y = -1;
		
	}
	
}