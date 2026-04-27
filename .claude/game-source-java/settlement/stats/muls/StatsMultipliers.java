package settlement.stats.muls;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.battle.div.Div;
import game.boosting.BOOSTING;
import game.boosting.BValue;
import game.boosting.BoostSpec;
import game.boosting.BoostSpecs;
import game.faction.FACTIONS;
import game.faction.FCredits.CTYPE;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import init.sprite.SPRITES;
import init.sprite.UI.UI;
import init.type.CAUSE_ARRIVES;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.type.HTYPES;
import init.type.POP_CL;
import settlement.entity.EntityIterator;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.RoomBlueprint;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomInstance;
import settlement.room.main.employment.RoomEmploymentSimple;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import settlement.stats.StatsInit.Addable;
import settlement.stats.StatsInit.StatUpdatable;
import settlement.stats.StatsInit.StatUpdatableI;
import settlement.stats.law.LAW;
import settlement.stats.service.StatsService;
import settlement.stats.stat.SETT_STATISTICS.SettStatistics;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.Json;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayInt;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.SPRITE;
import util.data.DOUBLE.DoubleImp;
import util.data.INT_O.INT_OE;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.info.INFO;
import util.keymap.MAPPED;
import util.keymap.RMAP;
import util.keymap.RMapInt.RMapIntTwo;
import util.statistics.HistoryInt;
import util.statistics.HistoryRace;
import util.text.Dic;
import world.map.regions.Region;

public final class StatsMultipliers {
	
	private final static String pre = "EVENT_";

	private final LIST<LIST<StatMultiplier>> classes;
	private final LIST<StatMultiplier> all;

	public final StatMultiplier KILLER;
	public final StatMultiplierAction PROSECUTION;
	public final StatMultiplierAction EMANCIPATE;
	public final StatMultiplierAction HANDOUT;
	public final StatMultiplierAction DAY_OFF;
	public final StatMultiplierWork OVERTIME;
	
	public final RMAP<StatMultiplier> MAP;
	
	public StatsMultipliers(StatsInit init, StatsService service){
		
		LinkedList<StatMultiplier> all = new LinkedList<>();

		KILLER = new Killer(init, all);
		PROSECUTION = new Prosecution(init, all);
		EMANCIPATE = new Emancipate(init, all);
		
		HANDOUT = new Handout("HANDOUT", init, all, SPRITES.icons().s.money, HCLASSES.CITIZEN());
		DAY_OFF = new StatMultiplierActionImp("DAY_OFF", init, all, new SPRITE.Twin(SPRITES.icons().m.workshop, SPRITES.icons().m.anti), HCLASSES.SLAVE(), HCLASSES.CITIZEN());
		OVERTIME = new StatMultiplierWork("OVERTIME", init, all, new SPRITE.Twin(SPRITES.icons().m.workshop, SPRITES.icons().m.arrow_up), HCLASSES.SLAVE(), HCLASSES.CITIZEN());
		
		this.all = new ArrayList<>(all);
		
		
		
		ArrayList<LIST<StatMultiplier>> classes = new ArrayList<>(HCLASSES.ALL().size());
		
		for (HCLASS cl : HCLASSES.ALL()) {
			all = new LinkedList<>();
			for (StatMultiplier m : this.all) {
				if (m.classes[cl.index()])
					all.add(m);
			}
			LIST<StatMultiplier> res = new ArrayList<>(all);
			classes.add(res);
		}
		
		this.classes = classes;

		init.upers.add(new StatUpdatable() {
			
			@Override
			public void update(double ds) {
				for (StatMultiplier m : StatsMultipliers.this.all)
					m.update(ds);
			}
		});
		

		
		
		MAP = new RMAP<StatsMultipliers.StatMultiplier>("MULTIPLIERS", StatsMultipliers.this.all);
	}
	
	public void setBoost(StatMultiplier m, Json json, String key) {
		BValue b = new BValue() {
			
			@Override
			public double vGet(Div div) {
				return 0;
			}
			
			@Override
			public double vGet(Induvidual indu) {
				return m.value(indu);
			}

			@Override
			public double vGet(Region reg) {
				return 0;
			}


			@Override
			public double vGet(Player f) {
				return vGet(POP_CL.clP());
			}

			@Override
			public double vGet(FactionNPC f) {
				return 0;
			}

			@Override
			public double vGet(PopTime t) {
				return m.value(t.pop.cl, t.pop.race, t.daysBack);
			}
		};

		m.boosters.read(key, json, b);
	}
	
