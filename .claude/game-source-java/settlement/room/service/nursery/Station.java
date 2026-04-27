package settlement.room.service.nursery;

import static settlement.main.SETT.ROOMS;

import game.audio.SoundRace;
import game.time.TIME;
import init.resources.RBIT;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.job.SETT_JOB;
import settlement.room.industry.module.IndustryResource;
import snake2d.util.bit.Bits;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import util.data.INT.INTE;

class Station {

	private int data;
	final Coo coo = new Coo();
	NurseryInstance ins;
	final ROOM_NURSERY b;

	final INTE[] resources = new INTE[]{
		new BBit(new Bits(0b0000_0000_0111)),
		new BBit(new Bits(0b0000_0011_1000)),
		new BBit(new Bits(0b0001_1100_0000)),
		new BBit(new Bits(0b1110_0000_0000)),
		
	};
	
	final INTE reserved = 		new BBit(new Bits(	0b0000_0000_0000_0000_0001_0000_0000));
	final INTE amount = 		new BBit(new Bits(	0b0000_0000_0000_0000_1110_0000_0000));
	final INTE age = 			new BBit(new Bits(	0b0000_0000_1111_1111_0000_0000_0000));
	final Bits all = 					new Bits(	0b0000_0000_1111_1111_1110_1111_1111);
	final INTE usedByKid = 		new BBit(new Bits(	0b0000_0001_0000_0000_0000_0000_0000));
	
	Station(ROOM_NURSERY b){
		this.b = b;
	}
	
	boolean init(int tx, int ty) {
		if (b.is(tx, ty) && SETT.ROOMS().fData.tileData.get(tx, ty) == NurseryConstructor.MARK) {
			this.data = ROOMS().data.get(tx, ty);
			this.coo.set(tx, ty);
			this.ins = b.get(tx, ty);
			return true;
		}
		return false;
	}
	
	private void save() {
		int current = data;
		data = ROOMS().data.get(coo);
		count(-1);
		data = current;
		count(1);
		ROOMS().data.set(ins, coo, data);
	}
	
	void count(int delta) {

		if (usedByKid.get() == 1 ) {
			ins.kidspotsUsed += delta;
			b.kidSpotsUsed += delta;
		}else if(all.get(data) > 0) {
			b.babies += delta;
			ins.infantspotsUsed += delta;
		}
	}
	
	private final RBITImp bits = new RBITImp();
	
	private RBIT resMask() {
		bits.clear();
		for (IndustryResource r : b.productionData.ins()) {
			if (resources[r.index()].get() == 0)
				bits.or(r.resource);
		}
		return bits;
	}
	
	final SETT_JOB job = new SETT_JOB() {
		
		private final int wt = (int) (TIME.workSeconds()/(amount.max()*8));
		
		@Override
		public boolean jobUseTool() {
			return false;
		}
		
		@Override
		public void jobStartPerforming() {
			
		}
		
		@Override
		public SoundRace jobSound() {
			return b.employment().sound();
		}
		
		@Override
		public RBIT jobResourceBitToFetch() {
			RBIT rr = resMask();
			if (rr.isClear())
				return null;
			return rr;
		}

		@Override
		public int jobResourcesNeeded(Humanoid skill) {
			return 7;
		};
		
		@Override
		public boolean jobReservedIs(RESOURCE r) {
			return reserved.get() == 1;
		}
		
		@Override
		public void jobReserveCancel(RESOURCE r) {
			reserved.set(0);
		}
		
		@Override
		public boolean jobReserveCanBe() {
			if (reserved.get() == 1)
				return false;
			
			if (age.get() == 0 && usedByKid.get() == 0) {
				if (ins.searchFirst)
					return false;
				if (!b.canWorkNewBed(ins))
					return false;
			}
			
			return amount.get() < amount.max() || !resMask().isClear();
		}
		
		@Override
		public void jobReserve(RESOURCE r) {
			reserved.set(1);
		}
		
		@Override
		public double jobPerformTime(Humanoid skill) {
			return wt;
		}
		
		@Override
		public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int am) {
			if (r == null) {
				amount.inc(1);
			}else {
				for (IndustryResource rr : b.productionData.ins()) {
					if (rr.resource == r) {
						resources[rr.index()].inc(am);
						
						break;
					}
					
				}
			}
			jobReserveCancel(null);
			return null;
		}
		
		@Override
		public CharSequence jobName() {
			return b.employment().verb;
		}
		
		@Override
		public COORDINATE jobCoo() {
			return coo;
		}
	};
	
	private class BBit implements INTE {

		private final Bits bits;
		
		BBit(Bits bits){
			this.bits = bits;
		}
		
		@Override
		public int get() {
			return bits.get(data);
		}

		@Override
		public int min() {
			return 0;
		}

		@Override
		public int max() {
			return bits.mask;
		}

		@Override
		public void set(int t) {
			data = bits.set(data, t);
			save();
		}
		
		
	}
	
}
