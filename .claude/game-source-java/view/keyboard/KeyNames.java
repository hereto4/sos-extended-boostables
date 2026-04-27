package view.keyboard;

import snake2d.CORE;
import snake2d.KEYCODES;
import snake2d.LOG;
import snake2d.util.sprite.text.Str;
import util.text.D;

class KeyNames {

	private final CharSequence[] names;
	private static CharSequence ¤¤none = "---";
	private static CharSequence ¤¤unknown = "???";
	
	KeyNames(){
		D.t(this);
		
		names = new CharSequence[KEYCODES.lastCode()+1];
		
		for (int i : KEYCODES.all) {
			names[i] = CORE.getInput().getKeyboard().translate(i);
		}
		
		
		
		
		names[KEYCODES.KEY_SPACE] = D.g("space");
		names[KEYCODES.KEY_ESCAPE] = D.g("escape");
		names[KEYCODES.KEY_CAPS_LOCK] = D.g("caps-lock");
		names[KEYCODES.KEY_SCROLL_LOCK] = D.g("scroll-lock");
		names[KEYCODES.KEY_NUM_LOCK] = D.g("num-lock");
		names[KEYCODES.KEY_PAUSE] = D.g("pause");
		names[KEYCODES.KEY_ENTER] = D.g("enter");
		names[KEYCODES.KEY_TAB] = D.g("tab");
		names[KEYCODES.KEY_BACKSPACE] = D.g("backspace");
		names[KEYCODES.KEY_INSERT] = D.g("insert");
		names[KEYCODES.KEY_DELETE] = D.g("delete");
		names[KEYCODES.KEY_RIGHT] = D.g("right");
		names[KEYCODES.KEY_LEFT] = D.g("left");
		names[KEYCODES.KEY_DOWN] = D.g("down");
		names[KEYCODES.KEY_UP] = D.g("up");
		names[KEYCODES.KEY_PAGE_UP] = D.g("page-up");
		names[KEYCODES.KEY_PAGE_DOWN] = D.g("page-down");
		names[KEYCODES.KEY_HOME] = D.g("home");
		names[KEYCODES.KEY_END] = D.g("end");
		names[KEYCODES.KEY_PRINT_SCREEN] = D.g("print-screen");
		names[KEYCODES.KEY_LEFT_SHIFT] = D.g("left-shift");
		names[KEYCODES.KEY_LEFT_CONTROL] = D.g("left-ctrl");
		names[KEYCODES.KEY_RIGHT_SHIFT] = D.g("right-shift");
		names[KEYCODES.KEY_RIGHT_CONTROL] = D.g("right-ctrl");
		names[KEYCODES.KEY_KP_ENTER] = D.g("pad-enter");
		names[KEYCODES.KEY_KP_EQUAL] = D.g("pad-equals");
		names[KEYCODES.KEY_LEFT_ALT] = D.g("left-alt");
		names[KEYCODES.KEY_LEFT_SUPER] = D.g("left-super");
		names[KEYCODES.KEY_MENU] = D.g("menu");
		names[KEYCODES.KEY_RIGHT_ALT] = D.g("right-alt");
		names[KEYCODES.KEY_RIGHT_SUPER] = D.g("right-super");

		
		CharSequence ff = D.g("F", "F{0}");
		for (int i = KEYCODES.KEY_F1; i <= KEYCODES.KEY_F25; i++) {
			int k = (i-KEYCODES.KEY_F1+1);
			names[i] = "" + new Str(ff).insert(0, k);
		}
		
		for (int i : KEYCODES.all) {
			if (names[i] == null || names[i].length() == 0)
				LOG.ln(i);
		}
//		
//		
		
	
	}
	
	public CharSequence getCode(int code) {
		if (code < 0) {
			return ¤¤none;
		}
		if (code >= names.length || names[code] == null) {
			return ¤¤unknown;
		}
		return names[code];
	}

	
	
}
