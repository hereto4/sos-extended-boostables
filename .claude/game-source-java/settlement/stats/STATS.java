package settlement.stats;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import game.GAME;
import game.debug.Profiler;
import init.paths.PATH;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.value.GVALUES;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT.SettResource;
import settlement.stats.StatsInit.Addable;
import settlement.stats.StatsInit.StatDisposable;
import settlement.stats.StatsInit.StatInitable;
import settlement.stats.StatsInit.StatUpdatable;
import settlement.stats.StatsInit.StatUpdatableI;
import settlement.stats.colls.StatsAccess;
import settlement.stats.colls.StatsAppearance;
import settlement.stats.colls.StatsBattle;
import settlement.stats.colls.StatsBurial;
import settlement.stats.colls.StatsEducation;
import settlement.stats.colls.StatsEnv;
import settlement.stats.colls.StatsFood;
import settlement.stats.colls.StatsGovern;
import settlement.stats.colls.StatsHome;
import settlement.stats.colls.StatsNeeds;
import settlement.stats.colls.StatsPopulation;
import settlement.stats.colls.StatsReligion;
import settlement.stats.colls.StatsStored;
import settlement.stats.colls.StatsTraits;
import settlement.stats.colls.StatsWork;
import settlement.stats.disease.StatsDisease;
import settlement.stats.equip.StatsEquip;
import settlement.stats.event.StatsEvent;
import settlement.stats.law.StatsLaw;
import settlement.stats.muls.StatsMultipliers;
import settlement.stats.muls.StatsMultipliers.StatMultiplier;
import settlement.stats.service.StatsService;
import settlement.stats.standing.StatStanding;
import settlement.stats.standing.StatStanding.StandingDef;
import settlement.stats.stat.STAT;
import settlement.stats.stat.StatCollection;
import settlement.stats.util.StatBooster;
import settlement.stats.util.StatsJson;
import snake2d.LOG;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.Json;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.ACTION.ACTION_O;
import snake2d.util.misc.CLAMP;
import snake2d.util.misc.Dictionary;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.data.DataO;
import util.data.DataRandom;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.info.INFO;
import util.text.D;
import util.updating.IUpdater;

public final class STATS extends SettResource {

	public static final int DAYS_SAVED = 32;

	private static STATS s;

	private final StatsBattle battle;
	private final StatsEnv environment;
	private final StatsAccess access;
	private final StatsPopulation population;
	private final StatsGovern govern;
	private final StatsLaw law;
	private final StatsWork work;
	private final StatsHome home;
	private final StatsService services;
	private final StatsNeeds needs;
	private final StatsFood food;
	private final StatsTraits traits;
	private final StatsDisease disease;
	private final StatsEvent event;
	private final StatsEquip equipables;
	private final StatsEducation education;
	private final StatsStored stored;
	private final StatsBurial burial;
	private final StatsReligion religion;
	private final StatsMultipliers multipliers;
	private final StatsAppearance appearance;
	private final DataRandom<Induvidual> random;
	private final DataO<Induvidual> count;
	private final KeyMap<SAVABLE> savables;
	
	private final LIST<Addable> addables;
	private final LIST<StatUpdatableI> updaters;
	private final LIST<StatUpdatable> uppers;

	private final LIST<StatInitable> constructers;
	private final LIST<StatDisposable> disposables;
	private final LIST<STAT> stats;
	private final LIST<StatCollection> collections;
	private final KeyMap<StatCollection> mapColl;
	private final KeyMap<STAT> mapStat;

	private final IUpdater upper;

//	final ArrayList<StatGlobal> statistics;
//	final ArrayList<StatStat> stats;

	private final short[] iOff = new short[256];

	public final INFO iStats;
	
	
	private final StatArrival arrival;
	final StatCopy copy;

	public static void create() throws IOException {
		new STATS();
	}

