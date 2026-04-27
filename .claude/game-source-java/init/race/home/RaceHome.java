package init.race.home;

import java.io.IOException;

import init.paths.PATHS;
import init.type.HCLASS;
import init.type.HCLASSES;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.Induvidual;
import snake2d.util.file.Json;

public final class RaceHome {

	private final RaceHomeClass DUMMY;
	private final RaceHomeClass[] all = new RaceHomeClass[HCLASSES.ALL().size()];
	
	
	public RaceHome(String key) throws IOException{
		
		Json json = new Json(PATHS.INIT().getFolder("race").getFolder("home").get(key));
		DUMMY = new RaceHomeClass();
		for (int i = 0; i < all.length; i++)
			all[i] = DUMMY;
		all[HCLASSES.CITIZEN().index()] = new RaceHomeClass(json.json(HCLASSES.CITIZEN().key));
		all[HCLASSES.NOBLE().index()] = new RaceHomeClass(json.json(HCLASSES.NOBLE().key));
		all[HCLASSES.SLAVE().index()] = new RaceHomeClass(json.json(HCLASSES.SLAVE().key));
	}
	
	public RaceHomeClass clas(Humanoid h) {
		if (h == null)
			return DUMMY;
		return all[h.indu().clas().index()];
	}
	
	public RaceHomeClass clas(Induvidual h) {
		return all[h.clas().index()];
	}
	
	public RaceHomeClass clas(HCLASS c) {
		return all[c.index()];
	}
	
	
}
