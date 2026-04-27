package settlement.maintenance;

import java.io.IOException;

import game.GAME;
import game.boosting.BOOSTABLES;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import init.type.POP_CL;
import settlement.main.SETT;
import settlement.main.SETT.SettResource;
import settlement.room.main.Room;
import snake2d.util.datatypes.AREA;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_BOOLEANE;
import snake2d.util.map.MAP_DOUBLE;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.Bitmap2D;
import snake2d.util.sets.Bitsmap2D;
import snake2d.util.sprite.SPRITE;
import util.keymap.MAPSAVE;
import view.sett.IDebugPanelSett;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

public final class MAINTENANCE extends SettResource{

	private final Bitmap2D pisser = new Bitmap2D(SETT.TILE_BOUNDS, false);
	private final Bitmap2D preserved = new Bitmap2D(SETT.TILE_BOUNDS, false);
	private final Bitmap2D pdisabled = new Bitmap2D(SETT.TILE_BOUNDS, false);
	
	private final Bitsmap2D bresource = new Bitsmap2D(0, 4, SETT.TILE_BOUNDS);
	public final PLACABLE enablePlacer = new PlacerDormant();
	private final MConsumption cons = new MConsumption(this);
	
	public final double tilesPerDay = 1.0/48.0;
	public final double resRate = 1.0/64.0;
	public final SPRITE icon = UI.icons().s.degrade;
	
	final MType[] types = new MType[] {
		new MRoom(),
		new MFloor(),
	
	};
	
	public MAINTENANCE() {
		super("MAINTENANCE", false);
		
		IDebugPanelSett.add(new PlacableMulti("MAINTENANCE_DEGRADEx4") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				vandalise(tx, ty);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		
		IDebugPanelSett.add(new PlacableMulti("MAINTENANCE_DEGRADE_X1") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				updateTileDay(tx, ty, tx+ty*SETT.TWIDTH, 10);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		
		
		IDebugPanelSett.add(new PlacableMulti("MAINTENANCE_DEGRADE") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				updateTileDay(tx, ty, tx+ty*SETT.TWIDTH);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				// TODO Auto-generated method stub
				return null;
			}
		});
		
		IDebugPanelSett.add("MAINTENANCE ana", new ACTION() {
			
			@Override
			public void exe() {
				new Test();
			}
		});
	}
	
	double sp = 1;
	int upI = -1;
	
	
	public void updateTileDay(int tx, int ty, int tile) {
		if (upI != GAME.updateI()) {
			upI = GAME.updateI();
			sp = speed();
		}
		updateTileDay(tx, ty, tile, sp);
	}
	
	private void updateTileDay(int tx, int ty, int tile, double speed) {
		for (MType t : types) {
			if (t.degrade(tx, ty, tile, speed)) {
				if (t.validate(tx, ty) && !pisser.is(tx, ty) && t.shouldPlace(tx, ty, false)) {
					SETT.PATH().finders.maintenance.remove(tx, ty);
					pisser.set(tx, ty, true);
					bresource.set(tx, ty, t.shouldPlaceResource(tx, ty));
					preserved.set(tx, ty, false);
					SETT.PATH().finders.maintenance.add(tx, ty);
				}
				break;
			}
		}
	}
	
	public void setChanged(int tx, int ty) {
		cons.change(tx, ty);
	}
	
	@Override
	public void save(FilePutter file) {
		pisser.save(file);
		preserved.save(file);
		bresource.save(file);
		pdisabled.save(file);
		MAPSAVE.saveMeta(file, RESOURCES.ALL());
	}
	@Override
	public void load(FileGetter file) throws IOException {
		pisser.load(file);
		preserved.load(file);
		bresource.load(file);
		pdisabled.load(file);
		int[] oo = MAPSAVE.saveWash(file, RESOURCES.ALL(), 0);
		if (oo != null) {
			clear();
		}
	}
	@Override
	public void clear() {
		pisser.clear();
		preserved.clear();
		bresource.clear();
		pdisabled.clear();
	}

	@Override
	protected void afterTick() {
		cons.update();
	}
	
	@Override
	protected void init(boolean loaded) {
		cons.init();
	}
	
