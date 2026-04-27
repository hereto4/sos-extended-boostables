package init.race.appearence;

import java.io.IOException;

import init.paths.PATHS;
import init.race.ExpandInit;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import init.type.HTYPES;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;
import world.map.regions.centre.WorldRaceSheet;

public final class RAppearence {

	public final RColors colors;
	public final Icon icon;
	public final Icon iconBig;
	public final TILE_SHEET sleep;
	public final RExtras extra;
	public final int off;
	public final LIST<String> lastNamesNoble;
	public final RFloors floors;
	
	public final RCrown crown;
	public final RType child;
	public final LIST<RType> types;
	public double tMax;
	public final WorldRaceSheet world;
	
	public RAppearence(Race race, Json data, ExpandInit init, int hitboxSize) throws IOException{
		
		icon = SPRITES.icons().get(data, "ICON_SMALL");
		iconBig = SPRITES.icons().get(data, "ICON_BIG");
		floors = new RFloors(data);
		data =  new Json(PATHS.RACE().init.getFolder("sprite").get(data.value("SPRITE_FILE")));
		colors = new RColors(data);
		
		lastNamesNoble = RNames.names("NAMESET_FILE_NOBLE", data, init.names);
		
		world = new WorldRaceSheet(data.json("WORLD"));
		
		String s = data.value("SPRITE_EXTRA_FILE");
		if (init.extras.containsKey(s)) {
			this.extra = init.extras.get(s);
		}else {
			this.extra = new RExtras(init.sg.getFolder("extra").get(s));
			init.extras.put(s, this.extra);
		}
		
		
		
		String ssleep = data.value("SLEEP_FILE");
		if (init.sleep.containsKey(ssleep)) {
			this.sleep = init.sleep.get(ssleep);
		}else {
			
			sleep = new ITileSheet(init.sg.getFolder("sleep").get(ssleep), 164, 44) {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					int am = c.getSource().height /38;
					s.singles.init(0, 0, 1, 1, 2, am, d.s32);
					for (int i = 0; i < am; i++) {
						s.singles.setSkip(i * 2, 2).paste(3, true);
					}
					return d.s32.saveGame();
				}
			}.get();
			
			init.sleep.put(ssleep, sleep);
		}
		

		
		crown = new RCrown(init, data);
		
		{
			this.child = new RType(colors, data.json("CHILD"), extra, init);
		}
		
		{
			Json[] jjs = data.jsons("TYPES", 1, 4);
			
			ArrayList<RType> types = new ArrayList<>(jjs.length);
			
			for (Json j : jjs) {
				types.add(new RType(colors, j, extra, init));
			}
			
			this.types = types;
		}
		
		double bb = 0;
		for (RType t : types) {
			bb += t.spec.occurrence;
		}
		tMax = bb;
		
		off = (types.get(0).sheet.sheet.size() - hitboxSize)/2;
		
	}



	public RType adult() {
		return types.get(0);
	}
//	
	public RType child() {
		return child;
	}
	
	public RType sheet(Induvidual indu) {
		return indu.hType() == HTYPES.CHILD() ? child : types.getC(STATS.APPEARANCE().gender.get(indu));
	}
	
	public TILE_SHEET skelleton(Induvidual indu) {
		return sheet(indu).sheet_skelleton;
	}
	
	public TILE_SHEET skelleton(boolean adult) {
		return !adult ? types.get(0).sheet_skelleton : child.sheet_skelleton;
	}
	
}
