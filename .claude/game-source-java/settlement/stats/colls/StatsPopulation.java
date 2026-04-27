package settlement.stats.colls;

import java.io.IOException;

import game.boosting.BOOSTABLES;
import game.time.TIME;
import game.time.TIMECYCLE;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.UI;
import init.type.CAUSE_ARRIVE;
import init.type.CAUSE_ARRIVES;
import init.type.CAUSE_LEAVE;
import init.type.CAUSE_LEAVES;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.type.HTYPE;
import init.type.HTYPES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.Humanoid.HumanoidResource;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import settlement.stats.StatsInit.Addable;
import settlement.stats.StatsInit.StatInitable;
import settlement.stats.StatsInit.StatUpdatable;
import settlement.stats.StatsInit.StatUpdatableI;
import settlement.stats.standing.StatStanding;
import settlement.stats.stat.DataStat;
import settlement.stats.stat.SETT_STATISTICS;
import settlement.stats.stat.SETT_STATISTICS.SettStatistics;
import settlement.stats.stat.STAT;
import settlement.stats.stat.STATData;
import settlement.stats.stat.STATFacade;
import settlement.stats.stat.STATImp;
import settlement.stats.stat.StatCollection;
import settlement.stats.stat.StatInfo;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.data.DOUBLE_O.DOUBLE_OE;
import util.data.DataO;
import util.data.GETTER_TRANS;
import util.data.GETTER_TRANS.GETTER_TRANSE;
import util.data.INT_O.INT_OE;
import util.info.INFO;
import util.keymap.RMapD.RMapDTwo;
import util.statistics.HISTORY.HISTORY_OBJECT;
import util.text.D;
import util.text.Dic;
import util.statistics.HISTORY_COLLECTION;
import util.statistics.HISTORY_INT;
import util.statistics.HistoryInt;
import util.statistics.HistoryRace;

public class StatsPopulation extends StatCollection{
	
	public final SETT_STATISTICS POP;
	private final DataStat[] pops = new DataStat[HTYPES.ALL().size()];
	public final STAT NOBLES;
	
	public final PopType TYPE;
	public final STAT TRAPPED;
	public final STAT EMMIGRATING;
	public final STAT MAJORITY;
	public final Age age;
	public final STAT SLAVES_SELF;
	public final STAT SLAVES_OTHER;
	public final STAT WRONGFUL;
	public final StatsDeath COUNT;
	
	private final HistoryInt popYearly = new HistoryInt(STATS.DAYS_SAVED, TIME.years(), false);
	
	private final Demography demo;
	public final INT_OE<Induvidual> NAKED;
	final int dy = (int) TIME.years().bitConversion(TIME.days());
	public final GETTER_TRANSE<Induvidual, ENTITY> FRIEND;
	
	private static CharSequence ¤¤name = "Population";
	private static CharSequence ¤¤desc = "Statistics regarding population.";
	
	static {
		D.ts(StatsPopulation.class);
	}
	
