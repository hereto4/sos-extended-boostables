package settlement.entity.humanoid.ai.service;

import init.type.NEED;
import init.type.POP_CL;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN.AiPlanActivation;
import settlement.room.service.module.RoomServiceNeed.ROOM_SERVICE_NEED_HASER;

final class S_PlanEntertain extends S_Plan{

	private final ROOM_SERVICE_NEED_HASER sh;
	private final M_PlanSpectator plan;
	
	S_PlanEntertain(NEED need, ROOM_SERVICE_NEED_HASER sh, M_PlanSpectator plan) {
		super(sh.service().stats(), sh.service().usage);
		this.sh = sh;
		this.plan = plan;
	}

	@Override
	public boolean hasAccess(Humanoid a, AIManager d) {
		return sh.service().stats().access(a);
	}

	@Override
	public boolean allowed(Humanoid a, AIManager d) {
		return sh.service().stats().permission().is(POP_CL.clP(a.indu()));
	}

	@Override
	public boolean goodTime(Humanoid a, AIManager d) {
		return sh.service().isGoodTime();
	}

	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d) {
		return getPlan(a, d, sh.service().radius());
	}

	@Override
	public AiPlanActivation getPlan(Humanoid a, AIManager d, int dist) {
		d.planByte3 = (byte) sh.service().room().typeIndex();
		MPlan.dist = dist;
		return plan.activate(a, d);
	}


	
}
