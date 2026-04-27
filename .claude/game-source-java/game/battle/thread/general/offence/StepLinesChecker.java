package game.battle.thread.general.offence;

import game.battle.formation.DivPlacability;
import game.battle.thread.general.StrategosUtil;
import game.battle.thread.general.offence.ContextLines.Line;
import game.battle.thread.status.BattleStatus;
import init.constant.C;
import settlement.main.SETT;
import snake2d.util.datatypes.VectorImp;
import util.data.INT.IntImp;

class StepLinesChecker {
	
	private final StrategosUtil u;
	private final ContextLines lines;
	private final IntImp checkI;
	
	StepLinesChecker(StrategosUtil u, Context c){
		this.u = u;
		this.lines = c.lines;
		this.checkI = c.checkI;
	}
	
	
	public void init() {
		checkI.set(0);
	}
	
	/**
	 * processes the existing lines, and removes the ones not valid.
	 */
	public boolean check() {
		
		if (checkI.get() >= lines.lines())
			return false;
		
		Line l = lines.get(checkI.get());
		
		if (!check(l)) {
			lines.remove(checkI.get());
		}else {
			checkI.inc(1);
		}
		return true;
		
	}
	
	private boolean check(Line l) {
		
		if (!checkBlockageAndChopUpIfNeeded(l)) {
			return false;
		}
		if (!checkSpaceInBack(l, C.TILE_SIZE*5)) {
			return false;
		}
		
		
		
		if (!checkEnemiesInFront(l)) {
			return false;
		}
		
		return true;
	}
	
	private boolean checkBlockageAndChopUpIfNeeded(Line l) {
		
		double start = 0;
		
		double minSize = C.TILE_SIZE*4;
		boolean blocked = false;
		
		for (int r = 0; r < l.length; r += C.TILE_SIZE) {
			
			int x = (int) (l.sx + r*l.dx);
			int y = (int) (l.sy + r*l.dy);
			x /= C.TILE_SIZE;
			y /= C.TILE_SIZE;
			
			if (blocked(x, y, r, l)) {
				blocked = true;
				if (r-start > minSize) {
					Line nl = lines.makeNew();
					nl.dx = l.dx;
					nl.dy = l.dy;
					nl.length = (int) (r-start);
					nl.sx = (int) (l.sx + start*l.dx);
					nl.sy = (int) (l.sy + start*l.dy);
				}
				start = r + C.TILE_SIZE;
			}
			
			
			
		}
		
		return !blocked;
		
	}
	
	private final VectorImp vec = new VectorImp();
	
	private boolean checkSpaceInBack(Line n, int minDist) {
		
		vec.set(n.dx, n.dy);
		vec.rotate90();
		
		for (int in = 0; in <= minDist; in+= C.TILE_SIZE) {
			
			
			
			for (int r = 0; r <= n.length; r += C.TILE_SIZE) {
				
				int x = (int) (n.sx + vec.nX()*in + r*n.dx);
				int y = (int) (n.sy + vec.nY()*in + r*n.dy);
				x /= C.TILE_SIZE;
				y /= C.TILE_SIZE;
				
				if (solid(x, y))
					return false;
			}
			
		}
		return true;
	}
	
	private boolean checkEnemiesInFront(Line n) {
		
		vec.set(n.dx, n.dy);
		vec.rotate90();
		vec.rotate90();
		vec.rotate90();
		double dist = 64*C.TILE_SIZE;
		for (int in = 0; in <= dist; in+= C.TILE_SIZE) {
			
			int sols = 0;
			boolean enemy = false;
			for (int r = 0; r <= n.length; r += C.TILE_SIZE) {
				
				int x = (int) (n.sx + vec.nX()*in + r*n.dx);
				int y = (int) (n.sy + vec.nY()*in + r*n.dy);
				x /= C.TILE_SIZE;
				y /= C.TILE_SIZE;
				
				if (solid(x, y))
					sols += C.TILE_SIZE;
				
				if (!enemy && BattleStatus.map().hasEnemy.is(x, y, u.getArmy())) {
					enemy = true;
				}
			}
			
			if (sols*2 > n.length)
				return false;
			if (enemy)
				return true;
			
		}
		return false;
	}
	
	
	private boolean blocked(int x, int y, double r, Line l) {
		if (solid(x, y))
			return true;
		if (r > 0) {
			int ox = (int) (l.sx + (r-C.TILE_SIZE)*l.dx);
			int oy = (int) (l.sy + (r-C.TILE_SIZE)*l.dy);
			ox /= C.TILE_SIZE;
			oy /= C.TILE_SIZE;
			if (solid(ox, y) || solid(x, oy)) {
				
				return true;
			}
		}
		return false;
	}
	
	public boolean solid(double dx, double dy) {
		
		int tx = (int) dx;
		int ty = (int) dy;
		if (!SETT.IN_BOUNDS(tx, ty))
			return true;
		if (!DivPlacability.tileIsOK(tx, ty, u.getArmy()))
			return true;
		return false;
	}
	

	
	
	
}