	public StatsPopulation(StatsInit init){
		super(init, "POPULATION", ¤¤name, ¤¤desc);
		D.gInit(this);
		age = new Age(init);
		demo = new Demography(init);
		NAKED = init.count.new DataBit("POP_NAKED");
		
		SettStatistics pPOP = new SettStatistics("POP_POP", init, new StatInfo(Dic.¤¤Population, "")) {
			
			@Override
			public int popDivider(HCLASS c, Race r, int daysback) {
				return 1;
			};
		};
		POP = pPOP;
		
		WRONGFUL = new STATImp("WRONGFUL_DEATHS", init) {
			
			@Override
			protected int getDD(HCLASS s, Race r) {
				double am = (int) COUNT.wrongful.get(s).getD(r)*50.0;
				if (s == HCLASSES.CITIZEN()) {
					am += COUNT.wrongful.get(HCLASSES.CHILD()).getD(r)*50.0;
				}
				return CLAMP.i((int)am, 0, STATS.POP().POP.data(s).get(r));
			}
			
			@Override
			public int pdivider(HCLASS c, Race r, int daysback) {
				return super.pdivider(c, r, daysback);
			}
			
			@Override
			public int dataDivider() {
				return 1;
			}
			
		};
		WRONGFUL.info().setMatters(true, false);
		WRONGFUL.info().icon = UI.icons().m.skull;
		
		INT_OE<Induvidual> indu = new INT_OE<Induvidual>() {

			@Override
			public int get(Induvidual t) {
				return t.clas() == HCLASSES.NOBLE() ? 1 : 0;
			}

			@Override
			public int min(Induvidual t) {
				return 0;
			}

			@Override
			public int max(Induvidual t) {
				return 1;
			}

			@Override
			public void set(Induvidual t, int i) {
				
			}
			
		};
		
		NOBLES = new STATFacade("NOBLES", init, indu) {
			
			@Override
			protected double getDD(HCLASS s, Race r, int daysBack) {
				double n = POP.data(HCLASSES.NOBLE()).get(r, daysBack);
				double p = POP.data(HCLASSES.NOBLE()).get(null, daysBack);
				if (p == 0)
					return n > 0 ? 1 : 0;
				return n/p;
			}
		};
		
		NOBLES.info().setInt();
		NOBLES.info().setMatters(true, false);
		NOBLES.info().icon = UI.icons().m.noble;
		
		TYPE = new PopType(init);

		TRAPPED = new STATData("TRAPPED", init, init.count.new DataBit("POP_TRAPPED"));
		TRAPPED.info().setInt();
		
		EMMIGRATING = new STATData("EMIGRATING", init, init.count.new DataBit("POP_EMMI"));
		EMMIGRATING.info().setInt();
		
		
		
		MAJORITY = new STATFacade("MAJORITY", init) {

			@Override
			protected double getDD(HCLASS s, Race r, int daysBack) {
				double pop = POP.data(s).get(null, daysBack);
				if (pop == 0)
					return 0;
				return POP.data(s).get(r, daysBack)/pop;
			}
		};
		MAJORITY.standing = new StatStanding(MAJORITY, 1);
		MAJORITY.info().setMatters(true, false);
		MAJORITY.info().icon = UI.icons().m.plus;
		
		SLAVES_SELF = new STATFacade("SLAVES_SELF", init) {
			
			@Override
			protected double getDD(HCLASS s, Race r, int daysBack) {
				double p = STATS.POP().POP.data(HCLASSES.CITIZEN()).get(r, daysBack)+1;
				return STATS.POP().POP.data(HCLASSES.SLAVE()).get(r, daysBack)/p;
			}

		};
		SLAVES_SELF.info().setMatters(true, false);
		SLAVES_SELF.info().icon = UI.icons().m.slave;
		
		SLAVES_OTHER = new STATFacade("SLAVES_OTHER", init) {
			
			@Override
			protected double getDD(HCLASS s, Race r, int daysBack) {
				double slaves = (STATS.POP().POP.data(HCLASSES.SLAVE()).get(null, daysBack));
				double p = STATS.POP().POP.data(null).get(null, daysBack);
				if (p == 0)
					return 0;
				return (slaves)/p;
			}

		};
		SLAVES_OTHER.info().setMatters(true, false);
		SLAVES_OTHER.info().icon = UI.icons().m.slave;
		init.updatable.add(updater);
		
		COUNT = new StatsDeath(init);
		
		FRIEND = new Friend(init);
		
		
		for (HTYPE t : HTYPES.ALL()) {
			pops[t.index()] = new DataStat("POP_" + t.key, init) {
				
				@Override
				public double getD(Race t, int fromZero) {
					double d = get(t, fromZero);
					double p = POP.data().get(t);
					if (p == 0)
						return CLAMP.d(d, 0, 1);
					return CLAMP.d(d/p, 0, 1);
				}
				
				@Override
				public int min(Race t) {
					return 0;
				}
				
				@Override
				public int max(Race t) {
					return POP.data().get(t);
				}
			};
		}
	
		
		init.addable.add(new Addable() {
			
			@Override
			public void removePrivate(Induvidual i) {
				pPOP.inc(i, -1);
				pops[i.hType().index()].incrFull(i, -1);
				if (i.hType().player)
					popYearly.inc(-1);
				
			}
			
			@Override
			public void addPrivate(Induvidual i) {
				pPOP.inc(i, 1);
				pops[i.hType().index()].incrFull(i, 1);
				if (i.hType().player)
					popYearly.inc(1);
				
			}
		});
		
		
		
	}
	
