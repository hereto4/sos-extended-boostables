package game.battle.thread.general.offence;

import game.GAME;
import game.battle.div.Div;
import game.battle.formation.DIV_FORMATION;
import game.battle.formation.DivFormationImp;
import game.battle.thread.general.StrategosUtil;
import game.battle.thread.general.offence.ContextLines.Line;
import game.battle.thread.order.BattleOrderTask;
import game.battle.thread.order.BattleOrderTask.DIVTASK;
import init.constant.C;
import init.constant.Config;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.Bitmap2D;
import snake2d.util.sets.LIST;

/**
 * uses the prelines, and sets them to divs. 
 * @author Jake
 *
 */
class StepLinesMoveTo {
	
	private final Bitmap1D deployed;
	private final int[] dists;
	private final int[] distsToBlob;
	
	private final StrategosUtil util;
	private final int maxRange = 1000;
	private final Context c;
	private final ContextLines lines;
	private final Bitmap2D blob;
	private final Bitmap2D penalty;
	private final UtilDivMap map;
	private final DivDeploment[] dall = new DivDeploment[Config.battle().DIVISIONS_PER_ARMY];
	private final ArrayList<DivDeploment> toDeploy = new ArrayList<StepLinesMoveTo.DivDeploment>(dall.length);
	private final VectorImp vec = new VectorImp();
	
	public StepLinesMoveTo(StrategosUtil context, Context c) {
		this.c = c;
		this.util = context;
		this.lines = c.lines;
		this.map = c.map;
		this.blob = c.blob;
		for (int i = 0; i < dall.length; i++)
			dall[i] = new DivDeploment();
		this.deployed = c.deployedToLine;
		this.dists = c.distsToLine;
		this.distsToBlob = c.distsFromLineToBlob;
		penalty = c.block;
	}
	
