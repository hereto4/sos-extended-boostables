package settlement.path.finders;

import init.type.HCLASSES;
import init.type.HGROUP;
import init.type.HGROUP.HTypeBits;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.path.components.SComponent;
import settlement.path.path.SPath;
import settlement.room.home.HOME;
import settlement.room.home.house.HomeInstance;
import settlement.room.main.RoomInstance;
import settlement.room.main.throne.THRONE;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;

public final class SFinderHome implements SFINDER {

	private Coo current = new Coo();
	private HGROUP type;

	public SFinderHome() {
		
		// TODO Auto-generated constructor stub
	}

	public boolean findHome(Humanoid h, SPath path) {
		
		
		if (h.indu().clas() != HCLASSES.NOBLE() && STATS.WORK().EMPLOYED.get(h) == null) {
			HOME home = STATS.HOME().GETTER.get(h, this);
			if (home != null) {
				int sx = home.serviceX();
				int sy = home.serviceY();
				return path.requestFull(h.tc(),sx, sy);
			}
			HOME oddJob = SETT.ROOMS().HOME.odd.get(h, this);
			if (oddJob == null)
				return false;
			STATS.HOME().GETTER.set(h, oddJob);
			int sx = oddJob.serviceX();
			int sy = oddJob.serviceY();
			return path.requestFull(h.tc(), sx, sy);
		}
		
		{
			HOME home = STATS.HOME().GETTER.get(h, this);
			if (home != null) {
				if (home.is(h.tc().x(), h.tc().y())) {
					return path.requestFull(h.tc(), h.tc());
				}
				int sx = home.serviceX();
				int sy = home.serviceY();
				if (STATS.WORK().EMPLOYED.get(h) == null) {
					return path.requestFull(h.tc(), sx, sy);
				}
				current.set(sx, sy);
				
			}else {
				current.set(-1, -1);
			}
		}
		

		
		type = HGROUP.get(h);
		
		if (findP(h, path)) {
			HOME old = STATS.HOME().GETTER.get(h, this);
			HOME n = SETT.ROOMS().HOME.service.get(path.destX(), path.destY());
			if (old != null) {
				
				if (path.destX() == old.serviceX() && path.destY() == old.serviceY()) {
					return true;
				}else {
					STATS.HOME().GETTER.set(h, n);
				}
				
			}else {
				STATS.HOME().GETTER.set(h, n);
			}
			return true;
		}
		
		return false;
		
	}
	
	private boolean findP(Humanoid h, SPath path) {
		RoomInstance ins = STATS.WORK().EMPLOYED.get(h);
		if (ins == null) {
			if (h.indu().clas() == HCLASSES.NOBLE())
				return path.request(THRONE.coo().x(), THRONE.coo().y(), this, Integer.MAX_VALUE);
			else
				return path.request(h.tc().x(), h.tc().y(), this, Integer.MAX_VALUE);
		}
		COORDINATE c = SETT.PATH().finders.finder().findDest(ins, this, 200);
		if (c != null)
			return path.requestFull(h.tc(), c);
		
		return false;
	}
	

	@Override
	public boolean isInComponent(SComponent c, double distance) {
		if (SETT.PATH().comps.data.home.has(c, type))
			return true;
		if (c.is(current))
			return true;
		return false;
	}

	@Override
	public boolean isTile(int tx, int ty, int tileNr) {
		if (current.isSameAs(tx, ty))
			return true;
		
		HomeInstance home = SETT.ROOMS().HOME.service.get(tx, ty);
		if (home == null)
			return false;
		HTypeBits s = home.availability();
		if (s != null && s.is(type))
			return true;
		return false;
	}




}