	public HISTORY_INT popYearly() {
		return popYearly;
	}
	
	private final StatUpdatableI updater = new StatUpdatableI() {
		
		
		
		@Override
		public void update16(Humanoid h, int updateI, boolean day, int ui) {

			
			Induvidual i = h.indu();
			
			if (day) {
				age.DAYS.inc(i, 1);
			}
			
			if ((updateI&0x0F) == (STATS.RAN().get(i, 100, 4)&0x0F)) {
								
				if (age.shouldDieOfOldAge(i))
					HumanoidResource.dead = CAUSE_LEAVES.AGE();
				
				
			}
			
		}		

	};

	

	
	public int pop(HTYPE type) {
		return pops[type.index()].get(null);
	}
	
	public int pop(Race r, HTYPE type) {
		return pops[type.index()].get(r);
	}
	
	public int pop(Race r, HTYPE type, int daysBack) {
		if (r == null)
			return pop(type);
		return pops[type.index()].get(r, daysBack);
	}
	
	public int total(HTYPE type) {
		int am = 0;
		for (Race r : RACES.all())
			am += pop(r, type);
		return am;
	}
	
	public HISTORY_OBJECT<Race> demography(){
		return demo;
	}
	
	public static class Age {
		
		public final STAT AGE_DAYS;
		
		public final INT_OE<Induvidual> DAYS;
		private final INT_OE<Induvidual> DEATH;
		private final double yy = TIME.years().bitConversion(TIME.days());
		private final double yI = 1.0/yy;
		
		Age(StatsInit init){
			DataO<Induvidual>.DataInt days = init.count.new DataInt("POP_AGE_DAYS") {
				
				@Override
				public void set(Induvidual i, int v) {
					STATS.POP().demo.removeH(i);
					super.set(i, v);
					STATS.POP().demo.addH(i);
				}
				
				@Override
				public double getD(Induvidual t) {
					double de = 1.0 + Math.ceil(t.race().bvalue(BOOSTABLES.PHYSICS().DEATH_AGE)*yy);
					int da = DAYS.get(t);
					return CLAMP.d(da/de, 0, 1);
				}
				
				@Override
				public DOUBLE_OE<Induvidual> setD(Induvidual t, double d) {
					int am = (int) Math.round(d*yy);
					DAYS.set(t, am);
					return this;
				}
				
			};
			
			DEATH = init.count.new DataByte("DEATH_AGE");
			
			
			STAT ss = new STATData(null, init, days);
			this.DAYS = ss.indu();
			
			AGE_DAYS = new STATFacade("AGE", init, DAYS) {
				
				@Override
				protected double getDD(HCLASS s, Race r, int daysBack) {
					double pop = STATS.POP().POP.data(s).get(r, daysBack);
					if (pop == 0)
						return 0;
					double dd = ss.data(s).get(r, daysBack)/pop;
					return dd;
				}
				
				
				
			};
			AGE_DAYS.info().setInt();
			
			init.onConstruct.add(new StatInitable() {
				
				@Override
				public void init(Induvidual i) {
					int min = i.race().physics.adultAt+1;
					int max = (int) (0.5*BOOSTABLES.PHYSICS().DEATH_AGE.get(i)*yy);
					int d = max-min;
					if (d <= 0)
						d = 1;
					d = min + RND.rInt(d);
					DAYS.set(i, d);
					DEATH.setD(i, death());
					if (shouldDieOfOldAge(i)) {
						DEATH.inc(i, 1+RND.rInt(5));
					}
					
				}
			});
			
			init.copier.add(days);
			init.copier.add(DEATH);
		}
		

		
//		public void tmpFix(Induvidual i) {
//			DEATH.setD(i, death());
//			
//		}
		
