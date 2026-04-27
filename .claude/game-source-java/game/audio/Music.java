package game.audio;

import java.util.Random;

import game.GAME;
import game.time.TIME;
import init.paths.PATHS;
import snake2d.SoundStream;
import snake2d.util.file.Json;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;

public final class Music {

	private final MusicFactory factory;
	
	public AudioFactory<SoundStream> factory(){
		return factory;
	}
	
	private SoundStream current;
	private SoundStream[] currentA;
	
	private final SoundStream[] normal;
	private final SoundStream[] battle;
	private int r = 0;
	
	private double fade = 0;
	private double timeout = 0;
	private boolean shuffle = false;
	
	Music() {
		
		factory = new MusicFactory();
		
		Json json = new Json(PATHS.AUDIO().config.get("Music"));
		
		normal = get(factory.read("NORMAL", json));
		battle = get(factory.read("BATTLE", json));
		
		currentA = normal;
		current = currentA[0];
		current.setGain(1f);
		fade = 1f;
		current.play();
		

		
	}
	
	void update(double ds) {
		
		if (GAME.ARMIES().enemy().men() > 0) {
			if (shuffle || currentA != battle) {
				if (fade < 0) {
					current.stop();
					currentA = battle;
					shuffle = false;
					fade = 1;
				}else {
					current.setGain(fade);
				}
				fade -= ds;
			}else if(!current.isPlaying()){
				fade = CLAMP.d(fade+ds, 0, 1);
				r++;
				r %= currentA.length;
				current = currentA[r];
				current.setGain(fade);
				current.play();
			}
		}else if(TIME.light().dayIs()) {
			if (shuffle || currentA != normal) {
				if (fade < 0) {
					current.stop();
					currentA = normal;
					shuffle = false;
					fade = 1;
					timeout = 2 + RND.rInt(10);
				}else {
					current.setGain(fade);
				}
				fade -= ds;
			}else if(!current.isPlaying()){
				if (timeout > 0) {
					timeout -= ds;
				}else {
					fade = CLAMP.d(fade+ds, 0, 1);
					r++;
					r %= currentA.length;
					current = currentA[r];
					current.setGain(fade);
					current.play();
				}
				
				
			}
		}else {
			if (currentA == battle) {
				if (fade < 0) {
					current.stop();
				}
				fade -= ds;
			}
		}
		
		
		
	}
	
	private SoundStream[] get(LIST<SoundStream> streams) {

		SoundStream[] res = new SoundStream[streams.size()];
		int i = 0;
		for (SoundStream s : streams) {
			res[i++] = s;
		}
		Random ran = new Random();
		ran.setSeed(System.currentTimeMillis());
		for (int k = 0; k < res.length*4; k++) {
			int i1 =ran.nextInt(res.length);
			int i2 = ran.nextInt(res.length);
			SoundStream s = res[i1];
			res[i1] = res[i2];
			res[i2] = s;
		}
		
		return res;
		
	}
	
	public void next() {
		shuffle = true;
	}
	
}
