package settlement.stats.colls;

import game.boosting.BOOSTING;
import game.boosting.BoostSpec;
import game.boosting.Booster;
import game.boosting.BoosterValue;
import init.race.Race;
import init.sprite.UI.UI;
import init.type.HCLASS;
import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.Humanoid;
import settlement.environment.SettEnvMap.SettEnv;
import settlement.environment.SettEnvShape.Type;
import settlement.main.SETT;
import settlement.room.infra.monument.ROOM_MONUMENT;
import settlement.room.main.RoomBlueprint;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.StatsInit;
import settlement.stats.StatsInit.StatUpdatableI;
import settlement.stats.standing.StatStanding;
import settlement.stats.stat.STAT;
import settlement.stats.stat.STATData;
import settlement.stats.stat.STATImp;
import settlement.stats.stat.StatCollection;
import settlement.stats.stat.StatInfo;
import settlement.stats.util.StatBooster;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.ACTION;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import util.data.INT_O.INT_OE;
import util.gui.misc.GBox;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;

public class StatsAccess extends StatCollection {

	private static CharSequence ¤¤name = "Other";
	private static CharSequence ¤¤desc = "Access to different environments.";
	
	private static CharSequence ¤¤nameA = "Decorations";
	private static CharSequence ¤¤descA = "Access to different Decoration.";
	
	public final StatsMonuments MONUMENTS;
	
	public final StatsA ACCESS;
	private static CharSequence ¤¤MonumentsD = "The value is based on access, upgrade and general decoration degrade in your city. The access can be higher than 100%, in which case it will compensate in places where there is no access.";
	private static CharSequence ¤¤MonumentsDeg = "Degrade";
	
	
	private static CharSequence ¤¤Upgrade = "Upgrade";
	private static CharSequence ¤¤UpgradeDesc = "The access to upgraded variants of this decoration.";
	
	static {
		D.ts(StatsAccess.class);
	}

	public StatsAccess(StatsInit init) {
		super(init, "OTHER", ¤¤name, ¤¤desc);
		ACCESS = new StatsA(init);
		init.coll = this;
		
		MONUMENTS = new StatsMonuments(init);
		
		init.updatable.add(new Updater());
		
		
	}
	
	private final class Updater implements StatUpdatableI {
		
		private final ArrayList<SettEnv> alle = new ArrayList<>(SETT.ENV().map.all());
		{
			alle.remove(SETT.ENV().map.NOISE);
		}

		@Override
		public void update16(Humanoid h, int updateI, boolean day, int ui) {

			Induvidual i = h.indu();
			
			if (!HPoll.Handler.works(h) || STATS.WORK().EMPLOYED.get(h) == null
					|| !STATS.WORK().EMPLOYED.get(h).constructor().envValue(SETT.ENV().map.NOISE)) {
				SettEnv e = SETT.ENV().map.NOISE;
				accessCheck2(h, ACCESS.all().get(e.index()).indu(), e.get(h.physics.tileC()) * 16, e.declineSpeed);
			}

			RoomBlueprint room = SETT.ROOMS().map.blueprint.get(h.physics.tileC());
			if (room == null || room.registersEnvironment()) {
				boolean deg = false;
				for (StatMonument m : MONUMENTS.ALL) {
					int a = m.m.mapData.get(h.physics.tileC());
					int c = m.amount.indu().get(i);
					if (a > 0) {
						m.access.indu().set(i, 1);
						deg |= SETT.ENV().map.MONUMENT.DEGRADE.is(h.tc());
						int up = m.m.mapUpgrade.get(h.physics.tileC());
						m.upgrade.indu().set(h.indu(), up);
					}
					
					if (a > c) {
						m.amount.indu().inc(h.indu(), 1);
					}else if (a < c) {
						if (m.amount.indu().get(i) == 0) {
							m.access.indu().set(i, 0);
							m.upgrade.indu().set(h.indu(), 0);
						}else
							m.amount.indu().inc(h.indu(), -1);
					}
				}
				MONUMENTS.degrade.indu().set(i, deg ? 1 : 0);
				
				
				for (SettEnv e : alle) {
					accessCheck(h, ACCESS.all().get(e.index()).indu(), e.get(h.physics.tileC()) * 16, e.declineSpeed);
				}
				
		
				for (Env ee : ACCESS.envs) {
					if (ee.t.is(h.physics.tileC())) {
						ee.stat.indu().inc(h.indu(), 1);
						for (Env e : ACCESS.envs) {
							if (e == ee)
								continue;
							e.stat.indu().inc(h.indu(), -1);	
						}
						break;
					}
				}
			}
		}

