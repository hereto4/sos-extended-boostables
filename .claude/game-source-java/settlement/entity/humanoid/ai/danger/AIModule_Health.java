package settlement.entity.humanoid.ai.danger;

import init.sprite.UI.UI;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.main.SETT;
import settlement.stats.STATS;
import util.text.D;

final class AIModule_Health extends AIModule{

	private final PlanSick sick = new PlanSick("dangerSick");
	private final PlanInjured bleed = new PlanInjured("dangerInjured");
	private static CharSequence ¤¤name = "Recover";
	private static CharSequence ¤¤desc = "Recover from injuries or illness, either at home or at a hospital.";
	static {
		D.ts(AIModule_Health.class);
	}
	
	public AIModule_Health() {
		super(UI.icons().s.plus, ¤¤name, ¤¤desc);
	}
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		if (STATS.WORK().incap.stat.indu().get(a.indu()) == 0)
			STATS.WORK().incap.stat.indu().set(a.indu(), 1);
		
		if (STATS.WORK().EMPLOYED.get(a) != null && STATS.WORK().EMPLOYED.get(a).blueprintI() == SETT.ROOMS().HOSPITAL) {
			STATS.WORK().EMPLOYED.set(a, null);
		}
		
		if (STATS.DISEASE().status(a.indu()).active) {
			return sick.activate(a, d);
		}
		if (STATS.NEEDS().INJURIES.inDanger(a.indu())) {
			return bleed.activate(a, d);
		}
		return null;
	}


	@Override
	public int getPriority(Humanoid a, AIManager d) {
		
		if (STATS.DISEASE().status(a.indu()).active) {
			return 7;
		}
		
		if (STATS.NEEDS().INJURIES.inDanger(a.indu())) {
			return 7;
		}
		
		return 0;
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateOfDay) {
		// TODO Auto-generated method stub
		
	}


	
	
}
