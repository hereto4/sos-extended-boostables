package settlement.weather;

import game.time.TIME;
import snake2d.util.misc.CLAMP;
import util.text.D;

public final class WeatherClouds extends WeatherThing{


	private static CharSequence ¤¤name = "Clouds";
	private static CharSequence ¤¤desc = "The amount of clouds.";
	private static double speed = 1.0/(1.5*TIME.secondsPerHour());
	private double target;
	
	static {
		D.ts(WeatherClouds.class);
	}
	
	WeatherClouds() {
		super(¤¤name, ¤¤desc);
	}
	
	@Override
	void update(double ds) {
		setD(adjustTowards(getD(), ds*speed, target));
		target = 0;
	}
	
	public void setTarget(double target) {
		this.target = CLAMP.d(target, 0, 1);
	}
	
	@Override
	protected void init() {
		setD(0);
	}

}
