package settlement.room.industry.refiner;

import static settlement.main.SETT.ROOMS;

import game.audio.SoundRace;
import init.resources.RBIT;
import init.resources.RESOURCE;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.job.SETT_JOB;
import settlement.room.industry.module.IndustryResource;
import settlement.room.industry.module.ROOM_PRODUCER_INSTANCE;
import settlement.room.main.job.RoomResDeposit;
import settlement.room.main.job.RoomResStorage;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;

class Job{


	final Work WORK = new Work();
	final RoomResDeposit FETCH;
	
	final RoomResStorage storage;
	
	Job(ROOM_REFINER print, int store) {
		
		storage = new RoomResStorage(store) {
			
			@Override
			public RESOURCE resource() {
				ROOM_PRODUCER_INSTANCE ins = (ROOM_PRODUCER_INSTANCE) SETT.ROOMS().map.get(this);
				return ins.industry().outs().get(0).resource;
			}

			@Override
			protected boolean is(int tx, int ty) {
				return SETT.ROOMS().fData.tileData.get(tx, ty) == Constructor.B_STORAGE;
			}
			
			@Override
			protected void changed(int tx, int ty) {
				if (hasRoom()) {
					RefinerInstance m = print.get(tx, ty);
					m.hasStorage = true;
					m.jobs.searchAgain();
				}
					
			}
			
		};
		FETCH = new RoomResDeposit(print) {
			
			@Override
			protected boolean is(int tx, int ty) {
				return SETT.ROOMS().fData.tileData.get(tx, ty) == Constructor.B_FETCH;
			}

			@Override
			protected void hasCallback() {
				// TODO Auto-generated method stub
				
			}

			@Override
			protected boolean regularJobCanBeReserved(COORDINATE coo) {
				RefinerInstance ins = print.get(coo.x(), coo.y());
				return ins.hasStorage;
			}

			@Override
			protected void regularJobStore(COORDINATE coo, int am) {
				RefinerInstance ins = print.get(coo.x(), coo.y());
				int x1 = ins.sx;
				int y1 = ins.sy;
				RoomResStorage ss = storage.get(x1, y1, ins);
				
				while(ss != null && am > 0) {
					if (ss.hasRoom()) {
						ss.deposit();
						am--;
						continue;
					}
					
					RoomResStorage sss = storage.get(ss.x()+1, ss.y(), ins);
					if (sss == null)
						sss = storage.get(x1, ss.y()+1, ins);
					ss = sss;
				}
				if (am > 0)
					ins.hasStorage = false;
				
			}

		};
	}
	
	
	SETT_JOB init(int tx, int ty, RefinerInstance ins) {
		if (!ins.is(tx, ty))
			return null;
		if (SETT.ROOMS().fData.tileData.is(tx, ty, Constructor.B_WORK))
			return WORK.init(tx, ty, ins);
		return FETCH.get(tx, ty, ins);
	}
	
	
	static DIR find(COORDINATE coo, int data, RefinerInstance ins) {
		for (DIR d : DIR.ORTHO) {
			if (ins.is(coo, d) && SETT.ROOMS().fData.tileData.get(coo, d) == data)
				return d;
		}
		throw new RuntimeException();
	}
	
	static DIR find(int tx, int ty, int data, RefinerInstance ins) {
		for (DIR d : DIR.ORTHO) {
			if (ins.is(tx, ty, d) && SETT.ROOMS().fData.tileData.get(tx, ty, d) == data)
				return d;
		}
		
		throw new RuntimeException();
	}

	

	
	final class Work implements SETT_JOB{
		
		private static final int BITRESERVED = 	0b1000000000000000;
		private final static int BIT_STORAGE_FULL =	BITRESERVED >> 1;
		private final static int BIT_HAS_RAW =	BIT_STORAGE_FULL >> 1;
		private final static int IS_WORKING = 	BIT_HAS_RAW >> 1;
		
		
		private final Coo coo = new Coo();
		RefinerInstance ins;
		int data;
		final static String name = "working";
		
		Work(){
		
		}
		
