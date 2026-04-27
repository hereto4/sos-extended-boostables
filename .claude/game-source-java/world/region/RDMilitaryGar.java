package world.region;

import static settlement.main.SETT.ROOMS;
import static settlement.main.SETT.TILE_BOUNDS;

import java.io.IOException;
import java.util.Iterator;

import game.GAME;
import game.battle.DivisionBanners.DivisionBanner;
import game.battle.div.Div;
import game.battle.util.DIV_SETTING;
import game.battle.util.DivGeneration;
import game.boosting.BOOSTABLES;
import game.faction.FACTIONS;
import game.faction.FResources.RTYPE;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import init.constant.Config;
import init.race.Race;
import init.resources.RESOURCES;
import init.type.CAUSE_LEAVES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.misc.util.RESOURCE_TILE;
import settlement.room.main.Room;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.KeyMap;
import snake2d.util.sets.LIST;
import util.text.Dic;
import world.army.AD;
import world.army.WDIV;
import world.entity.army.WArmy;
import world.map.regions.Region;
import world.region.pop.RDRace;

final class RDMilitaryGar {

	private final ArrayList<WDiv> divs = new ArrayList<WDiv>(128);
	private final ArrayList<WDIV> res = new ArrayList<>(128);
	private Region lReg = null;
	private int upI = -1;
	private final WDivsCapitol cap = new WDivsCapitol();
	
	public RDMilitaryGar() {
		while(divs.hasRoom())
			divs.add(new WDiv());
	}
	
	public LIST<WDIV> player(){
		cap.init();
		return cap;
	}
	
	void init() {
		//lReg = null;
	}
	
	public LIST<WDIV> divisions(Region r, int garrisonA, int garrisonT){
		
		if (FACTIONS.player().capitolRegion() == r) {
			cap.init();
			return cap;
		}
		
		if (lReg == r && GAME.updateI() == upI)
			return res;
		
		
		lReg = r;
		upI = GAME.updateI();
		
		res.clear();
		int soldiers = (int)Math.max(garrisonA, garrisonT);
		
		
		
		if (soldiers == 0)
			return res;
		
		double dv = (double)garrisonA/soldiers;
		dv = CLAMP.d(dv, 0, 1);
		
		int realSoldiers = garrisonA;
		int solRemaining = soldiers;
		
		double tot = RD.RACES().population.get(r)+1;
		int i = 0;
		int remain = 0;
		for (int ri = 0; ri < RD.RACES().all.size(); ri++) {
			RDRace ra = RD.RACES().all.get(ri);
			int sols = (int) Math.ceil(soldiers*ra.pop.get(r)/tot);
			if (sols > 50) {
				sols += remain;
			}
			if (sols > solRemaining)
				sols = solRemaining;
				
			while(sols > 50 && solRemaining > 0) {
				
				int m = CLAMP.i(sols, 0, Config.battle().MEN_PER_DIVISION);
				solRemaining -= m;
				sols -= m;
				WDiv d = divs.get(i);
				d.index = i++;
				d.f = r.faction() == null ? null : r.faction();
				int rs = (int) Math.ceil(m*dv);
				rs = CLAMP.i(rs, 0, realSoldiers);
				realSoldiers -= rs;
				d.men = rs;
				d.menTarget = m;
				d.race = ra.race;
				d.r = r;
				res.add(d);
			}
			remain += sols;
		}
		
		if (solRemaining > 0) {
			Race big = FACTIONS.player().race();
			int bb = 0;
			for (int ri = 0; ri < RD.RACES().all.size(); ri++) {
				RDRace ra = RD.RACES().all.get(ri);
				if (ra.pop.get(r) > bb) {
					big = ra.race;
					bb = ra.pop.get(r);
				}
			}
			while (solRemaining > 0) {
				int m = CLAMP.i(solRemaining, 0, Config.battle().MEN_PER_DIVISION);
				
				WDiv d = divs.get(i);
				d.index = i++;
				d.f = r.faction() == null ? null : r.faction();
				d.r = r;
				int rs = (int) Math.ceil(m*dv);
				rs = CLAMP.i(rs, 0, realSoldiers);
				realSoldiers -= rs;
				d.men = rs;
				d.menTarget = m;
				solRemaining -= m;
				d.race = big;
				res.add(d);
			}
		}
		
		return res;
	}
	private static COLOR colr = COLOR.ORANGE100.makeSaturated(0.5).shade(0.75);
	
