package view.tool;

import snake2d.SPRITE_RENDERER;
import util.gui.misc.GBox;
import view.subview.GameWindow;

public abstract class Tool {

	private final static ToolConfig normal = new ToolConfig() {};
	protected void update(float ds, GameWindow window){};
	protected abstract void updateHovered(float ds, GameWindow window);
	protected void render(SPRITE_RENDERER r, float ds, GameWindow window){};
	protected abstract void renderHovered(SPRITE_RENDERER r, float ds, GameWindow window, GBox box);
	
	protected abstract void click(GameWindow window);
	
	
	
	protected ToolConfig defaultConfig() {
		return normal;
	}
	
	private final ToolManager manager;
	
	protected Tool(ToolManager manager){
		this.manager = manager;
	}
	
	protected boolean rightClick(){
		return true;
	}
	
	protected ToolManager manager() {
		return manager;
	}
	
	public final void deactivate() {
		manager().set(null, null, true);
	}

	
	public boolean isActivated(){
		return manager().current() == this;
	}
	
}
