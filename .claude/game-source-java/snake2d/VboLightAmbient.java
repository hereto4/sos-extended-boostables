
package snake2d;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_SHORT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.nio.IntBuffer;

import org.lwjgl.opengl.GL11;

import snake2d.util.light.LIGHT_AMBIENT;

class VboLightAmbient extends VboAbsExt {

	private boolean specialLayer;
	private final Shader shader;
	private final IntBuffer sBuff;
	private final VboSorter sorter;

	VboLightAmbient(SETTINGS sett) {

		super(GL11.GL_TRIANGLES, 1024*2*2, 
				new VboAttribute(3, GL_FLOAT, false, 4), // direction/centre position 3*4
				new VboAttribute(2, GL_SHORT, false, 2), // coo 4
				new VboAttribute(3, GL_FLOAT, false, 4), // colour 3*4
				new VboAttribute(4, GL_UNSIGNED_BYTE, true, 1) // shaded + 3 padding
		);
		shader = new Shader(sett.getNativeWidth(), sett.getNativeHeight(), "LightAmbient", null, "LightAmbient");
		shader.setUniform1i("Tdiffuse", 2);
		shader.setUniform1i("Tnormal", 3);
		sBuff = buffer.asIntBuffer();
		sorter = new VboSorter(32*MAX_ELEMENTS);
		
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

		sorter.fill(sBuff);
		buffer.position(sBuff.position()*4);
		
		bind();
		upload();
		GlHelper.setBlendAdditative();
		GlHelper.enableDepthTest(true);
		GlHelper.setDepthTestLess();
		shader.bind();
		vTo[current] = count;
		for (int i = 0; i <= current; i++) {
			
			int fromI = vFrom[i];
			int toI = vTo[i];
			if (specialLayer && i == current) {
				GlHelper.Stencil.setLEQUALKeepOnFail(i);
			} else {
				GlHelper.Stencil.setEQUALKeepOnFail(i);
			}
			if (toI > fromI) {
				flush(fromI, toI);
			}
			
			
			
		}
		
//		int i = 0;
//		vTo[current] = count;
//		while (i <= current) {
//			if (specialLayer && i == current) {
//				GlHelper.Stencil.setLEQUALKeepOnFail(i);
//			} else {
//				GlHelper.Stencil.setEQUALKeepOnFail(i);
//			}
//			flush(vFrom[i], vTo[i]);
//			i++;
//		}
		glUseProgram(0);
		GlHelper.enableDepthTest(false);
		GlHelper.setBlendNormal();
		clear();

	}

	void render(LIGHT_AMBIENT l, int x1, int x2, int y1, int y2, byte depth) {

		sorter.add(current, Float.floatToRawIntBits(l.x()));
		sorter.add(current, Float.floatToRawIntBits(l.y()));
		sorter.add(current, Float.floatToRawIntBits(l.z()));
		sorter.add(current, ((y2) << 16) | ((x1 & 0x0FFFF)));
		sorter.add(current, Float.floatToRawIntBits((float) l.r()));
		sorter.add(current, Float.floatToRawIntBits((float) l.g()));
		sorter.add(current, Float.floatToRawIntBits((float) l.b()));
		sorter.add(current, (depth&0x0FF)|0xEFEFEF00);
		
		sorter.add(current, Float.floatToRawIntBits(l.x()));
		sorter.add(current, Float.floatToRawIntBits(l.y()));
		sorter.add(current, Float.floatToRawIntBits(l.z()));
		sorter.add(current, ((y2) << 16) | ((x2 & 0x0FFFF)));
		sorter.add(current, Float.floatToRawIntBits((float) l.r()));
		sorter.add(current, Float.floatToRawIntBits((float) l.g()));
		sorter.add(current, Float.floatToRawIntBits((float) l.b()));
		sorter.add(current, (depth&0x0FF)|0xEFEFEF00);
		
		sorter.add(current, Float.floatToRawIntBits(l.x()));
		sorter.add(current, Float.floatToRawIntBits(l.y()));
		sorter.add(current, Float.floatToRawIntBits(l.z()));
		sorter.add(current, ((y1) << 16) | ((x1 & 0x0FFFF)));
		sorter.add(current, Float.floatToRawIntBits((float) l.r()));
		sorter.add(current, Float.floatToRawIntBits((float) l.g()));
		sorter.add(current, Float.floatToRawIntBits((float) l.b()));
		sorter.add(current, (depth&0x0FF)|0xEFEFEF00);
		
		sorter.add(current, Float.floatToRawIntBits(l.x()));
		sorter.add(current, Float.floatToRawIntBits(l.y()));
		sorter.add(current, Float.floatToRawIntBits(l.z()));
		sorter.add(current, ((y1) << 16) | ((x2 & 0x0FFFF)));
		sorter.add(current, Float.floatToRawIntBits((float) l.r()));
		sorter.add(current, Float.floatToRawIntBits((float) l.g()));
		sorter.add(current, Float.floatToRawIntBits((float) l.b()));
		sorter.add(current, (depth&0x0FF)|0xEFEFEF00);

//		buffer.putFloat(l.x()).putFloat(l.y()).putFloat(l.z());
//		buffer.putShort((short) x1).putShort((short) y2);
//		buffer.putFloat((float) l.r()).putFloat((float) l.g()).putFloat((float) l.b());
//		buffer.put(depth);
//
//		buffer.put(Byte.MAX_VALUE).put(Byte.MAX_VALUE).put(Byte.MAX_VALUE);
//
//		// SE
//		buffer.putFloat(l.x()).putFloat(l.y()).putFloat(l.z());
//		buffer.putShort((short) x2).putShort((short) y2);
//		buffer.putFloat((float) l.r()).putFloat((float) l.g()).putFloat((float) l.b());
//		buffer.put(depth);
//
//		buffer.put(Byte.MAX_VALUE).put(Byte.MAX_VALUE).put(Byte.MAX_VALUE);
//
//		// NW
//		buffer.putFloat(l.x()).putFloat(l.y()).putFloat(l.z());
//		buffer.putShort((short) x1).putShort((short) y1);
//		buffer.putFloat((float) l.r()).putFloat((float) l.g()).putFloat((float) l.b());
//		buffer.put(depth);
//
//		buffer.put(Byte.MAX_VALUE).put(Byte.MAX_VALUE).put(Byte.MAX_VALUE);
//
//		// NE
//		buffer.putFloat(l.x()).putFloat(l.y()).putFloat(l.z());
//		buffer.putShort((short) x2).putShort((short) y1);
//		buffer.putFloat((float) l.r()).putFloat((float) l.g()).putFloat((float) l.b());
//		buffer.put(depth);
//
//		buffer.put(Byte.MAX_VALUE).put(Byte.MAX_VALUE).put(Byte.MAX_VALUE);

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
		sorter.clear();
		super.clear();
	}

}
