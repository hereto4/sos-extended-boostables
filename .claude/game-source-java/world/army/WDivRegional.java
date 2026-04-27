package world.army;

import java.io.IOException;
import java.util.Arrays;

import game.GAME;
import game.battle.util.DIV_SETTING;
import game.battle.util.DIV_SPEC;
import game.battle.util.DivGeneration;
import game.battle.util.DivType;
import game.faction.Faction;
import init.constant.Config;
import init.race.RACES;
import init.race.Race;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import snake2d.util.color.COLOR;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.text.Dic;
import world.entity.army.WArmy;

public final class WDivRegional extends ADDiv {

	public static int DAYS_TO_TRAIN = 3;
	private static final COLOR col = COLOR.ORANGE100.makeSaturated(0.5).shade(0.75);
	static final int type = 0;

	private short men;
	private short menTarget;
	private short ri;
	private final float[] training = new float[STATS.BATTLE().TRAINING_ALL.size()];
	private final byte[] trainingTarget = new byte[STATS.BATTLE().TRAINING_ALL.size()];
	private float experience;
	private byte trainingDay;
	private short bannerI;
	private final byte[] targets = new byte[STATS.EQUIP().BATTLE_ALL().size()];

	
	WDivRegional(int index){
		super(index);
	}
	
	public void init(Race race, double amount, WArmy a) {

		
		menTarget = (short) CLAMP.i((int)Math.round(amount*Config.battle().MEN_PER_DIVISION), 0, Config.battle().MEN_PER_DIVISION);
		men = 0;
		ri = (short) race.index;
		Arrays.fill(training, 0);
		Arrays.fill(trainingTarget, (byte)0);
		experience = 0;
		trainingDay = 0;
		Arrays.fill(targets, (byte)0);
		
		bannerSet(RND.rInt(GAME.ARMIES().banners.size()));
		reassign(a);
	}
	
	public void randomize(double training, double gear) {
		report(-1);
		
		
		DivType type = GAME.battle().types.rnd(race(), faction(), RND.rFloat());

		for (EquipBattle m : STATS.EQUIP().BATTLE_ALL()) {
			targets[m.indexMilitary()] = (byte) CLAMP.d(type.equip(m)*gear*m.max(), 0, m.max());
		}
		
		for (StatTraining t : STATS.BATTLE().TRAINING_ALL) {
			this.training[t.tIndex] = (byte) CLAMP.d(type.training(t)*training*StatTraining.MAX, 0, StatTraining.MAX);
			trainingTarget[t.tIndex] = (byte) Math.round(StatTraining.MAX*CLAMP.d(type.training(t)*training, 0, 1));
		}
		
		bannerSet(RND.rInt(GAME.ARMIES().banners.size()));

		report(1);
	}
	
	public void copyFrom(DIV_SPEC div) {
		report(-1);
		for (EquipBattle m : STATS.EQUIP().BATTLE_ALL()) {
			targets[m.indexMilitary()] = (byte)div.equipI(m);
		}
		
		for (StatTraining t : STATS.BATTLE().TRAINING_ALL) {
			this.training[t.tIndex] = (byte) div.training(t)*StatTraining.MAX;
		}
		report(1);
	}
	
	@Override
	public void save(FilePutter file) {
		super.save(file);
		file.s(men);
		file.s(menTarget);
		file.s(ri);
		file.bs(trainingTarget);
		file.fs(training);
		file.f(experience);
		file.b(trainingDay);
		file.s(bannerI);
		file.bs(targets);
	}

	@Override
	public void load(FileGetter file) throws IOException {
		super.load(file);
		men = file.s();
		menTarget = file.s();
		ri = file.s();
		file.bs(trainingTarget);
		file.fs(training);
		experience = file.f();
		trainingDay = file.b();
		bannerI = file.s();
		file.bs(targets);
	}


	@Override
	public int men() {
		return men;
	}

	@Override
	public int menTarget() {
		return menTarget;
	}

	@Override
	public void resolve(Induvidual[] hs) {
		double exp = 0;
		for (Induvidual i : hs)
			exp += STATS.BATTLE().COMBAT_EXPERIENCE.indu().getD(i);
		if (hs.length > 0)
			exp /= hs.length;
		resolve(hs.length, exp);
	}
	
	@Override
	public void resolve(int surviviors, double experiencePerMan) {
		int death = men - surviviors;
		menSet(surviviors);
		
		AD.conscripts().kill(race(), faction(), death);
		report(-1);
		this.experience = (float) CLAMP.d(experiencePerMan, 0, 1);
		report(1);
	}
	
	@Override
	public void menSet(int amount) {
		report(-1);
		double exp = experience*men;
		amount = CLAMP.i(amount, 0, Config.battle().MEN_PER_DIVISION);
		men = (short) amount;
		experience = 0;
		if (men > 0)
			experience = (float) CLAMP.d(exp/men, 0, 1);
		report(1);
	}

	@Override
	protected void armyChange(WArmy old, WArmy newW) {
		if (newW == null)
			AD.regional().retire(this);
	}
	
	@Override
	public Race race() {
		return RACES.all().get(ri);
	}
	
	@Override
	public double training(StatTraining tr) {
		return training[tr.tIndex]*StatTraining.MAXI;
	}

