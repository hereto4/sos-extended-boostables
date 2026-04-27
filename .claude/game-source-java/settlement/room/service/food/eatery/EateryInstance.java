package settlement.room.service.food.eatery;

import game.GAME;
import game.faction.FResources.RTYPE;
import init.resources.RBIT;
import init.resources.RBIT.RBITImp;
import init.resources.RESOURCES;
import init.resources.ResG;
import init.resources.ResGEat;
import settlement.main.SETT;
import settlement.misc.job.JOBMANAGER_HASER;
import settlement.misc.job.JOB_MANAGER;
import settlement.misc.job.SETT_JOB;
import settlement.misc.util.RESOURCE_TILE;
import settlement.misc.util.TILE_STORAGE;
import settlement.room.industry.module.Industry;
import settlement.room.industry.module.ROOM_PRODUCER_INSTANCE;
import settlement.room.main.RoomInstance;
import settlement.room.main.TmpArea;
import settlement.room.main.job.JobIterator;
import settlement.room.main.util.RoomInit;
import settlement.room.main.util.RoomState;
import settlement.room.main.util.RoomState.RoomStateInstance;
import settlement.room.service.food.eatery.Crate.Service;
import settlement.room.service.module.ROOM_SERVICER;
import settlement.room.service.module.RoomServiceInstance;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

final class EateryInstance extends RoomInstance implements JOBMANAGER_HASER, ROOM_PRODUCER_INSTANCE, ROOM_SERVICER{

	private final static long serialVersionUID = -7063521835843676015l;
	
	boolean autoE;
	private long[] pdata;
	private int[] amounts = new int[RESOURCES.EDI().all().size()];
	private int[] jobReserved = new int[RESOURCES.EDI().all().size()];
	private int serviceReserved = 0;
	private int amountTotal = 0;
	final int maxAmount;
	private final JobIterator jobs;
	private final RBITImp fetchMask = new RBITImp().clearSet(RESOURCES.EDI().mask);
	private final RBITImp useMask = new RBITImp();
	final RoomServiceInstance service;

	EateryInstance(ROOM_EATERY p, TmpArea area, RoomInit init) {
		super(p, area, init);

		maxAmount = 4*(int) blueprintI().constructor.storage.get(this);
		jobs = new JobIterator(this) {
			private static final long serialVersionUID = 1L;

			@Override
			protected SETT_JOB init(int tx, int ty) {
				return blueprintI().crate.job(tx, ty);
			}
		};
		jobs.setAlwaysNewJob();
		
		int m = 0;
		for (COORDINATE c : body()) {
			if (is(c) && blueprintI().constructor.isCrate(c.x(), c.y()))
				m++;
		}
		pdata = blueprintI().industry.makeData();
		service = new RoomServiceInstance(m, blueprintI().service);
		employees().maxSet(m);
		employees().neededSet((int) Math.ceil(blueprintI().constructor.workers.get(this)));
		activate();
		
		useMask.clear();
		for (int ei = 0; ei < RESOURCES.EDI().all().size(); ei++) {
			ResGEat e = RESOURCES.EDI().all().get(ei);
			if (blueprintI().instancesSize() > 1 && !blueprintI().uses(e)) {
				;
			}else if (e.serve) {
				useMask.or(e.resource);
			}
		}
	
		
		for (ResGEat e : RESOURCES.EDI().all()) {
			setMask(e);
		}
	}

	@Override
	protected boolean render(Renderer r, ShadowBatch shadowBatch, RenderData.RenderIterator it) {
		super.render(r, shadowBatch, it);
		it.lit();
		return false;
	}

	@Override
	protected void loadFix() {
		pdata = industry().makeDataFix(pdata);
		if (amounts.length != RESOURCES.EDI().all().size()) {
			int[] ams = new int[RESOURCES.EDI().all().size()];
			int[] amsI = new int[RESOURCES.EDI().all().size()];
			amountTotal = 0;
			fetchMask.clear();
			fetchMask.or(RESOURCES.EDI().mask);
			useMask.clear();
			for (int i = 0; i < ams.length; i++) {
				ams[i] = amounts[i%amounts.length];
				amsI[i] = jobReserved[i%amounts.length];
				amountTotal += ams[i];
			}
			this.amounts = ams;
			this.jobReserved = amsI;
		}
	}
	
	@Override
	protected void updateAction(double ds, boolean day) {
		if (day)
			service.updateDay();
		blueprintI().industry.updateRoom(this);
		jobs.searchAgain();
		if (!active() || employees().employed() <= 0)
			return;
		
	}
	
	@Override
	public void updateTileDay(int tx, int ty) {
		Service s = blueprintI().crate.service(tx, ty);
		if (s != null)
			s.check();
	}
	
