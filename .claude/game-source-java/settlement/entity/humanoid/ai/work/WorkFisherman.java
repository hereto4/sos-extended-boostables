package settlement.entity.humanoid.ai.work;
import game.time.TIME;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.food.fish.ROOM_FISHERY;
import snake2d.util.MATH;
import snake2d.util.sprite.text.Str;

final class WorkFisherman extends WorkAbs {

	
	protected WorkFisherman(AIModule_Work module, ROOM_FISHERY blueprint, PlanBlueprint[] map, Works works) {
		super(module, blueprint, map, works);
	}
	
	@Override
	protected AISubActivation work(Humanoid a, AIManager d) {
		
		ROOM_FISHERY f = (ROOM_FISHERY) work(a).blueprintI();
		
		if (f.launchFishingExpedition(a, d.planTile.x(), d.planTile.y())) {
			return goFishing.set(a, d);
		}
		
		
		return super.work(a, d);
	}
	
	final Resumer goFishing = new Resumer("fishyfish") {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			SETT.ENTITIES().moveIntoTheTheUnknown(a);
			a.speed.magnitudeInit(0);
			d.planByte2 = (byte) TIME.days().bitsSinceStart();
			d.planByte3 = 0;
			jobGet(a, d).jobStartPerforming();
			return AI.SUBS().STAND.activate(a, d);
		}
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
	
			if (d.planByte3 == 1) {
				return init(a, d);
			}
			
			if (MATH.distanceC(d.planByte2 & 0x0FF, TIME.days().bitsSinceStart()&0x0FF, 0x0FFF) >= 2) {
				can(a, d);
				return null;
			}
			
			return AI.SUBS().STAND.activate(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			WorkFisherman.super.work.can(a, d);
			SETT.ENTITIES().returnFromTheTheUnknown(a);
		}

		@Override
		public boolean con(Humanoid a, AIManager d) {
			return jobGet(a, d) != null;
		}
		
		@Override
		protected void name(Humanoid a, AIManager d, Str string) {
			if (jobGet(a, d) != null)
				string.add(jobGet(a, d).jobName());
			else
				super.name(a, d, string);
				
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.FISHINGTRIP_OVER) {
				if (work(a) != null && work(a).blueprintI() instanceof ROOM_FISHERY) {
					ROOM_FISHERY f = (ROOM_FISHERY) work(a).blueprintI();
					f.performFishingTrip(a, d.planTile.x(), d.planTile.y(), 0);
					d.planByte3 = 1;
				}else {
					can(a, d);
					d.planByte3 = 1;
				}
			}
			return super.event(a, d, e);
		}

	};

}