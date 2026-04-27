package settlement.entity.humanoid.ai.work;

import init.resources.RESOURCE;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.misc.job.SETT_JOB;
import settlement.room.main.Room;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;

abstract class PlanWork extends AIPLAN.PLANRES{

	public PlanWork(String key) {
		super(key);
	}

	static RoomInstance work(Humanoid a) {
		return STATS.WORK().EMPLOYED.get(a);
	}
	
	boolean shouldWork(Humanoid a, AIManager d) {
		return work(a) != null;
	}
	
	boolean hasEmployment(Humanoid a, AIManager d) {
		return work(a) != null && work(a).active() && !work(a).employees().isOverstaffed();
	}
	
	SETT_JOB jobGet(Humanoid a, AIManager d){
		if (shouldWork(a, d)) {
			SETT_JOB j = ((JOBMANAGER_HASER) work(a)).getWork().getJob(d.planTile);
			return j;
		}
		return null;
	}
	
	boolean jobIsReservedAndReserve(Humanoid a, AIManager d, RESOURCE r){
		if (shouldWork(a, d)) {
			SETT_JOB j = ((JOBMANAGER_HASER) work(a)).getWork().getJob(d.planTile);
			if (j != null) {
				if (j.jobReservedIs(r))
					return true;
				if (j.jobReserveCanBe()) {
					if (j.jobResourceBitToFetch() == null) {
						j.jobReserve(null);
						return jobGet(a, d) != null;
					}else if (r != null && r.bit.has(j.jobResourceBitToFetch())){
						j.jobReserve(r);
						return jobGet(a, d) != null;
					}
					return false;
				}
			}
		}
		return false;
	}
//	
	void jobCancel(Humanoid a, AIManager d, RESOURCE r) {
		if (shouldWork(a, d)) {
			Room room = work(a);
			if (room == null)
				return;
			if (room != null && room instanceof JOBMANAGER_HASER) {
				SETT_JOB j = ((JOBMANAGER_HASER) room).getWork().getJob(d.planTile);
				if (j != null && j.jobReservedIs(r))
					j.jobReserveCancel(r);
				
			}
		}
		
	}
	
	@Override
	protected String debug(Humanoid a, AIManager d) {
		return super.debug(a, d) + " w: " + work(a);
	}
	
	
	
}