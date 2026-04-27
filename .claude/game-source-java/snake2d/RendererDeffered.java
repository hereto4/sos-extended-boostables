package snake2d;

import static org.lwjgl.opengl.GL20.glUseProgram;

import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.light.LIGHT_AMBIENT;
import snake2d.util.light.LIGHT_POINT;
import snake2d.util.sprite.TextureCoords;

class RendererDeffered extends Renderer{

	private final VboSpriteOpti vboTextured;
	private final VboShadowPoints vboShadows;
	private final VboLightAmbient vboLightAmbient;
	private final VboTileLight vboTileLight;
	private final VboLightPoint vboLightPoint;
	private final VboParticles vboParticles;
	private final _FBODeffered fbo;
	private final VboSpriteDisplace vboDisplace2;
	private final VboLightPointUni vboLightPointUni;
	private final VboStencilMaxSetter depth;
	private final boolean debug;
//	private final VboSpriteTiles vboTiles;
//	private final VboSpriteTiles vboTilesO;
	
	public RendererDeffered(SETTINGS sett, int pointSize) {
		super(pointSize);
		GlHelper.checkErrors();
		Printer.ln("SHADERS: ");
		vboTextured = VboSpriteOpti.getDeffered(sett);
		vboShadows = new VboShadowPoints(sett);
		vboLightAmbient = new VboLightAmbient(sett);
		vboLightPoint = new VboLightPoint(sett);
		vboParticles = VboParticles.getDeffered(sett);
		vboTileLight = new VboTileLight(sett);
		vboDisplace2 = VboSpriteDisplace.getDeffered(sett);
		vboLightPointUni = new VboLightPointUni(sett);
		depth = new VboStencilMaxSetter(sett);
//		vboTiles = VboSpriteTiles.getDeffered(sett, false);
//		vboTilesO = VboSpriteTiles.getDeffered(sett, true);
		fbo = new _FBODeffered(sett);
		debug = sett.debugMode();
		GlHelper.Stencil.enable(true);
		GlHelper.checkErrors();
		Printer.fin();
		
	}
	
	@Override
	void dis() {
		GlHelper.checkErrors();
		vboTextured.dis();
		GlHelper.checkErrors();
		vboShadows.dis();
		GlHelper.checkErrors();
		fbo.dis();
		GlHelper.checkErrors();
		vboLightAmbient.dis();
		GlHelper.checkErrors();
		vboLightPoint.dis();
		GlHelper.checkErrors();
		vboParticles.dis();
		GlHelper.checkErrors();
		vboTileLight.dis();
		GlHelper.checkErrors();
		vboDisplace2.dis();
		GlHelper.checkErrors();
		vboLightPointUni.dis();
		GlHelper.checkErrors();
		depth.dis();
		GlHelper.checkErrors();
//		vboTiles.dis();
//		vboTilesO.dis();
		ElementArrays.dispose();
		GlHelper.checkErrors();
	}

	@Override
	protected int pnewLayer(boolean keepLights, int pointSize){
		if (keepLights) {
			vboLightAmbient.setNewButKeepLight();
			vboLightPoint.setNewButKeepLight();
			vboTileLight.setNewButKeepLight();
			vboLightPointUni.setNewButKeepLight();
		}else {
			vboLightAmbient.setNew();
			vboLightPoint.setNew();
			vboTileLight.setNew();
			vboLightPointUni.setNew();
		}
		vboParticles.setNew(pointSize);
		int i = vboTextured.setNew();
//		vboTiles.setNew();
//		vboTilesO.setNew();
		vboDisplace2.setNew();
		vboShadows.setNewFinalOverride(i);
		depth.setNewFinalOverride(i);
		return i;
		
	}
	
	@Override
	protected int pnewFinalLightLayer(int pointSize) {
		vboLightAmbient.setNewFinal();
		vboLightPoint.setNewFinal();
		vboTileLight.setNewFinal();
		vboLightPointUni.setNewFinal();
		vboParticles.setNew(pointSize);

		int i = vboTextured.setNew();
//		vboTiles.setNew();
//		vboTilesO.setNew();
		vboDisplace2.setNew();
		vboShadows.setNewFinalOverride(i);
		depth.setNewFinalOverride(i);
		return i;
	}
	
