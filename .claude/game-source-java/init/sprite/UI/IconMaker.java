package init.sprite.UI;

import java.io.IOException;
import java.nio.file.Path;

import init.paths.PATH;
import init.paths.PATHS;
import init.paths.PathParser;
import init.sprite.UI.Icon.IconSheet;
import snake2d.util.color.COLOR;
import snake2d.util.file.Json;
import snake2d.util.file.SnakeImage;
import snake2d.util.sets.KeyMap;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerDests.Tile;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

public class IconMaker {

	public static final String split = "->";
	
	final PATH path;
	private KeyMap<TILE_SHEET> map = new KeyMap<>();
	public final int DIM;
	public final Icon DUMMY;
	
	IconMaker(String root, int dim){
		path = PATHS.SPRITE().getFolder("icon").getFolder(root);
		this.DIM = dim;
		DUMMY = new Icon(DIM, COLOR.ORANGE100);
	}
	
	Icon get(String relPath, int nr) throws IOException {
		return get(relPath+PathParser.split + nr, null, null);
		
	}
	
	public Icon get(String relPath, Json json, String jsonKey) throws IOException {
		
		Path p = PathParser.get(path, relPath, json, jsonKey, 1);
		
		if (p == null)
			return DUMMY;
		
		String[] ss = relPath.split(PathParser.split);
		
		if (ss.length < 0) {
			String ne = "The row of the icon file is missing at the end of the path. Sytax is folder->folder->file->0, where 0 is the row of the icon file.";
			PathParser.error(ne, json, jsonKey);
			return DUMMY;
		}
		
		int nr = -1;
		
		try {
			nr = Integer.parseInt(ss[ss.length-1]);
		}catch(NumberFormatException e) {
			String ne = "The final element in the path must be a number. Sytax is folder->folder->file->0, where 0 is the row of the icon file.";
			PathParser.error(ne, json, jsonKey);
			return DUMMY;
		}
		
		String ne = "The selected row of the file is out of bounds for the file. Sytax is folder->folder->file->0, where 0 is the row of the icon file.";
		
		if (nr < 0) {
			PathParser.error(ne, json, jsonKey);
		}
		
		TILE_SHEET sheet = sheet(p, json, jsonKey);
		if (sheet == null)
			return DUMMY;
		
		if (nr >= sheet.tiles()) {
			PathParser.error(ne, json, jsonKey);
			return DUMMY;
		}
		
		return new IconSheet(DIM, sheet, nr);
		
	}
	
//	public Icon get(Json j, String key, String relPath) throws IOException {
//		
//
//		String[] ss = relPath.split(split);
//		
//		
//		
//		if (ss.length < 2) {
//			GAME.Warn(j.errorGet("is badly formatted. Needs to contain a path with separation denoted by -> and the final entry being a number indicating which icon to pick of the sheet", key));
//			return DUMMY;
//		}
//		
//		try {
//			int nr = Integer.parseInt(ss[ss.length-1]);
//			String rPath = relPath.substring(0, relPath.length()-ss[ss.length-1].length());
//			TILE_SHEET sheet = sheet(rPath, j);
//			if (sheet == null) {
//				return DUMMY;
//			}
//			if (nr >= sheet.tiles()) {
//				GAME.Warn(file(rPath, j) + " does not have an icon at index: " + nr);
//				return DUMMY;
//			}
//			return new IconSheet(DIM, sheet, nr);
//		}catch(NumberFormatException e) {
//			GAME.Warn(j.errorGet(relPath + " '" +ss[ss.length-1] + "' is badly formatted. Needs to end with  ->X where X is a number indicating which icon to pick of the sheet", key));
//		}
//		return DUMMY;
//		
//	}
	
//	private void complain(PATH p, String file, Json json) {
//		String err = p.get() + "/" + file + " does not contain the icon image file: " + file + System.lineSeparator();
//		if (!hasComplained) {
//			String available = "";
//			for (String s : p.getFiles()) {
//				available += s + "," + System.lineSeparator();
//			}
//			
//			if (json != null)
//				err += json.path() + System.lineSeparator();
//			err += "Available: " + System.lineSeparator() + available;
//			GAME.Warn(err);
//			hasComplained = true;
//		}else {
//			if (json != null)
//				err += json.path() + System.lineSeparator();
//			LOG.ln(err);
//		}
//	}
	
//	private String file(String relPath, Json json) {
//		String[] pp = relPath.split(split);
//		PATH p = path;
//		for (int i = 0; i < pp.length-1; i++) {
//			if (!p.existsFolder(pp[i])) {
//				complain(p, pp[i], json);
//				return null;
//			}
//			p = p.getFolder(pp[i]);
//		}
//		return pp[pp.length-1];
//	}
//	
	private TILE_SHEET sheet(Path path, Json json, String jsonKey) throws IOException {
		
		String kk = ""+path.toAbsolutePath();
		
		if (!map.containsKey(kk)) {

			SnakeImage im = new SnakeImage(path);
			final int iwidth = im.width/2;
			final int iheight = im.height;
			im.dispose();
			
			if ((iwidth-6)%(DIM+6) != 0 || (iheight-6)%(DIM+6) != 0) {
				PathParser.error(path.toAbsolutePath() + " does not have the right dimensions: Should be a multiple of " + DIM + " squares. Look at other file for reference.", json, jsonKey);
				return null;
			}
			
			int xs = (iwidth-6)/(DIM+6);
			int ys = (iheight-6)/(DIM+6);
			
			TILE_SHEET s = new ITileSheet(path, iwidth*2, iheight) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					Tile t = d.s16;
					if (DIM == 24)
						t = d.s24;
					if (DIM == 32)
						t = d.s32;
					s.singles.init(0, 0, 1, 1, xs, ys, t);
					s.singles.paste(true);
					return t.saveGame();
				}
			}.get();
			map.put(kk, s);
			
		}
		return map.get(kk);
	}

	

	
}
