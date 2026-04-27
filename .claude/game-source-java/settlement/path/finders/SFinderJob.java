package settlement.path.finders;

import static settlement.main.SETT.JOBS;

import init.resources.RBIT.RBITImp;
import init.resources.RESOURCE;
import settlement.job.Job;
import settlement.main.SETT;
import settlement.path.components.FindableDataSingle;
import settlement.path.components.FindableDatas;
import settlement.path.components.SComponent;
import settlement.path.components.SComponentLevel;
import settlement.path.components.finder.SCompFinder.SCompPatherExister;
import settlement.path.path.SPath;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.Bitsmap1D;

public final class SFinderJob{

	public static int DIST_SMALL = 128;
	private final SFinderUpdater updater = new SFinderUpdater();
	
	SFinderJob() {

		new TestPath("job", null) {
			@Override
			protected void place(int sx, int sy, SPath p) {
				Job j = find(sx, sy, p, true);
				if (j != null && j.resourceCurrentlyNeeded() != null) {
					p.request(sx,  sy, j.jobCoo());
				}
			}
		};
		
		
		
	}
	
	
	private FindableDatas d() {
		return SETT.PATH().comps.data;
	}
	
	final RBITImp resMask = new RBITImp();
	final RBITImp jobMask = new RBITImp();
	private Job result;
	
	private final SCompPatherExister exister = new SCompPatherExister() {
		
		
		
		@Override
		public boolean isInComponent(SComponent c, double distance) {
			if (d().job.get(c) > 0)
				return true;
			if (SETT.WEATHER().growthRipe.cropsAreRipe() && d().jobHarvest.get(c) > 0)
				return true;
			jobMask.or(d().jobs.bits(c));
			resMask.or(d().resScattered.bits(c));
			resMask.or(d().resCrate.bits(c));
			resMask.or(d().resPriority.bits(c));
			
			return jobMask.has(resMask);
		}
		
		@Override
		public void init(SComponentLevel l) {
			resMask.clear();
			jobMask.clear();
		}
	};
	
	private SFINDER fin = new SFINDER() {
		
		@Override
		public boolean isInComponent(SComponent c, double distance) {
			if (d().job.get(c) > 0)
				return true;
			if (resMask.has(d().jobs.bits(c)))
				return true;
			if (SETT.WEATHER().growthRipe.cropsAreRipe() && d().jobHarvest.get(c) > 0)
				return true;
			return false;
		}
		
		@Override
		public boolean isTile(int tx, int ty, int tileNr) {
			result = JOBS().getter.get(tx, ty);

			if (result == null)
				return false;
			if (!result.jobReserveCanBe())
				return false;
			if (result.needsRipe() && !SETT.WEATHER().growthRipe.cropsAreRipe())
				return false;
			if (result.resourceCurrentlyNeeded() == null)
				return true;
			if (result.resourceCurrentlyNeeded().bit.has(resMask))
				return true;
			
			return false;
		}
	};
	
	void update(double ds) {
		updater.update(ds);
	}
	
	public boolean hasJobs(int tx, int ty, boolean full) {
		
		if (full) {
			if (!updater.tryDistance(tx, ty))
				return false;
		}else {
			if (!updater.tryShort(tx, ty))
				return false;
		}
		
		return hasAnyJobs(tx, ty);
		
	}
	
	public boolean hasAnyJobs(int tx, int ty) {
		
		SComponent c = SETT.PATH().comps.superComp.get(tx, ty);
		if (c == null)
			return false;
		
		if (d().job.has(c))
			return true;
		if ((d().jobHarvest.has(c) && SETT.WEATHER().growthRipe.cropsAreRipe()))
			return true;
		resMask.clearSet(d().resScattered.bits(c)).or(d().resCrate.bits(c)).or(d().resPriority.bits(c));
		return d().jobs.has(c, resMask);
	}
	
	
	public Job findOnlyJobForced(int sx, int sy, int dist) {
		
		
		
		if (!hasAnyJobs(sx, sy))
			return null;
		
		if (dist == Integer.MAX_VALUE) {
			resMask.clearSet(d().resScattered.bits(sx, sy)).or(d().resCrate.bits(sx, sy)).or(d().resPriority.bits(sx, sy));
			COORDINATE c = SETT.PATH().finders.finder().findDest(sx, sy, fin, dist);
			if (c != null) {
				return result;
			}
		}else if (SETT.PATH().comps.pather.exists(sx, sy, exister, dist)) {
			COORDINATE c = SETT.PATH().finders.finder().findDest(sx, sy, fin, Integer.MAX_VALUE);
			if (c != null) {
				return result;
			}
		}
		
		return null;
		
	}
	
