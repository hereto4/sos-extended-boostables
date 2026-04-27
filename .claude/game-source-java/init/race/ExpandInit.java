package init.race;

import java.io.IOException;

import init.paths.PATH;
import init.paths.PATHS;
import init.race.appearence.RExtras;
import init.race.appearence.RaceFrameMaker;
import init.race.appearence.RaceSheet;
import init.sprite.UI.Icon;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.Tuple;
import snake2d.util.sprite.TILE_SHEET;

public final class ExpandInit {

	public final KeyMap<Tuple<Icon, Icon>> icons = new KeyMap<>();
	public final KeyMap<RaceSheet> map = new KeyMap<>();
	public final KeyMap<RExtras> extras = new KeyMap<>();
	public final KeyMap<RaceSheet> children = new KeyMap<>();
	public final KeyMap<String[]> names = new KeyMap<>();
	public final KeyMap<TILE_SHEET> skelletons = new KeyMap<>();
	public final KeyMap<TILE_SHEET> portraits = new KeyMap<>();
	public final KeyMap<TILE_SHEET> sleep = new KeyMap<>();
	public final KeyMap<TILE_SHEET> crowns = new KeyMap<>();
	public final KeyMap<KingMessages> kmessagess = new KeyMap<>();
	
	public final PATH p = PATHS.INIT().getFolder("race");
	public final PATH pt = PATHS.TEXT().getFolder("race");
	public final PATH sg = PATHS.SPRITE().getFolder("race");
	
	public final RaceFrameMaker fm = new RaceFrameMaker();
	
	public ExpandInit() throws IOException {
		
		
			
	}
	
}
