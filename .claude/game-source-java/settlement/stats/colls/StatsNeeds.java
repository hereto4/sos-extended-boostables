package settlement.stats.colls;


import game.boosting.BOOSTABLES;
import game.boosting.Boostable;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import init.type.CAUSE_ARRIVES;
import init.type.CAUSE_LEAVE;
import init.type.CAUSE_LEAVES;
import init.type.HCLASSES;
import init.type.HTYPES;
import init.type.NEEDS;
import init.type.NEED_E;
import init.type.POP_CL;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.Humanoid.HumanoidResource;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import settlement.stats.StatsInit.StatInitable;
import settlement.stats.StatsInit.StatUpdatableI;
import settlement.stats.stat.STAT;
import settlement.stats.stat.STATData;
import settlement.stats.stat.StatCollection;
import settlement.stats.stat.StatInfo;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.text.Str;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.text.D;
import view.sett.IDebugPanelSett;
import view.tool.PlacableSimpleTile;

public class StatsNeeds extends StatCollection{
	
	public final LIST<StatNeedNormal> SNEEDS;
	public final LIST<STAT> OTHERS;
	public final STAT EXHASTION;
	public final StatDanger INJURIES;
	public final StatExposure EXPOSURE;
	public final INT_OE<Induvidual> DIRTINESS;
	
//	public final StatsNeedsDisease disease;

	private static CharSequence ¤¤desc = "The current {0} need of a subject. As it increases, subjects will search out related services.";
	private static CharSequence ¤¤name = "Needs";
	private static CharSequence ¤¤descc = "Needs related stats";
	static {
		D.ts(StatsNeeds.class);
	}
	
	public StatsNeeds(StatsInit init){
		super(init, "NEEDS", ¤¤name, ¤¤descc);
		
		
		INJURIES = new StatDanger("INJURIES", init, init.count.new DataByte("NEED_DANGER"));
		EXPOSURE = new StatExposure(init);
		EXHASTION = new STATData("EXHAUSTION", init, init.count.new DataByte("NEED_EXHAUST", 32));
		OTHERS = new ArrayList<STAT>(INJURIES.COUNT, EXHASTION, EXPOSURE.COUNT);
//		disease = new StatsNeedsDisease(init);
		DIRTINESS = init.count.new DataNibble("NEED_DIRTY");
		
		ArrayListGrower<StatNeedNormal> all = new ArrayListGrower<>();
		
		for (NEED_E n : NEEDS.ALLE()) {
			if (n == NEEDS.TYPES().HUNGER) {
				INT_OE<Induvidual> ii = init.count.new DataByte("NEED_" + n.key, StatNeedNormal.CHUNK*4) {
					
					@Override
					public void set(Induvidual i, int s) {
						
						STATS.FOOD().STARVATION.indu().set(i, get(i) >= StatNeedNormal.CHUNK*3 ? 1 : 0);
						super.set(i, s);
					};
				};
				all.add(new StatNeedNormal(init, n, ii));
			}else {
				INT_OE<Induvidual> ii = init.count.new DataNibble1("NEED_" + n.key);
				all.add(new StatNeedNormal(init, n, ii));
			}
		}
		
		SNEEDS = all;
		
		IDebugPanelSett.add("Cure insanity", new ACTION() {
			
			@Override
			public void exe() {
				for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
					if (e instanceof Humanoid) {
						Humanoid a = (Humanoid) e;
						if (a.indu().hType() == HTYPES.DERANGED()) {
							a.HTypeSet(HTYPES.SUBJECT(), null, CAUSE_ARRIVES.CURED());
						}
						
					}
				}
			}
		});
		
