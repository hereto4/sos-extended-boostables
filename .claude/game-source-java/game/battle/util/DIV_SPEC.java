package game.battle.util;

import java.util.Arrays;

import game.GAME;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.constant.Config;
import init.race.RACES;
import init.race.Race;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.text.Dic;

public interface DIV_SPEC extends DIV_SETTING, DIV_SIMPLE{

	public double experience();
	public Faction faction();
	public CharSequence name();
	public int bannerI();
	
	public interface DIV_SPECE extends DIV_SPEC, DIV_SETTINGE{
		
		public void raceSet(Race race);
		public void experienceSet(double experience);
		public Str nameE();
		public void bannerISet(int bannerI);
		

		public void factionSet(Faction faction);
		
		public default DIV_SPECE copyFrom(DIV_SPEC other) {
			for (int i = 0; i < STATS.BATTLE().TRAINING_ALL.size(); i++) {
				StatTraining t = STATS.BATTLE().TRAINING_ALL.get(i);
				trainingSet(t, other.training(t));
			}
				
			for (int i = 0; i < STATS.EQUIP().BATTLE_ALL().size(); i++) {
				EquipBattle t = STATS.EQUIP().BATTLE_ALL().get(i);
				equipSet(t, other.equip(t));
			}
			
			menSet(other.men());
			experienceSet(other.experience());
			raceSet(other.race());
			nameE().clear().add(other.name());
			bannerISet(other.bannerI());
			factionSet(other.faction());
			return this;
		}
		
		public default void generate() {
			double tr = RND.rFloat();
			double eq = RND.rFloat();
			
			Race r = RACES.all().rnd();
			raceSet(r);
			menSet(10 + RND.rInt(Config.battle().MEN_PER_DIVISION-10));
			experienceSet(RND.rFloat());
			nameE().clear().add(r.info.armyNames.rnd());
			bannerISet(RND.rInt(GAME.ARMIES().banners.size()));

			DivType dd = GAME.battle().types.rnd(r, FACTIONS.player(), RND.rFloat());
			
			for (int i = 0; i < STATS.EQUIP().BATTLE_ALL().size(); i++) {
				EquipBattle t = STATS.EQUIP().BATTLE_ALL().get(i);
				equipSet(t, dd.equip(STATS.EQUIP().BATTLE_ALL().get(i))*eq);
			}
			for (int i = 0; i < STATS.BATTLE().TRAINING_ALL.size(); i++) {
				StatTraining t = STATS.BATTLE().TRAINING_ALL.get(i);
				trainingSet(t, dd.training(STATS.BATTLE().TRAINING_ALL.get(i))*tr);
			}
		}
		
	}
	
	
	public class DIV_SPECImp implements DIV_SPECE{
		
		private final double[] training = new double[STATS.BATTLE().TRAINING_ALL.size()];
		private final double[] gear = new double[STATS.EQUIP().BATTLE_ALL().size()];
		private int men = 10;
		private double experience = 0;
		private int race = 0;
		private Str name = new Str(24).add(Dic.¤¤rename);
		private int bannerI = 0;
		private int faction = 0; 
		
		public DIV_SPECImp() {
			
			
		}
		
		void clear(Race race) {
			Arrays.fill(training, 0);
			Arrays.fill(gear, 0);
			men = 10;
			experience = 0;
			this.race = race.index;
			name.clear().add(Dic.¤¤rename);
			bannerI = 0;
			faction = 0;
		}

		@Override
		public double training(StatTraining tr) {
			return training[tr.tIndex];
		}
		
		@Override
		public double equip(EquipBattle e) {
			return gear[e.indexMilitary()];
		}

		@Override
		public int men() {
			return men;
		}

		@Override
		public Race race() {
			return RACES.all().get(race);
		}
		
		@Override
		public void raceSet(Race race) {
			this.race = race.index;
		}

		@Override
		public double experience() {
			return experience;
		}

		@Override
		public Faction faction() {
			return FACTIONS.getByIndex(faction);
		}

		@Override
		public CharSequence name() {
			return name;
		}
		
		@Override
		public int bannerI() {
			return bannerI;
		}

		@Override
		public void menSet(int men) {
			this.men = men;
		}

		@Override
		public void experienceSet(double experience) {
			this.experience = experience;
			
		}

		@Override
		public Str nameE() {
			return name;
		}

		@Override
		public void bannerISet(int bannerI) {
			this.bannerI = bannerI;
		}

		@Override
		public void trainingSet(StatTraining tr, double d) {
			training[tr.tIndex] = d;
			
		}

		@Override
		public void equipSet(EquipBattle e, double d) {
			gear[e.indexMilitary()] = d;
		}

		@Override
		public void factionSet(Faction faction) {
			this.faction = faction.index();
		}
		
	}
	
}
