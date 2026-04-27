package settlement.thing.projectiles;

import init.constant.C;
import settlement.main.SETT;
import settlement.thing.projectiles.PData.Data;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.Rec;
import snake2d.util.sets.ArrayListInt;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

final class PRenderer {

	private final SProjectiles p;
	
	
	PRenderer(SProjectiles p){
		this.p = p;
	}
	
	private final ArrayListInt tmp = new ArrayListInt(2*2056);
	private final Rec rec = new Rec();
	
	
	
	public void renderAbove(Renderer r, ShadowBatch s, float ds, int zoomout, RenderData renData) {
		
		r.newLayer(false, zoomout);
		
		
		SETT.WEATHER().apply(renData.absBounds());		
		tmp.clear();
		int min = C.TILE_SIZE;
		rec.set(renData.gBounds().x1()-min, renData.gBounds().x2()+min, renData.gBounds().y1()-min, renData.gBounds().y2()+min);
		p.map.fill(rec, tmp);
		
		int offX = renData.offX1();
		int offY = renData.offY1();

		for (int i = 0; i < p.data.last(); i++) {
			Data d = p.data.data(i);
			double x = d.x()-offX;
			double y = d.y()-offY;
			int h = (int) d.z();
			Projectile pr = p.data.type(i);
			double ref = p.data.ref(i);
			pr.sprite().renderProj(pr, ref, r, s, x, y, h, i, d.speedX(), d.speedY(), d.dz(), ds, zoomout);
			
		}
		COLOR.unbind();
		
	}
	
}
