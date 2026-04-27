package settlement.battle.invasion;

import static settlement.main.SETT.IN_BOUNDS;

import game.GAME;
import game.battle.div.Div;
import game.battle.util.DivGeneration;
import init.constant.C;
import init.constant.Config;
import init.type.HTYPES;
import settlement.battle.invasion.SpotMaker.InvasionSpot;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.equip.EquipBattle;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.ArrayList;

final class DivDeployer {

	public static Div deploy(ArrayList<DivGeneration> divs, InvasionSpot spot) {
		if (divs.size() == 0)
			return null;
		
		Div d = getDiv();
		if (d == null)
			return null;
		
		DIR sDir = spot.dir;
		
		int sx = spot.body.x1();
		int sy = spot.body.y1();
		DIR dir = sDir.next(2);
		
		DivGeneration id = divs.get(0);
		
//		for (COORDINATE c : spot.body) {
//			if (DIR.get(sx, sy, c.x(), c.y()) == dir) {
//				sx = c.x();
//				sy = c.y();
//			}
//		}
		
		sx += sDir.x()*6;
		sy += sDir.y()*6;
		
		dir = sDir.next(2);
		int w = (int) Math.ceil(id.indus.length/5.0);
		if (w >= spot.size)
			w = spot.size-1;
		for (int i = 0; i < spot.size-w; i++) {
			
			if (can(sx, sy)) {
				boolean can= true;
				for (int k = 1; k <= w; k++) {
					int dx = sx + k*dir.x();
					int dy = sy + k*dir.y();
					if (!can(dx, dy)) {
						can = false;
						break;
					}
				}
				if (can) {
					place(sx, sy, id, d, sDir);
					divs.remove(0);
					return d;
				}
			}
			
			sx += dir.x();
			sy += dir.y();
		}
		return null;
		
		
		
	}
	
	private static ArrayList<Humanoid> tmp = new ArrayList<>(Config.battle().MEN_PER_DIVISION);
	
	private static boolean place(int sx, int sy, DivGeneration d, Div div, DIR spotdir) {
		int amount = d.indus.length;
		int w = (int) Math.ceil(d.indus.length/5.0);
		
		div.settings().musteringSet(true);
		div.info.menSet(d.indus.length);
		div.info.bannerISet(d.bannerI);
		div.info.name().clear().add(d.name);
		
		DIR right = spotdir.next(2);
		DIR down = right.next(2);
		tmp.clear();
		for (int y = 0; y < w; y++) {
			for (int x = 0; x < 5; x++) {
				if (amount-- > 0) {
					int cx = (sx + y*right.x() + x*down.x())*C.TILE_SIZE+C.TILE_SIZEH;
					int cy = (sy + y*right.y() + x*down.y())*C.TILE_SIZE+C.TILE_SIZEH;
					
					if (SETT.PIXEL_BOUNDS.holdsPoint(cx, cy)) {
						Humanoid h = new Humanoid(cx, cy, d.race(), HTYPES.ENEMY(), null);
						
						if (h != null && !h.isRemoved()) {
							STATS.NEEDS().clear(h.indu());
							h.indu().copyFrom(d.indus[amount]);
							STATS.BATTLE().basicTraining.setD(h.indu(), 1.0);
							for (EquipBattle m : STATS.EQUIP().BATTLE_ALL()) {
								m.set(h.indu(), m.get(d.indus[amount]));
							}
							
							h.setDivision(div);
							tmp.add(h);
						}
					}
					
					
					
				}
			}
		}
		

		
		int x1 = sx*C.TILE_SIZE+C.TILE_SIZEH;
		int y1 = sy*C.TILE_SIZE+C.TILE_SIZEH;
		DIR dir = spotdir.next(2);
		int x2 = x1 + dir.x()*w*C.TILE_SIZE;
		int y2 = y1 + dir.y()*w*C.TILE_SIZE;
		
		GAME.ARMIES().placer.deploy(div, x1, x2, y1, y2);
		
		GAME.ARMIES().initAndTeleport(new ArrayList<Div>(div));
		GAME.ARMIES().placer.deploy(div, x1, x2, y1, y2);
		return true;
	}
	
	private static boolean can(int sx, int sy) {
		
		
		if (!IN_BOUNDS(sx, sy)) {
			return false;
		}
		
		if (SETT.PATH().availability.get(sx, sy).isSolid(GAME.ARMIES().enemy())) {
			GAME.ARMIES().map.breakIt(sx, sy);
			return false;
		}
		
		
		for (ENTITY e : SETT.ENTITIES().getAtTile(sx, sy)) {
			if (e instanceof Humanoid && ((Humanoid) e).indu().army() == GAME.ARMIES().enemy())
				return false;
		}
		return true;
	}
	
	public static Div getDiv() {
		
		for (Div d : GAME.ARMIES().enemy().divisions()) {
			if (d.menNrOf() == 0) {
				return d;
			}
		}
		return null;
	}
	
}
