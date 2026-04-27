package snake2d.util.map;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

public interface MAP_OBJECT<T> extends MAP_OBJECT_ISSER<T>, MAP_BOOLEAN{
	
	@Override
	default boolean is(int tile) {
		return get(tile) != null;
	}
	
	@Override
	default boolean is(int tx, int ty) {
		return get(tx, ty) != null;
	}
	
	@Override
	public default boolean is(int tile, T value) {
		return get(tile) == value;
	}
	
	@Override
	public default boolean is(int tx, int ty, T value) {
		return get(tx, ty) == value;
	}
	
	/**
	 * 
	 * @param tile
	 * @return
	 */
	public abstract T get(int tile);
	
	/**
	 * 
	 * @param tx - tileX
	 * @param ty - tileY
	 * @return
	 */
	public abstract T get(int tx, int ty);
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @param d
	 * @return
	 */
	public default T get(int tx, int ty, DIR d) {
		return get(tx+d.x(), ty+d.y());
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public default T get(COORDINATE c) {
		return get(c.x(), c.y());
	}
	
	/**
	 * 
	 * @param c
	 * @param d
	 * @return
	 */
	public default T get(COORDINATE c, DIR d) {
		return get(c.x()+d.x(), c.y()+d.y());
	}
	
	
}
