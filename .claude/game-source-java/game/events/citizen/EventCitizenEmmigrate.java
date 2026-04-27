package game.events.citizen;

import java.io.IOException;
import java.util.Arrays;

import init.race.RACES;
import init.race.Race;
import init.type.HCLASSES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
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

final class EventCitizenEmmigrate implements EventCitizen.SMALL_EVENT{

	private static CharSequence ¤¤emigration = "¤Emigrants!";
	private static CharSequence ¤¤emigrationD = "¤A group of {RACE} have decided to leave your city, renouncing their citizenship, and your rule. This is a sign of weakness. Make sure you increase loyalty so that this will not happen again!";
	private static StrInserter<Race> iRace = new StrInserter<Race>("RACE") {
		@Override
		public void set(Race t, Str str) {
			str.add(t.info.names);
		}
	};
	static {
		D.ts(EventCitizenEmmigrate.class);
	}
	
	private final int[] emmigrations = new int[RACES.all().size()];

	public EventCitizenEmmigrate() {
		IDebugPanelSett.add("Event: Emmigration", new ACTION() {
			
			@Override
			public void exe() {
				int ri = RND.rInt(RACES.all().size());
				for (int i = 0; i < RACES.all().size(); i++) {
					Race r = RACES.all().getC(ri+i);
					int am = (int) Math.ceil(STATS.POP().POP.data(HCLASSES.CITIZEN()).get(r)*RND.rFloat());
					if (am > 0) {
						event(am, r);
						return;
					}
				}
			}
		});
	}
	
	@Override
	public void save(FilePutter file) {
		file.isE(emmigrations);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		file.isE(emmigrations);
	}

	@Override
	public void clear() {
		Arrays.fill(emmigrations, 0);
	}
	
	public boolean shouldEmigrate(Race r) {
		if (STATS.POP().POP.data(HCLASSES.CITIZEN()).get(r) == 0) {
			emmigrations[r.index()] = 0;
			return false;
		}
		return emmigrations[r.index()] > 0;
	}
	
	public void emigrate(Humanoid h) {
		emmigrations[h.race().index()]--;
		if (emmigrations[h.race().index] < 0) {
			emmigrations[h.race().index] = 0;
		}
	}

	@Override
	public boolean event(int amH, Race hr) {
		if (SETT.ENTRY().isClosed())
			return false;
		
		emmigrations[hr.index] = amH;
		
		Str t = Str.TMP;
		t.clear();
		t.add(¤¤emigrationD);
		iRace.insert(hr, t);
		new MessageText(¤¤emigration, t).send();
		return true;
	}

	@Override
	public void update(double ds) {
		// TODO Auto-generated method stub
		
	}


	
	
}
