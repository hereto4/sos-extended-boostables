package settlement.entity.humanoid.ai.work;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.util.AIPlanGladiator;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.service.arena.RoomArenaWork;
import settlement.stats.STATS;

final class WorkGladiator extends PlanBlueprint {

	private final AIPlanGladiator plan;
	private final RoomArenaWork w;
	
	protected WorkGladiator(RoomArenaWork g, RoomBlueprintIns<?> blue, AIModule_Work module, PlanBlueprint[] map) {
		super(module, blue, map);
		w = g;
		plan = new AIPlanGladiator("Work_" + blue.key, false, blue.employment().verb) {
			
			@Override
			public double poll(Humanoid a, AIManager d, HPollData e) {
				if (e.type == HPoll.WORKING)
					return 1.0;
				return super.poll(a, d, e);
			}
			
			@Override
			protected boolean shouldContinue(Humanoid a, AIManager d) {
				return hasEmployment(a, d);
			}
			
			@Override
			protected void cancel(Humanoid a, AIManager d) {
				super.cancel(a, d);
				if (work(a) != null && work(a).employees().isOverstaffed()) {
					STATS.WORK().EMPLOYED.set(a, null);
				}

			}

			@Override
			protected RoomArenaWork w(Humanoid a, AIManager d) {
				return g;
			}
			
		};
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		
		return res.set(a, d);
		
	}
	
	private final Resumer res = new Resumer() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().STAND.activate(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (work(a) != null) {
				if (!w.gladiatorInArena(a.tc().x(), a.tc().y())) {
					STATS.NEEDS().INJURIES.COUNT.indu().set(a.indu(), 0);
				}
				d.planTile.set(w.gladiatorGetSpot(work(a)));
				return d.resumeOtherPlan(a, plan);
			}
			return null;
			
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			return plan.event(a, d, e);
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			return plan.poll(a, d, e);
		}
	};

	



}