package game.faction;

import java.io.IOException;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public abstract class FactionResource{
	
	protected abstract void save(FilePutter file);
	protected abstract void load(FileGetter file) throws IOException;
	protected abstract void clear();
	protected abstract void update(double ds, Faction f);

}
