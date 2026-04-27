package settlement.stats.colls;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.battle.div.Div;
import game.boosting.BOOSTABLES;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.BoosterValue;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import game.time.TIME;
import init.race.RACES;
import init.race.Race;
import init.sprite.UI.UI;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.type.HTYPE;
import init.type.HTYPES;
import init.type.POP_CL;
import init.type.WGROUP;
import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.home.HOME;
import settlement.room.infra.elderly.ROOM_RESTHOME;
import settlement.room.main.RoomBlueprintImp;
import settlement.room.main.RoomInstance;
import settlement.room.main.employment.RoomEmploymentIns;
import settlement.room.main.employment.RoomEmploymentSimple;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import settlement.stats.StatsInit.Addable;
import settlement.stats.StatsInit.StatDisposable;
import settlement.stats.StatsInit.StatUpdatable;
import settlement.stats.StatsInit.StatUpdatableI;
import settlement.stats.standing.StatStanding;
import settlement.stats.stat.SETT_STATISTICS.SettStatistics;
import settlement.stats.stat.STAT;
import settlement.stats.stat.STATData;
import settlement.stats.stat.STATFacade;
import settlement.stats.stat.StatCollection;
import settlement.stats.stat.StatDecree;
import settlement.stats.stat.StatInfo;
import settlement.stats.stat.StatObject;
import settlement.stats.util.StatHoverer;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import util.data.DataO;
import util.data.GETTER_TRANS.GETTER_TRANSE;
import util.data.INT_O.INT_OE;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.keymap.RMapInt.RMapIntTwo;
import util.text.D;
import util.text.Dic;
import world.map.regions.Region;

public class StatsWork extends StatCollection{
	
	private static CharSequence ¤¤lim = "Decree";
	private static CharSequence ¤¤estimated = "Estimated Retirees";
	private static CharSequence ¤¤employment = "Employment";
	private static CharSequence ¤¤employmentD = "This subject's place of work.";
	private static CharSequence ¤¤retTarget = "Retirement age target";
	private static CharSequence ¤¤accessD = "Access to retirement activities.";
	private static CharSequence ¤¤qualityD = "Quality and Degrade of retirement activities.";
	private static CharSequence ¤¤Type = "Type";
	private static CharSequence ¤¤TypeD = "Subjects will try and find the activity that suits them best. (Highest work fulfillment)\")";
	
	private static CharSequence ¤¤name = "Work";
	private static CharSequence ¤¤desc = "Work related stats.";
	
	static {
		D.ts(StatsWork.class);
	}
	
	public final Retirement RET;
	public final StatObject<RoomInstance> EMPLOYED;
	public final STAT WORK_FULFILLMENT;
	public final STAT WORK_TIME;
	private final SettStatistics health;
	public final GETTER_TRANSE<Induvidual, RoomBlueprintImp> profession;
	public final Incapacitated incap;
	public final INT_OE<Induvidual> proximity;
	public final INT_OE<Induvidual> fetchProximity;
	public final INT_OE<Induvidual> slack;
	
	public final LIST<STAT> workStats;
	
