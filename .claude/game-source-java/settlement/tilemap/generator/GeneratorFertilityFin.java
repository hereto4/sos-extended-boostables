package settlement.tilemap.generator;

import static settlement.main.SETT.THEIGHT;
import static settlement.main.SETT.TWIDTH;

import settlement.main.CapitolArea;
import settlement.main.SETT;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Rec;

class GeneratorFertilityFin {

	static final double T_ROCK = 0.30;
	
	GeneratorFertilityFin(CapitolArea area, GeneratorUtil util) {
		
		SETT.GROUND().setColors(area.climate().colorGroundDry, area.climate().colorGroundWet, 0);
		
		for (int y = 0; y < THEIGHT; y++) {
			for (int x = 0; x < TWIDTH; x++) {
				if (SETT.TERRAIN().is(x, y)) {
					SETT.TERRAIN().get(x, y).placeFixed(x, y);
				}
			}
		}
		
		SETT.ENV().map.initWater();
		
		for (COORDINATE c : new Rec(SETT.TILE_BOUNDS)) {
			
			int x = c.x();
			int y = c.y();

			double v = util.fer.get(c.x(), c.y());			

			util.fer.set(c, v);
			v = util.fer.get(c.x(), c.y());
			
			
			
			SETT.GRASS().current.set(x, y, v);
			
			SETT.GRASS().grow(c.x(), c.y(), 16);
			
		}

	}
	



}
