package game.battle.thread.general.offence;

import game.battle.div.Div;
import init.constant.C;
import init.constant.Config;
import settlement.main.SETT;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap2D;
import snake2d.util.sets.LIST;

final class UtilDivMap {
	
	private final GTile[] tiles = new GTile[Config.battle().DIVISIONS_PER_ARMY];
	private int tileNewI = 0;
	private final GTile[][] grid = new GTile[(int) Math.ceil(SETT.TWIDTH/16.0)][(int) Math.ceil(SETT.THEIGHT/16.0)];
	private final Bitmap2D is = new Bitmap2D(SETT.TILE_BOUNDS, false);
	private int[] xs = new int[Config.battle().DIVISIONS_PER_ARMY];
	private int[] ys = new int[Config.battle().DIVISIONS_PER_ARMY];
	
	
	private final ArrayList<Div> res = new ArrayList<Div>(Config.battle().DIVISIONS_PER_ARMY);
	private final LIST<Div> none = new ArrayList<Div>(0);
	
	public UtilDivMap() {

		for (int i = 0; i < tiles.length; i++) {
			tiles[i] = new GTile();
		}
	}
	
	public void clear() {
		tileNewI = 0;
		is.clear();
		for (GTile[] tt : grid) {
			for (int i = 0; i < tt.length; i++)
				tt[i] = null;
		}
	}
	
	public void add(Div div) {
		int tx = div.centre().cUnitX()>>C.T_SCROLL;
		int ty = div.centre().cUnitY()>>C.T_SCROLL;
		if (!SETT.IN_BOUNDS(tx,ty))
			return;
		xs[div.indexArmy()] = tx;
		ys[div.indexArmy()] = ty;
		is.set(tx, ty, true);
		int gx = tx/16;
		int gy = ty/16;
		GTile t = tiles[tileNewI++];
		t.div = div;
		t.next = grid[gy][gx];
		grid[gy][gx] = t;
		
	}

	public LIST<Div> get(int tx, int ty){
		if (!SETT.IN_BOUNDS(tx, ty))
			return none;
		if (!is.is(tx, ty))
			return none;
		res.clearSloppy();
		GTile t = grid[ty/16][tx/16];
		while(t != null && res.hasRoom()) {
			if (xs[t.div.indexArmy()] == tx && ys[t.div.indexArmy()] == ty)
				res.add(t.div);
			t = t.next;
		}
		return res;
	}
	
	private static final class GTile {
		
		private Div div;
		private GTile next;
		
	}
	

	
}

