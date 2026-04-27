package settlement.path.components;

import static settlement.main.SETT.ENTITIES;
import static settlement.main.SETT.JOBS;
import static settlement.main.SETT.PATH;
import static settlement.main.SETT.ROOMS;
import static settlement.main.SETT.THEIGHT;
import static settlement.main.SETT.THINGS;
import static settlement.main.SETT.TWIDTH;

import init.resources.RESOURCE;
import settlement.entity.ENTITY;
import settlement.entity.animal.Animal;
import settlement.entity.humanoid.Humanoid;
import settlement.job.Job;
import settlement.main.SETT;
import settlement.misc.util.FINDABLE;
import settlement.misc.util.RESOURCE_TILE;
import settlement.misc.util.TILE_STORAGE;
import settlement.path.finders.SFinderFindable;
import settlement.room.home.house.HomeInstance;
import settlement.room.main.Room;
import settlement.thing.THINGS.Thing;
import settlement.thing.ThingFindable;
import settlement.thing.ThingsResources.ScatteredResource;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.GUTIL;
import util.data.DataOSimple;

public final class FindableDatas {
	
	
	public final FindableDataRes resScattered;
	public final FindableDataRes resCrate;
	public final FindableDataRes resPriority;
	public final FindableDataRes storage;
	public final FindableDataRes maintenanceRes;
	public final FindableDataSingle maintenance;
	public final FindableDataRes jobs;
	public final LIST<FindableDataRes> RESSES;
	
	private final FindableDataSingle[] findable;
	
	public final FindableDataSingle job;
	public final FindableDataSingle jobHarvest;
	private final FindableDataSingle[] people;
	public final FindableDataSingle reservableAnimals;
	public final FindableDataHome home;
	public final LIST<FindableDataSingle> SINGLES;
	
	FindableDatas() {
		
		FindableData.datao = new DataOSimple<SComponent>() {

			@Override
			protected long[] data(SComponent t) {
				return t.fdata;
			}
		
		};
		FindableData.all.clear();
		FindableDataSingle.all.clear();
		FindableDataRes.all.clear();		
		
		{
			findable = new FindableDataSingle[SFinderFindable.all().size()];
			for (int i = 0; i < findable.length; i++)
				findable[i] = new FindableDataSingle(SFinderFindable.all().get(i).name);
		}
		
		resScattered = new FindableDataRes("R");
		resCrate = new FindableDataRes("Crate");
		resPriority = new FindableDataRes("Prio");
		storage = new FindableDataRes("Store");
		
		maintenanceRes = new FindableDataRes("Ma");
		maintenance = new FindableDataSingle("Maintain");
		jobs = new FindableDataRes("jobs");
		job = new FindableDataSingle("Job");

		jobHarvest = new FindableDataSingle("Job Harvest");
		people = new FindableDataSingle[] {
			new FindableDataSingle("Friendlies"),
			new FindableDataSingle("Enemies"),
		};
		reservableAnimals = new FindableDataSingle("Animals");
		home = new FindableDataHome();
		RESSES = new ArrayList<>(FindableDataRes.all);
		SINGLES = new ArrayList<>(FindableDataSingle.all);
	}
	
	public FindableDataSingle people(boolean friend) {
		return people[friend ? 0 : 1];
	}
	