		public DOUBLE_OE<Induvidual> years = new DOUBLE_OE<Induvidual>() {

			@Override
			public double getD(Induvidual t) {
				return DAYS.get(t)*yI;
			}

			@Override
			public DOUBLE_OE<Induvidual> setD(Induvidual t, double d) {
				int am = (int) Math.round(d*yy);
				DAYS.set(t, am);
				return this;
			}
			
		};

		public DOUBLE_OE<Induvidual> dage = new DOUBLE_OE<Induvidual>() {

			@Override
			public double getD(Induvidual t) {
				return DAYS.getD(t);
			}

			@Override
			public DOUBLE_OE<Induvidual> setD(Induvidual t, double d) {
				int am = (int) Math.round(d*yy*t.race().bvalue(BOOSTABLES.PHYSICS().DEATH_AGE));
				DAYS.set(t, am);
				return this;
			}
			
		};
		
		public int lifespan(Induvidual i) {
			return (int) (i.race().bvalue(BOOSTABLES.PHYSICS().DEATH_AGE)*yy);
		}
		
		public boolean shouldDieOfOldAge(Induvidual i) {
			int now = DAYS.get(i);
			int death = deathDay(i);
			return (now >= death);
		}

		public int deathDay(Induvidual i) {
			int ll = (int) (i.race().bvalue(BOOSTABLES.PHYSICS().DEATH_AGE));
			ll *= DEATH.getD(i);
			int yy = (int) TIME.years().bitConversion(TIME.days());
			ll*= yy;
			ll += STATS.RAN().get(i, 13)%yy;
			return ll;
		}

//		public boolean shoudRetire(Induvidual i) {
//			int oa = (int) (BOOSTABLES.PHYSICS().DEATH_AGE.get(i)*yy);
//			double oldAge = oa * (0.4 + 0.6*(1.0-STATS.WORK().RETIREMENT_AGE.decree().get(i.clas()).getD(i.race())));
//			return DAYS.get(i) >= oldAge;
//		}
//		
//		public boolean shoudRetire(HCLASS cl, Race race, double lifeYears, double ageDays) {
//			int oa = (int) (lifeYears*yy);
//			int oldAge = (int) (oa - oa*(0.2 + 0.3*STATS.WORK().RETIREMENT_AGE.decree().get(cl).getD(race)));
//			return ageDays >= oldAge;
//		}
		
//		public void init(Induvidual i) {
//			int min = i.race().physics.adultAt+1;
//			int max = (int) (0.5*BOOSTABLES.PHYSICS().DEATH_AGE.get(i)*yy);
//			int d = min + RND.rInt((max-min));
//			DAYS.set(i, d);
//		}
		
		public boolean isAdult(Induvidual i) {
			return DAYS.get(i) > i.race().physics.adultAt;
		}
	}
	
	private static class Demography implements HISTORY_OBJECT<Race>, Addable{

		private static int size = 32;
		private int[][] perRace = new int[32][RACES.all().size()];
		{D.gInit(StatsPopulation.class);}
		private final INFO info = new INFO(D.g("Demography"), D.g("DemographyDesc", "The different age groups of your citizens"));
		
		Demography(StatsInit init){
			init.addable.add(this);
		}

		@Override
		public double getD(Race t, int fromZero) {
			
			if (t == null) {
				double acc = 0;
				int pop = 0;
				for (int i = 0; i < RACES.all().size(); i++) {
					Race r = RACES.all().get(i);
					int p = STATS.POP().POP.data(HCLASSES.CITIZEN()).get(r) + STATS.POP().POP.data(HCLASSES.CHILD()).get(r);
					acc += getD(r, fromZero)*p;
					pop += p;
				}
				if (pop == 0)
					return 0;
				return acc/pop;
			}

			return perRace[fromZero][t.index];
		}
		
