package game.battle.formation;

import init.constant.C;
import init.sprite.SPRITES;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.sprite.SPRITE;
import util.rendering.RenderData;

public final class DivRenderer {

	
	private DivRenderer() {
		
	}

	public static void render(SPRITE_RENDERER ren, DivFormation p, RenderData data) {
		
		render(ren, p, data, 0, 0);
		
	}
	
	public static void render(SPRITE_RENDERER ren, DivFormation p, RenderData data, int offX, int offY) {
		
		if (p == null)
			return;
		
		int men = p.deployed();
		
		if (men == 0)
			return;
		
		if (!p.body().touches(data.gBounds())) {
			return;
		}

		int ox = data.offX1()+C.TILE_SIZEH + offX;
		int oy = data.offY1()+C.TILE_SIZEH + offY;
		
		
		
		for (int i = 0; i < men; i++) {

			int rx = p.px(i)-ox;
			int ry = p.py(i)-oy;
			if (CORE.renderer().getZoomout() < 3) {
				DIR d = p.dir();
				SPRITE s = SPRITES.cons().ICO.arrows2.get(d.id());
				rx -= C.TILE_SIZEH/2*d.x();
				ry -= C.TILE_SIZEH/2*d.y();
				s.render(ren, rx, ry);
			}else {
				int m = p.dirMaskOrtho(i);
				SPRITES.cons().BIG.dots.render(ren, m, rx, ry);
			}
		}

		ox-=C.TILE_SIZEH;
		oy-=C.TILE_SIZEH;
		

		
		//COLOR.unbind();
	}

	
}
