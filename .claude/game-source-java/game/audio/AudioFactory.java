package game.audio;

import java.nio.file.Path;

import init.paths.PATH;
import init.paths.PathParser;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;

public abstract class AudioFactory<T>  {

	private final KeyMap<T> map = new KeyMap<T>();
	protected final LinkedList<T> all = new LinkedList<>();
	private final PATH path;

	private final T DUMMY;
	private final ArrayList<T> LDUMMY;
	private final String key;
	private final String split;
	protected AudioFactory(String key, PATH path, T DUMMY){
		this.path = path;
		LDUMMY = new ArrayList<T>(DUMMY);
		this.key = key;
		
		
		String pp = (""+path.get().toAbsolutePath());
		
		split = ""+pp.subSequence(pp.lastIndexOf("audio"), pp.length());
		this.DUMMY = DUMMY;
	}
	
	public LIST<T> create(String[] paths, Json json, String jsonKey) {
		
		LinkedList<T> res = new LinkedList<>();
		
		for (String relPath : paths) {
			if (relPath.equals("DUMMY"))
				res.add(LDUMMY);
			else {
				LIST<Path> pps = PathParser.getMany(path, relPath, json, jsonKey);
				if (pps == null || pps.size() == 0) {
					res.add(LDUMMY);
				}else {
					for (Path p : pps) {
						String pn = (""+p.toAbsolutePath());
						String kk = ""+pn.subSequence(pn.lastIndexOf(split) + split.length()+1, pn.length());
						if (!map.containsKey(kk)) {
							T e = create(all, p, kk);
							all.add(e);
							map.put(kk, e);
						}
						res.add(map.get(kk));
					}
				}
			}
			
		}
		
		return new ArrayList<T>(res);
		
	}
	
	
	
	public LIST<T> read(Json json) {
		return read(key, json);
	}
	
	public LIST<T> read(String key, Json json) {
		if (!json.has(key))
			return LDUMMY;
		if (json.arrayIs(key))
			return create(json.values(key), json, key);
		return create(new String[] {json.value(key)}, json, key);		
	}
	
	public LIST<T> all(){
		return all;
	}
	
	public KeyMap<T> map(){
		return map;
	}
	
	public final T DUMMY() {
		return DUMMY;
	}
	
	public LIST<T> LDUMMY(){
		return LDUMMY;
	}
	
	
	protected abstract T create(LinkedList<T> all, Path p, String key);
	
}