	private STATS() throws IOException {
		super("STATS", true);
		StatsInit init = new StatsInit();

		s = this;

		D.gInit(this);
		iStats = new INFO(D.g("Status"),
				D.g("desc", "Miscellaneous statistics about your city. Some affecting your subject's happiness."));

		random = new DataRandom<Induvidual>(init.count, 4);
		init.onConstruct.add(new StatInitable() {

			@Override
			public void init(Induvidual h) {
				random.randomize(h);
			}
		});
		
		CharSequence nn = D.g("random", "Random Chance");

		for (int i = 0; i < 8; i++) {
			final int k = 8 * i;
			
			INT_O<Induvidual> o = new INT_O<Induvidual>() {

				@Override
				public int get(Induvidual t) {
					return random.get(t, 64 + k) & 0x0FF;
				}

				@Override
				public int min(Induvidual t) {
					return 0;
				}

				@Override
				public int max(Induvidual t) {
					return 0x0FF;
				}

			};
			GVALUES.INDU.push("RANDOM_" + i + "_F", nn, UI.icons().s.question, o);
			GVALUES.INDU.pushI("RANDOM_" + i + "_I", nn, UI.icons().s.question, o);
		}

		population = new StatsPopulation(init);
		law = new StatsLaw(init);
		govern = new StatsGovern(init);
		equipables = new StatsEquip(init);
		work = new StatsWork(init);
		home = new StatsHome(init);
		food = new StatsFood(init);
		services = new StatsService(init);
		environment = new StatsEnv(init);
		access = new StatsAccess(init);
		
		battle = new StatsBattle(init);
		needs = new StatsNeeds(init);
		education = new StatsEducation(init);
		traits = new StatsTraits(init);
		disease = new StatsDisease(init);
		event = new StatsEvent(init);
		stored = new StatsStored(init);
		burial = new StatsBurial(init);
		religion = new StatsReligion(init);
		multipliers = new StatsMultipliers(init, services);

		appearance = new StatsAppearance(init);
		
		addables = new ArrayList<>(init.addable);

		savables = init.savers;
		updaters = new ArrayList<>(init.updatable);
		disposables = new ArrayList<>(init.disposable);
		stats = new ArrayList<STAT>(init.stats);
		collections = new ArrayList<>(init.holders);
		constructers = new ArrayList<>(init.onConstruct);
		uppers = new ArrayList<>(init.upers);
		{
			Arrays.fill(iOff, (short) -1);
			int v = 255;
			int div = 2;

			while (v >= 0) {
				int i = 256 / div;
				for (int k = 1; k < div; k++) {
					if (iOff[k * i] == -1) {
						iOff[k * i] = (short) v;
						v--;
					}
				}
				div++;
			}
		}

		count = init.count;

		mapColl = init.collMap;
		mapStat = init.statMap;

		upper = new IUpdater(uppers.size(), 8) {

			@Override
			protected void update(int i, double timeSinceLast) {
				uppers.get(i).update(timeSinceLast);
			}
		};

//		new StatsJson(new Json(init.pt.get("NAMES"))) {
//			
//			@Override
//			public void doWithTheJson(settlement.stats.stat.STAT s, Json j, String key) {
//				// TODO Auto-generated method stub
//				
//			}
//			
//			@Override
//			public void doWithMultiplier(StatMultiplier m, Json j, String key) {
//				// TODO Auto-generated method stub
//				
//			}
//		};
		
		
		{
			PATH p = init.pd.getFolder("loyalty");
			for (String f : p.getFiles()) {
				
				Json file = new Json(p.get(f));
				new StatsJson(file) {

					@Override
					public void doWithMultiplier(StatMultiplier m, Json j, String key) {
						handleFault(j, key);
					}

					@Override
					public void doWithTheJson(STAT s, Json j, String key) throws IOException{
						
						double def = s.standing() != null ? s.standing().defaultInput : 0;
						s.standing = new StatStanding(s, def, new StandingDef(j.json(key)));
						SPRITE icon = UI.icons().get(j.json(key), (Icon)null);
						if (icon != null)
							s.info().icon = icon;
						
					}
				};
			}
			
			for (STAT s : all()) {
				if (s.standing == null)
					s.standing = new StatStanding(s, 0);
			}
			
		}
		{
			PATH p = init.pd.getFolder("bonus");
			for (String f : p.getFiles()) {
				
				Json file = new Json(p.get(f));
				new StatsJson(file) {

					@Override
					public void doWithMultiplier(StatMultiplier m, Json j, String key) {
					
						multipliers.setBoost(m, j, key);
						
					}

					@Override
					public void doWithTheJson(STAT s, Json j, String key) {
						s.boosters.read(key, j, StatBooster.make(s));
						
					}
				};
			}
		}
		
		new SValues();
		arrival = new StatArrival(init);
		copy = new StatCopy(init);
//		if (GAME.version() < VERSION.version(54, 7))
//			new Fixer(addables);
	}

