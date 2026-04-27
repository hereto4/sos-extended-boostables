package world.map.terrain;

import static world.WORLD.FOREST;
import static world.WORLD.MOUNTAIN;
import static world.WORLD.THEIGHT;
import static world.WORLD.TWIDTH;
import static world.WORLD.WATER;

import init.constant.Config;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.HeightMap;
import snake2d.util.rnd.RND;
import world.WORLD;

class GeneratorForest {
	
	private final HeightMap noise = new  HeightMap(TWIDTH(), THEIGHT(), 16, 4);
	private final HeightMap bigNoise = new  HeightMap(TWIDTH(), THEIGHT(), 64, 64);
	
	private double tres = 1.0-Config.world().FOREST_AMOUNT;
	
	GeneratorForest(float[][] climate){
		
		for (int y = 0; y < THEIGHT(); y++){
			for (int x = 0; x < TWIDTH(); x++){
				FOREST().amount.set(x, y, 0);
				if (FOREST().placable.is(x, y))
					if (WORLD.MOISTURE().get(x, y) >= 0.15)
						place(x, y, climate[y][x]);
			}
		}
		
		
	}
	
	private void place(int tx, int ty, double c) {
		
		double n = noise.get(tx, ty)*(0.35+0.65*bigNoise.get(tx,ty));
		if (n > 0.8)
			n = 0.8;
		
		if (WATER().RIVER.is(tx, ty)) {
			n+= n*0.050;
		}
		double f = 0;
		for (int i = 0; i < DIR.ALL.size(); i++) {
			DIR d = DIR.ALL.get(i);
			if (WATER().RIVER.is(tx, ty, d) || WATER().LAKE.normal.is(tx, ty, d))
				n += 0.010;
			f+= WORLD.MOISTURE().get(tx+d.x(), ty+d.y());
		}
		f /= DIR.ALL.size();
		n+= 0.2*f;


		n *= 0.5+c*1.2;
		n = Math.pow(n,1.5);
		//if (!MOUNTAIN().is(tx, ty))
			
		
		if (MOUNTAIN().is(tx, ty)){
			double h = MOUNTAIN().heighter.get(tx, ty);
			if (h > 3)
				return;
			h = 1.0 - h/3;
			n -= 0.3*h;
//			
//			
//			double h = MOUNTAIN().getHeightNormalized(tx, ty);
//			if (h < 0.10) {
//				n+= n*0.05;
//			}else {
//				n -= n*(MOUNTAIN().getHeightNormalized(tx, ty)-0.10)*5;
//			}
		}
		
		if (n > tres)  {
			n-= tres;
			n *= 1.0/(1.0-tres);
			float r = RND.rFloat();
			n+= RND.rBoolean() ? 0.1*r : -0.1*r;
			if ( n > 1)
				n = 1;
			if (MOUNTAIN().is(tx, ty)) {
				double h = MOUNTAIN().heighter.get(tx, ty);
				if (h > 3)
					return;
				n*= 1.0-h/3;
			}
				
			FOREST().amount.set(tx, ty, n);
		}else {
			
			float r = RND.rFloat();
			r*= r;
			n+= RND.rBoolean() ? 0.3*r : -0.3*r;
			if (n > 0.6) {
				n-= 0.6;
				n*= 1.66;
				n -= 0.75 - WORLD.MOISTURE().get(tx, ty);
				n*= RND.rFloat()*RND.rFloat();
				
				FOREST().amount.set(tx, ty, n);
				
			}
			
		}
		
	}
	
}
