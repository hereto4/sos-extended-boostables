package settlement.thing.projectiles;

import init.constant.C;
import settlement.main.SETT;
import settlement.thing.projectiles.PData.Data;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.sets.ArrayListInt;

class Map {

	private final Quad[][] quadrants;
	private final int qMaxX;
	private final int qMaxY;
	static final int gridSize = C.TILE_SIZE*16;
	static final int gridScroll = Integer.numberOfTrailingZeros(gridSize);
	public Map(int mapSizeX, int mapSizeY){
		qMaxX = mapSizeX/gridSize;
		qMaxY = mapSizeY/gridSize;
		
		quadrants = new Quad[qMaxX][qMaxY];
		
		for (int y = 0; y < quadrants.length; y++){
			for (int x = 0; x < quadrants[0].length; x++){
				quadrants[y][x] = new Quad(x, y);
			}
		}
		
	}
	
	public void add(int e){
		
		Data d = SETT.PROJS().data.data(e);
		
		int gridX = d.qx();
		int gridY = d.qy();
		quadrants[gridY][gridX].add(e);
		
	}
	
	public boolean contains(int e){
		
		Data d = SETT.PROJS().data.data(e);
		
		int gridX = d.qx();
		int gridY = d.qy();
		return quadrants[gridY][gridX].contains(e);
		
	}
	
	public void remove(int e){
		
		Data d = SETT.PROJS().data.data(e);
		
		int gridX = d.qx();
		int gridY = d.qy();
		quadrants[gridY][gridX].remove(e);
		
	}
	
	void fill(RECTANGLE bounds, ArrayListInt result) {
		
		int qx1 = (bounds.x1())/gridSize;
		if (qx1 < 0)
			qx1 = 0;
		int qy1 = (bounds.y1())/gridSize;
		if (qy1 < 0)
			qy1 = 0;
		int qx2 = (bounds.x2())/gridSize;
		if (qx2 >= qMaxX)
			qx2 = qMaxX -1;
		int qy2 = (bounds.y2())/gridSize;
		if (qy2 >= qMaxY)
			qy2 = qMaxY -1;
		
		for (int y = qy1; y <= qy2; y++){
			for (int x = qx1; x <= qx2; x++){
				quadrants[y][x].fill(bounds, result);
			}
		}
			
	}
	
	void clear(){
		for (int y = 0; y < quadrants.length; y++){
			for (int x = 0; x < quadrants[0].length; x++){
				quadrants[y][x].clear();
			}
		}
	}

	
}
