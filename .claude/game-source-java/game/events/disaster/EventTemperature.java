package game.events.disaster;

import java.io.IOException;

import game.events.EVENTS.EventResource;
import game.time.TIME;
import settlement.main.SETT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import util.text.D;
import view.sett.IDebugPanelSett;
import view.ui.message.MessageText;

public final class EventTemperature extends EventResource{
	
	private static CharSequence ¤¤ExtremeTemp = "Extreme Temperatures";
	private static CharSequence ¤¤ExtremeTempHot = "The temperature is rising to an extreme level today. Make sure you have ways for subjects to cool down!";
	private static CharSequence ¤¤ExtremeTempCold = "The temperature has plunged to record lows. Make sure our hearths are stocked with wood.";
	static {
		D.ts(EventTemperature.class);
	}
	
	private int dayLast = -1;
	
	public EventTemperature() {
		super("TEMP");
		IDebugPanelSett.add("Event: Temp", new ACTION() {
			
			@Override
			public void exe() {
				event(0.20, SETT.WEATHER().temp.average(TIME.years().bitPartOf()));
			}
		});
	}
	
	@Override
	protected void update(double ds) {
		if (dayLast != TIME.days().bitsSinceStart()) {
			dayLast = TIME.days().bitsSinceStart();
			if (TIME.days().bitsSinceStart() > 6) {
				double ave = SETT.WEATHER().temp.average(TIME.years().bitPartOf());
				double ran = RND.rFloat();
				for (int i = 0; i < 5; i++) {
					ran*= ran;
				}
				ran *= 0.25;
				
				if (ran > 0.15) {
					event(ran, ave);
				}
			}
			
		}
	}
	
	private void event(double ran, double ave) {
		
		if (ave < 0.5) {
			SETT.WEATHER().temp.setTarget(CLAMP.d(ave-ran, 0, 1));
			new MessageText(¤¤ExtremeTemp, ¤¤ExtremeTempCold).send();
		}else {
			SETT.WEATHER().temp.setTarget(CLAMP.d(ave+ran, 0, 1));
			new MessageText(¤¤ExtremeTemp, ¤¤ExtremeTempHot).send();
		}
	}

	@Override
	protected void save(FilePutter file) {
		file.i(dayLast);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		dayLast = file.i();
	}

	@Override
	protected void clear() {
		dayLast = -1;
	}

}
