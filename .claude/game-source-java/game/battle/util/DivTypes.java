package game.battle.util;

import game.GAME;
import game.faction.Faction;
import init.paths.PATH;
import init.paths.PATHS;
import init.race.RACES;
import init.race.Race;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.LOG;
import snake2d.util.file.Json;
import snake2d.util.misc.ACTION;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.LIST;
import util.text.Dic;

public class DivTypes {

	
	private final ArrayListGrower<DivType> types = new ArrayListGrower<>();
	private final double[] occMaxs = new double[RACES.all().size()];
	private DivType tmp = new DivType();
	
	DivTypes() {
		PATH p = PATHS.INIT().getFolder("battle").getFolder("divType");
		
		for (String f : p.getFiles()) {
			Json j = new Json(p.get(f));
			Json[] mm = j.jsons("TYPES");
			
			for (Json jj : mm) {
				double occ = jj.d("OCCURENCE");
				LIST<StatTraining> tr = STATS.BATTLE().TRAINING_MAP.readMany(jj);
				Json eqs = jj.json("EQUIPMENT");
				LIST<String> ekeys = eqs.keys();
				occ /= ekeys.size();
				for (String k : ekeys) {
					
					LIST<EquipBattle> eqps = STATS.EQUIP().militaryColl.readMany(k, eqs);
					types.add(new DivType(occ, tr, eqps));
				}
				
				
				
				
			}
		}
		

		
		GAME.addOnInit(new AA());
		
	}
	
	public DivType rnd(Race race, Faction f, double ran) {
		
		ran -= (int) ran;
		ran *= occMaxs[race.index];
		
		
		
		for (int i = 0; i < types.size(); i++) {
			if (!types.get(i).valid(race))
				continue;
			ran -= types.get(i).roccurence[race.index()];
			if (ran <= 0)
				return types.get(i);
		}
		
		DivType r = types.rnd();
		
		for (EquipBattle b : STATS.EQUIP().BATTLE_ALL()) {
			if (b.allowed(race))
				tmp.equip[b.indexMilitary()] = r.equip(b);
			else
				tmp.equip[b.indexMilitary()] = 0;
		}
		
		for (int i = 0; i < tmp.training.length; i++)
			tmp.training[i] = r.training[i];
		
		return tmp;
	}
	
	void debug() {
		for (DivType t : types) {
			LOG.ln(t.occurence);
			for (StatTraining tr : STATS.BATTLE().TRAINING_ALL)
				LOG.ln(tr.stat.stats.info().name + " " + t.training(tr));
			for (EquipBattle e : STATS.EQUIP().BATTLE_ALL())
				LOG.ln(e.resource.name + " " + t.equip(e));
			
			for (Race r : RACES.all()) {
				LOG.ln(r.key + " " + t.roccurence[r.index()]/occMaxs[r.index()]);
			}
			LOG.ln();
		}
	}
	
	
	public LIST<DivType> ALL(){
		return types;
	}
	
	private class AA implements ACTION {

		private Race race;
		private DivType type;
		
		private DIV_SPEC stats = new DIV_SPEC() {
			
			@Override
			public double training(StatTraining tr) {
				return type.training(tr);
			}
			
			@Override
			public double equip(EquipBattle e) {
				return type.equip(e);
			}
			
			@Override
			public Race race() {
				return race;
			}
			
			@Override
			public int men() {
				return 10;
			}
			
			@Override
			public Faction faction() {
				return null;
			}
			
			@Override
			public double experience() {
				return 0.2;
			}
			
			@Override
			public CharSequence name() {
				return Dic.empty;
			}

			@Override
			public int bannerI() {
				return 0;
			};
		};
		
		
		@Override
		public void exe() {
			for (DivType t : types) {
				type = t;
				for (int ri = 0; ri < RACES.all().size(); ri++) {
					
					
					race = RACES.all().get(ri);
					if (!t.valid(race))
						type.roccurence[ri] = 0;
					else
						type.roccurence[ri] = type.occurence*GAME.battle().power.get(stats);
					
					occMaxs[ri] += type.roccurence[ri];
				}
			}
			//debug();
			//debug();
		}
		
		
	}
	
	
}
