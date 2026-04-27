package settlement.path.finders;

import static settlement.main.SETT.ROOMS;

import init.resources.RBIT.RBITImp;
import settlement.main.SETT;
import settlement.misc.util.TILE_STORAGE;
import settlement.path.components.FindableDatas;
import settlement.path.components.SComponent;
import settlement.path.components.SComponentLevel;
import settlement.path.components.finder.SCompFinder.SCompPatherExister;
import settlement.path.path.SPath;
import settlement.room.main.Room;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.sets.Bitmap1D;

public final class SFinderResourceStore {

	private Coo result = new Coo();
	private final Updater updater = new Updater();
	SFinderResourceStore() {
		new TestPath("storage", null) {
			@Override
			protected void place(int sx, int sy, SPath p) {
				TILE_STORAGE j = findAny(sx, sy, Integer.MAX_VALUE);
				if (j != null) {
					LOG.ln(j.x() + " " + j.y());
					p.request(sx,  sy, j);
				}else {
					LOG.ln("Nay");
				}
			}
		};
		
	}
	
	void update(double ds) {
		
		updater.update(ds);
	}
	
	final RBITImp resMask = new RBITImp();
	final RBITImp storeMask = new RBITImp();
	
	private FindableDatas d() {
		return SETT.PATH().comps.data;
	}
	
	private final SCompPatherExister wierd = new SCompPatherExister() {
		
		
		
		@Override
		public boolean isInComponent(SComponent c, double distance) {
			storeMask.or(d().storage.bits(c));
			resMask.or(d().resScattered.bits(c));
			
			return storeMask.has(resMask);
		}
		
		@Override
		public void init(SComponentLevel l) {
			resMask.clear();
			storeMask.clear();
		}
	};
	
	private SFINDER fin = new SFINDER() {
		
		@Override
		public boolean isInComponent(SComponent c, double distance) {
			return resMask.has(d().storage.bits(c));
		}
		
		@Override
		public boolean isTile(int tx, int ty, int tileNr) {
			Room r = SETT.ROOMS().map.get(tx, ty);
			if (r != null) {
				
				TILE_STORAGE result = r.storage(tx, ty);
				if (result != null && result.storageIsFindable() && result.storageReservable() > 0 && result.resource() != null && resMask.has(result.resource())) {
					SFinderResourceStore.this.result.set(result);
					return true;
				}
			}
			return false;
		}
	};
	

	
	/**
	 * 
	 * @param sx
	 * @param sy
	 * @param maxDistance
	 * @param path
	 * @return null if nothing was found. Else a job. If job resource == null, then the path is not set. Otherwise it is set.
	 */
	public TILE_STORAGE findAny(int sx, int sy, int maxDistance) {
		
		
		if (maxDistance == Integer.MAX_VALUE) {
			resMask.clearSet(d().resScattered.bits(sx, sy));
			COORDINATE c = SETT.PATH().finders.finder().findDest(sx, sy, fin, maxDistance);
			if (c != null) {
				return ROOMS().map.get(result).storage(result.x(), result.y());
			}
			return null;
		}
		
		if (SETT.PATH().comps.pather.exists(sx, sy, wierd, maxDistance)) {
			if (SETT.PATH().finders.finder().findDest(sx, sy, fin, maxDistance) != null)
				return ROOMS().map.get(result).storage(result.x(), result.y());
		}
		return null;
	
	}
	
	public TILE_STORAGE find(int sx, int sy) {

		
		if (!has(sx, sy))
			return null;
		
		TILE_STORAGE ss = findAny(sx, sy, 100);
		if (ss == null)
			updater.failShort(sx, sy);
		return ss;
		
	
	}
	
	public boolean has(int sx, int sy) {
		
		if (!hasAny(sx, sy))
			return false;
		
		return updater.tryShort(sx, sy);
		
	}
	
	public boolean hasAny(int sx, int sy) {
		SComponent s = SETT.PATH().comps.superComp.get(sx, sy);
		if (s == null)
			return false;
		
		return d().resScattered.bits(s).has(d().storage.bits(s));
		
	}
	
	final static class Updater {

		private final Bitmap1D tryShort = new Bitmap1D(Short.MAX_VALUE, false);
		
		private final double speed = 1.0/64.0;
		double ci = 0;
		
		public Updater() {

		}
		
		public void update(double ds) {
			int old = (int) ci;
			int max = SETT.PATH().comps.levels.get(0).componentsMax();
			if (max <= 0)
				return;
			ci += ds*max*speed;
			int now = (int) ci;
			int delt = old-now;
			
			if (ci >= max) {
				ci -= max;
			}
			
			for (int k = 0; k <= delt; k++) {
				int i = k+old;
				i %= max;
				tryShort.set(i, false);
			}
			
		}
		
		public boolean tryShort(int tx, int ty) {
			SComponent c = SETT.PATH().comps.levels.get(0).get(tx, ty);
			if (c == null)
				return false;
			if (tryShort.get(c.index()))
				return false;
			return true;
		}
		
		public void failShort(int tx, int ty) {
			SComponent c = SETT.PATH().comps.levels.get(0).get(tx, ty);
			if (c == null)
				return;
			tryShort.set(c.index(), true);
		}
		
	}


}
