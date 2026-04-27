package view.battle;


import static game.GAME.s;

import java.io.IOException;

import game.GAME;
import game.battle.state.BattleState;
import game.save.Savable;
import init.constant.C;
import settlement.entity.ENTITY;
import settlement.main.SETT;
import settlement.room.main.throne.THRONE;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import util.gui.misc.GBox;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import view.battle.editor.BattleViewEditor;
import view.interrupter.ISidePanels;
import view.keyboard.KEYS;
import view.main.VIEW;
import view.sett.ui.minimap.UIMinimapSett;
import view.subview.GameWindow;
import view.ui.top.UIPanelTop;

public final class BattleView extends VIEW.ViewSub{
	

	private final GameWindow window = new GameWindow( 
			1,
			C.DIM(),
			SETT.PIXEL_BOUNDS,
			0);
	private final DivSelection selection = new DivSelection();
	private final BattlePlacer placer = new BattlePlacer(window, selection);
	private final BattleRenderer renderer = new BattleRenderer(selection);
	public final ISidePanels panels;
	final BattlePanel panel; 
	final UIMinimapSett minimap;
	private BattleState state;
	public final BattleViewEditor editor;
	
	public BattleView(){

		UIPanelTop pp = new UIPanelTop(uiManager, true, true);
		panels = new ISidePanels(uiManager, 0);
		minimap = new UIMinimapSett(uiManager, UIPanelTop.HEIGHT, window, null);
		
		
		panel = new BattlePanel(panels, window, pp, selection, true);
		new UISelection(uiManager, selection, false);
		
		window.setzoomoutMax(3);
		new IDeploy(uiManager);
		GAME.saver().add(new Savable("BATTLE_VIEW") {
			
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
		
		editor = new BattleViewEditor();
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
		if (MButt.RIGHT.isDown()) {
			ENTITY e = SETT.ENTITIES().getAtPoint(window.pixel().x(), window.pixel().y());
			if (e != null) {
				e.hover(text);
				return;
			}
			
		}
		placer.hoverTimer(text);
		
	}

	
	public BattleState state() {
		return state;
	}
	
	@Override
	protected boolean update(float ds, boolean should){
		
		if (state != null)
			state.update(ds*GAME.SPEED.speedTarget());
		
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
	
	@Override
	protected void afterTick() {
		selection.clearHover();
		super.afterTick();
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
		super.activate();
	}
	
	public void activate(BattleState state) {
		this.state = state;
		window.stop();
		super.activate();
	}
	
	
	public void clear() {
		selection.clear();
	}
	
	@Override
	public void renderBelowTerrain(Renderer r, ShadowBatch s, RenderData data) {
		renderer.renderBelow(r, data);
	}
	
	@Override
	protected boolean canSave() {
		return false;
	}
	
	
}
