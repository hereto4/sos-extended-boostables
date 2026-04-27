package settlement.entity.humanoid.ai.consume;

import init.resources.Meal;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.room.service.food.tavern.ROOM_TAVERN;
import settlement.room.service.module.RoomServiceAccess;
import settlement.room.service.module.RoomServiceAccess.ROOM_SERVICE_ACCESS_HASER;
import settlement.stats.STATS;
import snake2d.util.rnd.RND;

final class PlanTavern extends SPlanAbs<ROOM_TAVERN>{

	private final AIModule_Drink m;
	
	public PlanTavern(AIModule_Drink m) {
		super("Tavern", SETT.ROOMS().TAVERNS, false);
		this.m = m;
	}

	@Override
	protected AISubActivation arrive(Humanoid a, AIManager d) {
		return eat.set(a, d);
	}
	
	Resumer eat = new Resumer("eat") {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			
			d.planByte1 = (byte) (STATS.FOOD().DRINK.decree().get(a));
			d.planByte2 = 0;
		
			int rr = blue(d).grab(a.race().pref().drink, STATS.FOOD().DRINK.decree().get(a), d.planTile.x(), d.planTile.y());
			STATS.FOOD().drink(a, Meal.amount(rr), Meal.pref(rr));
			FSERVICE f = get(a, d);
			f.startUsing();
			return m.subdrink.activate(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {

			blue(d).anotherRound(d.planTile.x(), d.planTile.y());
			
			
			d.planByte2 ++;
			if (d.planByte2 >= STATS.FOOD().DRINK.decree().get(a)) {
				can(a, d);
				if (RND.rFloat() < STATS.FOOD().DRINK.indu().getD(a.indu()))
					return d.resumeOtherPlan(a, m.drunk);
				return null;
			}
			
			return m.subdrink.activate(a, d);
			
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			FSERVICE f = get(a, d);
			return f != null && f.findableReservedIs();
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			FSERVICE f = get(a, d);
			if ( f != null && f.findableReservedIs())
				f.consume();
		}
	};

	public boolean worthTrying(Humanoid a, AIManager d) {
		for (ROOM_SERVICE_ACCESS_HASER s : SETT.ROOMS().FOOD) {
			RoomServiceAccess b = s.service();
			if (b.accessRequest(a) && b.finder.has(a.tc()))
				return true;
			
		}
		return false;
	}
	
}
