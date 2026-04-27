package game.battle.thread.status;

import java.util.Arrays;

import game.GAME;
import game.battle.div.Div;
import game.battle.formation.DivPosition;
import game.battle.formation.DivPositionCopyable;
import init.constant.C;
import init.constant.Config;
import settlement.main.SETT;
import settlement.room.military.artillery.ArtilleryInstance;
import settlement.room.military.artillery.ROOM_ARTILLERY;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListResize;
import snake2d.util.sets.MapInt;

final class Updater {

	volatile boolean stop = false;
	private final ArrayList<Div> list = new ArrayList<>(Config.battle().DIVISIONS_PER_ARMY);
	private final Surrounder surrounder = new Surrounder();
	private final Flanker flanker = new Flanker();
	
	Updater(){

	}
	
	void init(BattleContext u) {
		
		u.map.clear();
		u.quads.clear();
		u.space.clear();
		for (DivStatus s : u.statuses) {
			s.clear();
		}
		
		addToMaps(u);
		setStats(u);
		addArtillery(u);
		
	}

	
	private void addToMaps(BattleContext u) {
		for (short i = 0; i < u.statuses.length; i++) {
			if (stop)
				return;
			Div d = GAME.ARMIES().division(i);
			if (d.menNrOf() == 0)
				continue;
			DivPositionCopyable pos = d.current();
			if (pos.deployed() == 0)
				continue;
			
			u.map.add(i, pos);
			u.space.add(d, pos);
			u.army.add(d, pos);
			u.quads.add(d, d.centre().cUnitX(), d.centre().cUnitY());
		}
	}


	
	private void setStats(BattleContext u) {
		
		for (short i = 0; i < u.statuses.length; i++) {
			
			if (stop)
				return;
			Div d = GAME.ARMIES().division(i);
			if (d.menNrOf() == 0)
				continue;
			DivPosition pos = d.current();
			if (pos.deployed() == 0) {
				continue;
			}
			
			DivStatus s = u.statuses[i];
			
			double friends = d.settings().getPower();
			
			
			list.clear();
			
			double distMax = 300*C.TILE_SIZE;
			surrounder.init();
			u.quads.getNearest(list, d.centre().cUnitX(), d.centre().cUnitY(), (int)distMax, d.armyEnemy(), d);
			
			double distMin = distMax*0.5;
			byte threatDirs = 0;
			double enemyThreats = 0;
			int k = 0;
			for (Div e : list) {
			
				
				int dx = e.centre().cUnitX()-d.centre().cUnitX();
				int dy = e.centre().cUnitY()-d.centre().cUnitY();;
				double dist = Math.sqrt(dx*dx+dy*dy);
				if (dist > distMax)
					continue;
				
				double threat = Math.max(e.settings().getPower()*(1.0 - dist/distMax), 0);
				
				surrounder.add(e.centre().cUnitX(), e.centre().cUnitY(), threat);
				enemyThreats += threat;
				DIR dir = DIR.get(dx, dy);
				if (dist < distMin) {
					
					if (dir.isOrtho())
						threatDirs |= dir.mask();
					else
						threatDirs |= dir.mask()<<4;
					
				}
				if (k < DivStatus.iSize) {
					s.enemiesClosestSet(e.index(), (int) (Math.sqrt(dist)*C.ITILE_SIZE));
					k++;
				}
				
			}
			
			double encirclement = surrounder.getValue(d.centre().cUnitX(), d.centre().cUnitY());
			
			s.encirclement = encirclement;
					
					
			s.enemyDirMask = threatDirs;
			s.enemyThreats = enemyThreats;
			
			
			
			list.clearSloppy();
			u.quads.getNearest(list, d.centre().cUnitX(), d.centre().cUnitY(), (int)distMax, d.army(), d);
			k =  0;
			for (int ii = 0; ii < list.size(); ii++) {
				Div e = list.get(ii);
				s.friendlyClosestSet(e.index());
				
				int dx = e.centre().cUnitX()-d.centre().cUnitX();
				int dy = e.centre().cUnitY()-d.centre().cUnitY();;
				double dist = Math.sqrt(dx*dx+dy*dy);
				
				if (dist < distMax) {
					friends += Math.max(e.settings().getPower()*(1.0 - dist/distMax), 0);
				}
				
				
			}
			s.friends = friends;

			
			
			s.flanks = flanker.get2(u, d, pos);
			
			
		}
	}

