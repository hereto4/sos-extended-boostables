package game.raiding;

import java.io.IOException;

import game.GAME;
import game.time.TIME;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import util.updating.IUpdater;

class Updater extends IUpdater{

	private static double raidingInterval = 6*16*TIME.secondsPerDay();
	private double timer = 0;
	
	public Updater(RAIDING r) {
		super(r.AMOUNT, TIME.secondsPerDay());
	}

	@Override
	protected void update(int i, double timeSinceLast) {
		if (GAME.raiders().current.current() != null)
			return;
		
		
		Raider r = GAME.raiders().ALL().get(i);
		if (!r.defeated && r.hasInterrest() && !r.isScared()) {
			timer += timeSinceLast;
			
			if (timer >= raidingInterval) {
				r.text.set(r, r.raids == 0);
				GAME.raiders().current.raid(r);
				timer -= raidingInterval*Math.sqrt(GAME.raiders().ALL().size());
			}
		}
	}

	@Override
	public void save(FilePutter file) {
		file.d(timer);
		super.save(file);
	}
	
	@Override
	public void load(FileGetter file) throws IOException {
		timer = file.d();
		super.load(file);
	}
	
	@Override
	public void clear() {
		timer = 0;
		super.clear();
	}
	
}
