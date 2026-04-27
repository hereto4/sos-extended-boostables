package settlement.tilemap.generator;

import settlement.main.CapitolArea;
import settlement.main.SETT;
import settlement.main.SettlementGrid;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.HeightMap;
import world.WORLD;

class GeneratorFertilityInit {

	private final double aI = 1.0/SettlementGrid.QUAD_AREA;
	
	GeneratorFertilityInit(CapitolArea area, GeneratorUtil util) {
		
		
		HeightMap ferMap = new HeightMap(SETT.TWIDTH, SETT.THEIGHT, 32, 2);
		
		for (COORDINATE c : SETT.TILE_BOUNDS) {
			
			double base = getBase(area, c);
			double f = ferMap.get(c);
			double h = 1.0-util.height.get(c);
			double res = get(base, f, h);
			util.fer.set(c, res);
			

			
			
		}

	}
	
	private double getBase(CapitolArea area, COORDINATE c) {
		double v = 0;
		int wx = area.tiles().x1() + c.x()/SettlementGrid.QUAD_SIZE;
		int wy = area.tiles().y1() + c.y()/SettlementGrid.QUAD_SIZE;
		
		int dx = (c.x()%SettlementGrid.QUAD_SIZE)- SettlementGrid.QUAD_HALF;
		int dy = (c.y()%SettlementGrid.QUAD_SIZE) -SettlementGrid.QUAD_HALF;
		
		double ax = Math.abs(dx)*(SettlementGrid.QUAD_SIZE-Math.abs(dy));
		double ay = Math.abs(dy)*(SettlementGrid.QUAD_SIZE-Math.abs(dx));
		double axy = Math.abs(dx)*Math.abs(dy);
		double a = SettlementGrid.QUAD_AREA-ax-ay-axy;
		
		v += WORLD.GROUND().moisture.get(wx, wy)*a;
		
		for (DIR d : DIR.ALL) {
			
			if (d.x()*dx < 0)
				continue;
			if (d.y()*dy < 0)
				continue;
			
			if (d.x() != 0 && d.y() != 0) {
				v+= WORLD.MOISTURE().get(wx+d.x(), wy+d.y())*axy;
			}else if (d.x() != 0) {
				v+= WORLD.MOISTURE().get(wx+d.x(), wy)*ax;
			}else if(d.y() != 0) {
				v+= WORLD.MOISTURE().get(wx, wy+d.y())*ay;
			}
			
		}
		
		
		return v*aI;
	}

	static double get(double base, double fe, double hi) {

		base = CLAMP.d(base, 0, 1)*0.7;
		
		double f = hi;
		f = Math.pow(f, 1 + 8*(1-base));
		//f = Math.pow(f, 1 + (1-base));
		
		f = CLAMP.d(base + f, 0, 1);
		f -= 0.2*fe;

		return CLAMP.d(f, 0, 1);
	}



}
