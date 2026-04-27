package settlement.entity.humanoid.ai.consume;

import init.resources.RBIT.RBITImp;
import init.sprite.UI.UI;
import init.type.NEEDS;
import init.type.NEED_E;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISTATE;
import settlement.entity.humanoid.ai.main.AISUB;
import settlement.stats.STATS;
import snake2d.util.rnd.RND;
import util.text.D;

final class AIModule_Drink extends AIModule{

	public final NEED_E need = NEEDS.TYPES().THIRST;
	
	final RBITImp bits = new RBITImp();
	
	public final D_PlanDrunk drunk = new D_PlanDrunk();
	private final D_PlanDrinkGround ground = new D_PlanDrinkGround(this);

	private final PlansServices plans = new PlansServices(new PlanTavern(this));
	
	private static CharSequence ¤¤name = "¤drink";
	private static CharSequence ¤¤desc = "¤Consume drink off the ground or in a tavern.";
	
	static {
		D.ts(AIModule_Drink.class);
	}
	
	public final AISUB subdrink = new AISUB.Simple("subsDrinking") {

		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {
			
			d.subByte ++;
			switch(d.subByte) {
			case 1 : return AI.STATES().STAND.activate(a, d, 2+RND.rFloat(4));
			case 2 : return AI.STATES().anima.fist.activate(a, d, 1.5);
			case 3 : return AI.STATES().STAND.activate(a, d, 2+RND.rFloat(4));
			case 4 : return AI.STATES().anima.fist.activate(a, d, 1.5);
			case 5 : return AI.STATES().STAND.activate(a, d, 2+RND.rFloat(4));
			case 6 : return AI.STATES().anima.fist.activate(a, d, 1.5);
			case 7 : return AI.STATES().STAND.activate(a, d, 2+RND.rFloat(4));
			case 8 : return AI.STATES().anima.fist.activate(a, d, 1.5);
			}
			return null;
		}

		
	};
	
	public AIModule_Drink() {
		super(UI.icons().s.jug, ¤¤name, ¤¤desc);
	}
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		need.stat().fixMax(a.indu());
		AiPlanActivation p = plans.getPlan(a, d);
		if (p != null)
			return p;
		p = ground.activate(a, d);
		if (p == null) {
			STATS.FOOD().drink(a, 0, 0);
		}
		return p;
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateI) {

	}
	
	@Override
	public int getPriority(Humanoid a, AIManager d) {
		
		int prio = need.stat().getPrio(a);
		
		if (prio == 0)
			return 0;
		
		return 4;
	}
	




}
