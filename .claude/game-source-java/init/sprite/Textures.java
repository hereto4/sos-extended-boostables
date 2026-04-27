package init.sprite;

import java.io.IOException;
import java.nio.file.Path;

import init.paths.PATH;
import init.paths.PATHS;
import snake2d.util.sprite.TileTexture;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerTexturer;
import util.spritecomposer.ComposerThings.ITileTexture;
import util.spritecomposer.ComposerUtil;
import util.spritecomposer.SpriteData;

public final class Textures {

	public final TileTexture dis_big;
	public final TileTexture dis_small;
	public final TileTexture dis_tiny;
	public final TileTexture fire;
	public final TileTexture water;
	public final TileTexture bumps;
	public final TileTexture dots;
	public final TileTexture dis_low;
	
	
	Textures() throws IOException{
		

		
		PATH path = PATHS.SPRITE().getFolder("textures");
		
		dots = get(path.get("Dots"));
		dis_low = get(path.get("Displacement_low"));
		dis_big = get(path.get("Displacement_Big"));
		dis_small = get(path.get("Displacement_small"));
		dis_tiny = get(path.get("Displacement_tiny"));
		bumps = get(path.get("Bumps"));
		water = get(path.get("Water"));
		fire = get(path.get("Fire"));
		
	}
	
	private static TileTexture get(Path path) throws IOException {
		return new ITileTexture(8,8, path, 280, 140) {
			
			@Override
			protected
			SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d, ComposerTexturer t) {
				return t.paste(0, 0, 8, 8);
			}
		}.get();
		
	}
	
}