	public StatsWork(StatsInit init){
		super(init, "WORK", ¤¤name, ¤¤desc);
		
		profession = new Profession(init);
		EMPLOYED = new Work(init);
		
		WORK_TIME = new STATData("WORK_TIME", "WORK_TIME", init, init.count.new DataNibble1("WORK_TIME", Humanoid.WORK_TICKS + Humanoid.WORK_TICKS/2));
		

		proximity = init.count.new DataShort("WORK_PROXIMITY");
		fetchProximity = init.count.new DataShort("WORK_FETCH_PROXIMITY");
		slack = init.count.new DataShort("WORK_SLACK");
		
		WORK_FULFILLMENT = new STATData("FULFILLMENT", init, init.count.new DataNibble("WORK_FULL")) {
			@Override
			public int pdivider(HCLASS c, Race r, int daysback) {
				if (c == HCLASSES.SLAVE())
					return STATS.POP().pop(r, HTYPES.SLAVE(), daysback);
				return STATS.POP().pop(r, HTYPES.STUDENT(), daysback) + STATS.POP().pop(r, HTYPES.RECRUIT(), daysback) + STATS.POP().pop(r, HTYPES.SUBJECT(), daysback);
			}
		};
		WORK_FULFILLMENT.info().icon = UI.icons().m.heart;
		
		
		
		RET = new Retirement(init);
		
		
		{
			health = new SettStatistics("WORK_HEALTH", init, "", "");
			double m = 1;
			for (RoomEmploymentSimple s : SETT.ROOMS().employment.ALLS()) {
				m = Math.min(m, 1.0-s.healthFactor);
			}
			
			BValue vv = new BValue() {
				
				@Override
				public double vGet(Player f) {
					return vGet(POP_CL.clP());
				}

				@Override
				public double vGet(Region reg) {
					return 0;
				}

				@Override
				public double vGet(Induvidual indu) {
					RoomInstance r = EMPLOYED.get(indu);
					if (r != null)
						return 1.0-r.blueprintI().employment().healthFactor;
					return 0;
				}

				@Override
				public double vGet(Div div) {
					return 0;
				}

				@Override
				public double vGet(PopTime time) {
					int emp = EMPLOYED.stat().data(time.pop.cl).get(time.pop.race);
					int tot = STATS.POP().POP.data(time.pop.cl).get(time.pop.race);
					if (tot == 0)
						return 0;
					double d = emp/tot;
					return d*(1.0 - health.data(time.pop.cl).get(time.pop.race, time.daysBack)/(emp*256.0));
				}

				@Override
				public double vGet(FactionNPC f) {
					return 0;
				}
			};
			
			new BoosterValue(vv, new BSourceInfo(Dic.¤¤Employment, UI.icons().s.hammer), 1, m, true).add(BOOSTABLES.PHYSICS().HEALTH);
			
		}
		
		incap = new Incapacitated(init);
		workStats = new ArrayList<STAT>(EMPLOYED.stat(), WORK_FULFILLMENT, WORK_TIME, incap.stat);
		init.updatable.add(updater);

	}
	
	private static class StatRet extends STATData {
		
		StatRet(StatsInit init, DataO<Induvidual>.DataAbs data, CharSequence name, CharSequence desc){
			super(null, init, data, new StatInfo(name, desc));
			info().setMatters(false, true);
		}
		
		@Override
		public int pdivider(HCLASS c, Race r, int daysback) {
			return STATS.POP().pop(r, HTYPES.RETIREE(), daysback);
		}
		
	}
	
	private final StatUpdatableI updater = new StatUpdatableI() {
		
		
		@Override
		public void update16(Humanoid h, int updateR, boolean day, int ui) {
			Induvidual i = h.indu();
		
			
			if (day) {
//				int extra = WORK_TIME.indu().max(i)-WORK_TIME.indu().get(i);
//				if (extra > 0 && EMPLOYED.get(h) != null && h.indu().hType().works) {
//					VIEW.s().ui.subjects.show(h);
//					GAME.Notify(extra);
//				}
				WORK_TIME.indu().inc(i, -(Humanoid.WORK_TICKS+(RND.rFloat() < 0.9 ? 1 : 0)));
				
				if (STATS.MULTIPLIERS().OVERTIME.markIs(h)) {
					STATS.MULTIPLIERS().OVERTIME.consume(h);
				}else if (STATS.MULTIPLIERS().DAY_OFF.markIs(h)) {
					STATS.MULTIPLIERS().DAY_OFF.consume(h);
					WORK_TIME.indu().setD(i, 1.0);
				}
			}
			
			
			if (HPoll.Handler.works(h)) {
				WORK_TIME.indu().inc(i, 1);
			}
			
			if (h.indu().hType() == HTYPES.RETIREE() && EMPLOYED.get(h) != null && EMPLOYED.get(h).blueprint() instanceof ROOM_RESTHOME) {
				double d = ((ROOM_RESTHOME)EMPLOYED.get(h).blueprint()).quality(EMPLOYED.get(h));
				RET.RETIREMENT_HOME_QUALITY.indu().setD(h.indu(), d);
			}
			
		}
		
	};
	
	
	private final class Work extends StatObject<RoomInstance> implements StatDisposable{

