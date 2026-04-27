package settlement.room.infra.embassy;

import static settlement.main.SETT.ROOMS;

import game.audio.SoundRace;
import init.resources.RBIT;
import init.resources.RESOURCE;
import init.resources.RBIT.RBITImp;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.job.SETT_JOB;
import settlement.room.industry.module.IndustryResource;
import settlement.room.industry.module.IndustryUtil;
import settlement.room.industry.module.consumption.RoomConsumption;
import settlement.room.main.util.RoomBits;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;

class Job implements SETT_JOB{

	private final Coo coo = new Coo();
	private final RoomBits reserved = new RoomBits(coo, 0b001);
	private EmbassyInstance ins;
	static final double time = 45;
	
	private final ROOM_EMBASSY b;
	private final RoomConsumption bluec;
	Job(ROOM_EMBASSY b, RoomConsumption bluec){
		this.b = b;
		this.bluec = bluec;
	}
	
	SETT_JOB get(int tx, int ty) {
		ins = b.get(tx, ty);
		if (ins == null)
			return null;
		if (ROOMS().fData.tileData.get(tx, ty) == Constructor.IWORK) {
			coo.set(tx, ty);
			return this;
		}
		return null;
		
	}

	@Override
	public void jobReserve(RESOURCE r) {
		
		if (reserved.get() == 1) {
			throw new RuntimeException();
		}
		reserved.set(ins, 1);
		
		if (r != null) {
			IndustryResource rr = bluec.in(r);
			if (rr == null)
				throw new RuntimeException();
			reserved.set(ins, 1);
			bluec.reseved(rr).inc(ins,1);
		}
		
	}

	@Override
	public boolean jobReservedIs(RESOURCE r) {
		return reserved.get() == 1;
	}

	@Override
	public void jobReserveCancel(RESOURCE r) {
		reserved.set(ins, 0);
		
		
		if (r == null)
			return;
		IndustryResource rr = bluec.in(r);
		if (rr == null)
			return;
		bluec.reseved(rr).inc(ins,-1);
	}
	
	@Override
	public boolean jobReserveCanBe() {
		if (jobReservedIs(null))
			return false;

		
		return true;
	}

	private final RBITImp resBit = new RBITImp();
	@Override
	public RBIT jobResourceBitToFetch() {
		resBit.clear();
		for (IndustryResource in : bluec.ins()) {
			if (bluec.enabled(in, ins) && ins.getWork().resourceShouldSearch(in.resource) && bluec.stored(in).get(ins) + bluec.reseved(in).get(ins) < b.maxRes(in.index(), ins)) {
				resBit.or(in.resource.bit);
			}
				
		}
		return resBit.isClear() ? null : resBit;
	}

	@Override
	public double jobPerformTime(Humanoid skill) {
		return time;
	}

	@Override
	public int jobResourcesNeeded(Humanoid skill) {
		return 4;
	}
	
	@Override
	public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int ram) {
		
		jobReserveCancel(r);
		
		if (r != null) {
			IndustryResource rr = bluec.in(r);
			if (rr == null)
				return null;
			if (bluec.enabled(rr, ins))
				bluec.stored(rr).inc(ins, ram);
			else
				SETT.THINGS().resources.create(skill.tc(), r, ram);
			return null;
		}
		
		
		double sk = IndustryUtil.calcProductionRate(1.0, skill, bluec, ins);
		ins.skill += sk;
		ins.skillI ++;
		
		return null;
	}
	
	@Override
	public COORDINATE jobCoo() {
		return coo;
	}

	@Override
	public CharSequence jobName() {
		return b.employment().verb;
	}

	@Override
	public boolean jobUseTool() {
		return false;
	}

	@Override
	public SoundRace jobSound() {
		return b.employment().sound();
	}

	@Override
	public void jobStartPerforming() {
		// TODO Auto-generated method stub
		
	}
	
	
}
