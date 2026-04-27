package init.race.appearence;

import java.io.IOException;

import init.race.ExpandInit;
import settlement.stats.Induvidual;
import snake2d.SPRITE_RENDERER;
import snake2d.util.file.Json;

public final class RPortrait {

	private final RPortraitFrame[] frames;
	public static final int P_WIDTH = 5*8;
	public static final int P_HEIGHT = 8*8;

	RPortrait(ExpandInit init, RColors colors, Json json) throws IOException{
		if (!json.has("FACE")) {
			frames = new RPortraitFrame[0];
			return;
		}
		
		Json[] js = json.jsons("FACE");
		frames = new RPortraitFrame[js.length];
		for (int i = 0; i < frames.length; i++) {
			frames[i] = new RPortraitFrame(init.fm, colors, js[i], i);
		}
		
	}

	public void render(SPRITE_RENDERER r, int x1, int y1, Induvidual indu, int scale) {
		
		y1 += scale*8;
		for (RPortraitFrame f : frames) {
			f.render(r, x1, y1, indu, scale);
		}
	}
	


	
	
}
