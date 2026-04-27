package view.keyboard;

import java.util.ArrayList;

import init.constant.C;
import init.sprite.UI.UI;
import snake2d.KEYCODES;
import snake2d.KeyBoard.KEYACTION;
import snake2d.KeyBoard.KeyEvent;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.LIST;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.text.D;
import view.interrupter.Interrupter;
import view.main.VIEW;

final class IChange extends Interrupter implements KeyPoller{

	private final GuiSection section = new GuiSection();
	private Key key;
	private int codeMod;
	private int codeKey;
	private int triedcodeMod;
	private int triedcodeKey;
	private final ArrayList<Key> errorKey = new ArrayList<>(64);
	private double timer = 0;
	
	private static CharSequence ¤¤Pick = "¤Pick a hotkey for:";
	private static CharSequence ¤¤Explanation = "¤Either press a single key, or press and hold a key to use as a modulator, then press another key. Hit escape to exit.";
	
	private static CharSequence ¤¤Sucess = "¤Hotkey {0} successfully mapped to {1}!";
	private static CharSequence ¤¤Fail = "¤Hotkey {0} is already in use by {1}, pick another one.";
	private static CharSequence ¤¤Overwritten = "¤{0} is now without a hotkey!";
	private final GText text = new GText(UI.FONT().M, 120);
	
	static {
		D.ts(IChange.class);
	}
	
	public IChange() {
		text.setMaxWidth(600);
	}
	
	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		section.hover(mCoo);
		return true;
	}

	@Override
	protected void mouseClick(MButt button) {
		section.click();
	}

	@Override
	protected void hoverTimer(GBox text) {
		section.hoverInfoGet(text);
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		
		int cx = C.DIM().cX();
		int y1 = C.DIM().cY()-200;
		
		text.setFont(UI.FONT().H1);
		text.lablify();
		text.set(¤¤Pick);
		text.renderC(r, cx, y1);
		y1+=text.height();
				
		text.setFont(UI.FONT().H2);
		text.lablifySub();
		text.set(key.name);
		text.renderC(r, cx, y1);
		y1+=text.height()+8;
		
		text.setFont(UI.FONT().M);
		text.normalify();
		text.set(key.desc);
		text.renderC(r, cx, y1);
		y1+=text.height()+16;
		
		if (timer > 0) {
			text.clear();
			text.setFont(UI.FONT().H2);
			text.normalify2();
			text.add(¤¤Sucess);
			text.insert(0, key.name);
			text.insert(1, key.repr());
			text.adjustWidth();
			text.renderC(r, cx, y1);
			int y = y1 +32;
			for (Key e : errorKey) {
				text.clear();
				text.setFont(UI.FONT().M);
				text.errorify();
				text.add(¤¤Overwritten);
				text.insert(0, e.name);
				text.adjustWidth();
				text.renderC(r, cx, y);
				y+= 32;
			}
		}else if (codeMod == -1 && triedcodeKey != -1) {
			if (errorKey.size() > 0) {
				int y = y1 +32;
				for (Key e : errorKey) {
					text.setFont(UI.FONT().M);
					text.errorify();
					text.clear().add(¤¤Fail);
					text.insert(0, e.repr());
					text.insert(1, e.name);
					text.adjustWidth();
					text.renderC(r, cx, y);
					y+= 32;
				}
				
			}
		}else {
			
			if (codeMod != -1) {
				text.setFont(UI.FONT().H2);
				text.normalify();
				text.set(KEYS.names.getCode(codeMod));
				text.renderC(r, cx, y1);
			}else {
				text.setFont(UI.FONT().M);
				text.color(COLOR.WHITE702WHITE100);
				text.normalify();
				text.set(¤¤Explanation);
				text.renderC(r, cx, y1+32);
			}
		}
		
		
		
		
		return false;
	}

	@Override
	protected boolean update(float ds) {
		
		
		
		if (timer > 0) {
			timer -= ds;
			if (timer <= 0 || MButt.LEFT.consumeClick()) {
				hide();
			}
			VIEW.setKeyPoller(this);
			return true;
		}
		VIEW.setKeyPoller(this);
		
		if (codeKey != -1) {
			
			triedcodeKey = codeKey;
			triedcodeMod = codeMod;
			this.codeKey = -1;
			this.codeMod = -1;
			this.errorKey.clear();;
			
			if (KEYS.MAIN().get(triedcodeMod, triedcodeKey) != null && KEYS.MAIN().get(triedcodeMod, triedcodeKey) != key)
				errorKey.add(KEYS.MAIN().get(triedcodeMod, triedcodeKey));
			if (key.page().get(triedcodeMod, triedcodeKey) != null  && key.page().get(triedcodeMod, triedcodeKey) != key)
				errorKey.add(key.page().get(triedcodeMod, triedcodeKey));
			
			if (key.page() == KEYS.MAIN()) {
				for (KeyPage p : KEYS.pages()) {
					if (p == key.page() || p == KEYS.MAIN())
						continue;
					if (p.get(triedcodeMod, triedcodeKey) != null && p.get(triedcodeMod, triedcodeKey) != key)
						errorKey.add(p.get(triedcodeMod, triedcodeKey));
					
				}
			}
			
			if (key.assign(triedcodeMod, triedcodeKey)) {
				KEYS.get().save();
				timer = 5;
			}
			
		}
		
		if (MButt.RIGHT.consumeClick()) {
			hide();
		}
		if (timer > 0 && MButt.LEFT.consumeClick()) {
			hide();
		}
		
		return false;
	}

	void show(Key key) {
		this.key = key;
		this.codeKey = -1;
		this.codeMod = -1;
		this.triedcodeMod = -1;
		this.triedcodeKey = -1;
		timer = 0;
		VIEW.inters().manager.add(this);
	}

	@Override
	public void poll(LIST<KeyEvent> keys) {
		
		if (timer > 0) {
			for (KeyEvent e : keys) {
				
				if (e.action() == KEYACTION.PRESS) {
					hide();
					return;
				}
			}
		}
		
		for (KeyEvent e : keys) {
			
			if (e.action() == KEYACTION.PRESS) {
				if (e.code() == KEYCODES.KEY_ESCAPE) {
					hide();
					return;
				}
				
				
				
				if (codeMod == -1) {
					codeMod = e.code();
				}else {
					codeKey = e.code();
				}
				
			}else if (e.action() == KEYACTION.RELEASE) {
				if (e.code() == codeMod) {
					codeKey = codeMod;
					codeMod = -1;
				}
			}
			
			
		}
		
		
	}
	
}
