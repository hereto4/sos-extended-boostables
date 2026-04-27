package game.battle;

import static settlement.main.SETT.ROOMS;
import static settlement.main.SETT.THINGS;

import game.GAME;
import init.constant.C;
import init.type.HTYPES;
import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.room.main.RoomBlueprint;
import settlement.room.military.artillery.ArtilleryInstance;
import settlement.room.spirit.grave.GraveData;
import settlement.stats.Induvidual;
import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.util.datatypes.AREA;
import snake2d.util.map.MAP_DOUBLE;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.map.MAP_OBJECT_ISSER;
import view.sett.IDebugPanelSett;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

public final class TargetMap {
	
	TargetMap(){
		
		IDebugPanelSett.add(new PlacableMulti("break something") {
			
			@Override
			public void place(int tx, int ty, AREA a, PLACER_TYPE t) {
				breakIt(tx, ty);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE t) {
				return attackable.is(tx, ty, GAME.ARMIES().player()) || attackable.is(tx, ty, GAME.ARMIES().enemy()) ? null : E;
			}
		});
		
	}
	
	public final MAP_OBJECT<Army> army = new MAP_OBJECT<Army>() {

		@Override
		public Army get(int tile) {
			Room r = SETT.ROOMS().map.get(tile);
			if (r != null) {
				if (r instanceof ArtilleryInstance) {
					return ((ArtilleryInstance)r).army();
				}
				return GAME.ARMIES().player();
			}
			
			return SETT.PATH().availability.get(tile).player < 0 ? GAME.ARMIES().player() : null;
		}

		@Override
		public Army get(int tx, int ty) {
			if (!SETT.IN_BOUNDS(tx, ty))
				return null;
			return get(tx+ty*SETT.TWIDTH);
		}
	};
	
	public final MAP_OBJECT_ISSER<Army> attackable = new MAP_OBJECT_ISSER<Army>() {
		
		@Override
		public boolean is(int tx, int ty, Army value) {
			if (!SETT.IN_BOUNDS(tx, ty))
				return false;
			return is(tx+ty*SETT.TWIDTH, value);
	
		}
		
		@Override
		public boolean is(int tile, Army value) {
			
			Room r = SETT.ROOMS().map.get(tile);
			if (r != null) {
				if (r instanceof ArtilleryInstance) {
					return ((ArtilleryInstance)r).army() != value;
				}
				return GAME.ARMIES().player() != value;
			}
			
			if (value == GAME.ARMIES().player()) {
				if (SETT.TERRAIN().get(tile).clearing().isStructure())
					return false;
			}
			
			if (SETT.PATH().availability.get(tile).isSolid(value) && SETT.TERRAIN().get(tile).clearing().canDestroy(tile%SETT.TWIDTH, tile/SETT.TWIDTH))
				return true;
			
			return false;
			
		}
	};
	
	public final MAP_OBJECT_ISSER<Induvidual> attackableI = new MAP_OBJECT_ISSER<Induvidual>() {
		
		@Override
		public boolean is(int tx, int ty, Induvidual value) {
			if (!SETT.IN_BOUNDS(tx, ty))
				return false;
			return is(tx+ty*SETT.TWIDTH, value);
	
		}
		
		@Override
		public boolean is(int tile, Induvidual in) {
			Army value = in.army();
			Room r = SETT.ROOMS().map.get(tile);
			if (r != null) {
				if (in.hType() == HTYPES.RIOTER() && r.blueprint() instanceof GraveData.GRAVE_DATA_HOLDER)
					return false;
				if (r instanceof ArtilleryInstance) {
					return ((ArtilleryInstance)r).army() != value;
				}
				return GAME.ARMIES().player() != value;
			}
			
			if (value == GAME.ARMIES().player()) {
				if (SETT.TERRAIN().get(tile).clearing().isStructure())
					return false;
			}
			
			if (SETT.PATH().availability.get(tile).isSolid(value) && SETT.TERRAIN().get(tile).clearing().canDestroy(tile%SETT.TWIDTH, tile/SETT.TWIDTH))
				return true;
			
			return false;
			
		}
	};
	
	public MAP_DOUBLE strength = new MAP_DOUBLE() {
		
		@Override
		public double get(int tx, int ty) {
			if (!SETT.IN_BOUNDS(tx, ty)) {
				return 0;
			}
			return get(tx+ty*SETT.TWIDTH);
		}
		
		@Override
		public double get(int tile) {
			RoomBlueprint p = SETT.ROOMS().map.blueprint.get(tile);
			if (p != null) {
				return p.strength(tile);
			}
			return SETT.TERRAIN().get(tile).clearing().strength();
		}
	};
	
	public void breakIt(int x, int y) {
		Room r = ROOMS().map.get(x, y);
		if (r != null && r.destroyTileCan(x, y)) {
			if (r.destroyTileCan(x, y)) {
				THINGS().gore.debris((x<<C.T_SCROLL)+C.TILE_SIZEH, (y<<C.T_SCROLL) + C.TILE_SIZEH, 0, 0);
				r.destroyTile(x, y);
			}
			return;
		}
		
		TerrainTile b = SETT.TERRAIN().get(x, y);
		if (b.clearing().canDestroy(x, y)) {
			THINGS().gore.debris((x<<C.T_SCROLL)+C.TILE_SIZEH, (y<<C.T_SCROLL) + C.TILE_SIZEH, 0, 0);
			b.clearing().destroy(x, y);
			return;
		}
	}
	
}
