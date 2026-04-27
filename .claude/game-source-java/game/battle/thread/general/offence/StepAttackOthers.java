package game.battle.thread.general.offence;

import game.battle.div.Div;
import game.battle.formation.DIV_FORMATION;
import game.battle.thread.general.StrategosUtil;
import game.battle.thread.order.BattleOrderTask;
import game.battle.thread.order.BattleOrderTask.DIVTASK;
import init.constant.Config;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.tilemap.terrain.TFortification;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.Bitmap1D;

final class StepAttackOthers {
	
	private final StrategosUtil util;
	private final Context context;
	private final Bitmap1D attacked = new Bitmap1D(Config.battle().DIVISIONS_PER_ARMY, false);
	
	public StepAttackOthers(StrategosUtil context, Context c) {
		this.util = context;
		this.context = c;
	}
	

	
	public void init() {
		for (int di = 0; di < Config.battle().DIVISIONS_PER_ARMY; di++) {
			Div d = util.getArmy().divisions().get(di);
			if (!valid(d)) {
				continue;
			}
			context.trickedDivs[di]++;
			context.deployedToLine.set(di, false);
		}
	}
	
	public boolean attack() {
		
		

		
		context.map.clear();
		
		
		int am = 0;
		for (int di = 0; di < Config.battle().DIVISIONS_PER_ARMY; di++) {
			Div d = util.getArmy().divisions().get(di);
			if (!valid(d)) {
				continue;
			}
			if (context.deployedToLine.get(di))
				continue;
			if (context.trickedDivs[di] < 4)
				continue;
			context.map.add(d);
			am++;
		}
		
		if (am == 0)
			return false;
		
		Flooder f = util.flooder.getFlooder();
		f.init(this);
		f.pushSloppy(util.getDestCoo(), 0);
		attacked.clear();
		for (int di = 0; di < Config.battle().DIVISIONS_PER_ARMY; di++) {
			Div d = util.getArmy().enemy().divisions().get(di);
			if (!d.active()) {
				continue;
			}
			int tx = d.centre().ctX();
			int ty = d.centre().ctY();
			if (SETT.IN_BOUNDS(tx, ty)) {
				f.pushSloppy(tx, ty, 0);
				f.setValue2(tx, ty, d.indexArmy());
			}
		}
		
		am = 0;
		while(f.hasMore()) {
			
			PathTile t = f.pollSmallest();
			
			if (t.getValue() > 100)
				break;

			if (t.getParent() != null)
				t.setValue2(t.getParent().getValue2());
			
			Div enemy = util.getArmy().enemy().divisions().get((int) t.getValue2());
			
			if (enemy == null)
				continue;
			
			if (attacked.get(enemy.indexArmy()))
				continue;
			
			for (Div d : context.map.get(t.x(), t.y())) {
				attack(d, enemy);
				am++;
				break;
			}
			
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR dir = DIR.ALL.get(di);
				int dx = t.x()+dir.x();
				int dy = t.y()+dir.y();
				if (SETT.IN_BOUNDS(dx, dy)) {
					double cost = cost(util, dx, dy);
					
					if (cost > 0) {
						if (!dir.isOrtho()) {
							cost = Math.min(cost, cost(util, dx, t.y()));
							cost = Math.min(cost, cost(util, t.x(), dy));
						}
						f.pushSmaller(dx, dy, t.getValue() + dir.tileDistance()*cost, t);
					}
				}
				
			}
		}
		f.done();
		return am > 0;
	}
	
	private final BattleOrderTask task = new BattleOrderTask();
	
	private void attack(Div mDiv, Div enemy) {
		if (mDiv.settings().ammo() != null) {
			task.attackRanged(enemy, mDiv);
		}else
			task.attackMelee(enemy, mDiv);
		
		mDiv.order().task.set(task);
		context.deployedToLine.set(mDiv.indexArmy(), true);
		attacked.set(enemy.indexArmy(), true);
		mDiv.settings().running = true;
		mDiv.settings().formation = enemy.settings().ammo() == null ? DIV_FORMATION.TIGHT : DIV_FORMATION.LOOSE;
	}


	public static double cost(StrategosUtil context, int dx, int dy) {
		
		AVAILABILITY a = SETT.PATH().availability.get(dx, dy);
		if (a.isSolid(context.getArmy()) || SETT.TERRAIN().get(dx, dy) instanceof TFortification.Tile) {
			return 1;
		}else {
			
			double res = 1;//ArmyAIUtil.map().hasEnemy.is(dx, dy, c.army) ? 1 : 10;
			double s = SETT.ENV().map.SPACE.get(dx, dy);
			if (s < 0.5)
				return res + 2 + a.movementSpeedI;
			return res + a.movementSpeedI;
		}
	}
	
	private boolean valid(Div d) {
		if (!d.active())
			return false;
		if (d.status().isFighting())
			return false;
		if (context.distsToLine[d.indexArmy()] + context.distsFromLineToBlob[d.indexArmy()] > 16)
			return false;
		
		d.order().task.get(task);
		
		if (task.task() != DIVTASK.STOP)
			return false;
		
		
		
		return true;
	}
	
	
		
}