	public LIST<StatMultiplier> get(HCLASS cl){
		return classes.get(cl.index());
	}
	
	public LIST<StatMultiplier> all(){
		return all;
	}
	
	public static abstract class StatMultiplier extends INFO implements MAPPED{
		
		private final int index;
		public final String key;
		public final CharSequence verb;
		private boolean[] classes = new boolean[HCLASSES.ALL().size()];
		public final BoostSpecs boosters;
		
		private StatMultiplier(C cc, LISTE<StatMultiplier> all, HCLASS... cl) {
			super(cc.name, cc.desc);
			this.verb = cc.verb;
			this.index = all.add(this);
			this.key = cc.key;
			
			boosters = new BoostSpecs(cc.name, UI.icons().s.crown, true);
			
			for (HCLASS c : cl) {
				classes[c.index()] = true;
			}
		}
		
		@Override
		public int index() {
			return index;
		}
		
		public boolean available(HCLASS cl, Race race) {
			return classes[cl.index()];
		}
		
		public boolean available(Induvidual i) {
			return available(i.clas(), i.race());
		}

		public abstract double value(Induvidual h);
		
		public abstract double value(HCLASS cl, Race race, int daysBack);
		
		protected abstract void update(double ds);
		
		@Override
		public String key() {
			return key;
		}
		
	}
	
	private static class Killer extends StatMultiplier {
		
		private final Data data = new Data();
		private static final double di = 1.0/1000;
		
		private Killer(StatsInit init, LISTE<StatMultiplier> all) {
			super(new C("SERIAL_KILLER", init), all, HCLASSES.CITIZEN(), HCLASSES.SLAVE());
			init.savers.put("SERIAL_KILLER_DATA", data);
		}
		
		@Override
		public double value(HCLASS cl, Race race, int daysBack) {
			if (cl == null) {
				double v = 0;
				double am = 0;
				for (int ci = 0; ci < HCLASSES.ALL().size(); ci++) {
					HCLASS cll = HCLASSES.ALL().get(ci);
					if (cll.player) {
						double p = STATS.POP().POP.data(cll).get(race);
						v += value(cll, race, daysBack)*p;
						am += p;
					}
				}
				if (am == 0)
					return 0;
				return di*v/am;
			}
			
			if (race == null) {
				double v = 0;
				double am = 0;
				for (int ri = 0; ri < RACES.all().size(); ri++) {
					Race r = RACES.all().get(ri);
					double p = STATS.POP().POP.data(cl).get(r);
					v += data.get(cl).history(r).get(daysBack)*p;
					am += p;
				}
				if (am == 0)
					return 0;
				return di*v/am;
			}
			return data.get(cl).history(race).get(daysBack)*di;
		}

		@Override
		protected void update(double ds) {
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				Race r = RACES.all().get(ri);
				double ra = GAME.events().killer.victimRace() == r ? GAME.events().killer.rate() : 0;
				data.get(HCLASSES.CITIZEN()).set(r, (int) (1000*ra));
				data.get(HCLASSES.SLAVE()).set(r, (int) (1000*ra));
			} 
			
		}

