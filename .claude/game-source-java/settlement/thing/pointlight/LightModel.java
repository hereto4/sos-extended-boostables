package settlement.thing.pointlight;

import static settlement.main.SETT.TERRAIN;

import game.time.TIME;
import init.constant.C;
import settlement.main.SETT;
import snake2d.CORE;
import snake2d.Renderer;
import snake2d.util.color.OPACITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.light.LIGHT_POINT;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TextureCoords;

abstract class LightModel{

	static final LightModel candle = new LightModel(10, 0) {
		
		@Override
		double flicker(PointLight l, int radius) {
			l.offX = (int) (RND.rExpo() * RND.rInt0(7));
			l.offY = (int) (RND.rExpo() * RND.rInt0(7));
			double intense = 5f + Math.pow(RND.rFloat0(0.8), 3);
			l.r = intense*1.7;
			l.g = intense*0.8;
			l.b = intense*0.4;
			l.radius = radius - RND.rInt(8);
			l.falloff = 1;
			l.z = 30 + RND.rInt0(5);
			return 0.030f + RND.rFloat(0.05f);
		}

		@Override
		void renderSprite(int x1, int y1, int ran) {
			SETT.LIGHTS().sprites.candle.renderC(CORE.renderer(), ran&0x07, x1, y1);
		}

		@Override
		void renderFlame(int tx, int ty, int x1, int y1, int ran) {
			flame(tx, ty, x1, y1, ran, SETT.LIGHTS().sprites.flame_small, 2);
		}
		
		@Override
		protected double intensity(int x, int y) {
			return super.intensity(x, y);
		}
	};
	
	static final LightModel torch = new LightModel(10, 1) {
		
		@Override
		double flicker(PointLight l, int radius) {
			l.offX = (int) (RND.rExpo() * RND.rInt0(7));
			l.offY = (int) (RND.rExpo() * RND.rInt0(7));
			double intense = 4f + RND.rExpo()*RND.rFloat0(0.8f);
			l.r = intense*1.8;
			l.g = intense*0.9;
			l.b = intense*0.4;
			l.radius = radius - RND.rInt(8);
			l.falloff = 1;
			l.z = 30 + RND.rInt0(5);
			return 0.025 + RND.rFloat(0.05f);
		}

		@Override
		void renderSprite(int x1, int y1, int ran) {
			
		}

		@Override
		void renderFlame(int tx, int ty, int x1, int y1, int ran) {
			flame(tx, ty, x1, y1, ran, SETT.LIGHTS().sprites.flame_medium, 24);
		}
	};
	
	static final LightModel torch_big = new LightModel(12, 2) {
		
		@Override
		double flicker(PointLight l, int radius) {
			l.offX = (int) (RND.rExpo() * RND.rInt0(7));
			l.offY = (int) (RND.rExpo() * RND.rInt0(7));
			double intense = 4f + RND.rExpo()*RND.rFloat0(0.8f);
			l.r = intense*1.8;
			l.g = intense*0.9;
			l.b = intense*0.3;
			l.radius = radius;
			l.falloff = 1;
			l.z = 30 + RND.rInt0(5);
			return 0.025 + RND.rFloat(0.05f);
		}

		@Override
		void renderSprite(int x1, int y1, int ran) {
			
		}

		@Override
		void renderFlame(int tx, int ty, int x1, int y1, int ran) {
			flame(tx, ty, x1, y1, ran, SETT.LIGHTS().sprites.flame_big, 48);
		}
	};
	

	
	static final LightModel fire = new LightModel(5, 3) {
		
		@Override
		double flicker(PointLight l, int radius) {
			l.offX = (int) (RND.rExpo() * RND.rInt0(7));
			l.offY = (int) (RND.rExpo() * RND.rInt0(7));
			double intense = 2.5f + Math.pow(RND.rFloat0(0.8), 3);
			l.r = intense*1.7;
			l.g = intense*0.8;
			l.b = intense*0.3;
			l.radius = radius;
			l.falloff = 1;
			l.z = 15;
			return 0.030f + RND.rFloat(0.05f);
		}

		@Override
		void renderSprite(int x1, int y1, int ran) {
			// TODO Auto-generated method stub
			
		}

		@Override
		void renderFlame(int tx, int ty, int cx, int cy, int ran) {
			flame(tx, ty, cx, cy, ran, SETT.LIGHTS().sprites.flame_big, 48);
		}
		
		@Override
		protected double intensity(int x, int y) {
			return 1.0;
		}
	};
	
	static final LightModel mouse = new LightModel(15, 4) {
		
		@Override
		double flicker(PointLight l, int radius) {
			l.offX = (int) (RND.rExpo() * RND.rInt0(7));
			l.offY = (int) (RND.rExpo() * RND.rInt0(7));
			double intense = 5 + RND.rExpo()*RND.rFloat0(0.2f);
			l.r = intense*1.3;
			l.g = intense*0.7;
			l.b = intense*0.3;
			l.radius = radius;
			l.falloff = 0.4;
			l.z = 20;
			return 0.025 + RND.rFloat(0.05f);
		}

		@Override
		void renderSprite(int x1, int y1, int ran) {
			// TODO Auto-generated method stub
			
		}

		@Override
		void renderFlame(int tx, int ty, int cx, int cy, int ran) {
			// TODO Auto-generated method stub
			
		}
	};
	
	final static ArrayList<LightModel> all = new ArrayList<>(candle, torch, torch_big, fire, mouse);
	
	static void flickerr(float ds) {
		for (LightModel m : all)
			m.flicker(ds);
	}
	
