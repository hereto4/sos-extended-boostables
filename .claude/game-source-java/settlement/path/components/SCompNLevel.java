package settlement.path.components;

import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.TWIDTH;

import settlement.main.SETT;

public final class SCompNLevel extends SComponentLevel{

	private final int level;
	private final SCompNFactory factory;
	private final SCompNUpdater updater;
	private final int size;
	
	
	public SCompNLevel(SComponentLevel prev, int level, int size){
		this.level = level;
		factory = new SCompNFactory(level, size);
		this.size = size;
		updater = new SCompNUpdater(this, factory, prev, size);
	}
	
	void remove(SComponent toBeRemoved) {
		updater.remove(toBeRemoved);
	}
	
	void addNew(SComponent newSubComponent) {
		updater.add(newSubComponent);
	}
	
	
	@Override
	public SComponent get(int tile) {
		SComponent c = SETT.PATH().comps.all.get(level-1).get(tile);
		if (c == null)
			return null;
		return c.superComp();
	}

	@Override
	public SComponent get(int tx, int ty) {
		if (IN_BOUNDS(tx, ty))
			return get(tx+ty*TWIDTH);
		return null;
	}

	@Override
	public int componentsMax() {
		return factory.maxAmount();
	}


	@Override
	public SComponent getByIndex(int index) {
		return factory.get(index);
	}


	@Override
	protected void update() {
		updater.update();
	}


	@Override
	public int level() {
		return level;
	}


	@Override
	public int size() {
		return size;
	}


	@Override
	protected void init() {
		factory.clear();
	}

}
