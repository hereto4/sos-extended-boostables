package settlement.misc;

import static settlement.main.SETT.JOBS;
import static settlement.main.SETT.ROOMS;

import settlement.main.SETT;
import settlement.room.main.furnisher.FurnisherItemTile;
import snake2d.util.datatypes.DIR;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_OBJECT;

public final class SettPlacability {
	
	public final MAP_BOOLEAN solidityWill = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tile) {
			throw new RuntimeException();
		}

		@Override
		public boolean is(int tx, int ty) {
			if (SETT.PATH().solidity.is(tx, ty))
				return true;
			if (JOBS().getter.get(tx, ty) != null && JOBS().getter.get(tx, ty).becomesSolid())
				return true;
			if (ROOMS().fData.tile.get(tx, ty) != null && ROOMS().fData.tile.get(tx, ty).isBlocker())
				return true;
			return false;
		}
	};
	
	public final MAP_BOOLEAN willBlock = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tile) {
			throw new RuntimeException();
		}

		@Override
		public boolean is(int tx, int ty) {
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get(di);
				if (willBeBlocked(d, tx, ty))
					return true;
			}
			return false;
		}
		
		private boolean willBeBlocked(DIR from, int tx, int ty) {
			tx+= from.x();
			ty+= from.y();
			FurnisherItemTile t = ROOMS().fData.tile.get(tx, ty);
			if (t != null && t.mustBeReachable) {
				
				from = from.perpendicular();
				for (int di = 0; di < DIR.ORTHO.size(); di++) {
					
					DIR d = DIR.ORTHO.get(di);
					if (d == from)
						continue;
					if (!SETT.IN_BOUNDS(tx, ty, d))
						continue;
					if (!solidityWill.is(tx, ty, d))
						return false;
				}
				return true;
			}
			return false;
		}
	};
	
	public boolean willBeBlocked(int tx, int ty, int rx, int ry, MAP_OBJECT<?> dontCareAboutNonNull) {
		
		int dc = 0;
		for (int di = 0; di < DIR.ORTHO.size(); di++) {
			DIR d = DIR.ORTHO.get(di);
			if (dontCareAboutNonNull.is(rx, ry, d)) {
				dc ++;
				continue;
			}
			if (!solidityWill.is(tx, ty, d))
				return false;
		}
		return dc != DIR.ORTHO.size();
		
	}
	
}
