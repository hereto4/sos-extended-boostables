package settlement.maintenance;

import java.util.Arrays;

import init.resources.RESOURCE;
import init.resources.RESOURCES;
import settlement.main.SETT;
import settlement.room.food.fish.ROOM_FISHERY;
import settlement.room.industry.mine.ROOM_MINE;
import settlement.room.industry.module.FlatIndustries.FlatIndustry;
import settlement.room.industry.module.IndustryResource;
import settlement.room.industry.refiner.ROOM_REFINER;
import settlement.room.industry.workshop.ROOM_WORKSHOP;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomBlueprintIns;
import settlement.room.main.furnisher.FurnisherItemGroup;
import snake2d.LOG;
import snake2d.util.sets.Bitmap1D;

class Test {

	private double[] acc = new double[RESOURCES.ALL().size()];

	private final double rate = 1.0/64.0;
	private final Bitmap1D check = new Bitmap1D(SETT.ROOMS().AMOUNT_OF_BLUEPRINTS, false);
	
	Test() {

		Arrays.fill(acc, Double.MAX_VALUE);
		for (int i = 0; i < 10; i++) {
			for (FlatIndustry ii : SETT.ROOMS().industries.flat.all()) {
				set(ii);
			}
		}
		for (RESOURCE res : RESOURCES.ALL()) {
			l(res.key + "\t" + dd(acc[res.index()]));
		}

		l("");

		{
			for (ROOM_MINE m : SETT.ROOMS().MINES) {

				double[][] groups = new double[][] {
						{ 1, (1.0 / m.constructor().groups().get(1).stat(2)) }, };

				print(m, groups);

			}
		}
		l("");
		{
			for (ROOM_REFINER m : SETT.ROOMS().REFINERS) {
				double[][] groups = new double[][] { { 0, 1.0/m.constructor().groups().get(0).stat(0)}, { 2, 1.0/(m.constructor().groups().get(2).stat(1)) }, };

				print(m, groups);


			}
		}
		l("");

		{
			for (ROOM_WORKSHOP m : SETT.ROOMS().WORKSHOPS) {

				double[][] groups = new double[][] { {1, 1.0/m.constructor().groups().get(1).stat(0)}, { 2, 1.0/ m.constructor().groups().get(2).stat(1) }, };

				print(m, groups);

			}
		}
		l("");

		{
			for (ROOM_FISHERY m : SETT.ROOMS().FISHERIES) {

				double[][] groups = new double[][] { { 1, 1.0 / m.constructor().groups().get(1).stat(2) }, };

				print(m, groups);

			}
		}
		l("");
		
		{
			RoomBlueprintIns<?> m = SETT.ROOMS().WOOD_CUTTER;
			double[][] groups = new double[][] {{ 1, 1.0 / m.constructor().groups().get(1).stat(1) }, };
			print(m, groups);
		}
		
		for (RoomBlueprint bb : SETT.ROOMS().all()) {
			if (check.get(bb.index()))
				continue;
			if (bb instanceof RoomBlueprintImp) {
				RoomBlueprintImp b = (RoomBlueprintImp) bb;
				
				double max = 0;
				for (FurnisherItemGroup i : b.constructor().groups()) {
					for (int uI = 0; uI <= b.upgrades().max(); uI++) {
						double m = 0;
						for (int ri = 0; ri < b.constructor().resources(); ri++) {
							m += i.cost(ri, ri)*acc[b.constructor().resource(ri).index()];
						}
						m /= i.item(0, 0).area;
						max = Math.max(max, m);
					}
					
					
				}
				for (int uI = 0; uI <= b.upgrades().max(); uI++) {
					double m = 0;
					for (int ri = 0; ri < b.constructor().resources(); ri++) {
						m += b.constructor().areaCost(ri, uI)*acc[b.constructor().resource(ri).index()];
					}
					max = Math.max(max, m);
				}
				l(b.key + " " + max*b.degradeRate());
				
			}
		}
		
	}
	
	private void print(RoomBlueprintIns<?> m, double[][] groups) {
		
		check.set(m.index(), true);
		l(m.key + " " + m.degradeRate());
		String res = "";
		for (int i = 0; i < m.constructor().resources(); i++) {
			res += m.constructor().resource(i) + " "
					+  dd((acc[m.constructor().resource(i).index()])) + " | ";
			
		}
		l("    res: " + res);

		res = "";
		for (double[] gi : groups) {
			FurnisherItemGroup g = m.constructor().groups().get((int) gi[0]);
			res += g.name + " " + gi[1] + " | ";
		}
		l("    items: : " + res);
		double prev = 0;
		
		for (int i = 0; i <= m.upgrades().max(); i++) {
			
			double bo = m.upgrades().boost(i);
			if (i > 0)
				bo -= m.upgrades().boost(i-1);
			double mm = 0;
			for (double[] gi : groups) {
				FurnisherItemGroup g = m.constructor().groups().get((int) gi[0]);
				
				for (int ri = 0; ri < m.constructor().resources(); ri++) {
					
					mm += g.cost(ri, i)*gi[1]*acc[m.constructor().resource(ri).index()];
				}
			}
			
			mm*= m.degradeRate();
			mm -= prev;
			prev += mm;
			
			if (i == 0)
				bo = 1;
			double d = mm/bo;
			

			l("  #" + i + ": " + dd(100*d*rate) + "%");

			
		}
	}
	
	private static void l(String s) {
		LOG.err(s);
	}
	
	private static String dd(double d) {
		return String.format( "%.2f", d);
		
	}
	
	private boolean set(FlatIndustry ii) {
		
		if (ii.industry.ins().size() > 0) {
			for (IndustryResource i : ii.industry.ins()) {
				if (acc[i.resource.index()] == Double.MAX_VALUE)
					return true;
			}
		}
		boolean has = false;
		for (IndustryResource oo : ii.industry.outs()) {
			double mm = 0;
			for (IndustryResource i : ii.industry.ins()) {
				if (acc[i.resource.index()] == Double.MAX_VALUE)
					return true;
				mm += i.rate*acc[i.resource.index()]/oo.rate;
			}
			
			
			mm += 1.0 / oo.rate;
			
			if (mm < acc[oo.resource.index()]) {
				acc[oo.resource.index()] = mm;
				has = true;
			}
		}
		return has;
	}

}
