package world.map.regions;

import game.faction.FACTIONS;
import init.sprite.SPRITES;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.ACTION;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import world.WORLD;
import world.overlay.WorldOverlays;

public class Gen {

	public Gen(ACTION lprinter) {
		
		WORLD.OVERLAY().debug = new WorldOverlays.OverlayTile(true, false) {
			
			@Override
			protected void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
				Region reg = WORLD.REGIONS().map.get(it.tile());
				if (reg == null)
					return;
				ColorImp.TMP.set(COLOR.UNIQUE.getC(reg.index())).setBrightnessSelf(2.0);
				int m = 0;
				for (DIR d : DIR.ORTHO) {
					if (WORLD.REGIONS().map.get(it.tx(), it.ty(), d) == reg) {
						m|= d.mask();
					}
				}
				ColorImp.TMP.bind();
				SPRITES.cons().BIG.outline.render(CORE.renderer(), m, it.x(), it.y());
				
					
				COLOR.unbind();
				
			}
		};
		
		if (!GenPlayer.gen())
			return;
		lprinter.exe();
		new GenAssign(lprinter);
		lprinter.exe();
		new GenInit(lprinter);
		lprinter.exe();
		new GenName();
		lprinter.exe();
		

		WORLD.REGIONS().player.fationSet(FACTIONS.player(), false);
		WORLD.REGIONS().player.setCapitol();
		
		WORLD.MINIMAP().repaint();

		
		//WORLD.OVERLAY().debug = null;
		
	}

	
	public void clear() {
		while(FACTIONS.NPCs().size() > 0) {
			FACTIONS.remove(FACTIONS.NPCs().get(0), false);
		}
		WORLD.REGIONS().saver().clear();
	}

	
}