		private final INT_OE<Induvidual> data; 
		private final STATData stat;
		
		
		Work(StatsInit init) {
			super(¤¤employment, ¤¤employmentD);
			data = init.count.new DataInt("WORK_WORKI");

			
			INT_OE<Induvidual> b = new INT_OE<Induvidual>(){

				@Override
				public int get(Induvidual t) {
					return t.hType().works && data.get(t) != 0 ? 1:0;
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
					// TODO Auto-generated method stub
					
				}
				
			};
			stat = new STATData("EMPLOYED", "WORK_WORKI", init, b) {
				
				@Override
				public int pdivider(HCLASS c, Race r, int daysback) {
					if (c == HCLASSES.SLAVE())
						return STATS.POP().pop(r, HTYPES.SLAVE(), daysback);
					return STATS.POP().pop(r, HTYPES.STUDENT(), daysback) + STATS.POP().pop(r, HTYPES.RECRUIT(), daysback) + STATS.POP().pop(r, HTYPES.SUBJECT(), daysback);
				}
				
				@Override
				public void addPrivate(Induvidual i) {
					RoomInstance ins = get(i);
					if (ins == null) {
						data.set(i, 0);
					}
					
					if (get(i) != null) {
						health.inc(i.clas(), i.race(), (int) (get(i).blueprintI().employment().healthFactor*256), -1);
					}
					super.addPrivate(i);
				}
				
				@Override
				public void removePrivate(Induvidual i) {
					if (get(i) != null) {
						health.inc(i.clas(), i.race(), -(int) (get(i).blueprintI().employment().healthFactor*256), -1);
					}
					super.removePrivate(i);
				}
			
			};
			stat.info().icon = UI.icons().m.workshop;
			
			init.disposable.add(this);
			
		}

		@Override
		public RoomInstance get(Induvidual f) {
			int i = data.get(f);
			if (i > 0)
				return (RoomInstance) SETT.ROOMS().map.getByIndex(i-1);
			return null;
		}

		@Override
		public void dispose(Humanoid h) {
			set(h, null);
		}

		
		@Override
		public void set(Humanoid h, RoomInstance t) {
			if (t != null) {
				if (!t.employees().active() || !t.active())
					GAME.Notify("shit!");
			}
			
			
			HOME home = STATS.HOME().GETTER.get(h, this);
			STATS.HOME().GETTER.set(h, null);
			
			Induvidual f = h.indu();
			
			if (!f.added())
				throw new RuntimeException();
			stat.removeH(f);
			if (get(f) != null) {
				get(f).employees().fire(h);
			}
			if (t!= null) {
				profession.set(f, t.blueprintI());
				data.set(f, t.index()+1);
				t.employees().employ(h);;

			}else {
				data.set(f, 0);
			}
			
			stat.addH(f);

			setData(f, t);
			if (home != null) {
				
				if (home.canOccupy(h)) {
					if (get(f) != null || home.occupants() < home.occupantsMax())
						STATS.HOME().GETTER.set(h, home);
				}
			}else
				STATS.HOME().GETTER.hasSearched.indu().set(f, 0);
				
			
		}
		
		private void setData(Induvidual f, RoomInstance t) {
			WORK_FULFILLMENT.indu().setD(f, 0);
			RET.RETIREMENT_HOME_ACCESS.indu().setD(f, 0);
			RET.RETIREMENT_HOME_QUALITY.indu().setD(f, 0);
			RET.RETIREMENT_HOME_TYPE.indu().setD(f, 0);
			if (t == null)
				return;
			
			if (f.clas() == HCLASSES.SLAVE() || f.hType() == HTYPES.STUDENT() ||  f.hType() == HTYPES.RECRUIT() || f.hType() == HTYPES.SUBJECT()) {
				double d = f.race().pref().getWork(t.blueprintI().employment());
				WORK_FULFILLMENT.indu().setD(f, d);
				
			}else if(f.hType() == HTYPES.RETIREE() && t.blueprint() instanceof ROOM_RESTHOME) {
				RET.RETIREMENT_HOME_ACCESS.indu().setD(f, 1);
				RET.RETIREMENT_HOME_QUALITY.indu().setD(f, ((ROOM_RESTHOME)t.blueprint()).quality(t));
				RET.RETIREMENT_HOME_TYPE.indu().setD(f, f.race().pref().getWork(t.blueprintI().employment()));
			}
			
		}

		@Override
		public STAT stat() {
			return stat;
		}
		

		
	}
	
	private static class Profession implements GETTER_TRANSE<Induvidual, RoomBlueprintImp>{

		private final INT_OE<Induvidual> ii;
		
		Profession(StatsInit init) {
			ii = init.count.new DataShort("WORK_PROF");
		}

		@Override
		public void set(Induvidual f, RoomBlueprintImp t) {
			int i = t == null ? 0 : (t.index()+1);
			ii.set(f, i);
		}

