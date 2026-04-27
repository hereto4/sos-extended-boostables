package init.sprite.game;

import game.time.TIME;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;

public final class SheetData {
	
	public double FPS = 0;
	private int FPS_INTERVAL = 64;
	public int shadowLength = 0;
	public int shadowHeight = 0;
	public boolean rotates = false;
	public LIST<COLOR> colors = shades;
	public boolean circular = false;
	
	private static final double RANI = 1.0/0x0FF;
	private static final ArrayList<COLOR> onlyWhite = new ArrayList<>(COLOR.WHITE100);
	private static final ArrayList<COLOR> shades = new ArrayList<>(48);
	static {
		
		for (int i = 0; i < 48; i++) {
			int d = i /3;
			int q = i %3;
			shades.add(new ColorImp(127-d*2-4*(q&1), 127-d*2-4*((q>>1)&1), 127-d*2-4*((q>>2)&1)));
		}
		
	}
	
	public static SheetData DUMMY = new SheetData();
	
	public SheetData(){
		
	}
	
	public SheetData(Json json){
		test(json);
		FPS = json.dTry("FPS", 0, 100000, 0);
		FPS_INTERVAL = (int) (64*json.dTry("FPS_INTERVAL", 0, 1, 1));

		shadowLength = (int) json.dTry("SHADOW_LENGTH", 0, 100, shadowLength);
		shadowHeight = (int) json.dTry("SHADOW_HEIGHT", 0, 100, shadowHeight);
		circular = json.bool("CIRCULAR", false);
		rotates = json.bool("ROTATES", true);
		if (json.has("COLOR")) {
			colors = new ArrayList<COLOR>(ColorImp.cols(json, "COLOR"));
		}else if (json.bool("TINT", true)) {
			colors = shades;
		}else
			colors = onlyWhite;
	}
	
	SheetData(SheetData def, Json json){
		test(json);
		FPS = json.dTry("FPS", 0, 100000, def.FPS);
		FPS_INTERVAL = (int) (64*json.dTry("FPS_INTERVAL", 0, 1, def.FPS_INTERVAL/64.0));
		shadowLength = (int) json.dTry("SHADOW_LENGTH", 0, 100, def.shadowLength);
		shadowHeight = (int) json.dTry("SHADOW_HEIGHT", 0, 100, def.shadowHeight);
		circular = json.bool("CIRCULAR", def.circular);
		rotates = json.bool("ROTATES", def.rotates);
		if (json.has("COLOR")) {
			colors = new ArrayList<COLOR>(ColorImp.cols(json, "COLOR"));
		}else if (json.bool("TINT", true)) {
			colors = shades;
		}else
			colors = def.colors;
	}
	
	private static KeyMap<String> oks;
	
	private static void test(Json json) {
		if (oks == null) {
			oks = new KeyMap<>();
			oks.put("FPS", "animation speed");
			oks.put("FPS_INTERVAL", "animate consistently");
			oks.put("SHADOW_LENGTH", "length shadow");
			oks.put("SHADOW_HEIGHT", "height shadow");
			oks.put("CIRCULAR", "animation circular");
			oks.put("ROTATES", "rotates");
			oks.put("COLOR", "color, or colors");
			oks.put("TINT", "tint sprite randomly");
			oks.put("FRAMES", "farmes spec");
			oks.put("OVERWRITE", "special stuff");
			oks.put("RESOURCES", "special stuff");
		}
		
		for (String k : json.keys()) {
			if (!oks.containsKey(k)) {
				String av = "";
				for (String kk : oks.keys()) {
					av += kk + " (" + oks.get(kk) + "), ";
				}
				json.error(k + " is not a valid key in a sprite json. Available: " + av, k);
			}
		}
	}
	
	public int frame(int random, double animationSpeed) {
		double frame = random >>8;
		animationSpeed *= FPS;
		if (animationSpeed <= 0) {
			return (int)frame;
		}
	
		frame += TIME.currentSecond()*animationSpeed;
		
		frame += ((random >> 16 & 0x0FF))*RANI;
		
		if (FPS_INTERVAL == 64)
			return (int)frame;
		int fi = (((int)frame)+(random>>>24))&(64-1);
		fi -= FPS_INTERVAL;
		if (fi > 0)
			frame -= fi;
			
		return (int)frame;
	}
	
	public COLOR color(int random) {
		return colors.getC(random);
	}
	
}