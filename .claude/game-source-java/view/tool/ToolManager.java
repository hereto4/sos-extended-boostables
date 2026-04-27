package view.tool;

import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.gui.Hoverable.HOVERABLE;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.ArrayList;
import util.gui.misc.GBox;
import view.interrupter.InterManager;
import view.interrupter.Interrupter;
import view.main.VIEW;
import view.subview.GameWindow;

public final class ToolManager extends Interrupter{

	private ToolConfig config;
	private Tool current;
	private Tool def;
	private boolean hovered;
	public final ToolPlacer placer;
	private final GameWindow window;
	private final ToolConfig configDummy = new ToolConfig() {
	};
	private ArrayList<RENDEROBJ> rens = new ArrayList<>(16);
	
	public ToolManager(InterManager manager, GameWindow window) {
		this.window = window;
		
		placer = new ToolPlacer(this, window);
		lastSet();
		pin();
		persistantSet();
		
		show(manager);
		
	}
	
	public GameWindow window() {
		return window;
	}
	
	public ToolManager setDefault(Tool def) {
		this.def = def;
		if (current == null)
			set(def, def.defaultConfig(), true);
		return this;
	}
	
	public void setHovered(boolean hovered){
		this.hovered = hovered;
	}
	
	public boolean isHovered() {
		return hovered;
	}
	
	
	@Override
	public boolean update(float ds){
		
		
		window.update(ds);
		if (current == null)
			return true;
		
		config.update(!hovered);
		if (current != null) {
			if (hovered){
				current.updateHovered(ds, window);
			}else{
				current.update(ds, window);
			}
		}
		
		return true;
		
	}

	public void place(PLACABLE placer, ToolConfig config) {
		if (placer == null) {
			set(null, null, true);
			return;
		}
		this.placer.activate(placer);
		set(this.placer, config, true);
	}
	
	public void place(PLACABLE placer, ToolConfig config, boolean disturb) {
		if (placer == null) {
			set(null, null, disturb);
			return;
		}
		this.placer.activate(placer);
		set(this.placer, config, disturb);
	}
	
	public void place(PLACABLE placer) {
		if (placer == null) {
			set(null, null, true);
			return;
		}
		this.placer.activate(placer);
		set(this.placer, null, true);
	}
	
	public void set(Tool t) {
		if (placer == null) {
			set(null, null, true);
			return;
		}
		set(t, t.defaultConfig(), true);
	}


	
	public void set(Tool t, ToolConfig config, boolean disturb){
		
		rens.clear();
		if (config != null)
			config.addUI(rens);
		
		if (t == current && this.config == config)
			return;
		
		ToolConfig old = this.config;
		
		
		
		if (t == null) {
			t = def;
		}
		
		this.config = config;
		if (this.config == null && t != null)
			this.config = t.defaultConfig();
		
		if (this.config == null)
			this.config = configDummy;
		
		this.config.activateAction();
		
		
		current = t;
		if (disturb)
			manager().disturb();
		if (added.isActivated())
			manager().remove(added);
		manager().add(added);
		
		if (old != null)
			old.deactivateAction();
	}
	
	public boolean is(PLACABLE t) {
		return isActivated() && current == this.placer && this.placer.getCurrent() == t;
	}

	@Override
	protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
		hovered = true;
		
		for (RENDEROBJ r : rens) {
			if (r instanceof HOVERABLE) {
				if (((HOVERABLE) r).hover(mCoo)) {
					
					hovered = false;
				}
			}
			
		}
		if (hovered) {
			window.hover();
		}
		return true;
	}

	@Override
	protected void mouseClick(MButt button) {
		if (button == MButt.LEFT) {
			for (RENDEROBJ r : rens) {
				if (r instanceof CLICKABLE) {
					if (((CLICKABLE) r).hoveredIs()) {
						((CLICKABLE) r).click();
						return;
					}
				}
			}
			if (hovered && current != null)
				current.click(window);
		}else
			otherClick(button);
	}
	
	@Override
	protected boolean otherClick(MButt button) {
		if (button == MButt.RIGHT) {
			if (current != null && current.rightClick()) {
				if (config.back())
					set(null, null, false);
			}
			return true;
		}
		return false;
	}

	@Override
	protected void hoverTimer(GBox text) {
		for (RENDEROBJ r : rens) {
			if (r instanceof HOVERABLE) {
				if (((HOVERABLE) r).hoveredIs())
					((HOVERABLE) r).hoverInfoGet(text);
				
			}
			
		}
	}

	@Override
	protected boolean render(Renderer r, float ds) {
		if (rens.size() > 0) {
			for (RENDEROBJ re : rens) {
				re.render(r, ds);
			}
		}
		
		r.newLayer(true, window.zoomout());
		
		
		if (current == null)
			return true;
		if (hovered)
			current.renderHovered(r, ds, window, VIEW.hoverBox());
		else
			current.render(r, ds, window);
		
		return true;
	}
	
	@Override
	protected void afterTick() {
		rens.clear();
		if (current != null)
			config.addUI(rens);
		hovered = false;
	}
	
	public Tool current() {
		return current;
	}
	
	/**
	 * makes it so that when other inters are open and you start placing something,
	 * a right klick will close the placer first, then the other inters
	 */
	private final Interrupter added = new Interrupter() {
		
		@Override
		protected boolean update(float ds) {
			return true;
		}
		
		@Override
		protected boolean render(Renderer r, float ds) {
			return true;
		}
		
		@Override
		protected void mouseClick(MButt button) {
			
		}
		
		@Override
		protected boolean otherClick(MButt button) {
			if (current != null && current != def)
				return ToolManager.this.otherClick(button);
			hide();
			return false;
		}
		
		@Override
		protected void hoverTimer(GBox text) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		protected boolean hover(COORDINATE mCoo, boolean mouseHasMoved) {
			return false;
		}
	};

	public ToolConfig configCurrent() {
		return config;
	}


	
}
