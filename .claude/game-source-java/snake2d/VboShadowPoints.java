package snake2d;

import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.GL_SHORT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;
import static org.lwjgl.opengl.GL11.glColorMask;
import static org.lwjgl.opengl.GL20.glUseProgram;

import snake2d.util.sprite.TextureCoords;

class VboShadowPoints extends VboAbsExt{

	private final int[] fromStencil = new int[255];
	private final Shader shader;

	public VboShadowPoints(SETTINGS sett) {
		super(GL_POINTS, 1<<16, 
			new VboAttribute(2, GL_SHORT, false, 2), // position upper left			4
			new VboAttribute(2, GL_SHORT, false, 2), // position lower right		4
			new VboAttribute(2, GL_UNSIGNED_SHORT, 2), // texture coords1			4
			new VboAttribute(2, GL_UNSIGNED_SHORT, 2), // texture coords2			4
			new VboAttribute(4, GL_UNSIGNED_BYTE, true, 1) //d + depth + 2padding	4
			); 
		this.shader = new Shader(sett.getNativeWidth(), sett.getNativeHeight(), "Shadow", "Shadow", "Shadow");
		shader.setUniform1i("sampler1", 0);
	}

	int setNewFinalOverride(int fromStencil) {
		vTo[current] = count;
		current++;
		vFrom[current] = count;
		this.fromStencil[current] = fromStencil;
		return current;

	}

	void flush() {
		if (count != 0) {

			
			bindAndUpload();
			GlHelper.enableDepthTest(true);
			GlHelper.setDepthTestAlways();
			glColorMask(false, false, false, false);
			shader.bind();
			int i = 0;
			vTo[current] = count;
			while (i <= current) {
				GlHelper.Stencil.setLESSKeepOnPass(fromStencil[i]);
				flush(vFrom[i], vTo[i]);
				i++;
			}
			glUseProgram(0);
			glColorMask(true, true, true, true);
			GlHelper.enableDepthTest(false);
		}
		clear();
		
	}


	public void render(TextureCoords t, int x1, int y1, int x2, int y2, int x3, int y3,
			int x4, int y4, byte d, byte depth) {

		if (count >= MAX_ELEMENTS) {
			return;
		}
		

		
		buffer.putShort((short) x1).putShort((short) y1);
		buffer.putShort((short) x3).putShort((short) y3);
		buffer.putShort(t.x1).putShort(t.y1);
		buffer.putShort(t.x2).putShort(t.y2);
		buffer.put(d);
		buffer.put(depth);
		buffer.put(Byte.MAX_VALUE);
		buffer.put(Byte.MAX_VALUE);
		count++;
	}
	
	@Override
	public void dis() {
		shader.dis();
		super.dis();
	}


}
