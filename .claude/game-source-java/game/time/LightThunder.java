package game.time;

import snake2d.util.light.AmbientLight;
import snake2d.util.rnd.RND;

public final class LightThunder {
	
	private final AmbientLight flash = new AmbientLight();
	private double flashI;
	private boolean flashIs;
	
	private final static float thunderTimer = 5f;
	private final static float thunderLength = 0.2f;
	private float timer1 = RND.rFloat()*thunderTimer;
	private float timer2 = RND.rFloat()*thunderLength;
	
	LightThunder() {
	
	}
	
	void update(double ds) {
		
		flashIs = false;
		
		if (timer1 > 0){
			timer1 -= ds;
			
			
		}else if(timer2 > 0){
			timer2 -= ds;
			flashIs = true;
			
			
		}else{
			timer2 = RND.rFloat()*thunderLength;
			if (RND.rInt(4) == 0){
				timer1 = RND.rFloat()*thunderTimer;
				flash.setDir(RND.rInt(360));
				flash.setTilt(-10+RND.rInt(20));
				flashI = RND.rFloat()*5;
				
			}else {
				flashI/= 1.5;
			}
			
		}
		
	}
	
	public void apply(int x1, int x2, int y1, int y2) {
		if (flashIs) {
			flash.r(flashI).g(flashI).b(flashI);
			flash.register(x1, x2, y1, y2);
		}
	}
	
}
