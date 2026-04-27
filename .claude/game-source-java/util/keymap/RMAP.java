package util.keymap;

import java.util.Set;

import game.GAME;
import snake2d.LOG;
import snake2d.util.file.Json;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.COLLECTION;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;

public class RMAP<T extends MAPPED> implements COLLECTION<T>{
	
	public static final String WILDCARD = "*";
	public final String key;
	protected final KeyMap<T> map; 
	private final LIST<T> all;
	
	public RMAP(String key, LIST<T> all) {
		this.key = key;
		this.map = new KeyMap<T>();
		for (T t : all)
			map.put(t.key(), t);
		this.all = all;
		map.expand();
	}
	
	public T read(Json reader){
		return read(this.key(), reader);
	}
	
	public T read(String key, Json reader){
		
		String value = reader.value(key);
		T t = tryGet(value);
		if (t != null) {
			return t;
		}
		String k = "   Available: ";
		for (String s : available())
			k += s + ", ";
		reader.error("no " + this.key() + " named: " + value + k, key);
		return null;
	}
	
	public T readTry(String key, Json reader){
		if (reader.has(key)) {

			String value = reader.value(key);
			T t = tryGet(value);
			if (t != null) {
				return t;
			}
			String k = "   Available: ";
			for (String s : available())
				k += s + ", ";
			GAME.WarnLight(reader.errorGet("no " + this.key() + " named: " + value + k, key));
			return null;
		}
		return null;
	}
	
	public T readTry(Json reader){
		return readTry(key(), reader);
	}
	
	public T get(String key, Json error) {
		T t = tryGet(key);
		if (t != null) {
			return t;
		}
		String k = "   Available: ";
		for (String s : available())
			k += s + ", ";
		if (key.endsWith(" ")) {
			
		}
		if (error == null)
			throw new RuntimeException("no " + this.key() + " named: " + key  + (key.endsWith(" ") ?  "It ends with space!" : "") + k);
		error.error("no " + this.key() + " named: " + key  + (key.endsWith(" ") ?  "It ends with space!" : "") + k, key);
		return null;
	}
	
	public LIST<T> get(String s) {
		
		ArrayListGrower<T> res = new ArrayListGrower<>();
		
		if (s.indexOf(WILDCARD) >= 0) {
			String beg = s.substring(0, s.indexOf(WILDCARD));
			for (String k : map.keys()) {
				if (k.startsWith(beg)) {
					T t = map.get(k);
					if (!res.contains(t))
						res.add(t);
				}
			}
		}else {
			T t = tryGet(s);
			if (t != null) {
				res.add(t);
			}
		}
		
		return res;
	}
	
	public T getWarn(String key, Json reader){
		
		T t = tryGet(key);
		if (t != null) {
			return t;
		}
		String k = "   Available: ";
		for (String s : available())
			k += s + ", ";
		GAME.WarnLight(reader.errorGet("no " + this.key() + " named: " + key + k, key));
		return null;
	}
	
	public LIST<T> readMany(String key, Json reader){

		if (!reader.has(key))
			return new ArrayList<T>();
		
		String[] values = reader.values(key);
		ArrayListGrower<T> res = new ArrayListGrower<>();
		for (String s : values) {
			if (s.indexOf(WILDCARD) >= 0) {
				String beg = s.substring(0, s.indexOf(WILDCARD));
				for (String k : map.keys()) {
					if (k.startsWith(beg)) {
						T t = map.get(k);
						if (!res.contains(t))
							res.add(t);
					}
				}
			}else {
				T t = tryGet(s);
				if (t != null) {
					res.add(t);
				}else {
					String k = "   Available: ";
					for (String ss : available())
						k += ss + ", ";
					reader.error("no " + this.key() + " named: " + s + k, key);
				}
			}
		}
		
		return res;
		
	}
	
	public LIST<T> readMany(Json reader){

		return readMany(this.key()+"S", reader);
		
	}
	
