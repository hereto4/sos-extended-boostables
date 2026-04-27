package settlement.entity.humanoid.ai.consume;

import game.time.TIME;
import init.resources.RBIT.RBITImp;
import init.sprite.UI.UI;
import init.type.NEEDS;
import init.type.NEED_E;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIData.AIDataSuspender;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.entity.humanoid.ai.main.AISTATE;
import settlement.entity.humanoid.ai.main.AISUB;
import settlement.stats.STATS;
import snake2d.util.rnd.RND;
import util.text.D;

final class AIModule_Food extends AIModule{

	public final NEED_E need = NEEDS.TYPES().HUNGER;
	private final AIDataSuspender suspenderStarvation = AI.suspender("starve");
	private final AIDataSuspender suspenderService = AI.suspender("food_service");
	private final AIDataSuspender suspenderFind = AI.suspender("foodfind");
	
	final RBITImp bits = new RBITImp();
	
	public final AISUB eat = new AISUB.Simple("eating") {

		@Override
		protected AISTATE resume(Humanoid a, AIManager d) {
			
			d.subByte ++;
			switch(d.subByte) {
			case 1 : return AI.STATES().STAND.activate(a, d, 1.5f+RND.rFloat(4));
			case 2 : return AI.STATES().anima.box.activate(a, d, 2.5+RND.rFloat(2));
			case 3 : return AI.STATES().STAND.activate(a, d, 1.5f+RND.rFloat(4));
			case 4 : return AI.STATES().anima.box.activate(a, d, 2.5+RND.rFloat(2));
			}
			return null;
		}

		
	};
	
	private final PlansServices plans;
	private final AIPLAN eatPlan = new F_PlanEat(eat);
	private final AIPLAN starve = new F_PlanStarve(eat, suspenderStarvation);
	
	private static CharSequence ¤¤name = "¤eat";
	private static CharSequence ¤¤desc = "¤Find food";
	
	static {
		D.ts(AIModule_Food.class);
	}
	
	public AIModule_Food() {
		super(UI.icons().s.plate, ¤¤name, ¤¤desc);
		plans = new PlansServices(new F_SPlanCanteen(eat), new F_SPlanEatery(eat));
	}
	
	int dayI = -1;
	int am = 0;
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		if (dayI != TIME.days().bitsSinceStart()) {
			dayI = TIME.days().bitsSinceStart();
		}
		

		am ++;
		
		if (!suspenderService.is(d)) {
			AiPlanActivation p =  plans.getPlan(a, d);
			if (p == null)
				suspenderService.suspend(d);
			else
				return p;
		}
		
		int prio = need.stat().getPrio(a);
		
		if (prio >= 2 || !plans.worthTrying(a, d)) {
			if (!suspenderFind.is(d)) {
				AiPlanActivation p =  eatPlan.activate(a, d);
				if (p == null)
					suspenderFind.suspend(d);
				else
					return p;
			}
			
			if (STATS.FOOD().STARVATION.indu().getD(a.indu()) > 0) {
				return starve.activate(a, d);
			}
		}
		
		am--;
		
		return null;
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateI) {
		suspenderStarvation.update(d);
		suspenderFind.update(d);
		suspenderService.update(d);
	}
	
	@Override
	public int getPriority(Humanoid a, AIManager d) {
		
		if (STATS.FOOD().STARVATION.indu().getD(a.indu()) > 0)
			return 10;
		int p = need.stat().getPrio(a);
		if (p == 0)
			return 0;
		if (suspenderFind.is(d) && suspenderService.is(d))
			return 0;
		
		else if (p == 1) {
			return 4;	
		}
		return 6;
	}
	




}
