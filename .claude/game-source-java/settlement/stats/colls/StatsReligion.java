package settlement.stats.colls;

import game.GAME;
import game.battle.div.Div;
import game.boosting.BOOSTABLE_O;
import game.boosting.BOOSTING;
import game.boosting.BSourceInfo;
import game.boosting.BValue;
import game.boosting.BoostSpec;
import game.boosting.Boostable;
import game.boosting.BoostableCat;
import game.boosting.Booster;
import game.boosting.BoosterImp;
import game.faction.npc.FactionNPC;
import game.faction.player.Player;
import init.race.Race;
import init.religion.Religion;
import init.religion.RELIGIONS;
import init.sprite.UI.UI;
import init.type.HCLASS;
import init.type.HCLASSES;
import init.type.NEED;
import init.type.NEEDS;
import init.type.POP_CL;
import settlement.entity.EntityIterator;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.room.service.module.ROOM_SERVICER;
import settlement.room.spirit.shrine.ROOM_SHRINE;
import settlement.room.spirit.temple.ROOM_TEMPLE;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import settlement.stats.StatsInit.Addable;
import settlement.stats.StatsInit.StatInitable;
import settlement.stats.StatsInit.StatUpdatableI;
import settlement.stats.service.StatService;
import settlement.stats.stat.SETT_STATISTICS.SettStatistics;
import settlement.stats.stat.STAT;
import settlement.stats.stat.STATData;
import settlement.stats.stat.STATFacade;
import settlement.stats.stat.STATImp;
import settlement.stats.stat.StatCollection;
import settlement.stats.stat.StatInfo;
import settlement.stats.util.StatHoverer;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.ACTION.ACTION_O;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.data.GETTER_TRANS.GETTER_TRANSE;
import util.data.INT_O.INT_OE;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.info.INFO;
import util.race.PERMISSION.Permission;
import util.text.D;
import util.text.Dic;
import view.sett.IDebugPanelSett;
import world.map.regions.Region;

public class StatsReligion extends StatCollection {

	private static CharSequence ¤¤religion = "Religion";
	private static CharSequence ¤¤religionD = "Affiliated religion.";
	
	private static CharSequence ¤¤followers = "Followers";
	private static CharSequence ¤¤followersD = "The amount of followers for this religion.";
	
	private static CharSequence ¤¤access = "Access";
	private static CharSequence ¤¤accessD = "The Access of:";
	
	private static CharSequence ¤¤value = "Quality";
	private static CharSequence ¤¤valueD = "The quality of:";
	
	
	private static CharSequence ¤¤Temple = "Temple";
	private static CharSequence ¤¤Shrine = "Shrine";
	
	
	private static CharSequence ¤¤name = "Religion";
	private static CharSequence ¤¤desc = "Stats related to religion and afterlife.";
	
	static {
		D.ts(StatsReligion.class);
	}
	
	private final ArrayList<StatReligion> religions;
	public final LIST<StatReligion> ALL;
	public final GETTER_TRANSE<Induvidual, StatReligion> getter;
	
	public final ReligionTot TEMPLE;
	public final ReligionTot SHRINE;
	
	public final STAT OPPOSITION;
	
