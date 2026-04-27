package snake2d;

import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_VISIBLE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwDefaultWindowHints;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.opengl.GL11.GL_RENDERER;
import static org.lwjgl.opengl.GL11.GL_VENDOR;
import static org.lwjgl.opengl.GL11.GL_VERSION;
import static org.lwjgl.opengl.GL11.glGetString;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.io.File;
import java.util.List;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALUtil;
import org.lwjgl.opengl.GL;

import snake2d.Displays.DisplayMode;
import snake2d.util.file.Json;
import snake2d.util.file.JsonE;
import snake2d.util.process.Proccesser;

class CORE_INFO{

	public String error = null;
	public String SGL_VENDOR;
	public String SGL_RENDERER;
	public String SGL_VERSION;
	public int monitors = 0;
	public DisplayMode[][] displays = new DisplayMode[0][0];
	public DisplayMode[] currentdisplays = new DisplayMode[0];
	public String[] audioDevices;
	
	public static CORE_INFO cre2ate() {
		Process p = Proccesser.executeLwjgl(CORE_INFO.class, new String[] {}, new String[] {}, new String[] {});
		while(p.isAlive())
			;
	
		return new CORE_INFO( new Json(new File("Coreinfo.txt").toPath()));
	}
	
	public static void main(String[] args) {
		
		CORE_INFO f = new CORE_INFO();
		JsonE j = new JsonE();
		j.addString("ERROR", f.error);
		j.addString("SGL_VENDOR", f.SGL_VENDOR);
		j.addString("SGL_RENDERER", f.SGL_RENDERER);
		j.addString("SGL_VERSION", f.SGL_VERSION);
		j.add("MONITORS", f.monitors);
		
		{
			JsonE[] displays = new JsonE[f.displays.length];
			int i = 0;
			for (DisplayMode[] disps : f.displays) {
				
				int k = 0;
				JsonE[] res = new JsonE[disps.length];
				
				for (DisplayMode disp : disps) {
					res[k] = new JsonE();
					res[k].add("WI", disp.width);
					res[k].add("HI", disp.height);
					res[k].add("HZ", disp.refresh);
					k++;
				}
				
				displays[i] = new JsonE();
				displays[i].add("AVAILABLE", res);
				i++;
				
			}
			j.add("DISPLAYS", displays);
		}
		{
			JsonE[] displays = new JsonE[f.currentdisplays.length];
			for (int i = 0; i < displays.length; i++) {
				displays[i] = new JsonE();
				displays[i].add("WI", f.currentdisplays[i].width);
				displays[i].add("HI", f.currentdisplays[i].height);
				displays[i].add("HZ", f.currentdisplays[i].refresh);
			}
			j.add("CURRENT", displays);
		}
		
		j.addStrings("AUDIO_DEVICES", f.audioDevices);

		j.save(new File("").toPath().resolve("Coreinfo.txt"));
	}
	
	private CORE_INFO(Json json) {
		
		error = json.text("ERROR");
		SGL_VENDOR = json.text("SGL_VENDOR");
		SGL_RENDERER = json.text("SGL_RENDERER");
		SGL_VERSION = json.text("SGL_VERSION");
		monitors = json.i("MONITORS");
		Json[] displays = json.jsons("DISPLAYS");
		this.displays = new DisplayMode[displays.length][];
		int i = 0;
		for (Json j: displays) {
			Json[] modes = j.jsons("AVAILABLE");
			
			this.displays[i] = new DisplayMode[modes.length];
			int k = 0;
			for (Json m : modes) {
				this.displays[i][k++] = new DisplayMode(m.i("WI"), m.i("HI"), m.i("HZ"), false);
			}
			i++;
		}
		{
			Json[] ss = json.jsons("CURRENT");
			currentdisplays = new DisplayMode[ss.length];
			for (int k = 0; k < displays.length; k++) {
				currentdisplays[k] = new DisplayMode(ss[k].i("WI"), ss[k].i("HI"), ss[k].i("HZ"), false);
			}
		}
		audioDevices = json.texts("AUDIO_DEVICES");
		
	}
	
	public CORE_INFO() {
		
		
		try {
			// Setup an error callback. The default implementation
			// will print the error message in System.err.
			GLFWErrorCallback.createPrint(System.err).set();

			// Initialize GLFW. Most GLFW functions will not work before doing this.
			if (!glfwInit()) {
				error = "Unable to initialize GLFW";
				return;
			}
			

			// Configure GLFW
			glfwDefaultWindowHints(); // optional, the current window hints are already the default
			glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation

			// Create the window
			long window = NULL;
			window = glfwCreateWindow(300, 300, "test", NULL, NULL);
			
			if (window == NULL) {
				error = "Unable to create window";
				return;
			}

			// Make the OpenGL context current
			glfwMakeContextCurrent(window);

			GL.createCapabilities();
			
			new Displays();
			
			monitors = Displays.monitors();
			
			displays = new DisplayMode[monitors][];
			
			for (int i = 0; i < displays.length; i++) {
				displays[i] = new DisplayMode[Displays.available(i).size()];
				for (int k = 0; k < displays[i].length; k++)
					displays[i][k] = Displays.available(i).get(k);
			}
			
			currentdisplays = new DisplayMode[monitors];
			for (int i = 0; i < monitors; i++) {
				currentdisplays[i] = Displays.current(i);
			}
			
			SGL_VENDOR = glGetString(GL_VENDOR);
			SGL_RENDERER = glGetString(GL_RENDERER);
			SGL_VERSION = glGetString(GL_VERSION);
			
			glfwTerminate();
			glfwSetErrorCallback(null).free();
		}catch(Exception e) {
			e.printStackTrace(System.err);
			error = e.getMessage();
		}
		
		List<String> ss = ALUtil.getStringList(NULL, ALC11.ALC_ALL_DEVICES_SPECIFIER);
		if (ss.size() == 0)
			error = "No OpenAL device could be found. Try enabling sound and or / plug in/out speakers/earphones or restart your computer.";
		
		audioDevices = new String[ss.size()];
		for (int i = 0; i < ss.size(); i++)
			audioDevices[i] = ss.get(i);
	}

	
}
