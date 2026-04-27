package init.race.appearence;

import java.io.IOException;

import game.GAME;
import init.race.appearence.RColors.ColorCollection;
import init.value.GVALUES;
import init.value.Lockable;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.color.OpacityImp;
import snake2d.util.file.Json;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;

final class RPortraitFrame {

	private final LIST<RaceFrameRaw> frames;
	private final int random;
	final int occurence;
	
	private int dx,dy,dxr,dyr;
	
	private final ColorCollection color;
	private final int opacity;
	private static final OpacityImp op = new OpacityImp(0);

	public final boolean stains;
	
	public final Lockable<Induvidual> cons = GVALUES.INDU.LOCK.push();
	
	
	private static KeyMap<String> keepClean = new KeyMap<>();
	static {
		String[] keep = new String[] {
			"FRAMES",
			"FRAME_RANDOM",
			"FRAME_OCCURENCE",
			"OFF_X",
			"OFF_Y",
			"OFF_X_RANDOM",
			"OFF_Y_RANDOM",
			"CONDITIONS",
			"COLOR",
			"OPACITY",
			"STAINS",
		};
		
		for (String s : keep) {
			keepClean.put(s, s);
		}
	}
	
	
	RPortraitFrame(RaceFrameMaker fm, RColors colors, Json json, int i) throws IOException{
		
		for (String s : json.keys()) {
			if (!keepClean.containsKey(s)) {
				GAME.Warn(json.errorGet(s + " is not a valid modifier, available:  " + keepClean.keysString(), s));
			}
		}
		
		frames = fm.read(json);
		random = json.has("FRAME_RANDOM") ? json.i("FRAME_RANDOM", 0, 16) : i%16;
		occurence = (int) (0x010*(json.has("FRAME_OCCURRENCE") ? json.d("FRAME_OCCURRENCE", 0, 1) : 1.0));
		dx = json.has("OFF_X") ? json.i("OFF_X", -40, 40) : 0;
		dy = json.has("OFF_Y") ? json.i("OFF_Y", -48, 48) : 0;
		dxr = json.has("OFF_X_RANDOM") ? json.i("OFF_X_RANDOM", 0, 40) : 0;
		dyr = json.has("OFF_Y_RANDOM") ? json.i("OFF_Y_RANDOM", 0, 48) : 0;
		cons.push("CONDITIONS", json);
		
		
		
		color = json.has("COLOR") ? colors.collection.get(json.value("COLOR"), json) : RColors.dummy;
		opacity = json.has("OPACITY") ? json.i("OPACITY", 0, 256) : 255;
		stains = json.has("STAINS") ? json.bool("STAINS") : true;
		
	}
	
	public void render(SPRITE_RENDERER r, int x1, int y1, Induvidual indu, int scale) {
		
		if (frames.size() == 0)
			return;
		

		
		if (!cons.passes(indu))
			return;
		
		int ran = (int) ((STATS.RAN().get(indu, (random*16))) & 0x0FF);
		if (occurence <= (ran&0x0F))
			return;
		

		COLOR col = color.get(indu, STATS.APPEARANCE().dead.indu().get(indu) == 1);
		
//		double grayAt = STATS.POP().age.dage.getD(indu);
		
//		if (color.turnsGrayWhenOld && grayAt > 0.7) {
//			double d = grayAt-0.7;
//			d /= 0.2;
//			d = CLAMP.d(d, 0, 1);
//			col = ColorImp.TMP.interpolate(col, RColors.grey, d);
//		}
		
		col = ColorImp.TMP.set(col).shadeSelf(1.2);
		
		col.bind();
		op.set(opacity);
		op.bind();
		
		x1 = (int) (x1 + (dx + (ran/15.0)*dxr)*scale);
		y1 = (int) (y1 + (dy + ((ran>>4)/15.0)*dyr)*scale);
		
		
		
		int var = ran % frames.size();
		
		frames.get(var).render(r, x1, y1, scale);
		COLOR.unbind();
		OPACITY.unbind();

		if (stains) {
			frames.get(var).renderOverlay(r, x1, y1, scale, STATS.NEEDS().INJURIES.COUNT.indu().getD(indu), STATS.NEEDS().grime(indu), indu.race().appearance().colors.blood);
		}
		
	}
	
}
