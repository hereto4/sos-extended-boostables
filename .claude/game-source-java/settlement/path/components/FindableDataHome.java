package settlement.path.components;

import init.type.HGROUP;
import init.type.HGROUP.HTypeBits;
import snake2d.util.sets.ArrayList;

public final class FindableDataHome {

	private final ArrayList<FindableData> all;

	FindableDataHome() {
		
		all = new ArrayList<>(HGROUP.all().size());
		for (HGROUP t : HGROUP.all()) {
			all.add(new FindableData("home " + t.name));
		}
		
	}

	void add(SComponent c, HTypeBits t) {
		for (int ti = 0; ti < HGROUP.all().size(); ti++) {
			if (t.is(ti)) {
				all.get(ti).add(c);
			}
		}
	}
	
	void remove(SComponent c, HGROUP t) {
		all.get(t.index()).remove(c);
	}

	public boolean has(SComponent c, HGROUP t) {
		return all.get(t.index()).get(c) > 0;
	}
	
	public final void reportPresence(int tx, int ty, HTypeBits t) {
		
		for (int ti = 0; ti < HGROUP.all().size(); ti++) {
			if (t.is(ti)) {
				all.get(ti).reportPresence(tx, ty);
			}
		}
		
		
		
	}
	
	public final void reportAbsence(int tx, int ty, HTypeBits t) {
		for (int ti = 0; ti < HGROUP.all().size(); ti++) {
			if (t.is(ti)) {
				all.get(ti).reportAbsence(tx, ty);
			}
		}
	}

	FindableData get(HGROUP t) {
		return all.get(t.index());
	}

}