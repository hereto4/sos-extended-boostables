package init.race.bio;

import java.io.IOException;

import init.paths.PATHS;
import init.race.Race;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.standing.STANDINGS;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;

public final class Bio {

	private static final KeyMap<BioLines> cachebio = new KeyMap<>();

	private final BioLines data;
	private final BioOpinion improve;
	private final LIST<Str> tmp = new ArrayList<Str>(new Str(128));
	
	public Bio(Json json, Race race)  throws IOException{

		
		
		String f = json.value("BIO_FILE");
		Json org = new Json(PATHS.TEXT().getFolder("race").getFolder("bio").get(f));
		if (!cachebio.containsKey(f)) {
			
			BioLines d = new BioLines(org);
			cachebio.put(f, d);
		}
		
		
		BioLines data = cachebio.get(f);
		Json spe = null;
		if (json.has("BIO_FILE_SPECIFIC")) {
			spe = new Json(PATHS.TEXT().getFolder("race").getFolder("bio").getFolder("specific").get(json.value("BIO_FILE_SPECIFIC")));
			data = new BioLines(data, spe);
		}
		this.data = data;
		
		improve = new BioOpinion(
				new BioOpinionData(org, spe),
				race);
	}
	
	public LIST<BioLine> lines(){
		return data.descs;
	}
	
	public CharSequence opinionTitle(Humanoid indu) {
		return improve.title(indu, STANDINGS.get(indu.indu().clas()).current(indu.indu()));
	}
	
	public void opinions(LIST<Str> res, Humanoid indu) {
		improve.get(res, indu);
	}
	
	public CharSequence opinion(Humanoid indu) {
		improve.get(tmp, indu);
		return tmp.get(0);
	}
	
	public CharSequence houseProblem(Humanoid a) {
		for (BioLine d : data.houseP) {
			CharSequence s = d.get(a);
			if (s != null) {
				return s;
			}
		}
		return null;
	}
	
}
