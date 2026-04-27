package settlement.battle.invasion;

import game.GAME;
import game.battle.div.Div;
import game.battle.util.DivGeneration;
import game.faction.FACTIONS;
import init.constant.C;
import init.race.RACES;
import init.race.Race;
import init.type.CAUSE_ARRIVES;
import init.type.HTYPES;
import settlement.battle.invasion.SpotMaker.InvasionSpot;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.path.components.SComponent;
import settlement.room.main.throne.THRONE;
import settlement.room.military.artillery.ArtilleryInstance;
import settlement.room.military.artillery.ROOM_ARTILLERY;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import util.GUTIL;

final class ArtilleryPlacer {

	
	static boolean placeArt(LIST<DivGeneration> divs, InvasionSpot spot, int amount) {
		
		mark();
		PathTile t = find(spot);
		if (t == null)
			return false;
		PathTile target = rewind(t, 2);
		if (target == null)
			return false;
		PathTile safe = rewind(t, 1);
		if (safe == null)
			return false;
		PathTile dest = findPos(spot, safe);
		if (dest == null)
			return false;
		Race r = getRace(divs);
		return deploy(r, spot, dest, DIR.get(dest, target), amount);
		
	}
	
	private static Race getRace(LIST<DivGeneration> divs) {
		int[] amount = new int[RACES.all().size()];
		for (DivGeneration d : divs) {
			amount[d.race().index] += d.indus.length;
		}
		if (divs.size() == 0)
			return FACTIONS.player().race();
		Race best = divs.get(0).race();
		int bestV = 0;
		for (Race r : RACES.playable()) {
			if (amount[r.index] > bestV) {
				best = r;
				bestV = amount[r.index];
			}
				
		}
		return best;
	}
	
	private static void mark() {
		
		Flooder f = GUTIL.flooder();
		f.init(ArtilleryPlacer.class);
		
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			f.setValue2(c, 0);
		}
		
		
		
		for (ROOM_ARTILLERY b : SETT.ROOMS().ARTILLERY) {
			for (int i = 0; i < b.instancesSize(); i++) {
				ArtilleryInstance ins = b.getInstance(i);
				if (ins.army() == GAME.ARMIES().enemy()) {
					f.pushGreater(ins.body().cX(), ins.body().cY(), ins.rangeMax()-32);
					f.setValue2(ins.body().cX(), ins.body().cY(), 2);
				}
			}
		}
		
		f.pushGreater(THRONE.coo().x(), THRONE.coo().y(), 48);
		f.setValue2(THRONE.coo().x(), THRONE.coo().y(), 2);
		
		for (Div d : GAME.ARMIES().player().divisions()) {
			if (d.active()) {
				for (int i = 0; i < d.current().deployed(); i++) {
					int tx = d.current().tile(i).x();
					int ty = d.current().tile(i).y();
					if (SETT.IN_BOUNDS(tx, ty)) {
						f.pushGreater(tx, ty, 128);
						f.setValue2(tx, ty, 2);
					}
					
				}
			}
			
		}
		
