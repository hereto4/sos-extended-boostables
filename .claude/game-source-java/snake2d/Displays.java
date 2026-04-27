package snake2d;

import static org.lwjgl.glfw.GLFW.glfwGetMonitors;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetVideoModes;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFWVidMode;

import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;

public final class Displays {

	private static Displays self;
	private final DisplayMode[] currentDisplay;
	private final LIST<LIST<DisplayMode>> supported;
	private static final LIST<DisplayMode> ssupported = new ArrayList<>(0);
	private final long[] monitors;
	
	Displays(){
		self = this;
		
		Printer.ln("DISPLAYS");

		PointerBuffer mBuffer = glfwGetMonitors();
		
		if (mBuffer.capacity() == 0) {
			throw new IllegalStateException("No monitors are avalible!");
		}
		
		monitors = new long[mBuffer.capacity()];
		
		ArrayList<LIST<DisplayMode>> tmp = new ArrayList<>(mBuffer.capacity());
		currentDisplay = new DisplayMode[mBuffer.capacity()];

		for (int mi = 0; mi < mBuffer.capacity(); mi++) {
			monitors[mi] = mBuffer.get();
			
			GLFWVidMode vmode = glfwGetVideoMode(monitors[mi]);
			currentDisplay[mi] = new DisplayMode(vmode.width(), vmode.height(), vmode.refreshRate(), false);
			GLFWVidMode.Buffer vModes = glfwGetVideoModes(monitors[mi]);
			Printer.ln("DISPLAY " + mi + " ( " + currentDisplay[mi].toString() + " ) : ");
			ArrayList<DisplayMode> supp = new ArrayList<>(vModes.capacity());
			
			for (int i = 0; i < vModes.capacity(); i++) {
				supp.add(new DisplayMode(vModes.width(), vModes.height(), vModes.refreshRate(), true));
				Printer.pr(" | " + supp.get(i).toString());
				vModes.position(vModes.position() + 1);
			}
			Printer.ln();
			
			tmp.add(supp);
		}
		Printer.fin();
		
		this.supported = tmp;
	}
	
	public static int monitors() {
		if (self == null)
			return 0;
		return self.supported.size();
	}
	
	static long pointer(int monitor) {
		return self.monitors[monitor];
	}
	
	public static LIST<DisplayMode> available(int monitor){
		if (self == null)
			return ssupported;
		return self.supported.get(monitor);
	}
	
	public static DisplayMode current(int monitor) {
		if (self == null)
			return null;
		return self.currentDisplay[monitor];
	}

	
	public static class DisplayMode {

		public final int width;
		public final int height;
		public final int refresh;
		public final boolean fullScreen;
		
		public DisplayMode(int width, int height, int refresh, boolean fullScreen) {
			this.width = width;
			this.height = height;
			this.refresh = refresh;
			this.fullScreen = fullScreen;
		}
		
		@Override
		public String toString() {
			return width+"x"+height+"@"+refresh+"Hz";
		}
		
	}
	
}
