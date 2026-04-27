package snake2d;

import static org.lwjgl.opengl.GL11.GL_INVERT;
import static org.lwjgl.opengl.GL11.GL_KEEP;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.GL_SHORT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;
import static org.lwjgl.opengl.GL11.glColorMask;
import static org.lwjgl.opengl.GL11.glStencilFunc;
import static org.lwjgl.opengl.GL11.glStencilOp;
import static org.lwjgl.opengl.GL20.glUseProgram;

import snake2d.util.sprite.TextureCoords;

class VboStencilMaxSetter extends VboAbsExt{

	private final int[] fromStencil = new int[255];
	private final Shader shader;

	public VboStencilMaxSetter(SETTINGS sett) {
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
			GlHelper.enableDepthTest(false);
			GlHelper.setDepthTestAlways();
			glColorMask(false, false, false, false);
			shader.bind();
			int i = 0;
			vTo[current] = count;
			while (i <= current) {
				glStencilFunc(GL_LEQUAL, fromStencil[i], ~0); 
				glStencilOp(GL_KEEP, GL_KEEP ,GL_INVERT);
				
				flush(vFrom[i], vTo[i]);
				i++;
			}
			glUseProgram(0);
			glColorMask(true, true, true, true);
		}
		clear();
		
	}


	public void render(TextureCoords t, int x1, int y1, int x2, int y2, int stencil) {

		if (count >= MAX_ELEMENTS) {
			return;
		}
		
		
		
		buffer.putShort((short) x1).putShort((short) y1);
		buffer.putShort((short) x2).putShort((short) y2);
		buffer.putShort(t.x1).putShort(t.y1);
		buffer.putShort(t.x2).putShort(t.y2);
		buffer.put(Byte.MAX_VALUE);
		buffer.put(Byte.MAX_VALUE);
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
