package game.save;

import java.io.IOException;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;

public abstract class Savable {

	public final String key;
	
	public Savable(String key){
		this.key = key;
	}
	
	protected abstract void save(FilePutter file);
	protected abstract void load(FileGetter file) throws IOException;
	protected void loadFail() {
		throw new RuntimeException("Failed to load critical resource: " + key);
	}
}
