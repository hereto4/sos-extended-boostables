package snake2d;

import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.GL_SHORT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glPointSize;
import static org.lwjgl.opengl.GL20.glUseProgram;

import org.lwjgl.opengl.GL11;

import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;

class VboParticles extends VboAbsExt{

	private final byte byteZero = 0;
	private final byte byteFull = -1;
	private final int[] size = new int[255];
	private final Shader shader;
	
	static VboParticles getDebug(SETTINGS sett) {
		Shader shader = new Shader(sett.getNativeWidth(), sett.getNativeHeight(), "Particle_debug", null, "Particle_debug");
		return new VboParticles(shader);
	}

	static VboParticles getForTexture(int width, int height) {
		Shader shader = new Shader(width-0.5, height+0.5, "Particle_texture", null, "Particle_texture");
		return new VboParticles(shader);
	}
//
	static VboParticles getDeffered(SETTINGS sett) {
		Shader shader = new Shader(sett.getNativeWidth(), sett.getNativeHeight(), "Particle", null, "Particle");
		return new VboParticles(shader);
	}

	public VboParticles(Shader shader) {
		super(GL_POINTS, 1 << 17, 
				new VboAttribute(2, GL_SHORT, false, 2), // position		4
				new VboAttribute(4, GL_UNSIGNED_BYTE, true, 1), // normal	4
				new VboAttribute(4, GL_UNSIGNED_BYTE, true, 1));// color	4
		
		this.shader = shader;
		size[0] = 1;

	}

	void setNew(int pointSize) {
		vTo[current] = count;
		current++;
		vFrom[current] = count;
		size[current] = pointSize;
	}

	final void flush(int pointSize) {
		
		bindAndUpload();
		shader.bind();
		int i = 0;
		vTo[current] = count;
		while (i <= current) {
			if (vFrom[i] != vTo[i]) {
				GlHelper.Stencil.setLEQUALreplaceOnPass(i);
				flush(vFrom[i], vTo[i], size[i]);
			}
			i++;
		}
		clear(pointSize);
		glUseProgram(0); // puts an end to the goddamn nvidia errors
		
	}

	public int count() {
		return buffer.position();
	}
	
	private void flush(int from, int to, int size) {
		if (size < 1)
			throw new RuntimeException();
		glPointSize(size);
		glDrawElements(GL11.GL_POINTS, (to - from), GL11.GL_UNSIGNED_INT, from * 4);
	}


	public void clear(int pointSize) {
		super.clear();
		size[current] = pointSize;
	}

	public void render(short x, short y, byte nX, byte nY, byte nZ, byte nA, COLOR color, OPACITY opacity) {
		if (count >= MAX_ELEMENTS) {
			return;
		}

		buffer.putShort(x).putShort(y);
		buffer.put(nX).put(nY).put(nZ).put(nA);
		buffer.put(color.red()).put(color.green()).put(color.blue()).put(opacity.get());

		count++;
		
	}

	public void render(short x, short y, byte red, byte green, byte blue) {

		if (count >= MAX_ELEMENTS) {
			return;
		}

		buffer.putShort(x).putShort(y);
		buffer.put(byteZero).put(byteZero).put(byteZero).put(byteZero);
		buffer.put(red).put(green).put(blue).put(byteFull);

		count++;
	}



	@Override
	public void dis() {
		shader.dis();
		super.dis();
	}
	
	public void dis(boolean leaveIndexArrayTheFuckAlone) {
		shader.dis();
		super.dis();
	}

}