	public StatsReligion(StatsInit init){
		
		super(init, "RELIGION", ¤¤name, ¤¤desc);
		
		religions = new ArrayList<>(RELIGIONS.ALL().size());
		
		TEMPLE = new ReligionTot(init, "TEMPLE", ¤¤Temple, UI.icons().s.temple, NEEDS.TYPES().TEMPLE);
		SHRINE = new ReligionTot(init, "SHRINE", ¤¤Shrine, UI.icons().s.shrine, NEEDS.TYPES().SHRINE);

		for (Religion t : RELIGIONS.ALL()) {
			religions.add(new StatReligion(t, init));
			IDebugPanelSett.add("Convert to: " + t.key, new ACTION() {
				final Religion kk = t;
				@Override
				public void exe() {
					new EntityIterator.Humans() {
						
						@Override
						protected boolean processAndShouldBreakH(Humanoid h, int ie) {
							getter.set(h.indu(), religions.get(kk.index()));
							return false;
						}
					}.iterate();
				}
			});
		}
		
		ALL = religions;
		
		getter = new Getter(init);
		
		OPPOSITION = new STATImp("RELIGION_OPPOSITION", init) {
			
			@Override
			protected int getDD(HCLASS s, Race r) {
				return (int) (opposition()*pdivider(s, r, 0));
			}
			
			
			@Override
			public void hover(GUI_BOX text, HCLASS cl, Race type) {
				GBox b = (GBox) text;
				StatHoverer.hover(text, this);
				b.sep();
				for (int x = 0; x < ALL.size(); x++) {
					b.tab(1+x*2);
					b.add(ALL.get(x).religion.icon.small);
					
				}
				b.NL(4);
				for (int y = 0; y < ALL.size(); y++) {
					StatReligion r = ALL.get(y);
					b.add(r.religion.icon.small);
					for (int x = 0; x < ALL.size(); x++) {
						b.tab(1+x*2);
						if (r.opposition(ALL.get(x)) > 0)
							b.add(GFORMAT.f(b.text(), r.opposition(ALL.get(x))).errorify());
						else
							b.add(GFORMAT.f(b.text(), r.opposition(ALL.get(x))).normalify());
					}
					b.NL(2);
					
				}
				b.sep();
				
				StatHoverer.hover(text, this, cl, type);
			}
			
		};
		OPPOSITION.info().setMatters(true, false);
		
		
		init.updatable.add(new StatUpdatableI() {
			
			@Override
			public void update16(Humanoid h, int updateR, boolean day, int updateI) {
				if (day && RND.oneIn(8)) {
					StatReligion curr = getter.get(h.indu());
					double current = curr.religion.conversionCity.get(h.indu());
					current *= current;
					double tot = 0;
					
					for (StatReligion r : ALL) {
						
						double oo = r.religion.conversionCity.get(POP_CL.clP());
						oo *= oo;
						if (r == curr)
							oo = current*4;
						else if (oo < current)
							oo*=0.1;
						tot += oo;
					}
					
					tot *= RND.rFloat();
					
					for (StatReligion r : ALL) {
						double oo = r.religion.conversionCity.get(POP_CL.clP());
						oo *= oo;
						if (r == curr)
							oo = current*4;
						else if (oo < current)
							oo*=0.1;
						tot -= oo;
						if (tot <= 0) {
							getter.set(h.indu(), r);
							return;
						}
					}
					
				}
				
			}
			
		});
		
		BOOSTING.connecter(new ACTION() {
			
			@Override
			public void exe() {
				
				KeyMap<BB> map = new KeyMap<>();
				
				for (int ri = 0; ri < RELIGIONS.ALL().size(); ri++) {
					Religion r = RELIGIONS.ALL().get(ri);
					for (int bi = 0; bi < r.boosts.all().size(); bi++) {
						BoostSpec s = r.boosts.all().get(bi);
						String k = s.identifier();
						if ((s.boostable.cat.typeMask & BoostableCat.TYPE_SETT) != 0 && !map.containsKey(k)) {
							BB b = new BB(s.boostable, s.booster.isMul);
							TEMPLE.TOTAL.boosters.push(b, s.boostable);
							map.put(k, b);
						}
					}
					
				}
				
				
			}
		});

		for (Religion r : RELIGIONS.ALL()) {
			conBoost(r);
		}
		
		init.onArrivalActions.add(new ACTION_O<Induvidual>() {

			@Override
			public void exe(Induvidual t) {
				set(t, TEMPLE);
				set(t, SHRINE);
				
			}
			
			private void set(Induvidual t, ReligionTot tt) {
				StatReligion r = getter.get(t);
				double d = tt.access(r.religion).data(t.clas()).getD(t.race());
				d = d*tt.ACCESS.indu().max(t);
				int v = (int) d;
				if (d-v > RND.rFloat())
					v++;
				int a = v;
				if (a > tt.ACCESS.indu().max(t))
					a = tt.ACCESS.indu().max(t);
				
				double q = tt.quality(r.religion).data(t.clas()).getD(t.race());
				tt.ACCESS.indu().set(t, a);
				tt.QUALITY.indu().setD(t, a > 0 ? q : 0);
			}
			
		});
	}
	
