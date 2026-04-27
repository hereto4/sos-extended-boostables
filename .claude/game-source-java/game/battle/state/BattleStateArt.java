package game.battle.state;

import init.constant.C;
import init.race.Race;
import init.type.CAUSE_ARRIVES;
import init.type.HTYPES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.path.AVAILABILITY;
import settlement.room.military.artillery.ArtilleryInstance;
import settlement.room.military.artillery.ROOM_ARTILLERY;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;

final class BattleStateArt {
	
	public static int placeArt(int cx, int cy, int depth, DIR dir, LIST<ROOM_ARTILLERY> art, Race race, boolean player) {
		depth += 10;
		
		dir = dir.perpendicular();
		int am = art.size()-1;
		for (int d = 0; d < 32 && am >= 0; d+= 8) {
			
			int dx = cx + dir.perpendicular().x()*depth;
			int dy = cy + dir.perpendicular().y()*depth;
			
			for (int w = 0; w < 128; w+=4) {
				
				for (int i = -1; i <= 1; i+=2) {
					int x = dx + dir.next(2).x()*i*w;
					int y = dy + dir.next(2).y()*i*w;
					ROOM_ARTILLERY a = art.get(am);
					if (deploy(x, y, dir, a, race, player)) {
					
						am --;
						if (am < 0) {
							return depth;
						}
					}
				}
				
			}
			depth += 8;
		}
		return depth;
	}
	
	private static boolean deploy(int sx, int sy, DIR d, ROOM_ARTILLERY art, Race race, boolean player) {
		
		
		if (!d.isOrtho())
			d = d.next((int)RND.rSign());
		
		int index = -1;
		for (int i = 0; i < DIR.ORTHO.size(); i++) {
			if (DIR.ORTHO.get(i) == d)
				index = i;
		}

		art.eplacer.rotSet(index);
		
		for (int y = 0; y < art.eplacer.height(); y++) {
			for (int x = 0; x < art.eplacer.width(); x++) {
				if (SETT.ENTITIES().amountAtTile(sx+x, sy+y) > 0)
					return false;
				if (art.eplacer.placable(sx+x, sy+y, x, y) != null)
					return false;
			}
		}
		
		if (art.eplacer.placableWhole(sx, sy) != null)
			return false;
		
		for (int y = 0; y < art.eplacer.height(); y++) {
			for (int x = 0; x < art.eplacer.width(); x++) {
				art.eplacer.place(sx+x, sy+y, x, y);
			}
		}
		art.eplacer.afterPlaced(sx, sy);
		
		ArtilleryInstance r = art.getter.get(sx, sy);
		
		if (r == null)
			return false;
		
		if (!player)
			r.setEnemy();
		
		r.muster(true);
		r.fireAtWill(true);
		r.setVisible();
		int am = art.services;
		for (COORDINATE c : r.body()) {
			if (r.is(c) && SETT.ROOMS().getAvailability(c.x(), c.y()) == AVAILABILITY.ROOM) {
				if (am > 0)
					new Humanoid(c.x()*C.TILE_SIZE+C.TILE_SIZEH, c.y()*C.TILE_SIZE+C.TILE_SIZEH, race, player ? HTYPES.SUBJECT() : HTYPES.ENEMY(), CAUSE_ARRIVES.SOLDIER_RETURN());
				am--;
			}
		}
		
		return true;
		
	}
	
}
