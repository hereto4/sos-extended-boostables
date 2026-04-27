package view.sett.invasion;


import static game.GAME.s;

import java.io.IOException;

import game.GAME;
import game.save.Savable;
import init.constant.C;
import settlement.main.SETT;
import settlement.room.main.throne.THRONE;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.gui.GuiSection;
import util.gui.misc.GBox;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import view.battle.BattlePanel;
import view.battle.BattlePlacer;
import view.battle.BattleRenderer;
import view.battle.DivSelection;
import view.battle.UISelection;
import view.interrupter.ISidePanels;
import view.keyboard.KEYS;
import view.main.VIEW;
import view.sett.ui.minimap.UIMinimapSett;
import view.subview.GameWindow;
import view.ui.top.UIPanelTop;

public final class SBattleView extends VIEW.ViewSub{
	

	private final GameWindow window = new GameWindow( 
			1,
			C.DIM(),
			SETT.PIXEL_BOUNDS,
			0);
	private final DivSelection selection = new DivSelection();
	private final BattlePlacer placer = new BattlePlacer(window, selection);
	public final BattleRenderer renderer = new BattleRenderer(selection);
	final ISidePanels panels;
	final BattlePanel panel; 
	final UIMinimapSett minimap;
	
	public SBattleView(){
		UIPanelTop pp = new UIPanelTop(uiManager, false, true);
		{
			GuiSection s = new GuiSection();
			s.addRightC(0, UIPanelTop.bToggle());
			pp.addRightRight(s);
		}
		
		
		panels = new ISidePanels(uiManager, 0);
		minimap = new UIMinimapSett(uiManager,  UIPanelTop.HEIGHT, window, null);
		
		panel = new BattlePanel(panels, window, pp, selection, false);
		new UISelection(uiManager, selection, true);
		
		window.setzoomoutMax(3);
		GAME.saver().add(new Savable("S_BATTLEVIEW") {
			
			@Override
			protected void save(FilePutter file) {
				window.saver.save(file);
			}
			
			@Override
			protected void load(FileGetter file) throws IOException {
				window.saver.load(file);
				selection.clear();
			}
		});

	}
	
	
	@Override
	protected void hover(COORDINATE mCoo, boolean mouseHasMoved) {
		if (!uiManager.isHovered())
			window.hover();
	}
 
	@Override
	protected void mouseClick(MButt button) {
		placer.click(button);
		
	}
	
	@Override
	protected void hoverTimer(double mouseTimer, GBox text) {
		placer.hoverTimer(text);
		
	}

	
	@Override
	protected boolean update(float ds, boolean should){
		
		window.update(ds);
		placer.update(!uiManager.isHovered());
		if (KEYS.MAIN().THRONE.consumeClick()) {
			window.centererTile.set(THRONE.coo());
		}
		
		return true;
	}

	@Override
	protected void render(Renderer r, float ds, boolean hide) {
		window.crop(uiManager.viewPort());
		renderer.add();
		
		s().render(r, ds, window, minimap.config);
		if (VIEW.hideUI()) {
			return;
		}
		
		if (window.consumeHover()) {
			SETT.LIGHTS().renderMouse(window.pixel().x(), window.pixel().y(), -window.pixels().relX(), -window.pixels().relY(), 5);
			
			if (window.hasZoomedOutMoreandConsumeThatMotherFZoom())
				minimap.open();
		}
	}
	
	public GameWindow getWindow(){
		return window;
	}
	
	public void clearAllInterrupters(){
		uiManager.clear();
	}
	
	@Override
	public void activate() {
		window.stop();
		window.copy(VIEW.s().getWindow());
		super.activate();
		
	}
	
	@Override
	public void deactivate() {
		VIEW.s().getWindow().copy(window);
		super.deactivate();
	}
	
	public void clear() {
		selection.clear();
	}
	
	@Override
	protected void afterTick() {
		selection.clearHover();
	}


	@Override
	public void renderBelowTerrain(Renderer r, ShadowBatch s, RenderData data) {
		renderer.renderBelow(r, data);
	}
	
	
}
