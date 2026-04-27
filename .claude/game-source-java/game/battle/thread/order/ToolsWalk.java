package game.battle.thread.order;

import static game.battle.thread.order.BattleOrderUpdater.Plan.a;
import static game.battle.thread.order.BattleOrderUpdater.Plan.current;
import static game.battle.thread.order.BattleOrderUpdater.Plan.dest;
import static game.battle.thread.order.BattleOrderUpdater.Plan.div;
import static game.battle.thread.order.BattleOrderUpdater.Plan.order;
import static game.battle.thread.order.BattleOrderUpdater.Plan.path;
import static game.battle.thread.order.BattleOrderUpdater.Plan.prev;

import game.battle.div.Div;
import game.battle.formation.DivFormation;
import game.battle.formation.DivFormationImp;
import game.battle.formation.DivPlacability;
import game.battle.thread.order.BattleOrderUpdater.Plan;
import init.constant.C;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import snake2d.PathGame;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.VectorImp;

final class ToolsWalk {

	private final VectorImp vec = new VectorImp();
	private final VectorImp vec2 = new VectorImp();
	private final Tools t;

	public static final int destMoveStart = 90;
	public static final int destMoveResume = 80;

	ToolsWalk(Tools t) {
		this.t = t;
	}

	public boolean setStart(int tilesCheckDest) {

		PathTile c = getStart();
		
		if (c == null)
			return false;

		int sx = (c.x() << C.T_SCROLL) + C.TILE_SIZEH;
		int sy = (c.y() << C.T_SCROLL) + C.TILE_SIZEH;

		//if sx is close to prev, and prev is coherent, use prev maybe

		if (c.getParent() != null) {
			PathTile p = c.getParent();
			c.parentSet(null);
			c = reverse(c, p);
		}

		if (c.getParent() != null)
			c = c.getParent();


		
		pp.clear();
		pp.set(c);
		
		
		path.clear();
		COORDINATE dest = t.div.getSafeCentrePixel(Plan.dest);
		int destX = dest.x() >> C.T_SCROLL;
		int destY = dest.y() >> C.T_SCROLL;
		path.init(sx, sy, pp, destX, destY, t.pathCost, a, div.race());
		order.path.set(path);

		if (path.isDest()) {
			
			return false;
		}
		boolean b = setStartPosition(tilesCheckDest);
		return b;

	}
	
	private PathTile reverse(PathTile newParent, PathTile t) {
		if (t.getParent() == null) {
			t.parentSet(newParent);
			return t;
		}
		PathTile res = reverse(t, t.getParent());
		t.parentSet(newParent);
		return res;

	}

	private PathTile getStart() {
		
		if (dest.deployed() <= 0 || dest.centreTile() == null || current.deployed() <= 0) {
			return null;
		}
		
		int destX = dest.centreTile().x();
		int destY = dest.centreTile().y();
		
		if (!SETT.IN_BOUNDS(destX, destY))
			return null;
		
		COORDINATE pp = t.div.getSafeCentrePixel(prev);

		Flooder f = t.pather.getFlooder();
		f.init(this);

		for (int i = 0; i < current.deployed(); i++) {
			if (div.reporter.reachable(i)) {
				double dist = 0;
				if (pp != null) {
					dist = pp.tileDistanceTo(current.pixel(i));
					dist *= C.ITILE_SIZE;
				}
				f.pushSloppy(current.tile(i), dist);
				f.setValue2(current.tile(i), dist);
			}
		}

		if (!f.hasMore()) {
			for (int i = 0; i < current.deployed(); i++) {
				double dist = 0;
				if (pp != null) {
					dist = pp.tileDistanceTo(current.pixel(i));
					dist *= C.ITILE_SIZE;
				}
				f.pushSloppy(current.tile(i), dist);
				f.setValue2(current.tile(i), dist);
			}
		}

		while (f.hasMore()) {
			PathTile t = f.pollSmallest();
			if (t.isSameAs(destX, destY)) {
				f.done();

				return startReturn(t);

			}
			for (int i = 0; i < DIR.ALL.size(); i++) {
				DIR d = DIR.ALL.get(i);
				int dx = t.x() + d.x();
				int dy = t.y() + d.y();
				if (SETT.IN_BOUNDS(dx, dy)) {
					double cost = this.t.pathCost.cost(t.x(), t.y(), d);
					if (cost < 0)
						continue;
					cost *= DIR.ALL.get(i).tileDistance();

					double di = COORDINATE.tileDistance(dx, dy, destX, destY);
					if (f.pushSmaller(dx, dy, t.getValue2() + cost + di, t) != null)
						f.setValue2(dx, dy, t.getValue2() + cost);

				}

			}
		}
		f.done();
		
		return null;

	}

