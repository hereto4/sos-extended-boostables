package view.world.generator;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.player.PTitles.PTitle;
import init.race.Race;
import init.sprite.SPRITES;
import snake2d.LOG;
import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.misc.ACTION;
import util.gui.misc.GBox;
import util.text.D;
import util.text.Dic;
import view.interrupter.ISidePanels;
import view.main.VIEW;
import view.subview.GameWindow;
import view.tool.ToolManager;
import view.world.WorldView;
import world.WORLD;
import world.WorldGen;

public class WorldViewGenerator extends VIEW.ViewSubSimple{
	
	static CharSequence ¤¤generate = "¤generate";
	static CharSequence ¤¤regenerate = "¤regenerate";
	static CharSequence ¤¤start = "¤start";
	static CharSequence ¤¤home = "¤home";
	
	static {
		D.ts(WorldViewGenerator.class);
	}
	
	final GameWindow window;
	final ToolManager tools;
	final IMinimap minimap;
	final Intr dummy;
	final ISidePanels panels;
	public final static ACTION loadPrint = new ACTION() {
		
		@Override
		public void exe() {
			if (!SPRITES.loader().isMini())
				SPRITES.loader().minify(true, Dic.¤¤Generating);
			SPRITES.loader().print(Dic.¤¤Generating);;
		}
	};
	
	boolean canSelectRace = true;
	boolean hasSeletedRace = false;
	boolean hasProfiled = false;
	boolean hasSelectedTitles = true;
	
	public WorldViewGenerator(){
		
		for (PTitle t : FACTIONS.player().titles.all()) {
			if (t.unlocked())
				hasSelectedTitles = false;
		}
		
		this.window = WorldView.createwindow();
		dummy = new Intr(this);
		minimap = new IMinimap(this);
		tools = new ToolManager(uiManager, window);
		window.setZoomout(2);
		window.centererTile.set(WORLD.TWIDTH()/2, WORLD.THEIGHT()/2);
		panels = new ISidePanels(uiManager, 0);
		set();
		
		
	}

	public static void setresettle(Race race) {
		if (VIEW.current() instanceof WorldViewGenerator) {
			WorldViewGenerator g = (WorldViewGenerator) VIEW.current();
			FACTIONS.player().setRace(race);
			g.hasSeletedRace = true;
			g.hasProfiled = true;
			g.canSelectRace = false;
			g.set();
		}else {
			LOG.err(VIEW.current());
		}
		
	}
	
	@Override
	public void activate() {
		super.activate();
		window.stop();
		WORLD.FOW().toggled.set(false);
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
		GAME.world().render(r, ds, window.zoomout(), window.pixels(), window.view().x1(), window.view().y1());

	}
	
	void reset() {
		uiManager.clear();
		dummy.add(null, null);
		tools.place(null);
		minimap.hide();
	}
	
	public void set() {
		reset();
		WorldGen g = WORLD.GEN();
		
		if (!hasSeletedRace && canSelectRace) {
			new StagePickRace(this);
		}else if (!hasProfiled) {
			new StageVisuals(this);
		}else if (!hasSelectedTitles && FACTIONS.player().titles.unlocked() > 0) {
			new StagePickTitles(this);
		}else if (!g.hasGeneratedTerrain) {
			new StageTerrain(this);
		}else if (g.playerX < 0) {
			new StageCapitol(this, false);
		}else {
			new StageFinish(this);
		}
	}
	
}
