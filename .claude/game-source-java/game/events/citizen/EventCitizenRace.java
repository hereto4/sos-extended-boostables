package game.events.citizen;

import java.io.IOException;

import game.faction.FACTIONS;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import init.type.HCLASSES;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StrInserter;
import util.text.D;
import view.sett.IDebugPanelSett;
import view.ui.message.MessageText;

final class EventCitizenRace implements EventCitizen.SMALL_EVENT{

	private static CharSequence ¤¤title = "Brawls!";
	private static CharSequence ¤¤desc = "A local dispute between a {RACE_A} and {RACE_B} citizen has spread across the whole city. The two species are now at each others throats and fighting each other wherever they meet. We must fix our happiness issues before this spreads any further.";

	private final StrInserter<Race> iA = new StrInserter<Race>("RACE_A") {

		@Override
		protected void set(Race t, Str str) {
			str.add(t.info.namePosessive);
		}
		
	};
	
	private final StrInserter<Race> iB = new StrInserter<Race>("RACE_B") {

		@Override
		protected void set(Race t, Str str) {
			str.add(t.info.namePosessive);
		}
		
	};
	

	
	private int ra;
	private int rb;
	private double timer;
	
	static {
		D.ts(EventCitizenRace.class);
		
	}
	
	EventCitizenRace() {
		IDebugPanelSett.add("Event: race war", new ACTION() {
			
			@Override
			public void exe() {
				event(100, FACTIONS.player().race());
			}
		});
		clear();
	}


	@Override
	public void update(double ds) {
		timer -= ds;
	}
	
	@Override
	public boolean event(int am, Race race) {
		
		ra = -1;
		rb = -1;

		return spawnRace(race);
		
	}
	
	private boolean spawnRace(Race race) {
		double max = 0;
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			Race r = RACES.all().get(ri);
			if (STATS.POP().POP.data(HCLASSES.CITIZEN()).get(r) > 0 && race != r && race.pref().race(r) < 1.0) {
				max += STATS.POP().POP.data(HCLASSES.CITIZEN()).get(r)*(1 - race.pref().race(r)); 
			}
		}
		if (max == 0)
			return false;
		
		max *= RND.rFloat();
		
		for (int ri = 0; ri < RACES.all().size(); ri++) {
			Race r = RACES.all().get(ri);
			if (STATS.POP().POP.data(HCLASSES.CITIZEN()).get(r) > 0 && race != r && race.pref().race(r) < 1) {
				max -= STATS.POP().POP.data(HCLASSES.CITIZEN()).get(r)*(1 - race.pref().race(r)); 
				if (max <= 0) {
					timer = TIME.secondsPerDay()*(1+RND.rFloat(2));
					ra = race.index;
					rb = r.index;
					Str s = new Str(¤¤desc);
					iA.insert(race, s);
					iB.insert(r, s);
					new MessageText(¤¤title, s).send();
				}
			}
		}
		return true;
	}
	

	
	public boolean isAtOdds(Humanoid a, Humanoid b) {
		if (timer <= 0)
			return false;
		if (ra != -1) {
			return (a.race().index == ra && b.race().index == rb) || (a.race().index == rb && b.race().index == ra);
		}
		return false;
	}
	


	@Override
	public void save(FilePutter file) {
		file.i(ra);
		file.i(rb);
		file.d(timer);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		ra = file.i();
		rb = file.i();
		timer = file.d();
	}

	@Override
	public void clear() {
		timer = -1;
	}

}
