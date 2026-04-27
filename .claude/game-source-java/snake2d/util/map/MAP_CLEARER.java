package snake2d.util.map;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

public interface MAP_CLEARER {

	public MAP_CLEARER clear(int tile);
	
	public MAP_CLEARER clear(int tx, int ty);
	
	public default MAP_CLEARER clear(int tx, int ty, DIR d) {
		return clear(tx+d.x(), ty+d.y());
	}
	
	public default MAP_CLEARER clear(COORDINATE c) {
		return clear(c.x(), c.y());
	}
	
	public default MAP_CLEARER clear(COORDINATE c, DIR d) {
		return clear(c.x()+d.x(), c.y()+d.y());
	}
	
}
