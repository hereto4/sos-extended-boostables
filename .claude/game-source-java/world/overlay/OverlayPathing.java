package world.overlay;

import init.sprite.SPRITES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import util.colors.GCOLOR;
import util.rendering.RenderData.RenderIterator;
import util.text.D;
import util.rendering.ShadowBatch;
import world.WORLD;
import world.map.regions.Region;

final class OverlayPathing extends WorldOverlays.OverlayTileNormal{


	private static CharSequence ¤¤name = "¤Paths";
	private static CharSequence ¤¤desc = "¤show available paths";
	static {
		D.ts(OverlayPathing.class);
	}

	OverlayPathing() {
		super(¤¤name, ¤¤desc, true, true);
	}

	@Override
	protected void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
		
		COLOR.ORANGE100.bind();
		
		if (WORLD.PATH().map.is.is(it.tile())) {

			for (DIR d : DIR.ALL) {
				if (WORLD.PATH().map.can(it.tile(), d)) {
					SPRITES.cons().ICO.arrows2.get(d.id()).render(r, it.x(), it.y());
				}
			}

			
		}
		
		
		COLOR.unbind();
		
	}
	
	@Override
	protected void renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
		int m = 0x0F;
		Region reg = WORLD.REGIONS().map.get(it.tile());
		if (WORLD.REGIONS().border().is(it.tile())) {
			m = 0;
			for (DIR d : DIR.ORTHO) {
				if (!WORLD.IN_BOUNDS(it.tx(), it.ty(), d) || reg == WORLD.REGIONS().map.get(it.tx(), it.ty(), d)){
					m |= d.mask();
				}
			}
		}
		GCOLOR.MAP().F_REBEL.bind();
		renderUnder(m, r, it);
		
	}
	
}
