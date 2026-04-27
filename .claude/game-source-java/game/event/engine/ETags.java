package game.event.engine;

import snake2d.util.file.Json;
import snake2d.util.sets.KeyMap;

final class ETags {

	public final String[] adds;
	public final String[] removes;
	public final String[] allows;
	public final String[] allows_not;
	
	ETags(Json d){
		if (d.has("TAGS")) {
			d = d.json("TAGS");
			adds = read(d, "ADD");
			removes = read(d, "REMOVE");
			allows = read(d, "ALLOW");
			allows_not = read(d, "ALLOW_NOT");
			d.checkUnused();
		}else {
			adds = new String[0];
			removes = adds;
			allows = adds;
			allows_not = adds;
		}
	}
	
	private String[] read(Json d, String key) {
		if (d.has(key))
			return d.values(key);
		else
			return new String[0];
	}
	
	public boolean can(KeyMap<Boolean> tags) {
		for (String k : allows) {
			if (!tags.containsKey(k) || tags.get(k) == Boolean.FALSE)
				return false;
		}
		
		for (String k : allows_not) {
			if (tags.containsKey(k) && tags.get(k) == Boolean.TRUE)
				return false;
		}
		return true;
	}
	
}
