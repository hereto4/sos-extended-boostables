package init.race.home;

import java.io.IOException;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.resources.RES_AMOUNT;
import init.sprite.game.SheetType;
import init.sprite.game.Sheets;
import settlement.room.home.HOME;
import snake2d.util.file.Json;
import snake2d.util.sets.LIST;

public final class RaceHomeSheet {

	private final int[][] needed;
	private Sheets[] ani;
	
	RaceHomeSheet() throws IOException{
		needed = new int[0][0];
		ani = new Sheets[0];
	}
	
	RaceHomeSheet(LIST<RES_AMOUNT> resources, Json json, String key, SheetType it) throws IOException{
		
		if (!json.has(key)) {
			needed = new int[0][0];
			ani = new Sheets[0];
		}else {
			Json[] jsons = json.jsons(key);
			needed = new int[jsons.length][resources.size()];
			ani = new Sheets[jsons.length];
			
			for (int i = 0; i < jsons.length; i++) {
				Json j = jsons[i];
				addResource(resources, j, i, needed);
				ani[i] = new Sheets(it, j);
			}
		}
	}
	
	static void addResource(LIST<RES_AMOUNT> resources, Json json, int i, int[][] needed) {
		Json j = json.json(RESOURCES.KEYS);
		for (String k : j.keys()) {
			RESOURCE res = RESOURCES.map().tryGet(k);
			if (res != null)
				for (int ri = 0; ri < resources.size(); ri++) {
					if (resources.get(ri).resource() == res) {
						needed[i][ri] = j.i(k, 0, 15);
					}
				}
		}
	}
	
	public Sheets get(HOME data) {
		outer:
			for (int ai = ani.length-1; ai >= 0; ai--) {
				int[] amounts = needed[ai];
				for (int i = 0; i < amounts.length; i++) {
					if (data.resourceAm(i) < amounts[i])
						continue outer;
				}
				return ani[ai];
			}
			return null;
	}
	
}
