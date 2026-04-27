package game.events.faction;

import java.io.IOException;

import game.events.EVENTS.EventResource;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import world.WORLD;
import world.map.pathing.WRegFinder.RegDist;
import world.map.pathing.WRegFinder.Treaty;
import world.map.pathing.WRegSel;
import world.region.RD;
import world.region.pop.RDRace;

public class EventFactionWar extends EventResource{

	
	private static final double dtime = TIME.secondsPerDay()*16;
	private double timer = dtime;
	private int nextFaction;
	
	EventFactionWar(){
		super("FACTION_WAR");
	}
	
	@Override
	protected void update(double ds) {
		
		timer -= ds*CLAMP.d(FACTIONS.player().realm().regions()/8.0, 0, 1);
		if (timer > 0)
			return;
		
		FactionNPC f = FACTIONS.NPCs().getC(nextFaction);
		
		if (f == null)
			return;
		
		
		clear();
		timer = 16;
		
		if (DIP.get(f).ally)
			return;
		if (f.sanctified)
			return;
		
		if (f != null && f.isActive() && f.capitolRegion() != null && DIP.WAR().all(f).size() == 0) {
			
			Faction enemy = null; 
			double bestE = 0;
			
			for (RegDist d : WORLD.PATH().regFinder.all(f, Treaty.FACTION_BORDERS, WRegSel.CAPITOLS())) {
				if (d.reg.faction() == null)
					continue;
				if (d.reg.faction() == f || d.reg.faction() == FACTIONS.player())
					continue;
				if (FACTIONS.player().realm().regions() < 3 && RD.DIST().reachable(d.reg))
					continue;
				FactionNPC ff = (FactionNPC) d.reg.faction();
				if (DIP.get(ff).ally)
					continue;
				if (ff.sanctified)
					continue;
				
				double v = 0;
				
				for (RDRace race : RD.RACES().all) {
					v += race.pop.get(d.reg)*race.race.pref().race(f.race());	
				}
				v = 1.0/v;
				if (v > bestE) {
					enemy = d.reg.faction();
					bestE = v;
				}
			}
			
			if (enemy != null) {
				DIP.WAR().set(f, enemy);
				timer += TIME.secondsPerDay()*20;
			}else {
				
			}
				
		}
		
	}


	@Override
	protected void save(FilePutter file) {
		file.d(timer);
		file.i(nextFaction);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		timer = file.d();
		nextFaction = file.i();
		timer = 16;
	}

	@Override
	protected void clear() {
		timer = dtime;
		nextFaction = RND.rInt();
	}	

}
