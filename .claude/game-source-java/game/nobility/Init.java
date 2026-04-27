package game.nobility;

import init.paths.PATH;
import init.paths.PATHS;
import snake2d.util.sets.ArrayList;

class Init {

	final PATH pData = PATHS.INIT().getFolder("race").getFolder("nobility");
	final PATH pText = PATHS.TEXT().getFolder("race").getFolder("nobility");
	final ArrayList<Noble> all = new ArrayList<>(100);
	
}
