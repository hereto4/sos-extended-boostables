package settlement.entity.humanoid.ai.subject;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.misc.util.FSERVICE;
import settlement.room.law.court.ROOM_COURT;
import snake2d.util.rnd.RND;

class ActivityCourt extends AIPLAN.PLANRES{

	public ActivityCourt() {
		super("SUBJECT_COURT");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected final AISubActivation init(Humanoid a, AIManager d) {
		return walk.set(a, d);
	}
	
	private final Resumer walk = new Resumer("Walk") {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			AISubActivation s = AI.SUBS().walkTo.serviceInclude(a, d, blue(d).access, 500);
			if (s == null)
				return null;
			d.planTile.set(d.path.destX(), d.path.destY());
			return s;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return first.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	
	private final Resumer first = new Resumer("") {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			get(a, d).startUsing();
			d.planByte1 = (byte) (4+RND.rInt(8));
			return AI.SUBS().STAND.activate(a, d);
			
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			d.planByte1--;
			if (d.planByte1 >= 0) {
				return AI.SUBS().STAND.activateRndDir(a, d);
			}
			
			FSERVICE s = get(a, d);
			if (s != null)
				s.consume();
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			FINDABLE s = get(a, d);
			return s != null && s.findableReservedIs();
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			FINDABLE s = get(a, d);
			if (s != null)
				s.findableReserveCancel();
		}
	};
	
	private ROOM_COURT blue(AIManager d) {
		return SETT.ROOMS().COURT;
	}
	
	private FSERVICE get(Humanoid a, AIManager d) {
		ROOM_COURT blue = blue(d);
		if (blue != null)
			return blue.access.get(d.planTile.x(), d.planTile.y());
		return null;
	}

}