	public void setChildReligion(Humanoid h) {
		
		Race r = h.race();
		double f = RND.rFloat();
		for (StatReligion rel : ALL) {
			f -= rel.followers.data(h.indu().clas()).getD(r);
			if (f <= 0) {
				getter.set(h.indu(), rel);
				return;
			}
		}
	}
	
	private double opCache = 0;
	private int updateI = -1;
	
	private double opposition() {
		if (updateI == GAME.updateI())
			return opCache;
		
		double pop = STATS.POP().POP.data().get(null);
		if (pop == 0)
			return 1;
		double v = 0;
		for (int ri = 0; ri < religions.size(); ri++) {
			double vv = 0;
			StatReligion r = religions.get(ri);
			for (int ri2 = 0; ri2 < religions.size(); ri2++) {
				StatReligion r2 = religions.get(ri2);
				double am = r2.followers.data().get(null)/pop;
				am *= r.opposition(r2);
				vv += am;
			}
			v += vv*r.followers.data().get(null);
		}
		v /= pop;
		opCache = CLAMP.d(v, 0, 1);
		updateI = GAME.updateI();
		return opCache;
	}
	
	
	private final class Getter implements GETTER_TRANSE<Induvidual, StatReligion>, Addable{

		private final INT_OE<Induvidual> ii;
		private final INFO info = new INFO(¤¤religion, ¤¤religionD);
		
		Getter(StatsInit init){
			ii = init.count. new DataByte("REL_ID");
			init.addable.add(this);
			init.onConstruct.add(new StatInitable() {
				
				@Override
				public void init(Induvidual h) {

					double d = 0;
					for (StatReligion re : ALL) {
						d += re.religion.conversionCity.get(h);
					}
					d *= RND.rFloat();
					for (StatReligion re : ALL) {
						d -= re.religion.conversionCity.get(h);
						if (d <= 0) {
							getter.set(h, re);
							return;
						}
					}
					getter.set(h, ALL.get(ALL.size()-1));
				}
			});
			
			init.copier.add(new INT_OE<Induvidual>() {

				@Override
				public int get(Induvidual t) {
					return ii.get(t);
				}

				@Override
				public int min(Induvidual t) {
					return 0;
				}

				@Override
				public int max(Induvidual t) {
					return Byte.MAX_VALUE;
				}

				@Override
				public void set(Induvidual t, int i) {
					Getter.this.set(t, religions.get(i));					
				}
				
			});
		}
		
		@Override
		public StatReligion get(Induvidual f) {
			return religions.get(ii.get(f));
		}

		@Override
		public void set(Induvidual f, StatReligion t) {
			if (ii.get(f) == t.index())
				return;
			TEMPLE.clearAccess(f);
			SHRINE.clearAccess(f);
			removeH(f);
			ii.set(f, t.index());
			addH(f);
		}
		
		@Override
		public INFO info() {
			return info;
		}

		@Override
		public void addPrivate(Induvidual i) {
			StatReligion r = get(i);
			r.followers.inc(i, 1);
			TEMPLE.access(r.religion).inc(i, TEMPLE.ACCESS.indu().get(i));
			TEMPLE.quality(r.religion).inc(i, TEMPLE.QUALITY.indu().get(i));
			SHRINE.access(r.religion).inc(i, SHRINE.ACCESS.indu().get(i));
			SHRINE.quality(r.religion).inc(i, SHRINE.QUALITY.indu().get(i));
			
		}

		@Override
		public void removePrivate(Induvidual i) {
			StatReligion r = get(i);
			r.followers.inc(i, -1);
			TEMPLE.access(r.religion).inc(i, -TEMPLE.ACCESS.indu().get(i));
			TEMPLE.quality(r.religion).inc(i, -TEMPLE.QUALITY.indu().get(i));
			SHRINE.access(r.religion).inc(i, -SHRINE.ACCESS.indu().get(i));
			SHRINE.quality(r.religion).inc(i, -SHRINE.QUALITY.indu().get(i));
		}
		
	}
	

	
	public final class StatReligion implements INDEXED{
		
