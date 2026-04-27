package settlement.path.components;

import settlement.main.SETT;
import snake2d.util.map.MAP_BOOLEAN;

public final class SComp0Level extends SComponentLevel{

	
	public static final int SIZE = 8;
	public static int startSize = (SETT.TWIDTH/SIZE)*(SETT.TWIDTH/SIZE);
	private final SComp0Factory factory = new SComp0Factory();
	private final SComp0Map map = new SComp0Map(factory);
	final SComp0Quads quads = new SComp0Quads(SIZE);
	private final SComp0Updater updater = new SComp0Updater(map, factory, this);

	public SComp0Level(){
		
	}
	
	@Override
	protected void init() {
		factory.clear();
		map.clear();
		quads.changeAll();
	}
	
	public void update(int tx, int ty) {
		quads.setChangedAvailability(tx, ty);
	}
	
	public void changeSerives(int tx, int ty) {
		quads.setChangedServices(tx, ty);
	}
	
	public int comps() {
		return factory.maxAmount();
	}
	
	public SComponent comp(int i) {
		SComp0 c = factory.get(i);
		if (c != null && c.superComp() != null)
			return c;
		return null;
	}
	
	@Override
	protected void update() {
		quads.update(updater);
	}
	
	@Override
	public int componentsMax() {
		return factory.maxAmount();
	}

	@Override
	public SComp0 get(int tile) {
		return map.get(tile);
	}

	@Override
	public SComp0 get(int tx, int ty) {
		return map.get(tx, ty);
	}

	public boolean uping() {
		return quads.updating();
	}
	
	public MAP_BOOLEAN updating() {
		return quads.updating;
	}

	@Override
	public SComponent getByIndex(int index) {
		return factory.get(index);
	}

	@Override
	public int level() {
		return 0;
	}

	@Override
	public int size() {
		return SIZE;
	}


}
