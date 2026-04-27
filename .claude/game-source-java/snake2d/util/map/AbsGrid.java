package snake2d.util.map;


import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public class AbsGrid {

	private final GridTile[][] quadrants;
	public final LIST<GridTile> all;
	public final RECTANGLE bounds;
	public final int qMaxX;
	public final int qMaxY;
	public final int gridSize;
	
	public AbsGrid(int mapSizeX, int mapSizeY, int gridSize) {
		this.gridSize = gridSize;
		qMaxX = mapSizeX/gridSize;
		qMaxY = mapSizeY/gridSize;
		bounds = new Rec(mapSizeX, mapSizeY);
		quadrants = new GridTile[qMaxX][qMaxY];
		ArrayList<GridTile> quadrantsI = new ArrayList<>(qMaxX*qMaxY);
		int in = 0;
		
		for (int y = 0; y < quadrants.length; y++){
			for (int x = 0; x < quadrants[0].length; x++){
				int x1 = x*gridSize;
				int x2 = CLAMP.i(x1+gridSize, 0, mapSizeX);
				int y1 = y*gridSize;
				int y2 = CLAMP.i(y1+gridSize, 0, mapSizeY);
				GridTile t = new GridTile(in, x1, x2, y1, y2);
				
				quadrantsI.add(t);
				quadrants[y][x] = t;
				
				in++;
			}
		}
		this.all = quadrantsI;
	}
	
	public static final class GridTile extends Rec {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public final int index;
		
		GridTile(int index, int x1, int x2, int y1, int y2){
			super(x1, x2, y1, y2);
			this.index = index;
		}
		
	}
	
	public GridTile get(int index) {
		return all.get(index);
	}
	
	public final MAP_OBJECT<GridTile> map = new MAP_OBJECT<AbsGrid.GridTile>() {
		
		@Override
		public GridTile get(int tx, int ty) {
			if (!bounds.holdsPoint(tx, ty))
				return null;
			return quadrants[ty/gridSize][tx/gridSize];
		}
		
		@Override
		public GridTile get(int tile) {
			return get(tile%bounds.width(), tile/bounds.width());
		}
	};
	
	
}
