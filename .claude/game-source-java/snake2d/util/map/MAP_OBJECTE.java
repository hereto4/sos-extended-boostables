package snake2d.util.map;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

public interface MAP_OBJECTE<T> extends MAP_OBJECT<T>{
	
	public void set(int tile, T object);
	public void set(int tx, int ty, T object);
	public default void set(int tx, int ty, DIR d, T object) {
		set(tx+d.x(), ty+d.y(), object);
	}
	public default void set(COORDINATE c, T object) {
		set(c.x(), c.y(), object);
	}
	
	public default void set(COORDINATE c, DIR d, T object) {
		set(c.x()+d.x(), c.y()+d.y(), object);
	}
	
}
