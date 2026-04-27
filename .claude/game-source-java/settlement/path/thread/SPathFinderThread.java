package settlement.path.thread;

import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.PATH;

import game.GAME;
import init.constant.C;
import settlement.main.SETT;
import settlement.path.components.SCOMPONENTS;
import settlement.path.components.SComponent;
import settlement.path.components.SComponentChecker;
import settlement.path.components.SComponentEdge;
import settlement.path.components.finder.SCompFinder;
import settlement.path.components.finder.SCompFinder.SCompPath;
import snake2d.PathGame.COST;
import snake2d.PathTile;
import snake2d.PathUtilOnline;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.DIR;

final class SPathFinderThread {
	
	double lastDistance;
	private final int chunkD;
	private final PathUtilOnline p;
	private final SCompFinder cf;
	private final SComponentChecker CHECK;
	
	SPathFinderThread(SCOMPONENTS comps, PathUtilOnline p, int chunks) {
		this.p = p;
		cf = new SCompFinder(comps, p);
		CHECK = new SComponentChecker(comps.zero);
		chunkD = chunks;
	}
	
	
	public PathTile find(int startX, int startY, int destX, int destY, boolean full) {
		
		lastDistance = 0;
		SCompPath comp = cf.findDest(startX, startY, destX, destY);
		if (comp == null)
			return null;
		
		lastDistance = comp.distance();
	
		if (comp.path().size() > chunkD) {
			return findComp(comp, startX, startY, comp.path().get(comp.path().size()-chunkD));
		}
		return find(comp, startX, startY, destX, destY, full);
	}

	private PathTile find(SCompPath comp, int startX, int startY, int destX, int destY, boolean full) {
		
		Flooder f = p.getFlooder();
		f.init(SPathFinderThread.class);
		
		f.pushSloppy(startX, startY, 0);
		f.setValue2(startX, startY, 0);
		OpDist.init(destX, destY);

		while (f.hasMore()) {
			PathTile t = f.pollSmallest();
			
			if (full && t.x() == destX && t.y() == destY) {
				f.done();
				return t;
			}
			
			if (Math.abs(t.x()-destX) + Math.abs(t.y()-destY) == 1) {
				if (full) {
					t = f.force((short)destX, (short)destY, t.getValue2(), t);
				}
				f.done();
				return t;
			}
			
			if (!comp.is(t))
				continue;
			
			for (int i = 0; i < DIR.ALL.size(); i++) {
				DIR d = DIR.ALL.get(i);
				int tx = t.x()+d.x();
				int ty = t.y()+d.y();
				
				if (!IN_BOUNDS(tx, ty))
					continue;
				
				double cost = SETT.PATH().huristics.getCost(t.x(), t.y(),tx,ty);
				if (cost > 0) {
					cost*= d.tileDistance();
					
					cost += t.getValue2();
					PathTile t2 = f.pushSmaller(tx, ty, cost+OpDist.get(tx, ty), t);
					if (t2 != null)
						t2.setValue2(cost);
					
					
					
				}else if(cost == COST.BLOCKED) {
					f.close(tx, ty, 0);
				}
				
				
				
			}
		}
		
		if (!PATH().willUpdate()) {
			GAME.Notify(startX + " " + startY + " -> " + destX + " " + destY + " " + full);
		}
		
		f.done();
		return null;
		
	}
	
	private PathTile findComp(SCompPath comp, int startX, int startY, SComponent dest) {
		
		Flooder f = p.getFlooder();
		f.init(this);
		SComponentChecker check = CHECK;
		check.init();
		
		check.isSetAndSet(dest);
		SComponentEdge e = dest.edgefirst();
		while(e != null) {
			check.isSetAndSet(e.to());
			e = e.next();
		}
		
		
		OpDist.init(dest.centreX(), dest.centreY());
		f.pushSloppy(startX, startY, 0);
		f.setValue2(startX, startY, 0);
		while (f.hasMore()) {
			PathTile t = f.pollSmallest();
			
			SComponent c = SETT.PATH().comps.zero.get(t);
			if (check.is(c)) {
				f.done();
				return t;
			}
			
			
			for (int i = 0; i < DIR.ALL.size(); i++) {
				DIR d = DIR.ALL.get(i);
				int tx = t.x()+d.x();
				int ty = t.y()+d.y();
				if (!comp.is(tx, ty))
					continue;
				double cost = SETT.PATH().huristics.getCost(t.x(), t.y(),tx,ty);
				if (cost > 0) {
					cost*= d.tileDistance();
					cost += t.getValue2();
					PathTile t2 = f.pushSmaller(tx, ty, cost+OpDist.get(tx, ty), t);
					if (t2 != null)
						t2.setValue2(cost);
					
				}else if(cost == COST.BLOCKED) {
					f.close(tx, ty, 0);
				}
				
			}
		}
		
		if (!PATH().willUpdate()) {
			GAME.Notify(startX + " " + startY + " -> " + dest.centreX() + " " + dest.centreY());
		}
		
		f.done();
		return null;
		
	}
	

	private static class OpDist {
		
		private static int destX,destY;
		private static double weight = 0.7;
		
		static void init(int dx, int dy) {
			destX = dx;
			destY = dy;
		}
		
		private static double get(int x, int y) {
			x = Math.abs(x-destX);
			y = Math.abs(y-destY);
			
			if (x > y){
				return weight*(C.SQR2*y + x-y);
			}else if(x < y){
				return weight*(C.SQR2*x + y-x);
			}else{
				return weight*C.SQR2*x;
			}
		}
		
	}


	
}
