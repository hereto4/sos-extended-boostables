package settlement.weather;

import game.time.TIME;
import util.text.D;

public final class WeatherDownfall extends WeatherThing{


	private static CharSequence ¤¤name = "Downfall";
	private static CharSequence ¤¤desc = "The amount of downfall.";
	private static double speed = 0.5/(TIME.secondsPerHour());
	private double target = 0;
	
	static {
		D.ts(WeatherDownfall.class);
	}
	
	WeatherDownfall() {
		super(¤¤name, ¤¤desc);
	}
	
	@Override
	void update(double ds) {
		setD(adjustTowards(getD(), ds*speed, target));
		target = 0;
	}
	
	public void setTarget(double target) {
		this.target = target;
	}
	
	@Override
	protected void init() {
		setD(0);
	}

}
