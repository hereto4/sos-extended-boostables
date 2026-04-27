package init.resources;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map.Entry;

import init.paths.PATH;
import init.type.TERRAIN;
import init.type.TERRAINS;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.text.Str;
import util.keymap.MAPPED;
import util.keymap.RMAP;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.text.D;
import util.spritecomposer.ComposerUtil;

public final class Minable implements MAPPED{

	private static CharSequence ¤¤minable = "¤{0} Deposits";
	static {
		D.ts(Minable.class);
	}
	public final RESOURCE resource;
	public final CharSequence name;
	public final TILE_SHEET sheet;
	public final boolean onEverymap;
	public final COLOR tint;
	public final COLOR miniColor;
	public final int index;
	private final double[] terrainPref;
	public final double occurence;
	public double fertilityIncrease;
	private final String key;
	
	Minable(String key, int index, TILE_SHEET sheet, Json json){
		onEverymap = json.bool("ON_EVERY_MAP");
		tint = new ColorImp(json);
		miniColor = new ColorImp(json, "MINIMAP_COLOR");
		fertilityIncrease = json.d("FERTILITY_INCREASE", -1, 1);
		this.sheet = sheet;
		this.index = index;
		this.resource = RESOURCES.map().read(json);
		name = new Str(¤¤minable).insert(0, resource.name).trim();
		terrainPref = TERRAINS.MAP().readFill(json, 1.0);
		double mm = 0;
		for (double d : terrainPref) {
			mm += d;
		}
		for (int i = 0; i < terrainPref.length; i++) {
			terrainPref[i] /= mm;
		}
		
		
		occurence = json.dTry("OCCURENCE", 0, 1000, 1);
		this.key = key;

	}
	
	static RMAP<Minable> make(PATH pathData, PATH pathSprites) throws IOException{
		String folder = "minable";
		
		final PATH pd = pathData.getFolder(folder);
		final PATH ps = pathSprites.getFolder(folder);
		final HashMap<String, TILE_SHEET> spriteMap = new HashMap<>();
		
		Util util = new Util();
		String[] files = pd.getFiles(1,31);
		final ArrayList<Minable> res = new ArrayList<>(files.length);
		
		for (String p : files) {
			Json j = new Json(pd.get(p));
			String sprite = j.value("SPRITE");
			if (!spriteMap.containsKey(sprite)) {
				if (!ps.exists(sprite)) {
					
					String er = "Could not find texture file named: " + sprite + " Found only: " + System.lineSeparator();
					for (Entry<String, TILE_SHEET> e: spriteMap.entrySet()) {
						er += System.lineSeparator() + e.getKey();
					}
					j.error(er, sprite);
				}
				spriteMap.put(sprite, util.sprite(ps.get(sprite)));
				
			}
			Minable g = new Minable(p, res.size(), spriteMap.get(sprite), j);
			res.add(g);
		}
		
		return new RMAP<Minable>("MINABLE", res);
		
	}
	
	static final class Util {
		
		
		Util(){
			
		}
		
		private TILE_SHEET sprite(Path path) throws IOException {
			return new ITileSheet(path, 364, 94) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.init(0, 0, 1, 1, 8, 2, d.s16);
					s.singles.paste(1, true);
					return d.s16.saveGame();
				}
			}.get();
		}
		
	}

	@Override
	public int index() {
		return index;
	}
	
	public double terrain(TERRAIN t) {
		return terrainPref[t.index()];
	}
	
	@Override
	public String key() {
		return key;
	}
	
}
