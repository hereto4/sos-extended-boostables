package world.overlay;

import init.constant.C;
import init.sprite.SPRITES;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import view.main.VIEW;
import world.map.pathing.WPATHING;
import world.map.pathing.WPath;
import world.map.pathing.WRegFinder.Treaty;

public final class EPath {
	
	private final double dist = 4;
	
	private Treaty treaty;
	
	private WPath path = new WPath() {

		@Override
		public Treaty treaty() {
			return treaty;
		}
		
	};
	private Coo start = new Coo();
	private Coo end = new Coo();
	private boolean newPath = true;
	private final Rec rr = new Rec(C.TILE_SIZE);
	private boolean added = false;
	
	EPath() {
	
	}

	void render(Renderer r, ShadowBatch s, RenderData data) {
		
		if (!added)
			return;
		
		added = false;
					
		
		path.find(start.x(), start.y(), end.x(), end.y());
		if (!path.isValid()) {
			return;
		}

		if (newPath) {
			newPath = false;
		}
		COLOR.WHITE100.bind();
		
		double move = VIEW.renderSecond()%dist;
		
		int prevx = path.x()*C.TILE_SIZE;
		int prevy = path.y()*C.TILE_SIZE;
		if (!path.setNext())
			return;
		
		
		do {
			
			
			int x = path.x()*C.TILE_SIZE;
			int y = path.y()*C.TILE_SIZE;
			
			double md = 0.5/(path.dir().tileDistance()*WPATHING.movementSpeed(path.x(), path.y()));
			
			move -= md;
			
			if (move < 0) {
				
				
				double dd = move/md;
				move += dist;
				int dx = (int) ((x-prevx)*dd);
				int dy = (int) ((y-prevy)*dd);
				
				DIR d = DIR.get(prevx, prevy, x, y);
				COLOR.YELLOW100.bind();
				SPRITES.cons().ICO.arrows2.get(d.id()).render(r, data.transformGX(x+dx), data.transformGY(y+dy));
				//SPRITES.cons().BIG.outline.render(r, 0, data.transformGX(x+dx), data.transformGY(y+dy));
			}
			
			
			prevx = x;
			prevy = y;
			
			
			COLOR.WHITE100.bind();
			x = data.transformGX(x);
			y = data.transformGY(y);
			SPRITES.cons().BIG.line.render(r, 0, x, y);
		}while(path.setNext());
		
		
		
		

	}
	
	
	public void add(int sx, int sy, int dx, int dy, Treaty treaty) {
		newPath |= start.set(sx, sy);
		newPath |= end.set(dx, dy);
		this.treaty = treaty;
		if (newPath) {
			path.find(start.x(), start.y(), end.x(), end.y());
			rr.moveC(sx*C.TILE_SIZE+C.TILE_SIZEH, sy*C.TILE_SIZE+C.TILE_SIZEH);
		}
		added = true;
		
	}

}
