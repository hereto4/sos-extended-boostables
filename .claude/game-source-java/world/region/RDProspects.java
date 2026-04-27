package world.region;

import settlement.room.industry.module.Industry;
import settlement.room.industry.module.IndustryRegion;
import snake2d.LOG;
import snake2d.util.sets.ArrayList;
import util.data.INT_O.INT_OE;
import world.WORLD;
import world.map.regions.Region;
import world.map.regions.WREGIONS;
import world.region.RD.RDInit;

public class RDProspects {

	private final ArrayList<II> all = new ArrayList<II>(IndustryRegion.ALL().size());
	private boolean init = false;
	
	public RDProspects(RDInit init) {
		for (IndustryRegion ii : IndustryRegion.ALL()) {
			all.add(new II(init, ii));
		}
		
		
		
	}
	
	public double get(Industry ins, Region reg) {
		
		return get(ins.reg(), reg);
	}
	
	public double get(IndustryRegion ins, Region reg) {
		if (reg == null)
			return 0;
		return all.get(ins.index).data.getD(reg);
	}
	
	
	public double getAi(Industry ins, Region reg) {
		return getAi(ins.reg(), reg);
	}
	
	
	private static boolean log = false;
	
	public void tt() {
		init = false;
		init();
	}
	
	private void init() {
		if (init)
			return;
		init = true;
		for (II ii : all) {
			double am = 0;
			for (Region reg : WORLD.REGIONS().active()) {
				am += ii.data.get(reg);
			}
			if (log)
				LOG.ln(ii.reg.ins.blue.key + " " + ii.reg.rarity + " " + am + " " + am/WORLD.REGIONS().active().size());
			am/= WORLD.REGIONS().active().size();
			ii.average = 1.0/am;
		}	
		
		for (II ii : all) {
			double min = Integer.MAX_VALUE;
			double max = 0;
			double am = 0;
			for (Region reg : WORLD.REGIONS().active()) {
				double a = getAi(ii.reg, reg);
				am += a;
				min = Math.min(a, min);
				max = Math.max(max, a);
			}
			
			am/= WORLD.REGIONS().active().size();
			if (log)
				LOG.ln(ii.reg.ins.blue.key + " " +am + " " + min + " <-> " + max);
		}	
	}
	
	public double getAi(IndustryRegion ins, Region r) {
		if (ins == null)
			return 1;
		init();
		return 0.8 + 0.2*all.get(ins.index).data.get(r)*all.get(ins.index).average;
	}
	
	private class II {
		
		private final INT_OE<Region> data;
		private final IndustryRegion reg;
		private double average;
		
		II(RDInit init, IndustryRegion reg){
			data = init.count.new DataCrumb("PROSPECT_" + reg.ins.blue.key);
			this.reg = reg;
		}
		
	}
	
	void generate(){
		
		for (Region r : WORLD.REGIONS().all()) {
			for (II ii : all) {
				ii.data.set(r, 0);
				ii.average = 0;
			}
		}
		
		int regs = WORLD.REGIONS().active().size();
		
		
		double amPerRegion = 2;
		double tot = regs*amPerRegion;
		double rareTot = 0;
		for (IndustryRegion ii : IndustryRegion.ALL()) {
			rareTot += ii.rarity;
		}
		
		int[] toAssign = new int[IndustryRegion.ALL().size()];
		int[] assigned = new int[WREGIONS.MAX];
		
		for (int i = 3; i > 0; i--) {
			for (IndustryRegion ii : IndustryRegion.ALL()) {
				toAssign[ii.index] = (int) Math.ceil(tot*ii.rarity/rareTot);
				
			}
			
			while(assign(toAssign, assigned, i))
				;
		}
		
		for (II ii : all) {
			double am = 0;
			for (Region reg : WORLD.REGIONS().active()) {
				am += ii.data.get(reg);
			}
			
			am/= WORLD.REGIONS().active().size();
			ii.average = am;
		}	
		
		init = false;
		init();
		
	}

	
	private boolean assign(int[] toAssign, int[]assigned, int value) {
		boolean a = false;
		for (IndustryRegion ii : IndustryRegion.ALL()) {
			if (toAssign[ii.index] > 0) {
				
				
				Region best = null;
				double bv = Double.NEGATIVE_INFINITY;
				
				for (Region reg : WORLD.REGIONS().active()) {
					
					if (reg != WORLD.REGIONS().player && all.get(ii.index).data.get(reg) == 0) {
						double v = ii.occurence(reg)/(1.0+assigned[reg.index()]);
						if (v > bv) {
							
							bv = v;
							best = reg;
						}
					}
				}
				
				if (best != null) {
					all.get(ii.index).data.set(best, value);
					toAssign[ii.index] --;
					assigned[best.index()]++;
					a = true;
				}
			}
		}
		return a;
	}
}