	public void init() {
		for (int i = 0; i < lines.lines(); i++) {
			Line l = lines.get(i);
			l.back = 0;
		}
		map.clear();
		
		penalty.clear();
		Flooder f = util.flooder.getFlooder();
		f.init(this);
		for (int ty = 0; ty < SETT.THEIGHT; ty++) {
			for (int tx = 0; tx < SETT.TWIDTH; tx++) {
				if (blob.is(tx, ty))
					f.pushSloppy(tx, ty, 0);
			}
		}
		
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			if (t.getValue() >= 24) {
				break;
			}
			penalty.set(t, true);
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				int dx = t.x()+d.x();
				int dy = t.y()+d.y();
				if (!SETT.IN_BOUNDS(dx, dy))
					continue;
				f.pushSmaller(dx, dy, d.tileDistance()+t.getValue(), t);
				
			}
			
		}
		f.done();
	}
	
	public boolean deployDivsToLine() {

		fillLines(false);
		return deploy();
		
	}
	
	public boolean deployDivsToLineRanged() {

		fillLines(true);
		return deploy();
	}
	
	public boolean setSpeedAndFormation() {
		
		double dist = 0;
		int am = 0;
		int fighters = 0;
		double tot = 0;
		for (int i = 0; i < Config.battle().DIVISIONS_PER_ARMY; i++) {
			if (deployed.get(i)) {
				
				dist += dists[i];
				am++;
			}
			Div d = util.getArmy().divisions().get(i);
			int m = d.menNrOf();
			if (d.status().engagements() > 1)
				fighters += m;
			tot += m;
		}
	
		if (am == 0)
			return false;
		
		dist /= am;
		
		dist += 16;
		
		dist -= dist*(fighters/tot);
		
		for (int i = 0; i < Config.battle().DIVISIONS_PER_ARMY; i++) {
			if (deployed.get(i)) {
				Div d = util.getArmy().divisions().get(i);
				d.settings().running = dists[i] > dist;
			}
		}
		
		
		
		return true;
	}
	
	private boolean deploy() {
		
		if (toDeploy.size() == 0)
			return false;
		
		for (DivDeploment d : toDeploy) {
			
			if (d.dir == null) {
				if (d.l.deploy(util, d.div) != null) {
					deployed.set(d.div.indexArmy(), true);
					
				}else {
					d.l.back = -1;
				}
			}else {
				deployed.set(d.div.indexArmy(), true);
				DivFormationImp f = util.divDeployer.deployTile(d.div, d.cx, d.cy, d.dir);
				if (f != null) {
					for (int i = 0; i < f.deployed(); i++) {
						for (int di = 0; di < DIR.ORTHO.size(); di++) {
							DIR dir = DIR.ORTHO.get(di);
							penalty.set(f.tile(i).x()+8*dir.x(), f.tile(i).y()+8*dir.y(), true);
						}
						penalty.set(f.tile(i), true);
					}
				}
			}
			
			
		}
		
		return true;
	}
	
	private void fillLines(boolean ranged) {
		Flooder f = util.flooder.getFlooder();
		
		int ll = 0;
		
		
		
		f.init(this);
		for (int i = 0; i < lines.lines(); i++) {
			Line l = lines.get(i);
			if (l.back >= 0) {
				l.mark = 0;
				int cx = l.cx();
				int cy = l.cy();
				
				vec.set(l.dx, l.dy);
				vec.rotate90();
				
				cx += vec.nX()*l.back;
				cy += vec.nY()*l.back;
				cx /=C.TILE_SIZE;
				cy /=C.TILE_SIZE;
				int pe = penalty(l, ranged);
				
				if (SETT.IN_BOUNDS(cx, cy)) {
					f.pushSloppy(cx, cy, pe);
					f.setValue2(cx, cy, i);
					ll++;
					
				}
			}
		}
		
		if (ll == 0) {
			f.done();
			return;
		}
		
		toDeploy.clearSloppy();
		map.clear();
		
		int am = 0;
		
		for (int di = 0; di < Config.battle().DIVISIONS_PER_ARMY; di++) {
			Div d = util.getArmy().divisions().get(di);
			if (valid(d, ranged)) {
				map.add(d);
				am++;
			}
			
		}
		
		if (am == 0) {
			f.done();
			return;
		}

		while(f.hasMore()) {
			
			PathTile t = f.pollSmallest();
			if (t.getValue() > maxRange) {
				break;
			}
			Line l = lines.get((int) t.getValue2());
			if (l.mark != 0) {
				continue;
			}
				
			
			LIST<Div> ddd = map.get(t.x(), t.y());
			if (ddd.size() > 0) {
				
				for (Div m : ddd) {
					l.mark = 1;
					DivDeploment dd = dall[toDeploy.size()];
					init(m, l, t, dd, ranged);
					toDeploy.add(dd);
				}
			}
			
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR dir = DIR.ALL.get(di);
				int dx = t.x()+dir.x();
				int dy = t.y()+dir.y();
				if (SETT.IN_BOUNDS(dx, dy)) {
					double cost = cost(dx, dy);
					if (cost > 0) {
						if (f.pushSmaller(dx, dy, t.getValue()+dir.tileDistance()*cost, t) != null) {
							f.setValue2(dx, dy, t.getValue2());
						}
					}
				}
				
			}
		}

		
		f.done();
	}
	
	private int penalty(Line l, boolean ranged) {
		return (int) ((l.back*C.ITILE_SIZE)*(1 + (ranged ? 1*c.flanking : 2*c.flanking)));
	}
	
	private void init(Div div, Line line, PathTile t, DivDeploment dd, boolean ranged) {
		
		dd.dir = null;
		dd.div = div;
		dd.l = line;
		
		
		double dist = t.getValue()-penalty(line, ranged);
		
		
		PathTile p = t;
		while(p.getParent() != null) {
			if (penalty.is(p))
				dist -= 10*DIR.get(p, p.getParent()).tileDistance();
			p = p.getParent();
		}
		dists[div.indexArmy()] =  (int) dist;
		distsToBlob[div.indexArmy()] = (int) (line.blobID*C.ITILE_SIZE);
		DIV_FORMATION f = DIV_FORMATION.LOOSE;
		
		PathTile start = t;
		t = setDest(t);
		
		if (t.getParent() == null) {
			
			t = avoidWalkingThroughBlobDest(div, start);
			if (t != null && t.getParent() != null) {
				dd.cx = t.x();
				dd.cy = t.y();
				dd.dir = DIR.get(t, t.getParent());
			}else if (dists[div.indexArmy()] + line.blobID*C.ITILE_SIZE < 32)
				f = DIV_FORMATION.TIGHT;
			
		}else {
			
			dd.cx = t.x();
			dd.cy = t.y();
			dd.dir = DIR.get(t, t.getParent());
			
			
		}
		
		div.settings().formation = f;
		
	}
	

	private final BattleOrderTask task = new BattleOrderTask();
	
	private boolean valid(Div d, boolean ranged) {
		if (!d.active())
			return false;
		if (deployed.get(d.indexArmy()))
			return false;
		if (ranged && d.settings().ammo() == null)
			return false;
		if (!ranged && d.settings().ammo() != null)
			return false;
		
		d.order().task.get(task);
		
		if (task.task() != DIVTASK.MOVE && task.task() != DIVTASK.STOP)
			return false;
		
		return true;
	}
	
	private PathTile avoidWalkingThroughBlobDest(Div d, PathTile start) {
		
		int rewind = 1;
		PathTile t = start;
		
		outer:
		while(t != null) {
			
			if (rewind ++ %14 == 0) {
				double l = vec.set(t, start);
				
				for (int i = 0; i < l; i++) {
					int tx = (int) (t.x() + vec.nX()*i);
					int ty = (int) (t.y() + vec.nY()*i);
					if (blob.is(tx, ty))
						break outer;
				}
				
			}			
			
			t = t.getParent();
		}
		
		if (t == null)
			return null;
		
		t = start;
		
		rewind -= 14;
		
		while(t != null) {
			
			if (rewind -- <=0) {
				return t;
			}			
			
			t = t.getParent();
		}	
		
		return t;
		
	}
	
	private double cost(int dx, int dy) {
		if (blob.is(dx, dy))
			return -1;
		if (penalty.is(dx, dy))
			return 10;
		
		AVAILABILITY a = SETT.PATH().availability.get(dx, dy);
		if (a.isSolid(util.getArmy())) {
			return 1 + GAME.ARMIES().map.strength.get(dx, dy)/(C.TILE_SIZE*10);
		}else {
			
			double res = a.movementSpeedI;//ArmyAIUtil.map().hasEnemy.is(dx, dy, c.army) ? 1 : 10;
			double s = SETT.ENV().map.SPACE.get(dx, dy);
			if (s < 0.5)
				return res*=2;
			return res;
		}
	}


	

	
	private PathTile setDest(PathTile start) {
		
		if (start.getParent() == null) {
			return start;
		}
		
		while(start.getParent() != null) {	
			
			
			if ((SETT.PATH().availability.get(start).isSolid(util.getArmy()))) {
				return start;
			}
			start = start.getParent();
		}
		return start;
	}
	
	private static class DivDeploment {
		
		Div div;
		Line l;
		DIR dir;
		int cx;
		int cy;
	}
	

}