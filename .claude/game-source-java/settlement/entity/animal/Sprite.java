package settlement.entity.animal;

import game.GAME;
import init.constant.C;
import init.resources.RESOURCE;
import settlement.main.SETT;
import snake2d.Renderer;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.sprite.TILE_SHEET;
import util.rendering.ShadowBatch;



abstract class Sprite {
	
	final static int NR = 8;
	public final static int bstanding = 0;
	public final static int bwalk1 = 1 * NR;
	public final static int bwalk2 = 2 * NR;
	public final static int standing = 3 * NR;
	public final static int walk1 = 4 * NR;
	public final static int walk2 = 5 * NR;
	public final static int laying = 6 * NR;
	public final static int eating = 7 * NR;
	public final static int bodypart1 = 8 * NR;
	public final static int bodypart2 = 9 * NR;
	public final static int rotten = 10 * NR;
	public final static int bones = 11 * NR;

	public final static int[] WATER = new int[]{0,1*NR,2*NR,3*NR};
	private static TILE_SHEET water(){
		return SETT.ANIMALS().sprites.texture_water.get(0);
	}
	
	public final static int[] BLOOD = new int[]{0,1*NR,2*NR,3*NR,4*NR};
	static TILE_SHEET blood(){
		return SETT.ANIMALS().sprites.texture_blood;
	}
	
	private Sprite(float h){
		this.height = h;
	}
	
	final float height;
	protected abstract int getDir(Animal a, float ds);
	protected abstract int getRow(Animal a, float ds);
	
	static final Sprite STAND_STILL = new Sprite(1f) {
		@Override
		public int getDir(Animal a, float ds) {
			return a.speed.dir().id();
		}

		@Override
		protected int getRow(Animal a, float ds) {
			if (a.isBaby())
				return bstanding;
			return standing;
		}
	};
	static final Sprite MOVE = new Sprite(1f) {
		private final int[] rows = new int[]{standing,walk1,walk2};
		private final float fps = 25;
		@Override
		public int getDir(Animal a, float ds) {
			return a.speed.dir().id();
		}
		@Override
		protected int getRow(Animal a, float ds) {
			if (a.speed.magnitude() == 0)
				if (a.isBaby())
					return bstanding;
				else
					return standing;
			float t = a.spriteTimer += (fps*ds*a.speed.magnitudeRelative());
			if (t >= rows.length){
				t = 0;
				a.spriteTimer = 0;
			}
			if (a.isBaby())
				return rows[(int) t]-standing;
			return rows[(int) t];
		}
	};
	static final Sprite LAYING_STILL = new Sprite(0.5f) {
		@Override
		public int getDir(Animal a, float ds) {
			return a.speed.dir().id();
		}
		@Override
		protected int getRow(Animal a, float ds) {
			if (a.isBaby())
				return bstanding;
			return laying;
			
		}
	};
	static final Sprite EATING = new Sprite(1f) {
		private final int[] st = new int[]{standing, eating};
		private final float fps = 8;
		@Override
		public int getDir(Animal a, float ds) {
			return a.speed.dir().id();
		}
		@Override
		protected int getRow(Animal a, float ds) {
			float t = a.spriteTimer += (fps*ds);
			if (t >= st.length){
				t = 0;
				a.spriteTimer = 0;
			}
			if (a.isBaby())
				return bstanding;
			return st[(int) t];
		}
	};
	static final Sprite LAYING_SPIN = new Sprite(0.5f) {
		@Override
		public int getDir(Animal a, float ds) {
			int t = (int) (a.spriteTimer += (ds*25));
			if (t > 8){
				a.spriteTimer = 0;
				t = 0;
			}
			return t;
		}

		@Override
		protected int getRow(Animal a, float ds) {
			if (a.isBaby())
				return bstanding;
			return laying;
		}
	};
	
