package settlement.entity.humanoid.ai.types.retired;

import init.sprite.UI.UI;
import init.type.HTYPE;
import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.misc.job.SETT_JOB;
import settlement.room.infra.elderly.ROOM_RESTHOME;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.text.D;

public final class AIModule_Retired extends AIModule{

	private final Plan plan = new Plan(this);
	
	private static CharSequence ¤¤name = "Retire";
	private static CharSequence ¤¤desc = "Spend time at a retirement facility.";
	static {
		D.ts(AIModule_Retired.class);
	}
	
	
	public AIModule_Retired(){
		super(UI.icons().s.clock, ¤¤name, ¤¤desc);
		
		
	}

	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		for (ROOM_RESTHOME h : a.race().pref().resthomes) {
			if (h.emp.employ(a)) {
				AI.modules().work.swapInstance(a);
				return plan.activate(a, d);
			}
		}
		
		return null;
	}

	@Override
	protected void init(Humanoid a, AIManager d, HTYPE prev, HTYPE current) {
		for (ROOM_RESTHOME h : a.race().pref().resthomes) {
			if (h.emp.employ(a))
				return;
		}
	}


	@Override
	public int getPriority(Humanoid a, AIManager d) {
		
		if (STATS.WORK().EMPLOYED.get(a) == null) {
			for (ROOM_RESTHOME h : a.race().pref().resthomes) {
				if (h.emp.employable() > 0)
					return 4;
			}
			return 0;
		}
		
		return STATS.WORK().WORK_TIME.indu().getD(a.indu()) < 1 ? 4 : 0;
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateOfDay) {
		
	}
	
	private static final class Plan extends AIPLAN.PLANRES{

		private final AIModule_Retired module;
		
		Plan(AIModule_Retired module){
			super("Retired");
			this.module = module;
		}
		
		@Override
		protected AISubActivation init(Humanoid a, AIManager d) {
			d.planByte1 = (byte) ((ROOM_RESTHOME)STATS.WORK().EMPLOYED.get(a).blueprint()).typeIndex();
			return walk.set(a, d);
		}
		
		private ROOM_RESTHOME blue(AIManager d) {
			return SETT.ROOMS().RESTHOMES.get(d.planByte1);
		}
		
		private final JOBMANAGER_HASER jobs(Humanoid a) {
			RoomInstance ins = STATS.WORK().EMPLOYED.get(a);
			if (ins == null)
				return null;
			if (ins.blueprintI() instanceof ROOM_RESTHOME) {
				return (JOBMANAGER_HASER) ins;
			}
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
		
		
		final Resumer walk = new Resumer() {
			
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
				AISubActivation s = null;
				if (SETT.PATH().solidity.is(d.planTile)) {
					s = AI.SUBS().walkTo.coo(a, d, d.planTile);
				}else
					s = AI.SUBS().walkTo.cooFull(a, d, d.planTile);
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
		
		final Resumer work = new Resumer() {
			
			@Override
			protected AISubActivation setAction(Humanoid a, AIManager d) {
				d.planByte2 = (byte) (10+RND.rInt(20));
				job(a, d).jobStartPerforming();
				return res(a, d);
			}
			
			@Override
			protected AISubActivation res(Humanoid a, AIManager d) {
				
				if (!conn(a, d)) {
					cancel(a, d);
					return null;
				}
				
				d.planByte2--;
				if (d.planByte2 <= 0) {
					cancel(a, d);
					return walk.set(a, d);
				}
				
				if (blue(d).cards(d.planTile)) {
					if (RND.oneIn(8))
						return AI.SUBS().DUMMY.activate(a, d, AI.STATES().anima.fist.activate(a, d, 1 + RND.rFloat()*2));
					return AI.SUBS().STAND.activateTime(a, d, 6+RND.rInt(3));
				}else if (blue(d).dance(d.planTile)) {
					if (RND.oneIn(5))
						return AI.SUBS().STAND.activateRndDir(a, d, 5+RND.rInt(3));
					a.speed.setDirCurrent(a.speed.dir().next(-1+RND.rInt(3)));
					return AI.SUBS().DUMMY.activate(a, d, AI.STATES().animaArr.dance().activate(a, d, 1+RND.rFloat()*10));
				}else if (blue(d).sitDir(d.planTile) != null) {
					a.speed.setDirCurrent(blue(d).sitDir(d.planTile));
					if (RND.oneIn(5)) {
						return AI.SUBS().DUMMY.activate(a, d, AI.STATES().animaArr.speak().activate(a, d, 2+RND.rFloat()*5));
					}
					
					return AI.SUBS().STAND.activateTime(a, d, 5);
				
				}
				
				
				int dx = a.tc().x()+a.speed.dir().x();
				int dy = a.tc().y()+a.speed.dir().y();
				if (SETT.ENTITIES().hasAtTile(dx, dy)) {
					if (RND.rBoolean())
						return AI.SUBS().DUMMY.activate(a, d, AI.STATES().animaArr.speak().activate(a, d, 2+RND.rFloat()*5));
					return AI.SUBS().STAND.activateTime(a, d, 5);
				}else if (RND.rBoolean()) {
					a.speed.setDirCurrent(a.speed.dir().next(-1+RND.rInt(3)));
				}
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
		
		@Override
		protected void cancel(Humanoid a, AIManager d) {
			SETT_JOB j = job(a, d);
			if (j != null)
				j.jobReserveCancel(null);
		}
		
		private boolean conn(Humanoid a, AIManager d) {
			return job(a, d) != null && module.moduleCanContinue(a, d) && module.getPriority(a, d) > 0;
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			if (e.type == HPoll.WORKING)
				return 1.0;
			return super.poll(a, d, e);
		}
		
		@Override
		protected void name(Humanoid a, AIManager d, Str string) {
			string.add(blue(d).employment().verb);
		}

	}

}
