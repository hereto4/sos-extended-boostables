package settlement.entity.animal.spawning;

import static settlement.main.SETT.TERRAIN;

import init.constant.C;
import init.type.TERRAIN;
import init.type.TERRAINS;
import settlement.entity.animal.Animal;
import settlement.entity.animal.AnimalSpecies;
import settlement.entity.animal.Animals;
import settlement.main.CapitolArea;
import settlement.main.SETT;
import snake2d.PathTile;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Rec;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import util.GUTIL;

class Generator {

	private final int radius = 8;
	private final Rec bounds = new Rec(SETT.TWIDTH-radius*2, SETT.THEIGHT-radius*2).moveX1Y1(radius, radius);

	Generator(Animals as, CapitolArea carea, LIST<AnimalSpawnSpot> spots){
	
		GUTIL.flooder().init(this);
		
		double[] terrains = new double[TERRAINS.ALL().size()];
		
		double tot = 0;
		for (COORDINATE c : bounds) {
			double ff = getValue(c.x(), c.y());
			if (ff >= 0) {
				tot += ff;
				if (valid(c.x(), c.y()))
					GUTIL.flooder().pushSloppy(c, ff*0-5 + RND.rFloat()).setValue2(0);
			}
			terrains[TERRAINS.sett.get(c.x(), c.y()).index()]++;
			
		}
		double area = bounds.width()*bounds.height();
		tot /= area;
		tot = Math.pow(tot, 0.5);
		for (TERRAIN t : TERRAINS.ALL())
			terrains[t.index()] /= area;
		
		double otot = 0;
		double[] occ = new double[as.species.size()];
		
		for (AnimalSpecies a : as.species) {

			double o = 0;  
			for (TERRAIN t : TERRAINS.ALL())
				o += terrains[t.index()]*a.occurence(t);
			o *= a.occurence(carea.climate());  
			otot += o;
			occ[a.index()] = o;
		}
		
		if(otot <= 0)
			return;
			
		
		int[] amounts = new int[as.species.size()];
		int[] caveAmouts = new int[as.species.size()];
		for (AnimalSpecies a : as.species) {
			occ[a.index()] /= otot; 
			double aa = (occ[a.index()]*75*tot);
			int am = (int) aa;
			aa -= am;
			if (RND.rFloat() < aa)
				am ++;
			
			amounts[a.index()] = am;
			caveAmouts[a.index()] = (int) (am*a.caveLiving);
		}
		
		LinkedList<AnimalSpawnSpot> list = new LinkedList<AnimalSpawnSpot>(spots);
		
		while(GUTIL.flooder().hasMore() && !list.isEmpty()) {
			PathTile c = GUTIL.flooder().pollGreatest();
			if (c.getValue2() != 0)
				continue;
			if (!place(c.x(), c.y(), amounts, caveAmouts, list))
				break;
		}
		
		GUTIL.flooder().done();
		
	}
	
	private boolean place(int x, int y, int[] amounts, int[] camounts, LinkedList<AnimalSpawnSpot> list) {
		int roff = RND.rInt(SETT.ANIMALS().species.size());
		
		if (SETT.TERRAIN().get(x, y).roofIs()) {
			for (int i = 0; i <SETT.ANIMALS().species.size(); i++) {
				int k = (roff+i)%amounts.length;
				int am = camounts[k];
				if (am > 0) {
					AnimalSpawnSpot spot = list.removeFirst();
					int a = place(x, y, SETT.ANIMALS().species.get(k), am, spot);
					if (a > 0) {
						spot.init(x, y, a, SETT.ANIMALS().species.get(k));
					}else
						list.add(spot);
					camounts[k] -= a;
					amounts[k] -= a;
					return true;
				}
			}
			for (int i = 0; i < SETT.ANIMALS().species.size(); i++) {
				if (amounts[i] > 0) {
					return true;
				}
			}
			return false;
		}
		
		
		
		for (int i = 0; i < SETT.ANIMALS().species.size(); i++) {
			int k = (roff+i)%amounts.length;
			int am = amounts[k];
			if (am > 0) {
				AnimalSpawnSpot spot = list.removeFirst();
				int a = place(x, y, SETT.ANIMALS().species.get(k), am, spot);
				if (a > 0) {
					spot.init(x, y, a, SETT.ANIMALS().species.get(k));
				}else
					list.add(spot);
				amounts[k] -= a;
				return true;
			}
		}
		return false;
	}
	
	private int place(int cx, int cy, AnimalSpecies animal, int am, AnimalSpawnSpot spot) {
		
		Rec rec = new Rec(radius*2);
		rec.moveC(cx, cy);
		
		
		for (COORDINATE c : rec) {
			if (SETT.IN_BOUNDS(c))
				GUTIL.flooder().setValue2(c, 1);
		}
		
		GUTIL.coos().set(0);
		
		int max = 40 + RND.rInt(20);
		if (max > am)
			max = am;
		else if(am - max < 5)
			max = am;
		am = max;
		
		int a = 0;
		for (int i = 0; GUTIL.circle().radius(i) < radius && a < am; i++) {
			if (!RND.oneIn(3 + GUTIL.circle().radius(i)))
				continue;
			int x = cx + GUTIL.circle().get(i).x();
			int y = cy + GUTIL.circle().get(i).y();
			if (!valid(x, y)) {
				continue;
			}
			GUTIL.coos().get().set(x, y);
			GUTIL.coos().inc();
			a ++;
		}
		
		if (a >= am) {
			am = 0;
			int m = GUTIL.coos().getI();
			for (int i = 0; i < m; i++) {
				GUTIL.coos().set(i);
				int ax = GUTIL.coos().get().x()*C.TILE_SIZE+C.TILE_SIZEH;
				int ay = GUTIL.coos().get().y()*C.TILE_SIZE+C.TILE_SIZEH;
				Animal an = new Animal(ax, ay, animal, spot);
				if (!an.isRemoved())
					am++;
			}
			return am;
		}
		return 0;
		
		
	}
	
	private double getValue(int x, int y) {
		
		double ff = SETT.GROUND().MOISTURE_BASE.get(x, y);
		if (ff > 0.2) {
			return 0.5 + (ff-0.2)/(0.8*2);
		}
		return -1;
	}
	
	private boolean valid(int x, int y) {
		if (!SETT.IN_BOUNDS(x, y))
			return false;	
		if (SETT.PATH().availability.get(x, y).player < 0)
			return false;
		if (TERRAIN().WATER.is.is(x, y))
			return false;
		
		double ff = SETT.GROUND().MOISTURE_BASE.get(x, y);
		if (ff > 0.2) {
			return true;
		}
		return false;
	}
	
}