	public static StatsBattle BATTLE() {
		return s.battle;
	}

	public static StatsEnv ENV() {
		return s.environment;
	}

	public static StatsAccess ACCESS() {
		return s.access;
	}

	public static StatsPopulation POP() {
		return s.population;
	}

	public static StatsLaw LAW() {
		return s.law;
	}

	public static StatsGovern GOVERN() {
		return s.govern;
	}

	public static StatsFood FOOD() {
		return s.food;
	}

	public static StatsAppearance APPEARANCE() {
		return s.appearance;
	}

	public static StatsTraits TRAITS() {
		return s.traits;
	}

	public static StatsEquip EQUIP() {
		return s.equipables;
	}

	public static StatsWork WORK() {
		return s.work;
	}

	public static StatsEducation EDUCATION() {
		return s.education;
	}

	public static StatsMultipliers MULTIPLIERS() {
		return s.multipliers;
	}

	public static StatsNeeds NEEDS() {
		return s.needs;
	}
	

	public static StatsHome HOME() {
		return s.home;
	}

	public static StatsBurial BURIAL() {
		return s.burial;
	}

	public static StatsReligion RELIGION() {
		return s.religion;
	}

	public static StatsService SERVICE() {
		return s.services;
	}

	public static StatsStored STORED() {
		return s.stored;
	}

	public static LIST<StatCollection> COLLECTIONS() {
		return s.collections;
	}

	public static StatCollection COLLECTION(String key) {
		return s.mapColl.get(key);
	}
	
	public static STAT STAT(String key) {
		return s.mapStat.get(key);
	}

	public static DataRandom<Induvidual> RAN() {
		return s.random;
	}
	
	public static StatsDisease DISEASE() {
		return s.disease;
	}
	
	public static StatsEvent EVENT() {
		return s.event;
	}

//	public static HISTORY_INT_OBJECT<Race> POP() {
//		return s.population.POPULATION.data(null);
//	}

	public static LIST<STAT> all() {
		return s.stats;
	}

	public static LIST<STAT> createThoseThatMatters(Race r) {
		if (r == null)
			return createThoseThatMatters();
		ArrayList<STAT> res = new ArrayList<STAT>(all().size());

		for (STAT s : all()) {
			boolean added = false;
			for (HCLASS c : HCLASSES.ALL()) {
				if (!added && s.key() != null && s.standing().max(c, r) > 0) {
					res.add(s);
					added = true;
				}
			}
		}

		return res;
	}

	public static LIST<STAT> createThoseThatMatters() {
		ArrayList<STAT> res = new ArrayList<STAT>(all().size());

		for (STAT s : all()) {
			boolean added = false;
			for (HCLASS c : HCLASSES.ALL()) {
				for (Race r : RACES.all()) {
					if (!added && s.key() != null && s.standing().max(c, r) > 0) {
						res.add(s);
						added = true;
					}
				}

			}
		}

		return res;
	}

	public static LIST<STAT> createMatterList(boolean indu, boolean standing, Race race) {
		ArrayList<STAT> res = new ArrayList<STAT>(all().size());

		LIST<Race> races = race == null ? RACES.all() : new ArrayList<>(race);

		for (STAT s : all()) {
			if (s.key() == null)
				continue;
			if (!s.info().matters())
				continue;
			if (indu && !s.info().indu())
				continue;

			if (standing) {
				boolean added = false;
				for (HCLASS c : HCLASSES.ALL()) {
					for (Race r : races) {
						if (!added && s.key() != null && s.standing().max(c, r) > 0) {
							res.add(s);
							added = true;
							break;
						}
					}

				}
			} else {
				res.add(s);
			}
		}

		res.sort(new Comparator<STAT>() {

			@Override
			public int compare(STAT o1, STAT o2) {
				return Dictionary.compare(o1.info().name, o2.info().name);
			}
		});

		return res;
	}

