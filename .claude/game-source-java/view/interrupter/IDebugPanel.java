package view.interrupter;

import java.util.TreeMap;

import game.GameDisposable;
import init.sprite.UI.UI;
import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.misc.ACTION;
import util.GUTIL;
import util.data.BOOLEAN.BOOLEAN_MUTABLE;
import util.gui.misc.GButt;
import view.main.VIEW;
import view.ui.util.VideoMaker;

public class IDebugPanel extends IDebugPanelAbs{

	static {
		new GameDisposable() {
			
			@Override
			protected void dispose() {
				hash.clear();
			}
		};
	}
	
	private final static TreeMap<CharSequence, CLICKABLE> hash = new TreeMap<CharSequence, CLICKABLE>();
	
	private static CLICKABLE get(String name, BOOLEAN_MUTABLE toggle) {
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
	
	private static CLICKABLE get(String name, ACTION action) {
		CLICKABLE c = new GButt.Glow(UI.FONT().S.getText(name)) {
			@Override
			protected void clickA() {
				VIEW.inters().debugpanel.hide();
			}
		};
		c.clickActionSet(action);
		return c;
	}
	
	private static void put(String key, CLICKABLE obj) {
		while(hash.containsKey(key))
			key += 'I';
		hash.put(key.toLowerCase(), obj);
		
	}
	
	public static void add(String name, BOOLEAN_MUTABLE toggle) {
		put(name, get(name, toggle));
	}
	
	public static void add(String name, ACTION a) {
		put(name, get(name, a));
	}
	
	public IDebugPanel(InterManager manager) {
		super(addStaticStuff(manager), hash);
	}
	
	@Override
	protected void addMisc() {
//		add("Timeoff", new ACTION() {
//			final GuiSection ss = TIME.debugSection();
//			@Override
//			public void exe() {
//				VIEW.getInterrupters().message.acivate(ss);
//			}
//		});
	}
	
	static InterManager addStaticStuff(InterManager manager) {
		
		add("show stats", new BOOLEAN_MUTABLE() {



			@Override
			public BOOLEAN_MUTABLE set(boolean bool) {
				GUTIL.debugger().toggle();
				return this;
			}

			@Override
			public boolean is() {
				return GUTIL.debugger().isToggled();
			}
			
		});
		
		add("crash", new ACTION() {

			@Override
			public void exe() {
				throw new RuntimeException("Crash");
				
			}
			
		});
		
		add("hideUI(cancel with esc)", new ACTION() {

			@Override
			public void exe() {
				VIEW.hide();
			}
			
		});
		
		add("garbage Collect", new ACTION() {

			@Override
			public void exe() {
				new CORE.GlJob() {
					
					@Override
					protected void doJob() {
						gc();
					}
				}.perform();
			}
			
		});
		
		new VideoMaker();
		
		return manager;

	}

}
