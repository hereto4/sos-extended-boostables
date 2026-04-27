package snake2d;

import snake2d.Displays.DisplayMode;

public interface SETTINGS {

	/**
	 * 
	 * @return if vsync should be enabled. Locks refresh rate. Reduces screen tearing.
	 */
	public boolean getVSynchEnabled();
	public int getNativeWidth();
	public int getNativeHeight();
	
	/**
	 * 
	 * @return if the rendered scene should be stretched and distorted to fit the selected screen resolution
	 */
	public boolean getFitToScreen();
	
	/**
	 * 
	 * @return size of particles
	 */
	public int getPointSize();
	
	/**
	 * 
	 * @return if native width < displaywidth, which filtering to use.
	 */
	public boolean getLinearFiltering();
	
	/**
	 * 
	 * @return
	 * 0 == normal
	 * 1 == deffered lightning + normal map
	 */
	public int getRenderMode();
	
	/**
	 * 
	 * @return the name that will be displayed in the decoration of the window
	 */
	public String getWindowName();
	
	
	/**
	 * 
	 * @return the absolute path of the location of:
	 * icon16.png
	 * icon32.png
	 * icon48.png
	 * ex C:\syx\
	 * can be null
	 */
	public String getIconFolder();
	
	public String getScreenshotFolder();
	
	public default boolean mutonfocus() {
		return true;
	}
	
	/**
	 * 
	 * @return if a windowed window should be decorated
	 */
	public boolean decoratedWindow();
	
	public int monitor();
	public DisplayMode display();
	
	/**
	 * if you want messages from LWJGL / OPENGL
	 * @return
	 */
	public boolean debugMode();
	
	public String openALDevice();
	

	
	
	public boolean vsyncAdaptive();
	
	public boolean windowFloating();
	public boolean autoIconify();
	
	public boolean windowFullFull();
	
}
