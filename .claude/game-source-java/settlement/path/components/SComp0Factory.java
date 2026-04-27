package settlement.path.components;

import snake2d.util.sets.ArrayListIntegerResize;
import snake2d.util.sets.ArrayListResize;

final class SComp0Factory {

	
	private ArrayListResize<SComp0> all = new ArrayListResize<>(SComp0Level.startSize, Integer.MAX_VALUE);
	private ArrayListIntegerResize unused = new ArrayListIntegerResize(1024, Integer.MAX_VALUE);
	final SComp0 NONE = create();
	
	public SComp0 create() {
		if (unused.isEmpty()) {
			int i = all.size();
			SComp0 c = new SComp0(i);
			all.add(c);
			c.retire(false);
			return c;
		}
		int i = unused.get(unused.size()-1);
		unused.remove(unused.size()-1);
		all.get(i).retire(false);
		return all.get(i);
	}

	public void clear() {
		all.clear();
		unused.clear();
		all.add(NONE);
	}
	
	public SComp0 get(int id) {
		return all.get(id);
	}
	
	public void retire(SComp0 c) {
		unused.add(c.index());
		c.retire();
	}
	
	public int maxAmount() {
		return all.size();
	}
}
