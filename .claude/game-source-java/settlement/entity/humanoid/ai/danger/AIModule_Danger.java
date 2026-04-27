package settlement.entity.humanoid.ai.danger;

import settlement.entity.humanoid.ai.main.AIModule;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;

public final class AIModule_Danger {

	public final LIST<AIModule> all;
	
	public AIModule_Danger(){
		ArrayListGrower<AIModule> all = new ArrayListGrower<>();
		all.add(new AIModule_Exposure());
		all.add(new AIModule_Health());
		this.all = all;
	}
	
}
