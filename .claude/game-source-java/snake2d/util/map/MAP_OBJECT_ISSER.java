package snake2d.util.map;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

public interface MAP_OBJECT_ISSER<T> {
	
	
	/**
	 * 
	 * @param tile
	 * @return
	 */
	public boolean is(int tile, T value);
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @return
	 */
	public boolean is(int tx, int ty, T value);
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @param d
	 * @return
	 */
	public default boolean is(int tx, int ty, DIR d, T value) {
		return is(tx+d.x(), ty+d.y(), value);
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public default boolean is(COORDINATE c, T value) {
		return is(c.x(), c.y(), value);
	}
	
	/**
	 * 
	 * @param c
	 * @param d
	 * @return
	 */
	public default boolean is(COORDINATE c, DIR d, T value) {
		return is(c.x()+d.x(), c.y()+d.y(), value);
	}
	
}
