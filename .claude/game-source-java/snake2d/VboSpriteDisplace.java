package snake2d;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.GL_SHORT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;
import static org.lwjgl.opengl.GL20.glUseProgram;

import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;

class VboSpriteDisplace extends VboAbsExt{

	private final Shader shader;


	static VboSpriteDisplace getDeffered(SETTINGS sett) {
		Shader shader = new Shader(sett.getNativeWidth(), sett.getNativeHeight(), "Displace", "Displace", "Displace");
		shader.setUniform1i("sampler1", 0);
		shader.setUniform1i("sampler2", 1);
		return new VboSpriteDisplace(shader);
	}

	public VboSpriteDisplace(Shader shader) {
		super( GL_POINTS,
				1<<15,
				new VboAttribute(2, GL_SHORT, false, 2), // position upper left		4
				new VboAttribute(2, GL_SHORT, false, 2), // position lower right	4
				new VboAttribute(2, GL_FLOAT, false, 4), // texture coords1		4
				new VboAttribute(2, GL_FLOAT, false, 4), // texture coords2		4
				new VboAttribute(2, GL_UNSIGNED_SHORT, 2), // texture coords width	4
				
				new VboAttribute(4, GL_UNSIGNED_BYTE, true, 1), // color				4
				new VboAttribute(1, GL_FLOAT, false, 4) // scale
		);
		this.shader = shader;

	}

	int setNew() {
		vTo[current] = count;
		current++;
		vFrom[current] = count;
		return current;
	}

	void flush() {
		bindAndUpload();
		shader.bind();
		int i = 0;
		vTo[current] = count;
		while (i <= current) {
			if (vFrom[i] != vTo[i]) {
				GlHelper.Stencil.setEQUALKeepOnFail(i);
				flush(vFrom[i], vTo[i]);
			}
			i++;
		}
		clear();
		glUseProgram(0);
	}

	void render(float tx1, float ty1, float dx1, float dy1, int w, int h, double scale, int x1, int x2, int y1, int y2, COLOR color, OPACITY opacity) {
		
		if (count >= MAX_ELEMENTS) {
			return;
		}
		
		buffer.putShort((short) x1).putShort((short) y1);
		buffer.putShort((short) x2).putShort((short) y2);
		buffer.putFloat(tx1).putFloat(ty1);
		buffer.putFloat(dx1).putFloat(dy1);
		buffer.putShort((short) (w)).putShort((short) (h));
		buffer.put(color.red()).put(color.green()).put(color.blue()).put(opacity.get());
		buffer.putFloat((float) scale);

		count++;
	}

	@Override
	public void dis() {
		shader.dis();
		super.dis();
	}

}
