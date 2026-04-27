package game.event.engine;

import java.io.IOException;

import game.GAME;
import game.event.actions.EventActions;
import init.paths.PATHS.ResFolder;
import snake2d.Errors;
import snake2d.LOG;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;

public class EventCollection {

	private final KeyMap<Event> map = new KeyMap<Event>();
	public final ArrayListGrower<Event> all = new ArrayListGrower<>();
	
	public EventCollection(ResFolder fo) throws IOException {
		
		if (GAME.EVENT() != null)
			throw new Errors.DataError("This must be done before events are setup");
		
		
		KeyMap<Integer> occMap = new KeyMap<Integer>();
		for (String file : fo.init.getFiles()) {
			
			Json jfile = new Json(fo.init.get(file));
			Json jtext = fo.text.exists(file) ? new Json(fo.text.get(file)) : null;
			for (String pkey : jfile.keys()) {
				String key = file + "_" + pkey;

				Json text = jtext != null && jtext.has(pkey) ? jtext.json(pkey) : null;
				Json d =  jfile.json(pkey);
				map.put(key, new Event(all, key, d, text));
				
				if (d.has("OCCURENCE")) {
					d = d.json("OCCURENCE");
					if (d.has("TYPE")) {
						int i = 0;
						String t = d.value("TYPE");
						if (occMap.containsKey(t)) {
							i += occMap.get(t);
						}
						occMap.putReplace(t, i);
					}
				}
				
				
			}
		}
		
		
		
		EventActions actions = new EventActions(this);
		
		
		
		for (String file : fo.init.getFiles()) {
			
			Json jfile = new Json(fo.init.get(file));
			Json jtext = fo.text.exists(file) ? new Json(fo.text.get(file)) : null;
			for (String pkey : jfile.keys()) {
				String key = file + "_" + pkey;
				Json d =  jfile.json(pkey);
				Json text = jtext != null && jtext.has(pkey) ? jtext.json(pkey) : null;
				map.get(key).read(d, text, actions, this);
				if (d.has("OCCURENCE")) {
					d = d.json("OCCURENCE");
					if (d.has("TYPE")) {
						int i = occMap.get(d.value("TYPE"));
						for (int di = 0; di < map.get(key).occurence.coccurence.length; di++) {
							map.get(key).occurence.coccurence[di] /= i;
						}
						
					}
				}
				
			}
		}
		actions.init();
		
	}
	
	private boolean hasError = false;
	
	public Event read(Event parent, String k, Json error, String kk) {
		
		Event e = map.get(k);
		if (e == null) {
			String f = parent.key.split("_")[0];
			e = map.get(f + "_" + k);
		}
		if (e == null) {
			
			String ee = error.errorGet("no event named: " + k  + (k.endsWith(" ") ?  "It ends with space!" : "") + k, kk);
			
			if (!hasError) {
				String av = "   Available: " + System.lineSeparator();
				av += map.keysString();
				ee += System.lineSeparator() + av;
				hasError = true;
			}
			
			LOG.err(ee);
			return null;
			
			
		}
		return e;
	}
	
	
	
}
