package snake2d.util.map;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

public interface MAP_PLACER extends MAP_BOOLEAN, MAP_SETTER, MAP_CLEARER{
	
	@Override
	public MAP_PLACER clear(int tile);
	
	@Override
	public MAP_PLACER clear(int tx, int ty);
	
	@Override
	public default MAP_PLACER clear(int tx, int ty, DIR d) {
		return clear(tx+d.x(), ty+d.y());
	}
	
	@Override
	public default MAP_PLACER clear(COORDINATE c) {
		return clear(c.x(), c.y());
	}
	
	@Override
	public default MAP_PLACER clear(COORDINATE c, DIR d) {
		return clear(c.x()+d.x(), c.y()+d.y());
	}
	
	@Override
	public MAP_PLACER set(int tile);
	
	@Override
	public MAP_PLACER set(int tx, int ty);
	
	@Override
	public default MAP_PLACER set(int tx, int ty, DIR d) {
		return set(tx+d.x(), ty+d.y());
	}
	
	@Override
	public default MAP_PLACER set(COORDINATE c) {
		return set(c.x(), c.y());
	}
	
	@Override
	public default MAP_PLACER set(COORDINATE c, DIR d) {
		return set(c.x()+d.x(), c.y()+d.y());
	}
	
}
