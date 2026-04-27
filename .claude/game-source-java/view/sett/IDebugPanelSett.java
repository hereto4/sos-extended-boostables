package view.sett;

import java.util.TreeMap;

import game.GameDisposable;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import util.data.BOOLEAN.BOOLEAN_MUTABLE;
import util.gui.misc.GButt;
import view.interrupter.IDebugPanelAbs;
import view.interrupter.InterManager;
import view.main.VIEW;
import view.tool.PLACABLE;

public class IDebugPanelSett extends IDebugPanelAbs{

	private final static TreeMap<CharSequence, CLICKABLE> hash = new TreeMap<CharSequence, CLICKABLE>();
	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				hash.clear();
			}
		};
	}
	
	
	private static CLICKABLE get(CharSequence name, BOOLEAN_MUTABLE toggle) {
		GButt.Checkbox c = new GButt.Checkbox(UI.FONT().S.getText(name)) {
			@Override
			protected void clickA() {
				selectedToggle();
				toggle.set(selectedIs());
			}
			@Override
			protected void render(SPRITE_RENDERER r, float ds, boolean isActive, boolean isSelected,
					boolean isHovered) {
				selectedSet(toggle.is());
				super.render(r, ds, isActive, isSelected, isHovered);
			}
		};
		return c;
	}
	
	private static CLICKABLE get(CharSequence name, ACTION action) {
		CLICKABLE c = new GButt.Glow(UI.FONT().S.getText(name)) {
			@Override
			protected void clickA() {
				VIEW.s().debug.hide();
			}
		};
		c.clickActionSet(action);
		return c;
	}
	
	public static void add(String name, BOOLEAN_MUTABLE toggle) {
		put(name, get(name, toggle));
	}
	
	public static void add(String name, PLACABLE...placables) {
		for (PLACABLE p : placables) {
			put(name + ": " + p.name(), get(name + ": " + p.name(), new ACTION() {
				@Override
				public void exe() {
					VIEW.s().tools.place(p);
				}
			}));
		}
	}
	
	public static void add(String name, Iterable<PLACABLE> placables) {
		for (PLACABLE p : placables) {
			put(name + ": " + p.name(), get(name + ": " + p.name(), new ACTION() {
				@Override
				public void exe() {
					VIEW.s().tools.place(p);
				}
			}));
		}
	}
	
	public static void add(PLACABLE placable) {
		put(placable.name(), get(placable.name(), new ACTION() {
			@Override
			public void exe() {
				VIEW.s().tools.place(placable);
			}
		}));
	}
	
	public static void add(String key, ACTION action) {
		put(key, get(key, action));
	}
	
	public static void add(String prefix, PLACABLE placable) {
		put(prefix + ": " +placable.name(), get(prefix + ": " + placable.name(), new ACTION() {
			@Override
			public void exe() {
				VIEW.s().tools.place(placable);
			}
		}));
	}
	
	private static void put(CharSequence key, CLICKABLE obj) {
		String s = "" + key;
		
		while(hash.containsKey(s))
			s += s  + 'I';
		hash.put(s.toLowerCase(), obj);
		
	}
	
	IDebugPanelSett(InterManager m) {
		super(m, init());
		hash.clear();
	}
	


	
	private static TreeMap<CharSequence, CLICKABLE> init() {
		
		return hash;
		
	}

	
}