	@Override
	protected void pclear(int pointSize) {
		vboTextured.clear();
		vboShadows.clear();
		vboLightAmbient.clear();
		vboLightPoint.clear();
		vboTileLight.clear();
		vboParticles.clear(pointSize);
		vboDisplace2.clear();
		vboLightPointUni.clear();
		depth.clear();
//		vboTiles.clear();
//		vboTilesO.clear();
	}
	
	@Override
	public final void renderShadow(TextureCoords t, int x1, int y1, int x2, int y2, int x3, int y3, int x4, int y4
			, byte d, byte depth) {
		vboShadows.render(t, x1, y1, x2, y2, x3, y3, x4, y4, d, depth);
		
	}
	
	@Override
	public final void renderSprite(TextureCoords t, TextureCoords to, int x1, int x2, int y1, int y2, COLOR color,
			OPACITY opacity) {
		vboTextured.render(t, to, x1, x2, y1, y2, color, opacity);
		
	}
	
	@Override
	public final void registerLight(LIGHT_POINT l, float x, float y, float z, int radius,
			int x1, int x2, int y1, int y2, byte ne, byte se, byte sw, byte nw, byte depth) {
		vboLightPoint.render(l, x, y, z, radius, x1, x2, y1, y2, ne, se, sw, nw, depth);
	}
	
	@Override
	public final void registerAmbient(LIGHT_AMBIENT l, int x1, int x2, int y1, int y2, byte depth) {
		vboLightAmbient.render(l, x1, x2, y1, y2, depth);
		
	}
	
	@Override
	public final void renderParticle(short x, short y, byte nx, byte ny, byte nz, byte nA, COLOR color, OPACITY opacity) {
		vboParticles.render(x, y, nx, ny, nz, nA, color, opacity);
	}

	private int di = 0;
	
	@Override
	public void pflush(int pointSize) {
		
		debug();
		fbo.bindDiffAndNorForTarget();
		debug();
		
		debug();
		vboParticles.flush(pointSize);
		debug();
//		vboTilesO.flush();
//		vboTiles.flush();
		debug();
		vboTextured.flush();
		debug();
		vboDisplace2.flush();
		debug();
		depth.flush();
		vboShadows.flush();
		
		//vboShadows.clear();
		debug();
		
		debug();
		fbo.bindLightTextureForTarget();
		debug();
		vboLightAmbient.flush();
		debug();
		vboLightPoint.flush();
		debug();
		vboLightPointUni.flush();
		debug();
		vboTileLight.flush();
		debug();
		//last fixes
		fbo.blitTexture();
		debug();
		glUseProgram(0); // puts an end to the goddamn nvidia errors
		
		
	}
	
	private void debug() {
		if (debug && di++ >= 1000) {
			di = 0;
			GlHelper.checkErrors();
		}
	}

	@Override
	public void renderTilelight(int x1, int y1, int dim, byte nw, byte ne, byte se, byte sw) {
		vboTileLight.render(x1, y1, dim, nw,ne,se,sw);
	}

	@Override
	public void setTileLight(LIGHT_AMBIENT l, byte depth) {
		vboTileLight.setLight((float)l.r(), (float)l.g(), (float)l.b(), l.x(), l.y(), l.z(), depth);
	}

	@Override
	public void renderPointlight(int x, int y, int z, int radius) {
		vboLightPointUni.render((short)x, (short)y, (short)z, (short)radius);
		
	}

	@Override
	public void setPointLight(LIGHT_POINT l, byte depth) {
		vboLightPointUni.setLight(l.getRadius(), l.getRed(), l.getGreen(), l.getBlue(), l.getFalloff(), depth);
	}

	@Override
	public void renderDisplace(float tx1, float ty1, float dx1, float dy1, int w, int h, double scale, int x1, int x2,
			int y1, int y2, COLOR color, OPACITY opacity) {
		vboDisplace2.render(tx1, ty1, dx1, dy1, w, h, scale, x1, x2, y1, y2, color, opacity);
		
		
	}

	@Override
	public void psetMaxDepth(int x1, int x2, int y1, int y2, TextureCoords stencil, int depth) {
		this.depth.render(stencil, x1, y1, x2, y2, depth);
		
	}

	
	
}
