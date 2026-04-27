package game.events.killer;

import java.io.IOException;

import game.events.EVENTS;
import game.time.TIME;
import init.paths.PATH;
import init.paths.PATHS;
import init.race.RACES;
import init.race.Race;
import init.type.CAUSE_LEAVES;
import init.type.HCLASSES;
import init.type.HTYPES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.types.prisoner.AIModule_Prisoner;
import settlement.main.SETT;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import settlement.stats.law.PRISONER_TYPE;
import settlement.thing.ThingsCorpses.Corpse;
import snake2d.LOG;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.Json;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import util.data.DOUBLE.DoubleImp;
import view.sett.IDebugPanelSett;

public final class EventKiller extends EVENTS.EventResource{

	private final Messenger messages = new Messenger();
	private final Messenger.Data mData = new Messenger.Data();
	private static final double interval = TIME.secondsPerDay()*16*24;
	private final LIST<KillerType> killerTypes;
	private int[] typeShuffle;
	private DoubleImp timer = new DoubleImp();
	
	private int type = 0;
	private int day;
	private boolean dormant;
	private int killerID = -1;
	private int suspect = -1;
	private int victimRace = -1;
	private int victims;
	private double rate;
	private static double rateSpeed = 1.0/(TIME.secondsPerDay()*8);

	public EventKiller(){
		super("KILLER");
		LinkedList<KillerType> ks = new LinkedList<>();
		PATH pp = PATHS.TEXT_MISC().getFolder("serialKiller");
		for (String k : pp.getFiles()) {
			ks.add(new KillerType(new Json(pp.get(k))));
		}
		this.killerTypes = new ArrayList<KillerType>(ks);
		this.typeShuffle = new int[killerTypes.size()];
		shuffle();
		
		clear();
		
		IDebugPanelSett.add("Event Serial killer", new ACTION() {
			
			@Override
			public void exe() {
				init();
				if (theKiller() == null) {
					LOG.ln("nay");
				}
			}
		});
	}
	
	private void shuffle() {
		for (int i = 0; i < typeShuffle.length; i++) {
			typeShuffle[i] = i;
		}
		for (int i = 0; i < typeShuffle.length; i++) {
			int i2 = RND.rInt(typeShuffle.length);
			int v = typeShuffle[i];
			typeShuffle[i] = typeShuffle[i2];
			typeShuffle[i2] = v;
		}
	}
	
	@Override
	protected void save(FilePutter file) {
		timer.save(file);
		file.i(type);
		file.i(day);
		file.bool(dormant);
		file.i(killerID);
		file.i(suspect);
		file.i(victimRace);
		file.i(victims);
		file.d(rate);
		file.isE(typeShuffle);
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		timer.load(file);
		type = file.i();
		day = file.i();
		dormant = file.bool();
		killerID = file.i();
		suspect = file.i();
		victimRace = file.i();
		victims = file.i();
		rate = file.d();
		if (!file.isE(typeShuffle))
			shuffle();
		type = CLAMP.i(type, 0, killerTypes.size());
	}

	@Override
	protected void clear() {
		reset();
		rate = 0;
	}
	
	private void reset() {
		dormant = true;
		suspect = -1;
		victims = 0;
		timer.setD(interval);
		timer.incD(interval*0.5*RND.rFloat());
	}
	
	private void init() {
		killerID = Util.pickKiller();
		victimRace = Util.pickRace();
		victims = 0;
		rate = 0;
		suspect = -1;

		type ++;
		type %= typeShuffle.length;
		timer.setD(-1);
		if (theKiller() == null) {
			reset();
		}
	}
	
	@Override
	protected void update(double ds) {
		
		if (timer.getD() > 0) {
			if (ds > 0)
				timer.incD(-ds);
			if (timer.getD() < 0) {
				if (SETT.ROOMS().PRISON.instancesSize() > 0) {
					init();
				}else
					reset();
				
			}
			
			if (rate > 0) {
				rate -= ds*rateSpeed;
				rate = CLAMP.d(rate, 0, 1.0);
				if (rate <= 0) {
					setData(null);
					messages.over(mData);
				}
			}
			return;
		}
		
		if (suspect != -1) {
			if (day != TIME.days().bitsSinceStart()) {
				ENTITY e = SETT.ENTITIES().getByID(suspect);
				if (e != null && e instanceof Humanoid) {
					if (theKiller() == e) {
						setData(null);
						STATS.LAW().prisonerType.set(mData.suspect.indu(), PRISONER_TYPE.MURDER);
						mData.suspect.HTypeSet(HTYPES.PRISONER(), CAUSE_LEAVES.PUNISHED(), null);
						messages.caught(mData);
						
						AIModule_Prisoner.DATA().punishmentSet.set(((Humanoid)e).ai(), LAW.process().execution);
						clear();
					}else {
						rate += 0.2;
						rate = CLAMP.d(rate, 0, 1);
						setData(null);
						messages.fail(mData);
						suspect = -1;
					}
				}
			}
		}
		
		if (theKiller() == null) {
			reset();
			return;
		}else if(theKiller().indu().hType() == HTYPES.PRISONER()) {
			setData(null);
			messages.caught(mData);
			AIModule_Prisoner.DATA().punishmentSet.set(theKiller().ai(), LAW.process().execution);
			clear();
		}
		
	
		
		if (day != TIME.days().bitsSinceStart()) {
			dormant = !RND.oneIn(3);
			day = TIME.days().bitsSinceStart();
		}
		
		rate = (double)victims / type().messages.length;
		
		
	}
	
	public KillerType type() {
		
		return killerTypes.get(typeShuffle[type]);
	}
	
	
	public Humanoid theKiller() {
		if (killerID == -1)
			return null;
		ENTITY e = SETT.ENTITIES().getByID(killerID);
		if (e instanceof Humanoid) {
			Humanoid a = (Humanoid) e;
			if (a.indu().clas() == HCLASSES.CITIZEN() || a.indu().hType() == HTYPES.PRISONER())
				return a;
		}
		return null;
	}
	
	public boolean theKillerShouldKill() {
		return !dormant;
	}
	
	public Race victimRace() {
		if (victimRace == -1)
			return null;
		return RACES.all().get(victimRace);
	}
	
	public int murders() {
		return victims;
	}
	
	public void setSuspect(int suspect) {
		this.suspect = suspect;
		day = TIME.days().bitsSinceStart();
	}
	
	int suspect() {
		return suspect;
	}
	
	public void reportKill(Corpse corpse) {
		victims ++;
		dormant = true;
		if (victims > type().messages.length) {
			reset();
			return;
		}
		
		if (victims == type().messages.length/2) {
			int suspect = killerID;
			if (RND.rBoolean())
				suspect = Util.pickKiller();
			if (suspect != -1) {
				
				setData(corpse);
				mData.suspect = (Humanoid) SETT.ENTITIES().getByID(suspect);;
				messages.murderSuspect(mData);
				return;
			}
		}
		
		setData(corpse);
		messages.murder(mData);
		
		rate = (double)(victims-1) / (type().messages.length-1);
		if (victims >= type().messages.length) {
			reset();
		}
	}
	
	void setData(Corpse corpse) {
		mData.killer = theKiller();
		mData.type = type();
		mData.murders = victims;
		mData.race = victimRace();
		mData.suspect = mData.killer;
		mData.victim = corpse;
	}

	public double rate() {
		return rate;
	}



	
	
}
