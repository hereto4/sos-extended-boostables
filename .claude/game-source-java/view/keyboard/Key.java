package view.keyboard;

import snake2d.KEYCODES;
import snake2d.util.file.Json;
import snake2d.util.file.JsonE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.INDEXED;
import snake2d.util.sprite.text.Str;
import util.gui.misc.GBox;
import util.text.D;

public final class Key implements INDEXED{


	private static Str stmp = new Str(64);

	private static CharSequence ¤¤none = "---";
	private static CharSequence ¤¤hotkey = "¤Hotkey: ";

	static {
		D.ts(Key.class);
	}
	private final int modCodeDef;
	private final int keyCodeDef;
	private int modCode = -1;
	private int keyCode = -1;
	private final KeyPage map;
	public final String key;
	public final CharSequence name;
	public final CharSequence desc;
	ACTION action;
	boolean isDown;
	boolean pressed;
	public final boolean rebindable;
	
	Key(String key, CharSequence name, CharSequence desc, KeyPage map){
		this(key, name, desc, map, -1,-1);
	}
	
	Key(String key, CharSequence name, CharSequence desc, KeyPage map, int defCode){
		this(key, name, desc, map, -1, defCode);
	}
	
	Key(String key, CharSequence name, CharSequence desc, KeyPage map, int defMod, int defCode){
		this(key, name, desc, map, defMod, defCode, true);
	}
	
	public KeyPage page() {
		return map;
	}
	
	Key(String key, CharSequence name, CharSequence desc, KeyPage map, int defMod, int defCode, boolean bindable){
		
		this.key = key;
		this.name = name;
		this.desc = desc;
		this.map = map;
		modCodeDef = defMod;
		keyCodeDef = defCode;
		assign(defMod, defCode);
		rebindable = bindable;
		for (Key k : map.all)
			if (k.key.equals(key))
				throw new RuntimeException("" + k.key);
		map.all.add(this);
		
	}
	
	void read(Json json) {
		if (json.has(key)) {
			int i = json.i(key);
			if (i == -1)
				assign(-1,-1);
			else {
				int mod = i / KEYCODES.lastCode();
				mod -= 1;
				int code = i % KEYCODES.lastCode();
				assign(mod, code);
			}
		}
	}
	
	void save(JsonE json) {
		int i = keyCode == -1 ? -1 : index();
		json.add(key, i);
	}
	
	public boolean assign(int mod, int key) {
		if (mod == modCode && key == keyCode)
			return true;
	
		if (key != -1) {
			if (map != KEYS.MAIN() && KEYS.MAIN() != null && KEYS.MAIN().map.contains(hash(mod, key))) {
				if (!KEYS.MAIN().map.get(hash(mod, key)).rebindable)
					return false;
				KEYS.MAIN().map.get(hash(mod, key)).assign(-1, -1);
			}
			if (map == KEYS.MAIN() && KEYS.pages() != null) {
				for (KeyPage p : KEYS.pages()) {
					if (p == KEYS.MAIN()) {
						continue;
					}
					if (p.map.contains(hash(mod, key)))
						p.map.get(hash(mod, key)).assign(-1, -1);
				}
			}
			
			if (map.map.contains(hash(mod, key))) {
				if (!map.map.get(hash(mod, key)).rebindable)
					return false;
				map.map.get(hash(mod, key)).assign(-1, -1);
			}
		}

		
		if (keyCode != -1)
			map.map.remove(index());		
		this.modCode = mod;
		this.keyCode = key;
		if (keyCode != -1)
			map.map.add(this);
		return true;
	}
	
	public void reset() {
		assign(modCodeDef, keyCodeDef);
	}
	
	public int modCode() {
		return modCode;
	}
	
	public int keyCode() {
		return keyCode;
	}
	
	public CharSequence repr() {
		if (keyCode == -1)
			return ¤¤none;
		stmp.clear();
		if (modCode != -1) {
			CharSequence code = KEYS.names.getCode(modCode);
			stmp.add(code);
			stmp.s().add('+').s();
		}
		CharSequence code = KEYS.names.getCode(keyCode);
		stmp.add(code);
		return stmp;
	}
	

	
	public void setMapping(GUI_BOX box) {
		GBox b = (GBox) box;
		b.textLL(¤¤hotkey);
		b.text(repr());
	}
	
	static int hash(int modCode, int keyCode) {
		if (keyCode == -1)
			throw new RuntimeException();
		if (modCode == -1)
			return keyCode;
		return (modCode+1)*KEYCODES.lastCode() + keyCode;
	}

	public boolean hasMapping() {
		return keyCode >= 0;
	}
	
	@Override
	public int index() {
		return hash(modCode, keyCode);
	}
	
	public boolean isPressed() {
		return isDown;
	}
	
	public boolean consumeClick() {
		if (pressed) {
			pressed = false;
			return true;
		}
		return false;
	}
	
	@Override
	public String toString() {
		return map.key + " " + key;
	}
	
}
