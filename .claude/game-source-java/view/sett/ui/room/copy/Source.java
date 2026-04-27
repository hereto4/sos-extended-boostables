package view.sett.ui.room.copy;

import settlement.main.SETT;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.map.MAP_BOOLEANE;
import snake2d.util.sets.Bitmap1D;

final class Source implements MAP_BOOLEANE{

	private final Bitmap1D check = new Bitmap1D(SETT.TAREA, false);
	private final Rec rec = new Rec();
	
	public void init() {
		rec.clear();
		check.clear();
	}
	
	@Override
	public boolean is(int tile) {
		return check.get(tile);
	}

	@Override
	public boolean is(int tx, int ty) {
		return is(tx+ty*SETT.TWIDTH);
	}

	@Override
	public MAP_BOOLEANE set(int tile, boolean value) {
		check.set(tile, value);
		rec.unify(tile%SETT.TWIDTH, tile/SETT.TWIDTH);
		return this;
	}

	@Override
	public MAP_BOOLEANE set(int tx, int ty, boolean value) {
		if(SETT.IN_BOUNDS(tx, ty)) {
			set(tx+ty*SETT.TWIDTH, value);
			
		}
		return this;
	}
	
	public RECTANGLE area() {
		return rec;
	}
	
	
}