	private static class WDiv implements WDIV {

		Race race;
		int men;
		int menTarget;
		int index;
		Faction f;
		Region r;
		
		@Override
		public int men() {
			return men;
		}

		@Override
		public Race race() {
			return race;
		}

		@Override
		public int menTarget() {
			return menTarget;
		}

		@Override
		public double experience() {
			return 0.1;
		}

		@Override
		public void resolve(Induvidual[] hs) {
			menSet(hs.length);
		}
		@Override
		public void resolve(int surviviors, double experiencePerMan) {
			menSet(surviviors);
		}
		
		void menSet(int amount) {
			RD.MILITARY().garrison.inc(r, -men);
			RD.MILITARY().garrison.inc(r, amount);
			this.men = amount;
		}

		@Override
		public int daysUntilMenArrives() {
			return 0;
		}

		@Override
		public CharSequence name() {
			return Dic.¤¤Garrison;
		}


		@Override
		public boolean needSupplies() {
			return false;
		}

		@Override
		public double training(StatTraining tr) {
			if (f != null && f.capitolRegion() == r && f instanceof FactionNPC ) {
				double d = 0.15*BOOSTABLES.NOBLE().AGRESSION.get(((FactionNPC)f).court().king().roy().induvidual);
				d = CLAMP.d(d, 0, 1);
				return d;
			}
			return 0.15;
		}


		@Override
		public DivisionBanner banner() {
			return GAME.ARMIES().banners.get(index);
		}

		@Override
		public void bannerSet(int bi) {
			
		}

		@Override
		public Faction faction() {
			return f;
		}
		
		@Override
		public DivGeneration generate() {
			return new DivGeneration(this, target);
		}

		@Override
		public double equip(EquipBattle e) {
			return (double) e.garrisonAmount()/e.equipMax;
		}

		@Override
		public int bannerI() {
			return index;
		}

		@Override
		public WArmy army() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public COLOR color() {
			return colr;
		}

		public final DIV_SETTING target = new DIV_SETTING() {

			@Override
			public double training(StatTraining tr) {
				return WDiv.this.training(tr);
			}

			@Override
			public double equip(EquipBattle e) {
				return WDiv.this.equip(e);
			}

			@Override
			public int men() {
				return menTarget;
			}
			
		};
		
		@Override
		public DIV_SETTING target() {
			return target;
		}
		
		
	}
	
