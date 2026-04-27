package settlement.room.service.nursery;

import init.type.CAUSE_ARRIVES;
import init.type.HTYPES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.industry.module.IndustryResource;
import settlement.room.industry.module.IndustryUtil;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

final class Util {

	private final Station ss;
	private final Coo spot = new Coo();
	
	Util(Station station){
		ss = station;
	}
	
	void updateDay(int tx, int ty) {
		if (!ss.init(tx, ty))
			return;
		
		
		if (ss.usedByKid.get() != 0)
			return;


		if (ss.amount.get() != ss.amount.max() || ss.job.jobResourceBitToFetch() != null) {
			ss.age.inc(-1);
		}else {
			if (ss.age.get() >= ss.b.BABY_DAYS) {
				DIR d = spawnDir();
				Humanoid h = SETT.HUMANOIDS().create(ss.b.race,tx+d.x(), ty+d.y(), HTYPES.CHILD(), CAUSE_ARRIVES.BORN());
				if (h != null) {
					STATS.POP().age.DAYS.set(h.indu(), ss.b.BABY_DAYS);
					STATS.POP().TYPE.NATIVE.set(h.indu());
				}
				ss.init(tx, ty);
				ss.age.set(0);
			}else {
				double prog = IndustryUtil.roomBonus(ss.ins, ss.b.productionData);
				while(prog >= 1) {
					ss.age.inc(1);
					prog -= 1;
				}
				if (RND.rFloat() <= prog)
					ss.age.inc(1);
			}
		}
		
		int i = 0;
		for (IndustryResource ins : ss.b.productionData.ins()) {
			if (ss.resources[i].get() > 0) {
				if (ss.age.get() == 0) {
					ins.inc(ss.ins, ss.resources[i].get());
					ss.resources[i].set(0);
				}
				else if (ins.rate > RND.rFloat()) {
					ss.resources[i].inc(-1);
					ins.inc(ss.ins, 1);
				}
			}
			i++;
		}
		
		
		
		ss.ins.getWork().searchAgainButDontReset();
		ss.amount.set(0);
	}
	
	private DIR spawnDir() {
		for (DIR d : DIR.ORTHO) {
			if (ss.ins.is(ss.coo, d) && SETT.PATH().availability.get(ss.coo, d).player > 0 && SETT.PATH().availability.get(ss.coo, d).player < AVAILABILITY.Penalty)
				return d;
		}
		throw new RuntimeException("A nursery has been weirdly furnished. Please provide screenshots of the nursery at tile: " + ss.coo + " For the development team!" );
	}
	
	void dispose(int tx, int ty) {
		if (!ss.init(tx, ty))
			return;
		for (IndustryResource r : ss.b.productionData.ins()) {
			if (ss.resources[r.index()].get() > 0)
				SETT.THINGS().resources.create(tx, ty, r.resource, ss.resources[r.index()].get());
		}
		ss.count(-1);
	}
	
	void cancelChildSpot(int tx, int ty) {
		if (ss.init(tx, ty)) {
			ss.age.set(0);
			ss.amount.set(0);
			ss.usedByKid.set(0);
		}
	}
	
	boolean useChildSpot(int tx, int ty, boolean eat) {
		
		if (ss.init(tx, ty)) {
			ss.amount.set(0);
			if (eat && ss.amount.get() != ss.amount.max()) {
				int ri = RND.rInt(ss.b.productionData.ins().size());
				for (int i = 0; i < ss.b.productionData.ins().size(); i++) {
					int ii = (i + ri)% ss.b.productionData.ins().size();
					if (ss.resources[ii].get() > 0) {
						ss.resources[ii].inc(-1);
						ss.b.productionData.ins().get(ii).inc(ss.ins, 1);
						return true;
					}
				}
				return false;
			}
			ss.ins.getWork().searchAgainButDontReset();
		}
		return false;
	}
	
	DIR getSleepDir(int tx, int ty) {
		if (ss.init(tx, ty))
			for (DIR d : DIR.ORTHO) {
				if (ss.b.constructor.isHead(ss.coo, d))
					return d;
			}
		return null;
	}
	
	COORDINATE getAndReserveChildSpot(int tx, int ty) {
		NurseryInstance ins = getChildSpotRoom(tx, ty);
		if (ins == null)
			return null;
		COORDINATE c = getChildSpotRoom(ins, tx, ty);
		ss.init(c.x(), c.y());
		ss.usedByKid.set(1);
		return c;
	}
	
	private NurseryInstance getChildSpotRoom(int tx, int ty) {
		NurseryInstance ins = ss.b.get(tx, ty);
		if (ins != null && ins.active() && ins.kidspotsUsed < ins.getWork().size()) {
			return ins;
		}
		if (ss.b.kidSpotsUsed >= ss.b.kidSpotsTotal) {
			return null;
		}
		
		int si = RND.rInt(ss.b.instancesSize());
		
		for (int di = 0; di < ss.b.instancesSize(); di++) {
			ins = ss.b.getInstance((di+si)%ss.b.instancesSize());
			if (ins != null && ins.active() && ins.kidspotsUsed < ins.getWork().size()) {
				return ins;
			}
		}
		throw new RuntimeException();
	}
	
	private COORDINATE getChildSpotRoom(NurseryInstance ins, int tx, int ty) {
		DIR best = null;
		for (DIR d : DIR.ALLC) {
			if (!ss.init(tx+d.x(), ty+d.y()))
				continue;
			if (ss.usedByKid.get() == 1)
				continue;
			int age = ss.age.get();
			if (age == 0) {
				spot.set(tx+d.x(), ty+d.y());
				return spot;
			}
			if (best == null)
				best = d;
			else {
				ss.init(tx+best.x(), ty+best.y());
				if (age < ss.age.get())
					best = d;
			}
		}
		if (best != null) {
			spot.set(tx+best.x(), ty+best.y());
			return spot;
		}
		
		spot.set(-1, -1);
		
		for (COORDINATE c : ins.body()) {
			if (!ss.init(c.x(), c.y()))
				continue;
			if (ss.usedByKid.get() == 1)
				continue;
			int age = ss.age.get();
			if (age == 0) {
				spot.set(c);
				return spot;
			}
			if (spot.x() == -1)
				spot.set(c);
			else {
				ss.init(spot.x(), spot.y());
				if (age < ss.age.get())
					spot.set(c);
			}
		}
		
		if (spot.x() != -1)
			return spot;
		
		throw new RuntimeException();
	}
	
}
