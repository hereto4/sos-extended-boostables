package game.battle.thread.trajectory;

import game.battle.div.Div;
import game.battle.formation.FormationBody;
import game.battle.thread.order.BattleOrderTask;
import game.battle.thread.status.BattleStatus;
import init.constant.C;
import init.constant.Config;
import settlement.main.SETT;
import settlement.stats.equip.EquipRange;
import settlement.thing.projectiles.SProjectiles;
import settlement.thing.projectiles.Trajectory;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.sets.ArrayList;

final class UpdaterTraj {

	private final DivTrajectory[] all = new DivTrajectory[Config.battle().DIVISIONS_PER_BATTLE];
	{
		for (int i = 0; i < all.length; i++)
			all[i] = new DivTrajectory();
	}

	private final BattleOrderTask task = new BattleOrderTask();
	private final Trajectory trajLow = new Trajectory();
	private final VectorImp vec1 = new VectorImp();
	private final VectorImp vec2 = new VectorImp();
	private final FormationBody bodyArcher = new FormationBody();
	private final FormationBody bodyTarget = new FormationBody();
	private final ArrayList<Div> targets = new ArrayList<Div>(16);

	public DivTrajectory update(Request req, Div div, DivTrajectory old) {

		DivTrajectory traj = all[div.index()];
		traj.clear();

		EquipRange ammo = div.settings().ammo();

		if (div.active() && ammo != null) {

			div.order().task.get(task);
			if (task.task() == BattleOrderTask.DIVTASK.ATTACK_RANGED) {
				setTrajectory(req, div, task.targetDiv(), traj);

			} else if (div.settings().fireAtWill()) {
				targets.clear();
				div.status().enemiesClosest(targets);
				for (Div d : targets) {
					if (setTrajectory(req, div, d, traj)) {
						break;
					}
				}

			}
		}

		all[div.index()] = old;

		return traj;

	}

	private boolean setTrajectory(Request req, Div div, Div target, DivTrajectory traj) {
		
		if (target == null || !target.active()) {
			return false;
		}
		
		if (!bodyTarget.init(target.current()))
			return false;
		
		if (SProjectiles.problem(trajLow, div, bodyTarget.cX(), bodyTarget.cY()) == SProjectiles.¤¤OUT_OF_RANGE) {
			return false;
		}
		
		if (!bodyArcher.init(div.current()))
			return false;
		
		EquipRange a = div.settings().ammo();
		if (a == null || a != req.ammo())
			return false;
		
		traj.potential = true;
		
		boolean hasCounters = false;
		
		outout:
		for (int ui = 0; ui < div.menNrOf(); ui++) {
			
			int i = ui;//div.reporter.positionSpot(ui);
			
			if (!req.count(i)) {
				hasCounters = true;
				continue;
			}
			
			float ref = req.ref(i);
			if (ref < 0)
				continue;
		
			double angle = a.projectile.maxAngle(ref);
			double vel = a.projectile.velocity(ref);
			
			final int startX = req.x(i);
			final int startY = req.y(i);
			
			double ddx = (startX-bodyArcher.x1());
			ddx /= bodyArcher.width();
			
			double ddy = (startY-bodyArcher.y1());
			ddy /= bodyArcher.height();
			
			int targetX = (int) (bodyTarget.x1() + ddx*bodyTarget.width());
			int targetY = (int) (bodyTarget.y1() + ddy*bodyTarget.height());
			
			vec1.set(startX, startY, targetX, targetY);

			vec2.set(vec1);
			vec2.rotate90();
			
			
			for (int vv1 = 0; vv1 > -5; vv1--) {
				
				int dx = (int) (targetX + (vec1.nX())*C.TILE_SIZE);
				int dy = (int) (targetY + (vec1.nY())*C.TILE_SIZE);
				if (isEnemy(div, dx, dy) && SProjectiles.problem(div.army(), trajLow, startX, startY, dx, dy, angle, vel) == null) {
					traj.set(i, trajLow);
					continue outout;
				}
				
			}
		
			
			outer:
			for (int vv1 = 1; vv1 <= 10; vv1++) {
				double v1 = vv1/2 * ((vv1&1) == 1 ? 1 : -1);
				for (int vv2 = 1; vv2 <= 10; vv2++) {
					double v2 = vv2/2 * ((vv2&1) == 1 ? 1 : -1);
					
					int dx = (int) (targetX + (vec1.nX()*v1+vec2.nX()*v2)*C.TILE_SIZE);
					int dy = (int) (targetY + (vec1.nY()*v1+vec2.nY()*v2)*C.TILE_SIZE);
					if (isEnemy(div, dx, dy) && SProjectiles.problem(div.army(), trajLow, startX, startY, dx, dy, angle, vel) == null) {
						traj.set(i, trajLow);
						break outer;
					}
				}
				
			}
			
			
		}
		
		if (traj.targets > 0 || hasCounters)
			return true;
		return false;
		
		
		
	}

	private static boolean isEnemy(Div div, int x, int y) {
		int tx = x >> C.T_SCROLL;
		int ty = y >> C.T_SCROLL;
		if (SETT.IN_BOUNDS(tx, ty)) {
			if (BattleStatus.map().hasEnemy.is(tx, ty, div.army()))
				return true;
		}
		return false;
	}

}
