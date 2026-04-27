package game.faction.npc;

import java.io.IOException;
import java.util.Arrays;

import game.boosting.BOOSTABLES;
import game.faction.FCredits.CTYPE;
import game.faction.FSlaves;
import game.faction.Faction;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import world.region.RD;
import world.region.pop.RDRace;

public final class NPCSlaves extends FSlaves{

	private double[] available = new double[RACES.all().size()];
	private final FactionNPC f; 
	private int ri = 0;
	private double timer;
	
	NPCSlaves(FactionNPC f) {
		this.f = f;
	}
	
	@Override
	public int available(Race race) {
		return (int) available[race.index];
	}

	@Override
	public void trade(Race race, int am, int credits) {
		available[race.index]+= am;
		f.credits().inc(credits, CTYPE.SLAVES);
	}

	@Override
	public int price(Race race, int am) {

		double bo = BOOSTABLES.NOBLE().MERCY.get(f.court().king().roy().induvidual);
		bo = CLAMP.d(bo/2.0, 0, 0.5);
		double price = am*BASE_PRICE(race);
		if (am > 0) {
			return (int) (price*(0.5+bo));
		}
		return (int) -((price*(1+bo)));

	}

	@Override
	protected void save(FilePutter file) {
		RACES.map().saver().save(available, file);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		RACES.map().loader().load(available, file, 0);
		ri = RND.rInt(RACES.all().size());
	}

	@Override
	protected void clear() {
		Arrays.fill(available, 0);
	}

	@Override
	protected void update(double ds, Faction f) {
		
		timer += ds*RACES.all().size();
		
		while(timer > TIME.secondsPerDay()) {
			timer -= TIME.secondsPerDay();
			ri %= RACES.all().size();
			Race r = RACES.all().get(ri);
			
			int target = target(r);
			double av = available[ri];
			double inc = target/(r.physics.adultAt+5.0);
			
			if (av < target) {
				
				av += 0.1*inc*r.bvalue(BOOSTABLES.BEHAVIOUR().SUBMISSION);
				av = CLAMP.d(av, 0, target);
				available[ri] = av;
			}else {
				av -= 0.25*inc;
				av = CLAMP.d(av, target, av);
				available[ri] = av;
			}
			
			ri++;
		}
	}
	
	public void init() {
		for (Race r : RACES.all()) {
			available[r.index] = target(r);
		}
	}
	
	public int target(Race race) {
		RDRace r = RD.RACE(race);
		if (r == null)
			return 0;
		double ii = 1.0-0.5*BOOSTABLES.NOBLE().MERCY.get(f.court().king().roy().induvidual);
		ii = Math.max(ii, 0);
		
		return (int) (r.pop.faction().get(f)*0.15*ii);
	}

}
