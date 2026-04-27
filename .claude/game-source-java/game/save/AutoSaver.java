package game.save;

import init.settings.S;
import snake2d.CORE;
import view.main.VIEW;

final class AutoSaver {

	private long last = -1;
	private long count = 0;
	private final GameSaver saver;
	
	
	AutoSaver(GameSaver saver){
		this.saver = saver;
	}
	
	void autosave(double ds) {
		
		
		if (S.get().autoSaveInterval.get() > 0 && VIEW.canSave()) {
			if (ds != 0) {
				if (last != -1) {
					count += CORE.getUpdateInfo().getNowMillis() - last;
				}
				last = CORE.getUpdateInfo().getNowMillis();
				
				long time = 1 + 2*(S.get().autoSaveInterval.max() - S.get().autoSaveInterval.get());
				time*= 1000*60;
				
				if (count >= time && VIEW.current().uiManager.isGoodTimeToSave()) {
					save();
					reset();
				}
			}
			
			
		}else {
			count = 0;
		}
	}
	
	private void save() {
		saver.saveNamed("AutoSave", S.get().autoSaveFiles.get(), true);
	}
	
	public void reset() {
		count = 0;
	}

	
}
