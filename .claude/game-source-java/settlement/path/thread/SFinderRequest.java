package settlement.path.thread;

import static settlement.main.SETT.ENTITIES;
import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.JOBS;
import static settlement.main.SETT.PATH;
import static settlement.main.SETT.THINGS;

import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.path.path.SPath;
import settlement.path.thread.FinderThread.ThreadPath;
import settlement.path.thread.FinderThread.ThreadPathJob;
import snake2d.PathTile;
import snake2d.PathUtilOnline;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.ArrayList;

public abstract class SFinderRequest {
	
	protected final ThreadPathJob job = new ThreadPathJob() {
		
		@Override
		public boolean doJob(PathUtilOnline p, SPathFinderThread fin, ThreadPath pp) {
			PathTile t = find(pp.sx, pp.sy, p);
			if (t != null) {
				
				pp.path.set(t);
				pp.destX = (short) t.x();
				pp.destY = (short) t.y();
				return true;
			}
			return false;
		}
	};
	
	public boolean checkAndSetRequest(int sx, int sy, SPath path) {
		
		if (path.thread.isProcessed(sx, sy, 0, 0)) {
			path.clear();
			if (path.thread.isSuccess()) {
				path.copy(path.thread.path, path.thread.destX, path.thread.destY, true);
			}
			return true;
		}else if (path.thread.isBeingProcessed()) {
			return false;
		}
		return true;
	}
	
	protected abstract PathTile find(int sx, int sy, PathUtilOnline p);
	
	public final static class FinderIdle extends SFinderRequest{

		public FinderIdle() {
			
		}
		private final ArrayList<DIR> dirs = new ArrayList<>(DIR.ALL);
		public boolean shouldFind(ENTITY e) {
			if (isGoodTileToStandOn(e.tc().x(), e.tc().y(), 1)) {
				return false;
			}
			return true;
		}
		
		public void request(Humanoid h, SPath path) {
			SETT.PATH().thread.prep(path, job, h.tc().x(), h.tc().y(), 0, 0, true);
		}

		@Override
		public final PathTile find(int sx, int sy, PathUtilOnline p) {
		
			
			p.getFlooder().init(null);
			PathTile t = p.getFlooder().pushSloppy(sx, sy, 0, null);
			
			while (p.getFlooder().hasMore()){
				
				t = p.getFlooder().pollSmallest();
				
				if (isGoodTileToStandOn(t.x(), t.y(), 0)) {
					p.getFlooder().done();
					
					return t;
				}
				
				for (DIR d : dirs) {
					
					int dx = d.x()+t.x();
					int dy = d.y()+t.y();
					if (!IN_BOUNDS(dx, dy))
						continue;
					if (!SETT.PATH().connectivity.is(dx, dy))
						continue;
					
					double v =  PATH().huristics.getCost(t.x(), t.y(), dx, dy);
					if (v < 0)
						continue;
					v *= d.tileDistance();
					v += t.getValue();
					if (v <= 40)
						p.getFlooder().pushSmaller(dx, dy, v, t);
				}
			}
			p.getFlooder().done();
			return null;
		
		}
		
		private boolean isGoodTileToStandOn(int tx, int ty, int max) {
			if (PATH().availability.get(tx, ty).player < 0)
				return false;
			if (PATH().availability.get(tx, ty).player >= 2)
				return false;
			if (JOBS().getter.is(tx, ty))
				return false;
			if (THINGS().getFirst(tx, ty) != null)
				return false;
			if (ENTITIES().amountAtTile(tx, ty) > max)
				return false;
			if (PATH().huristics.getter.get(tx, ty) > 0.1)
				return false;
			if (SETT.ROOMS().map.is(tx, ty))
				return false;
			return true;
		}
		
	}
	
	
}
