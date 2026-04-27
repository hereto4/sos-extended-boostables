package snake2d.util.map;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;

public interface MAP_BOOLEANE extends MAP_BOOLEAN {

	public abstract MAP_BOOLEANE set(int tile, boolean value);
	
	/**
	 * 
	 * @param tx - tileX
	 * @param ty - tileY
	 * @return
	 */
	public MAP_BOOLEANE set(int tx, int ty, boolean value);
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @param d
	 * @return
	 */
	public default MAP_BOOLEANE set(int tx, int ty, DIR d, boolean value) {
		return set(tx+d.x(), ty+d.y(), value);
	}
	
	/**
	 * 
	 * @param c
	 * @return
	 */
	public default MAP_BOOLEANE set(COORDINATE c, boolean value) {
		return set(c.x(), c.y(), value);
	}
	
	/**
	 * 
	 * @param c
	 * @param d
	 * @return
	 */
	public default MAP_BOOLEANE set(COORDINATE c, DIR d, boolean value) {
		return set(c.x()+d.x(), c.y()+d.y(), value);
	}
	

	
	public static abstract class BooleanMapE extends BooleanMap implements MAP_BOOLEANE {

		public BooleanMapE(int width, int height) {
			super(width, height);
		}

		@Override
		public MAP_BOOLEANE set(int tx, int ty, boolean value) {
			if (body.holdsPoint(tx, ty))
				set(tx+ty*width, value);
			return this;
		}
		
		public void setAll(boolean value) {
			int a = body.height()*body.width();
			for (int i = 0; i < a; i++) {
				set(i, value);
			}
		}
		
	}
	
}
