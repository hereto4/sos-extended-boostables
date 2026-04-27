package snake2d.util.datatypes;

import java.io.IOException;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.rnd.RND;

public class VectorImp implements SAVABLE, VECTOR{
	
	private double x = 0;
	private double y = -1;
	private double magnitude = 0;
	private DIR dir = DIR.N;
	
	public VectorImp(){}
	
	public VectorImp(double x, double y){
		set(x, y);
	}
	
	public double set(double x, double y){
		this.x = x;
		this.y = y;
		double d = normalize();
		dir = DIR.ALL.get(getDirNr(this.x, this.y));
		return d;
	}
	
	public VectorImp set(VECTOR v){
		this.x = v.nX();
		this.y = v.nY();
		magnitude = v.magnitude();
		dir = v.dir(); 
		return this;
	}
	
	public double set(double aX, double aY, double bX, double bY){
		return set(bX-aX,bY-aY);
	}
	
	public double set(double aX, double aY, COORDINATE b){
		return set(b.x()-aX,b.y()-aY);
	}
	
	public double set(COORDINATE a, COORDINATE b){
		return set(b.x()-a.x(),b.y()-a.y());
	}
	
	public double set(RECTANGLE a, RECTANGLE b){
		return set(a.cX(),a.cY(),b.cX(),b.cY());
	}
	
	public double set(RECTANGLE a, double bX, double bY){
		return set(a.cX(),a.cY(),bX,bY);
	}
	
	
	public void randomize(){
		setAngle(RND.rFloat()*2);
	}
	
	public void setAngle(double radians){
		radians*= Math.PI;
		x = Math.sin(radians);
		y = Math.cos(radians);
		dir = DIR.ALL.get(getDirNr(this.x, this.y));
	}
	
	@Override
	public double magnitude(){
		return magnitude;
	}
	
	public void setMagnitude(double m){
		magnitude = m;
	}
	
	@Override
	public double nX(){
		return x;
	}
	
	@Override
	public double nY(){
		return y;
	}
	
	@Override
	public DIR dir(){
		return dir;
	}
	
	@Override
	public double x(){
		return x * magnitude;
	}
	
	@Override
	public double y(){
		return y * magnitude;
	}
	
	public void reverseX(){
		x = -x;
		dir = DIR.ALL.get(getDirNr(this.x, this.y));
	}
	
	public void reverseY(){
		y = -y;
		dir = DIR.ALL.get(getDirNr(this.x, this.y));
	}
	
	private double normalize(){
		if (x == 0 && y == 0)
			return 0;
		
		double length = Math.sqrt(x*x + y*y);
		x /= length;
		y /= length;
		return length;
	}
	
	public void rotate(double degrees){
		double radians = Math.toRadians(degrees);
		double sin = Math.sin(radians);
		double cos = Math.cos(radians);
		double newX = x*cos - y*sin;
		double newY = x*sin + y*cos;
		x = newX;
		y = newY;
		dir = DIR.ALL.get(getDirNr(this.x, this.y));
	}
	
	public void rotateRad(double radians){
		double sin = Math.sin(radians);
		double cos = Math.cos(radians);
		double newX = x*cos - y*sin;
		double newY = x*sin + y*cos;
		x = newX;
		y = newY;
		dir = DIR.ALL.get(getDirNr(this.x, this.y));
	}
	
	public VectorImp rotate90(){
		double newX = -y;
		double newY = x;
		x = newX;
		y = newY;
		dir = DIR.ALL.get(getDirNr(this.x, this.y));
		return this;
	}
	
	private static int getDirNr(double x, double y){
		
		double abs = Math.abs(x);
		
		if (abs < 0.38){
			if (y > 0)
				return 4;
			return 0;
		}else if(abs > 0.92){
			if (x > 0)
				return 2;
			return 6;
		}else if (y > 0){
			if (x > 0)
				return 3;
			return 5;
		}else{
			if (x > 0)
				return 1;
			return 7;
		}
		
	}
	
	@Override
	public String toString() {
		return "vector x: " + x + ", y:" + y + ", m:" + magnitude; 
	}

	@Override
	public void save(FilePutter file) {
		file.d(x);
		file.d(y);
		file.d(magnitude);
		file.b((byte) dir.id());
	}

	@Override
	public void load(FileGetter file) throws IOException {
		x = file.d();
		y = file.d();
		magnitude = file.d();
		dir = DIR.ALL.get(file.b());
		
	}

	@Override
	public void clear() {
		x = 0;
		y = 0;
		magnitude = 0;
		dir = DIR.C;
	}
}
