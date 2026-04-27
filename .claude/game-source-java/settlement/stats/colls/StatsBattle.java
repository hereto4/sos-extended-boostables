package settlement.stats.colls;

import game.GAME;
import game.battle.Armies;
import game.battle.div.Div;
import game.boosting.BOOSTING;
import game.boosting.BValue;
import game.boosting.BoostSpec;
import game.boosting.Booster;
import game.boosting.BoosterValue;
import game.faction.FACTIONS;
import game.faction.diplomacy.DIP;
import game.time.TIME;
import init.constant.Config;
import init.race.Race;
import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import init.type.HCLASS;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.military.training.ROOM_M_TRAINER;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import settlement.stats.StatsInit.StatDisposable;
import settlement.stats.standing.StatStanding;
import settlement.stats.stat.STAT;
import settlement.stats.stat.STATData;
import settlement.stats.stat.STATFacade;
import settlement.stats.stat.STATImp;
import settlement.stats.stat.StatCollection;
import settlement.stats.stat.StatInfo;
import settlement.stats.stat.StatObject;
import settlement.stats.util.StatBooster;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import util.data.INT_O.INT_OE;
import util.info.INFO;
import util.keymap.MAPPED;
import util.keymap.RMAP;
import util.text.D;
import world.army.AD;

public class StatsBattle extends StatCollection {

	public final LIST<StatTraining> TRAINING_ALL;
	public final RMAP<StatTraining> TRAINING_MAP;
	public final INT_OE<Induvidual> basicTraining;
	
	public final STAT COMBAT_EXPERIENCE;

	public final STAT ENEMY_KILLS;
	public final StatObject<Div> DIV;
	public final StatProspect RECRUIT;
	public final STAT ROUTING;
	
	public final STAT PROWESS;
	public final STAT CHIVALRY;
	public final STAT CRUELTY;
	
	public final STAT BESIGED;

	public final STAT WAR;
	
	private final INT_OE<Induvidual> position;

	private static CharSequence ¤¤name = "Battle";
	private static CharSequence ¤¤desc = "Battle related stats";
	
	static {
		D.ts(StatsBattle.class);
	}

	public StatsBattle(StatsInit init) {
		super(init, "BATTLE", ¤¤name, ¤¤desc);
		D.t(this);
		position =  init.count.new DataShort("BATTLE_POSITION");
		basicTraining = init.count.new DataNibble("BATTLE_BTRAINING", new INFO(D.g("Basic", "Basic Training"), D.g("BasicD", "Basic training is acquired in any type of training room and is required to be able to join a division.")), 15);
		RECRUIT = new StatProspect(init);
		DIV = new StatDivision(init);

		init.copier.add(basicTraining);
		
		COMBAT_EXPERIENCE = new STATData("COMBAT_EXPERIENCE", init, init.count.new DataNibble("BATTLE_EXPERIENCE"));
		COMBAT_EXPERIENCE.info().icon = UI.icons().s.plusBig;
		init.copier.add(COMBAT_EXPERIENCE.indu());
		
		LinkedList<StatTraining> li = new LinkedList<>();
		KeyMap<StatTraining> map = new KeyMap<>();
		for (ROOM_M_TRAINER<?> tt : ROOM_M_TRAINER.ALL()) {
			StatTraining t = new StatTraining(init, tt);
			init.copier.add(t.count);
			init.copier.add(t.stat.indu());
			li.add(t);
			map.put(t.room.key, t);
		}
		
		this.TRAINING_ALL = new ArrayList<StatsBattle.StatTraining>(li);
		this.TRAINING_MAP = new RMAP<StatsBattle.StatTraining>("TRAINING", TRAINING_ALL);
		
		ENEMY_KILLS = new STATData("ENEMY_KILLS", init,  init.count.new DataShort("BATTLE_KILLS"));
		ENEMY_KILLS.info().icon = UI.icons().l.death.small;
		init.copier.add(ENEMY_KILLS.indu());
		ENEMY_KILLS.info().setInt();
		ROUTING = new STATData("ROUTING", init, init.count.new DataBit("BATTLE_ROUTING") {
			@Override
			public void set(Induvidual t, int s) {
				
				super.set(t, s);
			}
		});
		
		WAR = new STATImp("WAR_TIME", init) {
			@Override
			protected int getDD(HCLASS s, Race r) {
				return (int) DIP.secondsOFPlayerWar();
			}
			
			@Override
			public int pdivider(HCLASS c, Race r, int daysback) {
				return TIME.secondsPerDay()*16*10;
			}
		};
		
		
		PROWESS = new STATImp("PROWESS", init) {
			
			@Override
			protected int getDD(HCLASS s, Race r) {
				
				double d = AD.stats().repF().getD(FACTIONS.player());
				return (int) (CLAMP.d(d, 0, 1)*pdivider(s, r, 0));
			}
		};
		PROWESS.standing = new StatStanding(PROWESS, 0.5);
		
		CHIVALRY = new STATImp("CHIVALRY", init) {
			
			@Override
			protected int getDD(HCLASS s, Race r) {
				
				double d = AD.stats().mercy().getD(FACTIONS.player());
				return (int) (CLAMP.d(d, 0, 1)*pdivider(s, r, 0));
			}
		};
		
		CRUELTY = new STATImp("CRUELTY", init) {
			
			@Override
			protected int getDD(HCLASS s, Race r) {
				
				double d = -AD.stats().mercy().getD(FACTIONS.player());
				return (int) (CLAMP.d(d, 0, 1)*pdivider(s, r, 0));
			}
		};
		
		BESIGED = new STATImp("BESIEGED", init) {
			final double bi = 1.0/8*TIME.secondsPerDay();
			@Override
			protected int getDD(HCLASS s, Race r) {
				double d = SETT.ENTRY().besigeTime()*TIME.secondsPerDay()*bi;
				return (int) (CLAMP.d(d, 0, 1)*pdivider(s, r, 0));
			}
		};
		
	}

