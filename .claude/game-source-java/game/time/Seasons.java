package game.time;

import snake2d.util.color.RGB;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.text.DicTime;

public final class Seasons extends TIMECYCLE{
	
	public final Season SPRING;
	public final Season SUMMER;
	public final Season AUTUMN;
	public final Season WINTER;
	public final LIST<Season> ALL;
	public final InterPolation currentDay = new InterPolation();
	public final InterPolation previousDay = new InterPolation();
	public final InterPolation nextDay = new InterPolation();
	
	public Seasons(double seconds, Json jData, Json jText) {
		super((int)seconds, 4, DicTime.¤¤Season, DicTime.¤¤Seasons);
		SPRING = new Season(0, 0.25, "SPRING", jData, jText);
		SUMMER = new Season(1, 0, "SUMMER", jData, jText);
		AUTUMN = new Season(2, 0.25, "AUTUMN", jData, jText);
		WINTER = new Season(3, 1.0, "WINTER", jData, jText);
		ALL = new ArrayList<>(SPRING,SUMMER,AUTUMN,WINTER);
	}

	@Override
	public String bitName(int bit) {
		return ALL.get(bit).name;
	}
	
	public double winterValue() {
		double d = (1.0 - bitPartOf());
		double dd = 1.0-d;
		d = current().winterValue * d + next().winterValue * dd;
		return d; 
	}
	
	public Season current() {
		return ALL.get(bitCurrent()); 
	}
	
	public Season next() {
		return ALL.get((bitCurrent()+1)%ALL.size());
	}
	
	public Season next(int i) {
		i += bitCurrent();
		i &= 3;
		return ALL.get(i);
	}
	
	public final class Season{
		
		public final String name;
		private final int index;
		final double dayNightRatio;
		public final double red;
		public final double green;
		public final double blue;
		public final double winterValue;
		
		Season(int index, double winterValue, String key, Json data, Json text){
			name = text.text(key);
			data = data.json(key);
			dayNightRatio = data.d("NIGHTRATIO", 0.1, 0.9);
			red = data.d("RED", 0, 10); 
			green = data.d("GREEN", 0, 10); 
			blue = data.d("BLUE", 0, 10); 
			this.index = index;
			this.winterValue = winterValue;
		}
		
		public int index() {
			return index;
		}
		
	}

	public Season getWithOffset(double seconds) {
		int s = (int) (secondOfBit()+seconds);
		if (s < 0) {
			s += cycleSeconds()*Math.ceil(-s/cycleSeconds());
		}
		s /= bitSeconds();
		s %= ALL.size();
		return ALL.get(s);
	}
	
	@Override
	void update(double currentSecond) {
		super.update(currentSecond);
		currentDay.update(0);
		previousDay.update(-TIME.secondsPerDay());
		nextDay.update(TIME.secondsPerDay());
	}
	
	public class InterPolation implements RGB{
		
		private double red;
		private double green;
		private double blue;
		private double dayLength;
		private double winterValue;
		private int dayCurrent = -1;
		
		private InterPolation() {
			
		}
		
		private void update(int offSeconds) {
			double secSeasons = TIME.seasons().bitSeconds();
			int seasons = TIME.seasons().ALL.size();
			double s = TIME.years().bitSeconds() + TIME.currentSecond() + offSeconds;
			double d = TIME.secondsPerDay();

			double dPrev = s-d;
			double dNext = s+d;
			
			int prevI = (int)(dPrev/secSeasons);
			int nextI = (int)(dNext/secSeasons);
			int currentI = (int)(s/secSeasons);
			
			if (prevI != nextI) {
				if (prevI != currentI) {
					dPrev = currentI*secSeasons - dPrev;
					dNext = d + d-dPrev;
				}else if(nextI != currentI) {
					dNext = dNext-nextI*secSeasons;
					dPrev = d + d-dNext;
				}	
			}else {
				dPrev = d;
				dNext = d;
			}
			
			dPrev /= d*2.0;
			dNext /= d*2.0;
			
			prevI %= seasons;
			nextI %= seasons;
			currentI %= seasons;
			Season prev = TIME.seasons().ALL.get(prevI);
			Season next = TIME.seasons().ALL.get(nextI);
			
			red = dPrev*prev.red + dNext*next.red;
			green = dPrev*prev.green  + dNext*next.green;
			blue = dPrev*prev.blue + dNext*next.blue;
			winterValue = dPrev*prev.winterValue + dNext*next.winterValue;
			
			if (dayCurrent != TIME.days().bitsSinceStart()) {
				dayLength = 1.0 - (dPrev*prev.dayNightRatio + dNext*next.dayNightRatio);
				dayCurrent = TIME.days().bitsSinceStart();
			}
			
		}
		
		public double dayLength() {
			return dayLength;
		}
		
		public double winterValue() {
			return winterValue;
		}

		@Override
		public double r() {
			return red;
		}

		@Override
		public double g() {
			return green;
		}

		@Override
		public double b() {
			return blue;
		}
		
	}

}