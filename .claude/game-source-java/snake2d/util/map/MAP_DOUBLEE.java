package snake2d.util.map;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

public abstract interface MAP_DOUBLEE extends MAP_DOUBLE{
	


	public abstract MAP_DOUBLEE set(int tile, double value);
	
	/**
	 * 
	 * @param tx - tileX
	 * @param ty - tileY
	 * @return
	 */
	public abstract MAP_DOUBLEE set(int tx, int ty, double value);
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @param d
	 * @return
	 */
	public default MAP_DOUBLEE set(int tx, int ty, DIR d, double value) {
		return set(tx+d.x(), ty+d.y(), value);
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public default MAP_DOUBLEE set(COORDINATE c, double value) {
		return set(c.x(), c.y(), value);
	}
	
	/**
	 * 
	 * @param c
	 * @param d
	 * @return
	 */
	public default MAP_DOUBLEE set(COORDINATE c, DIR d, double value) {
		return set(c.x()+d.x(), c.y()+d.y(), value);
	}
	
	public default MAP_DOUBLEE increment(int tile, double value) {
		return set(tile, get(tile)+value);
	}
	
	/**
	 * 
	 * @param tx - tileX
	 * @param ty - tileY
	 * @return
	 */
	public default MAP_DOUBLEE increment(int tx, int ty, double value) {
		return set(tx, ty, get(tx, ty)+value);
	}
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @param d
	 * @return
	 */
	public default MAP_DOUBLEE increment(int tx, int ty, DIR d, double value) {
		return set(tx+d.x(), ty+d.y(), get(tx, ty, d)+value);
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public default MAP_DOUBLEE increment(COORDINATE c, double value) {
		return set(c.x(), c.y(),get(c)+ value);
	}
	
	/**
	 * 
	 * @param c
	 * @param d
	 * @return
	 */
	public default MAP_DOUBLEE increment(COORDINATE c, DIR d, double value) {
		return set(c.x()+d.x(), c.y()+d.y(), get(c, d)+value);
	}
	
	public static abstract class DoubleMapImp implements MAP_DOUBLEE{
		
		private final int width;
		private final int height;
		
		public DoubleMapImp(int width, int height) {
			this.width = width;
			this.height = height;
		}
		@Override
		public double get(int tx, int ty) {
			if (tx < 0 || tx >= width || ty < 0 || ty >= height)
				return 0;
			return get(tx+ty*width);
		}

		@Override
		public MAP_DOUBLEE set(int tx, int ty, double value) {
			if (tx < 0 || tx >= width || ty < 0 || ty >= height)
				return this;
			set(tx+ty*width, value);
			return this;
		}
		
	}
	
}
