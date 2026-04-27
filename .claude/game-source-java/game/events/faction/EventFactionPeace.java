package game.events.faction;

import java.io.IOException;

import game.events.EVENTS.EventResource;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.faction.diplomacy.DIP;
import game.faction.diplomacy.DipStance;
import game.faction.npc.FactionNPC;
import game.time.TIME;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import util.updating.IUpdater;

public class EventFactionPeace extends EventResource{

	private double[] secondWhenWarEnds = new double[FACTIONS.MAX()];

	
	EventFactionPeace(){
		super("FACTION_PEACE");
		new DIP.DipActivityListener() {
			


			@Override
			public void change(Faction a, Faction b, DipStance old, DipStance nn) {
				if (nn == DIP.WAR()) {
					secondWhenWarEnds[b.index()] = peaceTime();
					secondWhenWarEnds[a.index()] = peaceTime();	
				}
				
			}

			
			
		};
	}
	
	private double peaceTime() {
		return TIME.playedGame() + TIME.secondsPerDay() + RND.rFloat()*TIME.secondsPerDay()*32.0;
	}
	
	private final IUpdater updater = new IUpdater(FACTIONS.MAX(), TIME.secondsPerDay()/2) {
		
		@Override
		protected void update(int i, double timeSinceLast) {
			Faction f = FACTIONS.getByIndex(i);
			if (f.isActive() && f instanceof FactionNPC) {
				up((FactionNPC) f);
			}
		}
	};
	
	@Override
	protected void update(double ds) {
		updater.update(ds);

	}
	
	private void up(FactionNPC f) {
		
		if (DIP.WAR().all(f).size() == 0)
			return;
		
		if (DIP.WAR().is(f)) {
			return;
		}
		
		if (DIP.ALLY().is(f))
			return;
		
		if (TIME.playedGame() > secondWhenWarEnds[f.index()]) {
			secondWhenWarEnds[f.index()] = peaceTime()/2.0;
			Faction e = DIP.WAR().all(f).rnd();
			if (e == null)
				return;
			
			if (e == FACTIONS.player())
				return;
			
			if (DIP.WAR().is((FactionNPC) e) && DIP.ALLY().is(f)) {
				return;
			}
			DIP.NEUTRAL().set(f,e);
		}else {
			
		}
		
		
		
	}

	@Override
	protected void save(FilePutter file) {
		updater.save(file);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		updater.load(file);
	}

	@Override
	protected void clear() {
		updater.clear();
	}



}
