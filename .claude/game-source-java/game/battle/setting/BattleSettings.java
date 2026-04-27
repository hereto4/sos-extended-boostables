package game.battle.setting;

import java.io.IOException;

import game.GAME;
import game.battle.Armies;
import game.battle.div.Div;
import game.save.Savable;
import init.constant.Config;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import util.updating.IUpdater;

public class BattleSettings {

	private final DivSettings[] all = new DivSettings[Config.battle().DIVISIONS_PER_BATTLE];
	private final IUpdater updater = new IUpdater(Config.battle().DIVISIONS_PER_BATTLE, 1.0) {
		
		@Override
		protected void update(int i, double timeSinceLast) {
			all[i].update();
		}
	};
	
	public BattleSettings(Armies armies){
		for (Div d : armies.divisions()) {
			all[d.index()] = new DivSettings(d);
		}
		
		GAME.saver().addSpecialSaver(new Savable("BATTLE_DIV_SETTINGS") {
			
			@Override
			protected void save(FilePutter file) {
				for (DivSettings s : all) {
					s.save(file);
				}
			}
			
			@Override
			protected void load(FileGetter file) throws IOException {
				for (DivSettings s : all) {
					s.load(file);
				}
			}
			
			@Override
			protected void loadFail() {
				for (DivSettings s : all) {
					s.clear();
				}
			}
		});
		
	}
	
	public static DivSettings get(Div d) {
		return GAME.ARMIES().settings.all[d.index()];
	}
	
	public void update(double ds) {
		updater.update(ds);
	}
	
	public void init(Div d) {
		
		d.settings().update();
	}
	
}
