package game.audio;

import java.nio.file.Path;

import init.paths.PATHS;
import snake2d.CORE;
import snake2d.SoundStream;
import snake2d.util.sets.LinkedList;

public final class MusicFactory extends AudioFactory<SoundStream>{


	public MusicFactory(){
		super("MUSIC", PATHS.AUDIO().music, new SoundStream.Dummy());
	}
	
	@Override
	protected SoundStream create(LinkedList<SoundStream> all, Path p, String key) {
		return CORE.getSoundCore().getStream(p, true);
	}
	
}
