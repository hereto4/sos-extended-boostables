package world.map.regions.centre;

import init.constant.C;
import snake2d.CORE;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.rnd.RND;
import view.main.VIEW;

class Sparks {
	
	private static Sparks self = new Sparks();
	private final static int AMOUNT = 256;
	private final static int length = 128;
	private final static double duration = 10;
	private static final double ticksPerTime = length/duration;
	private final COLOR[] colors = new COLOR[AMOUNT];
	private final byte[][] xs = new byte[length][AMOUNT];
	private final byte[][] ys = new byte[length][AMOUNT];
	private final byte[][] op = new byte[length][AMOUNT];
	
	
	
	private Sparks() {
		
		for (int i = 0; i < AMOUNT; i++) {
			colors[i] = new ColorImp(60+RND.rInt(20), 90+RND.rInt0(20), 20+RND.rInt(10)).shade(1.2-RND.rExpo());
		}
		
		final int aniLength = 25;
		
		for (int a = 0; a < AMOUNT; a++) {
			
			int current = RND.rInt(length);
			int tickCount = aniLength;
			double y = RND.rInt0(4*C.SCALE);
			double x = RND.rInt0(4*C.SCALE);
			
			double dvx = -0.05*(1.25*C.TILE_SIZE + RND.rFloat(C.TILE_SIZE/4));
			double dvy = -0.05*(1.25*C.TILE_SIZE + RND.rFloat(C.TILE_SIZE/4));
			
			double xsin = RND.rFloat(1);
			double ysin = RND.rFloat(1);
			double dsin = RND.rFloat()/length;
			
			for (int t = 0; t < length; t++) {
				tickCount --;
				if (tickCount < 0) {
					tickCount = aniLength;
					y = RND.rInt0(4*C.SCALE);
					x = RND.rInt0(4*C.SCALE);
				}
				
				current = current % length;
				
				
				op[current][a] = (byte) (255.0*(1.0-t/(double)length));
				xs[current][a] = (byte) (x);
				ys[current][a] = (byte) (y);
//				xsin += dxsin;
//				ysin += dysin;
				x+= dvx*Math.sin(xsin);
				y+= dvy*Math.sin(ysin);
				xsin+=dsin;
				ysin+=dsin;
				current++;
			}
		}
		
	}

	public static void render(int x, int y, int amount, int ran) {
		int t = ((int)((VIEW.renderSecond()+ran/duration)*ticksPerTime)) & (length-1);
		

		for (int i = 0; i < amount; i++) {
			self.colors[(ran+i)&(AMOUNT-1)].bind();
			CORE.renderer().renderParticle(x+(int)(self.xs[t][i]), y+(int)(self.ys[t][i]));
		}
		COLOR.unbind();
		OPACITY.unbind();
	}

	
}