		while(f.hasMore()) {
			
			PathTile t = GUTIL.flooder().pollGreatest();
			if (t.getValue() <= 0) {
				break;
			}
			for (DIR d : DIR.ALL) {
				int dx = t.x()+d.x();
				int dy = t.y()+d.y();
				if (SETT.IN_BOUNDS(dx, dy)) {
					
					f.pushGreater(dx, dy, t.getValue()-d.tileDistance());
					f.setValue2(dx, dy, 1);
				}
				
			}
			
		}
		f.done();
		
			
		
	}
	
	private static PathTile find(InvasionSpot spot) {
		
		Flooder f = GUTIL.flooder();
		f.init(ArtilleryPlacer.class);

		for (COORDINATE c : spot.body) {
			f.pushSloppy(c, 0);
		}
		
		
		
		while(f.hasMore()) {
			
			PathTile t = GUTIL.flooder().pollSmallest();
			if (THRONE.coo().isSameAs(t)) {
				f.done();
				return t;
			}
			
			
			for (DIR d : DIR.ALL) {
				int dx = t.x()+d.x();
				int dy = t.y()+d.y();
				if (SETT.IN_BOUNDS(dx, dy)) {
					f.pushSmaller(dx, dy, t.getValue()+d.tileDistance()*(1+SETT.PATH().availability.get(dx, dy).movementSpeedI)*(11 - 10*SETT.ENV().map.SPACE.get(dx, dy)), t);
				}
				
			}
			
		}
		f.done();
		return null;
			
		
	}
	
	private static PathTile rewind(PathTile t, int va) {
		
		PathTile safe = t;
		
		while(t.getParent() != null) {
			if (t.getValue2() >= va) {
				
				safe = t.getParent();
			}
			t = t.getParent();
		}
		
		
		
		
		if (GUTIL.flooder().getValue2(safe.x(), safe.y()) >= va)
			return null;
		return safe;
	}
	
	private static PathTile findPos(InvasionSpot spot, PathTile t) {

		SComponent sup = SETT.PATH().comps.superComp.get(spot.body.cX(), spot.body.cY());
		PathTile pos = t;
		
		while(pos != null && ((SETT.ENV().map.SPACE.get(pos) < 0.5 || SETT.PATH().comps.superComp.get(pos) != sup) || SETT.PATH().availability.get(pos.x(), pos.y()).isSolid(GAME.ARMIES().enemy()))) {
			pos = pos.getParent();
		}
		
		if (pos == t) {
			if (pos.getParent() == null)
				return null;
			return pos.getParent();
		}
		
		return pos;
	}
	
	
	private static boolean deploy(Race r, InvasionSpot spot, COORDINATE c, DIR dir, int amount) {
		
		int am = 0;
		
		SComponent sup = SETT.PATH().comps.superComp.get(spot.body.cX(), spot.body.cY());
		
		for (int d = 0; d < 32; d+= 8) {
			
			int dx = c.x() + dir.perpendicular().x()*d;
			int dy = c.y() + dir.perpendicular().y()*d;
			
			for (int w = 0; w < 64; w+=4) {
				
				for (int i = -1; i <= 1; i+=2) {
					int x = dx + dir.next(2).x()*i*w;
					int y = dy + dir.next(2).y()*i*w;
					ROOM_ARTILLERY a = SETT.ROOMS().ARTILLERY.rnd();
					if (deploy(sup, x, y, dir, a)) {
						amount--;
						am += a.services;
						if (amount <= 0) {
							createDudes(sup, r, spot, am);
							return true;
						}
					}
				}
				
			}
		}
		
		
		if (am > 0) {
			createDudes(sup, r, spot, am);
		}
		return am > 0;
		
	}
	
	private static void createDudes(SComponent comp, Race r, InvasionSpot spot, int am) {
		Flooder f = GUTIL.flooder();
		f.init(ArtilleryPlacer.class);
		
		for (COORDINATE c : spot.body) {
			f.pushSloppy(c, RND.rFloat());
		}
		
		while(am > 0 && f.hasMore()) {
			
			PathTile t = f.pollSmallest();
			if (!comp.is(t))
				continue;
			Humanoid h = new Humanoid(t.x()*C.TILE_SIZE+C.TILE_SIZEH, t.y()*C.TILE_SIZE+C.TILE_SIZEH, r, HTYPES.ENEMY(), CAUSE_ARRIVES.SOLDIER_RETURN());
			if (!h.isRemoved())
				am--;
			for (DIR d : DIR.ALL) {
				int dx = t.x()+d.x();
				int dy = t.y()+d.y();
				if (SETT.IN_BOUNDS(dx, dy)) {
					
					f.pushSmaller(dx, dy, t.getValue()+d.tileDistance());
					f.setValue2(dx, dy, 1);
				}
				
			}
		}
		f.done();
	}
	
	private static boolean deploy(SComponent sup, int sx, int sy, DIR d, ROOM_ARTILLERY art) {
		
		
		
		if (SETT.PATH().comps.superComp.get(sx, sy) != sup)
			return false;
		
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
		
		r.setEnemy();
		
		return true;
		
	}
	
}
