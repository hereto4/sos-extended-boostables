package game.battle;

import java.io.IOException;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public abstract class ArmyDiv {

	
	protected ArmyDiv() {

	}
	
	protected abstract void save(FilePutter file);
	
	protected abstract void load(FileGetter file) throws IOException;
	
	protected abstract void clear();
	
}
