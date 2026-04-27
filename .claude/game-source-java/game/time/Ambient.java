package game.time;

import init.settings.S;
import snake2d.CORE;
import snake2d.util.color.RGB;
import snake2d.util.color.RGB.RGBImp;
import snake2d.util.light.AmbientLight;

final class Ambient {
	

	private final RGBImp moon2 = new RGBImp().r(0.65).g(0.65).b(1.7);
	private final RGBImp dawn = new RGBImp().r(1.2).g(1.0).b(0.8);
	private final RGBImp dusk = new RGBImp().r(1.6).g(0.8).b(0.6);
	private final RGBImp day = new RGBImp().r(1).g(1).b(1);

	private final RGBImp w = new RGBImp();
	private final RGBImp moon = new RGBImp();
	
	private final AmbientLight work = new AmbientLight();
	private final AmbientLight work2 = new AmbientLight();
	public Ambient(){

	}
	
	public void apply(int x1, int x2, int y1, int y2, RGB tint) {
		
		double t = TIME.light().shadow.dtilt();
		
		moon.copy(moon2);
		//moon.shade(0.65 + S.get().nightGamma.getD()*0.7);
		

		double dd = 0.10;
		if (t < dd) {
			double d = t/dd;
			if (TIME.light().shadow.rising) {
				if (TIME.light().shadow.isNight) {
					w.interpolate(dusk, moon, d);
				}else {
					w.interpolate(dawn, day, d);
				}
			}else {
				d = 1.0-d;
				if (TIME.light().shadow.isNight) {
					w.interpolate(moon, dawn, d);
				}else {
					w.interpolate(day, dusk, d);
				}
			}
		}else {
			if (TIME.light().shadow.isNight) {
				w.copy(moon);
			}else {
				w.copy(day);
			}
			
		}

		double tilt = TIME.light().shadow.dtilt();
		double dir = TIME.light().shadow.dir();
		if (S.get().lightCycle.get() == 0) {
			w.copy(day);
		}
		
		
		
		double strength = 0.55 + 0.75*Math.pow((1.0 - tilt), 2);
		strength *= 4.5;
		strength *= (0.95 + S.get().brightness.getD()*0.5);

		
		
		
		
		double reflect = 0.2 + 0.3*(Math.sqrt(tilt));
		double oo = (1-reflect)/2;
		
		work.setTilt(TIME.light().shadow.tilt()).setDir(dir).copy(w);
		work.shade(strength);
		work.multiply(tint);

		CORE.renderer().lightDepthSet((byte) 0);
		work2.Set(work, oo);
		work2.register(x1, x2, y1, y2);
		
		CORE.renderer().lightDepthSet((byte) 127);
		work2.Set(work, oo);
		work2.register(x1, x2, y1, y2);
		
		double dTilt = TIME.light().shadow.tilt();
		
		CORE.renderer().shadeLight(false);
		work.setTilt(90-dTilt).setDir(dir+180).copy(w);
		work2.Set(work, reflect);
		work2.register(x1, x2, y1, y2);
		
	}
	

	
	
	
}