		public final Religion religion;
		
		public final INFO info;
		public final SettStatistics followers;
		public final Permission permission = new Permission();
		
		
		StatReligion(Religion religion, StatsInit init){
			this.religion = religion;
			
			

			info = religion.info;
			permission.setDef(true);
			
			String k = "REL_" + religion + "_";
			followers = new SettStatistics(k + "F", init, ¤¤followers, ¤¤followersD);
			
			for (HCLASS s : HCLASSES.ALL())
				permission.set(s, null, true);
			init.savers.put(k+"PERM", permission);

			
		}
		
		public double opposition(StatReligion other) {
			return religion.opposition(other.religion);
		}

		@Override
		public int index() {
			return religion.index();
		}
		
	}
	

	
	private class BB extends Booster implements BValue{

		private final Booster[] vv = new Booster[RELIGIONS.ALL().size()];
		private final double min;
		private final double max;
		private final double aa;
		
		public BB(Boostable bb, boolean isMul) {
			super(new BSourceInfo(Dic.¤¤Religion, UI.icons().s.star), isMul);
			
			double min = Double.MAX_VALUE;
			double max = Double.MIN_VALUE;
			
//			if (isMul)
//				Arrays.fill(vv, 1.0);
//			
			aa = isMul ? 1 : 0;
			
			for (Religion r : RELIGIONS.ALL()) {
				for (BoostSpec s : r.boosts.all()) {
					
					if (s.boostable == bb && s.booster.isMul == isMul && (s.boostable.cat.typeMask & BoostableCat.TYPE_SETT) != 0) {
						vv[r.index()] = s.booster;
						
						min = Math.min(min, s.booster.from());
						max = Math.max(max, s.booster.to());
					}
				}
			}
			
			this.min = min;
			this.max = max;
			
		}

		
		@Override
		public double vGet(Region reg) {
			return 0;
		}

		@Override
		public double vGet(Induvidual indu) {
			return vv(STATS.RELIGION().getter.get(indu).religion, STATS.RELIGION().TEMPLE.TOTAL.indu().getD(indu));
		}

		private double vv(Religion rel, double v) {
			if (vv[rel.index()] == null)
				return aa;
			return vv[rel.index()].getValue(v);
		}
		
		@Override
		public double vGet(Div div) {
			double dd = 0;
			for (int ri = 0; ri < RELIGIONS.ALL().size(); ri++) {
				StatReligion rl = STATS.RELIGION().ALL.get(ri);
				double v = TEMPLE.access(rl.religion).div().getD(div)*TEMPLE.quality(rl.religion).div().getD(div);
				dd += vv(rl.religion, v)*rl.followers.div().getD(div);
				
			}
			return dd;
		}

		@Override
		public double vGet(PopTime popTime) {
			double dd = 0;
			for (int ri = 0; ri < RELIGIONS.ALL().size(); ri++) {
				StatReligion rl = STATS.RELIGION().ALL.get(ri);
				double v = TEMPLE.access(rl.religion).data(popTime.pop.cl).getD(popTime.pop.race, popTime.daysBack)*TEMPLE.quality(rl.religion).data(popTime.pop.cl).getD(popTime.pop.race, popTime.daysBack);
				dd += vv(rl.religion, v)*rl.followers.data(popTime.pop.cl).getD(popTime.pop.race, popTime.daysBack);
			}
			return dd;
		}

		@Override
		public double vGet(Player f) {
			return vGet(POP_CL.clP());
		}

		@Override
		public double vGet(FactionNPC f) {
			if (f.court().king() == null || f.court().king().roy() == null)
				return 0;
			return vv(STATS.RELIGION().getter.get(f.court().king().roy().induvidual).religion, 1.0);
		}

		@Override
		public double getValue(double input) {
			return input;
		}

		@Override
		protected double pget(BOOSTABLE_O o) {
			return o.boostableValue(this);
		}

		@Override
		public double from() {
			return min;
		}

