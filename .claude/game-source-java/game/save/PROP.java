package game.save;

import java.io.IOException;

import init.paths.PATHS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.Json;
import snake2d.util.file.JsonE;
import snake2d.util.sets.KeyMap;

public final class PROP extends Savable{

	private static PROP s;
	private boolean dirty;
	private final KeyMap<String> profileMap = new KeyMap<String>();
	private final KeyMap<String> gameMap = new KeyMap<String>();
	private static final String fn = "Properties";
	
	PROP(GameSaver s) {
		super("PROP");
		PROP.s = this; 
		read();
	}
	
	public static PropGame game(String prefix) {
		return new PropGame(prefix);
	}
	
	public static String prop(String key) {
		if (s.profileMap.containsKey(key))
			return s.profileMap.get(key);
		return null;
	}
	
	public static void propSet(String key, String value) {
		if (value.equals(prop(key)))
			return;
		s.dirty = true;
		s.profileMap.putReplace(key, value);
	}
	
	public static int propI(String key, int fallback) {
		String kk = prop(key);
		if (kk == null)
			return fallback;
		try {
			int r = Integer.parseInt(kk);
			return r;
		}catch(NumberFormatException e) {
			return fallback;
		}
	}
	
	public static void propISet(String key, int i) {
		propSet(key, ""+i);
	}
	
	private static void read() {
		try {
			s.profileMap.clear();
			Json json = new Json(PATHS.local().PROFILE.get(fn));
			for (String ss : json.keys()) {
				s.profileMap.put(ss, json.value(ss));
				
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
			s.profileMap.clear();
			try {
				if (!PATHS.local().PROFILE.exists(fn))
					PATHS.local().PROFILE.create(fn);
			}catch (Exception ee) {
				ee.printStackTrace();
			}
		}
	}
	
	private static void flush() {
		if (!s.dirty)
			return;
		s.dirty = false;
		try {
			JsonE j = new JsonE();
			for (String ss : s.profileMap.keysSorted()) {
				j.add(ss, s.profileMap.get(ss));
				
			}
			if (!PATHS.local().PROFILE.exists(fn))
				PATHS.local().PROFILE.create(fn);
			j.save(PATHS.local().PROFILE.get(fn));
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void save(FilePutter file) {
		file.i(gameMap.size());
		for (String s : gameMap.keys()) {
			file.chars(s);
			file.chars(gameMap.get(s));
		}
		flush();
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		gameMap.clear();
		int am = file.i();
		for (int i = 0; i < am; i++) {
			String k = file.chars();
			String v = file.chars();
			gameMap.put(k, v);
		}
		read();
	}
	
	public static final class PropGame {
		
		private final String prefix;
		
		PropGame(String prefix) {
			this.prefix = prefix;
		}
		
		public int i(String key, int fallback) {
			String kk = chars(prefix + "_" + key);
			if (kk == null)
				return fallback;
			try {
				int r = Integer.parseInt(kk);
				return r;
			}catch(NumberFormatException e) {
				return fallback;
			}
		}
		
		public void setI(String key, int i) {
			charsSet(prefix + "_" + key, ""+i);
		}
		
		public String chars(String key) {
			if (s.gameMap.containsKey(key))
				return s.gameMap.get(key);
			return null;
		}
		
		public void charsSet(String key, String value) {
			s.gameMap.putReplace(key, value);
		}
	}
	
}
