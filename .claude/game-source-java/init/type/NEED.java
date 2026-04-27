package init.type;

import game.boosting.BOOSTING;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import init.paths.PATHS.ResFolder;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import settlement.stats.service.StatService;
import snake2d.SPRITE_RENDERER;
import snake2d.util.file.Json;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.text.Str;
import util.keymap.MAPPED;
import util.text.D;

public class NEED implements MAPPED{
	
	private static CharSequence ¤¤rateD = "The rate at which the need of {0} increases daily.";
	static {
		D.ts(NEED.class);
	}
	
	public final CharSequence nameNeed;
	public final String key;
	public final double event;
	public final Boostable rate;
	private final int index;
	public final boolean basic;
	
	NEED(String key, ResFolder f, LISTE<NEED> all, BoostableCat cat, SPRITE icon, boolean basic) {
		this.index = all.add(this);
		this.key = key;
		Json jt = new Json(f.text.get(key));
		Json jd = new Json(f.init.get(key));
		this.nameNeed = jt.text("NAME_NEED");
		this.basic = basic;
		this.event = jd.dTry("EVENT", 0, 10000, 0);
		if (icon == null)
			icon = UI.icons().s.clock;
		final SPRITE ii = icon;
		SPRITE ico = new SPRITE.Imp(Icon.S) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				LIST<StatService> g = STATS.SERVICE().perNeed(NEED.this);
				if (g != null && g.size() > 0)
					g.get(0).icon.render(r, X1, X2, Y1, Y2);
				else
					ii.render(r, X1, X2, Y1, Y2);
				
				
			}
		};
		
		this.rate = BOOSTING.push(key, jd.d("RATE"), jt.text("NAME_RATE"), ""+Str.TMP.clear().add(¤¤rateD).insert(0, nameNeed), ico, cat);
	}
	
	@Override
	public int index() {
		return index;
	}
	
	@Override
	public String toString() {
		return key;
	}
	
	public LIST<StatService> sGroup() {
		return STATS.SERVICE().perNeed(NEED.this);
	}
	
	@Override
	public String key() {
		return key;
	}
	


}
