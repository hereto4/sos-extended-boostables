package world.map.pathing;

import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.Tree;
import util.GUTIL;
import world.WORLD;
import world.map.regions.Region;
import world.map.regions.WREGIONS;
import world.map.road.WTRAV;

final class GenConnect {

	private final RegGroup[] table = new RegGroup[WREGIONS.MAX];
	
	
	GenConnect(ACTION aa){

		Tree<RegGroup> sort = new Tree<RegGroup>(WREGIONS.MAX) {
			
			@Override
			protected boolean isGreaterThan(RegGroup current, RegGroup cmp) {
				return current.regs.size() > cmp.regs.size();
			}
		};
		
		for (Region reg : WORLD.REGIONS().all()) {
			if (reg.info.area() > 0) {
				table[reg.index()] = new RegGroup(reg);
				sort.add(table[reg.index()]);
			}
		}
		
		while(sort.hasMore()) {
			
			RegGroup g = sort.pollSmallest();
			if (g.regs.size() == 0)
				continue;
			if (!sort.hasMore())
				break;
			
			Flooder f = GUTIL.flooder();
			f.init(this);
			
			for (Region r : g.regs) {
				f.pushSloppy(r.cx(),r.cy(), 0, null);
			}
			

			while(f.hasMore()) {
				PathTile t = f.pollSmallest();
				Region now = WORLD.REGIONS().map.get(t);
				if (now != null && t.isSameAs(now.cx(), now.cy()) && table[now.index()] != g) {
					join(g, table[now.index()]);
					Gen.connect(t);
					sort.add(g);
					f.done();
					break;
				}
				
				
				for (DIR d : DIR.ALL) {
					
					if (!WORLD.IN_BOUNDS(t, d))
						continue;
					
					double v = 1;
					if (!WTRAV.can(t.x(), t.y(), d, false))
						v = 100;
					if (!WTRAV.can(t.x(), t.y(), d, true))
						v = 50;
					if (!WORLD.PATH().map.can(t, d))
						v = 25;
					f.pushSmaller(t, d, t.getValue()+v*d.tileDistance(), t);
					
				}
			}
			f.done();
			aa.exe();
		}
		
		
		
	}

	private void join(RegGroup ga, RegGroup gb){
		
		
		if (ga == gb)
			return;
		
		for (int i = 0; i < table.length; i++) {
			if (table[i] != null && table[i] == gb) {
				table[i] = ga;
			}
		}
		ga.regs.add(gb.regs);
		gb.regs.clear();
		
	}
	
	private static class RegGroup{
		
		final ArrayListGrower<Region> regs = new ArrayListGrower<>();
	
		RegGroup(Region reg){
			regs.add(reg);
		}
		
	}
	
	
}
