package init.structure;

import java.util.Locale;

import init.INIT;
import init.INIT.InitResource;
import init.paths.PATHS;
import init.paths.PATHS.ResFolder;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import util.keymap.RMAPS;

public class STRUCTURES extends InitResource{

	private static final String KEY = "STRUCTURE";
	private static Structure MUD;
	private static RMAPS<Structure> map;
	
	public STRUCTURES(INIT init) {
		super(init);
		
		ResFolder f = path();
		
		

		LinkedList<String> keys = new LinkedList<>();
		keys.add("_MUD");
		keys.add(f.init.getFiles());
		
		ArrayList<Structure> all = new ArrayList<>(keys.size());
		for (String key : keys) {
			Json d = new Json(f.init.get(key));
			Json t = new Json(f.text.get(key));
			new Structure(key, all, d, t);
		}
		map = new RMAPS<Structure>(KEY, all);
		MUD = all.get(0);
	}
	
	public static LIST<Structure> all(){
		return map.all();
	}
	
	public static RMAPS<Structure> map(){
		return map;
	}
	
	public static Structure mud(){
		return MUD;
	}
	
	public static ResFolder path() {
		String f = KEY.toLowerCase(Locale.ROOT);
		return PATHS.SETT().folder(f);
	}
	
	
}
