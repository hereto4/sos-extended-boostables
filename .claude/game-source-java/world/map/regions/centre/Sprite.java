package world.map.regions.centre;

import init.constant.C;
import snake2d.CORE;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Rec;
import snake2d.util.sets.ArrayCooShort;
import util.GUTIL;
import world.WORLD;
import world.WRenContext;
import world.map.regions.Region;

public final class Sprite {

	private final CSprite sprite = new CSprite();
	
	private final ArrayCooShort centres = new ArrayCooShort(128);
	private Rec tmp = new Rec();
	private final int from = -WCentre.TILE_DIM/2;
	private final int to = WCentre.TILE_DIM + from;
	
	
	
	public Sprite() {
		
	}
	
	public void renderGround(WRenContext data){
		
		int last = centres.getI();

		for (int i = 0; i < last; i++) {
			
			COORDINATE cen = centres.set(i);
			Region reg = WORLD.REGIONS().map.get(cen);

			for (int dty = from; dty <= to; dty++) {
				for (int dtx = from; dtx <= to; dtx++) {
					
					int tx = cen.x()+dtx;
					int ty = cen.y()+dty;
					
					int x = data.data.transformGX(tx*C.TILE_SIZE);
					int y = data.data.transformGY(ty*C.TILE_SIZE);
					sprite.renderOnGround(data, dtx-from, dty-from, reg, GUTIL.ran1().get(tx, ty), x, y);
					
				}
			}
			
		}
		

		centres.set(last);
		
	}
	
	

	public void renderAbove(WRenContext data){
		int last = centres.getI();
		
		for (int i = 0; i < last; i++) {
			
			COORDINATE cen = centres.set(i);
			Region reg = WORLD.REGIONS().map.get(cen);
			for (int dty = from; dty <= to; dty++) {
				for (int dtx = from; dtx <= to; dtx++) {
					
					int tx = cen.x()+dtx;
					int ty = cen.y()+dty;
					
					int x = data.data.transformGX(tx*C.TILE_SIZE);
					int y = data.data.transformGY(ty*C.TILE_SIZE);
					
					sprite.renderAboveB(data, dtx-from, dty-from, reg, GUTIL.ran1().get(tx, ty), x, y);
					
				}
			}
			
		}
		CORE.renderer().newLayer(false, CORE.renderer().getZoomout());
		
		for (int i = 0; i < last; i++) {
			
			COORDINATE cen = centres.set(i);
			Region reg = WORLD.REGIONS().map.get(cen);
			for (int dty = from; dty <= to; dty++) {
				for (int dtx = from; dtx <= to; dtx++) {
					
					int tx = cen.x()+dtx;
					int ty = cen.y()+dty;
					
					int x = data.data.transformGX(tx*C.TILE_SIZE);
					int y = data.data.transformGY(ty*C.TILE_SIZE);
					
					sprite.renderAboveA(data, dtx-from, dty-from, reg, GUTIL.ran1().get(tx, ty), x, y);
					
				}
			}
			
		}
		

		centres.set(last);
	}
	
	public void renderAboveTerrain(WRenContext data){
		tmp.setDim(data.data.tBounds().width()+WCentre.TILE_DIM*2+4, data.data.tBounds().height()+WCentre.TILE_DIM*2+4);
		tmp.moveC(data.data.tBounds().cX(), data.data.tBounds().cY());
		centres.set(0);
		for (Region reg : WORLD.REGIONS().active()) {
			if (reg.cx() >= 0 && tmp.holdsPoint(reg.cx(), reg.cy())) {
				centres.get().set(reg.cx(), reg.cy());
				if (centres.getI() >= centres.size()-1)
					continue;
				centres.inc();
			}
		}
		
		int last = centres.getI();

		for (int i = 0; i < last; i++) {
			
			COORDINATE cen = centres.set(i);
			Region reg = WORLD.REGIONS().map.get(cen);
			for (int dty = from; dty <= to; dty++) {
				for (int dtx = from; dtx <= to; dtx++) {
					
					int tx = cen.x()+dtx;
					int ty = cen.y()+dty;
					
//					if (!tmp.holdsPoint(tx, ty))
//						continue;
//					
					int x = data.data.transformGX(tx*C.TILE_SIZE);
					int y = data.data.transformGY(ty*C.TILE_SIZE);
					
					sprite.renderAboveTerrain(data, dtx-from, dty-from, reg, GUTIL.ran1().get(tx, ty), x, y);
//					data.hiBuildings.set(tx, ty, true);
				}
			}
			
		}
		
		centres.set(last);
		
	}
	
	
}
