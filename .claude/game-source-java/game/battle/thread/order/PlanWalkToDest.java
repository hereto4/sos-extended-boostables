package game.battle.thread.order;

import game.battle.thread.order.BattleOrderTask.DIVTASK;
import game.battle.thread.order.BattleOrderUpdater.Data;
import game.battle.thread.order.BattleOrderUpdater.Plan;
import snake2d.util.sets.LISTE;

final class PlanWalkToDest extends PlanWalkAbs{

	
	public PlanWalkToDest(Tools tools, LISTE<Plan> all, Data data) {
		super(tools, all, data, DIVTASK.MOVE);
	}
	
	@Override
	void init() {

		setWalkToDest();

	}
	
	@Override
	void update(int gamemillis) {
		state(m).update(gamemillis);

	}

	@Override
	void finished() {
		task.stop(div);
		order.task.set(task);
		
	}
	
	@Override
	boolean continueWhenFighting() {
		return true;
	}


}
