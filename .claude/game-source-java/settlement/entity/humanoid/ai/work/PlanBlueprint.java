package settlement.entity.humanoid.ai.work;

import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.room.main.RoomBlueprintIns;
import settlement.stats.STATS;

abstract class PlanBlueprint extends PlanWork {

	protected final RoomBlueprintIns<?> blueprint;
	protected final AIModule_Work module;
	static final int maxCarry = 4;
	
	protected PlanBlueprint(AIModule_Work module, RoomBlueprintIns<?> blueprint, PlanBlueprint[] map) {
		this("work_" + blueprint.key, module, blueprint, map);
	}
	
	protected PlanBlueprint(String key, AIModule_Work module, RoomBlueprintIns<?> blueprint, PlanBlueprint[] map) {
		super(key);
		if (map[blueprint.index()] != null)
			throw new RuntimeException();
		map[blueprint.index()] = this;
		this.blueprint = blueprint;
		this.module = module;
	}

	// protected Blueprint(String title, RoomBlueprint blueprint) {
	// if (map[blueprint.index()] != null)
	// throw new RuntimeException();
	// map[blueprint.index()] = this;
	// this.title = title;
	// }

	public boolean shouldReportWorkFailure(Humanoid a, AIManager d) {
		return true;
	}
		

	@Override
	protected void cancel(Humanoid a, AIManager d) {
		super.cancel(a, d);
		if (work(a) != null && work(a).employees().isOverstaffed()) {
			STATS.WORK().EMPLOYED.set(a, null);
		}

	}
	
	@Override
	public double poll(Humanoid a, AIManager d, HPollData e) {
		if (e.type == HPoll.WORKING) {
			return 1.0;
		}
		return super.poll(a, d, e);
	}
	
	protected double transportAmount(Humanoid a, AIManager d) {
		return 0;
	}

}