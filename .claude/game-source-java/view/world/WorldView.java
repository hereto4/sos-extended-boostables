package view.world;

import static world.WORLD.PIXELS;

import java.io.IOException;

import game.GAME;
import game.faction.FACTIONS;
import game.save.Savable;
import init.constant.C;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import util.gui.misc.GBox;
import view.interrupter.ISidePanels;
import view.keyboard.KEYS;
import view.main.VIEW;
import view.subview.GameWindow;
import view.tool.PLACABLE;
import view.tool.ToolManager;
import view.ui.top.UIPanelTop;
import view.world.editor.WorldViewEditor;
import view.world.panel.IDebugPanelWorld;
import view.world.panel.WorldViewPanel;
import world.WORLD;

public class WorldView extends VIEW.ViewSub{
	
	public final GameWindow window = createwindow();
	public static GameWindow createwindow() {
		return new GameWindow( 
				1,
				C.DIM(),
				PIXELS(),
				C.TILE_SIZE*5);
	}
	
	
	public final ToolManager tools;
	public final WorldUI  UI;
	public final ISidePanels panels = new ISidePanels(uiManager, 0);
	public final IDebugPanelWorld debug;
	public final WorldViewEditor editor;
	
	public WorldView(){
		
		UIPanelTop p = new UIPanelTop(uiManager);
		//p.addNoti();
		new WorldViewPanel(this, p);
		
		
		tools = new ToolManager(uiManager, window);
		UI  = new WorldUI(uiManager, panels, tools);
		tools.setDefault(new ToolDefault(tools));
		editor = new WorldViewEditor(window);
		GAME.saver().add(new Savable("VIEW_WORLD") {
			
			@Override
			protected void save(FilePutter file) {
				window.saver.save(file);
			}
			
			@Override
			protected void load(FileGetter file) throws IOException {
				window.saver.load(file);
				WORLD.MINIMAP().repaint();
			}
		});
		
		for (PLACABLE pl : WORLD.TERRAIN().saver().makePlacers(tools)) {
			IDebugPanelWorld.add(pl, "terrain");
		}
		debug = new IDebugPanelWorld(uiManager);
	}

	@Override
	public void activate() {
		if (VIEW.current() == this)
			return;
		super.activate();
		
		window.stop();
		tools.set(null, null, true);
		
	}
	
	@Override
	protected void hover(COORDINATE mCoo, boolean mouseHasMoved) {
		
	}

	@Override
	protected void mouseClick(MButt button) {

	}
	
	@Override
	protected void hoverTimer(double mouseTimer, GBox text) {
		
		
		
	}
	
	@Override
	protected boolean update(float ds, boolean should){
		if (KEYS.MAIN().THRONE.consumeClick()) {
			window.centererTile.set(FACTIONS.player().capitolRegion().cx(), FACTIONS.player().capitolRegion().cy());
		}
		return true;
	}
	
	@Override
	protected void render(Renderer r, float ds, boolean hide) {
		
		window.crop(uiManager.viewPort());
		GAME.world().render(r, ds, window.zoomout(), window.pixels(), window.view().x1()<<window.zoomout(), window.view().y1()<<window.zoomout());
		
	}

	@Override
	protected void afterTick() {
		
	}
	
}
