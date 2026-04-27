package util.data;

import static settlement.main.SETT.IN_BOUNDS;

import settlement.main.SETT;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.map.MAP_CLEARER;
import snake2d.util.map.MAP_SETTER;
import snake2d.util.sets.Bitmap1D;

public class AreaTmp implements AREA, MAP_SETTER, MAP_CLEARER{

	private final Bitmap1D data = new Bitmap1D(SETT.TAREA, false);
	private final Rec body = new Rec();
	private int area;
	
	public AreaTmp() {
		
	}
	
	@Override
	public RECTANGLE body() {
		return body;
	}

	@Override
	public boolean is(int tile) {
		return data.get(tile);
	}

	@Override
	public boolean is(int tx, int ty) {
		if (SETT.IN_BOUNDS(tx, ty))
			return data.get(tx+ty*SETT.TWIDTH);
		return false;
	}

	@Override
	public int area() {
		return area;
	}
	
	public void clear() {
		if (area > 0)
			data.clear();
		area = 0;
		body.setDim(0, 0).moveX1Y1(-1, -1);
		
	}

	@Override
	public MAP_SETTER set(int tile) {
		throw new RuntimeException();
	}

	@Override
	public MAP_SETTER set(int tx, int ty) {
		if (IN_BOUNDS(tx, ty)) {
			if (area == 0) {
				body.setDim(1).moveX1Y1(tx, ty);
			}else {
				body.unify(tx, ty);
			}
			area++;
			data.set(tx+ty*SETT.TWIDTH, true);
		}
		return this;
	}

	@Override
	public MAP_CLEARER clear(int tile) {
		throw new RuntimeException();
	}

	@Override
	public MAP_CLEARER clear(int tx, int ty) {
		if (IN_BOUNDS(tx, ty)) {
			int i = tx+ty*SETT.TWIDTH;
			if (data.get(i)) {
				area--;
				data.set(tx+ty*SETT.TWIDTH, false);
			}
		}
		return this;
	}
	
	
	
}
