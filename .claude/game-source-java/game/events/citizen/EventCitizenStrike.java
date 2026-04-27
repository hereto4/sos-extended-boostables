package game.events.citizen;

import java.io.IOException;

import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import init.type.WGROUP;
import init.type.HTYPES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.employment.RoomEmployment;
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

final class EventCitizenStrike implements EventCitizen.SMALL_EVENT{

	private static CharSequence ¤¤strike = "¤Worker Strike!";
	private static CharSequence ¤¤strikeD = "¤Your {RACE} workers have halted all work in our {WORKPLACES}, in protest of what they call your bad judgement. Take measures to increase their loyalty so that it doesn't happen again.";
	private static CharSequence ¤¤strikeOver = "¤Strike Over";
	private static CharSequence ¤¤strikeOverD = "¤Your {WORKPLACES} have resumed work and the strike is over.";
	
	private RoomEmployment strike = null;
	private double strikeTimer = 0;
	private StrInserter<Race> iRace = new StrInserter<Race>("RACE") {
		@Override
		public void set(Race t, Str str) {
			str.add(t.info.namePosessive);
		}
	};
	
	private StrInserter<RoomEmployment> iWork = new StrInserter<RoomEmployment>("WORKPLACES") {
		@Override
		public void set(RoomEmployment t, Str str) {
			str.add(t.blueprint().info.names);
		}
	};
	
	static {
		D.ts(EventCitizenStrike.class);
	}
	
	public EventCitizenStrike() {
		IDebugPanelSett.add("Event: Strike", new ACTION() {
			
			@Override
			public void exe() {
				int ri = RND.rInt(RACES.all().size());
				for (int i = 0; i < RACES.all().size(); i++) {
					Race r = RACES.all().getC(ri+i);
					if (event(0, r))
						return;
				}
			}
		});
	}
	
	@Override
	public void save(FilePutter file) {
		file.i(strike == null ? -1 : strike.index());
		file.d(strikeTimer);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		int i = file.i();
		if (i == -1)
			strike = null;
		else
			strike = SETT.ROOMS().employment.ALL().get(i);
		strikeTimer = file.d();
		
	}

	@Override
	public void clear() {
		strikeTimer = 0;
	}
	
	@Override
	public boolean event(int am, Race hr) {
		
		RoomEmployment strike = null;
		int most = 0;
		for (RoomEmployment e : SETT.ROOMS().employment.ALL()) {
			if (e.employed(WGROUP.get(HTYPES.SUBJECT(), hr)) > most) {
				strike = e;
				most = e.employed(WGROUP.get(HTYPES.SUBJECT(), hr));
			}
		}
		
		if (strike == null)
			return false;
		
		this.strike = strike;
		this.strikeTimer = TIME.secondsPerDay()*1.5;
		
		Str s = Str.TMP.clear();
		s.add(¤¤strikeD);
		iRace.insert(hr, s);
		iWork.insert(strike, s);
		new MessageText(¤¤strike, s).send();
		
		return true;
		
	}
	
	@Override
	public void update(double ds) {
		if (strikeTimer <= 0)
			return;
		
		strikeTimer-= ds;
		if (strikeTimer <= 0) {
			Str s = Str.TMP.clear();
			s.add(¤¤strikeOverD);
			iWork.insert(strike, s);
			new MessageText(¤¤strikeOver, s).send();
		}
		
	}
	
	public boolean isStriking(Humanoid h) {
		return h.indu().hType().player && strikeTimer > 0 && STATS.WORK().EMPLOYED.get(h) != null && STATS.WORK().EMPLOYED.get(h).blueprintI().employment() == strike;
	}


	
	
}
