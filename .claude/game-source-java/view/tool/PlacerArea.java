package view.tool;

import static settlement.main.SETT.THEIGHT;
import static settlement.main.SETT.TWIDTH;

import settlement.main.SETT;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.map.MAP_SETTER;
import snake2d.util.sets.Bitmap1D;

final class PlacerArea implements AREA{

	static PlacerArea self = new PlacerArea();
	
	private final Bitmap1D map = new Bitmap1D(SETT.TAREA, false);
	private final Rec bounds = new Rec();
	private int area = 0; 
	
	private PlacerArea() {
		
	}
	
	@Override
	public boolean is(int tile) {
		return map.get(tile);
	}


	@Override
	public boolean is(int tx, int ty) {
		if (SETT.IN_BOUNDS(tx, ty))
			return is(tx+ty*SETT.TWIDTH);
		return false;
	}

	final MAP_SETTER set = new MAP_SETTER() {
		
		@Override
		public MAP_SETTER set(int tx, int ty) {
			if (SETT.IN_BOUNDS(tx, ty)) {
				bounds.unify(tx, ty);
				int i = tx+ty*SETT.TWIDTH;
				if (!map.get(i)) {
					area++;
					map.set(i, true);
				}
				
			}
			return this;
		}

		@Override
		public MAP_SETTER set(int tile) {
			throw new RuntimeException();
		}

	};
	
	void clear(){
		if (area > 0) {
			
			
			for (COORDINATE c : bounds) {
				map.set(c.x()+c.y()*SETT.TWIDTH, false);
			}
			bounds.set(TWIDTH, 0, THEIGHT, 0);
			area = 0;
		}
	}

	@Override
	public RECTANGLE body() {
		return bounds;
	}
	
	@Override
	public int area() {
		return area;
	}

	
	
}
