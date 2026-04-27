package game.audio;

import game.GAME;
import init.paths.PATH;
import init.paths.PATHS;
import snake2d.util.file.Json;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import util.gui.misc.GButt;
import util.keymap.RMAP;

final class SoundRaces {

	private final RMAP<SoundRace> rmap;
	private boolean debugged = false;
	private final SoundRace RDUMMY;
	
	
	SoundRaces(SoundFactory factory){
		final LinkedList<SoundRace> all = new LinkedList<>();
		
		PATH p = PATHS.AUDIO().config.getFolder("mono");
		for (String file : p.getFiles()) {
			Json json = new Json(p.get(file));
			LIST<String> keys = json.keys();
			
			for (String k : keys) {
				all.add(new SoundRace(all.size(), k, factory.read(k, json)));
			}
			
		}

		rmap = new RMAP<SoundRace>("SOUND", all);
		RDUMMY = new SoundRace(0, "DUMMY", new Sound(factory.factory.LDUMMY()));
		
		GButt.defaultHoverSound = get("UI_HOVER"); 
		GButt.defaultClickSound = get("UI_CLICK"); 
	}
	
	public SoundRace get(String key) {
		if (rmap.tryGet(key) == null) {
			
			if (!debugged) {
				String a = "Available " + System.lineSeparator();
				for (String s : rmap.available()) {
					a += s + System.lineSeparator();
				}
				
				GAME.Warn("no race sound by the key of: " + key + System.lineSeparator() + a);
				debugged = true;
			}else {
				System.err.println("no race sound by the key of: " + key);
			}
			
			return RDUMMY;
				
		}
		return rmap.tryGet(key);
	}
	
}
