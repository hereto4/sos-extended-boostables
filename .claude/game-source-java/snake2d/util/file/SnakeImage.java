package snake2d.util.file;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;
import org.lwjgl.stb.STBImageResize;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryStack;

import snake2d.Errors;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;

public final class SnakeImage {

	private ByteBuffer image;
	public final int height;
	public final int width;
	public final ImageGraphics rgb = new ImageGraphics();
	public final String path;
	
	public SnakeImage(String path) {

		this.path = path;
		if (!new File(path).exists())
			throw new Errors.DataError("File doesn't exist", path);
		
		try (MemoryStack stack = MemoryStack.stackPush()) {

			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			IntBuffer comp = stack.mallocInt(1);

			image = STBImage.stbi_load(path, w, h, comp, 4);
			
//			if (comp.get() != 4)
//				throw new ErrorHandler.DataError("Failed to load a texture file!"
//						+ "not RGBA data", path);

			if (image == null)
				throw new Errors.DataError("Failed to load a texture file!"
						+ System.lineSeparator() + STBImage.stbi_failure_reason());

			width = w.get();
			height = h.get();

		}
		
	}
	
	public SnakeImage(Path path) {

		this.path = path.toString();
		if (!Files.exists(path))
			throw new Errors.DataError("File doesn't exist", this.path);
		
		try (MemoryStack stack = MemoryStack.stackPush()) {

			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			IntBuffer comp = stack.mallocInt(1);
			
			try {
				byte[] bs = Files.readAllBytes(path);
				ByteBuffer buff = BufferUtils.createByteBuffer(bs.length);
				for (int i = 0; i < bs.length; i++) {
					buff.put(bs[i]);
				}
				buff.flip();
				image = STBImage.stbi_load_from_memory(buff, w, h, comp, 4);
				
			} catch (IOException e) {
				e.printStackTrace();
				throw new Errors.DataError("Could not read file: ", this.path);
			}

			if (image == null)
				throw new Errors.DataError("Failed to load a texture file!"
						+ System.lineSeparator() + STBImage.stbi_failure_reason(), path);

			width = w.get();
			height = h.get();

		}
		
	}
	
	public static int height(Path path) {
		
		return dim(path).y();
	}
	
	public static COORDINATE dim(Path path) {
		
		if (!Files.exists(path))
			throw new Errors.DataError("File doesn't exist", path);
		
		try (MemoryStack stack = MemoryStack.stackPush()) {

			
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			IntBuffer comp = stack.mallocInt(1);
			
			try {
				byte[] bs = Files.readAllBytes(path);
				ByteBuffer buff = BufferUtils.createByteBuffer(bs.length);
				for (int i = 0; i < bs.length; i++) {
					buff.put(bs[i]);
				}
				buff.flip();
				ByteBuffer im = STBImage.stbi_load_from_memory(buff, w, h, comp, 4);
				
				if (im == null)
					throw new IOException();
				
				Coo coo = new Coo(w.get(), h.get());
				
				im.rewind();
				STBImage.stbi_image_free(im);
				im = null;
				return coo;
			} catch (IOException e) {
				e.printStackTrace();
				throw new Errors.DataError("Could not read file: ", path);
			}
			
			

		}
	}
	
	public SnakeImage(String path, int width, int height) throws IOException {
		this.path = path;
		if (!new File(path).exists())
			throw new Errors.DataError("File doesn't exist", path);
		
		try (MemoryStack stack = MemoryStack.stackPush()) {

			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);
			IntBuffer comp = stack.mallocInt(1);
			image = STBImage.stbi_load(path, w, h, comp, 4);
			

			if (image == null)
				throw new RuntimeException("Failed to load a texture file!"
						+ System.lineSeparator() + STBImage.stbi_failure_reason());

			this.width = w.get();
			this.height = h.get();
			
			if (this.width != width || this.height != height) {
				dispose();
				throw new IOException("Image has wrong dimentions. Resize to: " + width + "x" + height  + " " + path);
			}

		}
	}
	
	public SnakeImage(Path path, int width, int height) throws IOException {
		this(path);
		if (this.width != width || this.height != height) {
			dispose();
			throw new IOException("Image has wrong dimentions. Resize to: " + width + "x" + height  + " " + path);
		}
	}
	
	public SnakeImage(int width, int height){
		path = null;
		try {
			image = BufferUtils.createByteBuffer(width*height*4);
		}catch(OutOfMemoryError e) {
			e.printStackTrace();
			int s = width*height*4;
			throw new RuntimeException(s + " " + Runtime.getRuntime().totalMemory() + " " + Runtime.getRuntime().freeMemory());
			
		}
		
		this.width = width;
		this.height = height;
	}
	
	public ByteBuffer data() {
		image.rewind();
		return image;
	}
	
	public void dispose() {
		if (path == null) {
			return;
		}
		if (image != null) {
			image.rewind();
			STBImage.stbi_image_free(image);
			image = null;
		}
	}
	
	public void save(String path) {
		image.rewind();
		STBImageWrite.stbi_write_png(path, width, height, 4, image, 0);
	}
	
	public SnakeImage resized(int nwidth, int nheight) {
		
		SnakeImage nn = new SnakeImage(nwidth, nheight);
		image.rewind();
		nn.image.rewind();
		STBImageResize.stbir_resize_uint8_linear(image, width, height, 0, nn.image, nwidth, nheight, 0, 4);
		return nn;
		
	}
	
	
//	public void saveBMP(String path) {
//		image.rewind();
//		STBImageWrite.stbi_write_bmp(path, width, height, 4, image);
//	}
	
	public void saveJpg(String path) {
		image.rewind();
		STBImageWrite.stbi_write_jpg(path, width, height, 4, image, 90);
	}
	
	public final class ImageGraphics {
		
		private ImageGraphics() {
			
		}
		
		private void boundCheck(int x, int y) {
			if (x < 0 || y < 0 || x >= width || y >= height)
				throw new RuntimeException(x + " " +y  + "is out of bounds" + " " + width + " " + height);
		}
		
		public void set(int x, int y, int r, int g, int b, int a) {
			boundCheck(x, y);
			int i = 4*(x + y*width);
			image.position(i);
			image.put((byte)r).put((byte)g).put((byte)b).put((byte)a);
		}
		
		public void set(int x, int y, int c) {
			int r = (c >> 24) & 0x0FF;
			int g = (c >> 16) & 0x0FF;
			int b = (c >> 8) & 0x0FF;
			int a = (c) & 0x0FF;
			set(x, y, r, g, b, a);
		}
		
		public int get(int x, int y) {
			boundCheck(x, y);
			int i = 4*(x + y*width);
			image.position(i);
			int res = (image.get() & 0x0FF) << 24;
			res |= (image.get() & 0x0FF) << 16;
			res |= (image.get() & 0x0FF) << 8;
			res |= (image.get() & 0x0FF);
			return res;
		}
		
	}
}
