package world.map.regions;

import java.util.Comparator;

import game.faction.FACTIONS;
import init.constant.Config;
import init.sprite.SPRITES;
import snake2d.PathTile;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.Polymap;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import util.rendering.RenderData.RenderIterator;
import util.GUTIL;
import util.rendering.ShadowBatch;
import world.WORLD;
import world.map.regions.centre.WCentre;
import world.map.regions.centre.WorldCentrePlacablity;
import world.map.road.WTRAV;
import world.overlay.WorldOverlays;

final class GenAssign {

	private final Polymap pmap = new Polymap(WORLD.TWIDTH(), WORLD.THEIGHT(), 8, 1);
	
	public GenAssign(ACTION lprinter) {
		
		if (!GenPlayer.gen())
			return;
		
		WORLD.OVERLAY().debug = new WorldOverlays.OverlayTile(true, false) {

			@Override
			protected void renderBelow(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
				COLOR.UNIQUE.getC(pmap.get(it.tx(), it.ty())).bind();
				SPRITES.cons().BIG.outline.render(r, 0, it.x(), it.y());
				COLOR.unbind();
			}
			
		};
		
		ArrayList<Tile> tiles = new ArrayList<Tile>(WORLD.TAREA());
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			
			if (canBeCentre(c)) {
				tiles.add(new Tile(c));
			}
		}
		
		tiles.sort(new Comparator<Tile>() {
			
			@Override
			public int compare(Tile o1, Tile o2) {
				return o1.value > o2.value ? 1 : -1;
			}
		});
		
		int ri = 1;
		for (Tile t : tiles) {
			if (ri > WREGIONS.MAX)
				return;
			if (canBeCentre(t)) {
				assign(t, WORLD.REGIONS().getByIndex(ri));
				ri++;
			}
			
		}
				
