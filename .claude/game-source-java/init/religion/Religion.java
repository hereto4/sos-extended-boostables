package init.religion;

import java.io.IOException;

import game.boosting.BOOSTING;
import game.boosting.BValue;
import game.boosting.BoostSpecs;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import init.paths.PATHS;
import init.sprite.SPRITES;
import init.sprite.UI.Icon;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.Json;
import util.info.INFO;
import util.keymap.MAPPED;

public final class Religion implements MAPPED{
	
	private final int index;
	public final String key;
	public final COLOR color;
	public final INFO info;
	public final CharSequence diety;
	private double[] liking;
	public final Icon icon;
	public final double inclination;
	public final Boostable conversionCity;
	public final BoostSpecs boosts;
	

	
	Religion(String key, int index) throws IOException{
		this.key = key;
		this.index = index;
		Json d = json();
		Json t = new Json(PATHS.TEXT().getFolder("religion").get(key));
		info = new INFO(t);
		
		diety = t.text("DEITY");
		
		color = new ColorImp(d);
		icon = SPRITES.icons().get(d);
		inclination = d.d("DEFAULT_SPREAD");
		
		conversionCity = BOOSTING.push(key + "_CITY", 1, info.name, info.desc, icon, BoostableCat.ALL().RELIGION);
		boosts = new BoostSpecs(info.name, icon, false);
		boosts.read(d, BValue.VALUE1);
	}
	
	private Json json() {
		return new Json(PATHS.INIT().getFolder("religion").get(key));
	}
	
	public double opposition(Religion other) {
		return liking[other.index()];
	}

	void init() {
		liking = new double[RELIGIONS.ALL().size()];
		RELIGIONS.MAP().readFill("OPPOSITION", liking, json(), 0, 100);
	}

	@Override
	public int index() {
		return index;
	}
	
	@Override
	public String toString() {
		return "["+index+"]" + key;
	}
	
	@Override
	public String key() {
		return key;
	}

	
	
}