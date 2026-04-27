package settlement.path.finders;

import settlement.main.SETT;
import settlement.path.components.SComponent;
import settlement.path.path.SPath;

public final class SFinderEntry {

	SFinderEntry(){
		
	}
	
	public boolean find(int sx, int sy, SPath path, int max) {
		SComponent s = SETT.PATH().comps.superComp.get(sx, sy);
		if (s == null)
			return false;
		if (s.hasEntry()) {
			if (path.request(sx, sy, point, max))
				return true;
		}
		if (s.hasEdge()) {
			return path.request(sx, sy, any, max);
		}
		return false;
	}
	
	public boolean any(int sx, int sy, SPath path, int max) {
		SComponent s = SETT.PATH().comps.superComp.get(sx, sy);
		if (s == null)
			return false;
		if (s.hasEdge()) {
			return path.request(sx, sy, any, max);
		}
		return false;
	}
	
	public boolean anyHas(int sx, int sy) {
		SComponent s = SETT.PATH().comps.superComp.get(sx, sy);
		if (s == null)
			return false;
		return s.hasEdge();
	}
	

	private final SFINDER point = new SFINDER() {
		
		@Override
		public boolean isInComponent(SComponent c, double distance) {
			return c.hasEntry();
		}
		
		@Override
		public boolean isTile(int tx, int ty, int tileNr) {
			if (SETT.PATH().solidity.is(tx, ty))
				return false;
			if (tx == 0 || tx == SETT.TWIDTH-1 || ty == 0 || ty == SETT.THEIGHT-1) {
				return SETT.ENTRY().points.map.is(tx, ty);
			}
			return false;
		}
	};
	
	private final SFINDER any = new SFINDER() {
		
		@Override
		public boolean isInComponent(SComponent c, double distance) {
			return c.hasEdge();
		}

		@Override
		public boolean isTile(int tx, int ty, int tile) {
			if (SETT.PATH().solidity.is(tx, ty))
				return false;
			return tx == 0 || tx == SETT.TWIDTH-1 || ty == 0 || ty == SETT.THEIGHT-1;
		}
	};
	
}
