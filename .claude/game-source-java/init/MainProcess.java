package init;

import game.VERSION;
import game.faction.player.PTitles;
import init.paths.PATHS;
import init.paths.PATHS.PATHS_BASE;
import launcher.LSettings;
import menu.Menu;
import snake2d.CORE;
import snake2d.LOG;
import snake2d.PreLoader;
import util.error.ErrorHandler;
import util.text.D;

class MainProcess {

	public static void main(String[] args) {
		
		PreLoader.load(VERSION.VERSION_STRING, PATHS_BASE.PRELOADER, PATHS_BASE.ICON_FOLDER + "Icon64.png");
		CORE.init(new ErrorHandler());
		
		LOG.ln("*******************************");
		LOG.ln("* GAME " + VERSION.VERSION_STRING);
		LOG.ln("*******************************");
		
		LSettings s = new LSettings();
				
		String l = s.lang.get();
		PATHS.init(s.mods.get(), l != null && l.length() > 0 ? l : null, s.easy.get() == 1);
		
		D.init();

		// PreLoader.exit();
		Menu.start();
		PTitles.achieve();
		PreLoader.exit();
		
	}
	
}
