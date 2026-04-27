package game.events.killer;

import game.faction.FACTIONS;
import init.race.RACES;
import init.race.Race;
import init.type.HCLASSES;
import settlement.entity.ENTITY;
import settlement.entity.EntityIterator;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.STATS;
import snake2d.util.rnd.RND;
import util.data.GETTER;
import util.data.GETTER.GETTER_IMP;

class Util {

	public static int pickKiller() {
		
		GETTER.GETTER_IMP<Humanoid> res = new GETTER_IMP<>();
		
		new EntityIterator.Humans() {
			
			@Override
			protected boolean processAndShouldBreakH(Humanoid h, int ie) {
				if (isGoodKiller(h) != null) {
					res.set(h);
					if (RND.rBoolean())
						return true;
				}
				return false;
			}
		}.iterate(RND.rInt()&Integer.MAX_VALUE);
		if (res.get() == null)
			return -1;
		return res.get().id();
		
	}
	
	public static Humanoid isGoodKiller(ENTITY e) {
		if (e == null)
			return null;
		if (e instanceof Humanoid) {
			Humanoid a = (Humanoid) e;
			if (a.indu().clas() == HCLASSES.CITIZEN() && a.race().playable)
				return a;
		}
		return null;
	}
	
	public static int pickRace() {
		double pop = 0;
		for (Race race : RACES.all()) {
			if (race.playable) {
				pop += STATS.POP().POP.data().get(race);
			}
		}
		
		pop *= RND.rFloat();
		
		for (Race race : RACES.all()) {
			if (race.playable) {
				pop -= STATS.POP().POP.data().get(race);
				if (pop < 0)
					return race.index;
			}
		}
		
		return FACTIONS.player().race().index;
		
	}
	
}
