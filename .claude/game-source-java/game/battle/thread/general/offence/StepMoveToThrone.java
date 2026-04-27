package game.battle.thread.general.offence;

import game.GAME;
import game.battle.div.Div;
import game.battle.thread.general.StrategosUtil;
import game.battle.thread.status.BattleStatus;
import init.constant.C;
import init.constant.Config;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.tilemap.terrain.TFortification;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.Bitmap2D;

final class StepMoveToThrone {
	
	private final StrategosUtil util;
	private final Context context;
	private final Bitmap2D blocked;
	
	public StepMoveToThrone(StrategosUtil util, Context context) {
		this.util = util;
		this.context = context;
		blocked = context.block;
	}
	
	public void init() {
		blocked.clear();
	}
	
	public boolean setToThrone() {
		
		context.map.clear();
		
		int am = 0;
		
		for (int di = 0; di < Config.battle().DIVISIONS_PER_ARMY; di++) {
			Div d = util.getArmy().divisions().get(di);
			if (d.active() && !context.deployedToLine.get(di) && !d.status().isFighting()) {
				context.map.add(d);
				am++;
			}
			
		}
		
		if (am == 0)
			return false;
		
		Flooder f = util.flooder.getFlooder();
		f.init(this);
		f.pushSloppy(util.getDestCoo(), 0);
		

		while(f.hasMore()) {
			
			PathTile t = f.pollSmallest();
			
			
			for (Div m : context.map.get(t.x(), t.y())) {
				if (context.deployedToLine.get(m.indexArmy()))
					continue;
				
				context.deployedToLine.set(m.indexArmy(), true);
				f.done();
				
				PathTile dest = setDest(m, blocked, t);					
				block(t, dest, blocked);
				util.divDeployer.deployTile(m, res.destX, res.destY, res.destDir);
				return true;
			}
			
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR dir = DIR.ALL.get(di);
				int dx = t.x()+dir.x();
				int dy = t.y()+dir.y();
				if (SETT.IN_BOUNDS(dx, dy)) {
					double cost = cost(util, dx, dy);
					
					if (cost > 0) {
						if (!dir.isOrtho()) {
							cost = Math.max(cost, cost(util, dx, t.y()));
							cost = Math.max(cost, cost(util, t.x(), dy));
						}
						if (blocked.is(dx, dy)) {
							cost *= 4;
						}
						f.pushSmaller(dx, dy, t.getValue() + dir.tileDistance()*cost, t);
					}
				}
				
			}
		}
		f.done();
		return false;
	}
	
	
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
	
	private static void block(PathTile t, PathTile dest, Bitmap2D toBlock) {
		while(t != dest) {
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				toBlock.set(t, DIR.ORTHO.get(i), true);
			}
			t = t.getParent();
		}
	}

	
	private PathTile setDest(Div div, Bitmap2D blocked, PathTile t) {
		
		if (initBlocked(div, DIR.C, t.x(), t.y(), t))
			return t;
		
		if (t.getParent() == null) {
			res.destX = t.x();
			res.destY = t.y();
			res.destDir = DIR.C;
			return t;
		}
		
		PathTile prev = null;
		boolean b = true;
		while(t.getParent() != null) {	
			if (initBlocked(div, blocked, t))
				return t.getParent();
			if (!b && blocked.is(t.getParent())) {
				res.destX = t.x();
				res.destY = t.y();
				res.destDir = DIR.get(t, t.getParent());
				return t;
			}
			prev = t;
			b = false;
			t = t.getParent();
		}
		res.destX = t.x();
		res.destY = t.y();
		res.destDir = DIR.get(prev, t);
		return t;
	}
	
	private boolean initBlocked(Div div, Bitmap2D blocked, PathTile block) {
		if (initBlocked(div, DIR.get(block.getParent(), block), block.getParent().x(), block.getParent().y(), block))
			return true;
		if (initBlocked(div, DIR.get(block.getParent(), block), block.getParent().x(), block.y(), block))
			return true;
		if (initBlocked(div, DIR.get(block.getParent(), block), block.x(), block.getParent().y(), block))
			return true;
		return false;
	}
	
	private boolean initBlocked(Div div, DIR d, int dx, int dy, PathTile block) {
		if (SETT.PATH().availability.get(dx,dy).isSolid(div.army()) || BattleStatus.map().hasEnemy.is(dx, dy, util.getArmy())) {
			res.destX = dx;
			res.destY = dy;
			res.destDir = d;
			return true;
		}
		return false;
	}

	private final Res res = new Res();
	
	private class Res {
		int destX;
		int destY;
		DIR destDir;
		
	}

	
		
}
