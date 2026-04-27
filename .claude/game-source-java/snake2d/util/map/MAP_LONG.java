package snake2d.util.map;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

public interface MAP_LONG {

	/**
	 * 
	 * @param tile
	 * @return
	 */
	public default boolean is(int tile, long value) {
		return get(tile) == value;
	}
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @return
	 */
	public default boolean is(int tx, int ty, long value) {
		return get(tx, ty) == value;
	}
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @param d
	 * @return
	 */
	public default boolean is(int tx, int ty, DIR d, long value) {
		return get(tx, ty, d) == value;
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public default boolean is(COORDINATE c, long value) {
		return get(c) == value;
	}
	
	/**
	 * 
	 * @param c
	 * @param d
	 * @return
	 */
	public default boolean is(COORDINATE c, DIR d, long value) {
		return get(c, d) == value;
	}
	
	/**
	 * 
	 * @param tile
	 * @return
	 */
	public abstract long get(int tile);
	
	/**
	 * 
	 * @param tx - tileX
	 * @param ty - tileY
	 * @return
	 */
	public abstract long get(int tx, int ty);
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @param d
	 * @return
	 */
	public default long get(int tx, int ty, DIR d) {
		return get(tx+d.x(), ty+d.y());
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public default long get(COORDINATE c) {
		return get(c.x(), c.y());
	}
	
	/**
	 * 
	 * @param c
	 * @param d
	 * @return
	 */
	public default long get(COORDINATE c, DIR d) {
		return get(c.x()+d.x(), c.y()+d.y());
	}
	
	public default long max() {
		return Long.MAX_VALUE;
	}
	
}
