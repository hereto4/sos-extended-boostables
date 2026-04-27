package snake2d;

import static org.lwjgl.opengl.GL11.GL_POINTS;
import static org.lwjgl.opengl.GL11.GL_SHORT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;
import static org.lwjgl.opengl.GL20.glUseProgram;

import java.nio.IntBuffer;

import snake2d.VboSorter.Counts;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.sprite.TextureCoords;

final class VboSprite extends VboAbs{


	private final Shader shader;
	private final IntBuffer sBuff;
	private final VboSorter sorter;
	private int layer = 0;

	static VboSprite getDebug(SETTINGS sett) {
		Shader shader = new Shader(sett.getNativeWidth(), sett.getNativeHeight(), "SpritePoint", "SpritePoint", "SpritePoint_debug");
		shader.setUniform1i("u_texture", 0);
		return new VboSprite(shader);
	}

	static VboSprite getDeffered(SETTINGS sett) {
		Shader shader = new Shader(sett.getNativeWidth(), sett.getNativeHeight(), "SpritePoint", "SpritePoint", "SpritePoint");
		shader.setUniform1i("sampler1", 0);
		shader.setUniform1i("sampler2", 1);
		return new VboSprite(shader);
	}

	public VboSprite(Shader shader) {
		super( GL_POINTS,
				1<<17,
				new VboAttribute(2, GL_SHORT, false, 2), // position upper left
				new VboAttribute(2, GL_SHORT, false, 2), // position lower right
				new VboAttribute(2, GL_UNSIGNED_SHORT, 2), // texture coords1
				new VboAttribute(2, GL_UNSIGNED_SHORT, 2), // texture coords2
				new VboAttribute(2, GL_UNSIGNED_SHORT, 2), // texture coords width
				new VboAttribute(4, GL_UNSIGNED_BYTE, true, 1) // color
		);
		sorter = new VboSorter(MAX_ELEMENTS*6);
		this.shader = shader;
		sBuff = buffer.asIntBuffer();
	}

	int setNew() {
		layer++;
		return layer;
	}

	void flush() {
		
		bind();
		shader.bind();
		sBuff.position(0);
		Counts ss = sorter.fill(sBuff);
		buffer.position(sBuff.position()*4);
		upload();
		
		for (int i = 0; i <= layer; i++) {
			
			int fromI = ss.from[i];
			int toI = ss.to[i];
			if (toI > fromI) {
				GlHelper.Stencil.setLEQUALreplaceOnPass(i);
				flush(fromI/6, toI/6);
			}
			
			
			
		}

		clear();
		glUseProgram(0);
	}
	
	@Override
	public void clear() {
		sorter.clear();
		layer = 0;
		super.clear();
	}

	final void render(TextureCoords t, int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4, COLOR color,
			OPACITY opacity) {
		
		render(t, t, x1, x2, y1, y2, color, opacity);
	}
	
	final void render(TextureCoords t, TextureCoords to, int x1, int x2, int y1, int y2, COLOR color, OPACITY opacity) {
		VboSorter sorter = this.sorter;
		sorter.add(layer, ((y1) << 16) | ((x1 & 0x0FFFF)));
		sorter.add(layer, ((y2) << 16) | ((x2 & 0x0FFFF)));
		sorter.add(layer, ((t.y1) << 16) | ((t.x1)));
		sorter.add(layer, ((to.y1) << 16) | ((to.x1)));
		sorter.add(layer, ((t.y2-t.y1) << 16) | ((t.x2-t.x1)));
		sorter.add(layer, (((opacity.get()) << 24) | ((color.blue()&0x0FF) << 16) | ((color.green()&0x0FF) <<8) | ((color.red()&0x0FF))));

	}

	@Override
	public void dis() {
		shader.dis();
		super.dis();
	}

}