	public Job findOnlyJob(int sx, int sy, boolean full) {
		
		if (!hasJobs(sx, sy, full))
			return null;
		
		int dist = DIST_SMALL;
		if (full)
			dist = updater.distance(sx, sy);
		
		if (dist == Integer.MAX_VALUE) {
			resMask.clearSet(d().resScattered.bits(sx, sy)).or(d().resCrate.bits(sx, sy)).or(d().resPriority.bits(sx, sy));
			COORDINATE c = SETT.PATH().finders.finder().findDest(sx, sy, fin, dist);
			if (c != null) {
				return result;
			}
		}else if (SETT.PATH().comps.pather.exists(sx, sy, exister, dist)) {
			COORDINATE c = SETT.PATH().finders.finder().findDest(sx, sy, fin, Integer.MAX_VALUE);
			if (c != null) {
				return result;
			}
		}
		
		if (full) {
			updater.distanceFail(sx, sy);
		}else {
			updater.failShort(sx, sy);
		}
		
		return null;
		
	}
	
	/**
	 * 
	 * @param sx
	 * @param sy
	 * @param maxDistance
	 * @param path
	 * @return null if nothing was found. Else a job. If job resource == null, then the path is not set. Otherwise it is set.
	 */
	public Job find(int sx, int sy, SPath path, boolean full) {

		Job j = findOnlyJob(sx, sy, full);

		if (j != null) {
			if (j.resourceCurrentlyNeeded() == null && path != null) {
				int jx = j.jobCoo().x();
				int jy = j.jobCoo().y();
				if (path.request(sx, sy, jx, jy, false))
					return JOBS().getter.get(jx, jy);
				return null;
			}
			return j;
		}
		return null;
	}
	
	public final void report(Job job, int delta) {

		RESOURCE r = job.resourceCurrentlyNeeded();
		
		
		if (r != null) {
			if (delta == 1) {
				d().jobs.reportPresence(job.jobCoo().x(), job.jobCoo().y(), r);
				
			}else if(delta == -1) {
				d().jobs.reportAbsence(job.jobCoo().x(), job.jobCoo().y(), r);
			}
			return;
		}
		
		FindableDataSingle s = d().job;
		if (job.needsRipe())
			s = d().jobHarvest;
		
		if (delta == 1) {
			s.reportPresence(job.jobCoo().x(), job.jobCoo().y());
		}else if(delta == -1) {
			s.reportAbsence(job.jobCoo().x(), job.jobCoo().y());
		}
	
	}
	
	final static class SFinderUpdater {

		private final Bitmap1D tryShort = new Bitmap1D(Short.MAX_VALUE, false);
		private final Bitmap1D distanceFailed = new Bitmap1D(Short.MAX_VALUE, false);
		private final Bitsmap1D distance = new Bitsmap1D(0, 2, Short.MAX_VALUE);
		private final Bitsmap1D distanceTimeout = new Bitsmap1D(0, 2, Short.MAX_VALUE);
		
		private final double speed = 1.0/32.0;
		double ci = 0;
		int roundabout = 0;
		
		private final int[] dists = new int[] {
			150,
			400,
			1000,
			Integer.MAX_VALUE,
		};
		
		public SFinderUpdater() {

		}
		
		public void update(double ds) {
			int old = (int) ci;
			ci += ds*SETT.PATH().comps.levels.get(0).componentsMax()*speed;
			int now = (int) ci;
			int delt = old-now;
			
			if (ci >= SETT.PATH().comps.levels.get(0).componentsMax()) {
				roundabout ++;
				ci -= SETT.PATH().comps.levels.get(0).componentsMax();
			}
			
			for (int k = 0; k <= delt; k++) {
				int i = k+old;
				i %= SETT.PATH().comps.levels.get(0).componentsMax();
				tryShort.set(i, false);
				
				if (distanceFailed.get(i)) {
					
					distanceFailed.set(i, false);
					distance.inc(i, 1);
					distanceTimeout.set(i, (roundabout-1) & 3);
				}else if ((roundabout & 3) == distanceTimeout.get(i)){
					distance.set(i, 0);
					distanceTimeout.set(i, 0);
				}
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
		
		public boolean tryDistance(int tx, int ty) {
			
			if (STATS.WORK().workforce() - STATS.WORK().EMPLOYED.stat().data().get(null) < 80)
				return true;
			
			SComponent c = SETT.PATH().comps.levels.get(0).get(tx, ty);
			if (c == null)
				return false;
			return !distanceFailed.get(c.index()) || distance.get(c.index()) != distance.maxValue();
			
		}
		
		public int distance(int tx, int ty) {
			
			if (STATS.WORK().workforce() - STATS.WORK().EMPLOYED.stat().data().get(null) < 80)
				return Integer.MAX_VALUE;
			
			SComponent c = SETT.PATH().comps.levels.get(0).get(tx, ty);
			return dists[distance.get(c.index())];
		}
		
		public void distanceFail(int tx, int ty) {
			SComponent c = SETT.PATH().comps.levels.get(0).get(tx, ty);
			distanceFailed.set(c.index(), true);
			failShort(tx, ty);
		}
		


		
	}


}
