package world.map.terrain;

import static world.WORLD.IN_BOUNDS;
import static world.WORLD.THEIGHT;
import static world.WORLD.TWIDTH;

import snake2d.PathTile;
import snake2d.util.MATH;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.VectorImp;
import snake2d.util.map.MAP_BOOLEANE;
import snake2d.util.rnd.HeightMap;
import snake2d.util.rnd.Polymap;
import snake2d.util.rnd.RND;
import util.GUTIL;
import world.WORLD;
import world.WorldGen;

class GeneratorHeight {


	private final Polymap polly = new Polymap(TWIDTH(), THEIGHT(), (int) (60*(TWIDTH()/256.0)), 1.0);
	private final MAP_BOOLEANE checker;
	private final HeightMap height;
	
	GeneratorHeight(HeightMap height, WorldGen spec) {
		this.height = height;
		polly.checkInit();
		checker = polly.checker;
		rise();

		sink(height);
		
	}
	
	private void sink(HeightMap height) {
		
		int max = WORLD.TAREA()/2;
		
		while(max > 0)
			max -= sink(height, 1000, 16+RND.rFloat(32));

		
		
		
	}
	
	private void rise() {
		int max = WORLD.TAREA()/4;
		
		while(max > 0)
			max -= rise(height, RND.rInt(1000), RND.rInt(16));
	}
	
	private int sink(HeightMap height, int maxLength, double radius) {
		
		double sx = RND.rInt(TWIDTH());
		double sy = RND.rInt(THEIGHT());
		
		VectorImp dir = new VectorImp();		
		dir.setAngle(RND.rFloat()*10);

		GUTIL.flooder().init(this);
		GUTIL.flooder().pushSloppy((int)sx, (int)sy, 0);
		
		for (int i = 0; i < maxLength; i++) {
			
			sx += dir.nX();
			sy += dir.nY();
			dir.rotate(RND.rInt0(20));
			if (!IN_BOUNDS((int)sx, (int)sy)) {
				break;
			}
			GUTIL.flooder().pushSloppy((int)sx, (int)sy, 0);
		}
		
		int am = 0;
		
		while(GUTIL.flooder().hasMore()) {
			
			PathTile t = GUTIL.flooder().pollSmallest();
			double v = t.getValue()/radius;
			if (v >= 1)
				continue;
			v*=v;
			am++;
			
			
			height.set(t, height.get(t)*(0.4+0.6*v));
			for (DIR d : DIR.ALL) {
				if (IN_BOUNDS(t,  d)) {
					GUTIL.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance());
				}
			}
		}
		
		GUTIL.flooder().done();
		return am;
	
	}
	
	private int rise(HeightMap height, int maxLength, double radius) {
		
		polly.checkInit();
		
		double sx = RND.rInt(TWIDTH());
		double sy = RND.rInt(THEIGHT());
		
		VectorImp dir = new VectorImp();		
		dir.setAngle(RND.rFloat()*10);

		GUTIL.flooder().init(this);
		GUTIL.flooder().pushSloppy((int)sx, (int)sy, 0);
		
		int l2 = maxLength/2;
		
		for (int i = 0; i < maxLength; i++) {
			
			
			
			
			sx += dir.nX();
			sy += dir.nY();
			dir.rotate(RND.rInt0(15));
			if (!IN_BOUNDS((int)sx, (int)sy)) {
				break;
			}
			GUTIL.flooder().pushSloppy((int)sx, (int)sy, 0);
			polly.checker.set((int)sx, (int)sy, true);
			
			int ra = (int) (radius*(l2-MATH.distanceC(l2, i, maxLength))/l2);
			if (ra > 0) {
				for (int k = 0; k < 4; k++) {
					int x = (int) sx + RND.rInt0(ra);
					int y = (int) sy + RND.rInt0(ra);
					if (IN_BOUNDS(x, y)) {
						GUTIL.flooder().pushSloppy(x, y, 0);
						checker.set(x, y, true);
					}
				}
			}
			
			
		}
		
		int am = 0;
		
		while(GUTIL.flooder().hasMore()) {
			
			
			
			PathTile t = GUTIL.flooder().pollSmallest();
			
			double v = 0;
			
			if (checker.is(t)) {
				height.set(t, height.get(t)*20);
				am++;
			}else if (t.getValue() < 3){
				
				height.set(t, height.get(t)*(1 + 1.0-t.getValue()/3));
				//height.set(t, 1);
				v = 1;
			}else {
				continue;
			}
			am++;
			
			for (DIR d : DIR.ALL) {
				if (IN_BOUNDS(t,  d)) {
					GUTIL.flooder().pushSmaller(t, d, t.getValue()+v*d.tileDistance());
				}
			}
		}
		
		GUTIL.flooder().done();
		return am;
	
	}

}
