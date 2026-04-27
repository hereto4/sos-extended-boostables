package settlement.path.components;

import snake2d.util.map.MAP_OBJECT;

public abstract class SComponentLevel implements MAP_OBJECT<SComponent> {

	protected SComponentLevel() {
		
	}
	
	public abstract int componentsMax();

	public abstract SComponent getByIndex(int index);
	
	protected abstract void update();
	
	public abstract int level();
	
	public abstract int size();
	
	protected abstract void init();
	
}