	private final ArrayListResize<ArtilleryInstance> arts = new ArrayListResize<>(256);
	
	private void addArtillery(BattleContext u) {
		
		for (int bi = 0; bi < SETT.ROOMS().ARTILLERY.size(); bi++) {
			ROOM_ARTILLERY ab = SETT.ROOMS().ARTILLERY.get(bi);
			arts.clearSoft();
			ab.threadInstances(arts);
			for (ArtilleryInstance ins : arts) {
				u.quads.addArtillery(ins);
			}
		}
	}
	
	private static class Flanker {
		
		private final VectorImp vec = new VectorImp();
		int size = Math.max(Config.battle().DIVISIONS_PER_ARMY, Config.battle().MEN_PER_DIVISION);
		private final int[] engagedX = new int[size];
		private final int[] engagedY = new int[size];
		
		public double get(BattleContext u, Div d, DivPosition pos) {

			int deployed = pos.deployed();
			if (deployed == 0)
				return 0;
			int centreX = 0;
			int centreY = 0;
			int soldiersTotal = 0;
			int engaged = 0;
			
			for (int pi = 0; pi < pos.deployed(); pi++) {
				int px = pos.px(pi);
				int py = pos.py(pi);
				if (enemy(u, d, px, py)) {
					engagedX[engaged] = px;
					engagedY[engaged] = py;
					engaged++;
					centreX += px;
					centreY += py;
					soldiersTotal++;
				}else if (d.reporter.reachable(pi)){
					centreX += px;
					centreY += py;
					soldiersTotal++;
				}
				
			}
			
			if (engaged == 0)
				return 0;
			

			centreX /= soldiersTotal;
			centreY /= soldiersTotal;
			
			double generalEngageDirX = 0;
			double generalEngageDitY = 0;
			
			for (int i = 0; i < engaged; i++) {
				vec.set(engagedX[i]-centreX,engagedY[i]-centreY);
				generalEngageDirX += vec.nX();
				generalEngageDitY += vec.nY();
			}
			
			vec.set(generalEngageDirX, generalEngageDitY);
			generalEngageDirX = vec.nX();
			generalEngageDitY = vec.nY();
			
			
			double flanks = 0;
			for (int pi = 0; pi < engaged; pi++) {
				int px = engagedX[pi];
				int py = engagedY[pi];
				
				double engagedDX = 0;
				double engagedDY = 0;
				for (int di = 0; di < DIR.ALL.size(); di++) {
					DIR dir = DIR.ALL.get(di);
					int tx = (int) ((px + dir.xN()*C.TILE_SIZE)/C.TILE_SIZE);
					int ty = (int) ((py + dir.yN()*C.TILE_SIZE)/C.TILE_SIZE);
					if (u.map.hasEnemy.is(tx, ty, d.army())) {
						engagedDX += dir.xN();
						engagedDY += dir.yN();
					}
				}
				
				if (engagedDX == 0 && engagedDY == 0) {
					flanks++;
					continue;
				}
				
				vec.set(engagedDX, engagedDY);
				engagedDX = vec.nX();
				engagedDY = vec.nY();
				
				vec.set(centreX, centreY, px, py);
				
				if (vec.nX()*engagedDX + vec.nY()*engagedDY < 0) {
					continue;
				}
				
				double dot = generalEngageDirX*engagedDX + generalEngageDitY*engagedDY;
				if (dot < 0) {
					flanks += -dot;
				}
				
				
				
			}
			
			return flanks;
		}
		
		private int dirs[] = new int[DIR.ALL.size()];
		
		private final MapInt intmap = new MapInt();
		
