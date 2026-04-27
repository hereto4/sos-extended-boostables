package settlement.weather;

import game.GAME;
import game.audio.AUDIO;
import game.time.TIME;
import init.settings.S;
import snake2d.util.light.AmbientLight;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import util.text.D;

public final class WeatherThunder extends WeatherThing{

	private final AmbientLight flash = new AmbientLight();
	private double flashI;
	private boolean flashIs;
	
	private final static float thunderTimer = 4f;
	private final static float thunderLength = 0.2f;
	private float timer1 = RND.rFloat()*thunderTimer;
	private float timer2 = RND.rFloat()*thunderLength;
	private double soundTimer = 1;
	private double target;
	
	private static CharSequence ¤¤name = "Thunder";
	private static CharSequence ¤¤desc = "The amount of thunder.";

	private static double speed = 1.0/(TIME.secondsPerHour());
	
	static {
		D.ts(WeatherThunder.class);
	}
	
	
	WeatherThunder() {
		super(¤¤name, ¤¤desc);
	}

	@Override
	void update(double ds) {
		
		setD(adjustTowards(getD(), ds*speed, target));
		
		flashIs = false;
		
		if (timer1 > 0){
			timer1 -= getD()*ds;
		}else if(timer2 > 0){
			timer2 -= ds;
			flashIs = true;
		}else{
			timer2 = RND.rFloat()*thunderLength;
			if (RND.rInt(4) == 0){
				timer1 = RND.rFloat()*thunderTimer;
				flash.setDir(RND.rInt(360));
				flash.setTilt(RND.rInt(10));
				flashI = 0.5 + RND.rFloat()*8;
				
			}else {
				flashI/= 1.5;
			}
			
		}
		target = 0;
	}
	
	public void setTarget(double target) {
		this.target = CLAMP.d(target, 0, 1);
	}
	
	public void makeSounds(double gain, float ds){
		soundTimer -= ds*getD()/GAME.SPEED.speed();
		
		if (soundTimer < 0) {
			soundTimer = 1+RND.rFloat();
			AUDIO.AMBI().thunder.streams.rnd().playOnce();
		}
	}
	
	public void apply(int x1, int x2, int y1, int y2) {
		if (flashIs && S.get().graphics.get() == 1) {
			flash.r(flashI).g(flashI).b(flashI);
			flash.register(x1, x2, y1, y2);
		}
	}
	
	@Override
	protected void init() {
		setD(0);
	}
	
}
