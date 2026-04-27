package settlement.thing.halfEntity;

import init.constant.C;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.Tree;

class _WMap {

	private final _Quadrant[][] quadrants;
	private final int qMaxX;
	private final int qMaxY;
	private final int gridSize = C.TILE_SIZE*32;

	public _WMap(int mapSizeX, int mapSizeY){

		qMaxX = mapSizeX/gridSize;
		qMaxY = mapSizeY/gridSize;
		
		quadrants = new _Quadrant[qMaxX][qMaxY];
		
		for (int y = 0; y < quadrants.length; y++){
			for (int x = 0; x < quadrants[0].length; x++){
				quadrants[y][x] = new _Quadrant();
			}
		}
		
	}
	
	public void add(HalfEntity e){
		
		if (!isOut(e.gridX, e.gridY))
			throw new RuntimeException();
		
		e.gridX = (short) (e.body().cX()/gridSize);
		e.gridY = (short) (e.body().cY()/gridSize);
		
		
		if (isOut(e.gridX, e.gridY))
			return;
		
		quadrants[e.gridY][e.gridX].add(e);
		
	}
	
	public void remove(HalfEntity e){
		
		if (isOut(e.gridX, e.gridY))
			return;
		
		quadrants[e.gridY][e.gridX].remove(e);
		e.gridY = -1;
		
	}
	

	public void move(HalfEntity e) {
		
		short gridX = (short) (e.body().cX()/gridSize);
		short gridY = (short) (e.body().cY()/gridSize);
		
		if (e.gridX != gridX || e.gridY != gridY) {
			remove(e);
			add(e);
		}
		
		
	}
	
	private boolean isOut(int qx1, int qy1){
		return qx1 >= qMaxX || qy1 >= qMaxY || qx1 < 0 || qy1 < 0;
	}
	

	
	void fill(int x1, int x2, int y1, int y2, Tree<HalfEntity> result) {
		
		int min = 0;
		
		int qx1 = (x1-min)/gridSize;
		if (qx1 < 0)
			qx1 = 0;
		int qy1 = (y1-min)/gridSize;
		if (qy1 < 0)
			qy1 = 0;
		int qx2 = (x2+min)/gridSize;
		if (qx2 >= qMaxX)
			qx2 = qMaxX -1;
		int qy2 = (y2+min)/gridSize;
		if (qy2 >= qMaxY)
			qy2 = qMaxY -1;
		
		
		
		for (int y = qy1; y <= qy2; y++){
			for (int x = qx1; x <= qx2; x++){
				for (HalfEntity e : quadrants[y][x]) {
					if (e.body().touches(x1, x2, y1, y2)) {
						result.add(e);
						if (!result.hasRoom())
							return;
					}
				}
			}
		}
			
	}
	
	void fill(int x1, int x2, int y1, int y2, LISTE<HalfEntity> result) {
		
		int min = 3*C.TILE_SIZE;
		
		int qx1 = (x1-min)/gridSize;
		if (qx1 < 0)
			qx1 = 0;
		int qy1 = (y1-min)/gridSize;
		if (qy1 < 0)
			qy1 = 0;
		int qx2 = (x2+min)/gridSize;
		if (qx2 >= qMaxX)
			qx2 = qMaxX -1;
		int qy2 = (y2+min)/gridSize;
		if (qy2 >= qMaxY)
			qy2 = qMaxY -1;
		
		for (int y = qy1; y <= qy2; y++){
			for (int x = qx1; x <= qx2; x++){
				for (HalfEntity e : quadrants[y][x]) {
					if (e.body().touches(x1, x2, y1, y2))
						result.add(e);
				}
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
