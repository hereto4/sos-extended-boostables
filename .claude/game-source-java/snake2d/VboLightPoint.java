
package snake2d;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.GL_SHORT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL20.glUseProgram;

import snake2d.util.light.LIGHT_POINT;

class VboLightPoint extends VboAbsExt {

	private boolean specialLayer;
	private final Shader shader;

	VboLightPoint(SETTINGS sett) {

		super(GL_POINTS, 10000 * 3, new VboAttribute(3, GL_FLOAT, false, 4), // direction/centre position 3*4
				new VboAttribute(2, GL_SHORT, false, 2), // coo 4
				new VboAttribute(2, GL_SHORT, false, 2), // coo2 4
				new VboAttribute(4, GL_FLOAT, false, 4), // colour 4*4
				new VboAttribute(4, GL_UNSIGNED_BYTE, true, 1), // coo intensity 4
				new VboAttribute(1, GL_FLOAT, false, 4), // radius 4
				new VboAttribute(4, GL_UNSIGNED_BYTE, true, 1) // depth + 3 padding 4
		);
		shader = new Shader(sett.getNativeWidth(), sett.getNativeHeight(), "LightPoint", "LightPoint", "LightPoint");

		shader.setUniform1i("Tdiffuse", 2);
		shader.setUniform1i("Tnormal", 3);
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
			if (specialLayer && i == current) {
				GlHelper.Stencil.setLEQUALKeepOnFail(i);
			} else {
				GlHelper.Stencil.setEQUALKeepOnFail(i);
			}
			flush(vFrom[i], vTo[i]);
			i++;
		}
		glUseProgram(0);
		GlHelper.enableDepthTest(false);
		GlHelper.setBlendNormal();
		clear();

	}

	void render(LIGHT_POINT l, float x, float y, float z, int radius, int x1, int x2, int y1, int y2, byte ne, byte se,
			byte sw, byte nw, byte depth) {

		if (count >= MAX_ELEMENTS) {
			return;
		}

		float d = l.getRadius();
		d /= radius;

		buffer.putFloat(x).putFloat(y).putFloat(l.cz());
		buffer.putShort((short) x1).putShort((short) y1).putShort((short) x2).putShort((short) y2);

		buffer.putFloat(l.getRed()).putFloat(l.getGreen()).putFloat(l.getBlue()).putFloat(l.getFalloff() * d);
		buffer.put(nw).put(ne).put(se).put(sw);
		buffer.putFloat(radius);
		buffer.put(depth);
		buffer.put(Byte.MAX_VALUE).put(Byte.MAX_VALUE).put(Byte.MAX_VALUE);

		count++;
	}

	@Override
	public void dis() {
		shader.dis();
		super.dis();
	}

	@Override
	public void clear() {
		current = 0;
		specialLayer = false;
		super.clear();
	}

}
