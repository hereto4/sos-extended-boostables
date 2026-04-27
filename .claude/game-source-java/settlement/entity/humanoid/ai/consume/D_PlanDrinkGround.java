package settlement.entity.humanoid.ai.consume;

import game.GAME;
import game.faction.FResources.RTYPE;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCES;
import init.type.NEEDS;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.util.rnd.RND;
import util.text.D;

final class D_PlanDrinkGround extends AIPLAN.PLANRES{

	private static CharSequence ¤¤sDrink = "Having a drink";
	private static final RBITImp bi = new RBITImp();
	
	static {
		D.ts(D_PlanDrinkGround.class);
	}
	
	private final AIModule_Drink m;
	
	D_PlanDrinkGround(AIModule_Drink m){
		super("SerDrink");
		this.m = m;
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		return walk.set(a, d);
	}
	
	public boolean has(Humanoid a, AIManager d) {
		bi.clearSet(RESOURCES.DRINKS().mask).and(STATS.FOOD().fetchMask(a));
		return SETT.PATH().finders.resource.has(a.tc().x(), a.tc().y(), bi);
	}
	
	
	private final Resumer walk = new Resumer(¤¤sDrink) {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			bi.clearSet(RESOURCES.DRINKS().mask).and(STATS.FOOD().fetchMask(a));
			return AI.SUBS().walkTo.resource(a, d, bi);
		};
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {

			
			GAME.player().res().inc(d.resourceCarried(), RTYPE.CONSUMED, -1);
			NEEDS.TYPES().THIRST.stat().fix(a.indu());
			
			STATS.FOOD().DRINK.indu().set(a.indu(), 1);
			
			return drink.set(a, d);
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
	
	private final Resumer drink = new Resumer(¤¤sDrink) {
		
		@Override
		public AISubActivation setAction(Humanoid a, AIManager d) {
			if (RND.rBoolean())
				return AI.SUBS().STAND.activateTime(a, d, 1+RND.rInt(5));
			d.resourceCarriedSet(null);
			return m.subdrink.activate(a, d);
		};
		
		@Override
		public AISubActivation res(Humanoid a, AIManager d) {
			d.resourceCarriedSet(null);
			if (RND.rFloat() < STATS.FOOD().DRINK.indu().getD(a.indu()))
				return d.resumeOtherPlan(a, m.drunk);
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
