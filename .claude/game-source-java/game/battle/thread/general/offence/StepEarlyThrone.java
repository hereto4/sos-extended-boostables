package game.battle.thread.general.offence;

import game.battle.div.Div;
import game.battle.thread.general.StrategosUtil;
import init.constant.Config;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.main.throne.THRONE;
import settlement.tilemap.terrain.TFortification;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

final class StepEarlyThrone {
	
	private final StrategosUtil util;
	private final Context context;
	private final ArrayList<Dep> dall = new ArrayList<Dep>(Config.battle().DIVISIONS_PER_ARMY);
	private final ArrayList<Dep> deps = new ArrayList<Dep>(Config.battle().DIVISIONS_PER_ARMY);
	public StepEarlyThrone(StrategosUtil util, Context context) {
		this.util = util;
		this.context = context;
		while(dall.hasRoom())
			dall.add(new Dep());
	}
	
	public void se2tToThrone() {
		
		if (context.blob.is(THRONE.coo()))
			return;
		
		context.map.clear();
		deps.clearSloppy();
		context.block.clear();
		
		boolean has = false;
		
		for (int di = 0; di < Config.battle().DIVISIONS_PER_ARMY; di++) {
			Div d = util.getArmy().divisions().get(di);
			if (d.active() && !context.deployedToLine.get(di) && !d.status().isFighting()) {
				context.map.add(d);
				has = true;
			}
		}
		
		if (!has)
			return;
		
		Flooder f = util.flooder.getFlooder();
		f.init(this);
		for (int i = 0; i < DIR.ORTHO.size(); i++)
			f.pushSloppy(THRONE.coo(), DIR.ORTHO.get(i), 0);
		
		
		
		

		while(f.hasMore()) {
			
			PathTile t = f.pollSmallest();
			
			LIST<Div> divs = context.map.get(t.x(), t.y());
			if (divs.size() > 0 && isUnblobbed(t)) {
				for (Div div : divs) {
					preDeploy(t, div);
				}
				
				
			}
			
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR dir = DIR.ALL.get(di);
				int dx = t.x()+dir.x();
				int dy = t.y()+dir.y();
				if (SETT.IN_BOUNDS(dx, dy)) {
					double cost = cost(util, dx, dy);
					if (cost > 0 && !dir.isOrtho()) {
						cost = Math.min(cost, cost(util, dx, t.y()));
						cost = Math.min(cost, cost(util, t.x(), dy));
					}
					if (cost > 0) {
						f.pushSmaller(dx, dy, t.getValue() + dir.tileDistance()*cost, t);
					}
				}
				
			}
		}
		f.done();
		
		int am = 0;
		
		for (Dep dep : deps) {
			Div div = util.getArmy().divisions().get(dep.di);
			if (util.divDeployer.deployTile(div, dep.tx, dep.ty, dep.d) != null) {
				am++;
				context.deployedToLine.set(div.indexArmy(), true);
				if (am > 3)
					break;
			}
			
			
		}
		
	}
	
	private final Rec tiles = new Rec();
	
	private void preDeploy(PathTile t, Div div) {
		
		while(t.getParent() != null && !context.block.is(t.getParent())) {
			t = t.getParent();
		}
		if (context.block.is(t))
			return;
		
		Dep res = dall.get(deps.size());
		res.di = div.indexArmy();
		res.tx = t.x();
		res.ty = t.y();
		res.d = DIR.get(THRONE.coo(), t);
		deps.add(res);
		
		int w = (int) (Math.sqrt(div.men())+2);
		tiles.setDim(w);
		tiles.moveC(t);
		
		for (COORDINATE c : tiles) {
			context.block.set(c, true);
		}
		
		
	}

	private boolean isUnblobbed(PathTile t) {
		while(t != null) {
			if (context.blob.is(t))
				return false;
			t = t.getParent();
		}
		return true;
	}
	
	public static double cost(StrategosUtil context, int dx, int dy) {
		
		AVAILABILITY a = SETT.PATH().availability.get(dx, dy);
		if (a.isSolid(context.getArmy()) || SETT.TERRAIN().get(dx, dy) instanceof TFortification.Tile) {
			return -1;
		}else {
			double res = 1;//ArmyAIUtil.map().hasEnemy.is(dx, dy, c.army) ? 1 : 10;
			double s = SETT.ENV().map.SPACE.get(dx, dy);
			if (s < 0.5)
				return res + 2 + a.movementSpeedI;
			return res + a.movementSpeedI;
		}
	}
	
	private class Dep {
		
		int di;
		int tx;
		int ty;
		DIR d;
		
	}
	




	
		
}
