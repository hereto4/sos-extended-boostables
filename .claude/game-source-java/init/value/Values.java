package init.value;

import game.GAME;
import snake2d.LOG;
import snake2d.util.file.Json;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;

public class Values<T> {

	private final ArrayListGrower<Value<T>> all = new ArrayListGrower<>();
	private final GValueCat<T> mommy;
	
	Values(GValueCat<T> mommy) {
		this.mommy = mommy;
	}
	
	public void push(String targetKey, Object path){
		mommy.inits.add(new Promise(targetKey, path.toString()));
	}

	public void push(Json json, String... notallowed){
		push(GVALUES.KEY, json, notallowed);
	}
	
	public void push(String key, Json json, String... notallowed){
		
		if (!json.has(key))
			return;

		String path = json.path() + ", line" + json.line(key);
		
		for (String k : json.values(key)) {
			push(k, path);
		}
	}
	
	public void pushJson(String key, Json json, String... notallowed){
		
		if (!json.has(key))
			return;

		String path = json.path() + ", line" + json.line(key);
		
		for (String k : json.values(key)) {
			push(k, path);
		}
	}
	
	public LIST<Value<T>> all(){
		return all;
	}

	private class Promise implements ACTION{
		
		public final String key;
		public final String path;
		
		Promise(String key, String path){
			this.key = key;
			this.path = path;
		}

		@Override
		public void exe() {
			if (mommy.get(key) == null) {
				if (!mommy.hasSpewed) {
					GAME.Warn(path + System.lineSeparator() + "no value named : " + key + " available: " + System.lineSeparator()+ mommy.available());
				}else {
					LOG.ln("no value: " + key + "path: " + path);
				}
				mommy.hasSpewed = true;
				
			}else {
				all.add(mommy.get(key));
			}
			
		}

	}
}
