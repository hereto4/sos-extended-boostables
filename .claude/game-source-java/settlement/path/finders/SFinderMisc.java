package settlement.path.finders;

import static settlement.main.SETT.ENTITIES;
import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.JOBS;
import static settlement.main.SETT.PATH;
import static settlement.main.SETT.THINGS;

import settlement.entity.ENTITY;
import settlement.main.SETT;
import settlement.path.components.SComponent;
import settlement.path.components.finder.SCompFinder.SCompPath;
import settlement.path.path.SPath;
import snake2d.LOG;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import util.GUTIL;
import view.sett.IDebugPanelSett;
import view.tool.PlacableSingle;

public abstract class SFinderMisc {
	
	private final double max;
	
	protected SFinderMisc(int max) {
		this.max = max;
	}
	
	protected boolean has() {
		return true;
	}
	
	public abstract boolean isTile(int tx, int ty);
	
	public final boolean find(COORDINATE start, SPath path) {

		if (!has())
			return false;
		
		if (isTile(start.x(), start.y())) {
			return false;
		}
		
		GUTIL.flooder().init(this);
		GUTIL.flooder().pushSloppy(start.x(), start.y(), 0, null);
		
		while (GUTIL.flooder().hasMore()){
			
			PathTile t = GUTIL.flooder().pollSmallest();
			
			if (isTile(t.x(), t.y())) {
				path.setDirect(start.x(), start.y(), t.x(), t.y(), t, true);
				GUTIL.flooder().done();
				return true;
			}
			
			for (DIR d : DIR.ALL) {
				
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
				if (v <= max)
					GUTIL.flooder().pushSmaller(dx, dy, v, t);
			}
		}
		GUTIL.flooder().done();
		
		return false;
	
	}
	
	public final static class FinderIdle {

		FinderIdle() {
			
		}
		
		private final ArrayList<DIR> dirs = new ArrayList<>(DIR.ALL);

		public final boolean find(COORDINATE start, SPath path, ENTITY e) {
		
			
			if (isGoodTileToStandOn(start.x(), start.y(), e)) {
				return false;
			}
			
			GUTIL.flooder().init(null);
			PathTile t = GUTIL.flooder().pushSloppy(start.x(), start.y(), 0, null);
			
			while (GUTIL.flooder().hasMore()){
				
				t = GUTIL.flooder().pollSmallest();
				
				if (isGoodTileToStandOn(t.x(), t.y(), e)) {
					path.setDirect(start.x(), start.y(), t.x(), t.y(), t, true);
					GUTIL.flooder().done();
					return true;
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
						GUTIL.flooder().pushSmaller(dx, dy, v, t);
				}
			}
			GUTIL.flooder().done();
			
			return false;
		
		}
		
		private boolean isGoodTileToStandOn(int tx, int ty, ENTITY e) {
			if (PATH().availability.get(tx, ty).player < 0)
				return false;
			if (PATH().availability.get(tx, ty).player >= 2)
				return false;
			if (JOBS().getter.is(tx, ty))
				return false;
			if (THINGS().getFirst(tx, ty) != null)
				return false;
			if (ENTITIES().hasAtTile(e, tx, ty))
				return false;
			if (PATH().huristics.getter.get(tx, ty) > 0.1)
				return false;
			if (SETT.ROOMS().map.is(tx, ty))
				return false;
			return true;
		}
		
	}
	
	public abstract static class FinderMiscWithoutDest {
		
		private final double max;
		
		protected FinderMiscWithoutDest(int max) {
			this.max = max;
		}
		
		protected boolean has() {
			return true;
		}
		
		public abstract boolean isTile(int tx, int ty);
		
		public final boolean find(COORDINATE start, SPath path) {

			if (!has())
				return false;
			
			int sx = start.x();
			int sy = start.y();
			
			if (isTile(sx, sy)) {
				if (path != null) {
					path.request(sx, sy, sx, sy, false);
					return path.isSuccessful();
				}
				return true;
				
			}
			
			for (DIR d : DIR.ORTHO) {
				if (isTile(sx+d.x(), sy+d.y())) {
					if (path != null) {
						path.request(sx, sy, sx+d.x(), sy+d.y(), false);
						return path.isSuccessful();
					}
					return true;
				}
			}
			
			GUTIL.flooder().init(this);

			PathTile t = GUTIL.flooder().pushSloppy(sx, sy, 0, null);

			while (GUTIL.flooder().hasMore()){
				
				t = GUTIL.flooder().pollSmallest();
				
				for (DIR d : DIR.ALL) {
					
					int dx = d.x()+t.x();
					int dy = d.y()+t.y();
					if (!IN_BOUNDS(dx, dy))
						continue;
					if (d.isOrtho()) {
						if (isTile(dx, dy)) {
							GUTIL.flooder().done();
							if (path != null) {
								path.setDirect(sx, sy, dx, dy, t, false);
							}
							return true;
						}
					}
					
					double v =  PATH().huristics.getCost(t.x(), t.y(), dx, dy);
					if (v < 0)
						continue;
					v *= d.tileDistance();
					v += t.getValue();
					if (v <= max)
						GUTIL.flooder().pushSmaller(dx, dy, v, t);
				}
			}
			GUTIL.flooder().done();
			


			return false;
		
		}
		
	}
	
