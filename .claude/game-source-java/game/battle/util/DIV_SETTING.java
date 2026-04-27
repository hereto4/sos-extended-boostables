package game.battle.util;

import java.io.IOException;
import java.io.Serializable;
import java.util.Arrays;

import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.CLAMP;

public interface DIV_SETTING {

	/**
	 * 
	 * @param tr
	 * @return
	 */
	public double training(StatTraining tr);
	
	public double equip(EquipBattle e);
	
	public int men();
	
	public default int equipI(EquipBattle e) {
		return (int) CLAMP.d(Math.round(equip(e)*e.equipMax), 0, e.equipMax);
	}
	
	public interface DIV_SETTINGE extends DIV_SETTING {
		
		public void trainingSet(StatTraining tr, double d);
		public void equipSet(EquipBattle e, double d);
		public void menSet(int men);
		
		public default DIV_SETTINGE copySettings(DIV_SETTING other) {
			for (int i = 0; i < STATS.EQUIP().BATTLE_ALL().size(); i++) {
				EquipBattle b = STATS.EQUIP().BATTLE_ALL().get(i);
				equipSet(b, other.equip(b));
			}
			for (int i = 0; i < STATS.BATTLE().TRAINING_ALL.size(); i++) {
				StatTraining b = STATS.BATTLE().TRAINING_ALL.get(i);
				trainingSet(b, other.training(b));
			}
			menSet(other.men());
			return this;
		}
		
		public default DIV_SETTINGE copySettings(DIV_SETTING other, int men, double e, double t) {
			
			for (int i = 0; i < STATS.EQUIP().BATTLE_ALL().size(); i++) {
				EquipBattle b = STATS.EQUIP().BATTLE_ALL().get(i);
				double d =  (int)Math.round(other.equip(b)*e*b.max());
				d /= b.max();
				d = CLAMP.d(d, 0, 1);
				equipSet(b, d);
			}
			for (int i = 0; i < STATS.BATTLE().TRAINING_ALL.size(); i++) {
				StatTraining b = STATS.BATTLE().TRAINING_ALL.get(i);
				double d =  (int)(other.training(b)*t*b.stat.indu().max(null));
				d /= b.stat.indu().max(null);
				d = CLAMP.d(d, 0, 1);
				trainingSet(b, d);
			}
			menSet(men);
			return this;
		}
		
	}
	
	public static class DIV_SETTINGImp implements SAVABLE, DIV_SETTINGE, Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		public double[] equip = new double[STATS.EQUIP().BATTLE_ALL().size()];
		public double[] training = new double[STATS.BATTLE().TRAINING_ALL.size()];
		public int men;

		@Override
		public void save(FilePutter file) {
			file.ds(equip);
			file.ds(training);
			file.i(men);
		}

		@Override
		public void load(FileGetter file) throws IOException {
			file.ds(equip);
			file.ds(training);
			men = file.i();
		}

		@Override
		public void clear() {
			Arrays.fill(equip, 0);
			Arrays.fill(training, 0);
			men = 0;
		}
		
		@Override
		public double training(StatTraining tr) {
			if (training == null || training.length != STATS.BATTLE().TRAINING_ALL.size())
				training = new double[STATS.BATTLE().TRAINING_ALL.size()];
			return training[tr.tIndex];
		}

		@Override
		public double equip(EquipBattle e) {
			if (equip == null || equip.length != STATS.EQUIP().BATTLE_ALL().size())
				equip = new double[STATS.EQUIP().BATTLE_ALL().size()];
			return equip[e.indexMilitary()];
		}

		@Override
		public int men() {
			return men;
		}

		@Override
		public void trainingSet(StatTraining tr, double d) {
			training[tr.index()] = d;
			
		}

		@Override
		public void equipSet(EquipBattle e, double d) {
			equip[e.indexMilitary()] = d;
		}

		@Override
		public void menSet(int men) {
			this.men = men;
		}
		
	}
	
}
