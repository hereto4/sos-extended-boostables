package settlement.weather;

import java.io.IOException;

import game.time.TIME;
import settlement.main.SETT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import util.text.D;

public final class WeatherSnow extends WeatherThing{
	
	private static CharSequence ¤¤name = "Snow";
	private static CharSequence ¤¤desc = "Amount of snow on the ground";
	private static double rainspeed = 1.0/(TIME.secondsPerHour()*4);
	private static double thawspeed = 1.0/(4*TIME.secondsPerHour());
	private double snowCount;
	
	static {
		D.ts(WeatherSnow.class);
	}
	
	WeatherSnow() {
		super(¤¤name, ¤¤desc);
	}
	
	@Override
	void update(double ds) {
		
		if (SETT.WEATHER().temp.cold() > 0) {
			snowCount -= ds*SETT.WEATHER().temp.cold();
			if (snowCount < 0)
				snowCount = -4;
		}else {
			snowCount += ds*SETT.WEATHER().temp.heat();
			if (snowCount > 0)
				snowCount = 4;
		}
		
		double d = getD();
		if (rainIsSnow()) {
			d += SETT.WEATHER().rain.getD()*rainspeed*ds;
		}else {
			d -= 2*SETT.WEATHER().rain.getD()*rainspeed*ds;
		}
		if (SETT.WEATHER().temp.heat() > 0) {
			d -= thawspeed*SETT.WEATHER().temp.heat()*ds;
			
		}
		setD(CLAMP.d(d, 0, 1));
		
		
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
	
	public boolean rainIsSnow() {
		return snowCount < 0;
	}
	
	@Override
	protected void save(FilePutter file) {
		super.save(file);
		file.d(snowCount);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		super.load(file);
		snowCount = file.d();
	}
	
}
