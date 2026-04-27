package snake2d;

import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.light.LIGHT_AMBIENT;
import snake2d.util.light.LIGHT_POINT;
import snake2d.util.sprite.TextureCoords;


class RendererDebug extends Renderer{

	private final VboSpriteOpti vbo;
	private final VboParticles vboParticles;
	
	private final _FBODebug fbo;
	private final boolean debug;
	
	public RendererDebug(SETTINGS sett, int pointSize) {
		super(pointSize);
		vbo = VboSpriteOpti.getDebug(sett);
		fbo = new _FBODebug(sett);
		vboParticles = VboParticles.getDebug(sett);
		GlHelper.enableDepthTest(false);
		GlHelper.checkErrors();
		this.debug = sett.debugMode();
	}

	@Override
	public void dis() {
		GlHelper.checkErrors();
		vbo.dis();
		GlHelper.checkErrors();
		vboParticles.dis();
		GlHelper.checkErrors();
		ElementArrays.dispose();
		GlHelper.checkErrors();
	}

	@Override
	public int pnewLayer(boolean keepLights, int pointSize){
		vboParticles.setNew(pointSize);
		return vbo.setNew();
	}

	@Override
	public int pnewFinalLightLayer(int pointSize) {
		vboParticles.setNew(pointSize);
		return vbo.setNew();
		
	}
	
	@Override
	public void renderParticle(short x, short y, byte nx, byte ny, byte nz, byte nA, COLOR color, OPACITY opacity) {
		vboParticles.render(x, y, nx, ny, nz, nA, color, opacity);
		
	}
	
	@Override
	public void pclear(int pSize) {
		vbo.clear();
		vboParticles.clear(pSize);
	}
	

	@Override
	public void renderSprite(TextureCoords t, TextureCoords to, int x1, int x2, int y1, int y2, COLOR color,
			OPACITY opacity) {
		vbo.render(t, to, x1, x2, y1, y2, color, opacity);
		
	}

	@Override
	public void renderShadow(TextureCoords t, int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4
			, byte d, byte depth) {
	}


	@Override
	public void pflush(int pSize) {
		
		debug();
		fbo.bindAndClear();
		debug();
		GlHelper.enableDepthTest(false);
		debug();
		vboParticles.flush(pSize);
		debug();
		vbo.flush();
		debug();
//		GlHelper.bindNormalFrameBuffer();
//		
		fbo.blitTexture();
		debug();
	}
	
	private void debug() {
		if (debug)
			GlHelper.checkErrors();
	}

	@Override
	public void registerLight(LIGHT_POINT l, float x, float y, float z, int radius,
			int x1, int x2, int y1, int y2, byte ne, byte se, byte sw, byte nw, byte depth) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void registerAmbient(LIGHT_AMBIENT l, int x1, int x2, int y1, int y2, byte depth) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void renderTilelight(int x1, int y1, int dim, byte nw, byte ne, byte se, byte sw) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setTileLight(LIGHT_AMBIENT l, byte depth) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void renderPointlight(int x, int y, int z, int radius) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPointLight(LIGHT_POINT l, byte depth) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void renderDisplace(float tx1, float ty1, float dx1, float dy1, int w, int h, double scale, int x1, int x2,
			int y1, int y2, COLOR color, OPACITY opacity) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void psetMaxDepth(int x1, int x2, int y1, int y2, TextureCoords stencil, int depth) {
		// TODO Auto-generated method stub
		
	}

}
