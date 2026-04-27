package settlement.path.finders;

import static settlement.main.SETT.PATH;

import settlement.main.SETT;
import settlement.path.components.SComponent;
import settlement.path.components.finder.SCompFinder.SCompPath;
import settlement.path.path.SPath;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayCooShort;
import util.GUTIL;
import util.data.DOUBLE_O;

public class SFinderRND{
	
	private final ArrayCooShort cs = GUTIL.coos();
	
	public static final DOUBLE_O<SComponent> value = new DOUBLE_O<SComponent>() {

		@Override
		public double getD(SComponent t) {
			return 1.0;
		}
		
	};
	
	public static final DOUBLE_O<SComponent> otherPeople = new DOUBLE_O<SComponent>() {

		@Override
		public double getD(SComponent t) {
			return SETT.PATH().comps.data.people(true).get(t) > 0 ? 1 : 0.5;
		}
		
	};
	
	public static final DOUBLE_O<SComponent> noPeople = new DOUBLE_O<SComponent>() {

		@Override
		public double getD(SComponent t) {
			return SETT.PATH().comps.data.people(true).get(t) > 0 ? 0.5 : 1.0;
		}
		
	};
	
	public boolean find(int sx, int sy, SPath path, int distance) {
		
		return find(sx, sy, path, distance, value);
		
	}
	
	public boolean find(int sx, int sy, SPath path, int distance, DOUBLE_O<SComponent> value) {
		
		COORDINATE c = get(sx, sy, distance, value);
		if (c != null)
			return path.request(sx, sy, c);
		return false;
		
		
		
	}
	
	public COORDINATE get(int sx, int sy, int distance, DOUBLE_O<SComponent> value) {
		
		
		SCompPath p = PATH().comps.pather.fill(sx,  sy, distance);
		
		if (p == null || p.path().size() == 0)
			return null;
		
		cs.set(0);
		double vv = 0;
		for (SComponent c : p.path()) {
			vv = Math.max(vv, value.getD(c));
		}
		for (int i = 0; i < p.path().size() && cs.getI() < cs.size(); i++) {
			SComponent c = p.path().get(i);
			if (value.getD(c) >= vv) {
				cs.get().set(c.centreX(), c.centreY());
				cs.inc();
			}
		}
		
		cs.set(RND.rInt(cs.getI()));
		SComponent c = SETT.PATH().comps.zero.get(cs.get());
		
		int dim = c.level().size();
		int x1 = c.centreX() & ~(dim-1);
		int y1 = c.centreY() & ~(dim-1);
		
		cs.set(0);
		for (int y = 0; y < dim; y++) {
			for (int x = 0; x < dim; x++) {
				int dx = x1+x;
				int dy = y1+y;
				if (c.is(dx, dy) && SETT.PATH().finders.isGoodTileToStandOn(dx, dy, null)) {
					cs.get().set(dx, dy);
					cs.inc();
				}
			}
		}
		if (cs.getI() == 0) {
			for (int y = 0; y < dim; y++) {
				for (int x = 0; x < dim; x++) {
					int dx = x1+x;
					int dy = y1+y;
					if (c.is(dx, dy)) {
						cs.get().set(dx, dy);
						cs.inc();
					}
				}
			}
		}
		
		cs.set(RND.rInt(cs.getI()));
		return cs.get();
		
		
	}

}