package settlement.path.finders;

import static settlement.main.SETT.ENTITIES;
import static settlement.main.SETT.JOBS;
import static settlement.main.SETT.PATH;
import static settlement.main.SETT.ROOMS;
import static settlement.main.SETT.THINGS;

import java.io.IOException;

import game.battle.Army;
import init.settings.S;
import settlement.entity.ENTITY;
import settlement.main.SETT;
import settlement.path.finders.SFinderFindable.FinderThing;
import settlement.path.finders.SFinderMisc.FinderArround;
import settlement.path.finders.SFinderMisc.Rnd;
import settlement.path.thread.SFinderRequest;
import settlement.thing.THINGS.Thing;
import settlement.thing.ThingsCorpses.Corpse;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import util.GUTIL;


public class SFINDERS {
	
	public final SFinderResources resource = new SFinderResources();
	public final SFinderResourceStorage storage = new SFinderResourceStorage();
	public final SFinderWater water = new SFinderWater();
	public final SFinderUnreachable reachable = new SFinderUnreachable();
	public final SFinderIndoors indoor = new SFinderIndoors();
	public final SFinderJob job = new SFinderJob();
	public final SFinderEntry entryPoints = new SFinderEntry();
	public final SFinderRND randomDistanceAway = new SFinderRND();
	public final FinderArround arround = new FinderArround();
	public final SFinderResourceStore jobStore = new SFinderResourceStore();
	public final SFinderPrey prey = new SFinderPrey();
	public final SFinderHumanoid otherHumanoid = new SFinderHumanoid();
	public final SFinderEntity entity = new SFinderEntity();
	public final Rnd rndCoo = new Rnd();
	public final SFinderHumanTarget target = new SFinderHumanTarget();
	public final SFinderHome home = new SFinderHome();
	public final SFinderMaintenance maintenance = new SFinderMaintenance();
	private SPathFinder finder;
	private final SFindersUpdater uper = new SFindersUpdater();
	
	private final SFinderSoldierManning[] soldierManning = new SFinderSoldierManning[] {
		new SFinderSoldierManning(true),
		new SFinderSoldierManning(false),
	};
	
	public SFINDERS(){
		if (S.get().developer)
			new Tests(this);
	}
	
	public SPathFinder finder() {
		if (finder == null)
			finder = new SPathFinder(SETT.PATH().comps, GUTIL.pathTools(), 2);
		return finder;
	}
	
	
	public final SFinderMisc resourceDump = new SFinderMisc(25) {
		@Override
		public boolean isTile(int tx, int ty) {
			if (ROOMS().map.is(tx, ty))
				return false;
			if (JOBS().getter.is(tx, ty))
				return false;
			return true;
		}
	};
	
	public void update(double ds) {
		jobStore.update(ds);
		job.update(ds);
		uper.update(ds);
	}
	
	public SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			uper.saver.save(file);
			
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			uper.saver.load(file);
		}
		
		@Override
		public void clear() {
			uper.saver.clear();
			
		}
	};
	
	public SFinderSoldierManning manning(Army a) {
		return soldierManning[a.index()];
	}

	public final SFinderRequest.FinderIdle getOutofWay = new SFinderRequest.FinderIdle();
	
	public final FinderThing<Corpse> corpses = new FinderThing<Corpse>("corpse") {

		@Override
		public Corpse getReservable(int tx, int ty) {
			for (Thing t : SETT.THINGS().get(tx, ty))
				if (t instanceof Corpse) {
					Corpse c = (Corpse) t;
					if (c.findableReservedCanBe())
						return c;
				}
			return null;
		}

		@Override
		public Corpse getReserved(int tx, int ty) {
			for (Thing t : SETT.THINGS().get(tx, ty))
				if (t instanceof Corpse) {
					Corpse c = (Corpse) t;
					if (c.findableReservedIs())
						return c;
				}
			return null;
		}
		
		
	};
	
	public boolean isGoodTileToStandOn(int tx, int ty, ENTITY e) {
		if (PATH().availability.get(tx, ty).player <= 0)
			return false;
		if (PATH().availability.get(tx, ty).player >=2)
			return false;
		if (JOBS().getter.is(tx, ty))
			return false;
		if (THINGS().getFirst(tx, ty) != null)
			return false;
		if (ENTITIES().hasAtTile(e, tx, ty))
			return false;
		if (PATH().huristics.getter.get(tx, ty) > 0.2)
			return false;
		return true;
	}

	
	
}
