package init.resources;

import java.io.IOException;
import java.nio.file.Path;

import snake2d.util.color.COLOR;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.IColorSamplerSingle;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

final class Sprite{
	
	public final TILE_SHEET carry;
	public final TILE_SHEET lay;
	public final COLOR color;
	
	Sprite(Path path) throws IOException {
		
		carry = new ITileSheet(path, 244, 94) {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				
				s.singles.init(0, 0, 1, 1, 1, 4, d.s16);

				s.singles.setSkip(0, 2).paste(3, true);
				return d.s16.saveGame();
			}
		}.get();
		
		color = new IColorSamplerSingle() {
			
			@Override
			protected COLOR init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.setSkip(2, 1);
				return s.singles.sample();
			}
		}.get();
		
		lay = new ITileSheet() {
			
			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.singles.init(
						s.singles.body().x2(), 0, 
						1, 1, 
						4, 4, 
						d.s16);
				s.singles.paste(true);
				return d.s16.saveGame();
			}
		}.get();
		
	}

	
	static class Util {
		
		public TILE_SHEET getMinable(Path path) throws IOException {
			return new ITileSheet(path, 364, 94) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.init(0, 0, 1, 1, 8, 4, d.s16);
					s.singles.paste(true);	
					return d.s16.saveGame();
				}
			}.get();
		}
		
		public TILE_SHEET getGrowable(Path path) throws IOException {
			return new ITileSheet(path, 364, 182) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.init(0, 0, 1, 1, 8, 8, d.s16);
					s.singles.paste(true);
					return d.s16.saveGame();
				}
			}.get();
		}
		
	}
}