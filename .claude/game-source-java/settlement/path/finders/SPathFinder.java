package settlement.path.finders;

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
import settlement.room.main.RoomInstance;
import snake2d.PathGame.COST;
import snake2d.PathTile;
import snake2d.PathUtilOnline;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;

public final class SPathFinder {
	
	public double lastDistance;
	private final int chunkD;
	private final SPathUtilResult res = new SPathUtilResult();
	private final Coo coo = new Coo();
	private final PathUtilOnline p;
	public final SCompFinder cf;
	private final SPathFinderDest fDest;
	private final SComponentChecker CHECK;
	
//	SPathFinder(SCOMPONENTS comps, PathUtilOnline p) {
//		this(comps, p, 2);
//	}
	
	public SPathFinder(SCOMPONENTS comps, PathUtilOnline p, int chunks) {
		this.p = p;
		cf = new SCompFinder(comps, p);
		fDest = new SPathFinderDest(p);
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
	
	public PathTile cDebug(int startX, int startY, int destX, int destY, boolean full) {
		
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
	
	public PathTile reverse(PathTile abs) {
		if (abs.getParent() != null){
			PathTile p = abs.getParent();
			abs.parentSet(null);
			abs =reverse(abs, p);
		}
		return abs;
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

	private PathTile find(SCompPath comp, int startX, int startY, int destX, int destY, boolean full) {
		
		Flooder f = p.getFlooder();
		f.init(SPathFinder.class);
		
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
		
		SComponent start = SETT.PATH().comps.zero.get(startX, startY);
		
		check.isSetAndSet(dest);
		SComponentEdge e = dest.edgefirst();
		while(e != null) {
			if (e.to() != start) {
				check.isSetAndSet(e.to());
			}
			
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
	

	public COORDINATE findDest(int startX, int startY, SFINDER finder, int maxDistance) {
		lastDistance = 0;
		SCompPath comp = cf.find(startX, startY, finder, maxDistance);
		if (comp == null)
			return null;
		
		lastDistance = comp.distance();

		if (finder.isTile(startX, startY, 0)) {
			coo.set(startX, startY);
			return coo;
		}
		
		PathTile t = fDest.findDest(startX, startY, comp, finder);
		if (t != null) {
			coo.set(t);
			return coo;
		}
		return null;
	}
	
	public COORDINATE findDest(RoomInstance startRoom, SFINDER finder, int maxDistance) {
		lastDistance = 0;
		SCompPath comp = cf.find(startRoom, finder, maxDistance);

		if (comp == null)
			return null;
		
		lastDistance = comp.distance();

		int startX = comp.path().get(0).centreX();
		int startY = comp.path().get(0).centreY();
		if (comp.path().size() > 1) {
			startX = comp.path().get(1).centreX();
			startY = comp.path().get(1).centreY();
		}
		
		
		PathTile t = fDest.findDest(startX, startY, comp, finder);
		if (t != null) {
			coo.set(t);
			return coo;
		}
		return null;
	}

	public SPathUtilResult find(int startX, int startY, SFINDER finder, int maxDistance) {

		SCompPath comp = SETT.PATH().comps.pather.find(startX, startY, finder, maxDistance);
		SPathUtilResult r = find(startX, startY, finder, maxDistance,
				comp);
		return r;
		
	}
	
	SPathUtilResult find(int startX, int startY, SFINDER finder, int maxDistance, SCompPath comp) {
		lastDistance = 0;
		if (comp == null)
			return null;
		
		lastDistance = comp.distance();

		PathTile t = fDest.findDest(startX, startY, comp, finder);
		
		if (t != null) {
			if (comp.path().size() == 1 && t.getParent() != null) {
				res.destX = t.x();
				res.destY = t.y();
				res.t = t.getParent();
				return res;
			}
			
			res.destX = t.x();
			res.destY = t.y();
			
			if (comp.path().size() > chunkD) {
				t = findComp(comp, startX, startY, comp.path().get(comp.path().size()-chunkD));
				if (t != null) {
					res.t = t;
					return res;
				}
				return null;
			}
			t = find(comp, startX, startY, t.x(), t.y(), false); 
			if (t != null) {
				res.t = t;
				return res;
			}
		}
		if (!SETT.PATH().willUpdate()) {
			String ss = "";
			
			for (SComponent s : comp.path()) {
				ss += s.centreX() + " " + s.centreY();
				ss += System.lineSeparator();
				
			}
			GAME.Notify(ss);
		}
		
		
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

	
	public final static class SPathUtilResult {

		public int destX, destY;
		public PathTile t;
	}

	
}
