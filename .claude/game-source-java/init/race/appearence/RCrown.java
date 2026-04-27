package init.race.appearence;

import java.io.IOException;

import init.paths.PATHS;
import init.paths.PATHS.ResFolder;
import init.race.ExpandInit;
import snake2d.LOG;
import snake2d.SPRITE_RENDERER;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

public final class RCrown {

	private ArrayListGrower<SPRITE> crowns = new ArrayListGrower<>();
	private ArrayListGrower<SPRITE> raiders = new ArrayListGrower<>();
	private ArrayListGrower<SPRITE> merc = new ArrayListGrower<>();
	
	RCrown(ExpandInit init, Json data) throws IOException{
		make(init, data, "CROWN", crowns);
		make(init, data, "RAIDER", raiders);
		make(init, data, "MERC", merc);
	}
	
	private static void make(ExpandInit init, Json data, String key, ArrayListGrower<SPRITE> crowns) throws IOException {
		if (!data.has(key))
			data.error("Not declared", key);
		if (data.has(key)) {
			if (data.arrayIs(key)) {
				for (Json j : data.jsons(key)) {
					make(j, init, crowns);
				}
			}else {
				data = data.json(key);
				make(data, init, crowns);
			}
		}
	}
	
	private static void make(Json json, ExpandInit init,  ArrayListGrower<SPRITE> crowns) throws IOException {
		int offX = json.i("OFFX", -48, 48);
		int offY = json.i("OFFY", -48, 48);
		
		String she = json.value("FILE");
		if (!init.crowns.containsKey(she)) {
			
			
			ResFolder f = PATHS.RACE().folder("face").folder("addon");
			if (!f.sprite.exists(she)) {
				LOG.err(json.errorGet(f.sprite.get() +  " No file named this.", she));
				return;
			}
			
			TILE_SHEET sheet = new ITileSheet(f.sprite.get(she), 104, 36) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					int FRAMES = c.getSource().height/36;
					s.full.init(0, 0, 1, FRAMES, 5, 3, d.s8);
					for (int i = 0; i < FRAMES; i++)
						s.full.setVar(i).paste(true);
					return d.s8.saveGame();
					
					
				}
			}.get();
			init.crowns.put(she, sheet);
		}
		
		final int tot = 5*3;
		TILE_SHEET sheet = init.crowns.get(she);
		int am = sheet.tiles()/tot;
		
		
		for (int i = 0; i < am; i++) {
			final int k = i*tot;
			SPRITE sprite = new SPRITE.Imp(40, 24) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					
					int t = k;
					
					int w = (X2-X1)/5;
					int h = (Y2-Y1)/3;
					
					int ox = offX*(X2-X1)/48;
					int oy = offY*(Y2-Y1)/24;
					
					for (int y = 0; y < 3; y++) {
						for (int x = 0; x < 5; x++) {
							sheet.render(r, t, X1+ox+x*w, X1+ox+x*w + w, Y1+oy+y*h, Y1+oy+y*h + h);
							t++;
						}
					}
				}
			};
			crowns.add(sprite);
		}

	}
	
	public LIST<SPRITE> crowns() {
		return crowns;
	}
	
	public LIST<SPRITE> raiders() {
		return raiders;
	}
	
	public LIST<SPRITE> merc() {
		return merc;
	}
}
