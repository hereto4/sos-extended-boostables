package init.sprite.UI;

import java.io.IOException;
import java.nio.file.Path;

import init.constant.C;
import init.paths.PATH;
import init.paths.PATHS;
import init.paths.PathParser;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.file.Json;
import snake2d.util.file.SnakeImage;
import snake2d.util.sets.KeyMap;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TextureCoords;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

public class UIImageMaker {

	public static final String split = "->";
	
	final PATH path;
	private KeyMap<SPRITE> map = new KeyMap<>();
	public final int DIM = 32;
	public final SPRITE DUMMY;
	
	UIImageMaker(){
		path = PATHS.SPRITE().getFolder("image");
		DUMMY = new SPRITE.Imp(100, 100) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				COLOR.ORANGE100.render(r, X1, X2, Y1, Y2);
			}
		};
	}
	
	SPRITE get(String relPath) throws IOException {
		return get(relPath, null, null);
	}
	
	public SPRITE get(Json json) throws IOException{
		return get(json.value("IMAGE"), json, "IMAGE");
		
	}
	
	public SPRITE get(String relPath, Json json, String jsonKey) throws IOException {
		
		Path p = PathParser.get(path, relPath, json, jsonKey, 0);
		
		if (p == null)
			return DUMMY;
		
		
		String kk = ""+p.toAbsolutePath();
		
		if (!map.containsKey(kk)) {

			SnakeImage im = new SnakeImage(p);
			final int iwidth = im.width/2;
			final int iheight = im.height;
			im.dispose();
			
			
			
			
			if ((iwidth-12)%(DIM) != 0 || (iheight-12)%(DIM) != 0) {
				PathParser.error(p.toAbsolutePath() + " does not have the right dimensions: Should be a multiple of " + DIM + " squares. Look at other file for reference.", json, jsonKey);
				return DUMMY;
			}
			
			int xs = (iwidth-12)/(DIM);
			int ys = (iheight-12)/(DIM);
			
			TILE_SHEET s = new ITileSheet(p, iwidth*2, iheight) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, 0, 1, 1, xs, ys, d.s32);
					s.full.paste(true);
					return d.s32.saveNormal();
				}
				
			}.get();
			
			UIImage m = new UIImage(s, xs, ys);
			
			map.put(kk, m);
			
		}
		return map.get(kk);
		
		
	}
	
	private static class UIImage implements SPRITE{

		private final static int TILE_SIZE = 32*C.SCALE_NORMAL;
		private final int tilesX;
		private final int tilesY;
		private final int width;
		private final int height;
		
		private final TILE_SHEET sheet;
		
		UIImage(TILE_SHEET sheet, int tilesX, int tilesY) throws IOException{
			
			this.sheet = sheet;
			this.tilesX = tilesX;
			this.tilesY = tilesY;
			width = tilesX*TILE_SIZE;
			height = tilesY*TILE_SIZE;
			
		}

		@Override
		public int width() {
			return width;
		}

		@Override
		public int height() {
			return height;
		}

		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			int startX = X1;
			int tile = 0;
			for (int ty = 0; ty < tilesY; ty++) {
				X1 = startX;
				for (int tx = 0; tx < tilesX; tx++) {
					sheet.render(r, tile, X1, Y1);
					X1 += TILE_SIZE;
					tile++;
				}
				Y1 += TILE_SIZE;
			}
			
		}

		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			throw new RuntimeException();
		}
		
	}

	

	
}
