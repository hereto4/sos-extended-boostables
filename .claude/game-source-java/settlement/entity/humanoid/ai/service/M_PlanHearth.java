package settlement.entity.humanoid.ai.service;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISTATES.Animation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.room.main.Room;
import settlement.room.main.RoomInstance;
import settlement.room.service.hearth.ROOM_HEARTH;
import settlement.room.service.module.ROOM_SERVICER;
import settlement.stats.STATS;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;

final class M_PlanHearth extends MPlan<ROOM_HEARTH>{

	public M_PlanHearth() {
		super("Hearth", new ArrayList<>(SETT.ROOMS().HEARTH), true);
	}

	@Override
	protected AISubActivation arrive(Humanoid a, AIManager d) {
		return first.set(a, d);
	}
	
	private final Resumer first = new Resumer("2") {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			blue(d).service().service(d.path().destX(), d.path().destY()).startUsing();
			d.planByte1 = 0;
			RoomInstance r = blue(d).get(a.tc().x(), a.tc().y());
			a.speed.turn2(r.body().cX()-a.tc().x(), r.body().cY()-a.tc().y());
			return AI.SUBS().STAND.activateTime(a, d, 2+RND.rInt(15));
			
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {

			d.planByte1++;
			STATS.NEEDS().EXPOSURE.fix(a.indu());
			if (d.planByte1 < 8) {
				
				Room r = SETT.ROOMS().HEARTH.get(a.tc());
				
				if (r != null) {
					ROOM_SERVICER ss = (ROOM_SERVICER) r;
					if (ss.service().total()-ss.service().reserved() > 4) {
						if ((d.planByte1 & 1) == 1) {
							
							Animation s = RND.rBoolean() ? AI.STATES().anima.box : AI.STATES().anima.wave;
							return AI.SUBS().single.activate(a, d, s, 1+RND.rFloat(3));
						}
						return AI.SUBS().STAND.activateTime(a, d, 2+RND.rInt(10));
					}
					
				}
				
			}
			
			FSERVICE s = get(a, d);
			if (s != null && s.findableReservedIs())
				s.consume();
			
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			FSERVICE s = get(a, d);
			return (s != null && s.findableReservedIs());
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			FSERVICE s = get(a, d);
			 if (s != null)
				 s.findableReserveCancel();
		}

	};


	
}
