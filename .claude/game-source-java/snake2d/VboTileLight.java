package snake2d;

import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.GL_SHORT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL20.glUseProgram;

class VboTileLight extends VboAbsExt {

	public final static int MAX_ELEMENTS = 65536 / 4;
	private float[] lights = new float[255 * 7];
	private final float inv = 1f / 255f;
	private final Shader shader;
	private boolean specialLayer;

	private final int uTilt;
	private final int uColor;
	private final int uDepth;

	public VboTileLight(SETTINGS sett) {
		super(GL_POINTS, MAX_ELEMENTS, new VboAttribute(2, GL_SHORT, false, 2), // position upper left //4

				new VboAttribute(4, GL_UNSIGNED_BYTE, true, 1), // corner intensity //4
				new VboAttribute(2, GL_SHORT, false, 2) // dimension + 2 padding

		);

		shader = new Shader(sett.getNativeWidth(), sett.getNativeHeight(), "LightTile", "LightTile", "LightTile");

		shader.setUniform1i("Tdiffuse", 2);
		shader.setUniform1i("Tnormal", 3);

		uTilt = shader.getUniformLocation("v_tilt");
		uColor = shader.getUniformLocation("v_color");
		uDepth = shader.getUniformLocation("u_depth");
	}


	void upload(float r, float g, float b, float x, float y, float z, float depth) {
		shader.setUniform(uColor, r, g, b);
		shader.setUniform(uTilt, x, y, z);
		shader.setUniform(uDepth, depth);
	}

	void setNew() {
		if (specialLayer)
			return;
		vTo[current] = count;
		current++;
		vFrom[current] = count;
	}

	void setNewButKeepLight() {
		if (specialLayer)
			return;
		vTo[current] = count;
		current++;
		vFrom[current] = vFrom[current - 1];

	}

	void setNewFinal() {
//		if (specialLayer)
//			throw new RuntimeException("can't set final layer twice!");
		specialLayer = true;
		vTo[current] = count;
		current++;
		vFrom[current] = count;

	}

	void flush() {
		bindAndUpload();

		GlHelper.setBlendAdditative();
		GlHelper.enableDepthTest(true);
		GlHelper.setDepthTestLess();
		shader.bind();
		int i = 0;
		vTo[current] = count;
		while (i <= current) {
			if (vFrom[i] == vTo[i]) {
				i++;
				continue;
			}
			if (specialLayer && i == current) {
				GlHelper.Stencil.setLEQUALKeepOnFail(i);
			} else {
				GlHelper.Stencil.setEQUALKeepOnFail(i);
			}
			int k = i * 7;
			upload(lights[k], lights[k + 1], lights[k + 2], lights[k + 3], lights[k + 4], lights[k + 5],
					lights[k + 6]);
			flush(vFrom[i], vTo[i]);
			i++;
		}
		glUseProgram(0);
		GlHelper.enableDepthTest(false);
		GlHelper.setBlendNormal();
		clear();

	}

	@Override
	public void clear() {
		super.clear();
		specialLayer = false;
	}

	void render(int x1, int y1, int dim, byte nw, byte ne, byte se, byte sw) {

		if (count >= MAX_ELEMENTS) {
			return;
		}

		buffer.putShort((short) x1).putShort((short) y1);
		buffer.put(nw).put(ne).put(se).put(sw);
		buffer.putShort((short) dim);
		buffer.put(Byte.MAX_VALUE).put(Byte.MAX_VALUE);

		count++;
	}

	void setLight(float red, float green, float blue, float x, float y, float z, byte depth) {
		int i = current * 7;
		lights[i] = red;
		lights[i + 1] = green;
		lights[i + 2] = blue;
		lights[i + 3] = x;
		lights[i + 4] = y;
		lights[i + 5] = z;
		lights[i + 6] = (depth & 0x0FF) * inv;
	}

	@Override
	public void dis() {
		shader.dis();
		super.dis();
	}

}
