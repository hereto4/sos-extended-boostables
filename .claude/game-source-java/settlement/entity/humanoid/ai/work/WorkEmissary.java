package settlement.entity.humanoid.ai.work;
import static settlement.main.SETT.PATH;
import static settlement.main.SETT.ROOMS;

import game.time.TIME;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModules;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.stats.STATS;

final class WorkEmissary extends WorkAbs {

	protected WorkEmissary(AIModule_Work module, PlanBlueprint[] map, Works works) {
		
		super(module, ROOMS().EMBASSY, map, works);
	}
	
	@Override
	protected AISubActivation finishedWork(Humanoid a, AIManager d) {
		if ((TIME.days().bitsSinceStart() + STATS.RAN().get(a.indu(), 0) & 0b01) == 0) {
			return super.finishedWork(a, d);
		}
		
		if (STATS.WORK().WORK_TIME.indu().getD(a.indu()) > 0.5)
			return super.finishedWork(a, d);
		
		if (AIModules.nextPrio(d) > 7) {
			return super.finishedWork(a, d);
		}
		
		return goOnMission.set(a, d);
		
	}
	
	final Resumer goOnMission = new Resumer(SETT.ROOMS().EMBASSY.employment().verb) {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			if (PATH().finders.entryPoints.find(a.tc().x(), a.tc().y(), d.path, Integer.MAX_VALUE)) {
				return AI.SUBS().walkTo.pathFull(a, d);
			}
			return null;
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			return beOnMission.set(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}

	};
	
	final Resumer beOnMission = new Resumer(SETT.ROOMS().EMBASSY.employment().verb) {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			SETT.ENTITIES().moveIntoTheTheUnknown(a);
			a.speed.magnitudeInit(0);
			return AI.SUBS().STAND.activate(a, d);
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			if (STATS.WORK().WORK_TIME.indu().getD(a.indu()) > 0.9) {
				can(a, d);
				return null;
			}
			if (AIModules.nextPrio(d) > 7) {
				can(a, d);
				return null;
			}
			return AI.SUBS().STAND.activate(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			SETT.ENTITIES().returnFromTheTheUnknown(a);
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}

	};

	
	@Override
	protected boolean shouldContinue(Humanoid a, AIManager d) {
		if (!super.shouldContinue(a, d))
			return false;
		
		return true;
	}


}