package settlement.room.industry.module;

import java.util.Arrays;

import game.GAME;
import game.boosting.BOOSTING;
import game.boosting.Boostable;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.type.POP_CL;
import settlement.room.industry.module.FlatIndustries.FlatIndustry;
import settlement.room.main.ROOMS;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintImp;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;

public class RoomIndustries {

	public final LIST<Industry> all;
	public final FlatIndustries flat;
	public double[] rates = new double[RESOURCES.ALL().size()];
	public double[] ratescl = new double[RESOURCES.ALL().size()];
	private int rateI = -1;
	private int rateII = -1;
	
	public RoomIndustries(ROOMS rooms){
		int am = 0;
		for (RoomBlueprint b : rooms.all()) {
			if (b instanceof INDUSTRY_HASER) {
				INDUSTRY_HASER h = (INDUSTRY_HASER) b;
				am += h.industries().size();
			}
				
		}
		
		ArrayList<Industry> hh = new ArrayList<>(am);
		for (RoomBlueprint b : rooms.all()) {
			if (b instanceof INDUSTRY_HASER) {
				INDUSTRY_HASER h = (INDUSTRY_HASER) b;
				for (Industry i : h.industries())
					hh.add(i);
			}
				
		}
		all = new ArrayList<>(hh);
		flat = new FlatIndustries(rooms, this);
		
		Arrays.fill(rates, Double.MAX_VALUE);
		for (int i = 0; i < 10; i++) {
			for (FlatIndustry ii : flat.all()) {
				set(ii);
			}
		}
		
		
	}
	
	private boolean set(FlatIndustry ii) {
		
		if (ii.industry.ins().size() > 0) {
			for (IndustryResource i : ii.industry.ins()) {
				if (rates[i.resource.index()] == Double.MAX_VALUE)
					return true;
			}
		}
		boolean has = false;
		for (IndustryResource oo : ii.industry.outs()) {
			double mm = 0;
			for (IndustryResource i : ii.industry.ins()) {
				if (rates[i.resource.index()] == Double.MAX_VALUE)
					return true;
				mm += i.rate*rates[i.resource.index()]/oo.rate;
			}
			
			
			mm += 1.0 / oo.AI;
			
			if (mm < rates[oo.resource.index()]) {
				rates[oo.resource.index()] = mm;
				has = true;
			}
		}
		return has;
	}
	
	public double vanillaRate(RESOURCE res) {
		return rates[res.index()];
	}
	
	public double rate(POP_CL cl, RESOURCE res) {
		if (rateI == GAME.updateI() && rateII == cl.index())
			return ratescl[res.index()];
		rateI = GAME.updateI();
		rateII = cl.index;
		
		Arrays.fill(ratescl, Double.MAX_VALUE);
		for (int k = 0; k < 10; k++) {
			for (FlatIndustry ii : flat.all()) {
				if (ii.industry.ins().size() > 0) {
					for (IndustryResource i : ii.industry.ins()) {
						if (ratescl[i.resource.index()] == Double.MAX_VALUE)
							continue;
					}
				}
				for (IndustryResource oo : ii.industry.outs()) {
					double mm = 0;
					for (IndustryResource i : ii.industry.ins()) {
						if (ratescl[i.resource.index()] == Double.MAX_VALUE)
							continue;
						mm += i.rate*ratescl[i.resource.index()]/oo.rate;
					}
					
					
					mm += 1.0 / (oo.AI*ii.industry.bonus().get(cl));
					
					if (mm < ratescl[oo.resource.index()]) {
						ratescl[oo.resource.index()] = mm;
					}
				}
			}
		}
		
		
		return ratescl[res.index()];
	}
	
	public static final class RBonus {

		
		public final LIST<RoomBlueprintImp> all;
		private final RoomBlueprintImp[] map = new RoomBlueprintImp[BOOSTING.ALL().size()];
		{
			
		}
		
		public RoomBlueprintImp get(Boostable bo) {
			if (bo.index() >= map.length)
				return null;
			return map[bo.index()];
			
		}
		
		RBonus(LIST<RoomBlueprint> rooms){
			LinkedList<RoomBlueprintImp> all = new LinkedList<>();
			
			for (RoomBlueprint p : rooms) {
				if (p instanceof RoomBlueprintImp) {
					RoomBlueprintImp b = (RoomBlueprintImp) p;
					if (b.bonus() != null) {
						map[b.bonus().index()] = b;
						all.add(b);
					}
				}
			}
			this.all = new ArrayList<RoomBlueprintImp>(all);
		}
		
	}
	
}
