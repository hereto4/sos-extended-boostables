package world;

import java.io.IOException;

import init.paths.PATH;
import init.paths.PATHS;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

public class Sprites{

	private final PATH path = PATHS.SPRITE().getFolder("world").getFolder("map");
	
	public final TILE_SHEET edge = (new ITileSheet(path.get("Edge"), 1176, 28) {
		@Override
		protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
			ComposerDests.Tile t = d.s16;
			final ComposerSources.Full f = s.full;
			f.init(0, 0, 1, 1, 36, 1, t);
			f.setVar(0).paste(true);
			return t.saveGame();
			
		}
	}).get();
	

	
	

	

	Sprites() throws IOException {
		
		
	}

	
	
}
