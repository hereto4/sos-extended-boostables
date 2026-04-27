package settlement.entity.humanoid.ai.types.recruit;

import init.constant.C;
import init.type.HTYPES;
import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISTATE;
import settlement.entity.humanoid.ai.main.AISUB;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.misc.job.SETT_JOB;
import settlement.room.main.RoomInstance;
import settlement.room.military.training.archery.ROOM_ARCHERY;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;
import snake2d.util.sprite.text.Str;

final class PlanRange extends AIPLAN.PLANRES{

	private final AIModule_Recruit module;
	
	PlanRange(AIModule_Recruit module){
		super("recRange");
		this.module = module;
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		return inits.set(a, d);
	}
	
	private final Resumer done = new Res() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			if (job(a, d) != null)
				job(a, d).jobReserveCancel(null);
			return AI.SUBS().STAND.activate(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			a.HTypeSet(HTYPES.SUBJECT(), null, null);
			return d.resumeOtherPlan(a, AI.plans().NOP);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	

	
	final Resumer inits = new Res() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			JOBMANAGER_HASER w = work(a);
			
			if (w == null)
				return done.set(a, d);
			
			SETT_JOB j = w.getWork().getReservableJob(a.tc());
			
			if (j == null)
				return done.set(a, d);
			
			d.planTile.set(j.jobCoo());
			AISubActivation s = AI.SUBS().walkTo.cooFull(a, d, j.jobCoo());
			
			if (s != null) {
				j = w.getWork().getJob(d.planTile);;
				j.jobReserve(null);
				return s;
			}
			
			return done.set(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (job(a, d) == null)
				return null;
			DIR dir = blue(a).faceCoo(d.planTile.x(), d.planTile.y());
			a.speed.setDirCurrent(dir);
			a.speed.magnitudeTargetSet(0);
			a.speed.magnitudeInit(0);
			return work.set(a, d);
		}
		

	};

	
	@Override
	public double poll(Humanoid a, AIManager d, HPollData e) {
		if (e.type == HPoll.WORKING)
			return 1.0;
		return super.poll(a, d, e);
	}
	

	
	final Resumer work = new Res() {
		
		private final AISUB sub = new AISUB.Simple("BarracksRange") {
			
			@Override
			protected AISTATE resume(Humanoid a, AIManager d) {
				d.subByte ++;
				if (d.subByte == 1) 
					return AI.STATES().STAND.activate(a, d, 10);
				if (d.subByte == 2) 
					return AI.STATES().anima.archer1.activate(a, d, 3);
				if (d.subByte == 3) {
					if (blue(a) != null) {
						DIR dir = blue(a).faceCoo(d.planTile.x(), d.planTile.y());
						blue(a).fireArrow(a.tc().x(), a.tc().y(), a.body().cX()+dir.x()*C.TILE_SIZEH, a.body().cY()+dir.y()*C.TILE_SIZEH);
					}
					return AI.STATES().anima.archer2.activate(a, d, 3);
				}
				return null;
			}
		};
		
		
		
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			if (!module.planShouldContinue(a, d)) {
				can(a, d);
				return null;
			}
			return sub.activate(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			if (job(a, d) == null)
				return null;
			return set(a, d);
		}

		

	};
	
	private ROOM_ARCHERY blue(Humanoid a) {
		RoomInstance w = STATS.WORK().EMPLOYED.get(a);
		if (w != null && w.blueprintI() instanceof ROOM_ARCHERY)
			return (ROOM_ARCHERY) w.blueprintI();
		return null;
	}
	
	private JOBMANAGER_HASER work(Humanoid a) {
		if (blue(a) != null)
			return (JOBMANAGER_HASER) STATS.WORK().EMPLOYED.get(a);
		return null;
	}
	
	private SETT_JOB job(Humanoid a, AIManager d) {
		
		JOBMANAGER_HASER w = work(a);
		if (w != null) {

			SETT_JOB j = w.getWork().getJob(d.planTile);
			if (j == null || !j.jobReservedIs(null))
				return null;
			
			if (!module.planShouldContinue(a, d)) {
				j.jobReserveCancel(null);
				return null;
			}
				
			return j;
			
		}
		return null;
		
	}
	
	private abstract class Res extends Resumer {
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			SETT_JOB j = job(a, d);
			if (j != null)
				j.jobReserveCancel(null);
		}
		
		@Override
		protected void name(Humanoid a, AIManager d, Str string) {
			JOBMANAGER_HASER bb = work(a);
			if (bb == null)
				return;
			string.add(STATS.WORK().EMPLOYED.get(a).blueprintI().employment().verb);

		}
		
	}


}