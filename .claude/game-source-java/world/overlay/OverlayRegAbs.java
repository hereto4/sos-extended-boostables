package world.overlay;

import game.GAME;
import game.raiding.RaidingMap.RaidEntryPoint;
import init.constant.C;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DIR;
import snake2d.util.sprite.text.Str;
import util.colors.GCOLOR;
import util.rendering.RenderData;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;
import world.WORLD;
import world.map.regions.Region;

abstract class OverlayRegAbs extends WorldOverlays.OverlayTileNormal{

	private boolean inv;
	
	OverlayRegAbs(CharSequence name, CharSequence desc, boolean inv) {
		super(name, desc, true, true);
		this.inv = inv;
	}


	@Override
	public void renderAbove(Renderer ren, ShadowBatch s, RenderData data) {
		
		super.renderAbove(ren, s, data);
		
		for (Region reg : WORLD.REGIONS().active()) {
			if (is(reg)) {
				double v = value(reg)*100.0;
				
				int x = data.transformGX((reg.cx())*C.TILE_SIZE + C.TILE_SIZEH);
				int y = data.transformGY((reg.cy())*C.TILE_SIZE + C.TILE_SIZEH);
				Str.TMP.clear().add(v, 1);
				Str.TMP.add('%');
				
				int w = C.TILE_SIZE*2;
				int h = C.TILE_SIZE;
				
				GCOLOR.UI().panBG.render(ren, x-w, x+w, y-h, y+h);
				GCOLOR.UI().border().renderFrame(ren, x-w, x+w, y-h, y+h, C.SCALE, C.SCALE);
				UI.FONT().S.renderC(ren, x, y, Str.TMP, C.SCALE);
			}
		}
		
		COLOR.WHITE2WHITE.bind();
		
		
		for (RaidEntryPoint c : GAME.raiders().entry.entrySpots()) {
			int x = data.transformGX(c.c().x()*C.TILE_SIZE);
			int y = data.transformGY(c.c().y()*C.TILE_SIZE);
			
			UI.icons().s.alert.renderScaled(ren, x, y, C.SCALE);
			
			
		}

		
		COLOR.unbind();
		
		
		
		
	}
	
	public abstract boolean is(Region reg);
	
	public abstract double value(Region reg);
	
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
		
		if (reg != null && is(reg)) {
			double v = value(reg);
			if (inv)
				v = 1-v;
			if (v > 0)
				ColorImp.TMP.interpolate(GCOLOR.MAP().F_NEAUTRAL, GCOLOR.MAP().F_ENEMY, v).bind();
			else
				GCOLOR.MAP().F_ALLY.bind();
		}else {
			GCOLOR.MAP().F_REBEL.bind();
		}
		
		renderUnder(m, r, it);
		
	}
	
}
