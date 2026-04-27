package game.battle.thread.position;

import java.io.IOException;

import game.battle.div.Div;
import game.battle.thread.BattleThread;
import init.constant.Config;
import snake2d.util.file.FileGetter;

public final class DivCentres extends BattleThread {

	private Context current = new Context();
	
	private Context[] next = new Context[] {
			new Context(),
			new Context(),
	};
	private volatile int ci = 0;
	private final Updater updater = new Updater();
	
	public DivCentres() {
		super(1.0/60);
		new Tests(this);
		
		
		
	}
	
	public DivCentre centre(Div d) {
		return current.statuses[d.index()];
	}
	
	public DivCentre centre(int di) {
		return current.statuses[di];
	}
	
	@Override
	protected void stop() {
		updater.stop = true;
		super.stop();
		updater.stop = false;
	}
	
	@Override
	protected void init() {
		updater.init(current);
	}
	
	public void init(Div div) {
		
		boolean started = thread.working();
		stop();
		updater.init(current, div);
		if (started)
			start();
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		
	}

	@Override
	protected void doThreadJob() {
		updater.init(next[ci]);
		if (updater.stop)
			return;
		Context c = current;
		current = next[ci];
		next[ci] = c;
		ci++;
		ci %= next.length;
		
	}
	
	static class Context {
		
		final DivCentre[] statuses = new DivCentre[Config.battle().DIVISIONS_PER_BATTLE];
		
		Context() {
			for (int i = 0; i < statuses.length; i++)
				statuses[i] = new DivCentre();
		}
		
	}


	
}
