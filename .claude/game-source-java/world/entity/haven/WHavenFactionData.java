package world.entity.haven;

import game.faction.FACTIONS;
import game.faction.Faction;
import world.WORLD;

final class WHavenFactionData {

	final RaceData[] all;
	boolean dirty;
	private final int fi;
	
	public WHavenFactionData(WHavens havens, int fi) {
		this.fi = fi;
		all = new RaceData[havens.types.size()];
		for (int i = 0; i < all.length; i++)
			all[i] = new RaceData();
	}
	
	void init() {
		if (!dirty)
			return;
		Faction f = FACTIONS.getByIndex(fi);
		dirty = false;
		for (RaceData d : all)
			d.clear();
		if (!f.isActive())
			return;
		
		for (int i = 0; i < f.realm().regions(); i++) {
			for (WHaven h : WORLD.ENTITIES().havens.fill(f.realm().region(i))) {
				all[h.type().index()].add(h);
			}
			
		}
	}
	
	
	static class RaceData {
		
		int camps;
		int pop;
		double replenish;
		
		public void clear() {
			camps = 0;
			pop = 0;
			replenish = 0;
		}
		public void add(WHaven ii) {
			camps += 1;
			pop += ii.pop();
			replenish += ii.replenish();
		}
	}
	
}
