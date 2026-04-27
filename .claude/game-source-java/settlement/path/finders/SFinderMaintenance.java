package settlement.path.finders;

import static settlement.main.SETT.PATH;

import init.resources.RBIT;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import settlement.main.SETT;
import settlement.path.components.SComponent;
import settlement.path.finders.SPathFinder.SPathUtilResult;
import settlement.path.path.SPath;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;

public final class SFinderMaintenance {

	private final RBITImp tbits = new RBITImp();
	private Coo coo = new Coo();
	
	SFinderMaintenance() {
		
	}
	
	public boolean has(int sx, int sy, RBIT bits) {
		return PATH().comps.data.maintenanceRes.has(sx, sy, bits) || 
				PATH().comps.data.maintenance.has(sx, sy);
	}
	
	public boolean find(RBIT bits, COORDINATE start, SPath path, int maxdistance) {
		return find(bits, start.x(), start.y(), path, maxdistance);
	}
	
	public boolean find(RBIT bits, int sx, int sy, SPath path, int maxdistance) {

		if (has(sx, sy, bits)) {
			this.tbits.clearSet(bits);
			if (path.request(sx, sy, finder, maxdistance)) {
				coo.set(path.destX(), path.destY());
				return true;
			}
			
		}
		
		return false;
	}
	
	public COORDINATE find(RBIT bits, int sx, int sy, int maxdistance) {
		this.tbits.clearSet(bits);
		if (has(sx, sy, tbits)) {
			
			SPathUtilResult r = SETT.PATH().finders.finder().find(sx, sy, finder, maxdistance);
			if (r != null) {
				coo.set(r.destX, r.destY);
				return coo;
			};
			
		}
		
		return null;
	}

	public COORDINATE findWithin(RBIT bits, int sx, int sy, int maxdistance, int mx, int my) {
		
		if (has(sx, sy, bits)) {
			this.tbits.clearSet(bits);
			RadiusChecker.self.check(mx, my, maxdistance);
			if (!RadiusChecker.self.is(sx, sy)) {
				sx = mx;
				sy = my;
			}
			SPathUtilResult r = SETT.PATH().finders.finder().find(sx, sy, finder2, maxdistance+128);
			if (r != null) {
				coo.set(r.destX, r.destY);
				return coo;
			};
			
		}
		
		return null;
	}
	
	public RBIT mask(int sx, int sy) {
		return PATH().comps.data.maintenanceRes.bits(sx, sy);
	}
	
	
	private SFINDER finder = new SFINDER() {
		
		@Override
		public boolean isInComponent(SComponent c, double distance) {
			return PATH().comps.data.maintenanceRes.has(c, tbits)
					|| PATH().comps.data.maintenance.get(c) > 0;
		}
		
		@Override
		public boolean isTile(int tx, int ty, int tileNr) {
			if (SETT.MAINTENANCE().reservable.is(tx, ty)) {
				RESOURCE res = SETT.MAINTENANCE().resource.get(tx, ty);
				return res == null || tbits.has(res);
			}
			return false;
		}
	};
	
	private SFINDER finder2 = new SFINDER() {
		
		@Override
		public boolean isInComponent(SComponent c, double distance) {
			return PATH().comps.data.maintenanceRes.has(c, tbits)
					|| PATH().comps.data.maintenance.get(c) > 0;
		}
		
		@Override
		public boolean canCross(SComponent c) {
			return RadiusChecker.self.is(c);
		}
		
		@Override
		public boolean isTile(int tx, int ty, int tileNr) {
			if (SETT.MAINTENANCE().reservable.is(tx, ty)) {
				RESOURCE res = SETT.MAINTENANCE().resource.get(tx, ty);
				return res == null || tbits.has(res);
			}
			return false;
		}
	};
	
	
	
	public void add(int tx, int ty) {
		if (SETT.MAINTENANCE().reservable.is(tx, ty)) {
			RESOURCE res = SETT.MAINTENANCE().resource.get(tx, ty);
			if(res != null) {
				PATH().comps.data.maintenanceRes.reportPresence(tx, ty, res);
			}else {
				PATH().comps.data.maintenance.reportPresence(tx, ty);
			}
		}

	}
	
	public void remove(int tx, int ty) {
		if (SETT.MAINTENANCE().reservable.is(tx, ty)) {
			RESOURCE res = SETT.MAINTENANCE().resource.get(tx, ty);
			if(res != null) {
				PATH().comps.data.maintenanceRes.reportAbsence(tx, ty, res);
			}else {
				PATH().comps.data.maintenance.reportAbsence(tx, ty);
			}
		}
	}

}