		@Override
		public double value(Induvidual h) {
			return value(h.clas(), h.race(), 0);
		}

		
	}
	
	public static abstract class StatMultiplierAction extends StatMultiplier {
		
		public final SPRITE icon;
		
		private StatMultiplierAction(String key, StatsInit init, LISTE<StatMultiplier> all, SPRITE icon, HCLASS... cl) {
			super(new C(key, init), all, cl);
			this.icon = icon;
		}
		
		private StatMultiplierAction(C c, LISTE<StatMultiplier> all, SPRITE icon, HCLASS... cl) {
			super(c, all, cl);
			this.icon = icon;
		}
		
		public abstract boolean canUnmark();
		public abstract int unmarkable(HCLASS cl, Race race);
		public abstract void unmark(HCLASS cl, Race race);
		public boolean markIs(Humanoid a) {
			return markIs(a.indu());
		}
		public abstract boolean markIs(Induvidual a);
		public boolean canBeMarked(Induvidual a) {
			return maxAmount(a.clas(), a.race()) > 0;
		}
		public abstract void mark(HCLASS cl, Race race, int amount);
		public abstract void mark(Humanoid a, boolean set);
		public abstract void consume(Humanoid a);
		public final boolean consumeIs(Humanoid a) {
			return consumeIs(a.indu());
		}
		public abstract boolean consumeIs(Induvidual a);
		public abstract int maxAmount(HCLASS cl, Race race);
		public void info(GBox box, int amount) {
			
		}
		
	}

	
	private static class Prosecution extends StatMultiplierAction {
		
		private final INT_OE<Induvidual> in;
		protected final ArrayInt count;
		
		private Prosecution(StatsInit init, LISTE<StatMultiplier> all) {
			super("PROSECUTION", init, all, SPRITES.icons().m.slave, HCLASSES.CITIZEN());
			count = new ArrayInt(RACES.all().size());
			init.savers.put("PROSECUTION_COUNT", count);
			in = init.count.new DataBit("MUL_PROSECUTION");
			init.addable.add(new Addable() {
				
				@Override
				public void removePrivate(Induvidual i) {
					if (i.player())
						count.inc(i.race(), -in.get(i));
					else
						in.set(i, 0);
				}
				
				@Override
				public void addPrivate(Induvidual i) {
					if (i.player())
						count.inc(i.race(), in.get(i));
					else
						in.set(i, 0);
				}
			});
			
		}

		@Override
		public double value(HCLASS cl, Race race, int daysBack) {
			return CLAMP.d(200.0*LAW.process().prosecute.rate(race).getD(), 0, 1);
		}

		@Override
		protected void update(double ds) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean markIs(Induvidual a) {
			return in.get(a) == 1;
		}

		@Override
		public void mark(Humanoid a, boolean set) {
			count.inc(a.race(), -in.get(a.indu()));
			in.set(a.indu(), set ? 1 : 0);
			count.inc(a.race(), in.get(a.indu()));
		}

		@Override
		public int maxAmount(HCLASS cl, Race race) {
			return STATS.POP().POP.data(cl).get(race)-count.get(race);
		}

		@Override
		public void mark(HCLASS cl, Race race, int amount) {
			new Iter(cl, race, amount) {

				@Override
				protected boolean processAndShouldBreakH(Humanoid h, int ie) {
					if (h.race() == race && h.indu().clas() == HCLASSES.CITIZEN() && !markIs(h.indu())) {
						mark(h, true);
						amount--;
						if (amount <= 0)
							return true;
					}
					return false;
				}
				
			};
		}
		
		@Override
		public boolean canUnmark() {
			return true;
		}
		
		@Override
		public int unmarkable(HCLASS cl, Race race) {
			return count.get(race);
		}
		
		@Override
		public void unmark(HCLASS cl, Race race) {
			new Iter(cl, race, 1) {

				@Override
				protected boolean processAndShouldBreakH(Humanoid h, int ie) {
					if (h.race() == race && h.indu().clas() == HCLASSES.CITIZEN() && markIs(h.indu())) {
						mark(h, false);
					}
					return false;
				}
				
			};
		}

		@Override
		public void consume(Humanoid a) {
			
		}

		@Override
		public double value(Induvidual h) {
			return in.get(h) == 1 ? 1 :0;
		}

		@Override
		public boolean consumeIs(Induvidual a) {
			return false;
		}
		
		@Override
		public void info(GBox box, int amount) {
			if (SETT.ROOMS().GUARD.employment().employed() == 0) {
				box.add(box.text().add(SETT.ROOMS().GUARD.employment().verb).add(':').s().add('0'));
			}
		}
	}
	
	private static class Emancipate extends StatMultiplierAction {
		
		private final HistoryInt data = new HistoryInt(STATS.DAYS_SAVED, TIME.days(), true);
		private final DoubleImp timer = new DoubleImp();
		private final double rate = 16.0/(TIME.secondsPerDay()*8);
		
		private Emancipate(StatsInit init, LISTE<StatMultiplier> all) {
			super("SLAVES_FREED", init, all, SPRITES.icons().m.chainsFree, HCLASSES.SLAVE());
			init.savers.put("SLAVES_FREED_DATA", data);
			init.savers.put("SLAVES_FREED_DATA_TIMER", timer);;
		}
		
		@Override
		public double value(Induvidual h) {
			return STATS.POP().TYPE.get(h) == STATS.POP().TYPE.FORMER_SLAVE ? 1 : 0;
		}

		@Override
		public boolean canUnmark() {
			return false;
		}
		
		@Override
		public boolean markIs(Induvidual a) {
			return false;
		}

		@Override
		public int maxAmount(HCLASS cl, Race race) {
			return STATS.POP().POP.data(cl).get(race);
		}

		@Override
		public void mark(HCLASS cl, Race race, int amount) {
			
			new Ite(amount, race);
		}
		
		private  class Ite extends EntityIterator.Humans {

			private int amount;
			private final Race race;
			
			Ite(int amount, Race race){
				this.amount = amount;
				this.race = race;
				iterate();
			}
			
			@Override
			protected boolean processAndShouldBreakH(Humanoid h, int ie) {
				if (h.race() == race && h.indu().clas() == HCLASSES.SLAVE()) {
					mark(h, true);
					amount--;
					if (amount <= 0)
						return true;
				}
				return false;
			}
			
		}

		@Override
		public double value(HCLASS cl, Race race, int daysBack) {
			if (STATS.MULTIPLIERS().PROSECUTION.value(cl, race, daysBack) > 0)
				return 0;
			return data.get(daysBack)/(1.0+STATS.POP().POP.data(cl).get(null));
		}


		@Override
		protected void update(double ds) {
			timer.incD(-rate*ds);
			if (timer.getD() > 0)
				return;
			timer.incD(1);
			int tot = data.get();
			int pop = STATS.POP().POP.data(HCLASSES.SLAVE()).get(null);
			
			tot -= pop;
			tot = CLAMP.i(tot, 0, pop*16);
			data.set(tot);
			
		}

		@Override
		public void mark(Humanoid h, boolean set) {
			data.inc(16);
			h.HTypeSet(HTYPES.SUBJECT(), null, CAUSE_ARRIVES.EMANCIPATED());
			STATS.POP().TYPE.FORMER_SLAVE.set(h.indu());
			
		}

		@Override
		public void consume(Humanoid a) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public boolean consumeIs(Induvidual a) {
			return false;
		}

		@Override
		public int unmarkable(HCLASS cl, Race race) {
			return 0;
		}

		@Override
		public void unmark(HCLASS cl, Race race) {
			// TODO Auto-generated method stub
			
		}

		
	}

	
	private static class StatMultiplierActionImp extends StatMultiplierAction implements StatUpdatableI{
		
		protected final SettStatistics active;
		protected final RMapIntTwo<HCLASS, Race>  selected = new RMapIntTwo<>(HCLASSES.MAP(), RACES.map());
		protected final INT_OE<Induvidual> iActive;
		protected final INT_OE<Induvidual> iActiveCount;
		protected final INT_OE<Induvidual> iSelected;
		
		
		private StatMultiplierActionImp(String key, StatsInit init, LISTE<StatMultiplier> all, SPRITE icon, HCLASS... cl) {
			super(key, init, all, icon, cl);
			active = new SettStatistics("MUL_"+key, init, null);
			init.savers.put(key + "SEL", new SAVABLE() {
				
				@Override
				public void save(FilePutter file) {
					selected.save(file);
					
				}
				
				@Override
				public void load(FileGetter file) throws IOException {
					selected.load(file);
					selected.clear();
				}
				
				@Override
				public void clear() {
					selected.clear();
				}
			});
			iActive = init.count.new DataBit("MUL_"+key+"_ACTIVE");
			iActiveCount = init.count.new DataCrumb("MUL_"+key+"_ACOUNT");
			iSelected = init.count.new DataBit("MUL_" + key + "_SEL");
			
			init.addable.add(new Addable() {
				
				@Override
				public void removePrivate(Induvidual i) {
					selected.get(i.clas()).inc(i.race(), -iSelected.get(i));
					active.inc(i, -iActive.get(i));
				}
				
				@Override
				public void addPrivate(Induvidual i) {
					selected.get(i.clas()).inc(i.race(), iSelected.get(i));
					active.inc(i, iActive.get(i));
				}
			});
			init.updatable.add(this);
		}
		
		private void remove(Induvidual i) {
			if (i.added()) {
				selected.get(i.clas()).inc(i.race(), -iSelected.get(i));
				active.inc(i, -iActive.get(i));
			}
			
			
			
		}
		
		private void add(Induvidual i) {
			if (i.added()) {
				selected. get(i.clas()).inc(i.race(), iSelected.get(i));
				active.inc(i, iActive.get(i));
			}
		}
		
		@Override
		public void update16(Humanoid h, int updateR, boolean day, int updateI) {
			if ((updateI & 0xF) == 0 &&  iActive.get(h.indu()) > 0) {
				Induvidual i = h.indu();
				if (iActiveCount.get(i) == 0) {
					remove(i);
					iActive.inc(i, -1);
					add(i);
				}else {
					iActiveCount.inc(h.indu(), -1);
				}
			}
		}

		@Override
		public boolean markIs(Induvidual a) {
			return iSelected.get(a) != 0;
		}

		@Override
		public void consume(Humanoid a) {
			remove(a.indu());
			iActive.setD(a.indu(), 1.0);
			iActiveCount.set(a.indu(), 2 + RND.rInt(1));
			iSelected.set(a.indu(), 0);
			add(a.indu());
		}
		
		@Override
		public boolean consumeIs(Induvidual a) {
			return iActive.get(a) != 0;
		}

		@Override
		public void mark(Humanoid a, boolean set) {
			remove(a.indu());
			iActive.setD(a.indu(), 0);
			iSelected.set(a.indu(), set ? 1 : 0);
			add(a.indu());
		}


		@Override
		public void mark(HCLASS cl, Race race, int amount) {
			new Iter(cl, race, amount) {
				@Override
				protected boolean processAndShouldBreakH(Humanoid h, int ie) {
					if (h.indu().clas() == cl && h.race() == race && !markIs(h.indu()) && iActive.get(h.indu()) == 0) {
						mark(h, true);
						amount --;
						return amount <= 0;
					}
					return false;
				}
			};
			
		}
		
		@Override
		public double value(Induvidual h) {
			return iActive.get(h);
		}


		@Override
		public double value(HCLASS cl, Race race, int daysBack) {
			return active.data(cl).get(race, daysBack)/(1.0+STATS.POP().POP.data(cl).get(race));
		}


		@Override
		protected void update(double ds) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public int maxAmount(HCLASS cl, Race race) {
			return STATS.POP().POP.data(cl).get(race) - selected.get(cl).get(race)-active.data(cl).get(race);
		}

		@Override
		public boolean canUnmark() {
			return true;
		}

		@Override
		public int unmarkable(HCLASS cl, Race race) {
			return selected.get(cl).get(race);
		}

		@Override
		public void unmark(HCLASS cl, Race race) {
			new Iter(cl, race, 1) {
				@Override
				protected boolean processAndShouldBreakH(Humanoid h, int ie) {
					if (h.indu().clas() == cl && h.race() == race && markIs(h.indu())) {
						mark(h, false);
					}
					return false;
				}
			};
			
		}
		
		
	}
	
	public static class StatMultiplierWork extends StatMultiplierActionImp implements StatUpdatableI{

		public final LIST<RoomBlueprintImp> ROOMS;
		private final boolean[] roomsB = new boolean[SETT.ROOMS().AMOUNT_OF_BLUEPRINTS];
		private int[][] available = new int[HCLASSES.ALL().size()][RACES.all().size()+1];
		private int ai = -121;
		
		private StatMultiplierWork(String key, StatsInit init, LISTE<StatMultiplier> all, SPRITE icon, HCLASS... cl) {
			super(key, init, all, icon, cl);
			
			LinkedList<RoomBlueprintImp> rooms = new LinkedList<>();
			
			ROOMS = new ArrayList<RoomBlueprintImp>(rooms);
			
			BOOSTING.connecter(new ACTION() {
				
				@Override
				public void exe() {
					
					for (BoostSpec s : boosters.all()) {
						
						for (RoomEmploymentSimple b : SETT.ROOMS().employment.ALLS()) {
							if (b.blueprint().bonus() == s.boostable)
								roomsB[b.blueprint().index()] = true;
							
							
						}
						
					}
					
				}
			});
			
		}
		
		private final EntityIterator.Humans iter = new EntityIterator.Humans() {
			
			@Override
			protected boolean processAndShouldBreakH(Humanoid h, int ie) {
				if (canBeMarked(h.indu())) {
					available[h.indu().clas().index()][h.race().index] ++;
					available[h.indu().clas().index()][RACES.all().size()] ++;
				}
				return false;
			}
		};
		
		
		@Override
		public int maxAmount(HCLASS cl, Race race) {
			
			if (Math.abs(ai-GAME.updateI()) > 120) {
				ai = GAME.updateI();
				for (int[] i : available)
					Arrays.fill(i, 0);
				iter.iterate();
			}
			
			int ri = race == null ? RACES.all().size() : race.index();
			return available[cl.index()][ri];
			
		}
		
		@Override
		public void mark(HCLASS cl, Race race, int amount) {
			new Iter(cl, race, amount) {
				@Override
				protected boolean processAndShouldBreakH(Humanoid h, int ie) {
					if (h.indu().clas() == cl && h.race() == race && !markIs(h.indu()) && iActive.get(h.indu()) == 0) {
						if (canBeMarked(h.indu())) {
							mark(h, true);
							amount --;
							return amount <= 0;
						}
					}
					return false;
				}
			};
		}
		
		public boolean canMark(RoomBlueprint b) {
			return roomsB[b.index()];
		}
		
		@Override
		public boolean canBeMarked(Induvidual a) {
			if (a.player() && !markIs(a) && !consumeIs(a)) {
				RoomInstance ins = STATS.WORK().EMPLOYED.get(a);
				if (ins != null && roomsB[ins.blueprint().index()]) {
					return true;
				}
			}
			return false;
		}
		
	}
	
	private static class Handout extends StatMultiplierActionImp {
		
		private final int amount = 400;
		
		Handout(String key, StatsInit init, LISTE<StatMultiplier> all, SPRITE icon, HCLASS... cl){
			super(key, init, all, icon, cl);
		}
		
		@Override
		public int maxAmount(HCLASS cl, Race race) {
			int creds = (int) FACTIONS.player().credits().credits();
			creds /= amount;
			int am = super.maxAmount(cl, race);
			return Math.min(creds, am);
			
		}
		
		@Override
		public void mark(Humanoid a, boolean set) {
			super.mark(a, set);
			FACTIONS.player().credits().inc(-amount, CTYPE.MISC);
			if (set) {
				super.consume(a);
			}
		}
		
		@Override
		public void info(GBox box, int amount) {
			box.textL(Dic.¤¤Curr);
			box.tab(5);
			box.add(GFORMAT.iIncr(box.text(), -amount*this.amount));
		}
		
		@Override
		public boolean canUnmark() {
			return false;
		}
		
		
		
	}
	
	

	

	
	
	private static class Data implements SAVABLE {
		
		private final HistoryRace[] iii = new HistoryRace[HCLASSES.ALL().size()]; 
		
		Data(){
			for (int ci = 0; ci < HCLASSES.ALL().size(); ci++) {
				iii[ci]= new HistoryRace(STATS.DAYS_SAVED, TIME.days(), true);
			}
		}
		
		public HistoryRace get(HCLASS cl) {
			return iii[cl.index()];
		}

		@Override
		public void save(FilePutter file) {
			HCLASSES.MAP().saver().save(iii, file);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			HCLASSES.MAP().loader().load(iii, file);
		}

		@Override
		public void clear() {
			for (HistoryRace i : iii)
				i.clear();
		}
		
	}
	
	private static abstract class Iter extends EntityIterator.Humans {

		int amount;
		final Race race;
		final HCLASS cl;
		
		Iter(HCLASS cl, Race race, int amount){
			this.amount = amount;
			this.race = race;
			this.cl = cl;
			iterate();
		}
		
	}
	
	private static class C extends INFO{
		
		private final CharSequence verb;
		private final String key;
		
		C(String key, StatsInit init){
			super(init.dText.json(pre + key));
			key = pre + key;
			verb = init.dText.json(key).text("VERB");
			this.key = key;
		}
		
		C(String key, CharSequence name, CharSequence desc, CharSequence verb){
			super(name, desc);
			this.verb = verb;
			this.key = key;
		}
		
	}
	

}
