package settlement.thing;

import java.io.IOException;

import init.paths.PATH;
import init.paths.PATHS;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

public class Sprites {

	public final TILE_SHEET flesh;
	public final TILE_SHEET bloodPool;
	public final TILE_SHEET debris;
	public final TILE_SHEET caravan;
	public final TILE_SHEET rubbish;
	
	Sprites() throws IOException {

		

		final PATH path = PATHS.SPRITE_SETTLEMENT().getFolder("thing");
		
		flesh = (new ITileSheet(path.get("Gore"), 236, 62) {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources ss, ComposerDests d) {
				ComposerDests.Tile t = d.s8;
				final ComposerSources.Singles s = ss.singles;
				s.init(0, 0, 1, 1, 8, 4, t);
				s.paste(1, true);
				return t.saveGame();

			}
		}).get();


		bloodPool = flesh.slice(32, 64);
		
		debris = (new ITileSheet(path.get("Debris"), 292, 34) {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources ss, ComposerDests d) {
				ComposerDests.Tile t = d.s8;
				final ComposerSources.Singles s = ss.singles;
				s.init(0, 0, 1, 1, 10, 2, t);
				s.setSkip(0, 20).paste(true);
				return t.saveGame();

			}
		}).get();

		caravan = (new ITileSheet(path.get("Caravan"), 100, 116) {
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				
				s.singles.init(0, 0, 1, 1, 2, 5, d.s16);
				for (int i = 0; i < 5; i++) {
					s.singles.setSkip(i * 2, 2).paste(3, true);
				}
				return d.s16.saveGame();
			}
		}).get();
		
		rubbish = (new ITileSheet(path.get("Rubbish"), 460, 20) {
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources ss, ComposerDests d) {
				ComposerDests.Tile t = d.s8;
				final ComposerSources.Singles s = ss.singles;
				s.init(0, 0, 1, 1, 16, 1, t);
				s.paste(true);
				return t.saveGame();
			}
		}).get();

	}
	
}