		@Override
		public double to() {
			return max;
		}


		
		
	}

	public static final class ReligionTot extends StatService{
		
		public final STAT TOTAL;
		public final STAT ACCESS;
		public final STAT QUALITY;
		
		private final SettStatistics[] accesses = new SettStatistics[RELIGIONS.ALL().size()];
		private final SettStatistics[] qualities  = new SettStatistics[RELIGIONS.ALL().size()];
		
		ReligionTot(StatsInit init, String key, CharSequence name, SPRITE icon, NEED need){
			super(name, name, icon, need);
			INT_OE<Induvidual> indu = new INT_OE<Induvidual>() {

				@Override
				public int get(Induvidual t) {
					return (int) (ACCESS.indu().get(t)*QUALITY.indu().get(t)); 
				}

				@Override
				public int min(Induvidual t) {
					return 0;
				}

				@Override
				public int max(Induvidual t) {
					return QUALITY.indu().max(t);
				}

				@Override
				public void set(Induvidual t, int i) {
					
				}
			
			};
			
			TOTAL = new STATFacade(key, init, indu) {
				
				@Override
				protected double getDD(HCLASS s, Race r, int daysBack) {
					double a = ACCESS.data(s).getD(r, daysBack);
					double q = QUALITY.data(s).getD(r, daysBack);
					return a *(0.2 + 0.8*q);
				}
				
				@Override
				public void hover(GUI_BOX text, HCLASS cl, Race type) {
					StatHoverer.hover(text, this);
					GBox b = (GBox) text;
					b.sep();
					
					b.textL(ACCESS.info().name);
					b.tab(6);
					b.add(GFORMAT.perc(b.text(), ACCESS.data(cl).getD(type)));
					b.NL();
					b.textL(QUALITY.info().name);
					b.tab(6);
					b.add(GFORMAT.perc(b.text(), QUALITY.data(cl).getD(type)));
					b.sep();
					
					StatHoverer.hover(text, this, cl, type);
				}
				
				@Override
				public void hover(GUI_BOX text, Induvidual indu) {
					StatHoverer.hover(text, this);
					GBox b = (GBox) text;
					b.sep();
					
					b.textL(ACCESS.info().name);
					b.tab(6);
					b.add(GFORMAT.perc(b.text(), ACCESS.indu().getD(indu)));
					b.NL();
					b.textL(QUALITY.info().name);
					b.tab(6);
					b.add(GFORMAT.perc(b.text(), QUALITY.indu().getD(indu)));
					b.sep();
					
					StatHoverer.hover(text, this, indu);
				}
			};
			
			TOTAL.info().icon = icon;

			ACCESS = new STATData(null, init, init.count.new DataBit(key + "ACCESS") {
				@Override
				public void set(Induvidual t, int s) {
					
					StatReligion r = STATS.RELIGION().getter.get(t);
					accesses[r.index()].inc(t, -get(t));
					super.set(t, s);
					accesses[r.index()].inc(t, get(t));
					if (get(t) == 0) {
						QUALITY.indu().set(t, 0);
					}
					
					
				}
			}, new StatInfo(¤¤access, ¤¤access, ¤¤accessD + " " + name));
			
			QUALITY = new STATData(null,key + "_QUALITY", init, init.count.new DataNibble1(key + "_QUALITY") {
				@Override
				public void set(Induvidual t, int s) {
					if (ACCESS.indu().get(t) == 0)
						s = 0;
					StatReligion r = STATS.RELIGION().getter.get(t);
					qualities[r.index()].inc(t, -get(t));
					super.set(t, s);
					qualities[r.index()].inc(t, get(t));
				}
				
				
				
				
			}, new StatInfo(¤¤value, ¤¤value, ¤¤valueD + " " + name)) {
				
				@Override
				public int pdivider(HCLASS c, Race r, int daysback) {
					return ACCESS.data(c).get(r, daysback);
				}
				
				
			};
			
			for (Religion t : RELIGIONS.ALL()) {
				accesses[t.index()] = new SettStatistics(key + "_" + t.key + "_A", init, ¤¤access, ¤¤accessD + " " + name) {
					@Override
					protected int popDivider(HCLASS c, Race r, int daysback) {
						return STATS.RELIGION().ALL.get(t.index()).followers.data(c).get(r, daysback);
					}
				};
				qualities[t.index()] = new SettStatistics(key + "_" + t.key + "_Q", init, ¤¤value, ¤¤valueD + " " + name) {
					@Override
					protected int popDivider(HCLASS c, Race r, int daysback) {
						return accesses[t.index()].data(c).get(r, daysback);
					}
					
					@Override
					public int dataDivider() {
						return QUALITY.indu().max(null);
					}
				};
			}
			

		}
		
