package game.battle.formation;

import static settlement.main.SETT.PATH;

import game.GAME;
import game.battle.Army;
import game.battle.div.Div;
import game.battle.util.DIV_SPEC;
import init.constant.C;
import init.race.Race;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import snake2d.LOG;
import snake2d.PathTile;
import snake2d.PathUtilOnline;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.misc.CLAMP;

public class DivDeployer {

	private final DivFormationImp position;
	private int[] backers = new int[251];
	private final PathUtilOnline flooder;
	
	
	public DivDeployer(PathUtilOnline f) {
		this.position = new DivFormationImp();
		this.flooder = f;
	}

	private final VectorImp vec = new VectorImp();

	public DivFormationImp deploy(DIV_SPEC div, int men, DIV_FORMATION f, int x1, int y1, double dx, double dy, int width, Army a) {

		position.clear();

		
		int tileSize = f.size(div);
		
		vec.set(dx, dy);

		double stepX = vec.nX() * tileSize;
		double stepY = vec.nY() * tileSize;

		DIR faceDir = vec.dir().next(-2);

		int steps = getSteps(width/tileSize, stepX, stepY, x1, y1, tileSize, a, div.race());
		if (steps == 0)
			return null;
		if (steps > men)
			steps = men;
		if (steps > 250)
			steps = 250;

		position.deployInit(faceDir, x1, y1, dx, dy, f, width);

		for (int i = 0; i < steps; i++) {

			int x = x1 + (int) (i * stepX);
			int y = y1 + (int) (i * stepY);
			position.deploy(x, y, div);
			men--;
		}

		vec.rotate90();

		double backX = vec.nX() * tileSize;
		double backY = vec.nY() * tileSize;

		int backI = steps;

		{
			int backK = 1;
			backers[0] = steps / 2;

			for (int i = 1; i <= steps / 2; i++) {
				backers[backK++] = steps / 2 - i;
				backers[backK++] = steps / 2 + i;

			}
		}

		
		int depth = 1;
		while (men > 0 && backI > 0 && depth < 50) {

			for (int i = 0; i < backI && men > 0; i++) {
				int p = backers[i];
				int x = x1 + (int) (p * stepX);
				int y = y1 + (int) (p * stepY);
				int fx = x + (int) (backX * (depth - 1));
				int fy = y + (int) (backY * (depth - 1));
				x += backX * depth;
				y += backY * depth;
				if (!DivPlacability.checkPixelStep(fx, fy, x, y, div.race(), a) || !isDeployable(x, y, a)) {
					for (int k = i + 1; k < backI; k++) {
						backers[k - 1] = backers[k];
					}
					backI--;
					i--;

				} else {
					position.deploy(x, y, div);
					men--;
				}

			}
			depth++;
		}
		
		for (int i = 0; i < backI; i++) {
			int p = backers[i];
			int x = x1 + (int) (p * stepX);
			int y = y1 + (int) (p * stepY);
			int fx = x + (int) (backX * (depth - 1));
			int fy = y + (int) (backY * (depth - 1));
			x += backX * depth;
			y += backY * depth;
			if (!DivPlacability.checkPixelStep(fx, fy, x, y, div.race(), a) || !isDeployable(x, y, a)) {
				
			} else {
				position.setHasExtraRoom();
				break;
			}

		}
		
		position.deployFinish(flooder.filler, div);
		return position;

	}
	
	public DivFormationImp deployArroundCentre(DIV_SPEC div, int men, DIV_FORMATION f, int x1, int y1, double dx, double dy, int width, Army a) {
		
		int bestX1 = -1;
		int bestY1 = -1;
		int best = 0;
		
		
		DivFormationImp res = deployCentre(div, men, f, x1, y1, dx, dy, width/f.size(div), a);
		if (res != null && res.deployed() == men) {
			if (res.deployed() == men) {
				return res;
			}else if (res.deployed() > best) {
				best = res.deployed();
				bestX1 = x1;
				bestY1 = y1;
			}
		}
		
		for (int i = 1; i < 7; i++) {
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(i);
				int x = (int) (x1 + d.xN()*C.TILE_SIZE*i);
				int y = (int) (y1 + d.yN()*C.TILE_SIZE*i);
				res = deployCentre(div, men, f, x, y, dx, dy, width/f.size(div), a);
				if (res == null)
					continue;
				if (res.deployed() == men) {
					return res;
				}else if (res.deployed() > best) {
					best = res.deployed();
					bestX1 = x1;
					bestY1 = y1;
				}
			}
			
		}
		
