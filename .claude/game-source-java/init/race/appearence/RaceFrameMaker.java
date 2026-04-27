package init.race.appearence;

import java.io.IOException;
import java.nio.file.Path;

import game.GAME;
import init.paths.PATHS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.file.Json;
import snake2d.util.file.SnakeImage;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TextureCoords;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.IInit;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

public class RaceFrameMaker {

	public static final int TILES_X = 5;
	public static final int TILES_Y = 5;
	public static final int TILE_SIZE = 8;
	private static final int FRAMES_X = 4;
	
	private KeyMap<LIST<RaceFrameRaw>> map = new KeyMap<>();
	
	public final LIST<RaceFrameRaw> grit;
	public final LIST<RaceFrameRaw> blood;
	
	public RaceFrameMaker() throws IOException{
		ArrayList<RaceFrameRaw> ff  = new ArrayList<>(4);
		for (int i = 0; i < ff.max(); i++) {
			ff.add(frame("_Overlays", i, null));
		}
		grit = ff;;
		ff  = new ArrayList<>(4);
		for (int i = 0; i < ff.max(); i++) {
			ff.add(frame("_Overlays", 4+i, null));
		}
		blood = ff;
		
	}
	
	public LIST<RaceFrameRaw> read(Json json) throws IOException {

		String[] vals = json.values("FRAMES");
		
		ArrayListGrower<RaceFrameRaw> frames = new ArrayListGrower<>();
		
		for (String val : vals) {
			if (!read(frames, val, json)) {
				GAME.WarnLight("Unable to parse key " + val + " these keys should be FILE:INDEX, where FILE is a file in the portrait folder, and INDEX is the frame in that file (integer)");
			}
			
		}
		
		return frames;
		

	}
	
	private boolean read(ArrayListGrower<RaceFrameRaw> frames, String val, Json json) throws IOException {
		String[] ss = val.split(":");
		if (ss.length == 2) {
			String file = ss[0].trim();
			try {
				Integer row = Integer.parseInt(ss[1].trim());
				if (row != null) {
					frames.add(frame(file, row, json));
				}
				return true;
			}catch(NumberFormatException e){
				return false;
			}
			
			
		}
		return false;
		
		

	}
	
	private RaceFrameRaw frame(String file, int row, Json error) throws IOException {

		if (!PATHS.RACE().sprite.getFolder("face").exists(file)) {
			if (error != null) {
				GAME.Warn("the face file: " + file + " does not exist");
				return dFrame;
			}
			throw new RuntimeException(file);
		}

		if (!map.containsKey(file))
			map.put(file, sheet(file));
		
		LIST<RaceFrameRaw> rows = map.get(file);
		
		if (row < 0 || row >= rows.size()) {
			if (error != null) {
				GAME.Warn("the row number for face file: " + file + " is out of bounds " + row + " " + (rows.size()-1));
				return dFrame;
			}
			throw new RuntimeException(file);
		}
		
		return rows.get(row);
		

	}

	private LIST<RaceFrameRaw> sheet(String file) throws IOException {

		Path pp = PATHS.RACE().sprite.getFolder("face").get(file);



		SnakeImage im = new SnakeImage(pp);
		int ww = 416;
		int hh = 60;
		if (im.width != ww || im.height < hh) {
			throw new RuntimeException(pp.toString() + " has wrong dimensions " + im.width + " " + im.height);
		}

		final int framesY = im.height / hh;

		final int[][] offYs = new int[framesY][FRAMES_X];
		final int[][] startYs = new int[framesY][FRAMES_X];
		final int[][] rowss = new int[framesY][FRAMES_X];

		
		for (int fy = 0; fy < framesY; fy++) {
			for (int fx = 0; fx < FRAMES_X; fx++) {

				int sx = 6 + 52 * fx;
				int sy = 6 + 60 * fy;

				int y1 = sy;
				int y2 = y1 + 48;

				outer: for (int dy = 0; dy < 48; dy++) {
					for (int dx = 0; dx < 40; dx++) {
						int x = sx + dx;
						if ((im.rgb.get(x, y1) & 0xFF) != 0)
							break outer;

					}
					y1++;
				}

				outer: 
				for (int dy = 0; dy < 48; dy++) {
					for (int dx = 0; dx < 40; dx++) {
						int x = sx + dx;
						if ((im.rgb.get(x, y2-1) & 0xFF) != 0)
							break outer;

					}
					y2--;
				}
				
				int offY = y1-sy;
				
				int rows = (int) Math.ceil((double)(y2-y1)/TILE_SIZE);
				
				if (y1 + rows*TILE_SIZE > sy+48) {
					y1 = sy+48-rows*TILE_SIZE;
					offY -= offY-(y1-sy);
				}
				
				offYs[fy][fx] = offY;
				startYs[fy][fx] = y1-sy;
				rowss[fy][fx] = rows;
				
				
			}
		}
		
		im.dispose();
		
		new IInit(pp, ww, hh);
		
		LinkedList<RaceFrameRaw> res = new LinkedList<>();
		for (int fy = 0; fy < framesY; fy++) {
			
			for (int fx = 0; fx < FRAMES_X; fx++) {

				
				
				final int offY = offYs[fy][fx];
				final int startY = startYs[fy][fx];
				final int rows = rowss[fy][fx];
				
				final int var = fx + fy*FRAMES_X;
				
				TILE_SHEET sheet = new ITileSheet() {

					@Override
					protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
						if (rows == 0) {
							return dFrame.sheet;
						}
						s.full.init(0, startY, 4, framesY, TILES_X, 6, d.s8);
						s.full.setVar(var);
						s.full.setSkip(rows*TILES_X, 0);
						s.full.paste(true);
						return d.s8.save(1);
					}
				}.get();
				res.add(new RaceFrameRaw(this, sheet, offY));
			}
		}

		
		return res;
	}

	
	
	private final TILE_SHEET DUMMY = new TILE_SHEET() {

		@Override
		public int tiles() {
			return TILES_X;
		}

		@Override
		public int size() {
			return TILE_SIZE;
		}

		@Override
		public void renderTextured(TextureCoords texture, int tile, int x1, int x2, int scale) {
			// TODO Auto-generated method stub

		}

		@Override
		public void renderTextured(TextureCoords texture, int tile, int x1, int y1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void render(SPRITE_RENDERER r, int tile, int x1, int x2, int y1, int y2) {
			// TODO Auto-generated method stub

		}

		@Override
		public TextureCoords getTexture(int tile) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	
	private final RaceFrameRaw dFrame = new RaceFrameRaw(this, DUMMY, 0);

}
