package init.race.appearence;

import java.io.IOException;

import init.race.ExpandInit;
import init.race.appearence.RColors.ColorCollection;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

public final class RType {

	public final RTypeSpec spec;
	public final RPortrait portrait;
	public final RNames names;
	public final RaceSheet sheet;
	public final TILE_SHEET sheet_skelleton;
	
	public final LIST<RAddon> addonsBelow;
	public final LIST<RAddon> addonsAbove;

	RType(RColors colors, Json json, RExtras extra, ExpandInit init) throws IOException{
		String ssprite = json.value("SPRITE_FILE");
		{
			
			if (init.map.containsKey(ssprite)) {
				sheet = init.map.get(ssprite);
			}else {
				sheet = new RaceSheet(init.sg.get(ssprite));
				init.map.put(ssprite, sheet);
			}
		}
		
		{
			String sprite = json.value("SPRITE_SKELLETON_FILE");
			if (!init.skelletons.containsKey(sprite)) {
				sheet_skelleton = new ITileSheet(init.sg.getFolder("skelleton").get(sprite), 316, 120) {
					
					@Override
					protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
						s.singles.init(0, 0, 1, 1, 4, 3, d.s32);
						int a = 6;
						for (int i = 0; i < a; i++) {
							s.singles.setSkip(i * 2, 2).paste(3, true);
						}
						return d.s32.saveGame();
					}
				}.get();
				init.skelletons.put(sprite, sheet_skelleton);
			}else
				sheet_skelleton = init.skelletons.get(sprite);
		}
		
		
		
		spec = new RTypeSpec(colors, json);
		portrait = new RPortrait(init, colors, json);
		names = new RNames(json, init.names);
		
		LinkedList<RAddon> below = new LinkedList<>();
		LinkedList<RAddon> above = new LinkedList<>();
		
		if (json.has("ADDONS")) {
			
			new ComposerThings.IInit(init.sg.get(ssprite), 448, 546);
			RAddon[] done = new RAddon[8];
			
			
			for (Json j : json.jsons("ADDONS")) {
				if (j.bool("BELOW_HEAD"))
					below.add(new RAddon(j, colors, done));
				else
					above.add(new RAddon(j, colors, done));
			}
		}
		
		this.addonsAbove = new ArrayList<RAddon>(above);
		this.addonsBelow = new ArrayList<RAddon>(below);
	}
	
	public static class RTypeSpec {
		public final double occurrence;
		public final ColorCollection skin;
		public final ColorCollection leg;
		
		
		RTypeSpec(RColors colors, Json json){
			
			occurrence = json.has("OCCURRENCE") ? json.d("OCCURRENCE") : 0.5;
			skin = colors.collection.read("COLOR_SKIN", json);
			leg = colors.collection.read("COLOR_LEG", json);
		}
	}
	
}