	public MAP_BOOLEAN needs = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			for (MType t : types) {
				if (t.validate(tx, ty))
					return true;
			}
			return false;
		}
		
		@Override
		public boolean is(int tile) {
			return is(tile%SETT.TWIDTH, tile/SETT.THEIGHT);
		}
	};
	
	public MAP_BOOLEAN isser = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			if (pisser.is(tx, ty)) {
				for (MType t : types) {
					if (t.validate(tx, ty))
						return true;
				}
				pisser.set(tx, ty, false);
			}
			return false;
		}
		
		@Override
		public boolean is(int tile) {
			return is(tile%SETT.TWIDTH, tile/SETT.THEIGHT);
		}
	};
	
	public MAP_BOOLEANE reserved = new MAP_BOOLEANE() {
		
		@Override
		public boolean is(int tx, int ty) {
			return isser.is(tx, ty) && preserved.is(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			return is(tile%SETT.TWIDTH, tile/SETT.THEIGHT);
		}
		
		@Override
		public MAP_BOOLEANE set(int tx, int ty, boolean value) {
			SETT.PATH().finders.maintenance.remove(tx, ty);
			preserved.set(tx, ty, value);
			SETT.PATH().finders.maintenance.add(tx, ty);
			return this;
		}
		
		@Override
		public MAP_BOOLEANE set(int tile, boolean value) {
			return set(tile%SETT.TWIDTH, tile/SETT.THEIGHT, value);
		}
	};
	
	public MAP_BOOLEAN reservable = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			return isser.is(tx, ty) && !pdisabled.is(tx, ty) && !reserved.is(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			return is(tile%SETT.TWIDTH, tile/SETT.THEIGHT);
		}
		
	};
	
	public MAP_BOOLEANE disabled = new MAP_BOOLEANE() {
		
		@Override
		public boolean is(int tx, int ty) {
			return pdisabled.is(tx, ty);
		}
		
		@Override
		public boolean is(int tile) {
			return is(tile%SETT.TWIDTH, tile/SETT.THEIGHT);
		}
		
		@Override
		public MAP_BOOLEANE set(int tx, int ty, boolean value) {
			if (value == is(tx, ty))
				return this;
			SETT.PATH().finders.maintenance.remove(tx, ty);
			pdisabled.set(tx, ty, value);
			preserved.set(tx, ty, false);
			SETT.PATH().finders.maintenance.add(tx, ty);
			cons.change(tx, ty);
			return this;
		}
		
		@Override
		public MAP_BOOLEANE set(int tile, boolean value) {
			return set(tile%SETT.TWIDTH, tile/SETT.THEIGHT, value);
		}
	};
	
	public final MAP_DOUBLE degrade = new MAP_DOUBLE() {
		
		@Override
		public double get(int tx, int ty) {
			for (MType t : types) {
				if (t.validate(tx, ty))
					t.degrade(tx, ty);
			}
			return 0;
		}
		
		@Override
		public double get(int tile) {
			return get(tile%SETT.TWIDTH, tile/SETT.TWIDTH);
		}
	};
	
	public MAP_OBJECT<RESOURCE> resource = new MAP_OBJECT<RESOURCE>() {

		@Override
		public RESOURCE get(int tile) {
			return get(tile%SETT.TWIDTH, tile/SETT.THEIGHT);
		}

		@Override
		public RESOURCE get(int tx, int ty) {
			if (pisser.is(tx, ty)) {
				int bi = bresource.get(tx, ty);
				if (bi == 0)
					return null;
				for (MType t : types) {
					if (t.validate(tx, ty))
						return t.res(tx, ty, bi);
				}
				pisser.set(tx, ty, false);
			}
			return null;
		}
		
	};
	
	public void vandalise(int tx, int ty) {
		for (MType t : types) {
			if (t.validate(tx, ty)) {
				t.vandalize(tx, ty);
				if (!pisser.is(tx, ty) && t.shouldPlace(tx, ty, false)) {
					SETT.PATH().finders.maintenance.remove(tx, ty);
					pisser.set(tx, ty, true);
					preserved.set(tx, ty, false);
					bresource.set(tx, ty, t.shouldPlaceResource(tx, ty));
					SETT.PATH().finders.maintenance.add(tx, ty);
				}
				break;
			}
		}
	}
	
	public void maintain(int tx, int ty) {
		for (MType t : types) {
			if (t.validate(tx, ty)) {
				t.maintain(tx, ty);
				SETT.PATH().finders.maintenance.remove(tx, ty);
				pisser.set(tx, ty, false);
				if (t.shouldPlace(tx, ty, true)) {
					pisser.set(tx, ty, true);
					bresource.set(tx, ty, t.shouldPlaceResource(tx, ty));
					preserved.set(tx, ty, false);
				}
				SETT.PATH().finders.maintenance.add(tx, ty);
				return;
			}
		}
		
	}

	public void initRoomDegrade(Room room, int mX, int mY) {
		MRoom.initRoom(room, mX, mY);
	}
	
	public double estimateGlobal(RESOURCE res) {
		return cons.get(res)*speed();
	}
	
	public double estimateGlobalRaw(RESOURCE res) {
		return cons.get(res);
	}

	public double speed() {
		double m = BOOSTABLES.CIVICS().MAINTENANCE.get(POP_CL.clP(null, null));
		if (m <= 0)
			return 10;
		return CLAMP.d(1.0/(m), 0, 10);
	}
	
}
