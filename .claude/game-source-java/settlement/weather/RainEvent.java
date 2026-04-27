package settlement.weather;

import java.io.IOException;

import game.time.TIME;
import settlement.main.SETT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import view.sett.IDebugPanelSett;

final class RainEvent {
	
	private double downfall;
	private double time;
	private double thunder;
	private double cloud;
	private double timeToNext;
	
	private double droughtNext;
	private double droughtLength;

	
	RainEvent(){
		saver.clear();
		IDebugPanelSett.add("WEATHER DROUGHT", new ACTION() {
			
			@Override
			public void exe() {
				droughtNext = 0;
			}
		});
	}
	
	void rain(double size) {
		downfall = CLAMP.d(size+RND.rFloat(), 0.1, 1);
		time = TIME.secondsPerHour()*(0.5+RND.rFloat()*4);
		if (time*downfall < TIME.secondsPerHour()*0.7)
			time = 0.7*TIME.secondsPerHour()/downfall;
		cloud = 0.5 + RND.rFloat()*0.5;
		thunder = 0;
		double wind = RND.rFloat();
		
		if (SETT.WEATHER().temp.heat() > 0 && RND.rBoolean()) {
			wind += RND.rFloat();
			thunder = RND.rFloat();
			cloud += 0.5;
		}
		SETT.WEATHER().wind.setDayTarget(wind);
		

	}
	
//	private double dd = 0;
	
	void update(double ds) {
		SWEATHER w = SETT.WEATHER();
		
		droughtNext -= ds*(SETT.ENV().climate().tempCold + SETT.ENV().climate().tempWarm)*0.5;
		if (time > 0) {
			time -= ds;
			
			w.rain.setTarget(downfall);
			w.clouds.setTarget(cloud);
			w.thunder.setTarget(thunder);
			
		}else if (droughtNext < 0){
			if (w.moisture.growthValue() < 1) {
				droughtLength -= ds;
				if (droughtLength < 0) {
					setNextDrought(1);
					setNextRain();
					rain(RND.rFloat());
				}
			}
		}else {
			timeToNext -= ds;
			if (timeToNext < 0) {
				setNextRain();
				rain(RND.rFloat());
			}
		}
		
	}
	
	private void setNextRain() {
		timeToNext = (0.25 + RND.rFloat()*2.7)*TIME.secondsPerDay();
	}
	
	private void setNextDrought(int cooloffYears) {
		droughtNext = (cooloffYears + RND.rFloat()*10)*TIME.secondsPerDay()*TIME.years().bitConversion(TIME.days());
		droughtLength = (0.25 + 4 * RND.rFloat())*TIME.secondsPerDay();
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.d(downfall);
			file.d(time);
			file.d(thunder);
			file.d(cloud);
			file.d(timeToNext);
			file.d(droughtNext);
			file.d(droughtLength);
			
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			downfall = file.d();
			time= file.d();
			thunder= file.d();
			cloud= file.d();
			timeToNext= file.d();
			droughtNext = file.d();
			droughtLength = file.d();
		}
		
		@Override
		public void clear() {
			time = 0;
			setNextRain();
			setNextDrought(5);
			timeToNext /= 2;
		}
	};
	
}