		@Override
		public INFO info() {
			return info;
		}

		@Override
		public void addPrivate(Induvidual i) {

			if (i.clas() == HCLASSES.CITIZEN() || i.clas() == HCLASSES.CHILD()) {
				perRace[getT(i)][i.race().index] += 1;
			}
			
		}

		@Override
		public void removePrivate(Induvidual i) {
			if (i.clas() == HCLASSES.CITIZEN() || i.clas() == HCLASSES.CHILD()) {
				perRace[getT(i)][i.race().index] -= 1;
			}
		}
		
		private int getT(Induvidual i) {
			
			int max = (int) Math.ceil(i.race().bvalue(BOOSTABLES.PHYSICS().DEATH_AGE)*TIME.years().bitConversion(TIME.days()));
			int c = STATS.POP().age.DAYS.get(i);
			int ii = (size-1)*c /max;
			ii = CLAMP.i(ii, 0, size-1);
			return ii;
		}

		@Override
		public TIMECYCLE time() {
			return TIME.days();
		}

		@Override
		public int historyRecords() {
			return size;
		}

		@Override
		public double getD(Race t) {
			return getD(t, 0);
		}

	}
	
	public static final class StatsDeath {

		private final LIST<PopData> deaths;
		private final LIST<PopData> enters;
		private double newEntries = 0;
		private double timer = 0;
		public final GETTER_TRANSE<Induvidual, CAUSE_ARRIVE> arrive;

		private final RMapDTwo<HCLASS, Race> wrongful = new RMapDTwo<>(HCLASSES.MAP(), RACES.map()); 
		
		StatsDeath(StatsInit init) {
			
			ArrayList<PopData> deaths = new ArrayList<PopData>(CAUSE_LEAVES.ALL().size());
			for (CAUSE_LEAVE l : CAUSE_LEAVES.ALL()) {
				deaths.add(new PopData("L_"+l.key, l, init, true));
			}
			
			this.deaths = deaths;
			
			ArrayList<PopData> enters = new ArrayList<PopData>(CAUSE_ARRIVES.ALL().size());
			for (CAUSE_ARRIVE l : CAUSE_ARRIVES.ALL()) {
				enters.add(new PopData("A_"+l.index(), l, init, true));
			}
			this.enters = enters;
			
			if (CAUSE_ARRIVES.ALL().size() > 16)
				throw new RuntimeException("Change to bigger data");
			
			final INT_OE<Induvidual> data = init.count.new DataNibble("POP_ARRIVE");
			
			arrive = new GETTER_TRANSE<Induvidual, CAUSE_ARRIVE>(){

				@Override
				public CAUSE_ARRIVE get(Induvidual f) {
					return CAUSE_ARRIVES.ALL().get(data.get(f));
				}

				@Override
				public void set(Induvidual f, CAUSE_ARRIVE t) {
					data.set(f, t.index());
				}
				
			};
			
			init.savers.put("WRONG_DEATH_DATA",wrongful);
			
			init.savers.put("DEATH_MISC", new SAVABLE() {
				
				@Override
				public void save(FilePutter file) {
					file.d(timer);
					file.d(newEntries);
				}
				
				@Override
				public void load(FileGetter file) throws IOException {
					timer = file.d();
					newEntries = file.d();
				}
				
				@Override
				public void clear() {
					timer = 0;
					newEntries = 0;
				}
			});
			
			init.upers.add(new StatUpdatable() {
				
				@Override
				public void update(double ds) {
					{
						double d = newEntries/128.0;
						if (d < 1)
							d = 1;
						newEntries -= d*ds;
						newEntries = CLAMP.d(newEntries, 0, Double.MAX_VALUE);
					}
					timer+= ds;
					if (timer < TIME.secondsPerDay()) { 
						return;
					}
					
					timer -= TIME.secondsPerDay();
					for (int ci = 0; ci < HCLASSES.ALL().size(); ci++) {
						HCLASS cc = HCLASSES.ALL().get(ci);
						for (int ri = 0; ri < RACES.all().size(); ri++) {
							double c = wrongful.get(cc).getD(RACES.all().get(ri));
							
							double d = c*0.1;
							if (d - (int)d > RND.rFloat())
								d = (int)d + 1;
							if (d < 1)
								d = 1;
							c -= d;
							c = CLAMP.d(c, 0, c);
							wrongful.get(cc).setD(RACES.all().get(ri), c);
						}
					}
				}
			});


			
		}
		
