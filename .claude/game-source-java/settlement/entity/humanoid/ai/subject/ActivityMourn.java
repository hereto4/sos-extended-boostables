package settlement.entity.humanoid.ai.subject;

import static settlement.main.SETT.ROOMS;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.misc.util.FSERVICE;
import snake2d.util.rnd.RND;
import util.text.D;

final class ActivityMourn extends AIPLAN.PLANRES{

	private static CharSequence ¤¤verb = "Mourning old friend";
	
	static {
		D.ts(ActivityMourn.class);
	}
	
	public ActivityMourn() {
		super("SUBJECT_MOURN");
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		return start.set(a, d);
	}
	

	private final Resumer start = new Resumer(¤¤verb) {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			AISubActivation s = AI.SUBS().walkTo.service(a, d, ROOMS().graveServiceSpots, 500);
			return s;
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			return mourn.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
		}
	};
	
	private final Resumer mourn = new Resumer(¤¤verb) {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			d.planByte1 = (byte) (4+RND.rInt(8));
			return AI.SUBS().STAND.activate(a, d);
			
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			d.planByte1--;
			if (d.planByte1 >= 0) {
				return AI.SUBS().STAND.activateRndDir(a, d);
			}
			
			FSERVICE s = ROOMS().graveServiceSpots.get(d.path.destX(), d.path.destY());
			if (s != null)
				s.consume();
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return ROOMS().graveServiceSpots.get(d.path.destX(), d.path.destY()) != null;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			 FSERVICE s = ROOMS().graveServiceSpots.get(d.path.destX(), d.path.destY());
			 if (s != null)
				 s.findableReserveCancel();
		}
		
	};

}
