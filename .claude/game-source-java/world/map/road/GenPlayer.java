package world.map.road;

import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListGrower;
import util.GUTIL;
import world.WORLD;
import world.map.regions.Region;
import world.map.regions.WREGIONS;

final class GenPlayer {

	private final RegGroup[] table = new RegGroup[WREGIONS.MAX];
	
	GenPlayer(ACTION aa){
		Flooder f = GUTIL.flooder();
		f.init(this);
		for (Region reg : WORLD.REGIONS().all()) {
			if (reg.info.area() > 0) {
				table[reg.index()] = new RegGroup(reg);
				f.pushSloppy(reg.cx(), reg.cy(), 0);
				f.setValue2(reg.cx(), reg.cy(), reg.index());
			}
		}
		
		
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			if (t.getParent() != null)
				t.setValue2(t.getParent().getValue2());
			Region home = WORLD.REGIONS().getByIndex((int) t.getValue2());
			Region now = WORLD.REGIONS().map.get(t);
			
			if (now == home || now == null) {
				
				for (DIR d : DIR.ALL) {
					if (WTRAV.can(t.x(), t.y(), d, true)) {
						Region to = WORLD.REGIONS().map.get(t, d);
						if (home == to || to == null)
							f.pushSmaller(t, d, t.getValue()+d.tileDistance(), t);
					}
				}
			}
		}
		
		aa.exe();
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (f.hasBeenPushed(c.x(), c.y())) {
				
				int from = (int) f.getValue2(c.x(), c.y());
				
				for (DIR d : DIR.ALL) {
					if (f.hasBeenPushed(c.x(), c.y(), d) && WTRAV.can(c.x(), c.y(), d, true)){
						int regTo = (int) f.getValue2(c.x(), c.y(), d);
						if (from != regTo) {
							join(from, regTo);
							
						}
					}
				}
			}
		}
		
		f.done();
		aa.exe();
		for (int i = 0; i < table.length; i++) {
			if (!join())
				break;
			aa.exe();	
				
		}
		
		
	}
	
	private boolean join() {
		
		RegGroup g = null;
		
		for (int i = 0; i < table.length; i++) {
			if (table[i] != null && table[i] != g) {
				if (g != null) {
					path(g, table[i]);
					join(g, table[i]);
					return true;
				}else {
					g = table[i];
				}
			}
		}
		
		return false;
	}

	void join(int a, int b){
		
		RegGroup ga = table[a];
		RegGroup gb = table[b];
		join(ga, gb);
		
	}
	
	void join(RegGroup ga, RegGroup gb){
		
		
		if (ga == gb)
			return;
		
		for (int i = 0; i < table.length; i++) {
			if (table[i] != null && table[i] == gb) {
				table[i] = ga;
			}
		}
		ga.regs.add(gb.regs);
		
	}
	
	void path(RegGroup ga, RegGroup gb) {
		Flooder f = GUTIL.flooder();
		f.init(this);
		
		for (Region r : ga.regs) {
			f.pushSloppy(r.cx(),r.cy(), 0, null);
		}
		

		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			Region now = WORLD.REGIONS().map.get(t);
			if (now != null && t.isSameAs(now.cx(), now.cy()) && table[now.index()] != ga) {
				join(ga, table[now.index()]);
				WTRAV.makeRoad(t);
				f.done();
				return;
			}
			
			
			for (DIR d : DIR.ALL) {
				if (WTRAV.can(t.x(), t.y(), d, false)) {
					if (now == null || table[now.index()] == ga || now == WORLD.REGIONS().map.get(t, d)) {
						double v = d.tileDistance();
						if (!WTRAV.can(t.x(), t.y(), d, true))
							v *= 32;
						f.pushSmaller(t, d, t.getValue()+v, t);
					}
					
					
				}
			}
		}
		f.done();
		
	}
	
	
	private static class RegGroup{
		
		final ArrayListGrower<Region> regs = new ArrayListGrower<>();
	
		RegGroup(Region reg){
			regs.add(reg);
		}
		
	}
			
	
}
