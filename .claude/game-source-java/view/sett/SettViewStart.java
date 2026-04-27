package view.sett;


import static game.GAME.s;

import game.GAME;
import game.faction.FACTIONS;
import init.constant.C;
import init.sprite.SPRITES;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GuiSection;
import snake2d.util.gui.clickable.CLICKABLE;
import snake2d.util.gui.renderable.RENDEROBJ;
import snake2d.util.sets.LISTE;
import snake2d.util.sprite.SPRITE;
import util.gui.misc.GBox;
import util.gui.misc.GButt;
import util.gui.panel.GPanel;
import util.text.D;
import view.main.VIEW;
import view.sett.ui.minimap.UIMinimapSett;
import view.sett.ui.minimap.UIMinimapSettConfig;
import view.subview.GameWindow;
import view.tool.ToolConfig;
import view.tool.ToolManager;

public class SettViewStart extends VIEW.ViewSub{
	
	private final GameWindow window = new GameWindow( 
			1,
			C.DIM(),
			SETT.PIXEL_BOUNDS,
			0);
	private final ToolManager manager;
	private final UIMinimapSett mini;
	private final ToolDefault def;
	private final ToolConfig config;
	
	
	public SettViewStart(){
		
		D.t(this);
		
		GuiSection s = new GuiSection();
		s.body().setDim(200, 1);
//		{
//			int x = 0;
//			for (Minable m : RESOURCES.minables().all()) {
//				s.add(new GStat() {
//					
//					@Override
//					public void update(GText text) {
//						GFORMAT.i(text, SETT.MINERALS().totals.get(m));
//					}
//					
//					@Override
//					public void hoverInfoGet(GBox b) {
//						b.text(m.name);
//					};
//					
//				}.hv(m.resource.icon()), (x%6)*60, (x/6)*24);
//				x++;
//			}
//		}
//		
		{
			final CLICKABLE butt = new GButt.ButtPanel(new SPRITE.Twin(SPRITES.icons().m.terrain, SPRITES.icons().m.rotate)){
				@Override
				protected void clickA() {
					SETT.reGenerate();
				};
			};
			s.addRelBody(4, DIR.S, butt.hoverInfoSet(D.g("Regenerate")));
		}
		
		GPanel p = new GPanel(s.body()).setBig();
		p.setTitle(D.g("start", "Landing Party"));
		p.body.moveY1(80);
		p.body.centerX(C.DIM());
		s.body().centerIn(p.inner());
		s.add(p);
		s.moveLastToBack();

		
		manager = new ToolManager(uiManager, window);
		mini = new UIMinimapSett(uiManager, 0, window, UIMinimapSettConfig.ALL);
		mini.panel().addOverlays();
		mini.panel().addScreenshot(null);
		
		
		config = new ToolConfig() {
			
			@Override
			public boolean back() {
				return false;
			}
			
			@Override
			public void update(boolean UIHovered) {
				if (STATS.POP().POP.data(null).get(null, 0) > 0) {
					VIEW.s().activate();
					
					FACTIONS.player().bonusesCustom.apply();
					
					GAME.setGameStart();
				}
			}
			
			@Override
			public void addUI(LISTE<RENDEROBJ> uis) {
				uis.add(s);
				//uis.add(butt);
			}
			
		};
		window.setzoomoutMax(3);
		def = new ToolDefault(manager);
		manager.place(SETT.PLACERS().landingParty, config);
		
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
	protected boolean update(float ds, boolean shoudl){
		VIEW.s().getWindow().copy(window);
		if (MButt.RIGHT.isDown()) {
			manager.set(def);
		}else if (STATS.POP().POP.data(null).get(null, 0) == 0)
			manager.place(SETT.PLACERS().landingParty, config);
		
		
		return true;
		
	}

	@Override
	protected void render(Renderer r, float ds, boolean hide) {
		
		
		
		s().render(r, ds, window, mini.config);
		if (window.consumeHover()) {
			
			SETT.LIGHTS().renderMouse(window.pixel().x(), window.pixel().y(), -window.pixels().relX(), -window.pixels().relY(), 5);
			
			if (window.hasZoomedOutMoreandConsumeThatMotherFZoom())
				mini.open();
		}
	}
	
	@Override
	public void activate() {
		window.stop();
		window.copy(VIEW.s().getWindow());
		super.activate();
		
	}
	
	
	public void clear() {
		
	}
	
	
}
