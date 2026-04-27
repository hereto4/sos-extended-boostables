package settlement.tilemap.terrain;

import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.room.main.Room;
import snake2d.util.datatypes.AREA;
import util.text.D;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

public final class TerrainDiagonal {

	private static CharSequence ¤¤name = "¤Make Diagonal";
	private static CharSequence ¤¤desc = "¤Turns walls diagonal, only for aesthetic purposes.";
	
	private static CharSequence ¤¤undo = "¤Make Rectangular";
	private static CharSequence ¤¤undoDesc = "¤Turns walls rectangular, only for aesthetic purposes.";
	
	private static CharSequence ¤¤problem = "¤Must be placed on a wall like structure or a road.";
	
	static {
		D.ts(TerrainDiagonal.class);
	}
	
	public final PlacableMulti placer = new PlacableMulti(¤¤name, ¤¤desc, SPRITES.icons().l.dia) {
		
		@Override
		public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
			Room ff = SETT.ROOMS().map.get(tx, ty);
			if (ff != null && ff.constructor() != null && ff.constructor().dia(tx, ty) != null) {
				ff.constructor().dia(tx, ty).setDia(tx, ty, true);
			}
			if (!SETT.ROOMS().map.is(tx, ty) && SETT.FLOOR().getter.get(tx, ty) != null)
				SETT.FLOOR().square.set(tx, ty, false);
			if (SETT.TERRAIN().get(tx, ty) instanceof Diagonalizer) {
				Diagonalizer t = (Diagonalizer) SETT.TERRAIN().get(tx, ty);
				t.setDia(tx, ty, true);
			}
		}
		
		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			Room ff = SETT.ROOMS().map.get(tx, ty);
			if (ff != null && ff.constructor() != null && ff.constructor().dia(tx, ty) != null) {
				return null;
			}
			if (!SETT.ROOMS().map.is(tx, ty) && SETT.FLOOR().getter.get(tx, ty) != null)
				return null;
			if (SETT.TERRAIN().get(tx, ty) instanceof Diagonalizer)
				return null;
			return ¤¤problem;
			
		}
		
		@Override
		public PLACABLE getUndo() {
			return undo;
		};
	};
	
	public final PlacableMulti undo = new PlacableMulti(¤¤undo, ¤¤undoDesc, SPRITES.icons().l.square) {
		
		@Override
		public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
			Room ff = SETT.ROOMS().map.get(tx, ty);
			if (ff != null && ff.constructor() != null && ff.constructor().dia(tx, ty) != null) {
				ff.constructor().dia(tx, ty).setDia(tx, ty, false);
			}
			if (!SETT.ROOMS().map.is(tx, ty) && SETT.FLOOR().getter.get(tx, ty) != null)
				SETT.FLOOR().square.set(tx, ty, true);
			if (SETT.TERRAIN().get(tx, ty) instanceof Diagonalizer) {
				Diagonalizer t = (Diagonalizer) SETT.TERRAIN().get(tx, ty);
				t.setDia(tx, ty, false);
			}
			
		}
		
		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			Room ff = SETT.ROOMS().map.get(tx, ty);
			if (ff != null && ff.constructor() != null && ff.constructor().dia(tx, ty) != null) {
				return null;
			}
			if (!SETT.ROOMS().map.is(tx, ty) && SETT.FLOOR().getter.get(tx, ty) != null)
				return null;
			if (SETT.TERRAIN().get(tx, ty) instanceof Diagonalizer)
				return null;
			return ¤¤problem;
		}
		@Override
		public PLACABLE getUndo() {
			return placer;
		};
		
	};
	
	public interface Diagonalizer {
		
		public void setDia(int tx, int ty, boolean dia);
		public boolean getDia(int tx, int ty);
		
	}
	
}
