package world.map.regions;

import game.faction.FACTIONS;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.Rec;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import world.WORLD;
import world.map.regions.centre.WorldCentrePlacablity;

final class GenInit {

	public GenInit(ACTION lprinter) {
		
		Rec[] bounds = new Rec[WREGIONS.MAX];
		Coo[] ffs = new Coo[WREGIONS.MAX];
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			
			Region reg = WORLD.REGIONS().map.get(c);
			if (reg == null)
				continue;
			if (bounds[reg.index()] == null) {
				Rec rec = new Rec();
				rec.moveX1Y1(c);
				rec.setDim(1);
				ffs[reg.index()] = new Coo(c);
				bounds[reg.index()] = rec;
			}else {
				bounds[reg.index()].unify(c.x(), c.y());
			}
			
		}
		
		for (int i = 0; i < WREGIONS.MAX; i++) {
			if (bounds[i] != null) {
				Region reg = WORLD.REGIONS().getByIndex(i);
				reg.info.init(ffs[i].x(), ffs[i].y(), bounds[i]);
				centre(reg);
			}
		}
		
		
		
	}
	
	private void centre(Region a) {
		if (a == WORLD.REGIONS().player) {
			
			WORLD.REGIONS().player.fationSet(FACTIONS.player(), false);
			WORLD.REGIONS().player.setCapitol();
			WORLD.REGIONS().player.info.name().clear().add(FACTIONS.player().name);
			return;
		}
		
		if (WorldCentrePlacablity.regionC(a.info.cx(), a.info.cy()) != null)
			LOG.ln(a + " " + WorldCentrePlacablity.regionC(a.info.cx(), a.info.cy()));
		
		int bx = -1;
		int by = -1;
		double bv = 0;
		
		for (COORDINATE c : a.info.bounds()) {
			
			double v = value(c.x(), c.y(), a);

			if (v > bv) {
				bv = v;
				bx = c.x();
				by = c.y();
			}
		}
		
		if (bv > 0) {
			a.info.centreSet(bx, by);
		}
		
	}
	
	private static double value(int tx, int ty, Region r) {
		if (!test(tx, ty, r))
			return 0;
		
		double v = WORLD.MOISTURE().get(tx, ty); 
		for (DIR d : DIR.ALLC) {
			if (WORLD.WATER().has.is(tx, ty, d)) {
				v += WORLD.MOISTURE().get(tx, ty);
			}
			if (WORLD.MOUNTAIN().haser.is(tx, ty, d))
				v += WORLD.MOISTURE().get(tx, ty);
		}
		v *= RND.rFloat1(0.25);
		return v;
	}
	

	
	
	private static boolean test(int tx, int ty, Region r) {
		
		if (!WORLD.REGIONS().map.is(tx,ty,r))
			return false;
		if (WorldCentrePlacablity.regionC(tx, ty) != null)
			return false;
		return true;
		
	}

	
}
