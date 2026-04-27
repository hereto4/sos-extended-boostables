package game.battle.thread.status;

import game.battle.div.Div;
import game.battle.formation.DivPositionImp;
import settlement.main.SETT;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.sets.Bitmap2D;

public final class DivArmyMap {

	public static final int radius = 7;
	private Bitmap2D[] maps = new Bitmap2D[] {new Bitmap2D(SETT.TILE_BOUNDS, false), new Bitmap2D(SETT.TILE_BOUNDS, false)};
	
	
	
	public DivArmyMap(DivStatus[] statuses) {
		
	}
	
	void add(Div div, DivPositionImp next) {

		for (int i = 0; i < next.deployed(); i++) {
			int x = next.tile(i).x();
			int y = next.tile(i).y();
			Bitmap2D m = maps[div.army().index()];
			add(x, y, m);
		}
	}
	
	private void add(int tx, int ty, Bitmap2D map) {
		
		for (int y = -radius+1; y < radius; y++) {
			for (int x = -radius+1; x < radius; x++) {
				
				int dx = tx+x;
				int dy = ty+y;
				if (SETT.IN_BOUNDS(dx, dy)) {
					map.set(dx, dy, true);
				}
				
			}
		}
		
	}

	void clear() {
		for (Bitmap2D m : maps)
			m.clear();
	}
	
	public MAP_BOOLEAN enemy(Div div) {
		return maps[(div.army().index()+1)&1];
	}


	
}
