package view.sett;

import snake2d.LOG;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.KeyMap;

public final class UISettMap {

	private static final KeyMap<RENDEROBJ> map = new KeyMap<>();
	
	private UISettMap() {
		
	}
	
	public static void clear() {
		map.clear();
	}
	
	public static void add(RENDEROBJ o, String key) {
		map.put(key, o);
	}
	
	public static RENDEROBJ getByKey(String key) {
		if (!map.containsKey(key)) {
			for (String s : map.keysSorted())
				LOG.ln(s);
		}
		return map.get(key);
	}
	
}
