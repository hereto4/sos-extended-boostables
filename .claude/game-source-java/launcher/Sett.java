package launcher;

import init.paths.PATHS;
import init.paths.PATHS.PATHS_BASE;
import snake2d.Displays.DisplayMode;
import snake2d.SETTINGS;

class Sett implements SETTINGS{

	Sett(){}
	
	public static final int WIDTH = 896;
	public static final int HEIGHT = 448;
	public static final int SCALE = 4;
	private final DisplayMode display = new DisplayMode(WIDTH, HEIGHT, 60, false);

	@Override
	public int getNativeWidth() {
		return WIDTH;
	}

	@Override
	public int getNativeHeight() {
		return HEIGHT;
	}

	@Override
	public int getRenderMode() {
		return 0;
	}

	@Override
	public String getWindowName() {
		return "SOS Launcher";
	}

	@Override
	public boolean getVSynchEnabled() {
		return false;
	}

	@Override
	public int getPointSize() {
		return SCALE;
	}

	@Override
	public boolean getLinearFiltering() {
		return false;
	}

	@Override
	public String getIconFolder() {
		return PATHS_BASE.ICON_FOLDER;
	}

	@Override
	public boolean getFitToScreen() {
		return false;
	}

	@Override
	public boolean decoratedWindow() {
		return true;
	}

	@Override
	public DisplayMode display() {
		return display;
	}

	@Override
	public boolean debugMode() {
		return false;
	}

	
	@Override
	public String getScreenshotFolder() {
		return ""+PATHS.local().SCREENSHOT.get();
	}

	@Override
	public int monitor() {
		return 0;
	}
	
	@Override
	public String openALDevice() {
		return null;
	}

	@Override
	public boolean autoIconify() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean windowFloating() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean vsyncAdaptive() {
		return false;
	}

	@Override
	public boolean windowFullFull() {
		// TODO Auto-generated method stub
		return false;
	}

}