	private final class StatDivision extends StatObject<Div> implements StatDisposable {

		
		
		
		final STAT stat;
	
		private final INT_OE<Induvidual> idiv;
		private final StatsInit init;
		public StatDivision(StatsInit init) {
			super(D.g("Division"), D.g("DivisionD", "The Army Division this subject belongs to."));
			this.init = init;
			idiv = init.count.new DataByte("BATTLE_DIVI");
			INT_OE<Induvidual> b = new INT_OE<Induvidual>() {

				@Override
				public int get(Induvidual t) {
					return idiv.get(t) != 0 ? 1 :0;
				}

				@Override
				public int min(Induvidual t) {
					// TODO Auto-generated method stub
					return 0;
				}

				@Override
				public int max(Induvidual t) {
					return 1;
				}

				@Override
				public void set(Induvidual t, int i) {
					// TODO Auto-generated method stub
					
				}

			};
			stat = new STATData("SOLDIERS", "BATTLE_SOLDIERS", init, b);
			new STATFacade("SOLDIERS_TOTAL", init) {
				
				@Override
				protected double getDD(HCLASS s, Race r, int daysBack) {
					return (double)stat.data().get(null, daysBack)/pdivider(s, r, daysBack);
				}
				
				@Override
				public int pdivider(HCLASS c, Race r, int daysback) {
					int pop = STATS.POP().POP.data().get(null);
					return Math.max(pop, 1);
				}
			};
			
			if (Armies.DIVISIONS >= Short.MAX_VALUE)
				throw new RuntimeException();
			init.disposable.add(this);
		}

		@Override
		public final Div get(Induvidual i) {
			
			int di = idiv.get(i);
			if (di != 0)
				return GAME.ARMIES().division((short) (di - 1));
			return null;
		}

		@Override
		public void set(Humanoid h, Div d) {
			Induvidual i = h.indu();
			if (!i.added())
				throw new RuntimeException();
			if (d != null)
				RECRUIT.set(h, null);

			remove(h);

			if (d != null) {
				idiv.set(i, d.index() + 1);
			} else {
				idiv.set(i, 0);
			}
			add(h);
			
		}

		private void remove(Humanoid h) {

			Induvidual i = h.indu();

			for (int ai = 0; ai < init.addable.size(); ai++) {
				init.addable.get(ai).removeH(i);
			}

			Div old = get(h);
			if (old != null) {
				old.reporter.returnPosition((short) position.get(i));
			}
		}

		private void add(Humanoid a) {
			Induvidual i = a.indu();
			
			for (int ai = 0; ai < init.addable.size(); ai++) {
				init.addable.get(ai).addH(i);
			}

			Div now = get(a);
			if (now != null) {
				position.set(i, now.reporter.signUpAndGetPosition(a.body().cX(), a.body().cY(), i.race()));
			}
		}

		@Override
		public void dispose(Humanoid h) {
			set(h, null);

		}

		@Override
		public STAT stat() {
			return stat;
		}

	}

	public final class StatProspect extends StatObject<Div> implements StatDisposable{

		private final STATData stat;
		private final INT_OE<Induvidual>  idiv;
		private final short[] divs = new short[Config.battle().DIVISIONS_PER_ARMY*2];
		
