package snake2d.util.map;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIMENSION;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;

public interface MAP_BOOLEAN{

	public boolean is(int tile);
	
	public boolean is(int tx, int ty);
	
	public default boolean is(int tx, int ty, DIR d) {
		return is(tx+d.x(), ty+d.y());
	}
	
	public default boolean is(COORDINATE c) {
		return is(c.x(), c.y());
	}
	
	public default boolean is(COORDINATE c, DIR d) {
		return is(c.x()+d.x(), c.y()+d.y());
	}
	
	public static abstract class BooleanMap implements MAP_BOOLEAN{

		public final int width;
		public final RECTANGLE body;
		
		public BooleanMap(int width, int height) {
			this.width = width;
			this.body = new Rec(width, height);
		}
		
		public BooleanMap(DIMENSION dim) {
			this.width = dim.width();
			this.body = new Rec(dim.width(), dim.height());
		}

		@Override
		public boolean is(int tx, int ty) {
			return body.holdsPoint(tx, ty) && is(tx+ty*width);
		}

		
	}
	
	public abstract static class MAP_BOOLEAN_IMP implements MAP_BOOLEAN{
		
		private final int width;
		
		public MAP_BOOLEAN_IMP(int width) {
			this.width = width;
		}

		@Override
		public boolean is(int tx, int ty) {
			return is(tx+ty*width);
		}
		
	}
	
}