		@Override
		public boolean jobReserveCanBe() {
			if (jobReservedIs(null))
				return false;
			if (!ins.hasStorage)
				return false;
			DIR d = find(coo.x(), coo.y(), Constructor.B_FETCH, ins);
			if (!FETCH.get(coo.x()+d.x(), coo.y()+d.y(), ins).hasOneOfEach())
				return false;
			

			return true;
		}
		
		Work init(int tx, int ty, RefinerInstance ins) {
			data = ROOMS().data.get(tx, ty);
			coo.set(tx, ty);
			this.ins = ins;
			return this;
		}
		
		void save() {
			int d = ROOMS().data.get(coo);
			if ((d & IS_WORKING) == 1) {
				ins.WI--;
			}
			if ((data & IS_WORKING) == 1) {
				ins.WI++;
			}
			ROOMS().data.set(ins, coo, data);
			
		}

		@Override
		public COORDINATE jobCoo() {
			return coo;
		}

		@Override
		public String jobName() {
			return name;
		}

		@Override
		public boolean jobUseTool() {
			return false;
		}
		

		
		boolean working(int data) {
			return (data & IS_WORKING) != 0;
		}

		@Override
		public RBIT jobResourceBitToFetch() {
			return null;
		}

		@Override
		public double jobPerformTime(Humanoid skill) {
			return wv;
		}

		@Override
		public void jobReserve(RESOURCE r) {
			if (jobReservedIs(null))
				throw new RuntimeException();
			data |= BITRESERVED;
			save();
			DIR d = find(coo.x(), coo.y(),  Constructor.B_FETCH, ins);
			FETCH.get(coo.x()+d.x(), coo.y()+d.y(), ins).withdrawOneOfEach();
			
			
		}

		@Override
		public boolean jobReservedIs(RESOURCE r) {
			return (data & BITRESERVED) == BITRESERVED;
		}
		
		@Override
		public void jobReserveCancel(RESOURCE r) {
			data &= ~BITRESERVED;
			data &= ~IS_WORKING;
			save();
			DIR d = find(coo.x(), coo.y(),  Constructor.B_FETCH, ins);
			FETCH.get(coo.x()+d.x(), coo.y()+d.y(), ins).depositOneOfEach();
			ins.jobs.searchAgainButDontReset();
		}
		
		@Override
		public void jobStartPerforming() {
			data |= IS_WORKING;
			save();
		}

		@Override
		public SoundRace jobSound() {
			return ins.blueprintI().employment().sound();
		}

		private final int wv = 45;
		
		@Override
		public RESOURCE jobPerform(Humanoid s, RESOURCE res, int ram) {
			
			
			
			double w = ins.employees().fetchBonus(wv);
		
			DIR d = find(coo,  Constructor.B_FETCH, ins);
			RoomResDeposit dep = FETCH.get(coo.x()+d.x(), coo.y()+d.y(), ins);
			FETCH.get(coo.x()+d.x(), coo.y()+d.y(), ins).depositOneOfEach();
			if (!ins.hasStorage) {
				data &= ~BITRESERVED;
				data &= ~IS_WORKING;
				save();
				return null;
			}
				
			
			int ri = 0;
			for (IndustryResource r : ins.industry().ins()) {
				int a = r.work(s, ins, w);
				if (a > 0) {
					int dd = dep.amount(ri);
					if (dd < a) {
						double www = w*dd/a;
						if (www < w) {
							w = www;
						}
						r.inc(ins, -(a-dd));
						a = dd;
					}
					dep.withDraw(ri, a);
				}
				ri++;
			} 
			
			int am = ins.industry().outs().get(0).work(s, ins, w);
			
			int x1 = ins.sx;
			int y1 = ins.sy;
			RoomResStorage ss = storage.get(x1, y1, ins);
			
			while(ss != null && am > 0) {
				if (ss.hasRoom()) {
					ss.deposit();
					am--;
					continue;
				}
				
				RoomResStorage sss = storage.get(ss.x()+1, ss.y(), ins);
				if (sss == null)
					sss = storage.get(x1, ss.y()+1, ins);
				ss = sss;
			}
			if (am > 0)
				ins.hasStorage = false;
			
			data &= ~BITRESERVED;
			data &= ~IS_WORKING;
			
			save();
			return null;
		}
		
	}
	
}
