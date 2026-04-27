package settlement.thing.pointlight;

import java.io.IOException;

import init.paths.PATH;
import init.paths.PATHS;
import init.sprite.SPRITES;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TileTexture;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

final class Sprites {

	public final TILE_SHEET flame_small;
	public final TILE_SHEET flame_medium;
	public final TILE_SHEET flame_big;
	public final TILE_SHEET candle;
	
	public final TileTexture.TileTextureScroller displacement= SPRITES.textures().dis_big.scroller(4, -3);
	public final TileTexture.TileTextureScroller texture = SPRITES.textures().fire.scroller(-3, 4);

	Sprites() throws IOException {

		final PATH path = PATHS.SPRITE_SETTLEMENT().getFolder("thing");
		
		flame_small = (new ITileSheet(path.get("Fire"), 236, 62) {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources ss, ComposerDests d) {
				ComposerDests.Tile t = d.s8;
				final ComposerSources.Singles s = ss.singles;
				s.init(0, 0, 1, 1, 8, 4, t);
				s.setSkip(0, 8).paste(true);
				return t.saveGame();

			}
		}).get();
		
		flame_medium = (new ITileSheet() {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources ss, ComposerDests d) {
				ComposerDests.Tile t = d.s8;
				final ComposerSources.Singles s = ss.singles;
				s.setSkip(8, 8).paste(true);
				return t.saveGame();

			}
		}).get();
		
		flame_big = (new ITileSheet() {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources ss, ComposerDests d) {
				ComposerDests.Tile t = d.s8;
				final ComposerSources.Singles s = ss.singles;
				s.setSkip(16, 8).paste(true);
				return t.saveGame();

			}
		}).get();
		
		candle = (new ITileSheet() {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources ss, ComposerDests d) {
				ComposerDests.Tile t = d.s8;
				final ComposerSources.Singles s = ss.singles;
				s.setSkip(24, 8).paste(true);
				return t.saveGame();

			}
		}).get();
		
	}
	
}
