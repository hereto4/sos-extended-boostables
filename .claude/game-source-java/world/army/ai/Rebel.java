package world.army.ai;

import game.faction.FACTIONS;
import game.faction.diplomacy.DIP;
import game.time.TIME;
import snake2d.util.rnd.RND;
import util.updating.IUpdater;
import world.WORLD;
import world.army.AD;
import world.entity.army.WArmy;
import world.map.pathing.WRegFinder.RegDist;
import world.map.pathing.WRegFinder.Treaty;
import world.map.pathing.WRegSel;
import world.map.regions.Region;
import world.region.RD;

final class Rebel extends IUpdater{
	
	public Rebel() {
		super(1, TIME.secondsPerDay()/2);
	}
	
	@Override
	protected void update(int i, double timeSinceLast) {
		updateRebel(aa);
		
	}

	void updateRebel(WArmy a) {
		
		if (AD.men(null).get(a) == 0) {
			a.disband();
			return;
		}
			
		
		Region hh = a.region();
		aa = a;
		if (hh == null) {
			RegDist rr = WORLD.PATH().regFinder.single(a.ctx(), a.cty(), Treaty.REG_NEIGHS, ally.get(a));
			if (rr != null) {
				a.setDestination(rr.reg.cx(), rr.reg.cy());
			}else {
				for (RegDist r : WORLD.PATH().regFinder.all(a.ctx(), a.cty(), Treaty.REG_NEIGHS, rebelTarget.get(a))) {
					if (r != null && AD.power().get(a) > RD.MILITARY().power.getD(r.reg)) {
						a.raid(r.reg);
						return;
					}else {
						
					}
				}
				a.disband();
			}
		}else if(hh.faction() == FACTIONS.player()) {
			if (RD.DEVASTATION().current.getD(hh) < 0.9) {
				a.raid(true);
				return;
			}
			
			if (AD.power().get(a) > RD.MILITARY().power.getD(hh)) {
				a.besiege(hh);
			}else if (RND.oneIn(16)) {
				a.disband();
			}
		}else {
			for (RegDist r : WORLD.PATH().regFinder.all(a.ctx(), a.cty(), Treaty.REG_NEIGHS, rebelTarget.get(a))) {
				if (r != null && AD.power().get(a) > RD.MILITARY().power.getD(r.reg)) {
					a.raid(r.reg);
					return;
				}else {
					a.disband();
				}
			}
		}
		
		
	}
	
	private static WArmy aa;
	
	private static final Sel ally = new Sel() {
		
		@Override
		public boolean is(Region t) {
			return t.faction() == null;
		}
	};
	
	private static final Sel rebelTarget = new Sel() {
		
		@Override
		public boolean is(Region t) {
			return t.faction() == FACTIONS.player() &&  AD.power().get(aa) > power(t);
		}
		
		private double power(Region reg) {
			double m = 0;
			for (WArmy a : WORLD.ENTITIES().armies.fill(reg)) {
				if (a.faction() != null &&(a.faction() == FACTIONS.player() || DIP.get(a.faction(), FACTIONS.player()).ally))
					m += AD.power().get(a);
			}
			return m + RD.MILITARY().power.getD(reg);
		}
	};
	
	static abstract class Sel extends WRegSel {
		WArmy army;
		
		public WRegSel get(WArmy army){
			this.army = army;
			return this;
		}
	}



	
}