	static LIST<Addable> addables() {
		return s.addables;
	}

	public static INFO info() {
		return s.iStats;
	}

	static DataO<Induvidual> count() {
		return s.count;
	}

	static STATS get() {
		return s;
	}

	static void update(Humanoid h, int updateI, boolean day) {
		int updateR = s.iOff[updateI];
		for (StatUpdatableI u : s.updaters) {
			u.update16(h, updateR, day, updateI);
			if (h.isRemoved())
				return;
		}
	}

	void add(Induvidual h) {
		for (Addable s : addables) {
			s.addH(h);
		}
	}

	void remove(Induvidual i) {
		for (Addable s : addables) {
			s.removeH(i);
		}
	}

	void construct(Induvidual i) {
		for (StatInitable in : s.constructers) {
			in.init(i);
		}
	}

	void cancel(Humanoid h) {
		for (StatDisposable i : s.disposables) {
			i.dispose(h);
		}
		remove(h.indu());

	}

	@Override
	protected void save(FilePutter file) {
		file.i(savables.size());
		for (String k : savables.keys()) {
			file.chars(k);
			int pos = file.getPosition();
			file.i(0);
			savables.get(k).save(file);
			file.setAtPosition(pos, file.getPosition()-pos);
		}
	}

	@Override
	protected void load(FileGetter file) throws IOException {
		clear();
		int am = file.i();

		for (int i = 0; i < am; i++) {
			String k = file.chars();
			int pos = file.getPosition();
			int l = file.i();
			
			if (savables.containsKey(k)) {
				savables.get(k).load(file);
				if (file.getPosition() != pos+l) {
					GAME.Warn(k);
					savables.get(k).clear();
					file.setPosition(pos+l);
				}
					
			}else {
				file.setPosition(file.getPosition()+l);
				LOG.ln(k);
			}
				
		}
	}

	@Override
	protected void clear() {
		for (String k : savables.keys()) {
			savables.get(k).clear();;
		}
	}
	
	@Override
	protected void init(boolean loaded) {
		for (StatUpdatable i : uppers)
			i.update(1);
	}

	@Override
	protected void update(double ds, Profiler profiler) {
		upper.update(ds);
	}

	public static void Arrive(Humanoid h) {
		s.arrival.arrive(h);
	}
	
	private final static class StatArrival {
		
		private final ArrayList<StatInitable> inits;
		private final ArrayList<STAT> initsS;
		public final ArrayList<ACTION_O<Induvidual>> onArrivalActions;
		private StatArrival(StatsInit init) {
			inits = new ArrayList<StatsInit.StatInitable>(init.onArrival);
			initsS = new ArrayList<>(init.onArrivalStats);
			onArrivalActions = new ArrayList<ACTION_O<Induvidual>>(init.onArrivalActions);
		}
		
		public void arrive(Humanoid h) {
			for (StatInitable i : inits)
				i.init(h.indu());
			for (STAT s : initsS) {
				double d = s.data(h.indu().clas()).getD(h.race());
				
				

				d = d*s.indu().max(h.indu());
				int v = (int) d;
				if (d-v > RND.rFloat())
					v++;
//				LOG.ln(s.key() + " " + s.info().name + " " + s);
//				LOG.ln(d);
//				LOG.ln( s.data(h.indu().clas()).get(h.race()) + " " + s.data(h.indu().clas()).get(h.race(), 0) + " " + s.dataDivider() + " " + s.pdivider(h.indu().clas(), h.race(), 0));
//				LOG.ln(v);
				v = CLAMP.i(v, 0, s.indu().max(h.indu()));
				s.indu().set(h.indu(), v);
				
			}
			
			for (ACTION_O<Induvidual> a : onArrivalActions)
				a.exe(h.indu());
			
		}
	}
	
	static final class StatCopy {
		
		private final ArrayList<INT_OE<Induvidual>> copies;
		
		private StatCopy(StatsInit init) {
			copies = new ArrayList<INT_OE<Induvidual>>(init.copier);
		}
		
		public void copy(Induvidual dest, Induvidual source) {
			for (INT_OE<Induvidual> ii : copies) {
				ii.set(dest, ii.get(source));
			}
			STATS.RAN().copyFrom(dest, source);
		}
	}

}
