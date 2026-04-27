package world.overlay;

import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import util.colors.GCOLOR;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.text.D;
import util.rendering.ShadowBatch;
import world.WORLD;
import world.map.regions.Region;

class OverlayFaction extends WorldOverlays.OverlayTileNormal{

	private static CharSequence ¤¤name = "¤Factions";
	private static CharSequence ¤¤desc = "¤Shows a clear view of factions.";
	static {
		D.ts(OverlayFaction.class);
	}
	
	
	OverlayFaction() {
		super(¤¤name, ¤¤desc, true, true);
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
			
		
		
		COLOR c = (reg == null || reg.faction() == null) ? GCOLOR.MAP().F_REBEL : reg.faction().banner().colorBG();
		c.bind();
		renderUnder(m, r, it);
		
	}
	
	@Override
	public void renderAbove(Renderer r, ShadowBatch s, RenderData data) {
		WORLD.OVERLAY().regNames.renderAbove(r, s, data);
	}
	
}
