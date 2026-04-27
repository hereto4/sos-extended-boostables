package world.map.landmark;

import static world.WORLD.LANDMARKS;
import static world.WORLD.MOUNTAIN;
import static world.WORLD.TBOUNDS;
import static world.WORLD.THEIGHT;
import static world.WORLD.TWIDTH;
import static world.WORLD.WATER;

import init.paths.PATHS;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.Polymap;
import snake2d.util.rnd.RND;
import util.GUTIL;
import world.WORLD;

final class GeneratorLandmark {


	private final Polymap polly = new Polymap(TWIDTH(), THEIGHT(), (int) (40*(TWIDTH()/250.0)), 1.0);
	private final WorldLandmark rubbish = LANDMARKS().getByIndex(0);
	
	private final Json json = new Json(PATHS.NAMES().get("WorldLandmarks"));
	
	private final Type[] types = new Type[] {
		new Type(json, "MOUNTAIN", 40, 1000) {

			@Override
			public boolean is(int tx, int ty) {
				return MOUNTAIN().heighter.get(tx, ty) > 0 && !WATER().has.is(tx,ty);
			}
		},
		new Type(json, "LAKE", 20, 10000) {

			@Override
			public boolean is(int tx, int ty) {
				return WATER().LAKE.is.is(tx, ty);
			}
		},
		new Type(json, "RIVER", 25, 100) {

			@Override
			public boolean is(int tx, int ty) {
				return WATER().isRivery.is(tx, ty);
			}
		},
		new Type(json, "OCEAN", 50, 5000) {

			@Override
			public boolean is(int tx, int ty) {
				return WATER().OCEAN.is.is(tx, ty);
			}
		},
	};
	
	public GeneratorLandmark(ACTION loadprint) {
		
		loadprint.exe();
		
		WORLD.LANDMARKS().saver().clear();
		
		int nr = 1;
		
		for (COORDINATE c : TBOUNDS()) {
			if (nr >= WorldLandmarks.MAX)
				break;
			if (assignTerrain(c.x(), c.y(), LANDMARKS().getByIndex(nr)))
				nr++;
		}
		
		
		for (COORDINATE c : TBOUNDS()) {
			if (LANDMARKS().setter.get(c) == rubbish) {
				LANDMARKS().setter.set(c, null);
				continue;
			}
			
		}
		loadprint.exe();
		
		
		new GeneratorLandmarkValidator(null);

		
		loadprint.exe();
	}
	
	private boolean assignTerrain(int tx, int ty, WorldLandmark ass) {
		
		if (LANDMARKS().setter.get(tx, ty) != null) {
			return false;
		}
		
		Type type = type(tx, ty);
		if (type == null)
			return false;
		
		
		polly.checkInit();
		polly.checker.set(tx, ty, true);
		
		

		int minSize = type.minSize;
		int maxSize = type.maxSize;
		
		
		GUTIL.flooder().init(this);
		GUTIL.flooder().pushSloppy(tx, ty, 0);
		int area = 0;
		
		WorldLandmark neigh = null;
		
		while(GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollSmallest();
			
			LANDMARKS().setter.set(t, ass);
			
			
			
			area++;
			
			if (area > maxSize)
				break;
			
			
			for (DIR d : DIR.ORTHO) {
				int dx = t.x()+d.x();
				int dy = t.y()+d.y();
				if (TBOUNDS().holdsPoint(dx, dy)) {
					if (!type.is(dx, dy))
						continue;
					
					WorldLandmark kuk = LANDMARKS().setter.get(dx, dy);
					if (kuk != null) {
						if (kuk != rubbish && kuk != ass)
							neigh = kuk;
						continue;
					}
						
					
					double q = t.getValue() + d.tileDistance();
					double dd = polly.checker.is(t, d) ? q : q+100;
					polly.checker.set(t, true);
					GUTIL.flooder().pushSmaller(t, d, dd);
				}
			}
			
		}
		
		GUTIL.flooder().done();
		
		if (area < minSize) {
			if (neigh != null) {
				assign(tx, ty, ass, neigh);
			}else {
				assign(tx, ty, ass, rubbish);
			}
			return false;
		}
		
		type.init(ass);
		return true;
	}
	
	
	private void assign(int tx, int ty, WorldLandmark old, WorldLandmark newa) {
		
		GUTIL.flooder().init(this);
		
		GUTIL.flooder().pushSloppy(tx, ty, 0);
		
		
		while(GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollSmallest();
			if (LANDMARKS().setter.get(t) != old)
				continue;
			LANDMARKS().setter.set(t, newa);
			for (DIR d : DIR.ORTHO) {
				if (TBOUNDS().holdsPoint(t, d))
					GUTIL.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
			}
			
		}
		
		GUTIL.flooder().done();
	}
	
	public Type type(int tx, int ty) {
		for (Type t : types)
			if (t.is(tx, ty))
				return t;
		return null;
	}

	
	private abstract static class Type implements MAP_BOOLEAN{
		
		private final String[] names;
		private final String[] addons;
		private int nameI = 0;
		private final Json[] specials;
		private int sI = 0;
		private int minSize;
		private int maxSize;
		
		Type(Json json, String key, int min, int max){
			json = json.json(key);
			this.minSize = min;
			this.maxSize = max;
			names = json.texts("NAMES");
			for (int i = 0; i < names.length; i++) {
				int k = RND.rInt(names.length);
				String o = names[i];
				names[i] = names[k];
				names[k] = o;
			}
			addons = json.texts("ADDONS");
			specials = json.jsons("SPECIAL");
			
			for (int i = 0; i < specials.length; i++) {
				int k = RND.rInt(specials.length);
				Json o = specials[i];
				specials[i] = specials[k];
				specials[k] = o;
			}
		}
		
		@Override
		public boolean is(int tile) {
			return false;
		}
		
		void init(WorldLandmark l) {
			
			
			if (sI < specials.length) {
				l.name.clear().add(specials[sI].text("NAME"));
				l.description.clear().add(specials[sI].text("LORE"));
				if (l.description.length() > 1024)
					specials[sI].error("Lore is too long...", "LORE");
				sI ++;
			}else {
				if (names.length == 0) {
					l.name.clear().add(l.index);
				}else {
					if (addons.length > 0) {
						l.name.clear().add(addons[RND.rInt(addons.length)]);
						l.name.insert(0, names[nameI++]);
					}else
						l.name.clear().add(names[nameI++]);
					if (nameI >= names.length)
						nameI = 0;
				}
			}
		}
		
		
	}
	
}
