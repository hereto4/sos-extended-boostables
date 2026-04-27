package game.faction;

import java.util.Arrays;

import game.GAME;
import game.battle.div.Div;
import game.boosting.BUtil;
import game.faction.diplomacy.DIP;
import game.faction.npc.FactionNPC;
import game.faction.npc.stockpile.NPCStockpile;
import init.race.RACES;
import init.race.Race;
import init.resources.RESOURCE;
import init.resources.RESOURCES;
import init.sprite.UI.UI;
import settlement.stats.STATS;
import settlement.stats.equip.EquipBattle;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import util.info.INFO;
import util.text.D;
import util.text.Dic;
import world.map.regions.Region;
import world.map.regions.RegionInfo;
import world.region.RD;
import world.region.building.RDBuilding;

public final class FWorth {

	private static CharSequence ¤¤resD = "Worth of stored resources";
	private static CharSequence ¤¤popD = "Worth of population";
	private static CharSequence ¤¤regionD = "Worth of your regions";
	private static CharSequence ¤¤vassals = "Vassals";
	private static CharSequence ¤¤vassalsD = "Worth of your vassals";
	private static CharSequence ¤¤slaves = "Slaves";
	private static CharSequence ¤¤slavesD = "Worth of your slaves";
	
	static {
		D.ts(FWorth.class);
	}
	
	FWorth(){
		
	}
	
	public final WINT resources = new WINT(Dic.¤¤Resources, ¤¤resD, UI.icons().s.storage) {

		@Override
		public int pget(Faction f) {
			double cache = 0;
			for (int ri = 0; ri < RESOURCES.ALL().size(); ri++) {
				RESOURCE res = RESOURCES.ALL().get(ri);
				cache += worthResource(res, f.res().getAvailable(res));
			}
			if (f == FACTIONS.player()) {
				for (int ei = 0; ei < STATS.EQUIP().BATTLE_ALL().size(); ei++) {
					EquipBattle e = STATS.EQUIP().BATTLE_ALL().get(ei);
					int am = 0;
					for (int di = 0; di < GAME.ARMIES().player().divisions().size(); di++) {
						Div d = GAME.ARMIES().player().divisions().get(di);
						am += d.info.equipI(e)*d.menNrOf();
					}
					if (am > f.res().getAvailable(e.resource))
						am = f.res().getAvailable(e.resource);
					cache -= worthResource(e.resource, am);
					
				}
			}
			return (int) cache;
		}
	};
	
	public static double worthResource(RESOURCE res, int amount) {
		return amount*FACTIONS.PRICE().get(res);
	}
	
	
	
	public final WINT population = new WINT(Dic.¤¤Population, ¤¤popD, UI.icons().s.human) {
		@Override
		public int pget(Faction f) {
			double cache = 0;
			for (int ri = 0; ri < RD.RACES().all.size(); ri++) {
				Race res = RD.RACES().all.get(ri).race;
				cache += f.citizens(res)*NPCStockpile.AVERAGE_PRICE*5.0/res.population().max;
			}
			return (int) cache;
		}
	};
	
	public final WINT slaves = new WINT(¤¤slaves, ¤¤slavesD, UI.icons().s.slave) {
		@Override
		public int pget(Faction f) {
			double cache = 0;
			for (int ri = 0; ri < RACES.all().size(); ri++) {
				Race res = RACES.all().get(ri);
				cache += FSlaves.BASE_PRICE(res)*f.slaves().available(res);
			}
			return (int) cache;
		}
	};
	
	public static double pop(Race race, int amount) {
		return amount*FSlaves.BASE_PRICE(race);
	}
	
	public final WINT regions = new WINT(Dic.¤¤Regions, ¤¤regionD, UI.icons().s.world) {
		@Override
		public int pget(Faction f) {
			double cache = 0;
			for (int i = 0; i < f.realm().regions(); i++) {
				Region reg = f.realm().region(i);
				if (reg.capitol())
					continue;
				cache += region(reg);
			}
			return (int) cache;
		}
	};
	
	public static double region(Region reg) {
		double v = -1;
		for (int bi = 0; bi < RD.BUILDINGS().all.size(); bi++) {
			RDBuilding b = RD.BUILDINGS().all.get(bi);
			double a = BUtil.value(b.baseFactors, reg);
			v = Math.max(a, v);
		}
		v *= NPCStockpile.AVERAGE_PRICE;
		
		v += (RegionInfo.vFer().getAi(reg) + RegionInfo.vArea().getAi(reg))*FACTIONS.PRICE().edible();
		return v * 0.25*RD.RACES().population.get(reg);
	}
	
	public final WINT worthVassals = new WINT(¤¤vassals, ¤¤vassalsD, UI.icons().s.noble) {


		@Override
		public int pget(Faction f) {
			double c = 0;
			for (Faction f2 : DIP.VASSAL().all(f)){
				c += vassal(f2);
			}
			return (int) c;
				
		}
	};
	
	public static double vassal(Faction fa) {
		if (fa instanceof FactionNPC) {
			FactionNPC f = (FactionNPC) fa;
			DIP.TMP().setFactionAndClear(f);
			return (int) Math.ceil(DIP.TMP().npc.offerableWorth()*0.05);
		}
		return 0;
	}
	
	public final WINT cash = new WINT(Dic.¤¤Currs, Dic.¤¤Currs, UI.icons().s.money) {
		
		@Override
		public int pget(Faction f) {
			return (int) f.credits().getD();
		}
	};
	
	public final LIST<WINT> raider = new ArrayList<WINT>(cash, resources, slaves);
	public final LIST<WINT> faction = new ArrayList<WINT>(cash, resources, population, slaves, regions, worthVassals);
	
	public double raider() {
		return get(raider, FACTIONS.player());
	}
	
	public double faction() {
		return get(faction, FACTIONS.player());
	}
	
	public double raider(Faction f) {
		return get(raider, f);
	}
	
	public double faction(Faction f) {
		return get(faction, f);
	}
	
	private double get(LIST<WINT> li, Faction f) {
		double am = 0;
		for (WINT d : li)
			am += d.get(f);
		return am;
	}
	
	
	public static abstract class WINT {
		
		private int[] upI = new int[FACTIONS.MAX()];
		private int[] cache = new int[FACTIONS.MAX()];
		public final INFO info;
		public SPRITE icon;
		
		WINT(CharSequence name, CharSequence desc, SPRITE icon){
			this.info = new INFO(name, desc);
			this.icon = icon;
			Arrays.fill(upI, -1);
		}
		
		public int player() {
			return get(FACTIONS.player());
		}
		
		public final int get(Faction f) {
			if (f == null)
				return 0;
			if (upI[f.index()] != GAME.updateI()) {
				upI[f.index()] = GAME.updateI();
				cache[f.index()] = pget(f);
			}
			return cache[f.index()];
		}
		
		protected abstract int pget(Faction f);
		
	}
	
}
