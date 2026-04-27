package game.battle.thread;

import java.io.IOException;

import snake2d.SlaveThread;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.Debugger.Formatter;
import util.GUTIL;

public abstract class BattleThread{
	
	protected final SlaveThread thread;
	
	private final ACTION job = new ACTION() {
		
		@Override
		public void exe() {
			doThreadJob();
		}
	};
	
	protected BattleThread(double interval) {
		
		thread = new SlaveThread(this.getClass().getSimpleName(), interval);
		
		GUTIL.debugger().add(GUTIL.debugger().new Value(thread.name, 0, Formatter.PERCENTAGE) {
			
			@Override
			protected double getValue() {
				return thread.getUtilization();
			}
		});
		
	}
	

	
	protected void stop() {
		thread.setStopFlag();
		thread.waitUntilStopped();
	}
	
	protected void start() {
		thread.start(job);
	}

	protected abstract void doThreadJob();

	protected void save(FilePutter file) {
		// TODO Auto-generated method stub
		
	}
	
	protected void init() {
		
	}
	
	protected void load(FileGetter file) throws IOException {
		init();
	}
	
}