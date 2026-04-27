package settlement.room.infra.admin;

import static settlement.main.SETT.ROOMS;
import static settlement.main.SETT.THINGS;

import game.audio.SoundRace;
import init.resources.RBIT;
import init.resources.RESOURCE;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.job.SETT_JOB;
import settlement.room.industry.module.IndustryUtil;
import snake2d.util.bit.Bit;
import snake2d.util.bit.Bits;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

class Job implements SETT_JOB{

	final static Bit bit = new Bit				(0b0000000000001);
	final static Bit reserved = new Bit			(0b0000000000010);
	final static Bits used = new Bits			(0b0000000111100);
	final static Bits ran = new Bits			(0b0000000111110);
	final static Bits paper = new Bits			(0b0001111000000);
	final static Bits paperR = new Bits			(0b0010000000000);
	private int data;
	private final Coo coo = new Coo();
	private AdminInstance ins;
	static final int time = 45;
	
	private final ROOM_ADMIN b;
	Job(ROOM_ADMIN b){
		this.b = b;
	}
	
	SETT_JOB get(int tx, int ty) {
		ins = b.get(tx, ty);
		if (ins == null)
			return null;
		data = ROOMS().data.get(tx, ty);
		if (bit.is(data)) {
			coo.set(tx, ty);
			return this;
		}
		return null;
		
	}

	@Override
	public void jobReserve(RESOURCE r) {
		if (reserved.is(data)) {
			throw new RuntimeException();
		}
		data = reserved.set(data);
		data = paperR.set(data, r == b.industry.ins().get(0).resource ? 1 : 0);
		save();
	}

	@Override
	public boolean jobReservedIs(RESOURCE r) {
		return reserved.is(data);
	}

	@Override
	public void jobReserveCancel(RESOURCE r) {
		data = reserved.clear(data);
		data = used.set(data, 0);
		data = paperR.set(data, 0);
		save();
	}

	@Override
	public DIR jobStandDir() {
		for (DIR d : DIR.ORTHO) {
			if (ins.is(coo, d) && SETT.ROOMS().fData.tileData.get(coo, d) == Constructor.ICHAIR)
				return d;
		}
		return null;
	}
	
	@Override
	public boolean jobReserveCanBe() {
		return !jobReservedIs(null);
	}

	@Override
	public RBIT jobResourceBitToFetch() {
		if (paper.get(data) < 1)
			return ins.blueprintI().industry.ins().get(0).resource.bit;
		return null;
	}

	@Override
	public double jobPerformTime(Humanoid skill) {
		return time;
	}

	@Override
	public void jobStartPerforming() {
		data = used.set(data, 1 + RND.rInt(7));
		save();
	}
	
	@Override
	public int jobResourcesNeeded(Humanoid skill) {
		return paper.mask - paper.get(data);
	}
	
	@Override
	public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int ram) {
		if (r != null) {
			data = paper.inc(data, ram);
			jobReserveCancel(null);
			return null;
		}
		
		int t = ins.employees().fetchBonus(time);
		
		double sk = IndustryUtil.calcProductionRate(1.0, skill, b.industry, ins);
		
		b.data.perform(t, sk);
		
		if (paper.get(data) != 0) {
			int am = ins.blueprintI().industry.ins().get(0).work(skill, ins, t);
			if (am > 0) {
				data = paper.inc(data, -am);
			}
		}
		jobReserveCancel(null);
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
	
	private void save() {
		int o = data;
		data = ROOMS().data.get(coo);
		if ((paper.get(data) > 0))
			ins.paper --;
		data = o;
		ROOMS().data.set(ins, coo, data);
		if ((paper.get(data) > 0))
			ins.paper ++;
	}

	public void dispose() {
		int am = paper.get(data);
		if (am > 0) {
			THINGS().resources.create(coo, ins.blueprintI().industry.ins().get(0).resource, am);
		}
	}
	
	
}
