package game.audio;

import game.GAME;
import snake2d.CORE;

public class AUDIO {

	private static AUDIO s;
	
	private SoundRaces races;
	private final Music music;
	private final SoundFactory mono;
	private final Ambiances ambiences;
	private final AmbianceUpdater aUpdater;
	
	public AUDIO(GAME game) {
		CORE.getSoundCore().disposeSounds();
		s = this;
		mono = new SoundFactory();
		music = new Music();
		ambiences = new Ambiances();
		aUpdater = new AmbianceUpdater(ambiences);
		
	}
	
	public void update(double ds) {
		music.update(ds);
		aUpdater.update();
	}
		
	public static void setSettGain(double gain) {
		s.mono.settGain(gain);
	}
	
	public static SoundRace race(String key) {
		return s.races.get(key);
	}
	
	static SoundFactory mono() {
		return s.mono;
	}
	
	public static Music music() {
		return s.music;
	}
	
	public static Ambiances AMBI() {
		return s.ambiences;
	}
	
	public static AmbianceUpdater AMBI_UP() {
		return s.aUpdater;
	}

	public void init() {
		new Debug();
		races = new SoundRaces(mono);
	}
}
