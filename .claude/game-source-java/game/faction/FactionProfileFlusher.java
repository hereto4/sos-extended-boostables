package game.faction;

import game.faction.player.Player;
import game.faction.player.PlayerColors;
import game.faction.player.PlayerColors.PlayerColor;
import init.paths.PATHS;
import init.sprite.BitmapSprite;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import snake2d.util.file.JsonE;

public class FactionProfileFlusher {

	private static final String name = "FACTION";
	
	public static void flush(Player p) {
		
		try {
			JsonE j = new JsonE();
			j.addString("RULER_NAME", ""+p.rulerName());
			j.addString("FACTION_NAME", ""+p.name);
			color(j, p.banner().colorBG(), "COLOR_BANNER_BACKGROUND");
			color(j, p.banner().colorFG(), "COLOR_BANNER_FOREGROUND");
			color(j, p.banner().colorBorder(), "COLOR_BANNER_BORDER");
			color(j, p.banner().colorPole(), "COLOR_BANNER_POLE");
			
			JsonE cols = new JsonE();
			
			for (String k : PlayerColors.cats().keys()) {
				for (PlayerColor c : PlayerColors.cats().get(k)) {
					color(cols, c.color, c.cat + "_" + c.key);
				}
			}
			String s = "";
			for (int i = 0; i < BitmapSprite.AREA; i++) {
				s += p.banner().sprite.is(i) ? "1" : "0";
			}
			j.add("BANNER_DATA", s);
			
			j.add("COLORS", cols);
			
			if (!PATHS.local().PROFILE.exists(name))
				PATHS.local().PROFILE.create(name);
			j.save(PATHS.local().PROFILE.get(name));
		}catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	private static void color(JsonE j, COLOR c, String key) {
		String v = (2*(c.red()&0x0FF)) + "_" + 2*(c.green()&0x0FF) + "_" + 2*(c.blue()&0x0FF);
		j.add(key, v);
	}
	
	public static boolean canLoad(Player p) {
		return PATHS.local().PROFILE.exists(name);
	}
	
	public static void load(Player p) {

		if (!PATHS.local().PROFILE.exists(name))
			return;
		try {
			Json json = new Json(PATHS.local().PROFILE.get(name));
			p.rulerName.clear().add(json.text("RULER_NAME"));
			p.name.clear().add(json.text("FACTION_NAME"));
			p.banner().colorBG().set(new ColorImp(json, "COLOR_BANNER_BACKGROUND"));
			p.banner().colorFG().set(new ColorImp(json, "COLOR_BANNER_FOREGROUND"));
			p.banner().colorBorder().set(new ColorImp(json, "COLOR_BANNER_BORDER"));
			p.banner().colorPole().set(new ColorImp(json, "COLOR_BANNER_POLE"));
			

			
			String s = json.value("BANNER_DATA");
			for (int i = 0; i < s.length(); i++) {
				p.banner().sprite.set(i, s.charAt(i) == '1');
			}
			
			if (json.has("COLORS")) {
				json = json.json("COLORS");
				for (String k : PlayerColors.cats().keys()) {
					for (PlayerColor c : PlayerColors.cats().get(k)) {
						String kk =  c.cat + "_" + c.key;
						if (json.has(kk))
							c.color.set(new ColorImp(json, kk));
					}
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace(System.out);
		}
	}
	
}
