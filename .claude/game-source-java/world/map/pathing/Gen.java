package world.map.pathing;

import init.sprite.SPRITES;
import snake2d.PathTile;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.ACTION;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import world.WORLD;
import world.map.regions.Region;
import world.map.road.WTRAV;
import world.overlay.WorldOverlays;

class Gen {

	
	public void generateAll(int px, int py, ACTION astep) {
		
		WORLD.PATH().saver().clear();
		astep.exe();
		
		WORLD.OVERLAY().debug = new WorldOverlays.OverlayTile(true, false) {

			@Override
			protected void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {

				if (WORLD.PATH().map.is.is(it.tile())) {
					COLOR.ORANGE100.bind();
					for (int di = 0; di < DIR.ALL.size(); di++) {
						DIR d = DIR.ALL.get(di);
						if (WORLD.PATH().map.can(it.tx(), it.ty(), d))
							SPRITES.cons().ICO.arrows2.get(d.id()).render(r, it.x(),
									it.y());
					}
					COLOR.unbind();
				}

			}
		};
		
		new GenLand(astep);
		astep.exe();
		new GenPort(astep);
		astep.exe();
		new GenConnect(astep);
		astep.exe();
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (WORLD.PATH().map.is.is(c)) {
				
				Region reg = WORLD.PATH().regMap.get(c);
				boolean w = WORLD.WATER().isBig.is(c);
				for (DIR d : DIR.ALL) {
					
					if (w != WORLD.WATER().isBig.is(c))
						continue;
					if (!w && reg != WORLD.PATH().regMap.get(c, d))
						continue;
					
					if (WTRAV.can(c.x(), c.y(), d, true) && WORLD.PATH().map.is.is(c, d)){
						WORLD.PATH().map.add(c, d);
						WORLD.PATH().map.add(c.x()+d.x(), c.y()+d.y(), d.perpendicular());
					}
				}
			}
		}
		
		
	}

	static void connect(PathTile t) {

		PathTile parent = t;
		t = t.getParent();
		while (t != null) {
			WORLD.PATH().map.add(parent, DIR.get(parent, t));
			WORLD.PATH().map.add(t, DIR.get(t, parent));
			parent = t;
			t = t.getParent();
		}

	}
	


}
