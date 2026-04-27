package game.time;

import init.settings.S;
import snake2d.util.misc.CLAMP;

public final class LightShadows {

	
	private double dir;
	private double sx,sy;
	private double sLength;
	private double tilt;
	boolean isNight;
	boolean rising;
	
	LightShadows(){
		
		
	}
	
	void update(Light light) {
		
		
		
		double ttilt = 1.0;
		if (S.get().lightCycle.get() == 0) {
			dir = 180;
			tilt = 25;
		}else {
			dir = (360+90) - light.time.getD()*360;
			dir %= 360;
			
			double dayL = TIME.seasons().currentDay.dayLength();
			double nightL = 1.0-dayL;
			
			final double nightA = (nightL)/2;
			final double mid = nightA + dayL/2;
			final double nightB = nightA + dayL;
			if (light.time.getD() < nightA) {
				ttilt = 1.0 - light.time.getD()/nightA;
				isNight = true;
				rising = false;
			}else if (light.time.getD() < mid){
				ttilt = (light.time.getD()-nightA)/(mid-nightA);
				isNight = false;
				rising = true;
			}else if (light.time.getD() < nightB) {
				ttilt = 1.0 - (light.time.getD() - mid)/(nightB-mid);
				isNight = false;
				rising = false;
			}else {
				ttilt = (light.time.getD() - nightB)/(1.0-nightB);
				isNight = true;
				rising = true;
			}
			
			ttilt = Math.pow(ttilt, 1);
			tilt = 5 + 35*ttilt;
		}

		
		sLength = 0.5 + 3 - 3*Math.pow(CLAMP.d(tilt/50, 0, 1), 1);
		double ra = Math.toRadians(dir);
		sLength = (1+S.get().shadows.getD())*sLength;
		
		sx = -sLength * Math.cos(ra);
		sy = -sLength * Math.sin(ra);
	}
	
	double dir() {
		return dir;
	}
	
	double dirNight() {
		return (100+dir)%360;
	}
	
	public double sx() {
		return sx;
	}
	
	public double sy() {
		return sy;
	}
	
	public double tilt() {
		return tilt;
	}
	
	public double dtilt() {
		return (tilt-5)/35;
	}
	

	
}
