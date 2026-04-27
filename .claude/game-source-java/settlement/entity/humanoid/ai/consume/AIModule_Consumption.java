package settlement.entity.humanoid.ai.consume;

import settlement.entity.humanoid.ai.main.AIModule;
import snake2d.util.sets.ArrayList;

public class AIModule_Consumption extends ArrayList<AIModule>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public AIModule_Consumption() {
		super(new AIModule_Drink(), new AIModule_Food(), new AIModule_Shop());
	}
	
}
