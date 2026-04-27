package settlement.entity.humanoid.ai.service;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.misc.util.FSERVICE;
import settlement.room.service.pleasure.ROOM_PLEASURE;
import settlement.stats.STATS;
import snake2d.util.rnd.RND;

final class M_PlanBrothel extends MPlan<ROOM_PLEASURE>{

	public M_PlanBrothel() {
		super("Brothel", SETT.ROOMS().BROTHELS, false);
	}

	@Override
	protected AISubActivation arrive(Humanoid a, AIManager d) {
		return wait.set(a, d);
	}
	
	final Resumer wait = new Resumer("") {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			d.planByte2 = (byte) (25 + RND.rInt(10));
			d.planByte1	 = 0;
			get(a, d).startUsing();
			return res(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			d.planByte2--;
			
			if (blue(d).clientShouldUndress(d.planTile.x(), d.planTile.y()) || d.planByte2 <= 0) {
				STATS.POP().NAKED.set(a.indu(), 1);
				if (d.planByte1 == 1) {
					blue(d).clientUndress(d.planTile.x(), d.planTile.y());
					return second.set(a, d);
				}
				d.planByte1	 = 1;
			}
			
			return AI.SUBS().STAND.activateRndDir(a, d, 3);
			
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			FINDABLE s = get(a, d);
			return s != null && s.findableReservedIs();
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			STATS.POP().NAKED.set(a.indu(), 0);
			FSERVICE s = get(a, d);
			if (s != null && s.findableReservedIs())
				s.consume();
		}
	};
	
	final Resumer second = new Resumer("") {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			d.planByte2 += 5 + RND.rInt(5);
			return res(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			d.planByte2--;
			if (d.planByte2 < 0) {
				wait.can(a, d);
				return null;
			}
			
			return AI.SUBS().STAND.activateRndDir(a, d, 3);
			
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return wait.con(a, d); 
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			wait.can(a, d);
		}
	};


	
}
