package settlement.room.service.hygine.bath;

import static settlement.main.SETT.ROOMS;
import static settlement.room.service.hygine.bath.Bits.BITS;
import static settlement.room.service.hygine.bath.Bits.RESERVED;
import static settlement.room.service.hygine.bath.Bits.SERVICE;

import game.audio.SoundRace;
import init.resources.RBIT;
import init.resources.RESOURCE;
import settlement.entity.humanoid.Humanoid;
import settlement.misc.job.SETT_JOB;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;

public class Crank implements SETT_JOB{

	static final int BIT = Bits.CRANK;
	private static final Crank self = new Crank();
	static final int WORKING =	RESERVED >> 1;
	Bath bath;
	int data;
	final Coo coo = new Coo();
	BathInstance ins;
	
	static Crank init(int tx, int ty, ROOM_BATH b) {
		if (!b.is(tx, ty))
			return null;
		
		BathInstance ins = b.getter.get(tx, ty);
		
		int data = ROOMS().data.get(tx, ty);
		if ((data & BITS) != BIT)
			return null;
		for (DIR d : DIR.ORTHO) {
			if (ins.is(tx, ty, d) && (ROOMS().data.get(tx, ty, d) & BITS) == SERVICE) {
				self.bath = b.bath(tx+d.x(), ty+d.y());
				self.data = data;
				self.coo.set(tx, ty);
				self.ins = b.get(tx, ty);
				return self;
			}
		}
		throw new RuntimeException();
	}
	
	private Crank() {
		
	}

	@Override
	public boolean jobUseTool() {
		return false;
	}
	
	private void save() {
		ROOMS().data.set(ins, coo.x(), coo.y(), data);
	}
	
	@Override
	public void jobStartPerforming() {
		data |= WORKING;
		save();
	}
	
	@Override
	public SoundRace jobSound() {
		return ins.blueprintI().employment().sound();
	}
	
	@Override
	public RBIT jobResourceBitToFetch() {
		return null;
	}
	
	@Override
	public boolean jobReservedIs(RESOURCE r) {
		return (data & RESERVED) != 0;
	}
	
	@Override
	public void jobReserveCancel(RESOURCE r) {
		data &= ~RESERVED;
		data &= ~WORKING;
		save();
	}
	
	@Override
	public boolean jobReserveCanBe() {
		return bath.availbilityNeeds() && !jobReservedIs(null);
	}
	
	@Override
	public void jobReserve(RESOURCE r) {
		if (jobReservedIs(null))
			throw new RuntimeException();
		data |= RESERVED;
		save();
	}
	
	@Override
	public double jobPerformTime(Humanoid skill) {
		return 20;
	}
	
	@Override
	public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int ram) {
		bath.availabilityInc();
		jobReserveCancel(null);
		return null;
	}
	
	final String name = "pumping water";
	
	@Override
	public String jobName() {
		return name;
	}
	
	@Override
	public COORDINATE jobCoo() {
		return coo;
	}
	
}
