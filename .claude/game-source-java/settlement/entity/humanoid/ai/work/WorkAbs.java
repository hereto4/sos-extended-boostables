package settlement.entity.humanoid.ai.work;

import init.resources.RBIT;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.util.AIPlanResourceMany;
import settlement.entity.humanoid.ai.work.SubWork.SubWorkHands;
import settlement.entity.humanoid.ai.work.SubWork.SubWorkThink;
import settlement.entity.humanoid.ai.work.SubWork.SubWorkTool;
import settlement.main.SETT;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.misc.job.JOB_MANAGER;
import settlement.misc.job.SETT_JOB;
import settlement.room.main.RoomBlueprintIns;
import settlement.stats.STATS;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.text.Str;
import util.text.D;

class WorkAbs extends PlanBlueprint {

	private static CharSequence ¤¤walk = "¤walking to job";
	private static CharSequence ¤¤storing = "¤storing resource";
	static int maxCarry = 4;
	
	
	static {
		D.ts(WorkAbs.class);
	}
	
	private final Works works;
	
	WorkAbs(AIModule_Work module, RoomBlueprintIns<?> blueprint, PlanBlueprint[] map, Works works){
		super(module, blueprint, map);
		this.works = works;
	}
	
	WorkAbs(String key, AIModule_Work module, RoomBlueprintIns<?> blueprint, PlanBlueprint[] map, Works works){
		super(key, module, blueprint, map);
		this.works = works;
	}
	
	static class Works {
		final SubWorkTool subTool = new SubWorkTool("WORK_TOOL") {

			@Override
			protected SETT_JOB getJob(Humanoid a, AIManager d) {
				if (work(a) == null)
					return null;
				return ((JOBMANAGER_HASER) work(a)).getWork().getJob(d.planTile);
			}
			
		};
		
		final SubWorkHands subHands = new SubWorkHands("WORK_HANDS") {

			@Override
			protected SETT_JOB getJob(Humanoid a, AIManager d) {
				if (work(a) == null)
					return null;
				return ((JOBMANAGER_HASER) work(a)).getWork().getJob(d.planTile);
			}
			
		};
		
		final SubWorkThink subThink = new SubWorkThink("WORK_THINK") {

			@Override
			protected SETT_JOB getJob(Humanoid a, AIManager d) {
				if (work(a) == null)
					return null;
				return ((JOBMANAGER_HASER) work(a)).getWork().getJob(d.planTile);
			}
			
		};
	}
	

	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		
		if (!module.moduleCanContinue(a, d) || !hasEmployment(a, d))
			return null;
		if (STATS.WORK().WORK_TIME.indu().getD(a.indu()) == 1)
			return null;
		
		AISubActivation s = walkToRoom.set(a, d);
		if (s != null)
			return s;
		
		JOB_MANAGER jm = ((JOBMANAGER_HASER) work(a)).getWork();
		
		COORDINATE cc = null;
		

		if ((work(a).blueprintI() == SETT.ROOMS().JANITOR || work(a).is(d.planTile)) && a.physics.tileC().tileDistanceTo(d.planTile) == 1) {
			cc = d.planTile;
		}else if (work(a).is(a.physics.tileC())) {
			cc = a.physics.tileC();
		}
		
