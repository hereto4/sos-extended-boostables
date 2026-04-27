package settlement.weather;

import java.io.IOException;

import game.time.TIME;
import settlement.main.SETT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;

public final class WeatherGrowthRipe extends WeatherThing {

	
	private static final double ripeStart = 1/8.0;
	private static final double ripeEnd = 5/8.0;
	private double d = 0;
	WeatherGrowthRipe(){
		super("Ripness", "");
	}
	
	private boolean ripening;
	private boolean ripe;
	
	public boolean cropsAreRipe() {
		return ripe;
	}
	
	@Override
	void update(double ds) {
		
		
		
		ripening = false;
		double part = TIME.years().bitPartOf();
		if (part > ripeEnd){
			d = CLAMP.d(1.0-(part-ripeEnd)*4.0, 0, 1);
		}else if (part > ripeStart) {
			ripening = true;
			d = CLAMP.d((part-ripeStart)*4.0, 0, 1);
		}else {
			d = 0;
		}
		
		ripening |= getD() > 0;
		ripe = getD() == 1 && SETT.WEATHER().moisture.getD() > 0.25;
		setD(d);
		super.update(ds);
	}
	
	public boolean ripening() {
		return ripening;
	}
	
	@Override
	protected void init() {
		update(0);
	}
	
	@Override
	protected void save(FilePutter file) {
		super.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		super.load(file);
		update(0);
	}
	
}