		lprinter.exe();
		expand();
		
		
	}
	
	void expand() {
		pmap.checkInit();
		GUTIL.flooder().init(this);
		for (COORDINATE c : WORLD.TBOUNDS()) {
			
			Region reg = WORLD.REGIONS().map.get(c);
			
			if (reg != null && reg != WORLD.REGIONS().player) {
				GUTIL.flooder().pushSloppy(c, 0);
				GUTIL.flooder().setValue2(c, reg.index());
			}
		}
		
		while(GUTIL.flooder().hasMore()) {
			PathTile t = GUTIL.flooder().pollSmallest();
			Region dadda = WORLD.REGIONS().getByIndex(Math.round(t.getValue2()));
			WORLD.REGIONS().pmap.set(t, dadda);
			for (DIR d : DIR.ORTHO) {
				if (isExpandable(t.x(), t.y(), d, dadda))
					if (GUTIL.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance()*expandValue(t.x(), t.y(), d)) != null) {
						GUTIL.flooder().setValue2(t.x(), t.y(), d, dadda.index());
					}
			}
		}
		GUTIL.flooder().done();
	}
	
	void assign(COORDINATE centre, Region reg) {
		
		GUTIL.flooder().init(this);
		pmap.checkInit();
		
		
		for (int y = -(WCentre.TILE_DIM/2); y <= WCentre.TILE_DIM; y++) {
			for (int x = -(WCentre.TILE_DIM/2); x <= WCentre.TILE_DIM; x++) {
				int dx = centre.x()+x;
				int dy = centre.y()+y;
				GUTIL.flooder().pushSloppy(dx, dy, 0);
				WORLD.REGIONS().pmap.set(dx, dy, reg);
			}
		}
		
		int area = 0;
		
		double size = (WCentre.TILE_DIM+2)*(WCentre.TILE_DIM+2)+Config.world().REGION_SIZE;
		while(GUTIL.flooder().hasMore()) {
			
			PathTile t = GUTIL.flooder().pollSmallest();
			if (WORLD.REGIONS().pmap.get(t) != null && WORLD.REGIONS().pmap.get(t) != reg)
				continue;
			
			pmap.checker.set(t, true);
			
			
			area++;
			WORLD.REGIONS().pmap.set(t, reg);
			size-= tileValue(t.x(), t.y());
			if (size < 0) {
				GUTIL.flooder().done();
				return;
			}
			
			if (t.getValue() > Math.sqrt(area)*2.0)
				continue;
			
			for (DIR d : DIR.ORTHO) {
				if (WTRAV.canLand(t.x(), t.y(), d, false))
					GUTIL.flooder().pushSmaller(t, d, t.getValue()+d.tileDistance()*expandValue(t.x(), t.y(), d));
			}
			
		}
		GUTIL.flooder().done();
		
	}
	
	private double expandValue(int fromX, int fromY, DIR dir) {
		if (terrain(fromX, fromY) != terrain(fromX+dir.x(), fromY+dir.y()))
			return 5;
		if (!pmap.checker.is(fromX+dir.x(), fromY+dir.y()))
			return 20;
		return 1;
	}
	
	public static int terrain(int tx, int ty) {
		if (WORLD.MOUNTAIN().getHeight(tx, ty) > 0)
			return 1;
		
		if (WORLD.WATER().isBig.is(tx, ty)) {
			return 2;
		}
		if (WORLD.FOREST().amount.get(tx, ty) == 1)
			return 3;
		return 0;
	
		
	}
	
	private boolean isExpandable(int fromX, int fromY, DIR dir, Region reg) {
		
		int tx = fromX + dir.x();
		int ty = fromY + dir.y();
		if (!WORLD.IN_BOUNDS(tx, ty))
			return false;
		
		if (WORLD.REGIONS().pmap.get(tx, ty) != null && WORLD.REGIONS().pmap.get(tx,ty,dir) != reg)
			return false;
		

		
		if (WORLD.MOUNTAIN().coversTile(fromX, fromY)  && WORLD.MOUNTAIN().coversTile(tx, ty))
			return false;

		if (WORLD.MOUNTAIN().coversTile(tx, ty)) {
			for (int di = 0; di< DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				if (!WORLD.MOUNTAIN().coversTile(tx+d.x(), ty+d.y()))
					return true;
			}
			return false;
		}
		if (WORLD.WATER().has.is(tx, ty)) {
			if (!WORLD.WATER().coversTile.is(tx, ty))
				return true;
			for (int di = 0; di< DIR.ALL.size(); di++) {
				DIR d = DIR.ALL.get(di);
				if (WORLD.IN_BOUNDS(tx, ty,d) && !WORLD.WATER().coversTile.is(tx+d.x(), ty+d.y()))
					return true;
			}
			return false;
		}
		return true;
	}
	
	private boolean canBeCentre(COORDINATE c) {
		if (WorldCentrePlacablity.terrain(c.x(), c.y()) == null) {
			for (int y = -(1+WCentre.TILE_DIM/2); y < WCentre.TILE_DIM+2; y++) {
				for (int x = -(1+WCentre.TILE_DIM/2); x < WCentre.TILE_DIM+2; x++) {
					int dx = c.x()+x;
					int dy = c.y()+y;
					if (!WORLD.IN_BOUNDS(dx, dy))
						return false;
					if (WORLD.REGIONS().map.get(dx, dy) != null)
						return false;
					
					
					
				}
			}
			return true;
		}
		return false;
	}
	
	
	private static class Tile extends Coo {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public final double value;
		
		Tile(COORDINATE c){
			set(c);
			this.value = value(c.x(), c.y());
		}
		
		private double value(int tx, int ty) {
			
			double v = WORLD.MOISTURE().get(tx, ty); 
			for (DIR d : DIR.ALLC) {
				if (WORLD.WATER().has.is(tx, ty, d)) {
					v += WORLD.MOISTURE().get(tx, ty);
				}
				if (WORLD.MOUNTAIN().haser.is(tx, ty, d))
					v += WORLD.MOISTURE().get(tx, ty);
			}
			v *= RND.rFloat1(0.25);
			return v + RND.rFloat();
		}
		
	}
	
	private static double tileValue(int tx, int ty) {
		double v = CLAMP.d(WORLD.MOISTURE().get(tx, ty), 0.05, 1);
		if (WORLD.WATER().has.is(tx, ty)) {
			v = 0.75+0.25*v;
		}
		if (WORLD.MOUNTAIN().haser.is(tx, ty))
			v = 0.5 + v*0.5;
		if (WORLD.FOREST().is.is(tx, ty))
			v = 0.2 + v*0.8;
		return v;
	}

	
	public void clear() {
		while(FACTIONS.NPCs().size() > 0) {
			FACTIONS.remove(FACTIONS.NPCs().get(0), false);
		}
		WORLD.REGIONS().saver().clear();
	}


	
}
