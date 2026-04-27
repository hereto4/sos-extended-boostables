package settlement.entity.humanoid.ai.types.student;

import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISTATE;
import settlement.entity.humanoid.ai.main.AISTATES.Animation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.misc.job.SETT_JOB;
import settlement.room.knowledge.university.ROOM_UNIVERSITY;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import util.text.D;

final class Plan extends AIPLAN.PLANRES{

	private static CharSequence ¤¤study = "¤studying";
	private final AIModule_Student module;
	
	static {
		D.ts(Plan.class);
	}
	
	Plan(AIModule_Student module){
		super("student");
		this.module = module;
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		AI.modules().work.swapInstance(a);
		return walk.set(a, d);
	}
	
	private final ROOM_UNIVERSITY uni(Humanoid a) {
		RoomInstance ins = STATS.WORK().EMPLOYED.get(a);
		if (ins == null)
			return null;
		if (ins.blueprintI() instanceof ROOM_UNIVERSITY)
			return (ROOM_UNIVERSITY) ins.blueprintI();
		return null;
	}
	
	private final JOBMANAGER_HASER jobs(Humanoid a) {
		RoomInstance ins = STATS.WORK().EMPLOYED.get(a);
		if (ins == null)
			return null;
		if (ins.blueprintI() instanceof ROOM_UNIVERSITY)
			return (JOBMANAGER_HASER) ins;
		return null;
	}
	
	private final SETT_JOB job(Humanoid a, AIManager d) {
		JOBMANAGER_HASER jj = jobs(a);
		if (jj == null)
			return null;
		SETT_JOB j = jj.getWork().getJob(d.planTile);
		if (j == null || !j.jobReservedIs(null))
			return null;
		return j;
	}
	
	
	final Resumer walk = new Resumer(¤¤study) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			JOBMANAGER_HASER jobs = jobs(a);
			if (jobs == null)
				return null;
			
			SETT_JOB j = jobs.getWork().getReservableJob(a.tc());
			
			if (j == null)
				return null;
			j.jobReserve(null);
			d.planTile.set(j.jobCoo());
			AISubActivation s = AI.SUBS().walkTo.cooFull(a, d, d.planTile);
			if (s == null) {
				cancel(a, d);
			}
			return s;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (!conn(a, d)) {
				cancel(a, d);
				return null;
			}
			if (!uni(a).isLecturer(d.planTile))
				return walkLast.set(a, d);
			return lecture.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	
	final Resumer walkLast = new Resumer(¤¤study) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			DIR dir = uni(a).spotDir(d.planTile);

			AISTATE s = AI.STATES().WALK2.edge(a, d, dir);
			a.speed.setDirCurrent(dir);
			return AI.SUBS().DUMMY.activate(a, d, s);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (!conn(a, d)) {
				cancel(a, d);
				return null;
			}
			
			a.speed.magnitudeTargetSet(0);
			a.speed.magnitudeInit(0);
			return study.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	
	final Resumer study = new Resumer(¤¤study) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return res(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			if (!conn(a, d)) {
				cancel(a, d);
				return null;
			}
			
			ROOM_UNIVERSITY u = uni(a);
			if (!u.isTime.is()) {
				cancel(a, d);
				return null;
			}
			
			
			
			DIR dir = u.spotDir(d.planTile);
			if (RND.oneIn(5)) {					
				dir = dir.next(-1+RND.rInt(3));
			}
			a.speed.setDirCurrent(dir);
			return AI.SUBS().STAND.activateTime(a, d, 5);
		}
		
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
		
	};
	
	final Resumer lecture = new Resumer(¤¤study) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return res(a, d);
		}
		
		final Animation[] anima = new Animation[] {
			AI.STATES().anima.carry,
			AI.STATES().anima.fist,
			AI.STATES().anima.grab,
			AI.STATES().anima.fistRight,
			AI.STATES().anima.fistRight,
			AI.STATES().anima.fistRight,
		};
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (!conn(a, d)) {
				cancel(a, d);
				return null;
			}
			
			ROOM_UNIVERSITY u = uni(a);
			if (!u.isTime.is()) {
				cancel(a, d);
				return null;
			}
			
			if (RND.oneIn(4)) {
				a.speed.setDirCurrent(a.speed.dir().next(-1+RND.rInt(3)));
			}
			
			if (RND.oneIn(2)) {
				return AI.SUBS().single.activate(a, d, anima[RND.rInt(anima.length)], 2 + RND.rInt(3));
			}else {
				return AI.SUBS().STAND.activateTime(a, d, 3 + RND.rInt(4));
			}
		}
		
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
		
	};
	
	@Override
	protected void cancel(Humanoid a, AIManager d) {
		SETT_JOB j = job(a, d);
		if (j != null)
			j.jobReserveCancel(null);
	}
	
	private boolean conn(Humanoid a, AIManager d) {
		return module.moduleCanContinue(a, d) && AIModule_Student.shouldContinue(a, d) && job(a, d) != null;
	}
	
	@Override
	public double poll(Humanoid a, AIManager d, HPollData e) {
		if (e.type == HPoll.WORKING)
			return 1.0;
		return super.poll(a, d, e);
	}

}