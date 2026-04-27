package init.type;

import static settlement.main.SETT.GROUND;
import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.TERRAIN;

import init.paths.PATHS;
import settlement.main.SETT;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.info.INFO;
import util.keymap.RMAP;
import util.text.D;
import world.WORLD;

public final class TERRAINS {

	private static TERRAINS self;
	
	
	private final TERRAIN OCEAN;
	private final TERRAIN WET;
	private final TERRAIN MOUNTAIN;
	private final TERRAIN FOREST;
	private final TERRAIN NONE;
	private final INFO info;
	private final LIST<TERRAIN> all;
	private final RMAP<TERRAIN> map;
	
	TERRAINS() {
		self = this;
		D.t(TERRAINS.class);
		ArrayList<TERRAIN> ts = new ArrayList<>(40);
		String key = "TERRAIN";
		Json json = new Json(PATHS.CONFIG().get(key));
		info = new INFO(D.g("Terrain"), "");
		
		OCEAN = new TERRAIN(ts, "OCEAN", json, 
				D.g("Ocean"), 
				D.g("OceanD", "Salt water such as oceans. Fish is plentiful."), true) {

				@Override
				public SPRITE icon() {
					return WORLD.WATER().OCEAN.icon;
				}

				@Override
				public double value(int wx, int wy) {
					double res = 0;
					for (int di = 0; di < DIR.ORTHO.size(); di++) {
						if (WORLD.WATER().OCEAN.is.is(wx, wy, DIR.ORTHO.get(di)))
							res += 0.25;
					}
					return CLAMP.d(res, 0, 1);
				}

					
			
		};
		WET = new TERRAIN(ts, "WET", json, 
				D.g("Sweet", "Fresh water"), 
				D.g("SweetD", "Fresh water such as river beds or lakes. Offers natural irrigation, clay deposits and some fish."), true) {
			
			@Override
			public SPRITE icon() {
				return WORLD.WATER().LAKE.icon;
			}
			
			@Override
			public double value(int wx, int wy) {
				double res = 0;
				for (int di = 0; di < DIR.ORTHO.size(); di++) {
					if (WORLD.WATER().RIVER_SMALL.is(wx, wy, DIR.ORTHO.get(di)))
						res += 0.2;
					if (WORLD.WATER().LAKE.is.is(wx, wy, DIR.ORTHO.get(di)) || WORLD.WATER().isRivery.is(wx, wy, DIR.ORTHO.get(di)))
						res += 0.25;
				}
				return CLAMP.d(res, 0, 1);
			}
			
		};
		MOUNTAIN = new TERRAIN(ts, "MOUNTAIN", json, 
				D.g("Mountain"), 
				D.g("MountainD", "Rich in caverns and mineral deposits."), true) {

					@Override
					public SPRITE icon() {
						return SETT.TERRAIN().MOUNTAIN.getIcon();
					}

					@Override
					public double value(int wx, int wy) {
						double res = 0;
						for (int di = 0; di < DIR.ORTHO.size(); di++) {
							if (WORLD.MOUNTAIN().haser.is(wx, wy, DIR.ORTHO.get(di)))
								res += 0.25;
						}
						return CLAMP.d(res, 0, 1);
					}
			
		};
		FOREST = new TERRAIN(ts, "FOREST", json, 
				D.g("Forest"), 
				D.g("ForestD", "Forested areas. Good for lumber."), true) {

					@Override
					public SPRITE icon() {
						return WORLD.FOREST().icon;
					}

					@Override
					public double value(int wx, int wy) {
						double res = 0;
						for (int di = 0; di < DIR.ORTHO.size(); di++) {
							if (WORLD.MOUNTAIN().haser.is(wx, wy, DIR.ORTHO.get(di)))
								res += 0.25*WORLD.FOREST().amount.get(wx, wy);
						}
						return res;
					}
			
		};
		NONE = new TERRAIN(ts, "NONE", json, 
				D.g("OpenLand", "Open Land"), 
				D.g("Open Land", "Open land to roam about on."), true) {
			
			@Override
			public SPRITE icon() {
				return WORLD.GROUND().icon;
			}

			@Override
			public double value(int wx, int wy) {
				double res = 1;
				for (int ti = 0; ti < ALL().size(); ti++) {
					if (ti == index())
						continue;
					res -= ALL().get(ti).value(wx, wy);
				}
				
				return CLAMP.d(res, 0, 1);
			}
			
		};
		all = new ArrayList<>(ts);
		KeyMap<TERRAIN> m = new KeyMap<>();
		for (TERRAIN t : all)
			m.put(t.key, t);
		map = new RMAP<TERRAIN>(key,all);
	}
	
	public static final MAP_OBJECT<TERRAIN> sett = new MAP_OBJECT<TERRAIN>() {

		@Override
		public TERRAIN get(int tile) {
			throw new RuntimeException();
		}

		@Override
		public TERRAIN get(int tx, int ty) {
			if (!IN_BOUNDS(tx, ty))
				return NONE();
			if (TERRAIN().TREES.isTree(tx, ty))
				return FOREST();
			if (TERRAIN().MOUNTAIN.isMountain(tx, ty))
				return MOUNTAIN();
			if (TERRAIN().WATER.SHALLOW.is(tx, ty) && GROUND().types.SAND.is(tx, ty))
				return OCEAN();
			if (TERRAIN().WATER.SHALLOW.is(tx, ty))
				return WET();
			return NONE();
		}
	
	};
	
	public static final MAP_OBJECT<TERRAIN> world = new MAP_OBJECT<TERRAIN>() {

		@Override
		public TERRAIN get(int tile) {
			throw new RuntimeException();
		}

		@Override
		public TERRAIN get(int tx, int ty) {
			if (!WORLD.IN_BOUNDS(tx, ty))
				return NONE();
			if (WORLD.MOUNTAIN().is(tx, ty))
				return MOUNTAIN();
			if (WORLD.WATER().OCEAN.is.is(tx, ty))
				return OCEAN();
			if (WORLD.WATER().fertile.is(tx, ty))
				return WET();
			if (WORLD.FOREST().is.is(tx, ty))
				return FOREST();
			return NONE();
		}
	
	};
	
	public static LIST<TERRAIN> ALL(){
		return self.all;
	}
	
	public static RMAP<TERRAIN> MAP(){
		return self.map;
	}
	
	public static TERRAIN OCEAN() {
		return self.OCEAN;
	}
	
	public static TERRAIN WET() {
		return self.WET;
	}
	public static TERRAIN MOUNTAIN() {
		return self.MOUNTAIN;
	}
	public static TERRAIN FOREST() {
		return self.FOREST;
	}
	public static TERRAIN NONE() {
		return self.NONE;
	}
	
	public static INFO INFO() {
		return self.info;
	}
	
	
	
}
