package view.keyboard;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.misc.ACTION;
import util.text.D;

public final class KeyButt{

	private static CharSequence ¤¤assign = "¤To assign a new hotkey to this function, press: ";

	
	static {
		D.ts(KeyButt.class);
	}
	private KeyButt(){

	}

	public static CLICKABLE wrap(CLICKABLE base, Key key) {
		return new CLICKABLE.ClickWrap(base) {
			
			@Override
			public void hoverInfoGet(GUI_BOX text) {
				text.title(key.name);
				text.text(key.desc);
				text.NL(4);
				super.hoverInfoGet(text);
				text.NL(8);
				key.setMapping(text);
				text.NL(2);
				if (key.rebindable) {
					text.text(¤¤assign);
					text.text(KEYS.MAIN().ASSIGN_HOTKEY.repr());
				}
				text.NL(8);
			}
			
			@Override
			public boolean hover(COORDINATE mCoo) {
				
				if (super.hover(mCoo)) {
					if (key.rebindable)
						KEYS.get().toChange = key;
					return true;
				}
				return false;
			}

			@Override
			protected RENDEROBJ pget() {
				return base;
			}
			
		};
	}
	
	public static CLICKABLE wrap(ACTION a, CLICKABLE base, KeyPage page, String code, CharSequence name, CharSequence desc) {
		return wrap(a, base, page, code, name, desc, -1, -1);
	}
	
	public static CLICKABLE wrap(ACTION a, CLICKABLE base, KeyPage page, String code, CharSequence name, CharSequence desc, int mod, int key) {
		for (Key k : page.all) {
			if (k.key.equals(code)) {
				k.action = a;
				return wrap(base, k);
			}
		}
		
		Key k = new Key(code, name, desc, page, mod, key, true);
		k.action = a;
		return wrap(base, k);
	}
	
	public static void hover(Key key, GUI_BOX text) {
		text.title(key.name);
		text.text(key.desc);
		text.NL(8);
		key.setMapping(text);
		text.NL(2);
		if (key.rebindable) {
			text.text(¤¤assign);
			text.text(KEYS.MAIN().ASSIGN_HOTKEY.repr());
		}
		text.NL(8);
	}
	
	public static Key key(ACTION a, KeyPage page, String code, CharSequence name, CharSequence desc, int mod, int key) {
		Key k = new Key(code, name, desc, page, mod, key, true);
		return k;
	}


}
