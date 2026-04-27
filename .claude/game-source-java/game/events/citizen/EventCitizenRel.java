package game.events.citizen;

import java.io.IOException;

import game.faction.FACTIONS;
import game.time.TIME;
import init.race.Race;
import init.religion.Religion;
import init.religion.RELIGIONS;
import init.type.HCLASSES;
import settlement.entity.humanoid.Humanoid;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.RND;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sprite.text.Str;
import snake2d.util.sprite.text.StrInserter;
import util.text.D;
import view.sett.IDebugPanelSett;
import view.ui.message.MessageText;

final class EventCitizenRel implements EventCitizen.SMALL_EVENT{

	private static CharSequence ¤¤title = "War of the faiths";
	private static CharSequence ¤¤descRel = "Due to low happiness, a local dispute between two citizens of opposing faiths has spread across the whole city. Followers of {RELIGION_A} and {RELIGION_B} are now at each others throats and fighting each other wherever they meet. We must fix our happiness issues before this spreads any further.";
	private static CharSequence ¤¤descRel2 = "The tension between religious factions have spread. Followers of {RELIGION_A} and {RELIGION_B} are now also at odds, and fighting each other wherever they meet.";
	
	private final StrInserter<Religion> irA = new StrInserter<Religion>("RELIGION_A") {

		@Override
		protected void set(Religion t, Str str) {
			str.add(t.diety);
		}
		
	};
	
	private final StrInserter<Religion> irB = new StrInserter<Religion>("RELIGION_B") {

		@Override
		protected void set(Religion t, Str str) {
			str.add(t.diety);
		}
		
	};
	
	private double timer;
	private final Bitmap1D map = new Bitmap1D(RELIGIONS.ALL().size(), false);
	
	static {
		D.ts(EventCitizenRel.class);
		
	}
	
	EventCitizenRel() {
		IDebugPanelSett.add("Event: race war", new ACTION() {
			
			@Override
			public void exe() {
				event(0, FACTIONS.player().race());
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
		if (timer <= 0)
			map.clear();
		return spawnRel(race);
	}
	
	private boolean spawnRel(Race race) {
		
		Religion a = rel(race);
		if (a == null)
			return false;
		Religion b = ranRel(a);
		if (b == null)
			return false;
		
		
		map.set(a.index(), true);
		map.set(b.index(), true);
		Str s = new Str(timer > 0 ? ¤¤descRel2 : ¤¤descRel);
		irA.insert(a, s);
		irB.insert(b, s);
		timer = TIME.secondsPerDay()*(1+RND.rFloat(2));
		new MessageText(¤¤title, s).send();
		return true;
	}
	
	private Religion rel(Race race) {
		double max = 0;
		for (int ri = 0; ri < RELIGIONS.ALL().size(); ri++) {
			Religion r = RELIGIONS.ALL().get(ri);
			if (STATS.RELIGION().ALL.get(r.index()).followers.data(HCLASSES.CITIZEN()).get(race) > 0) {
				max += STATS.RELIGION().ALL.get(r.index()).followers.data(HCLASSES.CITIZEN()).get(race);
			}
		}
		
		if (max == 0)
			return null;
		
		max *= RND.rFloat();
		
		for (int ri = 0; ri < RELIGIONS.ALL().size(); ri++) {
			Religion r = RELIGIONS.ALL().get(ri);
			if (STATS.RELIGION().ALL.get(r.index()).followers.data(HCLASSES.CITIZEN()).get(race) > 0) {
				max -= STATS.RELIGION().ALL.get(r.index()).followers.data(HCLASSES.CITIZEN()).get(race); 
				if (max <= 0) {
					return r;
				}
			}
		}
		return null;
	}
	
	private Religion ranRel(Religion other) {
		double max = 0;
		for (int ri = 0; ri < RELIGIONS.ALL().size(); ri++) {
			Religion r = RELIGIONS.ALL().get(ri);
			if (r != other && STATS.RELIGION().ALL.get(r.index()).followers.data(HCLASSES.CITIZEN()).get(null) > 0) {
				max += STATS.RELIGION().ALL.get(r.index()).followers.data(HCLASSES.CITIZEN()).get(null);
			}
		}
		
		if (max == 0)
			return null;
		
		max *= RND.rFloat();
		
		for (int ri = 0; ri < RELIGIONS.ALL().size(); ri++) {
			Religion r = RELIGIONS.ALL().get(ri);
			if (r != other && STATS.RELIGION().ALL.get(r.index()).followers.data(HCLASSES.CITIZEN()).get(null) > 0) {
				max -= STATS.RELIGION().ALL.get(r.index()).followers.data(HCLASSES.CITIZEN()).get(null); 
				if (max <= 0) {
					return r;
				}
			}
		}
		return null;
	}
	
	public boolean isAtOdds(Humanoid a, Humanoid b) {
		if (timer <= 0)
			return false;
		Religion ra = STATS.RELIGION().getter.get(a.indu()).religion;
		Religion rb = STATS.RELIGION().getter.get(b.indu()).religion;
		return ra != rb && map.get(ra.index()) && map.get(rb.index());
	}
	


	@Override
	public void save(FilePutter file) {
		map.save(file);
		file.d(timer);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		map.load(file);
		timer = file.d();
	}

	@Override
	public void clear() {
		timer = -1;
	}

}
