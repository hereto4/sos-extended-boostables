package settlement.room.service.food.tavern;

import init.resources.RBIT.RBITImp;
import init.resources.RESOURCES;
import init.resources.ResGDrink;
import settlement.main.SETT;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.misc.job.JOB_MANAGER;
import settlement.misc.job.SETT_JOB;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.job.JobPositions;
import settlement.room.main.util.RoomInit;
import settlement.room.service.module.ROOM_SERVICER;
import settlement.room.service.module.RoomServiceInstance;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

final class TavernInstance extends RoomInstance implements JOBMANAGER_HASER, ROOM_SERVICER{
	
	private static final long serialVersionUID = 1L;
	final RoomServiceInstance service;
	final Jobs jobs;
	boolean auto = true;

	public final int maxAm;
	
	int tablesreserved = 0;
	private int amountTotal = 0;
	private int[] amounts;
	int[] incoming;
	final RBITImp fetch = new RBITImp();
	final RBITImp use = new RBITImp();
	
	protected TavernInstance(ROOM_TAVERN b, TmpArea area, RoomInit init) {
		super(b, area, init);
		
		jobs = new Jobs(this);
		jobs.setAlwaysNew();
		use.setAll();
		
		
		int sers = 0;
		for (COORDINATE c : body()) {
			if (is(c) && b.tile.servive(c.x(), c.y()) != null) {
				sers++;
			}
		}
		
		service = new RoomServiceInstance(sers, blueprintI().serviceData);
		
		employees().maxSet((int)Math.ceil(jobs.size()/2.0));
		employees().neededSet((int)Math.ceil(jobs.size()/4.0));
		activate();
		
		amounts = new int[RESOURCES.DRINKS().all().size()];
		incoming = new int[RESOURCES.DRINKS().all().size()];
		
		maxAm = sers*8;
		setMasks();
	}
	
	@Override
	protected void loadFix() {
		int[] na = new int[RESOURCES.DRINKS().all().size()];
		int[] in = new int[RESOURCES.DRINKS().all().size()];
		for (int i = 0; i < amounts.length; i++) {
			na[RESOURCES.DRINKS().MAP.loader().get(i).index()] = amounts[i];
			in[RESOURCES.DRINKS().MAP.loader().get(i).index()] = incoming[i];
		}
		amounts = na;
		incoming = in;
		setMasks();
		
		super.loadFix();
	}
	
	void setMasks() {
		fetch.clear();
		for (ResGDrink d : RESOURCES.DRINKS().all()) {
			if (use.has(d.resource)) {
				if (amounts[d.index()] + incoming[d.index()] < maxAm-4)
					fetch.or(d.resource);
			}else {
				
				if (amounts[d.index()] > 0)
					SETT.THINGS().resources.create(mX(), mY(), d.resource, amounts[d.index()]);
				blueprintI().amounts[d.index()] -= amounts[d.index()];
				blueprintI().total -= amounts[d.index()];
				amountTotal -= amounts[d.index()];
				amounts[d.index()] = 0;
				
				incoming[d.index()] = 0;
			}
		}
		jobs.resetResourceSearch();
	}
	
	public int amount(ResGDrink d) {
		if (d == null)
			return amountTotal;
		return amounts[d.index()];
	}
	
	void amountInc(ResGDrink d, int am) {
		amountTotal += am;
		amounts[d.index()] += am;
		blueprintI().amounts[d.index()] += am;
		blueprintI().total += am;
		setMasks();
	}
	
	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator i) {
		i.lit(); 
		return super.render(r, shadowBatch, i);
		
	}

	@Override
	protected void updateAction(double updateInterval, boolean day) {
		if (day)
			service.updateDay();
		jobs.searchAgain();
	}

	@Override
	protected void activateAction() {

	}

	@Override
	protected void deactivateAction() {

	}
	
	@Override
	public JOB_MANAGER getWork() {
		return jobs;
	}
	
	@Override
	protected void dispose() {
		use.clear();
		setMasks();
		service.dispose(blueprintI().serviceData);
	}

	@Override
	public ROOM_TAVERN blueprintI() {
		return (ROOM_TAVERN) blueprint();
	}

	@Override
	public RoomServiceInstance service() {
		return service;
	}

	@Override
	public double quality() {
		return ROOM_SERVICER.defQuality(this, blueprintI().constructor.coziness.get(this));
	}
	
	static class Jobs extends JobPositions<TavernInstance> {

		private static final long serialVersionUID = 1L;

		public Jobs(TavernInstance ins) {
			super(ins);
		}

		@Override
		protected SETT_JOB get(int tx, int ty) {
			return ins.blueprintI().tile.job(tx, ty);
		}

		@Override
		protected boolean isAndInit(int tx, int ty) {
			return ins.blueprintI().tile.job(tx, ty) != null;
		}
	}
	

}
