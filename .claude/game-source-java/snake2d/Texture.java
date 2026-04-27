
package snake2d;

import static org.lwjgl.opengl.GL11.GL_LINEAR;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_RGBA8;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glTexSubImage2D;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL31.GL_TEXTURE_RECTANGLE;

import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;

import snake2d.util.file.SnakeImage;

class Texture {
	
	final int id;
	final int width;
	final int height;
	
	private boolean disposed = false;
	
	private static int MAX_SIZE = 0x04000;
	private final int ACTIVE_TEXTURE;

	public static Texture normal(SnakeImage i, boolean pixelated) {
		return new Texture(i, pixelated, GL_TEXTURE1);
	}
	
	Texture(SnakeImage i, boolean pixelated){
		this(i, pixelated, GL_TEXTURE0);
	}
	
	Texture(SnakeImage i, boolean pixelated, int ACTIVE_TEXTURE){
		
		GlHelper.checkErrors();
		this.ACTIVE_TEXTURE = ACTIVE_TEXTURE;
		width = i.width;
		height = i.height;
		
		if (width > MAX_SIZE || height > MAX_SIZE)
			throw new RuntimeException();
		
		id = glGenTextures();
		
		glActiveTexture(ACTIVE_TEXTURE);
		glBindTexture(GL_TEXTURE_RECTANGLE, id);
		
		//some strange filters. Experiment!
		glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_RECTANGLE, GL_TEXTURE_MAG_FILTER, pixelated ? GL_NEAREST : GL_LINEAR);
		glTexImage2D(GL_TEXTURE_RECTANGLE, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, i.data());
		
		int e = GL11.glGetError();
		if (e != GL_NO_ERROR) {
			GlHelper.diagnozeMem();
			throw new RuntimeException("Texture Error " + width + " " + height + " " + e);
		}
		
		i.dispose();
		GlHelper.checkErrors();

	}
	
	void bind(){
		if (disposed)
			throw new IllegalStateException("trying to bind a texture that was disposed");
		else
			glActiveTexture(ACTIVE_TEXTURE);
			glBindTexture(GL_TEXTURE_RECTANGLE, id);
	}
	
	void dis() {
		GlHelper.checkErrors();
		glActiveTexture(ACTIVE_TEXTURE);
		glDeleteTextures(id);
		GlHelper.checkErrors();
		disposed = true;
	}
	
	public void uploadPixels(int px, int width, int py, int height, ByteBuffer pixels){
		glActiveTexture(ACTIVE_TEXTURE);
		glTexSubImage2D(GL_TEXTURE_RECTANGLE, 0, px, py, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
	}
	
	public void uploadPixel(int px, int py, ByteBuffer pixel){
		glActiveTexture(ACTIVE_TEXTURE);
		glTexSubImage2D(GL_TEXTURE_RECTANGLE, 0, px, py, 1, 1, GL_RGBA, GL_UNSIGNED_BYTE, pixel);
	}
	
	public int getWidth(){return width;}
	public int getHeight(){return height;}
	
}