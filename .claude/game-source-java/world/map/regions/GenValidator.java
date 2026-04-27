package world.map.regions;

import snake2d.LOG;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import util.GUTIL;
import util.text.D;
import view.main.VIEW;
import world.WORLD;
import world.WORLD.WorldError;
import world.map.regions.centre.WCentre;
import world.map.regions.centre.WorldCentrePlacablity;
import world.map.road.WTRAV;

final class GenValidator {

	private static CharSequence ¤¤error = "The player capital can not reach all of the world map. Ships can not depart or enter from the player capital.";

	static {
		D.ts(GenValidator.class);
	}
	
	public GenValidator(WorldError error) {
		
		final Rec[] bodies = new Rec[WREGIONS.MAX];
		final Coo[] firsts = new Coo[WREGIONS.MAX];

		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			
			Region reg = WORLD.REGIONS().map.get(c);
			
			if (reg != null) {
				if (bodies[reg.index()] == null) {
					firsts[reg.index()] = new Coo(c);
					bodies[reg.index()] = new Rec(1);
					bodies[reg.index()].moveX1Y1(c);
				}
				else
					bodies[reg.index()].unify(c.x(), c.y());
			}
		}

		if (bodies[WORLD.REGIONS().player.index()] == null) {
			error.problem = "no player region has been placed!";
			error.coo.set(VIEW.world().window.tiles().cX(), VIEW.world().window.tiles().cY());
			return;
		}
		
		for (int i = 0; i < WREGIONS.MAX; i++) {
			Region reg = WORLD.REGIONS().getByIndex(i);

			if (bodies[i] == null) {
				WORLD.REGIONS().getByIndex(i).info.clear();
				continue;
			}
			
			if (!reg.info.init(firsts[i].x(), firsts[i].y(), bodies[i])) {
				if (error != null) {
					error.problem = "Region is invalid. A centre can not be generated " + i;
					error.coo.set(bodies[i].cX(), bodies[i].cY());
					return;
				}else {
					LOG.ln(reg.cx() + " " + reg.cy() + " " + i + " " + firsts[i] + " " + WorldCentrePlacablity.regionMini(firsts[i].x(), firsts[i].y()));
				}

			}
			
			if (WorldCentrePlacablity.regionC(reg.cx(), reg.cy()) != null && reg != WORLD.REGIONS().player) {
				if (error != null) {
					error.problem = "Region problem: " + WorldCentrePlacablity.regionC(reg.cx(), reg.cy());
					error.coo.set(reg.cx(), reg.cy());
					return;
				}else {
					LOG.err("Region problem: " + WorldCentrePlacablity.terrainC(reg.cx(), reg.cy()) + " " + reg.cx() + " " + reg.cy() + " " + reg.index());
				}
				

				
			}
			
		}
		
		validateIfPlayerIsBlocking(error);
		
	}
	
	private void validateIfPlayerIsBlocking(WorldError error) {
		
		Region reg = WORLD.REGIONS().player;
		
		int cx = reg.cx();
		int cy = reg.cy();
		final Rec pp = new Rec(WCentre.TILE_DIM);
		pp.moveC(cx, cy);
		GUTIL.flooder().init(this);
		GUTIL.flooder().pushSloppy(reg.cx(), reg.cy(), 0);
		
		while (GUTIL.flooder().hasMore()) {
			PathTile p = GUTIL.flooder().pollSmallest();
			

			
			for (DIR d : DIR.ORTHO) {
				int dx = p.x()+d.x();
				int dy = p.y()+d.y();
				if (!WORLD.IN_BOUNDS(dx, dy))
					continue;
				if (!WTRAV.can(p.x(), p.y(), d, false))
					continue;
				GUTIL.flooder().pushSmaller(dx,  dy, p.getValue()+1);
			}
			
			
			
		}
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (WORLD.REGIONS().cTile.get(c.x(), c.y()) != null && !GUTIL.flooder().hasBeenPushed(c.x(), c.y())) {
				GUTIL.flooder().done();
				if (error != null) {
					error.problem = ¤¤error;
					error.coo.set(c.x(), c.y());
				}else {
					LOG.ln("reachability error");
				}
				
			}
		}
		GUTIL.flooder().done();
	}
	
}
