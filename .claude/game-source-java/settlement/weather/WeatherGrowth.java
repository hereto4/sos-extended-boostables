package settlement.weather;

import java.io.IOException;

import game.time.TIME;
import settlement.main.SETT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import util.text.D;

public final class WeatherGrowth extends WeatherThing {

	private static CharSequence ¤¤name = "Growth";
	private static CharSequence ¤¤desc = "The growth of plants.";
	private static double speed = 1.0/(2*TIME.secondsPerDay());
	private boolean isAutumn;
	
	static {
		D.ts(WeatherGrowth.class);
	}
	
	WeatherGrowth(){
		super(¤¤name, ¤¤desc);
	}
	
//	public boolean cropsAreRipe() {
//		return getD() == 1;
//	}
	
	@Override
	void update(double ds) {
		
		double g = getD();	
		isAutumn = TIME.years().bitPartOf() > 0.5;
		if(!isAutumn) {
			if (SETT.WEATHER().temp.heat() > 0)
				g += ds*speed*2;
		}else {
			if (SETT.WEATHER().temp.heat() < 0.25)
				g -= ds*speed;
		}
		
		setD(g);
		
		super.update(ds);
	}
	
	public boolean isAutumn() {
		return isAutumn;
	}
	
	@Override
	protected void init() {
		update(0);
		double d = 0;
		double p = TIME.years().bitPartOf();
		double tmp1 = SETT.WEATHER().temp.average(p-0.2);
		double tmp2 = SETT.WEATHER().temp.average(p);
		if (tmp1 > 0.5 && tmp2 > 0.5) {
			d = 1;
		}else if (tmp1 > 0.5) {
			d = 0.5;
		}else
			d = 0;
		setD(d);
	}
	
	@Override
	protected void save(FilePutter file) {
		super.save(file);
		file.bool(isAutumn);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		super.load(file);
		isAutumn = file.bool();
	}
	
}
