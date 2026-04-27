package game.faction.royalty;

import java.io.IOException;

import game.boosting.BOOSTABLES;
import game.boosting.superb.SuperBoostableObj;
import game.boosting.superb.SuperData;
import game.faction.royalty.opinion.ROPINIONS;
import game.time.TIME;
import init.race.Race;
import init.type.HTYPES;
import init.type.TRAIT;
import init.type.TRAITS;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sprite.text.Str;
import util.text.D;

public class Royalty implements SuperBoostableObj{

	public final Induvidual induvidual;
	public final NPCCourt court;
	final int deathDay;
	private boolean event;
	public short eventMark;
	
	private final SuperData bdata = ROPINIONS.BOOST().makeData();
	
	private static CharSequence ¤¤Ruler = "¤Ruler of {0}";
	private static CharSequence ¤¤Heir = "¤First Heir of {0}";
	private static CharSequence ¤¤Heir2 = "¤Second Heir of {0}";
	private static CharSequence ¤¤Heir3 = "¤Third Heir of {0}";
	static {
		D.ts(Royalty.class);
	}
	private final CharSequence[] sss = new CharSequence[] {
		¤¤Ruler,
		¤¤Heir,
		¤¤Heir2,
		¤¤Heir3
	};
	public final ArrayListGrower<TRAIT> traits = new ArrayListGrower<>();
	
	Royalty(NPCCourt court, Race race){
		induvidual = new Induvidual(HTYPES.NOBILITY(), race);
		this.court = court;
		//CharSequence first = race.appearance().types.get(STATS.APPEARANCE().gender.get(induvidual)).names.firstNames.rnd();
		//CharSequence last = race.appearance().lastNamesNoble.rnd();
		//name.clear().add(last);

//		listID = index;
//		this.index = court.faction.index()*FACTIONS.MAX+index;
		
		int ls = STATS.POP().age.lifespan(induvidual);
		int min = ls/4;
		int dd = ls-2*min;
		int days = (min + RND.rInt(dd));
		STATS.POP().age.DAYS.set(induvidual, days);
		if (false) {
			//something is wrong here!
		}
		deathDay = (int) (TIME.days().bitsSinceStart() + days + 1 + (ls-days)*RND.rFloat());
		setTitles();
	}

	
	private void setTitles() {
		traits.clear();
		for (TRAIT t : TRAITS.tmp(induvidual, 3)) {
			if (traits.size() == 0 || Math.abs(t.get(induvidual)-0.5) > 0.2)
				traits.add(t);
		}
	}
	
	int lifespan() {
		return (int) (induvidual.race().bvalue(BOOSTABLES.PHYSICS().DEATH_AGE)*TIME.years().bitConversion(TIME.days()));
	}

	
	Royalty(NPCCourt court, FileGetter file) throws IOException {
		this.court = court;
		induvidual = new Induvidual(file);
		deathDay = file.i();
		bdata.load(file);
		event = file.bool();
		eventMark = file.s();
		setTitles();
	}
	
	public void update(double seconds) {
		ROPINIONS.BOOST().update(this, seconds);
	}
	
	void save(FilePutter file) {
		induvidual.save(file);
		file.i(deathDay);
		bdata.save(file);
		file.bool(event);
		file.s(eventMark);
	}
	
	public void kill(boolean sendMessage) {
		court.kill(this);
	}
	
	public boolean isKing() {
		return court.king().roy() == this;
	}

	public CharSequence name() {
		if (isKing())
			return court.king().name;
		return STATS.APPEARANCE().nameLast(induvidual);
	}
	
	public Str nameFull(Str s) {
		if (isKing()) {
			s.add(court.king().name);
			return court.king().name;
		}else
			s.add(STATS.APPEARANCE().nameFirst(induvidual)).s().add(STATS.APPEARANCE().nameLast(induvidual));
		return s;
	}
	
	public Str nameSucc(Str s) {
		s.add(sss[successionI()]).insert(0, court.faction.name);
		return s;
	}
	
	public int successionI() {
		return court.all.indexOf(this);
	}



	
	public boolean event() {
		return event;
	}
	
	public void eventSet(boolean b) {
		event = b;
	}


	@Override
	public SuperData boostingData() {
		return bdata;
	}
	
}
