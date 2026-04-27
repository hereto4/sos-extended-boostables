package snake2d.util.light;

import snake2d.CORE;
import snake2d.util.color.RGB.RGBImp;
import snake2d.util.datatypes.RECTANGLE;

/**
 * The light that will effect all pixels equally
 * @author mail__000
 *
 */
public class AmbientLight extends RGBImp implements LIGHT_AMBIENT{

	public final static AmbientLight Strongmoonlight = new AmbientLight(1.0f, 1.0f, 1.3f, 135, 35);
	public final static AmbientLight none = new AmbientLight(0,0,0,90,90);
	public final static AmbientLight full = new AmbientLight(1,1,1,0,90);

	
	private double tilt;
	private double direction;
	
	private float dirX;
	private float dirY;
	private float dirZ;
	
	public AmbientLight(){
		//super.set(0, CORE.getDisplay().nativeWidth, 0, CORE.getDisplay().nativeHeight);
		setDir(0);
		setTilt(90);
	}
	
	/**
	 * 
	 * @param d
	 * @param e
	 * @param f
	 * @param direction
	 * @param tilt
	 * @param tiltZ
	 */
	public AmbientLight(double d, double e, double f, float direction, float tilt){
		//super.set(0, CORE.getDisplay().nativeWidth, 0, CORE.getDisplay().nativeHeight);
		this.r((float) d);
		this.g((float) e);
		this.b((float) f);
		this.setDir(direction);
		this.setTilt(tilt);
	}
	
	public AmbientLight Set(AmbientLight other, double i) {
		copy(other).shade(i);;
		
		tilt = other.tilt;
		direction = other.direction;
		dirX = other.dirX;
		dirY = other.dirY;
		dirZ = other.dirZ;	
		return this;
	}
	
	public void setFullLight(){
		set(1, 1, 1);
	}


	@Override
	public AmbientLight r(double red) {
		super.r(red);
		return this;
	}

	@Override
	public AmbientLight g(double green) {
		super.g(green);
		return this;
	}

	@Override
	public AmbientLight b(double blue) {
		super.b(blue);
		return this;
	}
	
	/**
	 * 0 is from east, 90 from south, 180 west, 270 is from north
	 * @param deg
	 */
	public AmbientLight setDir(double deg) {
		direction = deg;
		calc();
		return this;
	}

	/**
	 * @return 90 is straight up, 0 is none, -90 is straight down
	 */
	public double getTilt(){
		return tilt;
	}
	
	/**
	 * 90 is straight up, 0 is none, -90 is straight down
	 * @param tilt2
	 */
	public AmbientLight setTilt(double tilt2) {
		if (tilt2 < -90)
			tilt2 = -90;
		else if (tilt2 > 90)
			tilt2 = 90;
		tilt = tilt2;
		calc();
		return this;
	}

	public double getDir(){
		return direction;
	}
	
	private void calc(){
		
		dirZ = (float)(Math.sin(Math.toRadians(tilt)));
		
		float q = (float)(Math.cos(Math.toRadians(tilt)));
		
		dirX = q*(float) (Math.cos(Math.toRadians(direction)));
		dirY = q*(float) (Math.sin(Math.toRadians(direction)));
		
//		float tmp = (float) Math.toRadians(direction);
//		
//		dirX = (float) (Math.sin(Math.toRadians(tilt))*Math.cos(tmp));
//		dirY = (float) (Math.sin(Math.toRadians(tilt))*Math.sin(tmp));
//		dirZ = -(float) (Math.cos(Math.toRadians(tilt)));
		
		
	}
	
	public void register(RECTANGLE r){
		register(r.x1(), r.x2(), r.y1(), r.y2());
	}
	
	public void register(int x1, int x2, int y1, int y2){
		CORE.renderer().registerAmbient(this, x1, x2, y1, y2);
	}
	
	@Override
	public float x() {
		return dirX;
	}

	@Override
	public float y() {
		return dirY;
	}
	
	@Override
	public float z() {
		return dirZ;
	}
	
	public void interpolate(AmbientLight from, AmbientLight to, double part){
		super.interpolate(from, to, part);
		this.tilt = from.tilt + (to.tilt-from.tilt)*part;
		this.direction = from.direction + (to.direction-from.direction)*part;
		this.calc();
	}
	
}
