package settlement.entity.humanoid.ai.work;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.main.RoomInstance;
import settlement.room.main.job.ROOM_RADIUS.ROOM_RADIUS_INSTANCE;

final class WorkBuilder extends PlanBlueprint{

	protected WorkBuilder(AIModule_Work module, PlanBlueprint[] map) {
		super(module, SETT.ROOMS().BUILDER, map);
		
	}

	@Override
	public AiPlanActivation activate(Humanoid a, AIManager d) {
		RoomInstance i = work(a);
		ROOM_RADIUS_INSTANCE r = (ROOM_RADIUS_INSTANCE) i; 
		if (!r.searching()) {
			return null;
		}
		
		int sx = i.body().cX();
		int sy = i.body().cY();
		
		return AI.modules().work.oddjobber.activateWorker(a, d, sx, sy, r.radius());
		
	}

	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		return null;
	}
	
	@Override
	public boolean shouldReportWorkFailure(Humanoid a, AIManager d) {
		return d.plan() != AI.modules().work.oddjobber;
	}

}