		SETT_JOB j = jm.getReservableJob(cc);
		if (j == null) {
			return null;
		}
		crashI = 0;
		return initBegin(a, d, j, jm);
	}
	
	static int crashI;
	
	private AISubActivation initBegin(Humanoid a, AIManager d, SETT_JOB j, JOB_MANAGER jm) {
	

		if (crashI++ > 1000)
			throw new RuntimeException(work(a) + " " + j + " " + jm);
		
		d.planTile.set(j.jobCoo());
		d.planByte1 = -1;
		if (j.jobResourceBitToFetch() != null && !j.jobResourceBitToFetch().isClear()) {
			RBIT res = j.jobResourceBitToFetch();
			
			AISubActivation s = fetch.activate(a, d, res, CLAMP.i(j.jobResourcesNeeded(a), 0 , maxCarry), j.longFetch() ? 1000 : 250, true, true);
			
			if (s == null) {
				j = jm.reportResourceMissing(res, d.planTile.x(), d.planTile.y());
				if (j == null)
					return null;
				return initBegin(a, d, j, jm);
			}
			
			STATS.WORK().fetchProximityStart(a);
			d.planByte1 = (byte) fetch.resource(a, d).index();
			jm.reportResourceFound(resource(d));
			j.jobReserve(resource(d));
			return s;
		}else {
			
			
//			STATS.WORK().proximityStart(a);
//			if (!isInRoom(a))
//				STATS.WORK().proximityEnd(a);
			
			j.jobReserve(null);
			return walk(a, d);
		}
	}
	
	private final Resumer walkToRoom = new Resumer(¤¤walk) {
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			STATS.WORK().proximityEnd(a);
			return init(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return work(a) != null;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			STATS.WORK().proximityEnd(a);
		}

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			if (work(a).blueprintI() == SETT.ROOMS().JANITOR)
				return null;
			
			if (isInRoom(a))
				return null;
			STATS.WORK().proximityStart(a);
			
			JOB_MANAGER jm = ((JOBMANAGER_HASER) work(a)).getWork();
			SETT_JOB j = jm.getReservableJob(a.tc());
			if (j != null) {
				AISubActivation s = AI.SUBS().walkTo.coo(a, d, j.jobCoo());
				if (s != null)
					return s;
					
			}
			if (STATS.WORK().shouldReportForWork(a)) {
				AISubActivation s = AI.SUBS().walkTo.room(a, d, work(a));
				if (s != null)
					return s;
			}
			
			
			can(a, d);
			return null;
		}
	};
	
	private boolean isInRoom(Humanoid a) {
		if (work(a).is(a.tc()))
			return true;
		else {
			for (DIR dd : DIR.ORTHO) {
				if (work(a).is(a.tc(), dd)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private final AIPlanResourceMany fetch = new AIPlanResourceMany(this, 64) {
		
		@Override
		public AISubActivation next(Humanoid a, AIManager d) {
			SETT_JOB j = jobGet(a, d);
			if (j == null) {
				d.resourceDrop(a);
				return null;
			}
			
			return walk(a, d);
		}
		
		@Override
		public void cancel(Humanoid a, AIManager d) {
			jobCancel(a, d, this.resource(a, d));
		}
	};
	
	private AISubActivation walk(Humanoid a, AIManager d) {
		SETT_JOB j = jobGet(a, d);
		if (j == null) {
			//happens with janitor sometimes, if the maintenance is invalid.
			//new RuntimeException(""+work(a)).printStackTrace();
			return null;
		}
		if (resource(d) == null && (j.jobResourceBitToFetch() == null || j.jobResourceBitToFetch().isClear())) {
			DIR dir = jobGet(a, d).jobStandDir();
			if (dir != null && !a.tc().isSameAs(d.planTile.x()+dir.x(),d.planTile.y()+dir.y())) {
				return walkToJobAjacent.set(a, d);
			}
			if (a.tc().tileDistanceTo(d.planTile) == 1 && DIR.get(a.tc(), d.planTile) == a.speed.dir()) {
				return work.set(a, d);
			}
		}
		
			
		return walkToJob.set(a, d);
	}
	
	private final Resumer walkToJob = new Resumer(¤¤walk) {
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			//STATS.WORK().proximityEnd(a);
			
			SETT_JOB j = ((JOBMANAGER_HASER) work(a)).getWork().getJob(d.planTile);
			if (resource(d) != null) {
				STATS.WORK().fetchProximityEnd(a);
				int am = CLAMP.i(d.resourceA(), 0, j.jobResourcesNeeded(a));
				j.jobPerform(a, resource(d), am);
				d.resourceAInc(-am);
				d.resourceDrop(a);
				return init(a, d);
			}
			if(j.jobPerformTime(a) == 0) {
				return storeResource.set(a, d);
			}else {
				return work(a, d);
			}
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return jobIsReservedAndReserve(a, d, resource(d));
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			jobCancel(a, d, resource(d));
		}

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			AISubActivation s = AI.SUBS().walkTo.coo(a, d, d.planTile);
			if (s == null) {
				can(a, d);
			}
			return s;
		}
	};
	
	private final Resumer walkToJobAjacent = new Resumer(¤¤walk) {
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return walkToJob.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return jobIsReservedAndReserve(a, d, resource(d));
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			jobCancel(a, d, resource(d));
		}

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			DIR dir = jobGet(a, d).jobStandDir();
			AISubActivation s = AI.SUBS().walkTo.cooFull(a, d, d.planTile.x()+dir.x(), d.planTile.y()+dir.y());
			if (s == null) {
				can(a, d);
			}
			return s;
		}
	};
	
	final Resumer storeResource = new Resumer(¤¤storing) {
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return init(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			SETT_JOB j = jobGet(a, d);
			RESOURCE res = j.jobPerform(a, null, 0);
			if (res == null) {
				LOG.ln("" + j);
				return AI.SUBS().STAND.activate(a, d);
			}
			return AI.SUBS().walkTo.deposit(a, d, res);
		}
	};
	
	protected AISubActivation work(Humanoid a, AIManager d) {
		return work.set(a, d);
	}
	
	final Resumer work = new Resumer("working") {
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			d.resourceCarriedSet(null);
			SETT_JOB j = jobGet(a, d);
			if (j == null) {
				jobCancel(a, d, resource(d));
				return null;
			}
			RESOURCE res = j.jobPerform(a, null, 0);
			if (res != null) {
				returnResource.set(a, d);
				return AI.SUBS().walkTo.deposit(a, d, res);
			}
			return finishedWork(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return jobIsReservedAndReserve(a, d, resource(d));
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			jobCancel(a, d, resource(d));
		}

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			SETT_JOB j = jobGet(a, d);
			if (j == null) {
				jobCancel(a, d, resource(d));
				return null;
			}
			j.jobStartPerforming();
			if (j.jobUseTool())
				return works.subTool.activate(a, d, j);
			else if (j.jobUseHands())
				return works.subHands.activate(a, d, j);
			else 
				return works.subThink.activate(a, d, j);
		}
		
		@Override
		protected void name(Humanoid a, AIManager d, Str string) {
			if (jobGet(a, d) != null)
				string.add(jobGet(a, d).jobName());
			else
				super.name(a, d, string);
				
		}
		
	};
	
	protected AISubActivation finishedWork(Humanoid a, AIManager d) {
		return init(a, d);
	}

	
	final Resumer returnResource = new Resumer(¤¤storing) {
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return init(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
		}

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return null;
		}
	};
	
	private RESOURCE resource(AIManager d) {
		if (d.planByte1 == -1)
			return null;
		return RESOURCES.ALL().get(d.planByte1);
	}
	
	@Override
	protected String debug(Humanoid a, AIManager d) {
		// TODO Auto-generated method stub
		return super.debug(a, d);
	}
	


}