	protected void render(Animal a, boolean hovered, Renderer r, ShadowBatch s, float ds, int offsetX, int offsetY){
		
		int x = a.body().x1()+offsetX - a.species().spriteOff();
		int y = a.body().y1()+offsetY - a.species().spriteOff();
		int nr = 0;
		
		if (hovered){
			COLOR.WHITE2WHITE.bind();
		}else{
			a.color.bind();
		}
		
		int row = getRow(a, ds);
		int dir = getDir(a, ds);
		
		TILE_SHEET sheet = a.species().sheet;
		
		nr+= row + dir;
		sheet.render(r, nr, x, y);
		COLOR.unbind();
		
		
		if (a.damage > 0){
			float h = a.damage;
			if (h >= 1)
				h = 0.99f;
			int bloodI = (int) ((h)*BLOOD.length);
			OPACITY.O99.bind();
			sheet.renderTextured(blood().getTexture(BLOOD[bloodI]), nr, x, y);
			OPACITY.unbind();
			
		}
		
		
		if (a.inWater && a.physics.getZ() == 0){
			int i = GAME.intervals().get05() % WATER.length;
			sheet.renderTextured(water().getTexture((WATER[i] + dir)), nr, x, y);
		}else{
			s.setDistance2Ground(a.physics.getZ());
			int h = (int) (a.physics.getHeight()*height);
			if (a.isBaby())
				h/=2;
			s.setHeight(h);
			sheet.render(s, nr, x, y);
		}
		
	}
	
	public static void renderCorpse(AnimalSpecies s, Renderer r, ShadowBatch shadows, float ds, int x, int y, int state, int rot, int ran, double statef, COLOR decay) {

		TILE_SHEET sheet = s.sheet;
		
		shadows.setHeight(2).setDistance2Ground(0);
		if (state == 0) {
			int t = bodypart1;
			if ((ran & 1) == 1) {
				t += NR;
			}
			t += rot;
			sheet.render(r, t, x, y);
			sheet.render(shadows, t, x, y);
		}else if(state == 1) {
			int t = laying + rot;
			sheet.render(r, t, x, y);
			sheet.render(shadows, t, x, y);
			int bloodI = (int) ((statef)*BLOOD.length);
			
			if (bloodI > 0) {
				OPACITY.O99.bind();
				sheet.renderTextured(blood().getTexture(BLOOD[bloodI-1]), t, x, y);
				OPACITY.unbind();
			}
		}else if(state == 2) {
			decay.bind();
			int t = rotten + rot;
			sheet.render(r, t, x, y);
			sheet.render(shadows, t, x, y);
			COLOR.unbind();
			
		}else if(state == 3) {
			int t = bones + rot;
			sheet.render(r, t, x, y);
			sheet.render(shadows, t, x, y);
		}else {
			throw new RuntimeException();
		}
		
		
	}
	
	private static int[] cWalk = new int[] {
		standing,walk1,walk2
	};
	
	static void renderCaravan(SPRITE_RENDERER r, ShadowBatch s, double movement, int cx, int cy, RESOURCE res, int resAmount, boolean inWater, int dir, int ran){
		
		AnimalSpecies sp = SETT.ANIMALS().caravans.getC((ran&0x0FF));

		renderMount(sp, r, s, movement, cx, cy, inWater, dir, ran);
		
		if (res != null)
			SETT.ANIMALS().sprites.crate.renderC(r, dir, cx, cy);
		
		if (res != null && resAmount > 0) {
			res.renderLaying(r, cx-C.TILE_SIZEH, cy-C.TILE_SIZEH, 0, resAmount);
		}
		
	}
	
	static void renderMount(AnimalSpecies sp, SPRITE_RENDERER r, ShadowBatch s, double movement, int cx, int cy, boolean inWater, int dir, int ran){
		
		int nr = 0;
		
		int row = cWalk[((int)(movement*cWalk.length))%cWalk.length];
		nr+= row + dir;
		sp.sheet.renderC(r, nr, cx, cy);
		
		if (inWater){
			int i = GAME.intervals().get05() % WATER.length;
			int x1 = cx - sp.sheet.size()/2;
			int y1 = cy - sp.sheet.size()/2;
			OPACITY.O99.bind();
			sp.sheet.renderTextured(water().getTexture((WATER[i] + dir)), nr, x1, y1);
			OPACITY.unbind();
		}else{
			s.setDistance2Ground(0);
			s.setHeight(2);
			sp.sheet.renderC(s, nr, cx, cy);
		}
		
	}
	

	
}
