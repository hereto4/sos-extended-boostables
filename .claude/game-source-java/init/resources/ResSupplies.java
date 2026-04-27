package init.resources;

import init.paths.PATH;
import init.paths.PATHS;
import init.race.Race;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import util.keymap.RMAP;

public final class ResSupplies {

	public final LIST<ResSupply> ALL;
	private final ResSupply[] look = new ResSupply[RESOURCES.ALL().size()];
	

	private final RMAP<ResSupply> map;
	
	ResSupplies() {
		
		ArrayListGrower<ResSupply> all = new ArrayListGrower<>();
		PATH p = PATHS.INIT().getFolder("resource").getFolder("supply");
		String[] keys = p.getFiles();
		
		for (String k : keys) {
			Json j = new Json(p.get(k));
			ResSupply s = new ResSupply(k, j, all);
			if (look[s.resource.index()] != null)
				j.error("Army supply: " + look[s.resource.index()].resource.key + " refers to the same resource: " + s.resource.key, k);
			look[s.resource.index()] = s;
		}
		map = new RMAP<>("ARMY_SUPPLY", all);
		this.ALL = all;
	}


	
	public ResSupply get(RESOURCE res) {
		return look[res.index()];
	}
	
	public RMAP<ResSupply> MAP(){
		return map;
	}
	
	public void setEfficiency(Race race, Json json) {
		map.new KJson("MILITARY_SUPPLY_USE", json) {
			
			@Override
			protected void process(ResSupply s, Json j, String key, boolean isWeak) {
				s.setRace(race, j.i(key, 0, 10000));
			}
		};
	}
	
}
