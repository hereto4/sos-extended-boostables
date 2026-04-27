package settlement.room.service.market;

import game.audio.SoundRace;
import init.race.RACES;
import init.race.RaceResources.RaceResource;
import init.resources.RBIT;
import init.resources.RESOURCE;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.job.SETT_JOB;
import settlement.misc.util.FSERVICE;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import view.sett.SettDebugClick;

final class Crate {

	private final ROOM_MARKET e;
	private final Job job = new Job();
	private final Service service = new Service();
	
	Crate(ROOM_MARKET e){
		this.e = e;
	}
	
	public SETT_JOB job(int tx, int ty) {
		MarketInstance i = e.getter.get(tx, ty);
		if (i == null)
			return null;
		if (!e.constructor.isCrate(tx, ty))
			return null;
		job.ins = i;
		job.coo.set(tx, ty);
		return job;
	}
	
	public Service service(int tx, int ty) {
		MarketInstance i = e.getter.get(tx, ty);
		if (i == null)
			return null;
		if (!e.constructor.isCrate(tx, ty))
			return null;
		service.ins = i;
		service.coo.set(tx, ty);
		service.state = SETT.ROOMS().data.get(tx, ty);
		return service;
	}
	
	public void dispose(int tx, int ty) {
		FSERVICE f = service(tx, ty);
		if (f != null)
			service.dispose();
	}
	
	public void init(int tx, int ty) {
		FSERVICE f = service(tx, ty);
		if (f != null)
			service.init();
	}
	
	private class Job implements SETT_JOB {

		private MarketInstance ins;
		private final Coo coo = new Coo();
		
		{
			new SettDebugClick() {

				@Override
				public boolean debug(int px, int py, int tx, int ty) {
					LOG.ln("here!");
					if (job(tx, ty) != null) {
						RBIT r = ins.fetchMask();
						LOG.ln(jobReserveCanBe());
						for (RaceResource e : RACES.res().ALL) {
							LOG.ln(e.res.name + " " +  (e.res.bit.has(r)) + " " + ins.uses(e));
						}
						return true;
					}
					return false;
				}
				
			};
		}
		
		@Override
		public boolean jobUseTool() {
			return false;
		}
		
		@Override
		public void jobStartPerforming() {
			
		}
		
		@Override
		public SoundRace jobSound() {
			return e.employment().sound();
		}
		
		@Override
		public RBIT jobResourceBitToFetch() {
			return ins.fetchMask();
		}
		
		@Override
		public boolean longFetch() {
			return true;
		}
		
		@Override
		public int jobResourcesNeeded(Humanoid skill) {
			return SETT.ROOMS().STOCKPILE.carryCap(skill);
		}
		
		@Override
		public boolean jobReservedIs(RESOURCE r) {
			if (r == null)
				return false;
			RaceResource e = RACES.res().get(r);
			if (e == null)
				return false;
			return ins.jobReserved(e) > 0;
		}
		
		@Override
		public void jobReserveCancel(RESOURCE r) {
			RaceResource e = RACES.res().get(r);
			if (e == null)
				return;
			if (ins.jobReserved(e) > 0)
				ins.jobTally(e, -1, 0);
		}
		
		@Override
		public boolean jobReserveCanBe() {
			return !ins.fetchMask().isClear();
		}
		
		@Override
		public void jobReserve(RESOURCE r) {
			RaceResource e = RACES.res().get(r);
			ins.jobTally(e, 1, 0);
		}
		
		@Override
		public double jobPerformTime(Humanoid skill) {
			return 0;
		}
		
		@Override
		public RESOURCE jobPerform(Humanoid skill, RESOURCE r, int ram) {
			RaceResource e = RACES.res().get(r);
			if (e != null && ins.jobReserved(e) > 0) {
				ins.jobTally(e, -1, ram);
			}
			return null;
		}
		
		@Override
		public CharSequence jobName() {
			return e.employment().verb;
		}
		
		@Override
		public COORDINATE jobCoo() {
			return coo;
		}
	};
	
	class Service implements FSERVICE {

		private MarketInstance ins;
		private final Coo coo = new Coo();
		private int state;
		private final int S_RESERVABLE = 1;
		private final int S_RESERVED = 2;
		
		void init() {
			state = S_RESERVABLE;
			save();
		}
		
		private void save() {
			int tmp = state;
			state = SETT.ROOMS().data.get(coo);
			if (tmp == state)
				return;
			if (state == S_RESERVABLE) {
				ins.service.report(this, e.service, -1);

			}
			state = tmp;
			if (state == S_RESERVABLE) {
				ins.service.report(this, e.service, 1);
			}
			SETT.ROOMS().data.set(ins, coo, state);
		}
		
		void dispose() {
			state = 0;
			save();
		}
		
		@Override
		public boolean findableReservedCanBe() {
			return state == S_RESERVABLE;
		}

		@Override
		public void findableReserve() {
			state = S_RESERVED;
			save();
		}

		@Override
		public boolean findableReservedIs() {
			return state == S_RESERVED;
		}

		@Override
		public void findableReserveCancel() {
			state = S_RESERVABLE;
			save();
			
		}

		@Override
		public int x() {
			return coo.x();
		}

		@Override
		public int y() {
			return coo.y();
		}

		@Override
		public void consume() {
			findableReserveCancel();
		}
		
	}
	
}
