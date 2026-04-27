package game.audio;

import game.faction.FACTIONS;
import init.race.RACES;
import init.race.Race;
import settlement.entity.humanoid.Humanoid;
import snake2d.SoundSimple;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.rnd.RND;
import util.keymap.MAPPED;

public final class SoundRace implements MAPPED, SoundSimple{

	private Sound[] all;
	private final int index;
	private final String key;
	
	SoundRace(int index, String key, Sound sound){
		all = new Sound[RACES.all().size()];
		set(sound);
		
		this.key = key;
		this.index = index;
		
		if (false) {
			//filter world, sett and UI
		}
	}
	
	void set(Sound monos) {
		for (int ri = 0; ri < all.length; ri++) {
			set(ri, monos);
		}
	}
	
	void set(int ri, Sound monos) {
		all[ri] = monos;
	}
	
	@Override
	public int index() {
		return index;
	}

	@Override
	public String key() {
		return key;
	}
	
	public void rnd(Humanoid a) {
		rnd(a.race()).rnd(a.body());
	}
	
	private Sound rnd(Race race) {
		return all[race.index];
	}

	public void rnd(Race race, RECTANGLE body) {
		all[race.index].rnd(body);
	}
	
	public void play(Race race, int cx, int cy) {
		SoundFile f = all[FACTIONS.player().race().index].all.rnd();
		f.sound.play(cx, cy, 1.0f, (float)f.gain, false);
	}
	
	@Override
	public boolean play(boolean priority) {
		SoundFile f = all[FACTIONS.player().race().index].all.rnd();
		return f.sound.play(1.0f, (float)f.gain, false);
	}
	
	public void rnd(RECTANGLE body) {
		rnd(body, 0.8f + RND.rFloat(0.2));
	}
	
	public void rnd(RECTANGLE body,double gain) {
		rnd(body.cX(), body.cY(), gain);
	}
	
	public void rnd(int x, int y, double gain) {
		
		rnd(FACTIONS.player().race()).rnd(x, y, gain*AUDIO.mono().sGain);
	}
	
}
