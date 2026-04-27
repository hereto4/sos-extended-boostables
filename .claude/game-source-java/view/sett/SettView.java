package view.sett;


import static game.GAME.s;

import java.io.IOException;

import game.GAME;
import game.save.Savable;
import init.constant.C;
import settlement.main.SETT;
import settlement.overlay.Addable;
import settlement.room.main.throne.THRONE;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import util.data.GETTER.GETTER_IMP;
import util.gui.misc.GBox;
import util.rendering.RenderData;
import util.rendering.ShadowBatch;
import view.interrupter.ISidePanels;
import view.interrupter.InterGuisection;
import view.keyboard.KEYS;
import view.main.VIEW;
import view.sett.invasion.SBattleView;
import view.sett.ui.SettUI;
import view.sett.ui.minimap.UIMinimapSett;
import view.sett.ui.minimap.UIMinimapSettConfigExt;
import view.sett.ui.right.UIPanelRightSett;
import view.subview.GameWindow;
import view.tool.ToolManager;
import view.ui.top.UIPanelTop;

public class SettView extends VIEW.ViewSub{
	
	private final GameWindow window = new GameWindow( 
			1,
			C.DIM(),
			SETT.PIXEL_BOUNDS,
			0);
	public final Inters interrupters = new Inters();
	boolean hasPlaced = false;
	{
		UISettMap.clear();
	}
	private final SettViewStart start = new SettViewStart();
	public final UIPanelTopSett ui;
	public final SettUI misc = new SettUI(uiManager);
	public final ISidePanels panels;
	public final ToolManager tools = new ToolManager(uiManager, window);
	public final IDebugPanelSett debug;
	public final UIMinimapSett mini;
	public final UIPanelRightSett right;
	public final SBattleView battle;
	public final GETTER_IMP<Addable> overlayThing;
	
	public class Inters{
		
		
		public final InterGuisection section = new InterGuisection(uiManager);
		public final InterGuisection debugsection = new InterGuisection(uiManager);
		
		public Inters(){
			
			
			
			
		}
		
		
	}
	
	public SettView(){
		
		UIPanelTop pan = new UIPanelTop(uiManager);
		//pan.addNoti();
		panels = new ISidePanels(uiManager, 0);
		ui = new UIPanelTopSett(this, pan);
		
		window.setzoomoutMax(3);
		tools.setDefault(new ToolDefault(tools));
		debug = new IDebugPanelSett(uiManager);
		mini = new UIMinimapSett(uiManager, UIPanelTop.HEIGHT, window, new UIMinimapSettConfigExt("VIEW_SETT"));
		mini.panel().addScreenshot("VIEW_SETT");
		overlayThing = mini.panel().addOverlays();
		right = new UIPanelRightSett(mini, uiManager, window);
		//mini = new UIMinimap(pan, uiManager, UIPanelTop.HEIGHT, true, true, window, "VIEW_SETT");
		
		battle = new SBattleView();
		
		GAME.saver().add(new Savable("SETT_VIEW") {
			
			@Override
			protected void save(FilePutter file) {
				window.saver.save(file);
				right.save(file);
				file.bool(hasPlaced);
			}
			
			@Override
			protected void load(FileGetter file) throws IOException {
				window.saver.load(file);
				right.load(file);
				uiManager.clear();
				hasPlaced = file.bool();
				if (!hasPlaced)
					start.activate();
			}
		});

	}
	
	
	@Override
	protected void hover(COORDINATE mCoo, boolean mouseHasMoved) {

		
	}
 
	@Override
	protected void mouseClick(MButt button) {
//		if (!VIEW.hideUI()) {
//			if (interrupters.manager.click(button)){
//				tools.manager.click(button);
//			}
//		}
		
	}
	
	@Override
	protected void hoverTimer(double mouseTimer, GBox text) {
//		if (!VIEW.hideUI()) {
//			interrupters.manager.hoverTimer(mouseTimer, text);
//		}
	}

	
	@Override
	protected boolean update(float ds, boolean should){
		if (KEYS.MAIN().THRONE.consumeClick()) {
			window.centererTile.set(THRONE.coo());
		}
		
		return true;
		
	}

	@Override
	protected void render(Renderer r, float ds, boolean hide) {
		window.crop(uiManager.viewPort());
		s().render(r, (float) (ds*GAME.SPEED.speed()), window, mini.config);
		
		if (window.consumeHover()) {
			SETT.LIGHTS().renderMouse(window.pixel().x(), window.pixel().y(), -window.pixels().relX(), -window.pixels().relY(), 5);
			
			if (window.hasZoomedOutMoreandConsumeThatMotherFZoom())
				mini.open();
			
		}

		
//		if (VIEW.hideUI()) {
//			VIEW.mouse().hide(true);
//			s().render(r, ds, window);
//		}else {
//			if (interrupters.manager.render(r, ds)){
//				tools.manager.render(r, ds, VIEW.hoverBox());
//				
//				double s = MButt.getWheelSpin();
//				if (s != 0 && !KEY.CLEAR_PLACE.isPressed()) {
//					int d = s < 0 ? 1 : -1;
//					if (d > 0 && window.zoomout() == 2)
//						interrupters.minimap.show();
//					else
//						window.zoomInc(d);
//					MButt.clearWheelSpin();
//				}
//				
//				
//			}
//		}
		
	
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
	

	public void clear() {
		hasPlaced = false;
		start.activate();
		battle.clear();
		right.clear();
	}
	
	@Override
	public void renderBelowTerrain(Renderer r, ShadowBatch s, RenderData data) {
		SETT.JOBS().render(r, s, data);
	}
	
	
}
