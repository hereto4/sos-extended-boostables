package settlement.misc.job;

import init.resources.RBIT;
import init.resources.RESOURCE;
import snake2d.util.datatypes.COORDINATE;

public interface JOB_MANAGER {
	
	public SETT_JOB getReservableJob(COORDINATE prefered);
	
	public SETT_JOB reportResourceMissing(RBIT resourceMask, int jx, int jy);
	public void reportResourceFound(RESOURCE res);
	public boolean resourceReachable(RESOURCE res);
	public boolean resourceShouldSearch(RESOURCE res);
	public void resetResourceSearch();
	/**
	 * Get a job that might be reserved
	 * @param c
	 * @return
	 */
	public SETT_JOB getJob(COORDINATE c);

	public interface JOB_GETTER {
		
		public SETT_JOB init(int tx, int ty);
		
	}
	
}
