package game.battle.thread.general.offence;

import java.util.Arrays;

import game.GAME;
import game.battle.div.Div;
import game.battle.formation.DIV_FORMATION;
import game.battle.thread.general.StrategosUtil;
import game.battle.thread.order.BattleOrderTask;
import game.battle.thread.order.BattleOrderTask.DIVTASK;
import game.battle.thread.status.BattleStatus;
import init.constant.C;
import init.constant.Config;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.tilemap.terrain.TFortification;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.DIR;

final class StepAttackEnemyNear {
	
	private final StrategosUtil util;
	private final Context context;
	
	public StepAttackEnemyNear(StrategosUtil context, Context c) {
		this.util = context;
		this.context = c;
	}
	
	private int[] attacked = new int[Config.battle().DIVISIONS_PER_ARMY];
	
	public boolean attackEnemies() {
		
		
		
		
		
		context.map.clear();
		
		boolean has = false;
		
		for (int di = 0; di < Config.battle().DIVISIONS_PER_ARMY; di++) {
			Div d = util.getArmy().divisions().get(di);
			if (d.active() && !context.deployedToLine.get(di)) {
				
				d.order().task.get(task);
				
				if (task.task() == DIVTASK.CHARGE)
					continue;
				
				if (d.status().engagements() > Math.sqrt(d.menNrOf())*0.5) {
					
					react(d);
					continue;
				}
				context.map.add(d);
				has = true;
			}
			
		}
	
		if (!has)
			return false;
		
		Flooder f = util.flooder.getFlooder();
		f.init(this);
		Arrays.fill(attacked, 0);
		
		for (int di = 0; di < Config.battle().DIVISIONS_PER_ARMY; di++) {
			Div d = util.getArmy().enemy().divisions().get(di);
			if (d.active()) {
				int tx = d.centre().ctX();
				int ty = d.centre().ctY();
				if (SETT.IN_BOUNDS(tx, ty)) {
					f.pushSloppy(tx, ty, 0);
					f.setValue2(tx, ty, d.indexArmy());
				}
			}
		}
		
		while(f.hasMore()) {
			
			PathTile t = f.pollSmallest();
			
			if (!context.blob.is(t))
				continue;
			
			Div enemy = util.getArmy().enemy().divisions().get((int) t.getValue2());
			
			if (attacked[enemy.indexArmy()] >= enemy.menNrOf()*4) {
				continue;
			}
			
			if (t.getValue() > 48)
				break;

			for (Div d : context.map.get(t.x(), t.y())) {
				attack(enemy, d);
			}
			
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR dir = DIR.ALL.get(di);
				int dx = t.x()+dir.x();
				int dy = t.y()+dir.y();
				if (SETT.IN_BOUNDS(dx, dy)) {
					double cost = cost(util, dx, dy)*(1+attacked[enemy.indexArmy()]/(1+enemy.menNrOf()));
					
					if (cost > 0) {
						if (!dir.isOrtho()) {
							cost = Math.max(cost, cost(util, dx, t.y()));
							cost = Math.max(cost, cost(util, t.x(), dy));
						}
						if (f.pushSmaller(dx, dy, t.getValue() + dir.tileDistance()*cost, t) != null) {
							f.setValue2(dx,  dy, t.getValue2());
						}
					}
				}
				
			}
		}
		f.done();
		return false;
	}
	
	private final BattleOrderTask task = new BattleOrderTask();


	public static double cost(StrategosUtil context, int dx, int dy) {
		
		AVAILABILITY a = SETT.PATH().availability.get(dx, dy);
		if (a.isSolid(context.getArmy()) || SETT.TERRAIN().get(dx, dy) instanceof TFortification.Tile) {
			return 3 + GAME.ARMIES().map.strength.get(dx, dy)/(C.TILE_SIZE*10);
		}else {
			
			double res = 1;//ArmyAIUtil.map().hasEnemy.is(dx, dy, c.army) ? 1 : 10;
			double s = SETT.ENV().map.SPACE.get(dx, dy);
			if (s < 0.5)
				return res + 2 + a.movementSpeedI;
			return res + a.movementSpeedI;
		}
	}

	private int[] counts = new int[Config.battle().DIVISIONS_PER_ARMY];
	
	private void react(Div d) {
		
		Arrays.fill(counts, 0);
		
		Div ee = null;
		int max = 0;
		int tot = 0;
		
		context.deployedToLine.set(d.indexArmy(), true);
		
		for (int i = 0; i < d.current().deployed(); i++) {
			int tx = d.current().tx(i);
			int ty = d.current().ty(i);
			Div e = BattleStatus.map().getEnemySingle(tx, ty, util.getArmy());
			if (e != null) {
				counts[e.indexArmy()]++;
				max++;
				if (ee == null || counts[e.indexArmy()] > max) {
					ee = e;
				}
			}
			
		}

		if (ee != null && counts[ee.index()] > tot/2) {
			d.settings().guard = false;
			d.settings().formation = DIV_FORMATION.TIGHT;
			attack(ee, d);
		}else {
			d.settings().guard = true;
			d.settings().formation = DIV_FORMATION.TIGHT;
			task.stop(d);
			d.order().task.set(task);
		}
		
		
		
	}
	
	private void attack(Div d, Div mDiv) {

		if (mDiv.settings().ammo() != null) {
			task.attackRanged(d, mDiv);
		}else
			task.attackMelee(d, mDiv);
		
		mDiv.order().task.set(task);
		context.deployedToLine.set(mDiv.indexArmy(), true);
		attacked[d.indexArmy()] += d.menNrOf();
		mDiv.settings().formation = d.settings().ammo() == null ? DIV_FORMATION.TIGHT : DIV_FORMATION.LOOSE;
	}
		
}
