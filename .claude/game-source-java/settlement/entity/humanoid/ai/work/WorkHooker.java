package settlement.entity.humanoid.ai.work;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.misc.job.JOB_MANAGER;
import settlement.misc.job.SETT_JOB;
import settlement.room.service.pleasure.ROOM_PLEASURE;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.text.D;

final class WorkHooker extends PlanBlueprint {

	private final ROOM_PLEASURE b;
	private static CharSequence ¤¤waiting = "¤Waiting for business";
	static {
		D.ts(WorkHooker.class);
	}
	
	protected WorkHooker(ROOM_PLEASURE b, AIModule_Work module, PlanBlueprint[] map) {
		super(module, b, map);
		this.b = b;
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		JOB_MANAGER jm = ((JOBMANAGER_HASER) work(a)).getWork();
		

		SETT_JOB j = jm.getReservableJob(a.tc());
		if (j == null) {
			return null;
		}
		d.planTile.set(j.jobCoo());
		AISubActivation s = walk.set(a, d);
		if (s != null) {
			j = jm.getJob(d.planTile);
			j.jobReserve(null);
		}
		return s;
	}
	
	final Resumer walk = new Resumer(blueprint.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			STATS.WORK().proximityStart(a);
			return AI.SUBS().walkTo.cooFull(a, d, d.planTile);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			STATS.WORK().proximityEnd(a);
			return init.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return hasEmployment(a, d) && jobIsReservedAndReserve(a, d, null);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			jobCancel(a, d, null);
		}
	};
	
	private final Resumer init = new Resumer(blueprint.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			a.speed.setDirCurrent(a.speed.dir().perpendicular());
			d.planByte1 = (byte) a.speed.dir().id();
			return AI.SUBS().STAND.activateTime(a, d, 1);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			
			if (!module.moduleCanContinue(a, d) || !hasEmployment(a, d) || STATS.WORK().WORK_TIME.indu().getD(a.indu()) == 1) {
				can(a, d);
				return null;
			}
			
			if (b.workerReadyShouldUndress(d.planTile.x(), d.planTile.y())) {
				STATS.POP().NAKED.set(a.indu(), 1);
			}else {
				STATS.POP().NAKED.set(a.indu(), 0);
			}
			
			if (RND.oneIn(10)) {
				a.speed.setDirCurrent(DIR.ALL.get(d.planByte1).next(-1).next(RND.rInt(3)));
			}
			
			return AI.SUBS().STAND.activateTime(a, d, 4);
			
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return walk.con(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			SETT_JOB j = jobGet(a, d);
			if (j != null)
				j.jobPerform(a, null, 0);
			STATS.POP().NAKED.set(a.indu(), 0);
		}
		
		@Override
		protected void name(Humanoid a, AIManager d, Str string) {
			if (b.workerReadyShouldUndress(d.planTile.x(), d.planTile.y()))
				super.name(a, d, string);
			else
				string.add(¤¤waiting);
		}
	};


}