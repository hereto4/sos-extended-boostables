package settlement.stats.law;

import java.io.IOException;

import game.time.TIME;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import util.text.D;

public final class Curfew {

	public static CharSequence ¤¤name = "¤Curfew";
	public static CharSequence ¤¤desc = "¤When a curfew is active, subjects will not visit their ordinary jobs, and stay home or inside, except when they need to visit basic services. This deters criminals, and prevents disease from being spread.";
	
	static {
		D.ts(Curfew.class);
	}
	
	Curfew(){
		
	}
	
	private double timer;
	
	void update(double ds) {
		if (timer > 0)
			timer -= ds;
	}
	
	public boolean is() {
		return timer > 0;
	}
	
	public boolean isSetForADay() {
		return timer > 0;
	}
	
	public void setForADay(boolean set) {
		if (set) {
			timer = TIME.secondsPerDay();
		}else
			timer = 0;
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			file.d(timer);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			timer = file.d();
		}
		
		@Override
		public void clear() {
			timer = 0;
		}
	};
	
}
