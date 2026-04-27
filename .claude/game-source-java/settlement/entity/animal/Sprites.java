package settlement.entity.animal;

import java.io.IOException;

import init.paths.PATHS;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerThings.ITileSheetL;
import util.spritecomposer.ComposerUtil;

class Sprites {

	final TILE_SHEET texture_blood;
	final LIST<TILE_SHEET> texture_water;
	final TILE_SHEET crate;
	
	Sprites() throws IOException {
		
		texture_blood = (new ITileSheet(PATHS.SPRITE().getFolder("animal").get("_Texture"), 264,156) {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources ss, ComposerDests d) {
				final ComposerSources.Singles s = ss.singles;
				
				ComposerDests.Tile t = d.s24;
				s.init(0, 0, 3, 1, 2, 10, t);
				s.setVar(0);
				for (int i = 0; i < 5; i++) {
					s.setSkip(i * 2, 2).paste(3, true);
				}
				return t.saveGame();

			}
		}).get();

		texture_water = (new ITileSheetL() {

			@Override
			protected int init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.setVar(1);
				return 4;
			}

			@Override
			protected TILE_SHEET next(int i, ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.setSkip(i * 2, 2).paste(3, true);
				return d.s24.saveGame();
			}
		}).get();
		
		crate = (new ITileSheet() {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources ss, ComposerDests d) {
				
				final ComposerSources.Singles s = ss.singles;
				s.setSkip(8, 2).paste(3, true);
				return d.s24.saveGame();

			}
		}).get();
	}
	
}
