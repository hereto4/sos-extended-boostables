package settlement.thing.halfEntity.dingy;

import java.io.IOException;

import init.paths.PATHS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.sprite.TILE_SHEET;
import util.rendering.ShadowBatch;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

final class Sprite {

	private final TILE_SHEET sheetCart;
	
	Sprite() throws IOException{
		
		sheetCart = new ITileSheet(PATHS.SETT().sprite.getFolder("thing").get("BOAT_DINGY"), 164, 158) {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d)  {
				s.singles.init(0, 0, 1, 1, 2, 5, d.s32);
				for (int i = 0; i < 4; i++) {
					for (int r = 0; r < 4; r++) {
						s.singles.setSkip(i*2, 1).pasteRotated(r, true);
						s.singles.setSkip(i*2+1, 1).pasteRotated(r, true);
					}
				}
				
				return d.s32.saveGame();
			}
		}.get();
		
	}

	public void render(SPRITE_RENDERER r, ShadowBatch s, int rot, int x, int y, int frame, int upgrade) {
		int i = rot;
		i += (frame&1)*8;
		i+= 16*(upgrade&1);
		
		sheetCart.render(r, i, x, y);
		s.setHeight(4).setDistance2Ground(0);
		sheetCart.render(s, i, x, y);
		
	}
	
}