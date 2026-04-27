package settlement.entity.humanoid.ai.consume;

import init.resources.RBIT.RBITImp;
import init.sprite.UI.UI;
import init.type.NEEDS;
import init.type.NEED_E;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIModule;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.stats.STATS;
import settlement.stats.equip.WearableResource;
import util.text.D;

final class AIModule_Shop extends AIModule{

	public final NEED_E need = NEEDS.TYPES().SHOPPING;
	final RBITImp bits = new RBITImp();
	
	public final M_PlanEquip ground = new M_PlanEquip();
	public final M_PlanReturn ret = new M_PlanReturn();
	private final PlansServices plans = new PlansServices(new M_PlanMarket(this));
	private static CharSequence ¤¤name = "Shopping";
	private static CharSequence ¤¤desc = "Browse the local markets or warehouses for equipment and Furniture.";
	static {
		D.ts(AIModule_Shop.class);
	}

	
	public AIModule_Shop() {
		super(UI.icons().s.storage, ¤¤name, ¤¤desc);
	}
	
	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		
		need.stat().fixMax(a.indu());
		
		AiPlanActivation p;
		
		p = ret.activate(a, d);
		if (p != null)
			return p;
		
		p =  plans.getPlan(a, d);
		if (p == null)
			return ground.activate(a, d);
		
		
		return p;
	}

	@Override
	protected void update(Humanoid a, AIManager d, boolean newDay, int byteDelta, int updateI) {
		
	}
	
	@Override
	public int getPriority(Humanoid a, AIManager d) {
		
		for (WearableResource e : STATS.EQUIP().BATTLE_ALL()) {
			if (e.needed(a.indu()) < 0) {
				return 6;
			}	
		}	
		
		int prio = need.stat().getPrio(a);
		
		if (prio == 0)
			return 0;
		
		return 4;
	}
	




}
