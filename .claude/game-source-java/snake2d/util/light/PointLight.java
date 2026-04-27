package snake2d.util.light;

import snake2d.CORE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.RECTANGLE;

/**
 * A light-source that can be added to the renderer
 * @author mail__000
 *
 */
public class PointLight extends Coo implements LIGHT_POINT{
	
	private static final long serialVersionUID = 1L;
	private float red = 3;
	private float green = 3;
	private float blue = 3;
	private float z = 50;
	private float falloff = 2;
	private int radius;
	
	private int index;
	
	public PointLight() {
		setRadius(100);
	}

	public PointLight(double red, double green, double blue) {
		super();
		this.setRed(red);
		this.setGreen(green);
		this.setBlue(blue);
		setRadius(300);
	}
	
	public PointLight(double red, double green, double blue, int x, int y, int height) {
		super();
		this.setRed(red);
		this.setGreen(green);
		this.setBlue(blue);
		set(x, y);
		setRadius(300);
		this.setZ(height);
	}
	
	public PointLight(double red, double green, double blue, int x, int y, int height, int radius) {
		super();
		this.setRed(red);
		this.setGreen(green);
		this.setBlue(blue);
		set(x, y);
		setRadius(radius);
		this.setZ(height);
	}
	
	public static PointLight getFlashLight(){
		PointLight p = new PointLight();
		p.setRed(3f);
		p.setGreen(3f);
		p.setBlue(3f);
		p.setZ(45);
		p.setRadius(300);
		return p;
	}
	
	public static PointLight getFlashLight2(){
		PointLight p = new PointLight();
		p.setRed(3f);
		p.setGreen(3f);
		p.setBlue(3f);
		p.setZ(145);
		p.setRadius(300);
		return p;
	}
	
	public void register(){
		CORE.renderer().registerLight(this, x() - radius, x() + radius, y() - radius, y() + radius);
	}
	
	public void register(byte ne, byte se, byte sw, byte nw){
		CORE.renderer().registerLight(this, x() - radius, x() + radius, y() - radius, y() + radius, ne, se, sw, nw);
	}
	
	
	@Override
	public float getRed() {
		return red;
	}

	public PointLight setRed(double red) {
		this.red = (float) red;
		return this;
	}

	@Override
	public float getGreen() {
		return green;
	}

	public PointLight setGreen(double green) {
		this.green = (float) green;
		return this;
	}

	@Override
	public float getBlue() {
		return blue;
	}

	public PointLight setBlue(double blue) {
		this.blue = (float) blue;
		return this;
	}

	@Override
	public float cz() {
		return z;
	}

	public void setZ(int height) {
		this.z = height;
	}

	@Override
	public float getFalloff() {
		return falloff;
	}

	public void setFalloff(float falloff) {
		this.falloff = falloff;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}	
	
	public void setRadius(int radius) {
		this.radius = radius;
	}

	@Override
	public int getRadius() {
		return radius;
	}

	@Override
	public boolean isWithinRec(RECTANGLE other) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public float cx() {
		return (float) x();
	}

	@Override
	public float cy() {
		return (float) y();
	}
	
}
