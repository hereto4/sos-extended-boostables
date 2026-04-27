package game.audio;


import java.nio.file.Path;

import game.GAME;
import init.paths.PATH;
import init.paths.PATHS;
import snake2d.CORE;
import snake2d.SoundStream;
import snake2d.util.file.Json;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import util.keymap.RMAP;

public final class Ambiances {

	private final RMAP<Ambiance> rmap;
	//private final ArrayListGrower<Ambiance> all = new ArrayListGrower<>();

	public final Ambiance nature;
	public final Ambiance wind;
	public final Ambiance night;
	public final Ambiance water;
	public final Ambiance rain;
	public final Ambiance windTrees;
	public final Ambiance windhowl;
	public final Ambiance thunder;
	public final AudioFactory<SoundStream> factory;
	private boolean debugged = false;
	
	Ambiances(){
		
		factory = new AudioFactory<SoundStream>("AMBIANCE", PATHS.AUDIO().ambience, new SoundStream.Dummy()) {
			
		
			@Override
			protected SoundStream create(LinkedList<SoundStream> all, Path p, String key) {
				return CORE.getSoundCore().getStream(p, false);
			}
			
		};
		
		final LinkedList<Ambiance> all = new LinkedList<>();
		
		PATH p = PATHS.AUDIO().config.getFolder("ambience");
		for (String file : p.getFiles()) {
			Json json = new Json(p.get(file));
			LIST<String> keys = json.keys();
			
			for (String k : keys) {
				new Ambiance(k, all, factory.read(k, json));
			}
			
		}

		rmap = new RMAP<Ambiance>("AMBIENCE", all);
		
		
		
		nature = get("NATURE");
		wind = get("WIND");
		night = get("NIGHT");
		water = get("WATER");
		rain = get("RAIN");
		windTrees = get("WIND_TREES");
		windhowl = get("CAVE");
		thunder = get("THUNDER");
		
		
	}
	
	public Ambiance get(String key) {
		if (rmap.tryGet(key) == null) {
			if (!debugged) {
				String a = "Available " + System.lineSeparator();
				for (String s : rmap.available()) {
					a += s + System.lineSeparator();
				}
				
				GAME.Warn("no ambiance sound by the key of: " + key + System.lineSeparator() + a);
				debugged = true;
			}else {
				System.err.println("no ambiance sound  by the key of: " + key);
			}
			
			return null;
				
		}
		return rmap.tryGet(key);
	}
	
	public LIST<Ambiance> all(){
		return rmap.all();
	}

	
	
}
