package game.raiding;

import java.io.Serializable;

import game.GAME;
import game.battle.util.DIV_SPEC;
import game.battle.util.DivGeneration;
import game.battle.util.DIV_SETTING.DIV_SETTINGImp;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.constant.Config;
import init.race.RACES;
import init.race.Race;
import settlement.battle.invasion.InvasionSpec;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import snake2d.util.datatypes.DIR;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayListGrower;
import world.WORLD;
import world.army.AD;
import world.army.ADSupplies;
import world.army.WDivRegional;
import world.army.ADSupplies.ADArtillery;
import world.entity.army.WArmy;
import world.map.regions.Region;

public final class RaiderArmy implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final int men;
	public final ArrayListGrower<RaidDiv> sdivs = new ArrayListGrower<>();
	public int[] artillery = new int[SETT.ROOMS().ARTILLERY.size()];
	public final int power;
	
	public RaiderArmy(Race race, double totPower, double quality){
		
		int menTot = 0;
		int divMenTarget = 1;
		
		final double powChunk = totPower/8;
		
		{
			RaidDiv stat = new RaidDiv();
			double training = CLAMP.d(quality/2 + RND.rFloat()*quality, 0, 1);
			double equip = CLAMP.d(quality/2 + RND.rFloat()*quality, 0, 1);
			
			stat.race = race.index();
			stat.men = 1;
			stat.ex = CLAMP.d(RND.rFloat()*quality, 0, 1);
			stat.name = ""+stat.race().info.armyNames.rnd();
			stat.bannerI = RND.rInt(GAME.ARMIES().banners.size());
			stat.copySettings(GAME.battle().types.rnd(stat.race(),  FACTIONS.player(), RND.rFloat()), 1, training, equip);
			
			double p = GAME.battle().power.get(stat);
			
			double men = totPower/p;
			men /= 2;
			men /= 5;
			men = 5*(int) Math.ceil(men);
			men = CLAMP.d(men, 5, Config.battle().MEN_PER_DIVISION);
			divMenTarget = (int) men;
			
			stat.men = (int) men;
			totPower -= GAME.battle().power.get(stat);
			sdivs.add(stat);			
		}
		
		double raceMax = 0;
		for (Race r : RACES.all()) {
			raceMax += race(race, r, totPower);
		}
		
		
		while(totPower > 0 && sdivs.size() < Config.battle().DIVISIONS_PER_ARMY) {
			
			RaidDiv stat = new RaidDiv();
			double training = CLAMP.d(quality*0.25 + RND.rFloat()*quality*0.75, 0, 1);
			double equip = CLAMP.d(quality*0.25 + RND.rFloat()*quality*0.75, 0, 1);
			
			stat.race = race(race, raceMax, totPower).index;
			stat.men = 1;
			stat.ex = CLAMP.d(RND.rFloat()*quality, 0, 1);
			stat.name = ""+stat.race().info.armyNames.rnd();
			stat.bannerI = RND.rInt(GAME.ARMIES().banners.size());
			stat.copySettings(GAME.battle().types.rnd(stat.race(), FACTIONS.player(), RND.rFloat()), 1, training, equip);
			
			double p = GAME.battle().power.get(stat);
			double dd = totPower/p;
			if (dd > 1) {
				stat.men*=dd;
				stat.men = CLAMP.i(stat.men, 5, divMenTarget);
			}
			p = GAME.battle().power.get(stat);
			sdivs.add(stat);
			totPower -= p;
			int am = (int) ((0.5+RND.rFloat()*3)*(powChunk/p));
			while(am >= 0 && totPower > 0 && sdivs.size() < Config.battle().DIVISIONS_PER_ARMY) {
				sdivs.add(new RaidDiv(stat));
				stat.name = ""+stat.race().info.armyNames.rnd();
				totPower -= p;
				am--;
			}
			
			
			
			
		}

		double dd = 0;
		for (RaidDiv d : sdivs) {
			dd += GAME.battle().power.get(d);
			menTot += d.men();
		}
		this.power = (int) dd;
		
		
		this.men = menTot;
		
		int art = ADSupplies.artilleryMax*menTot/Config.battle().MEN_PER_ARMY;
		while(art-- > 0) {
			ADArtillery a = AD.supplies().arts().rnd();
			artillery[a.index()] ++;
		}
	}
	
	
	public WArmy spawn(int wx, int wy, CharSequence name) {
		Region reg = WORLD.REGIONS().map.get(wx, wy);
		if (reg != null && !isGoodTile(wx, wy, DIR.C, WORLD.REGIONS().map.get(wx, wy))) {
			for (DIR d : DIR.ALL) {
				if (WORLD.PATH().map.can(wx, wy, d) && isGoodTile(wx, wy, d, reg)) {
					wx += d.x();
					wy += d.y();
					break;
				}
			}
		}
		
		for (int i = 0; i < 10; i++) {
			if (!removeArmies(wx, wy))
				break;
		}
		
		final WArmy a = WORLD.ENTITIES().armies.create(wx, wy, null);

		for (RaidDiv div : sdivs) {
			WDivRegional d = AD.regional().create(div.race(), (double) div.men / Config.battle().MEN_PER_DIVISION, a);
			d.copyFrom(div);
			d.menSet(d.menTarget());
		}
		a.name.clear().add(name);
		AD.supplies().fillAll(a);
		AD.updateArmy(a);
		return a;
	}
	
	public int invade(int wx, int wy, Induvidual raider) {
		InvasionSpec sp = new InvasionSpec();
		boolean first = true;
		for (DIV_SPEC stat : sdivs) {
			DivGeneration g = new DivGeneration(stat, stat);
			if (first) {
				first = false;
				g.indus[0].copyFrom(raider);
			}
			sp.divs.add(g);
		}
		for (int i = 0; i < artillery.length; i++) {
			sp.artillery[i] = artillery[i];
		}
		sp.wx = wx;
		sp.wy = wy;

		return SETT.INVADOR().invade(sp, null);
	}
	
	private boolean isGoodTile(int tx, int ty, DIR d, Region home) {
		
		tx += d.x();
		ty += d.y();
		
		if (!home.is(tx, ty))
			return false;
		
		for (int di = 0; di < DIR.ALL.size(); di++) {
			d = DIR.ALL.get(di);
			if (WORLD.PATH().map.can(tx, ty, d)) {
				Region reg = WORLD.REGIONS().map.get(tx, ty, d);
				if (reg != null && reg != home && reg.faction() != null && reg.faction() != FACTIONS.player()) {
					return false;
				}
			}
		}
		return true;
	}
	
	private boolean removeArmies(int wx, int wy) {
		for (int di = 0; di < DIR.ALLC.size(); di++) {
			DIR d = DIR.ALLC.get(di);
			if (d == DIR.C || WORLD.PATH().map.can(wx, wy, d)) {
				
				int dx = wx + d.x();
				int dy = wy + d.y();
				
				for (WArmy a2 : WORLD.ENTITIES().armies.fillTile(dx, dy)) {
					if (a2.ctx() == dx && a2.cty() == dy) {
						if (a2.faction() != FACTIONS.player() && a2.faction() != null) {
							if (a2.region() != null && a2.region().faction() == a2.faction()) {
								a2.teleport(a2.region().cx(), a2.region().cy());
							}else
								a2.disband();
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	private double race(Race leader, Race r, double totPower) {
		double d = leader.pref().race(r);
		if (leader == r)
			d *= 4;
		else if (!r.playable)
			d *= 0.2;
		return d;
			
	}
	
	private Race race(Race leader, double tot, double totPower) {
		tot *= RND.rFloat();
		for (Race r : RACES.all()) {
			tot -= race(leader, r, totPower);
			if (tot <= 0)
				return r;
		}
		return leader;
	}
	
	static class RaidDiv extends DIV_SETTINGImp implements DIV_SPEC, Serializable {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		int race;
		double ex;
		private String name;
		private int bannerI;
		
		RaidDiv(){

		}
		
		RaidDiv(RaidDiv o){
			this.race = o.race;
			this.ex = o.ex;
			this.name = o.name;
			this.bannerI = o.bannerI;
			copySettings(o);
		}
		
		@Override
		public Race race() {
			return RACES.all().getC(race);
		}
		
		@Override
		public Faction faction() {
			return null;
		}
		
		@Override
		public double experience() {
			return ex;
		}

		@Override
		public CharSequence name() {
			return name;
		}

		@Override
		public int bannerI() {
			return bannerI;
		}
	};
	
}