		IDebugPanelSett.add("insanity", new PlacableSimpleTile("insanity") {

			@Override
			public CharSequence isPlacable(int tx, int ty) {
				return null; 
			}

			@Override
			public void place(int tx, int ty) {
				for (ENTITY e : SETT.ENTITIES().getAtTile(tx, ty)) {
					if (e instanceof Humanoid) {
						Humanoid h =(Humanoid) e;
						h.HTypeSet(HTYPES.DERANGED(), CAUSE_LEAVES.INSAVITY(), null);
					}
				}
			}
			
		});
		

		
		
		init.updatable.add(updater);
		init.onArrival.add(new StatInitable() {
			
			@Override
			public void init(Induvidual i) {
				if (RND.oneIn(8)) {
					DIRTINESS.incD(i, RND.rExpo());
				}
				for (StatNeedNormal n : SNEEDS) {
					n.inc(i, (int) (RND.rFloat()*StatNeedNormal.CHUNK*2));
				}
			}
		});
		
	}
	
	public double grime(Induvidual i) {
		return (DIRTINESS.get(i)>>1)/7.0;
	}
	
	private final StatUpdatableI updater = new StatUpdatableI() {
		
		
		@Override
		public void update16(Humanoid h, int updateI, boolean day, int ui) {
			
			Induvidual i = h.indu();

			if (INJURIES.update(h)) {
				return;
			}
			
			if(!STATS.DISEASE().status(i).active && i.hType() != HTYPES.TOURIST()) {
				
				for (StatNeedNormal n : SNEEDS) {
				
					if (RND.rFloat() < n.need.rate.get(i))
						n.inc(i, 1);
					
//					if (((updateI)&0x0FF) < n.need.rate.get(i) * 0x0100) {
//						n.inc(i, 1);
//					}
					
				}
				
				if (((ui)&0x0FF) < BOOSTABLES.PHYSICS().SOILING.get(i) * 0x0100)
					DIRTINESS.inc(i, 1);
				if (SETT.TERRAIN().get(h.physics.tileC()).roofIs()) {
					if (SETT.ROOMS().map.is(h.physics.tileC())
							&& SETT.ROOMS().map.get(h.physics.tileC()).blueprint().makesDudesDirty())
						DIRTINESS.inc(i, 1);

				} else if (SETT.WEATHER().rain.getD() > 0 && !SETT.WEATHER().snow.rainIsSnow()) {
					DIRTINESS.inc(i, -1);
				}
				
			}
			
			double insaneRate = InsaneRate(i.race());
			
			if (day && i.clas() == HCLASSES.CITIZEN() && i.hType() != HTYPES.DERANGED()) {
				if (DI*2.0*insaneRate/(1 + BOOSTABLES.BEHAVIOUR().SANITY.get(i)*20) > RND.rFloat()) {
					h.HTypeSet(HTYPES.DERANGED(), CAUSE_LEAVES.INSAVITY(), null);
				}
			}
			
			EXPOSURE.update(h, insaneRate);
			
		}
		
		private final double DI = 1.0/(8*10);
		private double itime = -10;
		private final double[] insaneRate = new double[RACES.all().size()];
		
		private double InsaneRate(Race r) {
			if (TIME.currentSecond() - itime > 10) {
				double pop = STATS.POP().POP.data().get(null);
				if (pop < 500) {
					for (int ri = 0; ri < RACES.all().size(); ri++) {
						Race race = RACES.all().get(ri);
						int insane = STATS.POP().pop(race, HTYPES.DERANGED());
						if (insane > STATS.POP().POP.data(HCLASSES.CITIZEN()).get(race)/(1+BOOSTABLES.BEHAVIOUR().SANITY.get(POP_CL.clP(race, HCLASSES.CITIZEN()))*100)) {
							insaneRate[race.index()] = 0;
						}else {
							insaneRate[race.index()] = 0.001;
						}
					
					}
				}else {
					double popD = CLAMP.d((pop-500)/2500.0, 0.001, 5);
					for (int ri = 0; ri < RACES.all().size(); ri++) {
						Race race = RACES.all().get(ri);
						double v = popD;
						
						int insane = STATS.POP().pop(race, HTYPES.DERANGED());
						if (insane > STATS.POP().POP.data(HCLASSES.CITIZEN()).get(race)/(1+BOOSTABLES.BEHAVIOUR().SANITY.get(POP_CL.clP(race, HCLASSES.CITIZEN()))*15)) {
							v = 0;
						}
						insaneRate[r.index()] = v;
					}
				}
				
				itime = TIME.currentSecond();
			}
			return insaneRate[r.index()];
		}
		
	};
	
	public static final class StatDanger {
		
		public final STAT DANGER;
		public final STAT COUNT;
		private final INT_O<Induvidual> count;
		
		private StatDanger(String key, StatsInit init, INT_OE<Induvidual> c) {
			
			INT_OE<Induvidual> count = new INT_OE<Induvidual>() {
				@Override
				public void set(Induvidual t, int s) {
					c.set(t, s);
					DANGER.indu().set(t, critical(t) ? 1 : 0);
				}

				@Override
				public int get(Induvidual t) {
					return c.get(t);
				}

				@Override
				public int min(Induvidual t) {
					return c.min(t);
				}

				@Override
				public int max(Induvidual t) {
					return c.max(t);
				}
			};
			
			COUNT = new STATData(key, "NEED_" + key, init, count);
			DANGER = new STATData(null, init, init.count.new DataBit("NEED_DANGER_" + key), COUNT.info());
			this.count = count;
		}
		
		public boolean inDanger(Induvidual i) {
			return count.get(i) >= count.max(i)/2;
		}
		
		public boolean willDie(Induvidual i, double treatment) {
			
			if (inDanger(i)) {
				double chance = 1.0-treatment;
				chance = CLAMP.d(chance, 0, 1);
				int ran = (int) (chance*0x0FFFF);
				
				
				return ran >= STATS.RAN().get(i, 7, 16);
			}
			return false;
		}
		
		public void setNonDanger(Induvidual i) {
			if (inDanger(i))
				COUNT.indu().set(i, count.max(i)/2-2);
		}
		
		public boolean critical(Induvidual i) {
			return count.get(i) >= 3*(count.max(i)/4);
		}
		
		boolean update(Humanoid a) {
			Induvidual i = a.indu();
			if (count.get(i) != 0) {
				if (count.isMax(i)) {
					HumanoidResource.dead = a.lastLeaveCause() != null ? a.lastLeaveCause() : CAUSE_LEAVES.getAccident();
					return true;
				}else if (inDanger(i)) {
					COUNT.indu().inc(i, 4);
				}else if (a.division() == null || !a.division().settings().mustering()){
					COUNT.indu().inc(i, -(int) Math.ceil(1 + 0x0F * BOOSTABLES.PHYSICS().HEALTH.get(i)));
				}
			}
			return false;
		}
		
	}
	
	public static final class StatExposure {
		
		public final STAT DANGER;
		public final STAT COUNT;
		private final INT_OE<Induvidual> count;
		private final INT_OE<Induvidual> type;
		
		private final int hidden = 8;
		private final int critical = 26;
		
		private StatExposure(StatsInit init) {
			
			count = init.count. new DataNibble1("NEED_EXPOSURE") {
				@Override
				public void set(Induvidual t, int v) {
					super.set(t, v);
					DANGER.indu().set(t, critical(t) ? 1 : 0);
				}
			};
			type = init.count. new DataBit("NEED_EXPOSURE_TYPE");
			
			INT_OE<Induvidual> statCount = new INT_OE<Induvidual>() {
				final int max = count.max(null)-hidden;
				
				@Override
				public void set(Induvidual t, int s) {
					s += hidden;
					s = CLAMP.i(s, 0, count.max(t));
					count.set(t, s);
				}

				@Override
				public int get(Induvidual t) {
					return CLAMP.i(count.get(t)-hidden, 0, max(t));
				}

				@Override
				public int min(Induvidual t) {
					return 0;
				}

				@Override
				public int max(Induvidual t) {
					return max;
				}
			};

			
			COUNT = new STATData("EXPOSURE", "NEED_EXPOSURE", init, statCount);
			DANGER = new STATData(null, init, init.count.new DataBit("NEED_DANGER_" + "EXPOSURE"), COUNT.info());
		}
		
		public boolean critical(Induvidual i) {
			return count.get(i) >= critical;
		};
		
		public void fix(Induvidual a) {
			count.set(a, 0);
		}
		
		public boolean isCold(Induvidual a) {
			return type.get(a) == 0;
		}
		
		void update(Humanoid a, double insaneRate) {
			
			
			Induvidual i = a.indu();
			
			if (STATS.DISEASE().status(i).active) {
				if (critical(i)) {
					count.set(i, critical-1);
				}
				return;
			}
			
			double exposure = SETT.WEATHER().temp.getEntityTemp()*4.0;
			if (exposure == 0) {
				if (count.get(i) > critical-1)
					count.set(i, critical-1);
				return;
			}
			int type = 1;
			Boostable b = BOOSTABLES.PHYSICS().RESISTANCE_HOT;
			CAUSE_LEAVE cause =  CAUSE_LEAVES.HEAT();
			if (exposure < 0) {
				b = BOOSTABLES.PHYSICS().RESISTANCE_COLD;
				cause = CAUSE_LEAVES.COLD();
				exposure = - exposure;
				type = 0;
			}
			
			double protection = b.get(i);
			if (SETT.TERRAIN().get(a.tc()).roofIs())
				protection += 1.0;
			else {
				protection = 0.5 + 0.5*SETT.GROUND().MOISTURE_CURRENT.get(a.tc());
			}
			
			exposure /= (1+protection);
			exposure = CLAMP.d(exposure, 0, 16);
			int ex = (int) exposure;
			
			if (exposure - ex > RND.rFloat()) {
				ex++;
			}

			if (type != this.type.get(i)) {
				int cc = count.get(i);
				int c = Math.min(ex, cc);
				cc -= c;
				count.inc(i, -cc);
				ex -= c;
			}
			
			if (ex > 0)
				this.type.set(i, type);
			
			count.inc(i, ex);
			if (i.hType().player && insaneRate > 0 && ((STATS.RAN().get(i, 22, 4)) == 0) && i.hType() == HTYPES.SUBJECT() && critical(i) ) {
				a.HTypeSet(HTYPES.DERANGED(), cause, null);
			}
		}
		
	}
	
	public static class StatNeedNormal {
		
		public final NEED_E need;
		private final STAT stat;
		private static final int CHUNK = 0x010; 
		
		StatNeedNormal(StatsInit init, NEED_E ni, INT_OE<Induvidual> ii) {
			this.need = ni;
			stat = new STATData(ni.key, "NEED_" + ni.key, init, ii, new StatInfo(ni.nameNeed, new Str(¤¤desc).insert(0, ni.nameNeed)));
		}
		
		public void fix(Induvidual h) {
			inc(h, -CHUNK);
		}
		
		public void fixMax(Induvidual h) {
			stat.indu().set(h, stat. indu().get(h)&~CHUNK);
		}

		private void inc(Induvidual h, int i) {
			stat.indu().inc(h, i);
		}
		
		public STAT stat() {
			return stat;
		}
		
		public int breakpoint() {
			return CHUNK;
		}
		
		public int getPrio(Humanoid h) {
			return stat.indu().get(h.indu())/CHUNK;
		}
		
		public int getPrio(Induvidual i) {
			return stat.indu().get(i)/CHUNK;
		}
		
	}


	public void clear(Induvidual indu) {
		for (StatNeedNormal n : SNEEDS)
			n.stat.indu().set(indu, 0);
		for (STAT s : OTHERS)
			s.indu().set(indu, 0);
		DIRTINESS.set(indu, 0);
	}

	
	
	
}