	public final static class FinderArround {
		
		private Coo result = new Coo();
		
		FinderArround() {
			IDebugPanelSett.add(new PlacableSingle("arround") {
				
				@Override
				public void placeFirst(int tx, int ty) {
					COORDINATE c = find(tx, ty, 10, 10+RND.rInt(10));
					if (c == null)
						LOG.ln("nono");
					LOG.ln(c.x() + " " + c.y());
				}
				
				@Override
				public CharSequence isPlacable(int tx, int ty) {
					SComponent c =  PATH().comps.zero.get(tx, ty);
					if(c == null) {
						return E;
					}
					return null;
				}
			});
		}
		
		/**
		 * Finds a reachable coordinate arround the start position (furthest away) within the given distance.
		 * @param sx
		 * @param sy
		 * @param distance
		 * @return
		 */
		public COORDINATE find(final int sx, final int sy, double distMin, double distMax) {
			
			SComponent su = PATH().comps.superComp.get(sx, sy);
			if (su == null)
				return null;
			boolean found = false;
			double dd = distMin + RND.rFloat(distMax-distMin);
			GUTIL.flooder().init(this);
			PathTile t = GUTIL.flooder().close(sx, sy, 0, null);
			
			for (DIR d : DIR.ALL) {
				found = push(t, d, su) | found;
			}
			
			while (GUTIL.flooder().hasMore()){
				
				t = GUTIL.flooder().pollSmallest();

				if (t.getValue() >= dd) {
					GUTIL.flooder().done();
					return result;
				}
				
				for (DIR d : DIR.ALL) {
					found = push(t, d, su) | found;
				}
			}
			GUTIL.flooder().done();
			if (found) {
				return result;
			}
			return null;
		}
		
		private boolean push(PathTile t, DIR d, SComponent su) {
			int dx = d.x()+t.x();
			int dy = d.y()+t.y();
			if (!IN_BOUNDS(dx, dy))
				return false;
			SComponent c = PATH().comps.zero.get(dx, dy);
			if (c == null || c.superCompTop() != su)
				return false;
			
			double v =  d.tileDistance();
			if (v < 0)
				return false;
			v *= d.tileDistance()*RND.rFloat1(0.1);
			v += t.getValue();
			GUTIL.flooder().pushSmaller(dx, dy, t.getValue()+v, t);
			result.set(dx, dy);
			return true;
			
		}
		
		
	}
	
	public final static class Rnd {
		
		private Coo result = new Coo();
		
		Rnd() {
			
		}
		
		/**
		 * Finds a reachable coordinate arround the start position (furthest away) within the given distance.
		 * @param sx
		 * @param sy
		 * @param distance
		 * @return
		 */
		public COORDINATE find(final int sx, final int sy, int r) {
			
			SCompPath p = SETT.PATH().comps.pather.fill(sx, sy, r);
			
			if (p.path().size() == 0)
				return null;
			
			SComponent s = p.path().rnd();
			
			GUTIL.coos().set(0);
			
			r = s.level().size();
			
			int x1 = s.centreX()&~(r-1);
			int y1 = s.centreY()&~(r-1);
			
			for (int dy = 0; dy < r; dy++) {
				for (int dx = 0; dx < r; dx++) {
					int x = x1+dx;
					int y = y1+dy;
					if (s.is(x, y)) {
						GUTIL.coos().get().set(x, y);
						GUTIL.coos().inc();
					}
				}
			}
			
			if (GUTIL.coos().getI() == 0) {
				result.set(s.centreX(), s.centreY());
				return result;
			}
			
			result.set(GUTIL.coos().set(RND.rInt(GUTIL.coos().getI())));
			return result;
		}
		
		
	}

}
