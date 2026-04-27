package snake2d;

import static org.lwjgl.glfw.GLFW.glfwSetWindowIcon;

import org.lwjgl.glfw.GLFWImage;

import snake2d.util.file.SnakeImage;
import snake2d.util.sets.LinkedList;

public class _IconLoader {

	static void setIcon(long window, String path) {

		String[] extens = new String[] {
			"Icon16", "Icon32", "Icon48"
		};
		
		LinkedList<SnakeImage> all = new LinkedList<>();
		GLFWImage.Buffer icons = GLFWImage.malloc(extens.length);
		
		for (int i = 0; i < extens.length; i++) {
			SnakeImage im = new SnakeImage(path + extens[i] + ".png");
			all.add(im);
			icons.position(i);
			icons.width(im.width).height(im.height).pixels(im.data());
		}
		
		icons.position(0);
		
		glfwSetWindowIcon(window, icons);
		
		icons.free();
		
		for (SnakeImage im : all)
			im.dispose();

	}

}
