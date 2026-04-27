package settlement.thing.pointlight;

import java.io.IOException;

import init.constant.C;
import snake2d.Renderer;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import util.rendering.ShadowBatch;

class PointMap implements SAVABLE {

	private final PointMapQuadrant[][] quadrants;
	private final int qMaxX;
	private final int qMaxY;
	private final int gridSize = 32;
	private Rec rec = new Rec();
	private final long[] tmp = new long[1024*3]; 

	PointMap(int mapSizeX, int mapSizeY){

		qMaxX = mapSizeX/gridSize;
		qMaxY = mapSizeY/gridSize;
		
		quadrants = new PointMapQuadrant[qMaxX][qMaxY];
		
		for (int y = 0; y < quadrants.length; y++){
			for (int x = 0; x < quadrants[0].length; x++){
				quadrants[y][x] = new PointMapQuadrant();
			}
		}
		
	}
	
	public void add(int tx, int ty, int offX, int offY, LightModel model){
		
		int qx = tx/gridSize;
		int qy = ty/gridSize;
		quadrants[qy][qx].add(Light.make(tx, ty, offX, offY, model, true));
	}
	
	public void remove(int tx, int ty){
		
		int qx = tx/gridSize;
		int qy = ty/gridSize;
		
		quadrants[qy][qx].remove(tx, ty);
	}
	
	public void hide(int tx, int ty, boolean hide) {
		int qx = tx/gridSize;
		int qy = ty/gridSize;
		
		for (int i = 0; i < quadrants[qy][qx].last(); i++) {
			Light l = Light.init(quadrants[qy][qx].get(i));
			if (l.tx() == tx && l.ty() == ty) {
				long d = l.hide(hide);
				quadrants[qy][qx].set(i, d);
			}
			
		}
		
	}
	
	@Override
	public void clear(){
		for (int y = 0; y < quadrants.length; y++){
			for (int x = 0; x < quadrants[0].length; x++){
				quadrants[y][x].clear();
			}
		}
	}

	@Override
	public void save(FilePutter file) {
		for (int y = 0; y < quadrants.length; y++){
			for (int x = 0; x < quadrants[0].length; x++){
				quadrants[y][x].save(file);
			}
		}
	}

	@Override
	public void load(FileGetter file) throws IOException {
		for (int y = 0; y < quadrants.length; y++){
			for (int x = 0; x < quadrants[0].length; x++){
				quadrants[y][x].load(file);
			}
		}
	}
	
	private int fill(int x1, int x2, int y1, int y2) {
		
		x1 = x1 >> C.T_SCROLL;
		y1 = y1 >> C.T_SCROLL;
		x2 = x2 >> C.T_SCROLL;
		y2 = y2 >> C.T_SCROLL;
		
		
		int start = 0;
		
		
		int qx1 = (x1-gridSize)/gridSize;
		if (qx1 < 0)
			qx1 = 0;
		int qy1 = (y1-gridSize)/gridSize;
		if (qy1 < 0)
			qy1 = 0;
		int qx2 = (x2+gridSize)/gridSize;
		if (qx2 >= qMaxX)
			qx2 = qMaxX -1;
		int qy2 = (y2+gridSize)/gridSize;
		if (qy2 >= qMaxY)
			qy2 = qMaxY -1;
		
		int min = 16;
		rec.set(x1-min, x2+min, y1-min, y2+min);
		for (int y = qy1; y <= qy2; y++){
			for (int x = qx1; x <= qx2; x++){
				PointMapQuadrant qq = quadrants[y][x];
				for (int i = 0; i < qq.last(); i++) {
					
					Light p = Light.init(qq.get(i));
					if (rec.holdsPoint(p.tx(), p.ty())) {
						if (start >= tmp.length)
							return start;
						tmp[start] = qq.get(i);
						start ++;	
					}
				}
			}
		}
		
		return start;
			
	}
	
	public void render(Renderer r, ShadowBatch s, float ds, RECTANGLE renWindow, int offX, int offY) {

		int start = fill(renWindow.x1(), renWindow.x2(), renWindow.y1(), renWindow.y2());
		
		
		offX = offX - renWindow.x1();
		offY = offY - renWindow.y1();
		
		for (int i = 0; i < start; i++) {
			Light.init(tmp[i]).render(r, s, ds, offX, offY);
		}
		
		r.newLayer(true, r.getZoomout());
		
		for (int i = 0; i < start; i++) {
			Light.init(tmp[i]).renderBelow(r, s, ds, offX, offY);
		}

	}

	public boolean is(int tx, int ty) {

		int qx = tx/gridSize;
		int qy = ty/gridSize;
		
		return quadrants[qy][qx].is(tx, ty);
	}





	
}
