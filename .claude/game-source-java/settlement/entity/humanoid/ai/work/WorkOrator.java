package settlement.entity.humanoid.ai.work;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISTATES.Animation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.misc.job.JOB_MANAGER;
import settlement.misc.job.SETT_JOB;
import settlement.room.main.RoomBlueprintIns;
import settlement.stats.STATS;
import snake2d.util.rnd.RND;

final class WorkOrator extends PlanBlueprint {

	private final Animation[] anima;
	
	private WorkOrator(AIModule_Work module, RoomBlueprintIns<?> blueprint, PlanBlueprint[] map, Animation[] anima){
		super(module, blueprint, map);
		this.anima = anima;
	}
	
	static WorkOrator getSpeaker(AIModule_Work module, RoomBlueprintIns<?> blueprint, PlanBlueprint[] map) {
		final Animation[] anima = new Animation[] {
			AI.STATES().anima.carry,
			AI.STATES().anima.fist,
			AI.STATES().anima.grab,
			AI.STATES().anima.fistRight,
			AI.STATES().anima.fistRight,
			AI.STATES().anima.fistRight,
		};
		return new WorkOrator(module, blueprint, map, anima);
	}
	
	static WorkOrator getDancer(AIModule_Work module, RoomBlueprintIns<?> blueprint, PlanBlueprint[] map) {
		final Animation[] anima = new Animation[] {
			AI.STATES().anima.carry,
			AI.STATES().anima.fist,
			AI.STATES().anima.grab,
			AI.STATES().anima.fistRight,
			AI.STATES().anima.fistLeft,
			AI.STATES().anima.dance,
			AI.STATES().anima.dance,
			AI.STATES().anima.dance,
			AI.STATES().anima.danceE,
			AI.STATES().anima.danceE,
			AI.STATES().anima.danceE,
		};
		return new WorkOrator(module, blueprint, map, anima);
	}
	
	static WorkOrator getLecture(AIModule_Work module, RoomBlueprintIns<?> blueprint, PlanBlueprint[] map) {
		final Animation[] anima = new Animation[] {
			AI.STATES().anima.box,
			AI.STATES().anima.fist,
			AI.STATES().anima.grab,
			AI.STATES().anima.wave,
		};
		return new WorkOrator(module, blueprint, map, anima);
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		
		if (!module.moduleCanContinue(a, d) || !hasEmployment(a, d))
			return null;
		if (STATS.WORK().WORK_TIME.indu().getD(a.indu()) == 1)
			return null;
		
		JOB_MANAGER jm = ((JOBMANAGER_HASER) work(a)).getWork();
		SETT_JOB j = jm.getReservableJob(null);
		if (j == null) {
			return null;
		}
		j.jobReserve(null);
		d.planTile.set(j.jobCoo());
		return walkToJob.set(a, d);
	}
	
	@Override
	protected boolean shouldContinue(Humanoid a, AIManager d) {
		return jobIsReservedAndReserve(a, d, null) && super.shouldContinue(a, d);
	}
	
	@Override
	protected void cancel(Humanoid a, AIManager d) {
		jobCancel(a, d, null);
		super.cancel(a, d);
	}
		
	
	final Resumer walkToJob = new Resumer(blueprint.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			AISubActivation s = AI.SUBS().walkTo.cooFull(a, d, d.planTile);
			if (s == null) {
				cancel(a, d);
			}
			return s;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return work.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}

		
	};
	
	final Resumer work = new Resumer(blueprint.employment().verb) {

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			SETT_JOB j = ((JOBMANAGER_HASER) work(a)).getWork().getJob(d.planTile);
			j.jobStartPerforming();
			return res(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			if (STATS.WORK().WORK_TIME.indu().getD(a.indu()) == 1 || !AI.modules().work.moduleCanContinue(a, d)) {
				SETT_JOB j = ((JOBMANAGER_HASER) work(a)).getWork().getJob(d.planTile);
				j.jobPerform(a, null, 0);
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
	


}