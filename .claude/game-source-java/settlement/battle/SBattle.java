package settlement.battle;

import java.io.IOException;

import game.debug.Profiler;
import settlement.main.SETT;
import settlement.main.SETT.SettResource;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public final class SBattle extends SettResource{

	public final BannerRenderer bannerR = new BannerRenderer();
	public final ArmyTrainingInfo info;
	
	
	public SBattle(SETT sett) {
		super("battle", false);
		info = new ArmyTrainingInfo();
	}
	
	@Override
	protected void save(FilePutter file) {
		info.saver.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		info.saver.load(file);
	}
	
	@Override
	protected void clear() {
		info.saver.clear();
	}

	private double ti = 0;
	@Override
	protected void update(double ds, Profiler profiler) {
		ti += ds;
		if (ti > 0.1) {
			ti-= 0.1;
			info.update();
		}
	}
	
}
