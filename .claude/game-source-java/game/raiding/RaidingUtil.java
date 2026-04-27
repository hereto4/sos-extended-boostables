package game.raiding;

import game.GAME;
import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import game.raiding.RaidingMap.RaidEntryPoint;
import game.raiding.RaidingMap.RaidRegion;
import init.constant.Config;
import init.type.POP_CL;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import world.WORLD;
import world.army.AD;
import world.entity.army.WArmy;
import world.map.regions.Region;
import world.region.RD;

public final class RaidingUtil {

	private final ArrayList<Raider> active;
	private int cp = -1;
	private int weakest = -1;

	
	RaidingUtil(int AMOUNT) {
		active = new ArrayList<Raider>(AMOUNT);
	}
	
	public int playerPow() {
		
		if (FACTIONS.player().capitolRegion() == null)
			return 0;
		
		return defences(FACTIONS.player().capitolRegion());
	}
	
	public int defences(Region reg) {
		
		double p = RD.MILITARY().power.getD(reg);
		
		if (reg == FACTIONS.player().capitolRegion() && GAME.raiders().entry.get(FACTIONS.player().capitolRegion()).points() > 0) {
			p *= 0.25;
			for (WArmy a : WORLD.ENTITIES().armies.fill(reg))
				if (a.faction() == reg.faction())
					p += AD.power().get(a);
		}else {
			for (WArmy a : FACTIONS.player().armies().all()) {
				if (a.region() != null && a.region().faction() == FACTIONS.player())
					p += AD.power().get(a);
			}
		}
		return (int) p;
	}
	
	public int ransomCurrent() {
		return (int) ((STATS.POP().POP.data().get(null)*Config.sett().POP_RAIDER_WORTH + FACTIONS.player().credits().getD()/Config.sett().POP_RAIDER_WORTH)/BOOSTABLES.CIVICS().RAID_SECURITY.get(POP_CL.clP()));
	}
	
	public COORDINATE attackSpot(Raider raider) {
		
		double prob = 0;
		
		for (RaidRegion r : GAME.raiders().entry.entryRegions()) {
			if (defences(r.r()) < raider.army.power) {
				prob += r.r().capitol() ? 10 : 1;
			}
		}
		prob *= RND.rFloat();
		for (RaidRegion r : GAME.raiders().entry.entryRegions()) {
			if (defences(r.r()) < raider.army.power) {
				prob -= r.r().capitol() ? 10 : 1;
				if (prob <= 0) {
					prob = 0;
					
					for (RaidEntryPoint e : GAME.raiders().entry.entrySpots()) {
						if (r.r().is(e.c()))
							prob ++;
					}
					prob *= RND.rFloat();
					for (RaidEntryPoint e : GAME.raiders().entry.entrySpots()) {
						if (r.r().is(e.c())) {
							prob --;
							if (prob <= 0)
								return e.c();
						}
					}
				}
			}
		}
		
		Coo.TMP.set(FACTIONS.player().capitolRegion().cx(), FACTIONS.player().capitolRegion().cy());
		return Coo.TMP;
	}
	
	public Region weakestRegion() {
		cache();
		if (weakest == -1)
			return null;
		return WORLD.REGIONS().all().get(cp);
	}
	
	public int weakestRegionPow() {
		Region reg = weakestRegion();
		if (reg == null)
			return 0;
		return defences(reg);
	}
	
	public LIST<Raider> active(){
		cache();
		return active;
	}
	
	public boolean validCoo(COORDINATE c, Raider raider) {
		Region reg = WORLD.REGIONS().map.get(c);
		if (reg == null || reg.faction() != FACTIONS.player())
			return false;
		return defences(reg) <= raider.army.power;
	}
	
	
	private void cache() {
		
		if (Math.abs(cp-GAME.updateI()) < 128)
			return;
		
		if (cp == GAME.updateI())
			return;
		cp = GAME.updateI();
		weakest = -1;
		double power = Double.MAX_VALUE*0.5;
		
		for (RaidRegion reg : GAME.raiders().entry.entryRegions()) {
			if (reg.r().capitol())
				continue;
			
			double pow = defences(reg.r());
			if (weakest == -1 || pow < power) {
				weakest = reg.r().index();
				power = pow;
			}
		}
		
		active.clearSloppy();
		
		for (Raider r : GAME.raiders().ALL()) {
			if (!r.defeated && !r.isScared() && r.hasInterrest())
				active.add(r);
		}
		
	}

	public void clear() {
		cp = -1;
		
	}
	
}
