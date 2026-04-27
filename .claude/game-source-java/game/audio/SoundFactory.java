package game.audio;

import java.nio.file.Path;

import game.GAME;
import init.paths.PATHS;
import snake2d.SoundEffect;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;

final class SoundFactory {

	
	double sGain;
	
	private final KeyMap<Sound> map = new KeyMap<>();
	
	public final AudioFactory<SoundFile> factory = new Factory();
	public final Sound DUMMY = new Sound(new ArrayList<SoundFile>(factory.DUMMY()));
	
	SoundFactory() {
		
	}
	
	public Sound get(String key) {
		if (!map.containsKey(key)) {
			GAME.Warn("no sound by the key of: " + key);
			return new Sound(factory.LDUMMY());
		}
		return map.get(key);
	}
	
	public Sound read(Json json) {
		return read("SOUND", json);
	}
	
	public Sound read(String key, Json json) {
		LIST<SoundFile> ss = factory.read(key, json);
		return new Sound(ss);
	}
	
	public void settGain(double gain) {
		this.sGain = gain;
	}
	
	static class Factory extends AudioFactory<SoundFile> {
		
		Factory(){
			super("SOUND", PATHS.AUDIO().mono, new SoundFile(new LinkedList<>(), new SoundEffect.Dummy(), "DUMMY"));
		}

		@Override
		protected SoundFile create(LinkedList<SoundFile> all, Path p, String key) {
			return new SoundFile(all, p, key);
		}
	}
}