		public double newEntries() {
			return newEntries /128;
		}
		
		public LIST<PopData> leaves(){
			return deaths;
		}
		
		public LIST<PopData> enters(){
			return enters;
		}
		
		public void reg(Induvidual i, CAUSE_ARRIVE c){
			if (c != null) {
				arrive.set(i, c);
				enters.get(c.index()).inc(i);
				if (c.fromoutside && i.player())
					newEntries ++;
			}
		}
		
		public void reg(Induvidual i, CAUSE_LEAVE c){
			if (c != null) {
				deaths.get(c.index()).inc(i);
				if (!c.natural) {
					wrongful.get(i.clas()).incD(i.race(), c.defaultStanding());
				}
			}
		}
		
		public static class PopData {

			private final HistoryRace[] data = new HistoryRace[HCLASSES.ALL().size()];
			private final HistoryRace total = new HistoryRace(STATS.DAYS_SAVED, TIME.days(), false);
			private final INFO info;

			PopData(String key, INFO info, StatsInit init, boolean save) {
				this.info = info;
				for (int i = 0; i < data.length; i++) {
					data[i] = new HistoryRace(STATS.DAYS_SAVED, TIME.days(), false);
				}
				
				init.savers.put("POPDATA_"+key, new SAVABLE() {
					
					@Override
					public void save(FilePutter file) {
						HCLASSES.MAP().saver().save(data, file);
						
					}
					
					@Override
					public void load(FileGetter file) throws IOException {
						HCLASSES.MAP().loader().load(data, file);
					}
					
					@Override
					public void clear() {
						for (HistoryRace r : data)
							r.clear();
					}
				});
				
			}
			
			public HISTORY_COLLECTION<Race> statistics(HCLASS c){
				if (c == null)
					return total;
				return data[c.index()];
			}
			
			void inc(Induvidual i) {
				if (i.player()) {
					total.inc(i.race(), 1);
				}
				data[i.clas().index()].inc(i.race(), 1);
			}
			
			public INFO info() {
				return info;
			}
			
		}
		

		

		
	}

	public static class PopType implements GETTER_TRANS<Induvidual, PopType.Type> {

		private final INT_OE<Induvidual> d;
		private final ArrayListGrower<Type> all = new ArrayListGrower<>();

		public final Type IMMIGRANT;
		public final Type NATIVE;
		public final Type FORMER_SLAVE;
		
		PopType(StatsInit init){
			d = init.count.new DataNibble("POP_TYPE");
			IMMIGRANT = all.addReturn(new Type(0, "IMMIGRANTS", init, UI.icons().m.arrow_right));
			NATIVE = all.addReturn(new Type(1, "NATIVES", init, UI.icons().m.citizen));
			FORMER_SLAVE = all.addReturn(new Type(2, "FORMER_SLAVES", init, UI.icons().m.chainsFree));
			
			for (Type t : all)
				t.info().setInt();
			
			init.addable.add(new Addable() {
				
				@Override
				public void removePrivate(Induvidual i) {
					if (i.clas().player && i.clas() != HCLASSES.SLAVE())
						all.getC(d.get(i)).data.inc(-1);
				}
				
				@Override
				public void addPrivate(Induvidual i) {
					if (i.clas().player && i.clas() != HCLASSES.SLAVE())
						all.get(d.get(i)).data.inc(1);
				}
			});
			
			init.copier.add(new INT_OE<Induvidual>() {

				@Override
				public int get(Induvidual t) {
					return PopType.this.get(t).index;
				}

				@Override
				public int min(Induvidual t) {
					return 0;
				}

				@Override
				public int max(Induvidual t) {
					return all.size()-1;
				}

				@Override
				public void set(Induvidual t, int i) {
					PopType.this.all.getC(i).set(t);
					
				}
				
			});
		}
		