		private void accessCheck(Humanoid h, INT_OE<Induvidual> data, double value, double deg) {
			Induvidual i = h.indu();
			int v = (int) Math.ceil(value);
			if (v > data.get(i) * 2)
				data.inc(i, 2);
			if (v > data.get(i))
				data.inc(i, 1);
			else if (v < data.get(i) && RND.oneIn(8 * (1 / deg))) {
				data.inc(i, -1);
			}
		}
		
		private void accessCheck2(Humanoid h, INT_OE<Induvidual> data, double value, double deg) {
			Induvidual i = h.indu();
			int v = (int) Math.ceil(value);
			if (v > data.get(i))
				data.inc(i, 1);
			if (v < data.get(i))
				data.inc(i, -1);
		}

		
	}



	public static class StatsA extends StatCollection {
		

		private final STATImp WATER;
		private final ArrayListGrower<Env> envs = new ArrayListGrower<>();
		
		private StatsA(StatsInit init) {
			super(init, "ACCESS", ¤¤nameA, ¤¤descA);
			for (SettEnv e : SETT.ENV().map.all()) {

				StatInfo info = new StatInfo(e.info.name,  e.info.names, e.info.desc);
				info.setOpinion(e.op);
				info.icon = e.icon;
				String dkey = key + "_" + e.key;
				STATData d = new STATData(e.key, init, init.count.new DataNibble(dkey), info);
				init.onArrivalStats.add(d);
				d.standing = new StatStanding(d, 0, e.standing);
				
				ACTION ac = new ACTION() {
					
					@Override
					public void exe() {
						for (BoostSpec sp : e.bonuses.all()) {
							
							StatBooster vv = StatBooster.make(d);
							Booster bo = new BoosterValue(vv, sp.booster.info, sp.booster.to(), sp.booster.isMul);
							d.boosters.push(bo, sp.boostable);
							
						}
					}
				};
				BOOSTING.connecter(ac);
			}
			

			
			for (Type t : SETT.ENV().map.SHAPE.all) {
				envs.add(new Env(init, t));
			}
			
			INT_OE<Induvidual> ii = new INT_OE<Induvidual>() {
				
				@Override
				public int min(Induvidual t) {
					return 0;
				}
				
				@Override
				public int max(Induvidual t) {
					return 16;
				}
				
				@Override
				public int get(Induvidual t) {
					int a = SETT.ENV().map.WATER_SALT.stat().indu().get(t) + SETT.ENV().map.WATER_SWEET.stat().indu().get(t);
					return CLAMP.i(a, 0, 16);
				}
				
				@Override
				public void set(Induvidual t, int i) {
					
					
				}
			};
			
			WATER = new STATImp(key + "_WATER", key + "_WATER", init, null, ii) {
				
				@Override
				protected int getDD(HCLASS s, Race r) {
					double d = SETT.ENV().map.WATER_SALT.stat().data(s).getD(r) + SETT.ENV().map.WATER_SWEET.stat().data(s).getD(r);
					d = CLAMP.d(d, 0, 1);
					return (int) (d*pdivider(s, r, 0));
				}
			};
			WATER.info().icon = UI.icons().m.water;
		}


	}
	
	public static final class StatsMonuments extends StatCollection {
		
		private final STAT degrade; 
		private final ArrayList<StatMonument> ALL = new ArrayList<>(SETT.ROOMS().MONUMENTS.all.size());
		
		private StatsMonuments(StatsInit init) {
			super(init, "MONUMENTS", ¤¤nameA, ¤¤MonumentsD);
			degrade = new STATData(null, init, init.count.new DataCrumb("MON_DEGRADE"), null);
			ROOM_MONUMENT [] rr = new ROOM_MONUMENT[SETT.ROOMS().MONUMENTS.all.size()];
			
			for (ROOM_MONUMENT m : SETT.ROOMS().MONUMENTS.all) {
				rr[m.monumentIndex] = m;
			}
			for (ROOM_MONUMENT m : rr) {
				ALL.add(new StatMonument(m, init, degrade));
			}
		}
		
		
		
		public LIST<StatMonument> ALL(){
			return ALL;
		}
		
	}
	
	public static class StatMonument extends STATImp{
		
		public ROOM_MONUMENT m;
		public final STAT statUpgrade;
		public final STAT upgrade;
		public final STAT amount;
		public final STAT access;
		private final STAT degrade;
		