		public double get2(BattleContext u, Div d, DivPosition pos) {

			int deployed = pos.deployed();
			if (deployed == 0)
				return 0;

			int centreX = 0;
			int centreY = 0;
			int soldiersTotal = 0;
			int engaged = 0;
			
			for (int pi = 0; pi < pos.deployed(); pi++) {
				int px = pos.px(pi);
				int py = pos.py(pi);
				if (enemy(u, d, px, py)) {
					engagedX[engaged] = px;
					engagedY[engaged] = py;
					engaged++;
					centreX += px;
					centreY += py;
					soldiersTotal++;
				}else if (d.reporter.reachable(pi)){
					centreX += px;
					centreY += py;
					soldiersTotal++;
				}
				
			}
			
			if (engaged == 0)
				return 0;
			

			centreX /= soldiersTotal;
			centreY /= soldiersTotal;
			
			intmap.clear();
			Arrays.fill(dirs, 0);
			
			for (int pi = 0; pi < pos.deployed(); pi++) {
				int px = pos.px(pi);
				int py = pos.py(pi);
				
				DIR dd = DIR.get(centreX, centreY, px, py);
				for (int di = -1; di <= 1; di++) {
					DIR dir = dd.next(di);
					int tx = (int) ((px + dir.xN()*C.TILE_SIZE)/C.TILE_SIZE);
					int ty = (int) ((py + dir.yN()*C.TILE_SIZE)/C.TILE_SIZE);
					if (SETT.IN_BOUNDS(tx, ty)) {
						int ii = tx+ty*SETT.TWIDTH;
						if (!intmap.contains(ii)) {
							intmap.add(ii);
							dirs[dir.id()] += u.map.soldiers(d.army().enemy()).get(tx, ty);							
						}
					}
					
				}
				
				
			}
			double res = 0;
			
			for (int d1 = 0; d1 < DIR.ALL.size(); d1++) {
				DIR da = DIR.ALL.get(d1);
				for (int d2 = d1+1; d2 < DIR.ALL.size(); d2++) {
					DIR bd = DIR.ALL.get(d2);
					double dot = da.xN()*bd.xN() + da.yN()*bd.yN();
					if (dot < 0) {
						res += Math.min(dirs[d1], dirs[d2])*(1-dot);
					}
				}
			}
			
			return res;
		}
		
		private boolean enemy(BattleContext u, Div div, int px, int py) {
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR dir = DIR.ALL.get(di);
				int tx = (int) ((px + dir.xN()*C.TILE_SIZE)/C.TILE_SIZE);
				int ty = (int) ((py + dir.yN()*C.TILE_SIZE)/C.TILE_SIZE);
				if (u.map.hasEnemy.is(tx, ty, div.army())) {
					return true;
				}
			}
			
			return false;
		}
		
	}
	
	
	private static class Surrounder {
		
		private final VectorImp vec = new VectorImp();
		int size = Math.max(Config.battle().DIVISIONS_PER_ARMY, Config.battle().MEN_PER_DIVISION);
		int current = 0;
		private final double[] dxs = new double[size];
		private final double[] dys = new double[size];
		private final double[] amounts = new double[size];
		
		public void init() {
			current = 0;
		}
		
		public void add(double px, double py, double amount) {
			
			dxs[current] = px;
			dys[current] = py;
			amounts[current] = amount;
			current++;
		}
		
		public double getValue(double cx, double cy) {
			if (current == 0)
				return 0;
			
			convertToVectors(cx, cy);
			
			double xs = 0;
			double ys = 0;
			double am = 0;
			for (int i = 0; i < current; i++) {
				xs += dxs[i]*amounts[i];
				ys += dys[i]*amounts[i];
				am += amounts[i];
			}
			
			if (am == 0)
				return 0;
			
			xs /= am;
			ys /= am;
			if (xs == 0 && ys == 0) {
				xs = dxs[0];
				ys = dys[0];
			}
			
			vec.set(xs, ys);
			xs = vec.nX();
			ys = vec.nY();
			
			double v = 0;
			
			for (int i = 0; i < current; i++) {
				double dot = dxs[i]*xs + dys[i]*ys;
				if (dot < -0.6) {
					dot = -dot;
					dot /= 0.4;
					
					
					v += dot*amounts[i];
				}
			}
			return v;
			
		}
		
		private void convertToVectors(double cx, double cy) {
			
			for (int i = 0; i < current; i++) {
				double dx = dxs[i]-cx;
				double dy = dys[i]-cy;
				vec.set(dx, dy);
				dx = vec.nX();
				dy = vec.nY();
				dxs[i] = vec.nX();
				dys[i] = vec.nY();
			}
		}
		
		
	}
	
	

	

}
