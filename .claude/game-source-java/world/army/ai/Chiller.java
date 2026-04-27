package world.army.ai;

import game.faction.FACTIONS;
import game.faction.Faction;
import snake2d.PathTile;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import world.WORLD;
import world.entity.army.WArmy;
import world.entity.army.WArmyState;
import world.map.pathing.WRegFinder.RegDist;
import world.map.pathing.WRegFinder.Treaty;
import world.map.pathing.WRegSel;
import world.region.RD;

final class Chiller {

	Chiller(){

	}
	
	public void chill(Faction f, ArrayList<WArmy> armies) {

		if (armies.size() > 0 && RND.oneIn(2) && RD.DIST().factionHasRegionBorderingPlayer(f)) {
			
			WArmy a = armies.rnd();
			
			if (a.state() == WArmyState.fortified) {
				LIST<RegDist> l = WORLD.PATH().regFinder.all(a.ctx(), a.cty(), Treaty.FACTION_BORDERS, WRegSel.FACTION(FACTIONS.player()));
				if (l.size() == 0)
					return;
				RegDist d = l.rnd();
				PathTile t = WORLD.PATH().path(a.ctx(), a.cty(), d.reg.cx(), d.reg.cy(), Treaty.FACTION_BORDERS);
				if (t != null) {
					PathTile ok = null;
					while(t != null) {
						if (WORLD.REGIONS().faction.get(t) != a.faction())
							ok = null;
						else if (ok == null)
							ok = t;
						t = t.getParent();
					}
					if (ok != null)
						a.setDestination(ok.x(), ok.y());
				}
				
			}
			
		}
		
		
		

	}



}