		@Override
		public RoomBlueprintImp get(Induvidual i) {
			int in = ii.get(i);
			if (in == 0)
				return null;
			if (SETT.ROOMS().all().get(in-1) instanceof RoomBlueprintImp)
				return (RoomBlueprintImp) SETT.ROOMS().all().get(in-1);
			return null;
		}
		
		
		
	}
	
	public double getWorkPriority(Humanoid h) {
		return WORK_TIME.indu().isMax(h.indu()) ? 0 : 1;
	}
	
	public boolean shouldReportForWork(Humanoid h) {
		return WORK_TIME.indu().getD(h.indu()) < 0.4;
	}
	
	public int workforce() {
		int am = STATS.POP().pop(HTYPES.SUBJECT()) - incap.get(HTYPES.SUBJECT());
		am += STATS.POP().pop(HTYPES.SLAVE()) - incap.get(HTYPES.SLAVE());
		return am;
	}
	
	public int workforce(Race race, int daysBack) {
		return (STATS.POP().pop(race, HTYPES.SUBJECT(), daysBack) + STATS.POP().pop(race, HTYPES.SLAVE(), daysBack));
	}
	
	public int workforce(Race race) {
		int am = STATS.POP().pop(race, HTYPES.SUBJECT()) - incap.get(HTYPES.SUBJECT(), race);
		am += STATS.POP().pop(race, HTYPES.SLAVE()) - incap.get(HTYPES.SLAVE(), race);
		return am;
	}
	
	public int workforce(WGROUP g) {
		if (g == null)
			return workforce();
		return STATS.POP().pop(g.race, g.type)-incap.get(g.type, g.race);
	}
	
	public void fetchProximityStart(Humanoid a) {
		pStart(a, fetchProximity);
	}
	
	public void fetchProximityEnd(Humanoid a) {
		int v = pEnd(a, fetchProximity);
		if (v != 0) {
			RoomInstance work = EMPLOYED.get(a);
			work.employees().reportFetchSeconds(v);
		}
	}
	
	public void proximityStart(Humanoid a) {
		pStart(a, proximity);
	}
	
	public void proximityEnd(Humanoid a) {
		
		int v = pEnd(a, proximity);
		if (v != 0) {
			RoomInstance work = EMPLOYED.get(a);
			work.employees().reportWalkSeconds(v);
		}
	}
	
	public void slackStart(Humanoid a) {
		pStart(a, slack);
	}
	
	public void slackEnd(Humanoid a, boolean slacking) {
		int v = pEnd(a, slack);
		if (v != 0) {
			RoomInstance work = EMPLOYED.get(a);
			work.employees().reportWorkSuccess(v, !slacking);
		}
	}
	
	private static void pStart(Humanoid a, INT_OE<Induvidual> proximity) {
		int v = ((int) TIME.currentSecond())&0b0011_1111_1111_1111;
		v |= 0b0100_0000_0000_0000;
		proximity.set(a.indu(), v);
	}
	
	private int pEnd(Humanoid a, INT_OE<Induvidual> proximity) {
		int v = proximity.get(a.indu());
		proximity.set(a.indu(), 0);
		
		if ((v & 0b0100_0000_0000_0000) != 0) {
			RoomInstance work = EMPLOYED.get(a);
			if (work != null) {
				int now = ((int) TIME.currentSecond())&0b0011_1111_1111_1111;
				v &= 0b0011_1111_1111_1111;
				if (v < now) {
					v = now-v;
					return v;
				}
				else if (v > now) {
					v = 0b0011_1111_1111_1111-v;
					v += now;
					return v;
				}

			}
		}
		return 0;
	}
	
	public final static class Incapacitated {
		
		private final DataO<Induvidual>.DataAbs data;
		private final int[] htypes = new int[HTYPES.ALL().size()];
		private final int[] etypes = new int[HTYPES.ALL().size()*RACES.all().size()];
		public final STAT stat;
		
