package view.world.editor;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.player.PTitles.PTitle;
import init.sprite.SPRITES;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.ACTION;
import util.gui.misc.GBox;
import util.text.Dic;
import view.interrupter.ISidePanels;
import view.main.VIEW;
import view.subview.GameWindow;
import view.tool.ToolManager;
import world.WORLD;

public class WorldViewEditor extends VIEW.ViewSubSimple{
	

	
	final GameWindow window;
	final ToolManager tools;
	final ISidePanels panels;
	public final static ACTION loadPrint = new ACTION() {
		
		@Override
		public void exe() {
			if (!SPRITES.loader().isMini())
				SPRITES.loader().minify(true, Dic.¤¤Generating);
			SPRITES.loader().print(Dic.¤¤Generating);;
		}
	};
	
	boolean hasSeletedRace = false;
	boolean hasSelectedTitles = true;
	
	public WorldViewEditor(GameWindow window){
		
		for (PTitle t : FACTIONS.player().titles.all()) {
			if (t.unlocked())
				hasSelectedTitles = false;
		}
		
		this.window = window;
		tools = new ToolManager(uiManager, window);
		window.setZoomout(2);
		window.centererTile.set(WORLD.TWIDTH()/2, WORLD.THEIGHT()/2);
		panels = new ISidePanels(uiManager, 0);
		reset();
		
		new TopPanel(this);
		
		
		
	}

	@Override
	public void activate() {
		super.activate();
		window.stop();
		WORLD.FOW().toggled.set(false);
		WORLD.GEN().isEditing = true;
		//RES.loader().minify(true, DicMisc.¤¤Generating);
	}
	
	@Override
	public void deactivate() {
		WORLD.FOW().toggled.set(true);
	}
	
	@Override
	protected void hover(COORDINATE mCoo, boolean mouseHasMoved) {
		
		window.hover();
	}

	@Override
	protected void mouseClick(MButt button) {
		
	}
	
	@Override
	protected void hoverTimer(double mouseTimer, GBox text) {
		
	}
	
	@Override
	protected boolean update(float ds, boolean should){
		
		return true;
	}
	
	@Override
	protected void render(Renderer r, float ds, boolean hide) {

		window.crop(uiManager.viewPort());
		GAME.world().render(r, ds, window.zoomout(), window.pixels(), window.view().x1()<<window.zoomout(), window.view().y1()<<window.zoomout());
		
		
	}
	
	void reset() {
		uiManager.clear();
		tools.place(null);
	}
	
}