	public LIST<T> readManyWarn(Json reader){

		return readManyWarn(this.key()+"S", reader);
		
	}
	
	public LIST<T> readManyWarn(String value, Json reader){
		
		if (!reader.has(value))
			return new ArrayList<T>();
		
		String[] values = reader.values(value);
		for (String v : values) {
			if (v.equals("*")) {
				return new ArrayList<T>(all());
			}
		}
		
		ArrayList<T> res = new ArrayList<>(values.length);
		for (String v : values) {
			LIST<T> t = tryGetMany(v);
			if (t.size() != 0) {
				res.add(t);
			}else {
				String k = "   Available: ";
				for (String s : available())
					k += s + ", \n";
				GAME.WarnLight(reader.errorGet("no " + this.key() + " named: " + v + k, v));
			}
		}
		return res;
		
	}
	
	public void readFill(double[] res, Json j, double min, double max){
		readFill(key(), res, j, min, max);
	}
	
	public void readFill(String key, double[] res, Json j, double min, double max){
		new KJson(key, j) {
			
			@Override
			protected void process(T s, Json j, String key, boolean isWeak) {
				res[s.index()] = j.d(key, min, max);
			}
		};
		
	}

	public double[] readFill(Json j, double max){
		double[] res = new double[all().size()];
		readFill(res, j, max);
		return res;
		
	}
	
	public void readFill(double[] res, Json j, double max){
		readFill(res, j, 0, max);
	}
	
	public boolean[] readIs(Json j){
		boolean[] res = new boolean[all().size()];
		for (T t : readMany(j)) {
			res[t.index()] = true;
		}
		return res; 
	}
	
	public LIST<T> all(){
		return all;
	}


	
	@Override
	public final T getAt(int index) {
		return all().get(index);
	}
	
	public T tryGet(String value) {
		if (map.containsKey(value)) {
			return map.get(value);
		}
		return null;
	}
	
	public LIST<T> tryGetMany(String s){

		
		ArrayListGrower<T> res = new ArrayListGrower<>();
		if (s.indexOf(WILDCARD) >= 0) {
			String beg = s.substring(0, s.indexOf(WILDCARD));
			for (String k : map.keys()) {
				if (k.startsWith(beg)) {
					T t = map.get(k);
					if (!res.contains(t))
						res.add(t);
				}
			}
		}else {
			T t = tryGet(s);
			if (t != null) {
				res.add(t);
			}
		}
		
		return res;
		
	}
	
	public String key() {
		return key;
	}
	
	public Set<String> available() {
		return map.keys();
	}
	
	private boolean hasErr = false;
	
	public abstract class KJson extends MAPJson<T>{
		
		public KJson(Json json) {
			this(key, json);
		}
		
		public KJson(String key, Json json) {
			super(key, json, map, RMAP.this.hasErr);
			RMAP.this.hasErr = hasErr;
		}

		
	}
	
	public static abstract class MAPJson<T> {

		public boolean hasErr;
		
		public MAPJson(String key, Json json, KeyMap<T> map, boolean hasErr) {
			
			if (json.has(key)) {
				json = json.json(key);
				for (String s : json.keys()) {
					
					if (s.indexOf(WILDCARD) >= 0) {
						String beg = s.substring(0, s.indexOf(WILDCARD));
						for (String k : map.keys()) {
							if (k.startsWith(beg)) {
								process(map.get(k), json, s, true);
							}
						}
					}else if (map.containsKey(s)) {
						process(map.get(s), json, s, false);
					}else{
						String p = "No " + key + " named " + s + " " + json.path() + " line: " + json.line(s);
						if (!hasErr) {
							p += System.lineSeparator() + "Available:" + System.lineSeparator();
							p += map.keysString();
							GAME.Warn(p);
							hasErr = true;
						}else {
							LOG.ln(p);
						}
					}
					
					
				}
				
			}
			this.hasErr = hasErr;
		}
		
		protected abstract void process(T s, Json j, String key, boolean isWeak);
		
	}
	
	

}
