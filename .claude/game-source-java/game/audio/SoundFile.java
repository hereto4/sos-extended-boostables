package game.audio;

import java.nio.file.Path;

import snake2d.CORE;
import snake2d.SoundEffect;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LISTE;
import util.keymap.MAPPED;

public final class SoundFile implements MAPPED{
	
	private final int index;
	private final String key;
	public final SoundEffect sound;
	public double gain = 1.0;
	public double pitch = 0.3;
	
	SoundFile(LISTE<SoundFile> all, Path p, String key) {
		sound = CORE.getSoundCore().getEffect(p);
		index = all.add(this);
		this.key = key;
	}
	
	SoundFile(LISTE<SoundFile> all, SoundEffect p, String key) {
		sound = p;
		index = all.add(this);
		this.key = key;
	}

	@Override
	public int index() {
		return index;
	}

	@Override
	public String key() {
		return key;
	}
	
	public void rnd(RECTANGLE body) {
		rnd(body, 0.8f + RND.rFloat(0.2));
	}
	
	public void rnd(RECTANGLE body,double gain) {
		rnd(body.cX(), body.cY(), gain);
	}
	
	public void rnd(int x, int y, double gain) {
		gain *= this.gain;
		if (gain <= 0)
			return;
		float pitch = RND.rFloat1(this.pitch);
		sound.play(x, y, pitch, (float)gain, false);
	}


}