	public int amount(ResG e) {
		return amounts[e.index()];
	}
	
	public int amountTotal() {
		return amountTotal;
	}
	
	public int jobReserved(ResG e) {
		return jobReserved[e.index()];
	}
	
	public int serviceReserved() {
		return serviceReserved;
	}
	
	
	public RBIT fetchMask() {
		return fetchMask;
	}
	
	public boolean uses(ResG e) {
		return useMask.has(e.resource);
	}
	
	public void usesToggle(ResG e) {
		useMask.toggle(e.resource);
		dump(e);
		for (COORDINATE c : body()) {
			if (is(c)) {
				Service ss = blueprintI().crate.service(c.x(), c.y());
				if (ss != null)
					ss.check();
			}
		}
		setMask(e);
	}

	private void dump(ResG e) {
		if (!useMask.has(e.resource)) {
			int am = amounts[e.index()];
			amounts[e.index()] = 0;
			amountTotal -= am;
			blueprintI().total -= am;
			blueprintI().amounts[e.index()] -= am;
			if (am > 0) {
				SETT.THINGS().resources.create(mX(), mY(), e.resource, am);
			}
		}

	}
	

	void jobTally(ResG e, int dReserved, int dAmount) {
		amounts[e.index()] += dAmount;
		amountTotal += dAmount;
		blueprintI().total += dAmount;
		blueprintI().amounts[e.index()] += dAmount;
		jobReserved[e.index()] += dReserved;
		dump(e);
		setMask(e);
	}
	
	void serviceTally(int dReserved) {
		serviceReserved += dReserved;
	}
	
	void consume(ResG e, int amount, int tx, int ty) {
		if (amount <= 0 || amounts[e.index()]+amount < 0)
			GAME.Notify("here " + amounts[e.index()] + " " + amount);
		jobTally(e, 0, -amount);
		blueprintI().industry.ins().get(e.index()).inc(this, amount, false);
		GAME.player().res().inc(e.resource, RTYPE.CONSUMED, -amount);
		blueprintI().crate.service(tx, ty).check();
	}
	
	private void setMask(ResG e) {
		if (amounts[e.index()] + jobReserved[e.index()]*4 <= maxAmount-4) {
			fetchMask.or(e.resource);
		}else {
			fetchMask.clear(e.resource);
		}
		fetchMask.and(useMask);
		if (fetchMask.isClear()) {
			jobs.dontSearch();
		}else {
			jobs.searchAgainWithoutResources();
		}
		jobs.resetResourceSearch();
	}
	
	@Override
	protected void dispose() {
		
		int amI = 0;
		for (COORDINATE c : body()) {
			if (is(c)) {
				int a = amounts[amI];
				if (a > 0)
					SETT.THINGS().resources.create(c, RESOURCES.EDI().all().get(amI).resource, a);
				blueprintI().total -= a;
				blueprintI().amounts[amI] -= a;
				amI++;
				if (amI == amounts.length)
					return;
			}
		}
		
		
		
		for (COORDINATE c : body()) {
			if (is(c)) {
				blueprintI().crate.dispose(c.x(), c.y());
			}
		}
		service.dispose(blueprintI().service);
		
	}
	
	@Override
	public ROOM_EATERY blueprintI() {
		return (ROOM_EATERY) blueprint();
	}
	
	@Override
	public RESOURCE_TILE resourceTile(int tx, int ty) {
		return null;
	}
	
	@Override
	public TILE_STORAGE storage(int tx, int ty) {
		return null;
	}

	@Override
	protected void activateAction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void deactivateAction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JOB_MANAGER getWork() {
		return jobs;
	}

	@Override
	public long[] productionData() {
		return pdata;
	}
	
	@Override
	public Industry industry() {
		return blueprintI().industry;
	}

	@Override
	public RoomServiceInstance service() {
		return service;
	}

	@Override
	public double quality() {
		return ROOM_SERVICER.defQuality(this, 1);
	}

	@Override
	public int industryI() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public RoomState makeState(int tx, int ty, boolean broken) {
		return new State(this, broken);
	}
	
	
	private static class State extends RoomStateInstance {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final RBITImp useMask = new RBITImp();
		
		
		public State(EateryInstance ins, boolean broken) {
			super(ins);
			useMask.clearSet(ins.useMask);
			
		}
		
		@Override
		public void applyIns(RoomInstance ins) {
			if (ins instanceof EateryInstance) {
				EateryInstance s = (EateryInstance) ins;
				for (ResGEat g : RESOURCES.EDI().all())
					if (useMask.has(g.resource) != s.useMask.has(g.resource)) {
						((EateryInstance) ins).usesToggle(g);
					}
			}
			
		}
		
		
	}

}