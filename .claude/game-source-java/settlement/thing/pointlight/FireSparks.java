package settlement.thing.pointlight;

import init.constant.C;
import snake2d.CORE;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.rnd.RND;

public class FireSparks {
	
	private static FireSparks self = new FireSparks();
	private final static int AMOUNT = 256;
	private final static int length = 128;
	private final static double duration = 5;
	private static final double ticksPerTime = length/duration;
	private final COLOR[] colors = new COLOR[AMOUNT];
	private final byte[][] xs = new byte[length][AMOUNT];
	private final byte[][] ys = new byte[length][AMOUNT];
	private double ani = RND.rInt();
	
	
	
	private FireSparks() {
		
		for (int i = 0; i < AMOUNT; i++) {
			colors[i] = new ColorImp(110+RND.rInt0(20), 80+RND.rInt(10), 20+RND.rInt(10)).shade(1.2-RND.rExpo());
		}
		
		final int aniLength = 25;
		
		for (int a = 0; a < AMOUNT; a++) {
			
			int current = RND.rInt(length);
			int tickCount = aniLength;
			double y = RND.rInt0(4*C.SCALE);
			double x = RND.rInt0(4*C.SCALE);
			
			double dvx = -0.05*(1.25*C.TILE_SIZE + RND.rFloat(C.TILE_SIZE/4));
			double dvy = 0.05*(1.25*C.TILE_SIZE + RND.rFloat(C.TILE_SIZE/4));
			
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
		
//		for (int ses = 1; ses< 32; ses++) {
//			for (int ran = 0; ran < AMOUNT; ran++) {
//				int r = (colors[0][ran].red() & 0x0FF);
//				int g = (colors[0][ran].green() & 0x0FF);
//				int b = (colors[0][ran].blue() & 0x0FF);
//				r+= ses;
//				g-= ses;
//				b-= ses/3;
//				colors[ses][ran] = new ColorImp(r, g, b);
//			}
//		}
	}
	
	static void update(float ds) {
		self.ani += ds*ticksPerTime;
	}
	
	public static void render(int x, int y, int amount, int ran, double wind) {
		render(self.ani, x, y, amount, ran, wind);
	}
	
	public static void render(double ani, int x, int y, int amount, int ran, double wind) {
		int t = ((int)(ani)) & (length-1);
		
		double d = 0.5 + 0.5*wind;
		
		for (int i = 0; i < amount; i++) {
			self.colors[(ran+i)&(AMOUNT-1)].bind();
			CORE.renderer().renderParticle(x+(int)(self.xs[t][i]*d), y+(int)(self.ys[t][i]*d));
		}
		COLOR.unbind();
		
	}

	
}
