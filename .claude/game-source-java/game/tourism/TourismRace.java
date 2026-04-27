package game.tourism;

import init.paths.PATHS;
import init.race.Race;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomBlueprintIns;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;

public class TourismRace {

	
	private final static KeyMap<Text> cache = new KeyMap<>();
	private final static Text DUMMY = new Text(null);
	
	public final double occurence;
	public final double credits;
	final Text data;
	public final LIST<RoomBlueprintIns<?>> attractions;
	
	
	public TourismRace(Json json, Race race) {

		if (json.has("TOURIST")) {
			json = json.json("TOURIST");
			occurence = json.d("OCCURENCE", 0, 100000);
			credits = json.d("CREDITS", 0, 100000);
			String d = json.value("TOURIST_TEXT_FILE");
			if (!cache.containsKey(d)) {
				cache.put(d, new Text(new Json(PATHS.TEXT().getFolder("race").getFolder("tourist").get(d))));
			}
			data = cache.get(d);
		}else {
			data = DUMMY;
			occurence = 0;
			credits = 0;
		}
		
		double high = -1;
		for (RoomBlueprintImp b : SETT.ROOMS().bonus.all) {
			if (b.employment() != null && race.pref().getWork(b.employment()) > high) {
				high = Math.max(high, race.pref().getWork(b.employment()));
			}
		}
		
		int am = 0;
		for (RoomBlueprintImp b : SETT.ROOMS().bonus.all) {
			if (b.employment() != null) {
				double m = Math.abs(race.pref().getWork(b.employment())-high);
				if (m < 0.1)
					am++;
			}
		}
		
		ArrayList<RoomBlueprintIns<?>> res = new ArrayList<>(am);
		for (RoomBlueprintImp b : SETT.ROOMS().bonus.all) {
			if (b.employment() != null && b instanceof RoomBlueprintIns) {
				double m = Math.abs(race.pref().getWork(b.employment())-high);
				if (m < 0.1)
					res.add((RoomBlueprintIns<?>) b);
			}
		}
		
		this.attractions = res;
		
	}
	
	public RoomBlueprintIns<?> getAttraction(long ran) {
		return attractions.getC((int)ran);
	}
	
}
