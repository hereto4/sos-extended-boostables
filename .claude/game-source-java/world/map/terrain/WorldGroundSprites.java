package world.map.terrain;

import java.io.IOException;

import init.paths.PATHS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

final class WorldGroundSprites {

	public final TILE_SHEET[] sheets;
	
	public final TILE_SHEET stencil;
//	public final TILE_SHEET lush;
//	public final TILE_SHEET normal;
//	public final TILE_SHEET desert;
//	public final TILE_SHEET steppe;
	public final TILE_SHEET[] cracked;
	
	public final int DIM = 8;
	
	public WorldGroundSprites() throws IOException {
		
		sheets = new TILE_SHEET[8];
		
		stencil = new ITileSheet(PATHS.SPRITE_WORLD_MAP().get("Ground"), 576, 300) {
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.house.init(0, 0, 4, 1, d.s16);
				for (int i = 0; i < 4; i++)
					s.house.setVar(i).paste(true);
				s.full.init(0, s.house.body().y2(), 2, 4, DIM, DIM, d.s16);
				return d.s16.saveGame();
				
			}
		}.get();
		
		for (int i = 0; i < sheets.length; i++) {
			double k = i;
			
			sheets[i] = new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					double dd = k/(sheets.length-1);
					int fg = 1;
					
					if (dd > 0.5) {
						fg = 3;
						dd = (dd-0.5)*2.0;
						dd = 1.0-dd;
					}else {
						dd*=2;
						
					}
					
					
					s.full.setVar(0);
					s.full.paste(false);
					s.full.setVar(fg);
					s.full.pasteOverBackground(true, dd);

					return d.s16.saveGame();
					
				}
			}.get();
		}
		
		cracked = new TILE_SHEET[] {
			new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {

					s.full.setVar(0);
					s.full.paste(false);
					s.full.setVar(2);
					s.full.pasteOverBackground(true, 0.5);

					return d.s16.saveGame();
					
				}
			}.get(),
			new ITileSheet() {
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.setVar(2);
					s.full.paste(true);
					return d.s16.saveGame();
					
				}
			}.get()
		};
		
	

		
	}
	
	public int ran(int tx, int ty) {
		return (tx&(DIM-1)) + (ty&(DIM-1))*DIM;
	}
	
	public final void renderNormal(TILE_SHEET sheet, SPRITE_RENDERER r, int x, int y, int ran) {
		sheet.render(r, ran&63, x, y);
	}
	
	public final void renderStenciled(TILE_SHEET sheet, SPRITE_RENDERER r, int x, int y, int mask, int ran1, int ran2) {
		stencil.renderTextured(sheet.getTexture(ran1&63), mask + 16*((ran2)&3), x, y);
	}
	
	
}