	private final static int RNDS = 64;
	private final int tileDiameter;
	private final PointLight[] lights = new PointLight[RNDS];
	private final PointRayTracer tracer;
	private final int radius;
	final int index;
	
	private LightModel(int tileDiameter, int index){
		this.index = index;
		if (tileDiameter % 2 == 0)
			tileDiameter += 1;
		tracer = new PointRayTracer(tileDiameter);
		this.tileDiameter = tileDiameter;
		for (int i = 0; i < RNDS; i++) {
			lights[i] = new PointLight();
		}
		this.radius = tileDiameter*C.TILE_SIZE/2-C.TILE_SIZEH;
	}
	
	void flicker(float ds) {
		for (int i = 0; i < RNDS; i++) {
			lights[i].timer -= ds;
			if (lights[i].timer > 0)
				continue;
			lights[i].timer = flicker(lights[i], radius);
		}
	}
	
	abstract double flicker(PointLight l, int radius);
	
	abstract void renderSprite(int cx, int cy, int ran);
	
	abstract void renderFlame(int tx, int ty, int cx, int cy, int ran);
	
	void flame(int tx, int ty, int x1, int y1, int ran, TILE_SHEET flame, int sparks) {
		double wi = SETT.WEATHER().wind.getD();
		if (TERRAIN().get(tx, ty).roofIs())
			wi = 0;
		FireSparks.render(x1, y1, sparks, ran, wi);
		
		int w = flame.size()/2;
		
		
		flame.render(CORE.renderer(), ran & 0x07, x1-w, y1-w);
		
		x1 -= C.TILE_SIZEH;
		y1 -= C.TILE_SIZEH;
		
		OPACITY.O75.bind();
		TextureCoords d, c;
		c = SETT.LIGHTS().sprites.texture.get((ran>>4)&0x07, (ran>>5)&0x07);
		d = SETT.LIGHTS().sprites.displacement.get((ran>>4)&0x07, (ran>>5)&0x07);
		CORE.renderer().renderDisplaced(x1, x1 +C.TILE_SIZE, y1, y1+C.TILE_SIZE, d, c);
		OPACITY.unbind();
	}
	
	void register(Renderer r, int ran, int x, int y, int offx, int offy) {
		
		double i = intensity(x, y);
		
		if (i == 0)
			return;
		
		renderFlame(x >> C.T_SCROLL, y >> C.T_SCROLL, x+offx, y+offy, ran);
		
		tracer.init(x, y);
		
		PointLight light = lights[ran & 0x2F];
		
		double lr = light.r;
		double lg = light.g;
		double lb = light.b;
		
		
		light.r*= i;
		light.g*= i;
		light.b*= i;
		
		light.x = x+offx;
		light.y = y+offy;
		
		int sx = ((x)&~C.T_MASK) + offx - tileDiameter/2*C.TILE_SIZE;
		int sy = ((y)&~C.T_MASK) + offy - tileDiameter/2*C.TILE_SIZE;

		for (int ty = 0; ty < tileDiameter; ty++) {
			for (int tx = 0; tx < tileDiameter; tx++) {
				
				x = sx + tx*C.TILE_SIZE;
				y = sy + ty*C.TILE_SIZE;
				if (tracer.litIs(tx, ty))
					r.registerLight(
						light, 
						x, x+C.TILE_SIZE,
						y, y+C.TILE_SIZE, 
						tracer.getSide(tx, ty, DIR.NE), 
						tracer.getSide(tx, ty, DIR.SE), 
						tracer.getSide(tx, ty, DIR.SW), 
						tracer.getSide(tx, ty, DIR.NW));
				//SPRITES.cons().filled.render(r, 0x0F, x, y);
			}
		}
		
		light.r = lr;
		light.g = lg;
		light.b = lb;
		

	}
	
	protected double intensity(int x, int y) {
		double i = 1.0;
		
		if (TERRAIN().get(x >> C.T_SCROLL, y >> C.T_SCROLL) == SETT.TERRAIN().CAVE) {
			if (TIME.light().dayIs())
				i*= 0.75;
			else if (TIME.light().partOfCircular() < 0.2) {
				i *= 0.75 + 0.75*(TIME.light().partOfCircular()/0.2);
			}else {
				i *= 1.5;
			}
		}else if (TERRAIN().get(x >> C.T_SCROLL, y >> C.T_SCROLL).roofIs()) {
			if (TIME.light().dayIs())
				i*= 0.25;
			else if (TIME.light().partOfCircular() < 0.2) {
				i *= 0.25 + (TIME.light().partOfCircular()/0.2);
			}
		}else if (TIME.light().partOfCircular() < 0.2) {
			if (TIME.light().dayIs())
				return 0;
			i *= ((TIME.light().partOfCircular())/0.2);
		}else if(TIME.light().dayIs()) {
			return 0;
		}
		return i;
	}
	

	static class PointLight implements LIGHT_POINT{

		double r,g,b;
		double falloff;
		int radius;
		byte rOff = (byte) RND.rInt(C.TILE_SIZEH);
		double x,y,z;
		double timer;
//		int spriteV;
		int offX,offY;
		
		@Override
		public float getRed() {
			return (float) r;
		}

		@Override
		public float getGreen() {
			return (float) g;
		}

		@Override
		public float getBlue() {
			return (float) b;
		}

		@Override
		public float getFalloff() {
			return (float) falloff;
		}

		@Override
		public int getRadius() {
			return radius + rOff;
		}

		@Override
		public float cx() {
			return (float) (x+offX);
		}

		@Override
		public float cy() {
			return (float) (y+offY);
		}

		@Override
		public float cz() {
			return (float) z;
		}

	}

}