	@Override
	public double equip(EquipBattle e) {
		if (army() == null)
			return 0;
		return AD.supplies().get(e).amountValue(army())*targets[e.indexMilitary()]/e.equipMax;
	}
	
	public void equipTargetset(EquipBattle e, int t) {
		report(-1);
		targets[e.indexMilitary()] = (byte) t;
		report(1);
	}

	@Override
	public double experience() {
		return (double)experience;
	}

	@Override
	public int daysUntilMenArrives() {
		return DAYS_TO_TRAIN-trainingDay;
	}



	public void updateDay() {
		
		if (!army().recruiting())
			return;
		
		if (men() < menTarget()) {
			int ava = AD.conscripts().canTrainI(race(), faction());
			if (ava > 0) {
				trainingDay ++;
				if (trainingDay >= DAYS_TO_TRAIN) {
					trainingDay = 0;
					int men = menTarget()-men();
					if (faction() != null) {
						men = CLAMP.i(men, 0, ava);
					}
					if (men > 0) {
						double vv = men();
						vv/=(men()+men);
						menSet(men()+men);
						report(-1);
						experience*= vv;
						for (int ti = 0; ti < training.length; ti++) {
							training[ti] = (float) CLAMP.d(training[ti]*vv, 0, StatTraining.MAX);
						}
						report(1);
						return;
					}
				}
			}else {
				trainingDay = 0;
			}
		}
		

		
		for (int ti = 0; ti < STATS.BATTLE().TRAINING_ALL.size(); ti++) {
			StatTraining st =  STATS.BATTLE().TRAINING_ALL.get(ti);
			double tr = training[ti];
			double ta = trainingTarget[ti];
			if (tr < ta) {
				double bo = faction() == null ? st.room.bonus().baseValue : st.room.bonus().get(faction());
				double n = tr + StatTraining.MAX*0.75*bo/st.room.TRAINING_DAYS;
				
				n = CLAMP.d(n, 0, ta);
				report(-1);
				training[ti] = (float) n;
				
				report(1);
				return;
			}
		}
		
	}
	
	public static int trainingDays(StatTraining tr, double delta, Faction faction) {
		if (delta <= 0)
			return 0;
		double div = 0.75;
		if (faction != null)
			div *= tr.room.bonus().get(faction);
		return (int) Math.ceil(delta*tr.room.TRAINING_DAYS/(0.75*div));
	}
	
	public static double tD(StatTraining tr, double delta, Faction faction) {

		return delta*tr.room.TRAINING_DAYS/(0.75*tr.room.bonus().get(faction));
	}
	
	@Override
	public int type() {
		return type;
	}



	@Override
	public CharSequence name() {
		return Str.TMP.clear().add(Dic.¤¤Regional).insert(0, Dic.¤¤Division);
	}



	@Override
	public boolean needSupplies() {
		return true;
	}
	


	@Override
	public DivGeneration generate() {
		return new DivGeneration(this, target);
	}
	
	@Override
	public boolean needConscripts() {
		return true;
	}
	
	@Override
	public int bannerI() {
		return bannerI;
	}

	@Override
	public void bannerSet(int bi) {
		this.bannerI = (short) bi;
	}

	@Override
	public COLOR color() {
		return col;
	}

	public final DIV_SPECE target = new DIV_SPECE() {
		
		@Override
		public double training(StatTraining tr) {
			return trainingTarget[tr.tIndex]*StatTraining.MAXI;
		}
		
		@Override
		public int men() {
			return menTarget;
		}
		
		@Override
		public double equip(EquipBattle e) {
			return (double)targets[e.indexMilitary()]/e.equipMax;
		}

		@Override
		public void trainingSet(StatTraining tr, double d) {
			trainingTarget[tr.tIndex] = (byte) Math.round(StatTraining.MAX*CLAMP.d(d, 0, 1));
		}

		@Override
		public void equipSet(EquipBattle tr, double am) {
			report(-1);
			targets[tr.indexMilitary()] = (byte) Math.round(am*tr.max());
			report(1);
			
		}

		@Override
		public void menSet(int am) {
			report(-1);
			menTarget = (short) CLAMP.i(am, 0, Config.battle().MEN_PER_DIVISION);
			trainingDay = 0;
			men = (short) CLAMP.i(men, 0, menTarget);
			report(1);
			
		}

		@Override
		public double experience() {
			return experience;
		}

		@Override
		public Faction faction() {
			return army() == null ? null : army().faction();
		}

		@Override
		public CharSequence name() {
			return WDivRegional.this.name();
		}

		@Override
		public int bannerI() {
			return WDivRegional.this.bannerI();
		}

		@Override
		public Race race() {
			return WDivRegional.this.race();
		}

		@Override
		public void raceSet(Race race) {
			report(-1);
			ri = (short) race.index;
			report(1);
		}

		@Override
		public void experienceSet(double experience) {
			WDivRegional.this.experience = (float) experience;
		}

		@Override
		public Str nameE() {
			return null;
		}

		@Override
		public void bannerISet(int bannerI) {
			WDivRegional.this.bannerI = (short) bannerI;
			
		}

		@Override
		public void factionSet(Faction faction) {
			// TODO Auto-generated method stub
			
		}
	};


	@Override
	public DIV_SETTING target() {
		return target;
	}

	
}
