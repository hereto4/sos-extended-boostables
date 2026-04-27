package game.battle.thread.status;

import java.io.IOException;

import init.constant.Config;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;

final class BattleContext implements SAVABLE{
	
	final DivStatus[] statuses = new DivStatus[Config.battle().DIVISIONS_PER_BATTLE];
	final DivsTileMap map = new DivsTileMap(statuses);
	final DivsQuadMap quads = new DivsQuadMap();
	final DivsSpaceMap space = new DivsSpaceMap(statuses);
	final DivArmyMap army = new DivArmyMap(statuses);
	
	BattleContext() {
		for (int i = 0; i < statuses.length; i++)
			statuses[i] = new DivStatus();
	}

	@Override
	public void save(FilePutter file) {
		for (DivStatus s : statuses)
			s.save(file);

		
	}

	@Override
	public void load(FileGetter file) throws IOException {
		for (DivStatus s : statuses)
			s.load(file);
		
	}

	@Override
	public void clear() {
		for (DivStatus s : statuses)
			s.clear();
		
	}
	
}
