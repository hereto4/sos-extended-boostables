package settlement.thing;

import static settlement.main.SETT.THINGS;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import settlement.misc.util.TILE_STORAGE;
import settlement.room.main.Room;
import settlement.thing.THINGS.Thing;
import settlement.thing.ThingsResources.ScatteredResource;
import snake2d.util.datatypes.AREA;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import view.sett.IDebugPanelSett;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

class ThingPlacer {

	static void init() {
		
		LinkedList<PLACABLE> pl = new LinkedList<>();
		for (RESOURCE r : RESOURCES.ALL()) {
			pl.add(new PlacableMulti(r.name) {
				
				@Override
				public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
					SETT.THINGS().resources.createPrecise(tx, ty, r, 1+RND.rInt(29));
				}
				
				@Override
				public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
					// TODO Auto-generated method stub
					return null;
				}
			});
			
		}
		
		pl.add(new PlacableMulti("resources all") {
			
			int ri = 0;
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				SETT.THINGS().resources.createPrecise(tx, ty, RESOURCES.ALL().get(ri), 64);
				ri++;
				ri %= RESOURCES.ALL().size();
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE type) {
				return null;
			}
		});
		
		pl.add(new PlacableMulti("resource increase") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				for (Thing t : THINGS().get(tx, ty)) {
					if (t instanceof ScatteredResource) {
						int a =((ScatteredResource) t).amount();
						a -= (a/10)*10;
						a = 10-a;
						if (((ScatteredResource) t).amount() + a < ThingsResources.MAX_AMOUNT)
							THINGS().resources.createPrecise(tx, ty, ((ScatteredResource) t).resource(), a);
						return;
					}
				}
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE type) {
				for (Thing t : THINGS().get(tx, ty)) {
					if (t instanceof ScatteredResource)
						return null;
				}
				return "";
			}
		});
		
		pl.add(new PlacableMulti("increase crate") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				Room r = SETT.ROOMS().map.get(tx, ty);
				if (r != null) {
					TILE_STORAGE c = SETT.MAPS().STORAGE.get(tx, ty);
					for (int i = 0; i < 16 && c.resource() != null && c != null && c.storageReservable() > 0; i++) {
						c.storageReserve(1);
						c.storageDeposit(1);
					}
					
				}
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE type) {
				if (SETT.MAPS().STORAGE.get(tx, ty) != null) {
					return null;
				}
				return "";
			}
		
		});
		

		
		pl.add(new PlacableMulti("remove things") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				LIST<Thing> l = THINGS().get(tx, ty);
				if (!l.isEmpty())
					l.get(0).remove();
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE type) {
				LIST<Thing> l = THINGS().get(tx, ty);
				if (l.isEmpty())
					return "";
				return null;
			}
		
		});
		
		IDebugPanelSett.add("resources", pl);
		
	}
	
}
