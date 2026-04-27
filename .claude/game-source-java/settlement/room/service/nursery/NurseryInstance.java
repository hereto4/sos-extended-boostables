package settlement.room.service.nursery;

import settlement.entity.ENTITY;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.misc.job.SETT_JOB;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.ROOM_PRODUCER_INSTANCE;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.job.JobPositions;
import settlement.room.main.util.RoomInit;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

final class NurseryInstance extends RoomInstance implements ROOM_PRODUCER_INSTANCE, JOBMANAGER_HASER{

	private static final long serialVersionUID = 1L;
	
	private final long[] pData;
	private final Jobs jobs;
	short kidspotsUsed = 0;
	short infantspotsUsed = 0;
	boolean searchFirst = false;
	short kidsAllowed;
	
	protected NurseryInstance(ROOM_NURSERY blue, TmpArea area, RoomInit init) {
		super(blue, area, init);
		pData = blue.productionData.makeData();
		
		jobs = new Jobs(this);
		
		employees().maxSet(jobs.size());
		kidsAllowed = (short) jobs.size();
		employees().neededSet((int) (Math.ceil(blue.constructor.workers.get(this))));
		
		activate();
	}
	
	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator it) {
		it.lit();
		return super.render(r, shadowBatch, it);
	}
	
	@Override
	protected void activateAction() {
		blueprintI().kidSpotsTotal += jobs.size();
	}

	@Override
	protected void deactivateAction() {
		blueprintI().kidSpotsTotal -= jobs.size();
	}

	@Override
	protected void updateAction(double updateInterval, boolean day) {
		blueprintI().productionData.updateRoom(this);
		jobs.searchAgain();
		if (day) {
			searchFirst = true;
		}
	}
	
	@Override
	public void updateTileDay(int tx, int ty) {
		blueprintI().util.updateDay(tx, ty);
	}

	@Override
	protected void dispose() {
		for (COORDINATE c : body()) {
			if (is(c)) {
				blueprintI().util.dispose(c.x(), c.y());
			}
		}
		for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
			if (e instanceof Humanoid) {
				Humanoid a = (Humanoid) e;
				HEvent.Handler.removeRoom(a, this);
				
			}
		}
		
	}
	@Override
	public ROOM_NURSERY blueprintI() {
		return (ROOM_NURSERY) blueprint();
	}


	@Override
	public long[] productionData() {
		return pData;
	}

	@Override
	public JobPositions<NurseryInstance> getWork() {
		if (!jobs.isSearching() && searchFirst) {
			jobs.searchAgain();
			searchFirst = false;
		}
			
		return jobs;
	}
	
	@Override
	public Industry industry() {
		return blueprintI().industries().get(0);
	}
	
	private static class Jobs extends JobPositions<NurseryInstance> {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public Jobs(NurseryInstance ins) {
			super(ins);
		}

		@Override
		protected boolean isAndInit(int tx, int ty) {
			return ins.blueprintI().ss.init(tx, ty);
		}

		@Override
		protected SETT_JOB get(int tx, int ty) {
			return ins.blueprintI().ss.init(tx, ty) ? ins.blueprintI().ss.job : null;
		}
		
		
	}

	@Override
	public int industryI() {
		// TODO Auto-generated method stub
		return 0;
	}

}
