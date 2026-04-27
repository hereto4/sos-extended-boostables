package settlement.entity.humanoid.ai.consume;

import init.resources.Meal;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.room.service.food.eatery.ROOM_EATERY;
import settlement.stats.STATS;

final class F_SPlanEatery extends SPlanAbs<ROOM_EATERY>{

	private final AISUB eat;
	
	public F_SPlanEatery(AISUB eat) {
		super("Eatery", SETT.ROOMS().EATERIES, false);
		this.eat = eat;
	}

	@Override
	protected AISubActivation arrive(Humanoid a, AIManager d) {
		return first.set(a, d);
	}
	
	final Resumer first = new Resumer("") {
		

		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return eat.activate(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			int da = blue(d).eat(a.race().pref().food, STATS.FOOD().FOOD.decree().get(a), d.planTile.x(), d.planTile.y());
			
			STATS.FOOD().eat(a, Meal.amount(da), Meal.pref(da));
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			FSERVICE ss = blue(d).service().service(d.planTile.x(), d.planTile.y());
			if (ss != null)
				ss.findableReserveCancel();
		}
	};


	
}