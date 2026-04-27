package settlement.path.components;

import snake2d.util.sets.ArrayListIntegerResize;
import snake2d.util.sets.ArrayListResize;

final class SCompNFactory {
	
	private final ArrayListResize<SCompN> all;
	private final ArrayListIntegerResize unused;
	private final byte level;
	
	SCompNFactory(int level, int size){
		this.level = (byte) level;
		all = new ArrayListResize<>(2048/level, Integer.MAX_VALUE);
		unused = new ArrayListIntegerResize(512/level, Integer.MAX_VALUE);
	}
	
	public SCompN create() {
		if (unused.isEmpty()) {
			int i = all.size();
			SCompN c = new SCompN(i, level);
			all.add(c);
			c.retired = false;
			return c;
		}
		int i = unused.get(unused.size()-1);
		unused.remove(unused.size()-1);
		all.get(i).retired = false;
		return all.get(i);
	}
	
	public void clear() {
		all.clear();
		unused.clear();
	}

	public SCompN get(int id) {
		return all.get(id);
	}
	
	public void retire(SCompN c) {
		if (c.retired)
			return;
		unused.add(c.index());
		c.retire();
	}
	
	public int maxAmount() {
		return all.size();
	}
}
