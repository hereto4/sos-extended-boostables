package game.event.engine;

import game.faction.Faction;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import init.type.CLIMATE;
import init.type.CLIMATES;
import init.type.TERRAIN;
import init.type.TERRAINS;
import init.value.GVALUES;
import init.value.Lockable;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.util.file.Json;

final class EOccurence {

	public final Lockable<Faction> plockable = GVALUES.FACTION.LOCK.push();
	public final double[] coccurence = new double[CLIMATES.ALL().size()];
	public final double[] roccurence = new double[RACES.all().size()];
	public final double[] toccurence = new double[TERRAINS.ALL().size()];
	public final int maxSpawns;
	public double onlyAfterTime;
	
	
	public EOccurence(Json data, EventCollection engine, Event parent) {
		
		if (data.has("OCCURENCE")) {
			data = data.json("OCCURENCE");
			data.value("TYPE", "");
			CLIMATES.MAP().readFill("CLIMATE", coccurence, data, 0, 10000000);
			RACES.map().readFill("RACE", roccurence, data, 0, 10000000);
			TERRAINS.MAP().readFill("TERRAIN", toccurence, data, 0, 100000);
			maxSpawns = data.i("MAX_SPAWNS", 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
			
			onlyAfterTime = data.dTry("ONLY_AFTER_DAYS", 0, 100000, 0)*TIME.secondsPerDay();
			plockable.push(data);
			data.checkUnused();
			
		}else {
			maxSpawns = 10;
		}
		
		
	}

	public double occurence(Race race) {
		double occ = 0;
		
		for (TERRAIN t : TERRAINS.ALL()) {
			occ += toccurence[t.index()]*SETT.WORLD_AREA().info.get(t).getD();	
		}
		CLIMATE climate = SETT.ENV().climate();
		occ *= coccurence[climate.index()]*roccurence[race.index()];
		return occ;
	}
	
	public double race(Race race) {
		return roccurence[race.index()];
	}
	
	public double occurence() {
		double occ = 0;
		
		for (TERRAIN t : TERRAINS.ALL()) {
			double d = toccurence[t.index()]*SETT.WORLD_AREA().info.get(t).getD();
			if (d > occ)
				occ = d;
		}
		
		double raM = 0;
		double tot = 1 + STATS.POP().POP.data(null).get(null);
		for (Race r : RACES.all()) {
			double d = roccurence[r.index()]*STATS.POP().POP.data().get(r)/tot;
			if (d > raM)
				raM = d;
		}
		occ *= coccurence[SETT.ENV().climate().index()]*raM;
		return occ;
	}

	
}
