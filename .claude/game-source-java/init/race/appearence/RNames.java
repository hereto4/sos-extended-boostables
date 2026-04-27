package init.race.appearence;

import init.paths.PATHS;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;

public final class RNames {

	public final LIST<String> firstNames;
	public final LIST<String> lastNames;
//	public final LIST<String> firstNamesNoble;
//	public final LIST<String> lastNamesNoble;
	
	RNames(Json json, KeyMap<String[]> names){
		firstNames = names("NAMESET_FILE_FIRST", json, names);
		lastNames = names("NAMESET_FILE_SURNAME", json, names);
//		firstNamesNoble = names("NAMESET_FILE_FIRST_NOBLE", json, names);
//		lastNamesNoble = names("NAMESET_FILE_SURNAME_NOBLE", json, names);
	}
	
	static ArrayList<String> names(String key, Json json, KeyMap<String[]> names){
		String v = json.value(key);
		if (names.containsKey(v))
			return new ArrayList<>(names.get(v));
		Json d = new Json(PATHS.TEXT().getFolder("names").getFolder("nameset").get(v));
		String[] mm = d.texts("NAMES", 1, 255);
		names.put(v, mm);
		return new ArrayList<>(mm);
		
	}
	
}
