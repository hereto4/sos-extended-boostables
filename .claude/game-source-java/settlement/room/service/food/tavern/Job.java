package settlement.room.service.food.tavern;

import game.audio.SoundRace;
import init.resources.RBIT;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.ResGDrink;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.job.SETT_JOB;
import settlement.misc.util.FSERVICE;
import settlement.room.main.RoomInstance;
import settlement.room.main.util.RoomBits;
import snake2d.util.bit.Bits;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;

final class Job {

	private final Coo coo = new Coo();
	private TavernInstance ins;
	private final ROOM_TAVERN b;
	
	private final int iUNAVAILABLE = 0;
	private final int iAVAILABLE = 1;
	private final int iDIRTY = 2;
	private final int iRESERVED = 3;
	
	
	private final RoomBits sState = new RoomBits(coo, 		new Bits(0b0000_0000_1111)) {
		
		@Override
		public void set(RoomInstance r, int t) {
			if (get() == iAVAILABLE)
				ins.service.report(service, b.service(), -1);
			
			super.set(r, t);
			if (get() == iAVAILABLE) {
				ins.service.report(service, b.service(), 1);
			}
			if (get() != iDIRTY)
				sUsedDrinks.set(r, 0);
			
			sActiveDrink.set(r, 0);
			ins.jobs.searchAgain();
		};
		
	};
	private final RoomBits jreserved = new RoomBits(coo, new Bits(0b0000_0001_0000));

	public final RoomBits sUsedDrinks = new RoomBits(coo, 	new Bits(0b0000_0000_0000_0000_0000_1111_1110_0000));
	public final RoomBits sActiveDrink = new RoomBits(coo, 	new Bits(0b0000_0000_0000_1111_1111_0000_0000_0000));

	
	Job(ROOM_TAVERN b){
		this.b = b;
	}

	public int usedDrinks(int tx, int ty) {
		if (servive(tx, ty) != null) {
			return sUsedDrinks.get();
		}
		return 0;
	}
	
	public ResGDrink currentDrink(int tx, int ty) {
		if (servive(tx, ty) != null && sActiveDrink.get() > 0) {
			return RESOURCES.DRINKS().all().getC(sActiveDrink.get()-1);
		}
		return null;
	}
	
	public SETT_JOB job(int tx, int ty) {
		ins = b.getter.get(tx, ty);
		if (ins != null) {
			coo.set(tx, ty);
			if (SETT.ROOMS().fData.tileData.get(tx, ty) == Constructor.ISTORAGE) {
				return jobStore;
			}else if (SETT.ROOMS().fData.tileData.get(tx, ty) == Constructor.ITABLE) {
				return jobTable;
			}
		}
		return null;
	}
	
	public void fix(COORDINATE c) {
		if (job(c.x(), c.y()) != null) {
			if (jreserved.get() == 1 && sState.get() == iUNAVAILABLE)
				ins.tablesreserved += 1;
		}
	}
	
	public FSERVICE servive(int tx, int ty) {
		ins = b.getter.get(tx, ty);
		if (ins != null) {
			coo.set(tx, ty);
			if (SETT.ROOMS().fData.tileData.get(tx, ty) == Constructor.ITABLE) {
				return service;
			}
		}
		return null;
	}
	
	private final SETT_JOB jobStore = new SETT_JOB() {
		
		
		
		@Override
		public void jobReserve(RESOURCE r) {
			ResGDrink dd = RESOURCES.DRINKS().get(r);
			if (dd != null) {
				ins.incoming[dd.index()] += 8;
			}
		}

		@Override
		public boolean jobReservedIs(RESOURCE r) {
			return true;
		}

		@Override
		public void jobReserveCancel(RESOURCE r) {
			ResGDrink dd = RESOURCES.DRINKS().get(r);
			if (dd != null) {
				ins.incoming[dd.index()] -= 8;
				ins.incoming[dd.index()] = Math.max(ins.incoming[dd.index()], 0);
			}
		}

		@Override
		public RBIT jobResourceBitToFetch() {
			
			return ins.fetch;
		}
		
		@Override
		public int jobResourcesNeeded(Humanoid skill) {
			return SETT.ROOMS().STOCKPILE.carryCap(skill);
		};
		
		@Override
		public double jobPerformTime(Humanoid skill) {
			return 0;
		}
		
		@Override
		public boolean jobReserveCanBe() {
			return !ins.fetch.isClear();
		}
		
		@Override
		public RESOURCE jobPerform(Humanoid skill, RESOURCE res, int ram) {
			jobReserveCancel(res);
			ResGDrink dd = RESOURCES.DRINKS().get(res);
			if (dd != null) {
				ins.amountInc(dd, ram);
				ins.jobs.searchAgain();
				ins.setMasks();
			}
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
		public void jobStartPerforming() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean jobUseTool() {
			return false;
		}

		@Override
		public SoundRace jobSound() {
			return b.employment().sound();
		}


	};
	
	private final SETT_JOB jobTable = new SETT_JOB() {
		
		
		
		@Override
		public void jobReserve(RESOURCE r) {
			
			if (jreserved.get() == 0 && sState.get() == iUNAVAILABLE)
				ins.tablesreserved += 1;
			jreserved.set(ins, 1);
		}

		@Override
		public boolean jobReservedIs(RESOURCE r) {
			return jreserved.get() == 1;
		}

		@Override
		public void jobReserveCancel(RESOURCE r) {
			
			if (jreserved.get() == 1 && sState.get() == iUNAVAILABLE) {
				ins.tablesreserved -= 1;
			}
			jreserved.set(ins, 0);
		}

		@Override
		public RBIT jobResourceBitToFetch() {
			return null;
		}
		
		@Override
		public int jobResourcesNeeded(Humanoid skill) {
			return 0;
		};
		
		@Override
		public double jobPerformTime(Humanoid skill) {
			return 20;
		}
		
		@Override
		public boolean jobReserveCanBe() {
			if (jobReservedIs(null))
				return false;
			if (sState.get() == iDIRTY)
				return true;
			if (sState.get() == iUNAVAILABLE)
				return ins.amount(null)-2 > ins.service.reserved() + ins.service().available() + ins.tablesreserved;
			return false;
		}
		
		@Override
		public RESOURCE jobPerform(Humanoid skill, RESOURCE res, int ram) {
			jobReserveCancel(res);
			if (ins.amount(null)-2 > ins.service.reserved() + ins.service().available() + ins.tablesreserved) {
				sState.set(ins, iAVAILABLE);
			}else {
				sState.set(ins, iUNAVAILABLE);
			}

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
		public void jobStartPerforming() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean jobUseTool() {
			return false;
		}

		@Override
		public SoundRace jobSound() {
			return b.employment().sound();
		}


	};
	

	
	private final FSERVICE service = new FSERVICE() {
		
		@Override
		public int y() {
			return coo.y();
		}
		
		@Override
		public int x() {
			return coo.x();
		}
		
		@Override
		public boolean findableReservedIs() {
			return sState.get() == iRESERVED;
		}
		
		@Override
		public boolean findableReservedCanBe() {
			return sState.get() == iAVAILABLE;
		}
		
		@Override
		public void findableReserveCancel() {
			sState.set(ins, iAVAILABLE);
		}
		
		@Override
		public void findableReserve() {
			sState.set(ins, iRESERVED);
		}
		
		@Override
		public void consume() {
			sState.set(ins, iDIRTY);
		}
	};



	
}
