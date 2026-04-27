package snake2d.util.map;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

public interface MAP_INTE extends MAP_INT{

	public abstract MAP_INTE set(int tile, int value);
	
	/**
	 * 
	 * @param tx - tileX
	 * @param ty - tileY
	 * @return
	 */
	public abstract MAP_INTE set(int tx, int ty, int value);
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @param d
	 * @return
	 */
	public default MAP_INTE set(int tx, int ty, DIR d, int value) {
		return set(tx+d.x(), ty+d.y(), value);
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public default MAP_INTE set(COORDINATE c, int value) {
		return set(c.x(), c.y(), value);
	}
	
	/**
	 * 
	 * @param c
	 * @param d
	 * @return
	 */
	public default MAP_INTE set(COORDINATE c, DIR d, int value) {
		return set(c.x()+d.x(), c.y()+d.y(), value);
	}
	
	public default MAP_INTE increment(int tile, int value) {
		return set(tile, get(tile)+value);
	}
	
	/**
	 * 
	 * @param tx - tileX
	 * @param ty - tileY
	 * @return
	 */
	public default MAP_INTE increment(int tx, int ty, int value) {
		return set(tx, ty, get(tx, ty)+value);
	}
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @param d
	 * @return
	 */
	public default MAP_INTE increment(int tx, int ty, DIR d, int value) {
		return set(tx+d.x(), ty+d.y(), get(tx, ty, d)+value);
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public default MAP_INTE increment(COORDINATE c, int value) {
		return set(c.x(), c.y(),get(c)+ value);
	}
	
	/**
	 * 
	 * @param c
	 * @param d
	 * @return
	 */
	public default MAP_INTE increment(COORDINATE c, DIR d, int value) {
		return set(c.x()+d.x(), c.y()+d.y(), get(c, d)+value);
	}
	
	public static abstract class INT_MAPEImp implements MAP_INTE {

		private final int width;
		private final int height;
		
		public INT_MAPEImp(int width, int height) {
			this.width = width;
			this.height = height;
		}
		

		@Override
		public int get(int tx, int ty) {
			if (tx >= 0 && tx < width && ty >= 0 && ty < height)
				return get(tx+ty*width);
			return 0;
		}

		@Override
		public MAP_INTE set(int tx, int ty, int value) {
			if (tx >= 0 && tx < width && ty >= 0 && ty < height)
				set(tx+ty*width, value);
			return this;
		}
		
	}
	
}
