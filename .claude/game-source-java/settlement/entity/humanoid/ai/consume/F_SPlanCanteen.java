package settlement.entity.humanoid.ai.consume;

import init.resources.Meal;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.service.food.canteen.ROOM_CANTEEN;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

final class F_SPlanCanteen extends SPlanAbs<ROOM_CANTEEN>{

	private final AISUB eat;

	public F_SPlanCanteen(AISUB eat) {
		super("Canteen", SETT.ROOMS().CANTEENS, false);
		this.eat = eat;

	}

	@Override
	protected AISubActivation arrive(Humanoid a, AIManager d) {
		return first.set(a, d);
	}
	
	
	final Resumer first = new Resumer("1") {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {

			int da = blue(d).grab(a.race().pref().food, STATS.FOOD().FOOD.decree().get(a), d.planTile.x(), d.planTile.y());
			
			STATS.FOOD().eat(a, Meal.amount(da), Meal.pref(da));
			COORDINATE c = blue(d).getChair(d.planTile.x(), d.planTile.y());
			
			if (c != null) {
				AISubActivation s = AI.SUBS().walkTo.cooFull(a, d, c);
				if (s != null) {
					d.planTile.set(c);
					d.planObject = da;
					walkTable.set(a, d);
					return s;
				}
			}
			return eat.activate(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {

		}
	};
	
	final Resumer walkTable = new Resumer("2") {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return null;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return walkLast.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return blue(d).is(d.planTile);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			blue(d).returnChair(d.planTile.x(), d.planTile.y());
		}
	};
	
	final Resumer walkLast = new Resumer("3") {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			DIR dir = blue(d).setChair(d.planTile.x(), d.planTile.y(), d.planObject);
			if (dir != null) {
				return AI.SUBS().single.activate(a, d, AI.STATES().WALK2.moveToEdge(a, d, dir));
			}
			return null;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			a.speed.magnitudeInit(0);
			return eatTable.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return walkTable.con(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			walkTable.can(a, d);
		}
	};
	
	final Resumer eatTable = new Resumer("4") {
		

		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			d.planByte1 = (byte) (4 + RND.rInt(10));
			return eat.activate(a, d);
		};
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			d.planByte1 --;
			if (d.planByte1 < 0) {
				can(a, d);
				return null;
			}else {
				return eat.activate(a, d);
			}
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return walkTable.con(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			walkTable.can(a, d);
		}
	};


	
}