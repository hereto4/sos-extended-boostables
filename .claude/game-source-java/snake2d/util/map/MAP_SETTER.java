package snake2d.util.map;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

public interface MAP_SETTER{

	public MAP_SETTER set(int tile);
	
	public MAP_SETTER set(int tx, int ty);
	
	public default MAP_SETTER set(int tx, int ty, DIR d) {
		return set(tx+d.x(), ty+d.y());
	}
	
	public default MAP_SETTER set(COORDINATE c) {
		return set(c.x(), c.y());
	}
	
	public default MAP_SETTER set(COORDINATE c, DIR d) {
		return set(c.x()+d.x(), c.y()+d.y());
	}
	

	
}
