package settlement.stats.standing;

import java.io.IOException;

import settlement.stats.Induvidual;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import util.info.INFO;

public abstract class Standing {

	Standing() {
		
	}
	
	
	abstract void save(FilePutter file);

	abstract void load(FileGetter file) throws IOException;

	abstract void clear();

	public abstract double current();
	public abstract double target();
	
	public abstract double current(Induvidual a);
	
	public abstract INFO info();
	
}
