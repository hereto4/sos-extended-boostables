package view.keyboard;

import init.paths.PATHS;
import snake2d.CORE;
import snake2d.KeyBoard.KEYACTION;
import snake2d.KeyBoard.KeyEvent;
import snake2d.util.file.Json;
import snake2d.util.file.JsonE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import view.main.VIEW;

public class KEYS implements KeyPoller{

	static final KeyNames names = new KeyNames();
	private static KEYS self;
	
	private final KeyPageMain MAIN;
	private final KeyPageSett SETT;
	private final KeyPageWorld WORLD;
	private final KeyPageBattle BATTLE;
	private final IChange iii = new IChange();
	Key toChange;
	
	private final LIST<KeyPage> views;
	private boolean anyDown;
	
	private KEYS() {
		self = this;

		MAIN = new KeyPageMain();
		SETT = new KeyPageSett();
		WORLD = new KeyPageWorld();
		BATTLE = new KeyPageBattle();
		views = new ArrayList<>(
			MAIN,SETT,WORLD,BATTLE
		);
	}
	
	public static LIST<KeyPage> pages(){
		return self.views;
	}
	
	public static KeyPageMain MAIN() {
		return self.MAIN;
	}
	
	public static KeyPageSett SETT() {
		return self.SETT;
	}
	
	public static KeyPageWorld WORLD() {
		return self.WORLD;
	}
	
	public static KeyPageBattle BATTLE() {
		return self.BATTLE;
	}
	
	public static KEYS get() {
		return self;
	}
	
	public static KeyPoller init() {
		self = new KEYS();
		return self;
	}
	
	@Override
	public void poll(LIST<KeyEvent> keys) {
		

		
		KeyPage other = MAIN;
		if (VIEW.s().isActive())
			other = SETT;
		else if(VIEW.world().isActive())
			other = WORLD;
		else if(VIEW.s().battle.isActive() || VIEW.b().isActive())
			other = BATTLE;
	
		for (KeyPage m : self.views) {
			if (m == MAIN || m == other) {
				for (Key k : m.all) {
					if (k.action != null && k.consumeClick()) {
						k.action.exe();
					}
				}
			}
			for (Key k : m.all) {
				k.isDown = false;
				k.pressed = false;
			}
		}
		self.anyDown = false;
		
		int mod = -1;
		
		for (Key k: MAIN.all) {
			k.isDown = false;
			k.pressed = false;
			if (mod == -1 && k.modCode() != -1 && CORE.getInput().getKeyboard().isPressed(k.modCode())) {
				mod = k.modCode();
				anyDown = true;
			}
		}
		if (other != null) {
			for (Key k: other.all) {
				k.isDown = false;
				k.pressed = false;
				if (mod == -1 && k.modCode() != -1 && CORE.getInput().getKeyboard().isPressed(k.modCode())) {
					mod = k.modCode();
					anyDown = true;
				}
			}
		}

		for (KeyEvent e : keys) {
			if (e.action() == KEYACTION.PRESS) {
				Key k = check(mod, e.code(), other);
				if (k != null) {
					k.pressed = true;
					anyDown = true;
				}
			}
		}

		for (Key k: MAIN.all) {
			if (k.keyCode() != -1 && (k.modCode() == mod || (k.modCode() == -1 && k.keyCode() == mod)) && CORE.getInput().getKeyboard().isPressed(k.keyCode())) {
				k.isDown = true;
				anyDown = true;
			}
		}
		if (other != null) {
			for (Key k: other.all) {
				if (k.keyCode() != -1 && (k.modCode() == mod || (k.modCode() == -1 && k.keyCode() == mod)) && CORE.getInput().getKeyboard().isPressed(k.keyCode())) {
					k.isDown = true;
					anyDown = true;
				}
			}
		}

		
		if (toChange != null && MAIN.ASSIGN_HOTKEY.isDown) {
			iii.show(toChange);
			
			clear();
		}
		
	}
	
	private Key check(int mod, int code, KeyPage other) {
		if (mod != -1) {
			int c = Key.hash(mod, code);
			if (MAIN.map.contains(c)) {
				return MAIN.map.get(c);
			}else if (other != null && other.map.contains(c)) {
				return other.map.get(c);
			}
		}
		
		if (MAIN.map.contains(code)) {
			return MAIN.map.get(code);
		}else if (other != null && other.map.contains(code)) {
			return other.map.get(code);
		}
		return null;
	}
	
	public void restore() {
		for (KeyPage m : views) {
			for (Key k : m.all) {
				k.reset();
			}
		}
	}
	
	public static boolean anyDown() {
		return self.anyDown;
	}
	
	public static void bind(Key key) {
		clear();
		self.iii.show(key);
	}
	
	public static void clear() {
		for (KeyPage m : self.views) {
			for (Key k : m.all) {
				k.isDown = false;
				k.pressed = false;
			}
		}
		self.anyDown = false;
		self.toChange = null;
	}
	
	public void readSettings() {

		restore();
		if (!PATHS.local().SETTINGS.exists("Keyboard"))
			return;
		
		try {
			Json json = new Json(PATHS.local().SETTINGS.get("Keyboard"));
			
			
			for (int ii = 0; ii < views.size(); ii++) {
				KeyPage m = views.get(ii);
				
				if (json.has(m.key)) {
					Json j = json.json(m.key);
					for (int ki = 0; ki < m.all.size(); ki++) {
						Key k = m.all.get(ki);
						k.read(j);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace(System.out);
			restore();
			save();
		}
		
	}

	public void save() {

		
		JsonE json = new JsonE();
		for (KeyPage m : views) {
			JsonE j = new JsonE();
			
			for (Key k : m.all) {
				k.save(j);
			}
			
			
//			try {
//				Json r = read != null && read.has(m.key) ? read.json(m.key) : null;
//				if (r != null) {
//					for (String k : r.keys()) {
//						if (!j.has(k)) {
//							int code = r.i(k);
//							if (m.map.get(code) == null || m.map.get(code) != r) {
//								
//							}
//							
//							j.add(k, r.i(k));
//						}
//					}
//				}
//			} catch (Exception e) {
//				
//			}
			
			json.add(m.key, j);
		}
		
		if (!PATHS.local().SETTINGS.exists("Keyboard"))
			PATHS.local().SETTINGS.create("Keyboard");
		json.save(PATHS.local().SETTINGS.get("Keyboard"));

	}
	
}
