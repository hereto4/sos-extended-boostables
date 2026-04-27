package world.map.regions;

import static world.WORLD.FOREST;
import static world.WORLD.GROUND;
import static world.WORLD.MOUNTAIN;
import static world.WORLD.REGIONS;
import static world.WORLD.WATER;

import game.faction.FACTIONS;
import init.paths.PATHS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.rnd.RND;
import world.WORLD;

final class GenName {

	private final Json jland = new Json(PATHS.NAMES().get("WorldAreas"));
	private final LandCounter cMisc = new LandCounter(jland.texts("MISC"), 0.2) {
			@Override
			boolean count(int tx, int ty) {
				return true;
			}
		};
	

	private final CharSequence[] sIslands  = jland.texts("ISLAND_ADDONS");
		
	private final LandCounter[] counts = new LandCounter[] {
		new LandCounter(jland.texts("MOUNTAIN"), 0.2) {
			@Override
			boolean count(int tx, int ty) {
				return MOUNTAIN().is(tx, ty);
			}
		},
		new LandCounter(jland.texts("FOREST"), 0.4) {
			@Override
			boolean count(int tx, int ty) {
				return FOREST().is.is(tx, ty);
			}
		},
		new LandCounter(jland.texts("RIVER"), 0.1) {
			@Override
			boolean count(int tx, int ty) {
				return WATER().isRivery.is(tx, ty);
			}
		},
		new LandCounter(jland.texts("OCEAN"), 0.15) {
			@Override
			boolean count(int tx, int ty) {
				return WATER().OCEAN.normal.is(tx, ty);
			}
		},
		new LandCounter(jland.texts("LAKE"), 0.15) {
			@Override
			boolean count(int tx, int ty) {
				return WATER().LAKE.normal.is(tx, ty);
			}
		},
		new LandCounter(jland.texts("STEPPE"), 0.6) {
			@Override
			boolean count(int tx, int ty) {
				return ty < WORLD.THEIGHT()/2 && GROUND().getter.get(tx, ty).moisture() < 0.2;
			}
		},
		new LandCounter(jland.texts("DESERT"), 0.6) {
			@Override
			boolean count(int tx, int ty) {
				return ty > WORLD.THEIGHT()/2 && GROUND().getter.get(tx, ty).moisture() < 0.2;
			}
		},
	};
	
	public GenName() {
		for (int ri = 1; ri < WREGIONS.MAX; ri++) {
			Region reg = WORLD.REGIONS().getByIndex(ri);
			init(reg);
		}
		WORLD.REGIONS().player.info.name().clear().add(FACTIONS.player().name);
	}


	
	void init(Region r) {
		if (r == null || r.info.area() == 0)
			return;
		
		for (LandCounter c : counts) {
			c.count = 0;
			c.value = 0;
		}
		
		for (COORDINATE coo : r.info.bounds()) {
			if (!REGIONS().map.is(coo, r))
				continue;
			for (LandCounter c : counts) {
				if (c.count(coo.x(), coo.y()))
					c.count++;
			}
		}
		
		LandCounter bestFit = cMisc;
		
		for (LandCounter c : counts) {
			c.value = c.count/(double)r.info.area();
			c.value /= c.treshold;
			if (c.value > 1.0 && c.value > bestFit.value)
				bestFit = c;
		}
		
		if (isIsland(r)) {
			r.info.name().clear().add(sIslands[RND.rInt(sIslands.length)]);
			r.info.name().insert(0, bestFit.getName());
		}else {
			r.info.name().clear().add(bestFit.getName());
		}

	}

	private boolean isIsland(Region r) {
		for (COORDINATE c : r.info.bounds()) {
			if (!REGIONS().map.is(c, r))
				continue;
			for (DIR d : DIR.ORTHO)
				if (!REGIONS().map.is(c, d, r) && !WATER().has.is(c)) {
					return false;
				}
		}
		return true;
	}
	
	abstract class LandCounter {
		
		private final String[] names;
		private int nameI =0;
		private double treshold;
		private double value;
		
		LandCounter(String[] names, double treshold){
			for (int i = 0; i < names.length; i++) {
				String old = names[i];
				int k = RND.rInt(names.length);
				names[i] = names[k];
				names[k] = old;
			}
			
			this.names = names;
			this.treshold = treshold;
		}
		
		String getName() {
			int i = nameI;
			nameI++;
			if (nameI >= names.length)
				nameI = 0;
			return names[i];
		}
		
		int count;
		abstract boolean count(int tx, int ty);
		
	}
	
}