		StatMonument(ROOM_MONUMENT m, StatsInit init, STAT degrade){
			super(m.key, m.key+"_D", init, new StatInfo(m.info.name, m.info.names, ¤¤MonumentsD));
			access = new STATData(null, init, init.count.new DataBit("ACCESS_" + m.key));
			amount = new STATData(null, init, init.count.new DataNibble("AMOUNT_" + m.key));
			upgrade = new STATData(null, init, init.count.new DataCrumb(m.key+"dupgrade"), null);
			this.degrade = degrade;
			this.m = m;
			info.icon = m.icon;
			info.setOpinion(m.opinion);
			standing = new StatStanding(this, 0, m.defaultStanding);
			info().setMatters(true, false);

			
			if (m.upgrades().max() > 0) {
				statUpgrade = new STATImp(m.key + "_UPGRADED", m.key + "_UPGRADEDD", init, new StatInfo(m.info.name + " (" + ¤¤Upgrade + ")", ¤¤UpgradeDesc)) {
					
					@Override
					protected int getDD(HCLASS s, Race r) {
						double acc = access.data(s).get(r);
						if (acc == 0)
							return 0;
						
						return (int) ((1.0-degrade.data(s).getD(r))*upgrade.data(s).get(r)*dataDivider()*pdivider(s, r, 0)/acc);
					}
				};
				
				statUpgrade.info().icon = m.icon.twin(UI.icons().s.chevron(DIR.N).createColored(COLOR.ORANGE100), DIR.NW, 2);
				statUpgrade.standing = new StatStanding(statUpgrade, 0, m.defaultStandingUp);
			}else {
				statUpgrade = null;
			}
			
		}

		@Override
		protected int getDD(HCLASS s, Race r) {
			
			double acc = access.data(s).get(r);
			if (acc == 0)
				return 0;
			
			
			double d = amount.data(s).get(r)/(acc*m.maxEnv());
			d = CLAMP.d(d, 0, 1);
			
			
			
			d *= 1.0 - degrade.data(s).getD(r);
			return (int) (d*dataDivider()*pdivider(s, r, 0));
		}
		
		@Override
		public int indu(Induvidual t) {
			double a = (double)amount.indu().get(t)/m.maxEnv();
			a = CLAMP.d(a, 0, 1);
			a *= 1-degrade.indu().get(t);
			return (int) (a*dataDivider());
		}
		
		@Override
		public int dataDivider() {
			return 128;
		}

		@Override
		public void hover(GUI_BOX text, HCLASS cl, Race type) {
			GBox b = (GBox) text;
			b.text(m.info.desc);
			b.sep();
			double acc = access.data(cl).get(type);
			
			b.textL(Dic.¤¤Access);
			b.tab(6);
			double d = 0;
			if (acc > 0)
				d = amount.data(cl).get(type)/(acc*m.maxEnv());
			b.add(GFORMAT.perc(b.text(), d));
			b.NL();
			
			b.textL(¤¤MonumentsDeg);
			b.tab(6);
			d = 0;
			if (acc > 0)
				d = degrade.data(cl).get(type)/(acc);
			b.add(GFORMAT.percInv(b.text(), d));
			b.NL();
			
			
			super.hover(text, cl, type);
		}
		
		@Override
		public void hover(GUI_BOX text, Induvidual indu) {
			
			GBox b = (GBox) text;
			b.text(m.info.desc);
			b.sep();
			
			b.textL(Dic.¤¤Access);
			b.tab(7);
			double d = (double) (amount.indu().get(indu))/m.maxEnv();
			b.add(GFORMAT.perc(b.text(), d));
			b.NL();
			
			b.textL(¤¤MonumentsDeg);
			b.tab(7);
			b.add(GFORMAT.perc(b.text(), degrade.indu().getD(indu)));
			b.NL();

			if (m.upgrades().max() > 0) {
				b.textL(Dic.¤¤Upgrade);
				b.tab(6);
				b.add(GFORMAT.perc(b.text(), (double)upgrade.indu().get(indu)/m.upgrades().max()));
				b.NL(4);
			}
			
			super.hover(text, indu);
		}


		
	}
	
	private static class Env {
		
		private final Type t;
		public final STAT stat;
		
		Env(StatsInit init, Type t) {
			this.t = t;
			String key = "SHAPE_" + t.key;
			
			STATData d = new STATData(key, init, init.count.new DataCrumb("D_" + key), null);
			d.info().icon = t.icon;
			init.onArrivalStats.add(d);

			this.stat = d;
		}
	}

}
