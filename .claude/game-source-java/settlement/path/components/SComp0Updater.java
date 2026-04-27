package settlement.path.components;

import static settlement.main.SETT.PATH;

import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import snake2d.PathTile;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import util.GUTIL;

final class SComp0Updater {

	private final SComp0Map map;
	private final SComp0Factory factory;
	private final Rec bounds = new Rec();
	private final SComponentChecker checker;
	private boolean[][] assigned = new boolean[SComp0Level.SIZE][SComp0Level.SIZE];
	
	SComp0Updater(SComp0Map map, SComp0Factory f, SComp0Level c){
		this.map = map;
		this.factory = f;
		checker = new SComponentChecker(c); 
	}
	
	void remove(RECTANGLE r, SComp0Quads sComp0Quads) {
		
		
		for (int y = r.y1(); y < r.y2(); y++) {
			for (int x = r.x1(); x < r.x2(); x++) {
				SComp0 c = map.get(x, y);
				if (c != null && !c.retired()) {
					factory.retire(c);
				}
				map.set(x, y, null);
			}
		}
	}
	
	void removeSuperComp(RECTANGLE r, SComp0Quads sComp0Quads) {
		
		
		for (int y = r.y1(); y < r.y2(); y++) {
			for (int x = r.x1(); x < r.x2(); x++) {
				SComp0 c = map.get(x, y);
				if (c != null && c.superComp() != null) {
					SETT.PATH().comps.levels.get(0).remove(c.superComp());
				}
			}
		}
	}
	
	void assign(RECTANGLE r, SComp0Quads sComp0Quads) {
		for (int y = r.y1(); y < r.y2(); y++) {
			for (int x = r.x1(); x < r.x2(); x++) {
				assigned[y-r.y1()][x-r.x1()] = false;
			}
		}
		
		for (int y = r.y1(); y < r.y2(); y++) {
			for (int x = r.x1(); x < r.x2(); x++) {
				if (assigned[y-r.y1()][x-r.x1()] == false)
					assign(r, x, y);
			}
		}
		
	}
	
	private void assign(RECTANGLE r, int tx, int ty) {
		
		if (map.get(tx, ty) != null)
			return;
		
		AVAILABILITY a = PATH().availability.get(tx, ty);
		if (a.player < 0)
			return;
		
		GUTIL.flooder().init(this);
		GUTIL.flooder().pushSloppy(tx, ty, 0);
		SComp0 c = factory.create();
		bounds.clear();
		int size = 0;
		
		boolean other = false;
		
		while(GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollSmallest();
			map.set(t, c);
			assigned[t.y()-r.y1()][t.x()-r.x1()] = true;
			size++;
			bounds.unify(t.x(), t.y());
			for (int di = 0; di < DIR.ALL.size(); di++) {
				int dx = t.x()+DIR.ALL.get(di).x();
				int dy = t.y()+DIR.ALL.get(di).y();
				if (PATH().coster.player.getCost(t.x(), t.y(), dx, dy) > 0) {
					if (r.holdsPoint(dx, dy)) {
						if (map.get(dx, dy) == null) {
							double v = t.getValue() + DIR.ALL.get(di).tileDistance();
							GUTIL.flooder().pushSmaller(dx, dy, v);
						}
					}else if (!other)
						other = true;
				}
				
				
				
				
			}
		}
		
		GUTIL.flooder().done();
		
		if (!other) {
			for (int y = bounds.y1(); y < bounds.y2(); y++) {
				for (int x = bounds.x1(); x < bounds.x2(); x++) {
					if (map.get(x, y) == c) {
						map.set(x, y, null);
					}
				}
			}
			factory.retire(c);
			return;
		}
		
		c.init(bounds, size, checker);
		
		

		if (c.edgefirst() != null && c.edgefirst().next() == null && size <= r.width()*r.height()/2) {
			
			
		}
		
		SETT.PATH().comps.data.initComponent0(c, bounds);
		SETT.PATH().comps.levels.get(0).addNew(c);
//		c = map.get(tx, ty);
//		initData(c, bounds);
		//SETT.PATH().comps.levels.get(0).update(c);
		//fix finderdata ( c may be gone )
		
	}
	
	public void initData(RECTANGLE r) {
		checker.init();
		
		for (int y = r.y1(); y < r.y2(); y++) {
			for (int x = r.x1(); x < r.x2(); x++) {
				SComp0 c = map.get(x, y);
				if (c != null && !checker.isSetAndSet(c)) {
					SETT.PATH().comps.levels.get(0).remove(c.superComp());
					SETT.PATH().comps.levels.get(0).addNew(c);
					SETT.PATH().comps.data.initComponent0(c, r);
					
					
				}
			}
		}		
	}

	

}