		public SettStatistics access(Religion r) {
			return accesses[r.index()];
		}
		
		public SettStatistics quality(Religion r) {
			return qualities[r.index()];
		}
		
		public void clearAccess(Humanoid h) {
			ACCESS.indu().set(h.indu(), 0);
			QUALITY.indu().set(h.indu(), 0);
		}
		
		@Override
		public void clearAccess(Induvidual h) {
			ACCESS.indu().set(h, 0);
			QUALITY.indu().set(h, 0);
		}
		
		public void setAccess(Humanoid h) {
			
			Room r = SETT.ROOMS().map.get(h.tc());
			if (r != null && r instanceof ROOM_SERVICER) {
				ROOM_SERVICER t = (ROOM_SERVICER) r;
				ACCESS.indu().set(h.indu(), 1);
				QUALITY.indu().setD(h.indu(), t.quality());
			}
		}

		@Override
		public boolean access(Humanoid h) {
			return ACCESS.indu().get(h.indu()) > 0;
		}

		@Override
		public STAT total() {
			return TOTAL;
		}

		@Override
		public CharSequence name(Induvidual i) {
			StatReligion r = STATS.RELIGION().getter.get(i);
			if (need == NEEDS.TYPES().TEMPLE) {
				if (SETT.ROOMS().TEMPLES.perRel.get(r.index()).size() > 0) {
					return SETT.ROOMS().TEMPLES.perRel.get(r.index()).get(0).info.name;
				}
			}else {
				if (SETT.ROOMS().TEMPLES.perRelShrine.get(r.index()).size() > 0) {
					return SETT.ROOMS().TEMPLES.perRelShrine.get(r.index()).get(0).info.name;
				}
			}
			return name;
		}
		
		@Override
		public SPRITE icon(Induvidual i) {
			return STATS.RELIGION().getter.get(i).religion.icon;
		}

		@Override
		public void cheatSetTotal(Induvidual i, double tot) {
			ACCESS.indu().set(i, tot>0 ? 1 :0);
			QUALITY.indu().setD(i, tot);
			
		}
		
		
		
	}
	
	private void conBoost(Religion rel) {
		

		for (ROOM_TEMPLE r : SETT.ROOMS().TEMPLES.perRel.get(rel.index())) {
			
			BSourceInfo in = new BSourceInfo(r.info.name, r.icon);
			new BoosterImp(in, 1.5, false) {

				
				@Override
				public double vGet(Induvidual indu) {
					if (getter.get(indu).religion == rel) {
						return TEMPLE.TOTAL.indu().get(indu);
					}
					return 0;
				}
				
				@Override
				public double vGet(PopTime popT) {
					if (r.service().total() > 0)
						return 1.0;
					return  0;
				}
			}.add(rel.conversionCity);
			
		}
		
		for (ROOM_SHRINE r : SETT.ROOMS().TEMPLES.perRelShrine.get(rel.index())) {
			
			BSourceInfo in = new BSourceInfo(r.info.name, r.icon);
			new BoosterImp(in, 1.25, false) {
				@Override
				public double vGet(Induvidual indu) {
					if (getter.get(indu).religion == rel) {
						return SHRINE.TOTAL.indu().get(indu);
					}
					return 0;
				}
				
				@Override
				public double vGet(PopTime popT) {
					if (r.service().total() > 0)
						return 1.0;
					return  0;
				}
			}.add(rel.conversionCity);
			
		}
	}

	
}
