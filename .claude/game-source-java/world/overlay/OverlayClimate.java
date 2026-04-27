package world.overlay;

import init.type.CLIMATE;
import init.type.CLIMATES;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import world.WORLD;

class OverlayClimate extends WorldOverlays.OverlayTileNormal{

	
	
	OverlayClimate() {
		super(CLIMATES.INFO().name, "", true, true);
	}
	
	@Override
	protected void renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
		CLIMATE cl = WORLD.TERRAIN().climate.getter.get(it.tile());
		int m = 0x0F;
		
		COLOR c = cl.color;
		c.bind();
		renderUnder(m, r, it);
		
	}
	
	@Override
	public void renderAbove(Renderer r, ShadowBatch s, RenderData data) {
		WORLD.OVERLAY().regNames.renderAbove(r, s, data);
	}
	
}