	FindableDataSingle service(SFinderFindable f) {
		return findable[f.index];
	}
	
	
	void initComponent0(SComp0 c, RECTANGLE tiles) {

		c.clearData();
		
		
		int tx1 = tiles.x1() - (tiles.x1() > 0 ? 1 : 0);
		int tx2 = tiles.x2() + (tiles.x2() < TWIDTH ? 1 : 0);
		int ty1 = tiles.y1() - (tiles.y1() > 0 ? 1 : 0);
		int ty2 = tiles.y2() + (tiles.y2() < THEIGHT ? 1 : 0);
		
		
		for (int y = ty1; y < ty2; y++) {
			for (int x = tx1; x < tx2; x++) {
				if (!is(c, x, y))
					continue;
				
				for (Thing t : THINGS().get(x, y)) {
					
					if ((t instanceof ScatteredResource)) {
						ScatteredResource rw = (ScatteredResource) t;
						if (!rw.findableReservedCanBe())
							continue;
						resScattered.add(c, rw.resource());
					}else if (t instanceof ThingFindable) {
						ThingFindable ti = (ThingFindable) t;
						if (ti.findableReservedCanBe())
							findable[ti.finder().index].add(c);
						
					}
						
				}
				
				for (ENTITY ent : ENTITIES().getAtTile(x, y)) {
					if (ent instanceof Humanoid) {
						Humanoid a = (Humanoid) ent;
						people(!a.indu().hostile()).add(c);
					}else if (ent instanceof Animal) {
						if (((Animal)ent).huntReservable())
							reservableAnimals.add(c);
					}
				}
				
				if (SETT.PATH().finders().water.getReservable(x, y) != null)
					findable[SETT.PATH().finders().water.index].add(c);
				else if (PATH().finders.indoor.getReservable(x, y) != null)
					findable[SETT.PATH().finders().indoor.index].add(c);
				
				Job j = JOBS().getter.get(x, y);
				if (j != null && j.jobReserveCanBe()) {
					RESOURCE r = j.resourceCurrentlyNeeded();
					if (r != null) {
						jobs.add(c, j.resourceCurrentlyNeeded());
					}else if (j.needsRipe())
						jobHarvest.add(c);
					else
						job.add(c);
						
				}
				
				if (SETT.MAINTENANCE().reservable.is(x, y)) {
					RESOURCE res = SETT.MAINTENANCE().resource.get(x, y);
					if (res != null) {
						maintenanceRes.add(c, res);
					}else {
						maintenance.add(c);
					}
				}
				

				Room i = ROOMS().map.get(x, y);
				if (i == null)
					continue;
				
				{
					SFinderFindable se = i.blueprint().service(x, y);
					
					if (se != null) {
						
						FINDABLE t = se.getReservable(x, y);
						
						if (t != null )
							findable[se.index].add(c);
					}
				}
				
				{
					RESOURCE_TILE r = i.resourceTile(x, y);
					if (r != null && r.findableReservedCanBe()) {
						if (!r.isFindable()) {
							
						}else if (r.isPrio()) {
							resPriority.add(c, r.resource()); ;
						}else if (r.isStorage()) {
							resCrate.add(c, r.resource());
						}else {
							resScattered.add(c, r.resource());
						}
					}
				}
				
				{
					TILE_STORAGE s = i.storage(x, y);
					if (s != null && s.storageIsFindable() && s.resource() != null && s.storageReservable() > 0) {
						storage.add(c, s.resource());
					}
				}
				
				{
					HomeInstance h = SETT.ROOMS().HOME.service.get(x, y);
					if (h != null) {
						if (h.availability() != null) {
							home.add(c, h.availability());
						}
					}
				}
				
			}
		}
		
	}
	
	void initComponentN(SCompN c) {

		c.clearData();
		c.edgeMask = 0;
		
		GUTIL.filler().init(this);
		GUTIL.filler().fill(c.centreX(), c.centreY());
		
		SComponentLevel l = SETT.PATH().comps.all.get(c.level().level()-1);

		while(GUTIL.filler().hasMore()) {
			COORDINATE coo = GUTIL.filler().poll();
			SComponent s = l.get(coo);
			c.edgeMask |= s.hasEdge() ? 1 : 0;
			c.edgeMask |= s.hasEntry() ? 2 : 0;
			for (FindableData d : FindableData.all) {
				if (d.get(s) > 0) {
					d.add(c);
				}
			}
			
			SComponentEdge e = s.edgefirst();
			while(e != null) {
				if (e.to().superComp() == c) {
					GUTIL.filler().fill(e.to().centreX(), e.to().centreY());
				}
				e = e.next();
			}
			
		}
		
		GUTIL.filler().done();
	}

	private boolean is(SComponent c, int tx, int ty) {
		
		for (DIR d : DIR.ORTHO)
			if (c.is(tx, ty, d))
				return true;
		return c.is(tx, ty);
	}

}