		if (best > 0)
			return deployCentre(div, men, f, bestX1, bestY1, dx, dy, width/f.size(div), a);
		return null;
	}
	
	protected boolean isDeployable(int px, int py, Army a) {
		return true;
	}
	
	public DivFormationImp move(DIV_SPEC div, DivFormationImp f, int dx, int dy, Army a) {
		if (position == f)
			throw new RuntimeException();
		position.deployInit(f.dir(), f.start().x()+dx, f.start().y()+dy, f.dx(), f.dy(), f.formation(), f.width());
		for (int i = 0; i < f.deployed(); i++) {
			int x = f.pixel(i).x()+dx;
			int y = f.pixel(i).y()+dy;
			int tx = x >>C.T_SCROLL;
			int ty = y >>C.T_SCROLL;
			AVAILABILITY av = SETT.PATH().availability.get(tx, ty);
			if (av != null && SETT.PATH().availability.get(tx, ty).isSolid(a)) {
				if (!GAME.ARMIES().map.attackable.is(tx, ty, a)) {
					position.deploy(f.pixel(i).x(), f.pixel(i).y(), div);
					continue;
				}
			}
			position.deploy(x, y, div);
		}
		position.deployFinish(flooder.filler, div);
		return position;
	}
	
	public boolean canMove(DIV_SPEC div, DivFormationImp f, double dx, double dy, Army a) {
		
		for (int i = 0; i < f.deployed(); i++) {
			int x = (int) (f.pixel(i).x()+dx);
			int y = (int) (f.pixel(i).y()+dy);
			if (DivPlacability.pixelIsBlocked(x, y, div.race(), a))
				return false;
		}
		return true;
	}
	
	public DivFormationImp deployCentre(DIV_SPEC div, int men, DIV_FORMATION form, int cx, int cy, double rightX, double rightY, int rowMen, Army a) {
		
		rowMen = CLAMP.i(rowMen, 1, men);
		
		double depth = (double)men/(rowMen);
		int stepsForward = (int) Math.ceil(depth/2);
		int ts = form.size(div);
		
		if (DivPlacability.pixelIsBlocked(cx, cy, div.race(), a)) {
			cx &= ~C.T_MASK;
			cy &= ~C.T_MASK;
			cx += C.TILE_SIZEH;
			cy += C.TILE_SIZEH;
		}
		
		if (DivPlacability.pixelIsBlocked(cx, cy, div.race(), a)) {
			return null;
		}
		
		Flooder f = flooder.getFlooder();
		f.init(this);
		position.clear();
		
		
		vec.set(rightX, rightY);
		vec.rotate90();
		vec.rotate90();
		vec.rotate90();
		
		{
			int ccx = (int) (cx - (rightX*rowMen*ts/2.0 + rightX*ts/2.0) + vec.nX()*(stepsForward-1)*ts); 
			int ccy = (int) (cy - (rightY*rowMen*ts/2.0 + rightY*ts/2.0) + vec.nY()*(stepsForward-1)*ts); 
			position.deployInit(vec.dir(), ccx, ccy, rightX, rightY, form, rowMen*ts + ts/2);
		}
	
		
		for (int i = 0; i < stepsForward; i++) {
			int dx = (int) (cx + vec.nX()*ts*i); 
			int dy = (int) (cy + vec.nY()*ts*i);
			int m = deployCentreRow(div, f, CLAMP.i(men, 0, rowMen), ts, dx, dy, rightX, rightY, position, a);
			if (m == 0)
				break;
			men -= m;
			if (men <= 0)
				break;
		}
		
		int i = 1;
		while(men > 0) {
			int dx = (int) (cx + -vec.nX()*ts*i); 
			int dy = (int) (cy + -vec.nY()*ts*i);
			int m = deployCentreRow(div, f, CLAMP.i(men, 0, rowMen), ts, dx, dy, rightX, rightY, position, a);
			if (m == 0)
				break;
			men -= m;
			i++;
		}
		
		deployCentreFinish(div, f, men, (int)(cx+vec.nX()*(stepsForward-1)), (int)(cy+vec.nY()*(stepsForward-1)), rightX, rightY, position, a);
		
		f.done();
		
		position.deployFinish(flooder.filler, div);
		return position;
		
	}
	

	
	private int deployCentreRow(DIV_SPEC div, Flooder f, int men, int ts, int cx, int cy, double rx, double ry, DivFormationImp target, Army a) {

		if (DivPlacability.pixelIsBlocked(cx, cy, div.race(), a)) {
			cx &= ~C.T_MASK;
			cy &= ~C.T_MASK;
			cx += C.TILE_SIZEH;
			cy += C.TILE_SIZEH;
		}
		
		if (DivPlacability.pixelIsBlocked(cx, cy, div.race(), a)) {
			return 0;
		}
		
		int left = men/2;
		int right = men/2;
		
		if (men % 2 == 0) {
			int dx = (int) (cx+rx*ts/2);
			int dy = (int) (cy+ry*ts/2);
			if (DivPlacability.pixelIsBlocked(dx, dy, div.race(), a)) {
				left--;
			}else {
				cx = dx;
				cy = dy;
			}
		}else {
			
		}

		
		
		int amount = deployCentreRowPos(div, f, cx, cy, target);
		
		if (men == amount)
			return amount;
		
		boolean canRight = true;
		boolean canLeft = true;
		
		for (int i = 1; canRight || canLeft; i++) {
			
			if (canLeft && amount < men && i <= left) {
				int x = (int) (cx - i*rx*ts);
				int y = (int) (cy - i*ry*ts);
				if (DivPlacability.pixelIsBlocked(x, y, div.race(), a) || !isDeployable(x, y, a))
					canLeft = false;
				else {
					amount += deployCentreRowPos(div, f, x, y, target);
				}
			}else {
				canLeft = false;
			}
			if (canRight && amount <men && i <= right) {
				int x = (int) (cx + i*rx*ts);
				int y = (int) (cy + i*ry*ts);
				if (DivPlacability.pixelIsBlocked(x, y, div.race(), a) || !isDeployable(x, y, a))
					canRight = false;
				else {
					amount += deployCentreRowPos(div, f, x, y, target);
				}
			}else {
				canRight = false;
			}
			
			
		}
		
		
		
		return amount;
	}
	

	
	private int deployCentreRowPos(DIV_SPEC div, Flooder f, int x, int y, DivFormationImp target) {
		target.deploy(x, y, div);
		int tx = x>>C.T_SCROLL;
		int ty = y>>C.T_SCROLL;
		f.pushSloppy(tx, ty, 0);
		return 1;
	}
	
	private void deployCentreFinish(DIV_SPEC div, Flooder f, int men, int ux, int uy, double rx, double ry, DivFormationImp target, Army a) {
		if (men == 0) {
			return;
		}
				
		int x1 = ux;
		int y1 = uy;
		while(SETT.PIXEL_BOUNDS.holdsPoint(x1, y1)) {
			x1 -= rx*100;
			y1 -= ry*100;
		}
		
		int x2 = ux;
		int y2 = uy;
		while(SETT.PIXEL_BOUNDS.holdsPoint(x2, y2)) {
			x2 += rx*100;
			y2 += ry*100;
		}
		
		while(f.hasMore() && men > 0) {
			
			PathTile t = f.pollSmallest();
			
			if (t.getValue() > 0) {
				target.deploy((t.x()<< C.T_SCROLL)+C.TILE_SIZEH, (t.y()<< C.T_SCROLL)+C.TILE_SIZEH, div);
				men --;
			}
			
			for (int i = 0; i < DIR.ORTHO.size(); i++) {
				DIR d = DIR.ORTHO.get(i);
				int dx = t.x()+d.x();
				int dy = t.y()+d.y();
				if (!SETT.IN_BOUNDS(dx, dy))
					continue;
				if (!DivPlacability.tileIsOK(dx, dy, a))
					continue;
				int x = (dx << C.T_SCROLL)+C.TILE_SIZEH;
				int y = (dy << C.T_SCROLL)+C.TILE_SIZEH;
				
				double D = (x2 - x1) * (y - y1) - (x - x1) * (y2 - y1);
				if (D >= 0) {
					f.pushSmaller(dx, dy, t.getValue()+1);
				}
			}
			
		}
		
	}
	
	public boolean isValid(DIV_SPEC div, DivFormationImp destination, Army a) {
		if (destination.deployed() == 0)
			return false;
		for (int i = 0; i < destination.deployed(); i++) {
			COORDINATE c = destination.pixel(i);
			if (DivPlacability.pixelIsBlocked(c.x(), c.y(), div.race(), a))
				return false;
		}
		return true;
	}

	public boolean fixFormation(DIV_SPEC div, DivFormationImp old, DIV_FORMATION form, int men, Army a) {

		if (old.deployed() == 0)
			return false;
		int sx = old.start().x();
		int sy = old.start().y();
		final double dx = old.dx();
		final double dy = old.dy();
		int steps = old.width()/old.formation().size(div);
		
		int bestI = -1;
		int bw = 0;
		double best = Double.MAX_VALUE;
		
		for (int i = 0; i < old.deployed(); i++) {
			int x = old.pixel(i).x();
			int y = old.pixel(i).y();
			int w = fixFormationWidth(x, y, steps, dx, dy, old.formation().size(div), a, div.race());
			if (w > 0) {
				double v = steps-w + Math.abs(x-sx) + Math.abs(y-sy);
				if (v < best) { 
					best = v;
					bw = w;
					bestI = i;
					if (v == 0)
						break;
				}
			}
		}
		
		
		
		if (bestI != -1) {
			
			int width = bw*old.formation().size(div);
			if (Math.abs(width-old.formation().size(div)) < old.formation().size(div))
				width = old.width();
			
			DivFormationImp f = deploy(div, men, form, old.pixel(bestI).x(), old.pixel(bestI).y(), dx, dy, width, a);
			if (f == null) {
				LOG.ln("no!");
			}
			else
				old.copy(f);
			
			return f != null;
		}
		
		return false;
	}
	
	public DivFormationImp getFixedFormation(DIV_SPEC div, DivFormationImp old, DIV_FORMATION form, int men, Army a) {

		if (old.deployed() == 0)
			return null;
		int sx = old.start().x();
		int sy = old.start().y();
		final double dx = old.dx();
		final double dy = old.dy();

		
		int size = form.size(div);
		
		int bw = old.width();
		
		for (int i = 0; i < old.width(); i+=size) {
			int x = (int) (sx + dx*i);
			int y = (int) (sy + dy*i);
			
			if (DivPlacability.pixelIsBlocked(x, y, div.race(), a)) {
				bw = i;
			}
			
		}
		
		if (bw < size || bw < old.width()/2) {
			return deployArroundCentre(div, men, form, old.body().cX(), old.body().cY(), dx, dy, old.width(), a);
		}
		
		
		DivFormationImp f = deploy(div, men, form, sx, sy, dx, dy, bw, a);
		return f;
	}
	
	private int fixFormationWidth(int px, int py, int steps, double dx, double dy, int tileSize, Army a, Race race) {
		for (int i = 0; i < steps; i++) {
			if (DivPlacability.pixelIsBlocked(px, py, race, a))
				return i;
			px += dx*tileSize;
			py += dy*tileSize;
		}
		return steps;
	}
	



	private int getSteps(int steps, double stepX, double stepY, int x1, int y1, int tileSize, Army a, Race race) {

		for (int i = 0; i < steps; i++) {

			int x = (int) (i * stepX);
			int y = (int) (i * stepY);
			x += x1;
			y += y1;

			if (DivPlacability.pixelIsBlocked(x, y, race, a) || !isDeployable(x, y, a))
				return i;

			if (i != 0) {
				int tx1 = ((int) (x1 + (i-1) * stepX)) >> C.T_SCROLL;
				int ty1 = ((int) (y1 + (i-1) * stepY)) >> C.T_SCROLL;
				int tx2 = ((int) (x1 + (i) * stepX)) >> C.T_SCROLL;
				int ty2 = ((int) (y1 + (i) * stepY)) >> C.T_SCROLL;
				if (tx1 != tx2 || ty1 != ty2) {
					if (PATH().coster.player.getCost(tx1, ty1, tx2, ty2) < 0)
						return i;
				}
			}

		}
		return steps;

	}

	static class DivDeployB {

		Div div;
		double dx, dy;
		int x1, y1;
		int width;
	
	}

	public boolean canDeploy(int x, int y, double nx, double ny, int width, int tz, Army a, Race race) {
		int steps = width /tz;
		return steps == getSteps(steps, nx*tz, ny*tz, x, y, tz, a, race);
	}

}
