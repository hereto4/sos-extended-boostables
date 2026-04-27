package game.battle.thread.order;

import static settlement.main.SETT.TWIDTH;

import game.battle.div.Div;
import game.battle.formation.DivPositionImp;
import game.battle.thread.order.BattleOrderUpdater.Plan;
import game.battle.thread.status.BattleStatus;
import game.battle.thread.status.DivsSpaceMap;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap1D;

final class PathCost {

	private final Bitmap1D map = new Bitmap1D(SETT.TAREA/4, false);
	private final ArrayList<Div> list = new ArrayList<>(8);
	
	void init(DivPositionImp ok, int dx, int dy) {
		
		map.clear();
		add(ok);
		
		list.clear();
		BattleStatus.map().get(list, dx, dy);
		
		for (Div d: list) {
			add(d.current());
		}
		
	}
	
	private void add(DivPositionImp ok) {
		for (int i = 0; i < ok.deployed(); i++) {
			int tx = ok.tile(i).x();
			int ty = ok.tile(i).y();
			
			for (int y = -DivsSpaceMap.radius; y <= DivsSpaceMap.radius; y++) {
				for (int x = -DivsSpaceMap.radius; x <= DivsSpaceMap.radius; x++) {
					int r = Math.abs(x)+Math.abs(y);
					if (r <= DivsSpaceMap.radius+1) {
						int dx = tx+x;
						int dy = ty+y;
						if (SETT.IN_BOUNDS(dx, dy)) {
							map.set((dx>>1)+(dy>>2)*SETT.TWIDTH, true);
						}
					}
				}
			}
			
		}
	}
	
	public double cost(int fx, int fy, DIR d) {
		return cost(fx, fy, fx+d.x(), fy+d.y());
	}
	
	public double cost(int fx, int fy, int tx, int ty) {
		if (!SETT.IN_BOUNDS(tx, ty))
			return -1;
		AVAILABILITY a = SETT.PATH().getAvailability(fx, ty);
		if (a.isSolid(Plan.a))
			return -1;
		a = SETT.PATH().getAvailability(tx, fy);
		if (a.isSolid(Plan.a))
			return -1;
		a = SETT.PATH().getAvailability(tx, ty);
		if (a.isSolid(Plan.a))
			return -1;

		int t = tx +ty*TWIDTH;
		double res = 1 + a.movementSpeedI;
		double space = SETT.ENV().map.SPACE.get(t);
		if (space < 0.5)
			res += 10-10*space;
		if (map.get((tx>>1)+(ty>>2)*SETT.TWIDTH))
			return res;
		return res; // + ArmyAIUtil.space().cost.get(tx, ty);
	}
	
	public double cost(int tx, int ty) {
		if (!SETT.IN_BOUNDS(tx, ty))
			return -1;
		AVAILABILITY a = SETT.PATH().getAvailability(tx, ty);
		if (a.isSolid(Plan.a))
			return -1;
		int t = tx +ty*TWIDTH;
		double res = 1 + a.movementSpeedI;
		double space = SETT.ENV().map.SPACE.get(t);
		if (space < 0.5)
			res += 10-10*space;
		if (map.get((tx>>1)+(ty>>2)*SETT.TWIDTH))
			return res;
		return res; // + ArmyAIUtil.space().cost.get(tx, ty);
	}
	
	
}
