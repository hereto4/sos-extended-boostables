package settlement.entity.humanoid.ai.work;
import game.time.TIME;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.law.slaver.ROOM_SLAVER;
import settlement.room.law.slaver.SlaverStation;
import snake2d.util.rnd.RND;

final class WorkSlaver extends PlanBlueprint {

	private final ROOM_SLAVER b = SETT.ROOMS().SLAVER;
	
	protected WorkSlaver(AIModule_Work module, PlanBlueprint[] map) {
		super(module, SETT.ROOMS().SLAVER, map);
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		SlaverStation s = b.workReserve(work(a));
		if (s == null)
			return null;
		d.planTile.set(s.coo());
		return walk.set(a, d);
	}
	
	final Resumer walk = new Resumer(blueprint.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			AISubActivation s = AI.SUBS().walkTo.coo(a, d, d.planTile);
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
			SlaverStation s = b.executionSpot(d.planTile);
			return hasEmployment(a, d) && s != null && s.workReserved();
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			SlaverStation s = b.executionSpot(d.planTile);
			if (s != null)
				s.workCancel();
		}
	};
	
	private final Resumer init = new Resumer(blueprint.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().STAND.activateTime(a, d, 1 + RND.rInt(5));
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			SlaverStation s = b.executionSpot(d.planTile);
			
			if (s.clientIsUsing()) {
				return execute.set(a, d);
			}else {
				can(a, d);
				return null;
			}
			
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return walk.con(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			walk.can(a, d);
		}
	};
	
	private final Resumer execute = new Resumer(blueprint.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().single.activate(a, d, AI.STATES().anima.work.activate(a, d, 15));
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			SlaverStation s = b.executionSpot(d.planTile);
			s.workExecute();
			return wait.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return walk.con(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			walk.can(a, d);
		}
	};
	
	private final Resumer wait = new Resumer(blueprint.employment().verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			d.planByte1 = (byte) (TIME.hours().bitCurrent() + (TIME.days().bitCurrent()&1)*TIME.hours().bitsPerCycle());
			return res(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			SlaverStation s = b.executionSpot(d.planTile);
			if (s.clientExecuted()) {
				if ((TIME.hours().bitCurrent() + (TIME.days().bitCurrent()&1)*TIME.hours().bitsPerCycle()) - d.planByte1 < 2) {
					return AI.SUBS().single.activate(a, d, AI.STATES().anima.work.activate(a, d, 5));
				}
			}
			s.clientClear();
			can(a, d);
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			SlaverStation s = b.executionSpot(d.planTile);
			return s!= null;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			walk.can(a, d);
		}
	};

}