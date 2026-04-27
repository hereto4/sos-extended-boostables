package settlement.thing.halfEntity;

import java.io.IOException;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.Stack;

public abstract class Factory<T extends HalfEntity>{

	final int index;
	private final Stack<T> free = new Stack<T>(128);
	
	protected Factory(LISTE<Factory<?>> all){
		index = all.add(this);
	}
	
	protected abstract void save(FilePutter file);

	protected abstract void load(FileGetter file) throws IOException ;

	protected abstract void clear();
	
	protected final T create() {
		if (!free.isEmpty())
			return free.pop();
		return make();
	}
	
	protected abstract T make();

	@SuppressWarnings("unchecked")
	protected void returnT(HalfEntity t) {
		if (!free.isFull())
			free.push((T) t);
	}
	
}
