package settlement.entity.humanoid.ai.service;

import init.type.NEED;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.stats.service.StatService;

abstract class S_Plan {

	public final StatService service;
	public final NEED need;
	public final double usage;
	
	S_Plan(StatService service, double usage){
		this.need = service.need;
		this.service = service;
		this.usage = usage;
	}
	
	public abstract boolean hasAccess(Humanoid a, AIManager d);
	
	public abstract boolean allowed(Humanoid a, AIManager d);
	public abstract boolean goodTime(Humanoid a, AIManager d);

	public abstract AiPlanActivation getPlan(Humanoid a, AIManager d);
	public abstract AiPlanActivation getPlan(Humanoid a, AIManager d, int dist);
	
}
