package settlement.main;

import static settlement.main.SETT.THEIGHT;
import static settlement.main.SETT.TWIDTH;

import java.io.IOException;

import game.GAME;
import game.boosting.BOOSTABLES;
import game.debug.Profiler;
import game.faction.FResources.RTYPE;
import game.time.TIME;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.type.POP_CL;
import settlement.main.SETT.SettResource;
import settlement.misc.util.RESOURCE_TILE;
import settlement.room.main.Room;
import settlement.thing.THINGS.Thing;
import settlement.thing.ThingsResources.ScatteredResource;
import snake2d.util.datatypes.AREA;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import util.updating.TileUpdater;
import view.sett.IDebugPanelSett;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

class TUpdater extends SettResource{
	
	private final double[] counts = new double[RESOURCES.ALL().size()];
	private final double degradePerYear = 1.0 / TIME.years().bitConversion(TIME.days());
	
	private final TileUpdater updater = new TileUpdater(TWIDTH, THEIGHT, TIME.secondsPerDay()) {
		
		@Override
		protected void update(int tx, int ty, int i, double timeSinceLast) {
			TUpdater.this.update(tx,ty,i);
		}
	};

	TUpdater() {
		super("TUPDATER", true);
		IDebugPanelSett.add(
		new PlacableMulti("update sett tile") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				update(tx, ty, tx+ty*TWIDTH);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return null;
			}
		});
		
	}
	
	private void update(int tx, int ty, int now) {
		
		SETT.TILE_MAP().updateTileDay(tx, ty, now);
		SETT.MAINTENANCE().updateTileDay(tx, ty, now);
		degrade(tx, ty, now);
	}
	
	private void degrade(int tx, int ty, int now) {
		
		double base = degradePerYear;
		
		
		double bonus = CLAMP.d(1.0/(BOOSTABLES.CIVICS().SPOILAGE.get(POP_CL.clP(null, null))), 0, 10);
		
		for (Thing t : SETT.THINGS().get(tx, ty)){
			double b = base;
			if (SETT.TERRAIN().get(now).roofIs())
				b *= 0.75;
			if (t instanceof ScatteredResource) {
				degrade((RESOURCE_TILE) t, b*2);
			}
		}
		
		Room r = SETT.ROOMS().map.get(tx, ty);
		
		if (r != null) {
			r.updateTileDay(tx, ty);
			
			RESOURCE_TILE t = r.resourceTile(tx, ty);
		
			if (t != null)
				degrade(t, bonus*base*t.spoilRate()*0.75);
		}
	}
	
	private void degrade(RESOURCE_TILE t, double value) {
		int r = t.reservable();
		if (r == 0)
			return;
		if (t.resource().degradeSpeed() == 0)
			return;
		
		value *= t.resource().degradeSpeed();
		if (value == 0)
			return;
		
		RESOURCE res = t.resource();
		
		counts[res.bIndex()] += value*t.reservable();
		int am = (int) counts[res.bIndex()];
		counts[res.bIndex()]-= am;
		
		
		int dec = 0;
		while(am > 0) {
			t.findableReserve();
			t.resourcePickup();
			dec ++;
			am--;
		}
		if (dec > 0) {
			GAME.player().res().inc(t.resource(), RTYPE.SPOILAGE, -dec);
		}
		
	}
	
	@Override
	protected void update(double ds, Profiler profiler) {
		updater.updateRandom(ds);
	}
	
	@Override
	protected void save(FilePutter file) {
		updater.save(file);
		file.ds(counts);
		super.save(file);
	}
	
	@Override
	protected void load(FileGetter file) throws IOException {
		updater.load(file);
		file.ds(counts);
		super.load(file);
	}
	
	@Override
	protected void clear() {
		updater.clear();
		for (int i = 0; i < counts.length; i++) {
			counts[i] =0;
		}
	}
	
}
