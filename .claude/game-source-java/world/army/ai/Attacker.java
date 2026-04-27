package world.army.ai;

import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.LIST;
import view.tool.PlacableSimpleTile;
import view.world.panel.IDebugPanelWorld;
import world.WORLD;
import world.army.AD;
import world.entity.army.WArmy;
import world.entity.army.WArmyState;
import world.map.pathing.WRegFinder.RegDist;
import world.map.pathing.WRegFinder.Treaty;
import world.map.pathing.WRegSel;
import world.map.regions.Region;
import world.map.regions.WREGIONS;
import world.region.RD;

final class Attacker {

	private final Bitmap1D check = new Bitmap1D(WREGIONS.MAX, false);

	Attacker(){
		IDebugPanelWorld.add(new PlacableSimpleTile("army debug") {
			
			@Override
			public void place(int tx, int ty) {
				LOG.ln("test " + tx + " " + ty);
				LIST<RegDist> ds = WORLD.PATH().regFinder.all(tx, ty, Treaty.FACTION_BORDERS, WRegSel.ENEMY(WORLD.REGIONS().map.get(tx, ty).faction()));
			
				for (RegDist d : ds) {
					LOG.ln(d.reg + " " + d.reg.faction());
				}
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty) {
				if (WORLD.PATH().map.is.is(tx, ty) && WORLD.REGIONS().map.get(tx, ty) != null && WORLD.REGIONS().map.get(tx, ty).faction() != null) {
					return null;
				}
				return E;
			}
		});
	}
	
	public void attack(Faction f, ArrayList<WArmy> armies) {

		if (armies.size() == 0)
			return;
		
		check.clear();
		
		double enemyPower = 0;
		
		for (Faction e : DIP.WAR().all(f)) {
			enemyPower += AD.power().get(e);
		}
		
		double power = 0;
		
		for (WArmy a : armies)
			power += AD.power().get(a);
		
		
		
		power -= enemyPower;
		if (War.logging) {
			War.log(f, ""+power);
		}
		
		while(power > 0 && armies.size() > 0) {
			WArmy a = armies.removeLast();
			power -= AD.power().get(a);
			attack(a);
		}
		
		while(armies.size() > 0) {
			WArmy a = armies.removeLast();
			guard(a);
		}
		

	}
	
	
	private void guard(WArmy a) {
		if (a.faction() == null)
			return;
		if (DIP.WAR().all(a.faction()).size() > 0) {
			if (a.region() == a.faction().capitolRegion())
				return;
			if (a.state() == WArmyState.moving && WORLD.REGIONS().map.get(a.path().destX(), a.path().destY()) == a.faction().capitolRegion())
				return;
			COORDINATE c = WORLD.PATH().rnd(a.faction().capitolRegion());
			if (c != null) {
				a.setDestination(c.x(), c.y());
				
			}
			return;
		}
		
		
		
		
		
		
	}


	private void attack(WArmy a) {
		if (a.region() != null && a.region().faction() == a.faction() && AD.supplies().health(a) < 1) {
			a.stop();
			if (War.logging) {
				War.log(a, "stop");
			}
			return;
		}
		
		
		LIST<RegDist> ds = WORLD.PATH().regFinder.all(a.ctx(), a.cty(), Treaty.FACTION_BORDERS, WRegSel.ENEMY(a.faction()));
		Region best = null;
		double bestValue = 0;
		double pow = AD.power().get(a);
		
		for (RegDist d : ds) {
			if (DIP.WAR().is(a.faction(), FACTIONS.player())) {
				
			}
			double v = pow/(RD.MILITARY().power.getD(d.reg)+100);
			if (v > 1.0) {
				v/= d.distance;
				if (v > bestValue) {
					best = d.reg;
				}
			}
			
		}
		
		if (War.logging) {
			War.log(a, " " + best);
		}
		
		if (best != null) {
			
			if (a.state() != WArmyState.besieging || a.region() != best)
				a.besiege(best);
		}else {
			guard(a);
		}
	}


}