	public void extractLostEquipment(int[] amounts) {
		upI = -1;
		for (COORDINATE c : TILE_BOUNDS) {
			Room r = ROOMS().STOCKPILE.get(c.x(), c.y());
			if (r == null)
				continue;
			RESOURCE_TILE cr = (RESOURCE_TILE) r.storage(c.x(), c.y());
			if (cr != null) {
				for (EquipBattle s : STATS.EQUIP().BATTLE_ALL()) {
					if (amounts[s.indexMilitary()] <= 0)
						continue;
					if (cr.resource() == s.resource()) {
						while (amounts[s.indexMilitary()] > 0 && cr.reservable() > 0) {
							cr.findableReserve();
							cr.resourcePickup();
							amounts[s.index()]--;
							FACTIONS.player().res().inc(cr.resource(), RTYPE.SPOILS, -1);
						}
					}
				}
			}
		}
		
		for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
			if (e instanceof Humanoid) {
				Humanoid a = (Humanoid) e;
				Div d = STATS.BATTLE().DIV.get(a);
				if (d != null && d.army() == GAME.ARMIES().player()) {
					for (EquipBattle s : STATS.EQUIP().BATTLE_ALL()) {
						if (amounts[s.indexMilitary()] <= 0)
							continue;
						if (s.stat().indu().get(a.indu()) > 0) {
							int am = CLAMP.i(s.stat().indu().get(a.indu()), 0, amounts[s.indexMilitary()]);
							s.stat().indu().inc(a.indu(), -am);
							amounts[s.indexMilitary()] -= am;
						}
					}

				}
			}

		}

	}
	
	final static class WDivsCapitol implements LIST<WDIV>, SAVABLE {

		private int updateTick = -1;
		private final ArrayList<WDivCity> list = new ArrayList<WDivCity>(Config.battle().DIVISIONS_PER_ARMY);
		private final WDivCity[] all = new WDivCity[Config.battle().DIVISIONS_PER_ARMY];
		private static COLOR cols = COLOR.BLUE100.makeSaturated(0.5).shade(0.75);
		
		@Override
		public void save(FilePutter file) {

		}

		@Override
		public void load(FileGetter file) throws IOException {
			updateTick = -1;
		}

		@Override
		public void clear() {
			updateTick = -1;
		}

		WDivsCapitol() {
			for (int di = 0; di < GAME.ARMIES().player().divisions().size(); di++) {
				all[di] = new WDivCity(GAME.ARMIES().player().divisions().get(di));
			}
			upI = -1;
		}

		private void init() {

			if (updateTick == GAME.updateI())
				return;

			updateTick = GAME.updateI();
			list.clear();

			for (int di = 0; di < GAME.ARMIES().player().divisions().size(); di++) {
				Div d = GAME.ARMIES().player().ordered().get(di);
				if ((AD.cityDivs().attachedArmy(d) == null && STATS.BATTLE().DIV.stat().div().get(d) > 0)) {
					list.add(all[d.indexArmy()]);
				}
			}
		}

		@Override
		public Iterator<WDIV> iterator() {
			init();
			ii = 0;
			return iterer;
		}

		private int ii;
		private final Iterator<WDIV> iterer = new Iterator<WDIV>() {

			@Override
			public boolean hasNext() {
				return ii < size();
			}

			@Override
			public WDIV next() {
				WDIV d = list.get(ii);
				ii++;
				return d;
			}

		};

		private static double[] supplies;
		private static int upI = -1;

		private static void initSupplies() {
			if (supplies == null || upI != GAME.updateI()) {
				upI = GAME.updateI();
				int[] suppliesHave = new int[RESOURCES.ALL().size()];
				int[] suppliesNeeded = new int[RESOURCES.ALL().size()];
				supplies = new double[STATS.EQUIP().BATTLE_ALL().size()];

				for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
					if (e instanceof Humanoid) {
						Humanoid a = (Humanoid) e;
						Div d = STATS.BATTLE().DIV.get(a);
						if (d != null && d.army() == GAME.ARMIES().player()) {
							for (EquipBattle s : STATS.EQUIP().BATTLE_ALL()) {
								suppliesNeeded[s.resource().index()] += s.target(d);
								suppliesHave[s.resource().index()] += s.stat().indu().get(a.indu());
							}

						}
					}

				}

				for (EquipBattle s : STATS.EQUIP().BATTLE_ALL()) {
					if (suppliesNeeded[s.resource().index()] != 0) {
						suppliesHave[s.resource().index()] += SETT.ROOMS().STOCKPILE.tally().amountReservable.get(s.resource());
						supplies[s.indexMilitary()] = (double) (suppliesHave[s.resource().index()]) / (suppliesNeeded[s.resource().index()]);
					} else {
						supplies[s.indexMilitary()] = 0;
					}
					supplies[s.indexMilitary()] = CLAMP.d(supplies[s.indexMilitary()], 0, 1);
				}
			}
		}



		@Override
		public WDIV get(int index) {
			init();
			return list.get(index);
		}

		
		
		@Override
		public boolean contains(int i) {
			init();
			return list.contains(i);
		}

		@Override
		public boolean contains(WDIV object) {
			if (object instanceof WDivCity)
				return list.contains((WDivCity) object);
			return false;
		}

		@Override
		public int size() {
			init();
			return list.size();
		}

		@Override
		public boolean isEmpty() {
			init();
			return list.size() == 0;
		}

		public class WDivCity implements WDIV {

			private final int di;

			WDivCity(Div div) {
			
				this.di = div.index();

			}

			private Div div() {
				return GAME.ARMIES().division((short) di);
			}

			@Override
			public int men() {
				return div().menNrOf();
			}

			@Override
			public Race race() {
				return div().info.race();
			}

			@Override
			public int menTarget() {
				return div().menNrOf();
			}

			@Override
			public double training(StatTraining tr) {
				return tr.stat.div().getD(div());
			}
			
			@Override
			public double equip(EquipBattle e) {
				initSupplies();
				return (double)(e.target(div())*supplies[e.indexMilitary()])/e.max();
			}

			@Override
			public double experience() {
				return STATS.BATTLE().COMBAT_EXPERIENCE.div().getD(div());
			}

			@Override
			public DivGeneration generate() {

				ArrayList<Induvidual> inus = new ArrayList<Induvidual>(men());
				for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
					if (e instanceof Humanoid) {
						Humanoid a = (Humanoid) e;
						Div d = STATS.BATTLE().DIV.get(a);
						if (d == div()) {
							Induvidual in = new Induvidual(a.indu().hType(), a.indu().race());
							in.copyFrom(a.indu());
							inus.add(in);
						}
					}

				}
				DivGeneration res = new DivGeneration(this, inus, target());
				return res;
			}
			
			@Override
			public void resolve(Induvidual[] hs) {
				
				KeyMap<Induvidual> map = new KeyMap<>();
				for (Induvidual ii : hs) {
					String k = "" + STATS.RAN().getL(ii, 0);
					if (!map.containsKey(k))
						map.put(k, ii);
				}
				for (ENTITY e : SETT.ENTITIES().getAllEnts()) {
					if (e instanceof Humanoid) {
						Humanoid a = (Humanoid) e;
						if (STATS.BATTLE().DIV.get(a) == div()) {
							String k = "" + STATS.RAN().getL(a.indu(), 0);
							if (map.containsKey(k)) {
								a.indu().copyFrom(map.get(k));
							} else {
								STATS.POP().COUNT.reg(a.indu(), CAUSE_LEAVES.SLAYED());
								a.helloMyNameIsInigoMontoyaYouKilledMyFatherPrepareToDie();
							}

						}

					}
				}
			}

			@Override
			public void resolve(int surviviors, double experiencePerMan) {

				
				
				double dExperience = experiencePerMan - experience();
				dExperience *= surviviors;

				int deaths = men() - surviviors;
				
				
				for (ENTITY e : SETT.ENTITIES().getAllEnts()) {

					if (e instanceof Humanoid) {
						Humanoid a = (Humanoid) e;

						if (STATS.BATTLE().DIV.get(a) == div()) {
							if (deaths <= 0) {
								int am = (int) dExperience;
								if (dExperience - am > RND.rFloat())
									STATS.BATTLE().COMBAT_EXPERIENCE.indu().inc(a.indu(), am);
							} else {
								STATS.POP().COUNT.reg(a.indu(), CAUSE_LEAVES.SLAYED());
								a.helloMyNameIsInigoMontoyaYouKilledMyFatherPrepareToDie();
								deaths--;
							}
						}
					}
				}

			}

			@Override
			public int daysUntilMenArrives() {
				return 0;
			}

			@Override
			public CharSequence name() {
				return div().info.name();
			}

			@Override
			public boolean needSupplies() {
				return true;
			}

			@Override
			public DivisionBanner banner() {
				return GAME.ARMIES().banners.get(div().info.bannerI());
			}

			@Override
			public void bannerSet(int bi) {
				div().info.bannerISet(bi);
			}

			@Override
			public Faction faction() {
				return FACTIONS.player();
			}



			@Override
			public int bannerI() {
				return div().info.bannerI();
			}

			@Override
			public WArmy army() {
				// TODO Auto-generated method stub
				return null;
			}

			@Override
			public COLOR color() {
				return cols;
			}

			@Override
			public DIV_SETTING target() {
				return div().info;
			}

		}

	}
	
}
