package settlement.misc;

import init.constant.C;
import settlement.main.SETT.SettResource;
import snake2d.CORE;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.DEG;
import snake2d.util.rnd.RND;

public class ParticleRenderer extends SettResource{

	private final static int MAX = 64;
	private final static float MAG = 16*C.SCALE;
	private final static float vel = 120f;
	private final static double iV = 1.0/(20.0*C.TILE_SIZE);
	
	private final float[] mags = new float[MAX];
	private final float[] dxs = new float[MAX];
	private final float[] dys = new float[MAX];
	private final COLOR color = new ColorImp(18,14,5);
	private boolean touched = false;
	
	
	public ParticleRenderer() {
		super("PART", false);
		for (int i = 0; i < MAX; i++) {
			mags[i] = RND.rFloat()*MAX;
			DEG.setRandom();
			dxs[i] = (float) DEG.getCurrentX();
			dys[i] = (float) DEG.getCurrentY();
		}
		
	}
	
	
	public void renderDust(int x, int y, double magnitude) {
		magnitude *= iV;
		int m = (int) (magnitude*MAX);
		
		if (m > MAX)
			m = MAX;
		if (m <= 0)
			return;
		color.bind();
		for (int i = 0; i < m; i++) {
			int dx = (int) (x + dxs[i]*mags[i]);
			int dy = (int) (y + dys[i]*mags[i]);
			CORE.renderer().renderParticle(dx, dy);
		}
		COLOR.unbind();
		touched = true;
	}
	
	@Override
	protected void postRender(float ds) {
		if (!touched)
			return;
		touched = false;
		for (int i = 0; i < MAX; i++) {
			mags[i] += ds*vel;
			if (mags[i] > MAG) {
				mags[i] -= MAG;
			}
		}
	}

}
