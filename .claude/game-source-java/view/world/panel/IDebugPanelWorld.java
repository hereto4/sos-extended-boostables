package view.world.panel;

import java.util.TreeMap;

import game.GameDisposable;
import init.sprite.UI.UI;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.AREA;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.map.MAP_PLACER;
import snake2d.util.misc.ACTION;
import util.data.BOOLEAN.BOOLEAN_MUTABLE;
import util.gui.misc.GButt;
import view.interrupter.IDebugPanelAbs;
import view.interrupter.InterManager;
import view.main.VIEW;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

public class IDebugPanelWorld extends IDebugPanelAbs{

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
				VIEW.world().debug.hide();
			}
		};
		c.clickActionSet(action);
		return c;
	}
	
	private static void put(CharSequence key, CLICKABLE obj) {
		String s = "" + key;
		while(hash.containsKey(s))
			s += 'I';
		hash.put(s.toLowerCase(), obj);
		
	}
	
	public static void add(CharSequence name, BOOLEAN_MUTABLE toggle) {
		put(name, get(name, toggle));
	}
	
	public static void add(CharSequence name, ACTION action) {
		put(name, get(name, action));
	}
	

	
	public static void add(PLACABLE placable) {
		put(placable.name(), get(placable.name(), new ACTION() {
			@Override
			public void exe() {
				
				VIEW.world().tools.place(placable);
			}
		}));
	}
	
	public static void add(PLACABLE placable, String prefix) {
		put(placable.name(), get(prefix + " " + placable.name(), new ACTION() {
			@Override
			public void exe() {
				
				VIEW.world().tools.place(placable);
			}
		}));
	}
	
	public static void add(MAP_PLACER placable, String name) {
		add(new PlacableMulti(name) {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				placable.set(tx, ty);
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				// TODO Auto-generated method stub
				return null;
			}
		});
	}
	
	public static void addClear(MAP_PLACER placable, String name) {
		add(new PlacableMulti(name) {
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				placable.clear(tx, ty);
				
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				// TODO Auto-generated method stub
				return null;
			}
		});
	}
	
	public static void add(String name, ACTION a) {
		put(name, get(name, a));
	}
	
	public IDebugPanelWorld(InterManager m) {
		super(m, init());
		hash.clear();
	}
	
	private static TreeMap<CharSequence, CLICKABLE> init() {
		
		return hash;
		
	}
	
}