		private Incapacitated(StatsInit init) {
			
			data = init.count.new DataBit("WORK_INCA") {
				@Override
				public void set(Induvidual t, int s) {
					if (t.added()) {
						htypes[t.hType().index()] -= get(t);
						etypes[t.hType().index()*RACES.all().size() + t.race().index] -= get(t);
					}
					super.set(t, s);
					if (t.added()) {
						htypes[t.hType().index()] += get(t);
						etypes[t.hType().index()*RACES.all().size() + t.race().index] += get(t);
					}
				}
			};
			
			init.addable.add(new Addable() {
				
				@Override
				public void removePrivate(Induvidual t) {
					htypes[t.hType().index()] -= data.get(t);
					etypes[t.hType().index()*RACES.all().size() + t.race().index] -= data.get(t);
				}
				
				@Override
				public void addPrivate(Induvidual t) {
					htypes[t.hType().index()] += data.get(t);
					etypes[t.hType().index()*RACES.all().size() + t.race().index] += data.get(t);
				}
			});
			
			init.savers.put("work_incap", new SAVABLE() {
				
				@Override
				public void save(FilePutter file) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void load(FileGetter file) throws IOException {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void clear() {
					Arrays.fill(htypes, 0);
				}
			});
			
			stat = new STATData("INCAPACITATED", init, data);
			
		}
		
		public int get(HTYPE t) {
			return htypes[t.index()];
		}
		
		public int get() {
			return get(HTYPES.SUBJECT()) + get(HTYPES.SLAVE());
		}

		public int get(HTYPE t, Race race) {
			if (race == null)
				return get(t);
			return etypes[t.index()*RACES.all().size() + race.index];
		}
		
	}
	
	public final static class Retirement {
		
		public final STAT RETIREMENT_AGE;
		public final STAT RETIREMENT_HOME;
		private final STAT RETIREMENT_HOME_ACCESS;
		private final STAT RETIREMENT_HOME_QUALITY;
		private final STAT RETIREMENT_HOME_TYPE;
		
