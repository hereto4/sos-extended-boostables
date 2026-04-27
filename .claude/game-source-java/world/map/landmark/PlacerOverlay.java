package world.map.landmark;

import init.sprite.SPRITES;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import util.colors.GCOLOR;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import world.WORLD;
import world.overlay.WorldOverlays.OverlayTile;

class PlacerOverlay extends OverlayTile{

	WorldLandmark hovered = null;
	
	public PlacerOverlay() {
		super(true, false);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
		WorldLandmark l = WORLD.LANDMARKS().setter.get(it.tile());
		if (l != null) {
			COLOR c = l == hovered ? GCOLOR.MAP().BEST : COLOR.WHITE35;
			c.bind();
			int m = 0;
			for (DIR d : DIR.ORTHO) {
				if (WORLD.LANDMARKS().setter.get(it.tx(), it.ty(), d) == l)
					m |= d.mask();
			}
			SPRITES.cons().BIG.dashed.render(r, m, it.x(), it.y());
		}
	}

}
