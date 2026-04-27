package world.overlay;

import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.CORE;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.sprite.text.Font;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import world.WORLD;
import world.map.landmark.WorldLandmark;

public final class OverlayExplore extends WorldOverlays.OverlayTile{

	private WorldLandmark hovered;
	
	OverlayExplore() {
		super(true, false);
	}
	
	public void hover(WorldLandmark m) {
		hovered = m;
	}

	@Override
	public void add() {
		super.add();
	}
	
	@Override
	protected void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
		
		WorldLandmark a = WORLD.LANDMARKS().setter.get(it.tile());
		
		if (a == null)
			return;
		
		if (a == hovered) {
			COLOR.WHITE2WHITE.bind();
			
			int m = 0;
			for (DIR d : DIR.ORTHO) {
				if (WORLD.LANDMARKS().setter.get(it.tx(), it.ty(), d) == a)
					m |= d.mask();
			}
			
			if (m != 0x0F) {
				SPRITES.cons().BIG.dashed_hollow.render(r, m, it.x(), it.y());
			}
			
			
		}else if (a != null && a.textSize != 0 && it.tx() == a.cx && it.ty() == a.cy) {
			COLOR.WHITE85.bind();
			Font f = UI.FONT().H2;
			int scale = 2 + CORE.renderer().getZoomout();
			int w = f.width(a.name, 0, a.name.length(), scale);
			s.setHeight(0).setDistance2Ground(0);
			COLOR.WHITE100.render(s, it.x()-w/2-8, it.x()+w/2+8, it.y()-8, it.y()+8+f.height()*scale);
			
			f.renderCX(r, it.x(), it.y(), a.name, scale);
			//f.renderCX(s, it.x(), it.y(), a.name, scale);
			
		}
		
		
		
		
	}
	
	@Override
	public void renderAbove(Renderer r, ShadowBatch s, RenderData data) {
		WORLD.OVERLAY().minerals.renderAbove(r, s, data);
		super.renderAbove(r, s, data);
		hovered = null;
	}
	
}
