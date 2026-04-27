package game.battle.thread.order;

import game.battle.div.Div;
import game.battle.formation.DivFormationImp;
import game.battle.thread.order.BattleOrderTask.DIVTASK;
import game.battle.thread.order.BattleOrderUpdater.Data;
import game.battle.thread.order.BattleOrderUpdater.Plan;
import game.battle.thread.order.BattleOrderUpdater.PlanData;
import game.battle.thread.trajectory.BattleTrajectories;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.LISTE;
import util.data.INT_O.INT_OE;

final class PlanFireDiv extends PlanWalkAbs{

	private final INT_OE<PlanData> pathI;
	private final INT_OE<PlanData> pathd;
	private final INT_OE<PlanData> timer;
	private final VectorImp vec = new VectorImp();
	
	public PlanFireDiv(Tools tools, LISTE<Plan> all, Data data) {
		super(tools, all, data, DIVTASK.ATTACK_RANGED);
		pathI = data.new DataShort();
		timer = data.new DataShort();
		pathd = data.new DataBit();
	}
	
	@Override
	void init() {

		if (checkTarget()) {
			if (!div.settings().shouldNotMoveToFire || BattleTrajectories.trajectories(div) > 0)
				wait.set();
			else {
				setDest();
				setWalkToDest();
			}
			
		}else {
			fail.set();
		}
	}
	
	private boolean checkTarget() {
		if (div.settings().ammo() == null)
			return false;
		Div target = task.targetDiv();
		if (target == null || !target.active()) {
			fail.set();
			return false;
		}
		return true;
	}
	
	private void setDest() {
		Div target = task.targetDiv();
		
		int sx = div.centre().cUnitX();
		int sy = div.centre().cUnitY();
		
		int dx = target.centre().cUnitX();
		int dy = target.centre().cUnitY();
		
		double nx = 1;
		double ny = 0;
		
		if (sx != dx || sy != dy) {
			vec.set(sx, sy, dx, dy);
			vec.rotate90();
			nx = vec.nX();
			ny = vec.nY();
		}
		
		int w = (int) Math.sqrt(men*2);
		
		DivFormationImp f = t.deployer.deployCentre(div.info, men, div.settings().formation, dx, dy, nx, ny, w, a);
		
		if (f == null) {
			fail.set();
			return;
		}
		
		dest.copy(f);
		setWalkToDest();	
		pathd.set(m, path.currentI()&1);
	}
	
	@Override
	void update(int gamemillis) {
		
		if (!checkTarget()) {
			fail.set();
			return;
		}
		
		if (state(m) == wait) {
			
			if (!div.settings().shouldNotMoveToFire)
				return;
			
			
			
			if (BattleTrajectories.trajectories(div) > 0) {
				return;
			}
			
			timer.inc(m, gamemillis);
			
			if (!BattleTrajectories.hasPotential(div) && timer.get(m) > 1000) {
				setDest();
				setWalkToDest();
			}
			
			if (timer.get(m) > 5000) {
				setDest();
				setWalkToDest();
			}
			return;
		}
		
		
		if (div.status().engagements() > 0) {
			return;
		}

		if ((path.currentI() & 1) != pathd.get(m)) {
			pathd.set(m, path.currentI()&1);
			pathI.inc(m, 1);
			
			if (pathI.get(m) > 5 && BattleTrajectories.hasPotential(div)) {
				dest.copy(prev);
				path.clear();
				order.dest.set(dest);
				order.path.set(path);
				wait.set();
				return;
			}
			
			
			int tres = 50;
			if (path.isComplete()) {
				tres = CLAMP.i(path.length()-path.currentI(), 1, 50); 
			}
			
			if (pathI.get(m) > tres) {
				setDest();
			}
		}
	}

	@Override
	void finished() {
		fail.set();
	}
	
	@Override
	boolean continueWhenFighting() {
		return false;
	}
	
	private final STATE wait = new STATE("wait") {
		
		@Override
		void update(int gameMillis) {
			
		}
		
		@Override
		boolean setAction() {
			timer.set(m, 0);
			return true;
		}
	};
	
	private final STATE fail = new STATE("fail") {
		
		@Override
		void update(int gameMillis) {
			
		}
		
		@Override
		boolean setAction() {
			task.stop(div);
			order.task.set(task);
			return true;
		}
	};
	



}
