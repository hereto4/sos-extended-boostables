package settlement.main;

import game.GameDisposable;
import init.constant.C;
import snake2d.CORE;
import snake2d.Renderer;
import snake2d.util.light.AmbientLight;
import snake2d.util.sets.ArrayList;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;

public abstract class ON_TOP_RENDERABLE{
	
	static final ArrayList<ON_TOP_RENDERABLE> renderables = new ArrayList<ON_TOP_RENDERABLE>(64);
	static {
		new GameDisposable() {
			@Override
			protected void dispose() {
				renderables.clear();
			}
		};
	}
	
	
	private boolean isAdded = false;
	
	public void render(ShadowBatch shadowBatch, RenderData data, int zoomout, double ds) {
		CORE.renderer().newLayer(false, zoomout);
		AmbientLight.full.register(0, C.WIDTH()<<zoomout, 0, C.HEIGHT()<<zoomout);
		render(CORE.renderer(), shadowBatch, data, ds);
	}
	
	protected abstract void render(Renderer r, ShadowBatch shadowBatch, RenderData data, double ds);
	
	
	
	public void add() {
		if (isAdded)
			return;
		renderables.add(this);
		isAdded = true;
	}
	
	public void remove() {
		if (!isAdded)
			return;
		renderables.remove(this);
		isAdded = false;
	}
	
	public boolean isAdded() {
		return isAdded;
	}
	

}