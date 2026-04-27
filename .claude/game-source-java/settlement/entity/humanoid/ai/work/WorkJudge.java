package settlement.entity.humanoid.ai.work;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.law.court.CourtStation;
import settlement.room.law.court.ROOM_COURT;
import snake2d.util.rnd.RND;

final class WorkJudge extends PlanBlueprint {

	private final ROOM_COURT b = SETT.ROOMS().COURT;
	
	protected WorkJudge(AIModule_Work module, PlanBlueprint[] map) {
		super(module, SETT.ROOMS().COURT, map);
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		CourtStation s = b.workReserve(work(a));
		if (s == null)
			return null;
		d.planTile.set(s.cooJudge());
		return walk.set(a, d);
	}
	
	final Resumer walk = new Resumer(blueprint.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			AISubActivation s = AI.SUBS().walkTo.cooFull(a, d, d.planTile);
			if (s == null) {
				can(a, d);
				return null;
			}
			return s;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return init.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			CourtStation s = b.executionSpot(d.planTile);
			return hasEmployment(a, d) && s != null && s.workReserved();
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			CourtStation s = b.executionSpot(d.planTile);
			if (s != null)
				s.workCancel();
		}
	};
	
	private final Resumer init = new Resumer(blueprint.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			CourtStation s = b.executionSpot(d.planTile);
			a.speed.setDirCurrent(s.jundgeDir());
			d.planByte1 = 20;
			s.workUse();
			return AI.SUBS().STAND.activateTime(a, d, 1 + RND.rInt(5));
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			CourtStation s = b.executionSpot(d.planTile);
			
			if (s == null || d.planByte1 -- <= 0 || !s.workReserved()) {
				can(a, d);
				return null;
			}
			
			if (RND.oneIn(5)) {
				return AI.SUBS().single.activate(a, d, AI.STATES().anima.fist.activate(a, d));
			}
			return AI.SUBS().STAND.activateTime(a, d, 1 + RND.rInt(5));
			
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			walk.can(a, d);
		}
	};

}