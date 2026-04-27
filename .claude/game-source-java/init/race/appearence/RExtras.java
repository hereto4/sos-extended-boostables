package init.race.appearence;

import java.io.IOException;
import java.nio.file.Path;

import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerDests.Tile;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

public final class RExtras{
	
	private static int from = 0;
	public final TILE_SHEET tool;
	public final TILE_SHEET water;
	public final TILE_SHEET trolly;
	public final TILE_SHEET Lwater;

	RExtras(Path path) throws IOException{
		
		tool = new ITileSheet(path, 428, 310) {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.init(0, 0, 1, 1, 2, 24, d.s24);
				return gets(6, d.s24, s);
			}
		}.get();
		water = new ITileSheet() {
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				from = 0;
				s.singles.init(s.singles.body().x2(), 0, 1, 1, 2, 24, d.s24);
				return gets(4, d.s24, s);
			}
		}.get();
		trolly = new ITileSheet() {
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				from = 4;
				return gets(4, d.s24, s);
			}
		}.get();
		
		Lwater = new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.init(s.singles.body().x2(), 0, 1, 1, 2, 24, d.s32);
				from = 0;
				return gets(4, d.s32, s);
			}
		}.get();
		
		
	}
	
	private TILE_SHEET gets(int nr, Tile d, ComposerSources s) {
		for (int i = 0; i < nr; i++) {
			s.singles.setSkip((from+i) * 2, 2).paste(3, true);
		}
		from += nr;
		return d.saveGame();
	}
	
}