		public StatProspect(StatsInit init) {
			super(D.g("Recruit"),
					D.g("RecruitD", "The Army Division this subject will join when training is complete."));
			idiv = init.count.new DataByte("BATTLE_RECI");
			INT_OE<Induvidual> b = new INT_OE<Induvidual>() {

				@Override
				public int get(Induvidual t) {
					return idiv.get(t) != 0 ? 1 :0;
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
			stat = new STATData("RECRUITS", "BATTLE_RECI", init, b) {
				
				@Override
				public void removeH(Induvidual i) {
					if (get(i) != null)
						divs[get(i).index()] --;
					super.removeH(i);
				}
				
				@Override
				public void addH(Induvidual i) {
					if (get(i) != null)
						divs[get(i).index()] ++;
					super.addH(i);
				}
				
			};
			

			init.disposable.add(new StatDisposable() {
				
				@Override
				public void dispose(Humanoid h) {
					set(h, null);
				}
			});
			
		}

		@Override
		public void set(Humanoid h, Div d) {

			Induvidual i = h.indu();
			if (!i.added())
				throw new RuntimeException();
			stat.removeH(h.indu());
			
			
			if (d != null) {
				idiv.set(i, d.index() + 1);
			} else {
				idiv.set(i, 0);
			}
			stat.addH(h.indu());
			
		}
		
		@Override
		public void dispose(Humanoid h) {
			set(h, null);
		}
		
		public int inDiv(Div div) {
			return divs[div.index()];
		}

		@Override
		public Div get(Induvidual i) {
			if (idiv.get(i) != 0)
				return GAME.ARMIES().division((short) (idiv.get(i) - 1));
			return null;
		}

		@Override
		public STAT stat() {
			return stat;
		}

	}

	public int position(Induvidual i) {
		return position.get(i);
	}
	
	public void makeAKill(Humanoid a) {
		ENEMY_KILLS.indu().inc(a.indu(), 1);
		COMBAT_EXPERIENCE.indu().inc(a.indu(), 1 + RND.rInt(4));
		GAME.ARMIES().factors.reportKill(a);
	}

	public static class StatTraining implements MAPPED {

		private final INT_OE<Induvidual> count;
		public final ROOM_M_TRAINER<?> room;
		public final int tIndex;
		public static final int MAX = 15;
		public static final double MAXI = 1.0/MAX;
		
		public final STATData stat;
		
		StatTraining(StatsInit init, ROOM_M_TRAINER<?> room) {
			this.stat = new STATData(room.key, init, init.count.new DataNibble("BATTLE_TRAINING_" + room.key), new StatInfo(room.tInfo.name, room.tInfo.desc));
			
			count = init.count.new DataNibble("BATTLE_TCOUNT_" + room.key);
			this.room = room;
			tIndex = room.INDEX_TRAINING;
			BOOSTING.connecter(new ACTION() {
				
				@Override
				public void exe() {
					
					for (BoostSpec b : room.boosters.all()) {
						Booster bo = new BoosterValue(bvalue, b.booster.info, b.booster.to(), b.booster.isMul);
						stat.boosters.push(bo, b.boostable);
					}
					
				}
			});
			
			stat.info().icon = room.icon.resized(Icon.S);
			
		}
		

		
		public boolean shouldTrain(Induvidual a, double target, boolean training) {

			if (!STATS.BATTLE().basicTraining.isMax(a))
				return true;
			double t = target;
			double i = stat.indu().getD(a);
			if (t > i)
				return true;
			else if (t < i)
				return false;
			if (t > 0 && training && !count.isMax(a))
				return true;
			return false;
			
		}
		
		public void inc(Induvidual a, double am) {
			
			int sign = am < 0 ? -1 : 1;
			am = Math.abs(am);
			am *= 0x0F*stat.indu().max(a);
			int iam = (int) am;
			if (RND.rFloat() < am-iam)
				iam++;
			iam *= sign;
			
			int c = count.get(a)+iam;

			
			while(c >= count.max(a)) {
				if (stat.indu().isMax(a)) {
					c = count.max(a);
					break;
				}
				stat.indu().inc(a, 1);
				c -= count.max(a);
			}
			while(c <= 0) {
				stat.indu().inc(a, -1);
				c += count.max(a);
			}
			count.set(a, c);
			
		}



		@Override
		public int index() {
			return tIndex;
		}
		
		@Override
		public String key() {
			return room.key;
		}
		
		public double bValue(double d) {
			d = CLAMP.d(d, 0, 1);
			return d;
		}
		
		public final BValue bvalue = new StatBooster() {

			
			
			@Override
			public double vGet(PopTime t) {
				return bValue(stat.data(t.pop.cl).getD(t.pop.race, t.daysBack));
			}
			
			@Override
			public double vGet(Div div) {
				return bValue(stat.div().getD(div));
			}
			
			@Override
			public double vGet(Induvidual indu) {
				return bValue(stat.indu().getD(indu));
			}
		};
	}
	
	public abstract static class HDivStat {

		public HDivStat() {

		}

		protected abstract void returnPosition(short pos);

		protected abstract short signUpAndGetPosition(int x, int y, Race r);

	}

}