	private PathTile startReturn(PathTile t) {
		if (t.getParent() != null) {
			PathTile p = t.getParent();
			t.parentSet(null);
			t = reverse(t, p);
		}

		if (t.getParent() != null)
			t = t.getParent();
		return t;
	}

	private boolean setStartPosition(int fDistance) {
		
		path.dCount = 0;
		if (!setNextPosition(fDistance, 0))
			return false;
		int bestI = -1;
		double bestDist = Integer.MAX_VALUE;

		for (int i = 0; i < current.deployed(); i++) {
			if (Plan.div.reporter.reachable(i)) {
				double dist = current.pixel(i).tileDistanceTo(path.x(), path.y());
				
				if (dist < bestDist) {
					bestDist = dist;
					bestI = i;
				}

			}
		}
		

		if (bestI >= 0) {
			bestDist = Integer.MAX_VALUE;
			int bestPos = -1;
			for (int i = 0; i < prev.deployed() && i < current.deployed(); i++) {
				double dist = prev.pixel(i).tileDistanceTo(current.pixel(bestI));
				if (dist < bestDist) {
					bestDist = dist;
					bestPos = i;
				}
			}
			
			if (bestPos >= 0) {
				prev.swap(bestPos, bestI);
				
			}
			
			
		}
		
		return true;
	}

	public boolean setNextPosition(int fDistance, int millis) {



		DivFormationImp pos = getNextPosition(fDistance, millis);

		if (pos == null) {
			return false;
		}
		
		Plan.nextPos = pos;
		return true;
	}

	private DivFormationImp getNextPosition(int fDistance, int millis) {
		// check if collisions ahead
		{
			int ii = path.currentI();
			for (int i = 0; i < 7; i++) {
				if (SETT.PATH().solidity.is(path.x() >> C.T_SCROLL, path.y() >> C.T_SCROLL)) {
					path.setCurrentI(ii);
					return null;
				}
				if (path.isDest())
					break;
				path.currentIInc(1);
			}
			path.setCurrentI(ii);
		}

		int prevX = path.x();
		int prevY = path.y();
		path.currentIInc(1);
		int cx = path.x();
		int cy = path.y();

		double m = vec.set(prevX, prevY, cx, cy);
		
		double old = path.dCount;
		path.dCount += millis;
		double dd = path.dCount-old;
		if (dd > 0 && path.dCount >= 0 && path.dCount < m) {
			path.currentIInc(-1);
		
			int dx = (int)(vec.nX() * (dd));
			int dy = (int) (vec.nY() * (dd));			
			
			if (tryStep(dx, dy)) {
				order.path.set(path);
				return prev;
			}
		} else {
			path.dCount = 0;
		}
		
		order.path.set(path);

		{
			int dx = (int) (vec.nX()*m);
			int dy = (int) (vec.nY()*m);
			vec.rotate90();
			
			if (prev.deployed() == Plan.men && vec.nX()*prev.dx() > 0.9 && vec.nY()*prev.dy() > 0.9) {
				if (tryStep(dx, dy)) {
					return prev;
				}
			}
			
		}
		
		
		
		
		int w = getWidth(fDistance);

		double nX = vec.nX();
		double nY = vec.nY();

		DivFormationImp pos = t.deployer.deployCentre(div.info, Plan.men, dest.formation(), cx, cy, nX, nY, w, a);
		
		if (pos == null) {
			return null;
		}
		prev.copy(t.mover.getFromMovedIntoTo(prev, pos));
		return prev;
	}

