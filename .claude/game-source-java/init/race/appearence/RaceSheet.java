package init.race.appearence;

import java.io.IOException;

import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

public final class RaceSheet {
	
	public final TILE_SHEET sheet;
	public final TILE_SHEET lay;
	
	public RaceSheet(java.nio.file.Path path) throws IOException {
		
		sheet = new ITileSheet(path, 448, 546) {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				int a = 18;
				s.singles.init(0, 0, 1, 1, 2, a, d.s24);
				for (int i = 0; i < a; i++) {
					s.singles.setSkip(i * 2, 2).paste(3, true);
				}
				return d.s24.saveGame();
			}
		}.get();
		
		lay = new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				int a = 6;
				s.singles.init(s.singles.body().x2(), 0, 1, 1, 4, 3, d.s32);
				for (int i = 0; i < a; i++) {
					s.singles.setSkip(i * 2, 2).paste(3, true);
				}
				return d.s32.saveGame();
			}
		}.get();

	}
	
}