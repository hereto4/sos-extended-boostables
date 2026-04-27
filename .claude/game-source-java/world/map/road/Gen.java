package world.map.road;

import static world.WORLD.TBOUNDS;

import snake2d.util.datatypes.DIR;
import snake2d.util.map.MAP_DOUBLE;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.Polymap;
import world.WORLD;
import world.map.regions.Region;

class Gen {

	
	public void generateAll(int px, int py, ACTION astep) {
		
		WORLD.ROADS().saver().clear();
		astep.exe();

		MAP_DOUBLE roadCost = new MAP_DOUBLE() {
			private final Polymap polly = new Polymap(TBOUNDS(), 6, 1);
			@Override
			public double get(int dx, int dy) {
				double v = (0.4 + (polly.isEdge(dx, dy) ? 0 : 0.6))*getTerrainCost(dx, dy);
				Region r = WORLD.REGIONS().map.get(dx, dy);
				for (int i = 0; i < DIR .ORTHO.size(); i++)
					if (r != WORLD.REGIONS().map.get(dx, dy, DIR.ORTHO.get(i))) {
						v *= 2;
						break;
					}
				if (WORLD.WATER().isBig.is(dx, dy)) {
					v*= 2;
					if (!WORLD.WATER().coversTile.is(dx, dy))
						v*= 3;
				}
				
				return v;
			}
			
			double getTerrainCost(int tx, int ty) {
				if (WORLD.WATER().isBig.is(tx, ty)) {
					return 1;
				}
				if (WORLD.MOUNTAIN().heighter.get(tx, ty) >= 1)
					return 12;
				if (WORLD.FOREST().amount.get(tx, ty) == 1.0)
					return 6;
				return 3;
			}
			
			@Override
			public double get(int tile) {
				// TODO Auto-generated method stub
				return 0;
			}
		};
		
		astep.exe();
		new GenRoad(astep, roadCost);
		astep.exe();
		new GenPort(astep, roadCost);
		astep.exe();
		new GenPolish(astep, roadCost);
		astep.exe();
		new GenPlayer(astep);
		astep.exe();

	}

	
	


}
