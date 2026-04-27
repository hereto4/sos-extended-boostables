package launcher;

import java.io.IOException;

import game.VERSION;
import init.paths.PATHS;
import init.paths.PATHS.PATHS_BASE;
import snake2d.CORE;
import snake2d.CORE.GlJob;
import snake2d.CORE_STATE;
import snake2d.KEYCODES;
import snake2d.KeyBoard.KeyEvent;
import snake2d.MButt;
import snake2d.PreLoader;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.gui.GuiSection;
import snake2d.util.sets.LIST;
import util.error.ErrorHandler;
import util.spritecomposer.Initer;
import util.text.D;

public class Launcher extends CORE_STATE{

	static boolean startGame = false;
	
	void reboot(){
		CORE.renderer().clear();
		CORE.swapAndPoll();
		CORE.setCurrentState(new Constructor() {
			@Override
			public CORE_STATE getState() {
				return new Launcher(false);
			}
		});
	}
	
	public static void main(String[] args) {
		boolean selectLang = !PATHS.local().SETTINGS.exists("LauncherSettings");
		LSettings settings = new LSettings();

		if (!PATHS_BASE.langs().existsFolder(settings.lang.get())) {
			
			selectLang = true;
		}
    	CORE.init(new ErrorHandler());
		CORE.create(new Sett());
		final boolean l = selectLang;
		CORE.start(new Constructor() {
			@Override
			public CORE_STATE getState() {
				
				PreLoader.exit();
				return new Launcher(l);
			}
		});
		if (!startGame)
			System.exit(1);
		System.exit(0);
	}
	
	RES res;
	final GUI g;
	private final BG bg;
	private COORDINATE mCoo = new Coo();
	final LSettings s = new LSettings();
	
	private GuiSection current;
	private final ScreenMain main;
	private final ScreenSetting setts;
	private final ScreenMods mods;
	private final ScreenInfo info;
	private final ScreenLog log;
	private final ScreenLang lang;
	
	private Launcher(boolean selectLang) {
		PATHS.init(new String[0], s.lang.get().length() > 0 ? s.lang.get() : null, false);
    	D.init();
		new GlJob() {
			@Override
			public void doJob() {
				new Initer() {
					
					@Override
					public void createAssets() throws IOException {
						res = new RES();
					}
				}.get("launcher", 1024, 0);
			}
		}.perform();
		
		g = new GUI(res);
		
		bg = new BG(res);
		log = new ScreenLog(this);
		lang = new ScreenLang(this, true);
		main = new ScreenMain(this, lang);
		info = new ScreenInfo(this);
		mods = new ScreenMods(this);
		setts = new ScreenSetting(this);
		current = main;
		
		if (selectLang)
			current = new ScreenLang(this, false);
		else {
			if (s.version.get() != VERSION.VERSION) {
				s.save();
				current = log;
			}
		}
			
	}


	@Override
	public void mouseClick(MButt button) {
		if (button == MButt.LEFT){
			current.click();
		}else if (button == MButt.RIGHT)
			setMain();
		
	}

	@Override
	public void update(float ds, double slow) {
		bg.update(ds);
		hover(CORE.getInput().getMouse().getCoo(), false);
	}

	@Override
	public void render(Renderer r, float ds) {
		
		bg.render(r, ds);
		
		current.render(r, ds);
		bg.renderClouds(r, ds);
		
	}


	//functions for the buttons
	
	void setInfo() {
		current = info;
		current.hover(mCoo);
	}
//	
	void setMain(){
		current = main;
		current.hover(mCoo);
	}
//	
	void setSetts(){
		current = setts;
		current.hover(mCoo);
	}
	
	void setMods(){
		current = mods;
		current.hover(mCoo);
	}
	
	void setLang(){
		current = lang;
		current.hover(mCoo);
	}
	
	void setModWarning(){
		current = new ScreenModWarning(this);
		current.hover(mCoo);
	}
//	
	void setLog(){
		current = log;
		current.hover(mCoo);
	}

	public void hover(COORDINATE mCoo, boolean mouseHasMoved) {
		this.mCoo = mCoo;
		current.hover(mCoo);
	}

	@Override
	protected void keyPush(LIST<KeyEvent> keys, boolean hasCleared) {
		
		for (KeyEvent c : keys) {
			if (CORE.getInput().getKeyboard().isPressed(KEYCODES.KEY_LEFT_CONTROL) && c.code() == KEYCODES.KEY_X)
				throw new RuntimeException("creating debugging info. Please send this to the developer if you're having issues");
			if (c.code() == KEYCODES.KEY_ESCAPE) {
				startGame = false;
				CORE.annihilate();
				return;
			}
		}
	}
	
}
