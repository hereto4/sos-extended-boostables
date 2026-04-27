package init.value;

import init.sprite.UI.UI;
import snake2d.LOG;
import snake2d.util.file.Json;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.data.BOOLEANO;
import util.data.DOUBLE_O;
import util.data.INT_O;

public class GValueCat<T> {

	private final KeyMap<Value<T>> map = new KeyMap<Value<T>>();
	private LIST<Value<T>> all;
	LinkedList<ACTION> inits = new LinkedList<>();
	boolean hasSpewed = false;
	public final String key;
	public Locks LOCK = new Locks();
	
	GValueCat (String key) {
		this.key = key;
		
		
	}
	
	void clear() {
		map.clear();
		inits.clear();
		hasSpewed = false;
		LOCK.clear();
	}
	
	public KeyMap<Value<T>> map(){
		return map;
	}
	
	void init() {
		for (ACTION a : inits)
			a.exe();
		inits.clear();
		LOCK.init();
		all = map.allSorted();
	}
	
	public LIST<Value<T>> all(){
		return all;
	}
	
	public void push(Value<T> value) {
		
		if (map.containsKey(value.key))
			throw new RuntimeException("Another value has the same key: " + value.key);
		map.put(value.key, value);
		
	}
	
	public void push(String key, CharSequence name, SPRITE icon, DOUBLE_O<T> value, boolean isPercentage) {
		
		Value<T> v = new Value<T>(key, icon, name, value, isPercentage, false);
		push(v);
		
	}
	
	public void push(String key, CharSequence name, SPRITE icon, DOUBLE_O<T> value, boolean isPercentage, boolean isBool) {
		
		Value<T> v = new Value<T>(key, icon, name, value, isPercentage, isBool);
		push(v);
		
	}
	
	public void push(String key, CharSequence name, SPRITE icon, BOOLEANO<T> value) {
		
		DOUBLE_O<T> v = new DOUBLE_O<T>() {

			@Override
			public double getD(T t) {
				return value.is(t) ? 1 :0;
			}
		};
		
		push(key, name, icon, v, false, true);
	}
	
	public void push(String key, CharSequence name, SPRITE icon, DOUBLE_O<T> value) {
		push(key, name, icon, value, true); 
	}
	
	public void pushI(String key, CharSequence name, SPRITE icon, INT_O<T> value) {
		DOUBLE_O<T> v = new DOUBLE_O<T>() {

			@Override
			public double getD(T t) {
				return value.get(t);
			}
		};
		
		push(key, name, icon, v, false);
	}
	
	public Value<T> get(String key) {
		return map.get(key);
	}
	
	
	private boolean eee = false;
	public LIST<Value<T>> get(String key, Json error) {
		ArrayListGrower<Value<T>> res = new ArrayListGrower<>();
		if (key.indexOf('*') > 0) {
			String s = key.substring(0, key.indexOf('*'));
			for (Value<T> v : all) {
				if (v.key.startsWith(s))
					res.add(v);
			}
		}else {
			Value<T> v = map.get(key);
			if (v == null) {
				String e = error.errorGet("No " + this.key + " named: " + key, key);
				if (!eee) {
					eee = true;
					e += " Available:";
					e += System.lineSeparator();
					e += map.keysString();
				}
				LOG.err(e);
			}else {
				res.add(v);
			}
		}
		return res;
	}
	
	public String available() {
		return map.keysString();
	}
	
	public final class Locks {
		
		final KeyMap<Lockable<T>> map = new KeyMap<>();
		LinkedList<ACTION> inits = new LinkedList<>();
		boolean hasSpewed = false;
		public final Lockable<T> empty = new Lockable<>("", "", "", UI.icons().s.DUMMY, GValueCat.this);
		private Locks() {
			
		}
		
		public void init() {
			for (ACTION a : inits)
				a.exe();
			inits.clear();
		}

		void clear() {
			map.clear();
			inits.clear();
			hasSpewed = false;
		}
		
		public Lockable<T> get(String key) {
			return map.get(key);
		}
		
		public String available() {
			return map.keysString();
		}
		
		public Lockable<T> push(String key, CharSequence name, CharSequence desc, SPRITE icon){
			key = key.replace("__", "_");
			Lockable<T> t = new Lockable<T>(key, name, desc, icon, GValueCat.this);
			map.put(key, t);
			return t;
		}
		
		public Lockable<T> push(){
			Lockable<T> t = new Lockable<T>("", "", "", UI.icons().s.DUMMY, GValueCat.this);
			return t;
		}
		
	}
	
	public abstract class LockJson {

		final Json json;
		
		public LockJson(String key, Json j){
			this.json = j.json(key);
			inits.add(new ACTION() {
				
				@Override
				public void exe() {
					for (String keyComp : json.keys()) {
						COMPARATOR comp = COMPARATOR.map.get(keyComp, json);
						if (comp != null) {
							Json j = json.json(keyComp);
							for (String k : j.keys()) {
								Value<T> v = get(k);
								if (v == null)
									continue;
								callback(comp, v, k, j);
							}
						}	
					}
				}
			});
		}
		
		public abstract void callback(COMPARATOR comp, Value<T> value,  String key, Json json);
		
	}
	
}
