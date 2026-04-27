package game.battle.thread.general.offence;

import game.battle.div.Div;
import game.battle.thread.general.StrategosUtil;
import game.battle.thread.general.offence.ContextLines.Line;
import init.constant.C;
import init.constant.Config;
import settlement.main.SETT;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.sets.Bitmap2D;

class StepLinesMaker {

	private final StrategosUtil u;
	private final Bitmap2D blob;
	private final ContextLines lines;
	
	StepLinesMaker(StrategosUtil u, Context c){
		
		this.u = u;
		this.blob = c.blob;
		this.lines = c.lines;
		
	}
	
	public void make() {
		
		lines.clear();
		
		Flooder f = u.flooder.getFlooder();
		f.init(this);
		
		
		for (int ty = 0; ty < SETT.THEIGHT; ty++) {
			for (int tx = 0; tx < SETT.TWIDTH; tx++) {
				f.setValue2(tx, ty, 0);
				if (!blob.is(tx, ty)) {
					f.pushSloppy(tx,  ty, ty*SETT.TWIDTH+tx);
					f.setValue2(tx,  ty, 0);
				}
			}
		}
		
		int ww = SETT.TAREA*2;
		
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			if (t.getValue() >= ww) {
				f.reopen(t);
				f.pushSloppy(t.x(),  t.y(), 0);
				break;
			}
			
			for (int di = 0; di < DIR.ORTHO.size(); di++) {
				DIR d = DIR.ORTHO.get(di);
				if (blob.is(t, d)) {
					f.pushSmaller(t, d, t.getValue()+ww);
					f.setValue2(t, d, 1);
				}
			}
		
		}
		
		//now, only the edges are pushed... and value2 == 1
		
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			make(f, t);
		
		}
		f.done();
		
		//now we'll assign blobID to the lines
		{
			f.init(this);
			int id = 0;
			for (int ty = 0; ty < SETT.THEIGHT; ty++) {
				for (int tx = 0; tx < SETT.TWIDTH; tx++) {
					if (!f.hasBeenPushed(tx, ty) && blob.is(tx, ty)) {
						f.pushSloppy(tx, ty, 0);
						while(f.hasMore()) {
							PathTile t = f.pollSmallest();
							t.setValue2(id);
							
							for (int i = 0; i < DIR.ORTHO.size(); i++) {
								if (blob.is(t, DIR.ORTHO.get(i)))
									f.pushSmaller(t, DIR.ORTHO.get(i), t.getValue()+1);
							}
						}
						id++;
					}
				}
			}
			f.done();
			
			for (int i = 0; i < lines.lines(); i++) {
				
				Line n = lines.get(i);
				int cx = n.sx;
				int cy = n.sy;
				cx /= C.TILE_SIZE;
				cy /= C.TILE_SIZE;
				
				n.blobID = (int) f.getValue2(cx, cy);
				
			}
			
		}
		
		
		f.init(this);
		for (int i = 0; i < lines.lines(); i++) {
			
			Line n = lines.get(i);
			int cx = n.cx();
			int cy = n.cy();
			cx /= C.TILE_SIZE;
			cy /= C.TILE_SIZE;
			
			f.setValue2(cx, cy, -(i+1));
		}
		
		//we now have lines, like a convex hull. They are ok, but we'll change the angle so that they face the enemy better.
		
		for (int di = 0; di < Config.battle().DIVISIONS_PER_ARMY; di++) {
			
			Div d = u.getArmy().enemy().divisions().get(di);
			for (int i = 0; i < d.position().deployed(); i++) {
				int x = d.position().tx(i);
				int y = d.position().ty(i);
				f.pushSloppy(x, y, 0);
			}
		}
		
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			if (t.getValue2() < 0) {
				Line n = lines.get((int)(-t.getValue2())-1);
				
				int cx = n.cx();
				int cy = n.cy();
				
				PathTile o = t;
				while(o.getParent() != null)
					o = o.getParent();
				
				int ex = o.x()*C.TILE_SIZE+C.TILE_SIZEH;
				int ey = o.y()*C.TILE_SIZE+C.TILE_SIZEH;
				if (vec.set(cx, cy, ex, ey) <= 0 || (vec.nX() == 0 && vec.nY() == 0)) {
					vec.set(1, 1);
				}
				
				vec.rotate90();
				
				n.sx = (int) (cx-vec.nX()*n.length/2);
				n.sy = (int) (cy-vec.nY()*n.length/2);
				n.dx = vec.nX();
				n.dy = vec.nY();
			}
			
			for (int di = 0; di < DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				if (SETT.IN_BOUNDS(t, d))
					f.pushSmaller(t, d, d.tileDistance(), t);
			}
			
		}
		f.done();
		
		
		
	}

	private final double minDist = dist(4, 4);
	private final double maxDist = dist(8, 8);
	private final VectorImp vec = new VectorImp();
	
	private void make(Flooder f, PathTile t) {
		if (t.getValue2() != 1)
			return;
		
		PathTile res = trymake(f, t, t, t, maxDist);
		if (res == null)
			res = trymake(f, t, t, t, minDist);
		
		if (res != null) {
			Line line = lines.makeNew();
			
			int sx = t.x()*C.TILE_SIZE+C.TILE_SIZEH;
			int sy = t.y()*C.TILE_SIZE+C.TILE_SIZEH;
			int ex = res.x()*C.TILE_SIZE+C.TILE_SIZEH;
			int ey = res.y()*C.TILE_SIZE+C.TILE_SIZEH;
			line.length = (int) vec.set(sx, sy, ex, ey);
			line.sx = sx;
			line.sy = sy;
			line.dx = vec.nX();
			line.dy = vec.nY();
		}
		
	}
	
	private PathTile trymake(Flooder f, PathTile start, PathTile prev, PathTile current, double targetDistPow) {
		
		if (current.getValue2() != 1)
			return null;
		
		f.setValue2(current, 2);
		
		int dx = start.x()-current.x();
		int dy = start.y()-current.y();
		double dist = dist(dx, dy);
		if (dist >= targetDistPow)
			return current;
		
		for (int i = 0; i < DIR.ALL.size(); i++) {
			DIR d = DIR.ALL.get(i);
			int x = current.x()+d.x();
			int y = current.y()+d.y();
			if (prev.isSameAs(x, y))
				continue;
			if (!SETT.IN_BOUNDS(x, y))
				continue;
			if (f.getValue2(x, y) != 1)
				continue;
			PathTile t = trymake(f, start, current, f.get(x, y), targetDistPow);
			if (t != null) {
				f.setValue2(current, 2);
				return t;
			}
		}
		
		f.setValue2(current, 1);
		
		return null;
	}
	
	private double dist(int dx, int dy) {
		return dx*dx+dy*dy;
	}
	

}