		@Override
		public Type get(Induvidual f) {
			return all.getC(d.get(f));
		}
		
		public Type getByIndex(int in) {
			return all.get(in);
		}
		
		public LIST<Type> all(){
			return all;
		}
		
		public class Type extends STATFacade {
			
			public final int index;
			private final HistoryInt data = new HistoryInt(STATS.DAYS_SAVED, TIME.days(), true) {
				
				@Override
				public void load(FileGetter file) throws IOException {
					super.load(file);
					set(0);
				};
			};
			
			Type(int index, String key, StatsInit init, SPRITE icon) {
				super(key, init, new INT_OE<Induvidual>() {

					@Override
					public int get(Induvidual t) {
						return d.get(t) == index ? 1 : 0;
					}

					@Override
					public int min(Induvidual t) {
						// TODO Auto-generated method stub
						return 0;
					}

					@Override
					public int max(Induvidual t) {
						// TODO Auto-generated method stub
						return 1;
					}

					@Override
					public void set(Induvidual t, int i) {
						// TODO Auto-generated method stub
						
					}
				}, null);
				this.index = index;
				init.savers.put("PTYPE_" + key, data);
				info.icon = icon;
				//info().setInt();
			}

			@Override
			protected double getDD(HCLASS s, Race r, int daysBack) {
				
				double pop = STATS.POP().POP.data().get(null, daysBack);
				double type = data.get(daysBack);
				//double non = STATS.POP().POP.data().get(null, daysBack)-type;
				if (pop <= 0)
					return 0;
				double v = type/pop;
				v = CLAMP.d(v, 0, 1);
				//v *= non/pop;
				return v;
			}
			
			@Override
			public int pdivider(HCLASS c, Race r, int daysback) {
				return STATS.POP().POP.data().get(null, daysback);
			}
			
			public void set(Induvidual f) {
				if (f.added() && f.clas().player && f.clas() != HCLASSES.SLAVE()) {
					all.getC(d.get(f)).data.inc(-1);
					d.set(f, index);
					data.inc(1);
				}else {
					d.set(f, index);
				}
			}


			
		}
		
		
	}
	
	private static class Friend implements GETTER_TRANSE<Induvidual, ENTITY> {

		private final INT_OE<Induvidual> i;
		
		Friend(StatsInit init){
			i = init.count.new DataInt("POP_FRIEND");
		}
		
		@Override
		public ENTITY get(Induvidual f) {
			int i = this.i.get(f);
			if (i == 0)
				return null;
			ENTITY e = SETT.ENTITIES().getByID(i-1);
			if (e == null || e.isRemoved())
				return null;
			return e;
		}

		@Override
		public void set(Induvidual f, ENTITY t) {
			if (t == null)
				i.set(f, 0);
			else
				i.set(f, t.id()+1);
		}
		
	}

	public static void main(String[] args) {
		
		int[] dd = new int[100];
		
		for (int i = 0; i < dd.length*200; i++) {
			int di = (int)(death()*dd.length);
			dd[di] ++;
		}	
		
		int k = 0;
		for (int d : dd) {
			d = (int) Math.ceil(d /= 10.0);
			System.out.print(k++);
			System.out.print("\t");
			while (d-- > 0) {
				System.out.print("*");
			}
			System.out.println();
		}
		
		
	}
	
	static double death() {
		double ran = Math.abs(RND.rFloat());
		
		if (ran < 0.9) {
			ran/= 0.9;
			ran = Math.pow(ran, 0.20);
			ran *= 0.9;
		}else {
			ran -= 0.9;
			ran /= 0.1;
			ran = Math.pow(ran, 0.4);
			ran = 1.0-ran;
			ran = 0.9 + 0.1*ran;
		}

		return 0.45 + 0.55*ran;
	}
}
