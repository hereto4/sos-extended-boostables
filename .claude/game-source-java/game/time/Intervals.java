package game.time;

import java.io.IOException;

import game.GAME;
import game.GAME.GameResource;
import game.debug.Profiler;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;

public final class Intervals extends GameResource{

	private double i20 = 0;
	private double i15 = 0;
	private double i10 = 0;
	private double i08 = 0;
	private double i05 = 0;
	
	private double i04 = 0;
	private double i02 = 0;
	private double i01 = 0;
	private double i005 = 0;
	private double ran = 0;
	private double[] rans = new double[128];
	
	public Intervals() {
		super("INTER", true);
		for (int i = 0; i < rans.length; i++) {
			rans[i] = 0.1 + RND.rFloat();
		}
	}
	
	@Override
	protected void update(double ds, Profiler prof){
		i20 += ds*20.0;
		i15 += ds*15;
		i10 += ds*10;
		i08 += ds*8;
		i05 += ds*5;
		
		i04 += ds*4;
		i02 += ds*2;
		i01 += ds;
		i005 += ds*0.5;
		
		int ri = (int)ran&127;
		ran += rans[ri]*ds;
	}
	
	public int get20(){
		return (int) i20;
	}
	
	public int get15(){
		return (int) i15;
	}
	
	public int get05(){
		return (int) i05;
	}
	
	public int get08(){
		return (int) i08;
	}
	
	public int get04(){
		return (int) i04;
	}
	
	public int get02() {
		return (int) i02;
	}
	
	public int get01() {
		return (int) i01;
	}
	
	public int get005() {
		return (int) i005;
	}
	
	public int get10() {
		return (int) i10;
	}
	
	public double circle(double speed) {
		
		return circle(i01, speed);
		
	}
	
	public static double circle(double second, double speed) {
		
		double d = speed*second;
		d -= (int) d;
		
		if (d < 0.5) {
			return d*2;
		}else {
			return 1 - (d-0.5)*2;
		}
	}
	
	public static double circlePow(double second, double speed) {
		
		double d = speed*second;
		d -= (int) d;
		
		if (d < 0.5) {
			return Math.pow(d*2, 2);
		}else {
			return 1 - Math.pow((d-0.5)*2, 2);
		}
	}
	
	public int ran(double speed, int ran) {
		int t = (int) (((ran&0x0FF)/16.0+this.ran)*speed);
		return t;
	}
	
	public int ranC(double speed, int ran, int max) {
		int t = ran(speed, ran);
		t %= max*2;
		if (t > max)
			t = max*2-t;
		
		return t;
	}
	
	public static int get(double speed) {
		return (int) (GAME.intervals().get01()*speed);
	}

	@Override
	protected void save(FilePutter file) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		// TODO Auto-generated method stub
		
	}
}