	private int getWidth(int fDistance) {
		if (path.tilesToDest() <= fDistance && canMoveAllTheWayToDest()) {
			return dest.width() / (dest.formation().size(div));
		}
		Div o = div.status().enemyClosest();
		if (o != null) {
			DivFormation other = o.position();
			if (other.deployed() > 0 && other.centrePixel() != null && prev.centrePixel() != null) {
				if (other.centrePixel().tileDistanceTo(prev.centrePixel()) < C.TILE_SIZE * 20)
					return dest.width() / (dest.formation().size(div));
			}
		}
		if (prev.deployed() > 0 && t.div.inPosition(current, prev, C.TILE_SIZE) > (Plan.men - Plan.unreachable) / 2) {
			if (Math.abs(dest.width() - prev.width()) < C.TILE_SIZE * 3) {
				return prev.width() / (prev.formation().size(div));
			}

		}
		int w = (int) Math.ceil(Math.sqrt(Plan.men / 2.0));
		if (w % 2 == 0)
			w++;
		return w;
	}
	
	private boolean tryStep(int dx, int dy) {
		
		
		for (int i = 0; i < prev.deployed(); i++) {
			int x = prev.pixel(i).x()+dx;
			int y = prev.pixel(i).y()+dy;
			int tx = x >>C.T_SCROLL;
			int ty = y >>C.T_SCROLL;
			AVAILABILITY av = SETT.PATH().availability.get(tx, ty);
			if (av != null && SETT.PATH().availability.get(tx, ty).isSolid(a)) {
				return false;
			}
		}
		
		prev.move(dx, dy);
		return true;
	}
	
	public boolean canMoveAllTheWayToDest() {

		if (path.length() == 0)
			return true;
		
		int pi = path.currentI();
		while (!path.isDest()) {
			if (!checkStep(path.x(), path.y())) {
				path.setCurrentI(pi);
				return false;
			}
			path.currentIInc(1);
		}

		if (path.isComplete()) {
			path.setCurrentI(pi);
			return true;
		}

		int sx = path.x();
		int sy = path.y();

		path.setCurrentI(pi);
		return canMoveAllTheWayToDest(sx, sy);
	}

	private boolean canMoveAllTheWayToDest(int sx, int sy) {
		int dx = dest.centrePixel().x();
		int dy = dest.centrePixel().y();
		double m = vec2.set(sx, sy, dx, dy);
		int steps = (int) Math.ceil(m / C.TILE_SIZE);
		for (int i = 0; i < steps; i++) {
			int tx = ((int) (sx + vec2.nX() * i * C.TILE_SIZE));
			int ty = ((int) (sy + vec2.nY() * i * C.TILE_SIZE));
			if (!checkStep(tx, ty))
				return false;
		}
		return true;
	}

	private boolean checkStep(int cx, int cy) {

		int size = dest.formation().size(div);
		double x1 = cx - dest.dx() * dest.width() / 2;
		double y1 = cy - dest.dy() * dest.width() / 2;
		int am = dest.width() / size;
		for (int i = 0; i < am; i++) {
			if (DivPlacability.pixelIsBlocked((int) x1, (int) y1, size, a))
				return false;
			x1 += dest.dx() * size;
			y1 += dest.dy() * size;
		}
		return true;
	}

	private final PathGame.PathFancy pp = new PathGame.PathFancy(1024 * 4);

	public boolean hasReachedPrev() {
		int am = t.walk.countPosition();
		int lim = (int) Math.ceil(0.9*(Plan.men-div.reporter.unreachable()));
		if (am == 0)
			return false;
		if (am < lim)
			return false;
		return true;
	}
	
	public int countPosition() {
		int dist = div.settings().running ? C.TILE_SIZE : C.TILE_SIZEH;
		return t.div.inPosition(current, prev, dist);
	}

	
	
}
