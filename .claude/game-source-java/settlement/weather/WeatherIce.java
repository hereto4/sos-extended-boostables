package settlement.weather;

import game.time.TIME;
import settlement.main.SETT;
import util.text.D;

public final class WeatherIce extends WeatherThing{
	
	private static CharSequence ¤¤name = "Ice";
	private static CharSequence ¤¤desc = "Amount of ice on the water.";
	private static double thawspeed = 1.0/(8*TIME.secondsPerHour());
	
	static {
		D.ts(WeatherIce.class);
	}
	
	WeatherIce() {
		super(¤¤name, ¤¤desc);
	}
	
	@Override
	void update(double ds) {
		
		double c = getD();
		double d = -(SETT.WEATHER().temp.getD()-0.5)*2*thawspeed*ds;
		setD(c+d);
	}
	
	@Override
	protected void init() {
		double snow = 0;
		double p = TIME.years().bitPartOf();
		double tmp1 = SETT.WEATHER().temp.average(p-0.2);
		double tmp2 = SETT.WEATHER().temp.average(p);
		if (tmp1 < 0.5 && tmp2 < 0.5) {
			snow = 1;
		}else if (tmp1 < 0.5) {
			snow = 1 - (tmp2-0.5)*16;
		}
		setD(snow);
	}
	
	
	public boolean canBatheOutside() {
		return getD() < 0.1;
	}
	
}
