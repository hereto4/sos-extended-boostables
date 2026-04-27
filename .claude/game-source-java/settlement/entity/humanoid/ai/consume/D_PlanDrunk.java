package settlement.entity.humanoid.ai.consume;

import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISTATE;
import settlement.entity.humanoid.ai.main.AISTATES.Animation;
import settlement.entity.humanoid.ai.main.AISUB;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import util.text.D;

final class D_PlanDrunk extends AIPLAN.PLANRES{

	private static CharSequence ¤¤drunk = "Intoxicated";
	private static CharSequence ¤¤sobering = "Sobering Up";

	static {
		D.ts(D_PlanDrunk.class);
	}
	
	public D_PlanDrunk() {
		super("serDrunk");
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		return walkWeird.set(a, d);
	}
	
	private final AISUB walk = new AISUB.Simple("DRUNK") {
		
		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {
			if (d.subByte == 0) {
				a.speed.turnRandom();
				AISTATE s =  AI.STATES().WALK.activate(a, d, 4+RND.rInt(5));
				a.speed.magnitudeTargetSet(0.2);
				a.speed.setDirCurrent(DIR.ALL.rnd());
				d.subByte = 1;
				return s;
			}
			a.speed.magnitudeInit(0);
			a.speed.magnitudeTargetSet(0);
			return null;
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.COLLISION_TILE) {
				return true;
			}
			return super.event(a, d, e);
		};
	};
	
	private final Resumer walkWeird = new Resumer(¤¤drunk) {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			return walk.activate(a, d);
		};
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			
			if (RND.rBoolean()) {
				return setAction(a, d);
			}else if (RND.oneIn(4)) {
				return sleep.set(a, d);
			}else {
				return drink.set(a, d);
			}
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			d.resourceCarriedSet(null);
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.COLLISION_TILE) {
				return true;
			}
			return super.event(a, d, e);
		};

	};
	
	private final Resumer drink = new Resumer(¤¤drunk) {
		
		private final Animation[] animi = new Animation[] {
			AI.STATES().anima.grab,
			AI.STATES().anima.box,
			AI.STATES().anima.fist,
			AI.STATES().anima.work,
		};
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			if (RND.rBoolean())
				return AI.SUBS().STAND.activateTime(a, d, 1+RND.rInt(5));
			return AI.SUBS().single.activate(a, d, animi[RND.rInt(animi.length)], RND.rFloat()*4);
		};
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			if (RND.oneIn(8))
				return sleep.set(a, d);
			return setAction(a, d);
			
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			d.resourceCarriedSet(null);
		}
	};
	
	private final Resumer sleep = new Resumer(¤¤sobering) {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().LAY.activateTime(a, d, 20+RND.rInt(40));
		};
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			d.resourceCarriedSet(null);
		}
	};
	

	
}