		Retirement(StatsInit init){
			final int DD = 1024*16;
			final double DEFAULT = 0.25;
			
			RMapIntTwo<HCLASS, Race> count = new RMapIntTwo<>(HCLASSES.MAP(), RACES.map(), -DD, DD);
			StatDecree dec = new StatDecree("RETIREMENT_AGE_DEC", init, 0, 100, 1, ¤¤retTarget, (int) (100*DEFAULT));
			
			init.savers.put("WORK_RETIREMENT_AGE_COUNT", count);
			RETIREMENT_AGE = new STATFacade("RETIREMENT_AGE", init) {
				
				@Override
				protected double getDD(HCLASS s, Race r, int daysBack) {
					if (s == null) {
						s = HCLASSES.CITIZEN();
					}
					if (r == null) {
						double tot = 0;
						double tt = 0;
						double pop = 0;
						for (Race ra : RACES.all()) {
							double p = STATS.POP().POP.data(s).get(ra);
							tt += count.get(s).getD(ra);
							tot += count.get(s).getD(ra)*p;
							pop += p;
						}
						if (pop == 0)
							return  tt/RACES.all().size();
						return DEFAULT+ tot / pop;
					}
					return DEFAULT + count.get(s).getD(r);
				}
				
				@Override
				public void hover(GUI_BOX text, HCLASS cl, Race type) {
					StatHoverer.hover(text, this);
					GBox b = (GBox) text;
					b.sep();
					
					b.textLL(¤¤lim);
					b.add(GFORMAT.perc(b.text(), decree().getD(cl, type)));
					b.NL();
					
					b.textLL(¤¤estimated);
					
					b.add(GFORMAT.i(b.text(), estimate(cl, type, decree().getD(cl, type))));
					b.NL();
					
					
					b.sep();
					StatHoverer.hover(text, this, cl, type);
				}
				
				@Override
				public void hover(GUI_BOX text, Induvidual indu) {
					hover(text, indu.clas(), indu.race());
				}
				
			};
			RETIREMENT_AGE.info().icon = UI.icons().m.time;
			init.upers.add(new StatUpdatable() {
				
				int ri = 0;
				@Override
				public void update(double ds) {
					ds *= RACES.all().size()*DD;
					ds /= TIME.secondsPerDay()*4.0;
					if (ri >= RACES.all().size())
						ri = 0;
					
					int ids = (int) ds;
					if (RND.rFloat() < ds-ids) {
						ids++;
					}
					
					Race r = RACES.all().get(ri);
					ri++;
					
					for (HCLASS cl : HCLASSES.ALLP()) {
						double dd = dec.getD(cl, r);
						int current = count.get(cl).get(r);
						
						if (dd < DEFAULT) {
							dd = -(DEFAULT-dd);
						}else {
							dd = (dd-DEFAULT);
						}
						int target = (int)(DD*dd);
						if (current < target) {
							current += ids;
							if (current > target)
								current = target;
						}else if (current > target) {
							current -= ids;
							if (current < target)
								current = target;
						}
						count.get(cl).set(r, current);
					}
					
				}
			});
			
			RETIREMENT_AGE.standing = new StatStanding(RETIREMENT_AGE, DEFAULT);
			RETIREMENT_AGE.addDecree(dec);
			
			
			
			RETIREMENT_HOME = new STATFacade("RETIREMENT", init) {
				
				@Override
				protected double getDD(HCLASS cl, Race r, int daysBack) {
					

					double am = STATS.POP().pop(r, HTYPES.RETIREE(), 0);
					double dRet = 5.0*am/STATS.POP().POP.data().get(r);
					dRet = CLAMP.d(dRet, 0, 1);
					
					
					
					double access = RETIREMENT_HOME_ACCESS.data(cl).getD(r, 0);
					double quality = RETIREMENT_HOME_QUALITY.data(cl).getD(r, 0);
					double type = RETIREMENT_HOME_TYPE.data(cl).getD(r, 0);
					
					double res = dRet*access*(0.5+quality*0.5*type);
					
					return 0.2*dRet + 0.8*res;
					
				}
				
				@Override
				public int pdivider(HCLASS c, Race r, int daysback) {
					return 1;
				}
				
				@Override
				public void hover(GUI_BOX text, HCLASS cl, Race r) {
					StatHoverer.hover(text, this);
					
					GBox b = (GBox) text;
					b.sep();
					b.NL();
					double am = STATS.POP().pop(r, HTYPES.RETIREE(), 0);
					double dRet = Math.sqrt(5.0*am/STATS.POP().POP.data().get(r));
					dRet = CLAMP.d(dRet, 0, 1);
					
					b.textL(HTYPES.RETIREE().names);
					b.tab(6);
					b.add(GFORMAT.i(b.text(), (int)am));
					b.add(UI.icons().s.arrow_right);
					b.add(GFORMAT.i(b.text(), STATS.POP().POP.data().get(r)/5));
					b.NL();
					
					double access = RETIREMENT_HOME_ACCESS.data(cl).getD(r, 0);
					double quality = RETIREMENT_HOME_QUALITY.data(cl).getD(r, 0);
					double type = RETIREMENT_HOME_TYPE.data(cl).getD(r, 0);
					
					b.textL(RETIREMENT_HOME_ACCESS.info().name);
					b.tab(6);
					b.add(GFORMAT.perc(b.text(), access));
					b.NL();
					
					b.textL(RETIREMENT_HOME_QUALITY.info().name);
					b.tab(6);
					b.add(GFORMAT.perc(b.text(), quality));
					b.NL();
					
					b.textL(RETIREMENT_HOME_TYPE.info().name);
					b.tab(6);
					b.add(GFORMAT.perc(b.text(), type));
					b.NL();
					
					b.sep();
					StatHoverer.hover(text, this, cl, r);
					
				}
			};
			RETIREMENT_HOME.info().icon = SETT.ROOMS().RESTHOMES.get(0).icon.medium;
			
			RETIREMENT_HOME_ACCESS = new StatRet(init, init.count. new DataBit("WORK_RA"), 
					Dic.¤¤Access, ¤¤accessD);
			RETIREMENT_HOME_QUALITY = new StatRet(init, init.count. new DataNibble("WORK_RQ"), 
					Dic.¤¤Quality, ¤¤qualityD);
			RETIREMENT_HOME_TYPE = new StatRet(init, init.count. new DataNibble("WORK_RT"), 
					¤¤Type, ¤¤TypeD);
		}
		
		public boolean shoudRetire(Induvidual i) {
			int oa = STATS.POP().age.lifespan(i);
			double oldAge = oa * (0.4 + 0.6*(1.0-RETIREMENT_AGE.decree().getD(i.clas(), i.race())));
			return STATS.POP().age.DAYS.get(i) >= oldAge;
		}
		
		public int estimate(HCLASS cl, Race race, double dec) {
			double fromD = (1.0 - 0.6* dec);
			fromD*= STATS.POP().demography().historyRecords()-1;
			double am = 0;
			am += (1.0-(fromD-(int)fromD))*STATS.POP().demography().getD(race, (int)fromD);
			int fi = (int) fromD;
			fi ++;
			for (; fi < STATS.POP().demography().historyRecords(); fi++) {
				am += STATS.POP().demography().getD(race, fi);
			}
			return (int)Math.ceil(am);
		}
		
	}
	
}
