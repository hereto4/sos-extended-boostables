package settlement.entity.humanoid.ai.service;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.room.service.hygine.well.ROOM_WELL;
import settlement.stats.STATS;
import snake2d.util.rnd.RND;

final class M_PlanWell extends MPlan<ROOM_WELL>{

	public M_PlanWell() {
		super("Well", SETT.ROOMS().WELLS, true);
	}

	@Override
	protected AISubActivation arrive(Humanoid a, AIManager d) {
		return first.set(a, d);
	}
	
	final Resumer first = new Resumer("1") {

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			STATS.POP().NAKED.set(a.indu(), 1);
			blue(d).service().service(d.planTile.x(), d.planTile.y()).startUsing();
			d.planByte1 = (byte) (1 + RND.rInt(8));
			return AI.SUBS().STAND.activate(a, d);
		}

		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			if(d.planByte1 <= 0) {
				
				STATS.NEEDS().EXPOSURE.fix(a.indu());
				STATS.NEEDS().DIRTINESS.set(a.indu(), 0);
				if (STATS.NEEDS().EXPOSURE.COUNT.indu().get(a.indu()) == 0) {
					can(a, d);
					return null;
				}
				d.planByte1 = (byte) (1 + RND.rInt(8));
			}

			d.planByte1 --;
			
			if ((d.planByte1 & 1) == 1) {
				return AI.SUBS().STAND.activateTime(a, d, 1+RND.rInt(5));
			}else {
				return AI.SUBS().single.activate(a, d, AI.STATES().anima.box, 1+RND.rInt(5));
			}
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			FSERVICE s = blue(d).service().service(d.planTile.x(), d.planTile.y());
			return s != null && s.findableReservedIs();
		}

		@Override
		public void can(Humanoid a, AIManager d) {
			FSERVICE s = blue(d).service().service(d.planTile.x(), d.planTile.y());
			if (s != null && s.findableReservedIs())
				s.consume();
			STATS.POP().NAKED.set(a.indu(), 0);
		}
	};
	



	
}
