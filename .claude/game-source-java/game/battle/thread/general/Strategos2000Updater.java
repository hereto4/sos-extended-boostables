package game.battle.thread.general;

import snake2d.Renderer;
import snake2d.util.file.SAVABLE;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import util.rendering.RenderData.RenderIterator;

public abstract class Strategos2000Updater implements SAVABLE{

	
	public abstract boolean update();
	public abstract void render(Renderer r, RenderIterator it);
	public abstract void render(Renderer r, ShadowBatch shadowBatch, RenderData data);
}
