package game.battle.util;

import java.io.IOException;

import game.GAME;
import game.VERSION;
import game.battle.util.DIV_SETTING.DIV_SETTINGImp;
import game.battle.util.DIV_SPEC.DIV_SPECImp;
import game.faction.FACTIONS;
import game.faction.Faction;
import init.constant.Config;
import init.race.RACES;
import init.race.Race;
import init.type.HTYPES;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import settlement.stats.equip.EquipRange;
import settlement.stats.stat.STAT;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.LIST;
import world.army.WDIV;

public final class DivGeneration {

	/**
	 * 
	 */
	public Induvidual[] indus;
	public final short race;
	public final CharSequence name;
	public final int bannerI;
	public final boolean isRange;
	public final DIV_SETTINGImp target = new DIV_SETTINGImp(); 
	
	public DivGeneration(DIV_SPEC div, DIV_SETTING target) {
		indus = new Induvidual[div.men()];
		race = (short) div.race().index();
		this.name = "" + div.name();
		this.bannerI = div.bannerI();
		if (div.men() > Config.battle().MEN_PER_DIVISION)
			throw new RuntimeException();

		for (int i = 0; i < indus.length; i++) {
			Induvidual ii = new Induvidual(HTYPES.SUBJECT(), div.race());
			init(div, ii, true);
			indus[i] = ii;

		}
		isRange = range(div);
		
		
		this.target.copySettings(target);
		this.target.men = div.men();
		
	}

	private DivGeneration(DIV_SPEC div,  CharSequence name, int bannerI, LIST<Induvidual> all, DIV_SETTING target) {
		indus = new Induvidual[div.men()];
		race = (short) div.race().index();
		this.name = "" + name;
		this.bannerI = bannerI;

		for (int i = 0; i < indus.length; i++) {
			indus[i] = all.get(i);
			init(div, indus[i], false);
		}
		isRange = range(div);
		this.target.copySettings(target);
		this.target.men = div.men();
	}

	public DivGeneration(WDIV div, LIST<Induvidual> all, DIV_SETTING target) {
		this(div, div.name(), div.bannerI(), all, tar(div, target));
	}

	private static DIV_SETTING tar(WDIV div, DIV_SETTING target) {
		DIV_SETTINGImp tar = new DIV_SETTINGImp();
		tar.copySettings(div);
		tar.men = div.men();
		return tar;
	}
	
	
	public static DivGeneration rnd() {
	
		DIV_SPEC stats = new DIV_SPEC() {
			final Race race = RACES.all().rnd(); 
			final DivType dd = GAME.battle().types.rnd(race, FACTIONS.player(), RND.rFloat());
			
			final int men = CLAMP.i(RND.rInt(Config.battle().MEN_PER_DIVISION)+25, 1, Config.battle().MEN_PER_DIVISION);
			final String name = ""+race.info.armyNames.rnd();
			final int bannerI = RND.rInt(GAME.ARMIES().banners.size());
			
			double tr = RND.rFloat();
			double eq = RND.rFloat();
			double ex = RND.rFloat();
			
			@Override
			public double training(StatTraining t) {
				return dd.training(t)*tr;
			}
			
			@Override
			public double equip(EquipBattle e) {
				return dd.equip(e)*eq;
			}
			
			@Override
			public Race race() {
				return race;
			}
			
			@Override
			public int men() {
				return men;
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
		
		return new DivGeneration(stats, stats); 
	}
	

	
	public void setMen(int men) {
		Induvidual[] in = new Induvidual[Math.min(men, indus.length)];
		for (int i = 0; i < in.length; i++)
			in[i] = indus[i];
		indus = in;
	}
	
	public DivGeneration(FileGetter file) throws IOException{
		race = (short) file.i();
		name = file.chars();
		bannerI = file.i();
		isRange = file.bool();
		
		indus = new Induvidual[file.i()];
		for (int i = 0; i < indus.length; i++)
			indus[i] = new Induvidual(file);
		
		if (!VERSION.versionIsBefore(70, 25))
			target.load(file);
		else
			target.men = indus.length;
		
	}
	
	private static boolean range(DIV_SPEC div) {
		for(EquipRange r : STATS.EQUIP().RANGED())
			if (div.equip(r) > 0)
				return true;
		return false;
	}
	
	public void save(FilePutter file) {
		
		file.i(race);
		file.chars(name);
		file.i(bannerI);
		file.bool(isRange);
		
		file.i(indus.length);
		for (Induvidual a : indus)
			a.save(file);
		target.save(file);
	}
	
	public Race race() {
		return RACES.all().get(race);
	}
	
	private void init(DIV_SPEC div, Induvidual ii, boolean training) {
		if (training) {
			set(ii, STATS.BATTLE().COMBAT_EXPERIENCE, div.experience());
			for (StatTraining tt : STATS.BATTLE().TRAINING_ALL)
				set(ii, tt.stat, div.training(tt));
		}
		

		for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
			set(ii, e.stat(), div.equip(e));
		}
	}



	private static void set(Induvidual ii, STAT s, double d) {

		double ex = d * s.indu().max(ii);
		if (ex != (int) ex && (ex - (int) ex) > RND.rFloat())
			ex++;
		ex = CLAMP.d(ex, 0, s.indu().max(ii));

		s.indu().set(ii, (int) ex);

	}

	public DIV_SPECImp makeSpec() {
		DIV_SPECImp spec = new DIV_SPECImp();
		
		spec.raceSet(race());
		spec.menSet(indus.length);
		
		for (EquipBattle e : STATS.EQUIP().BATTLE_ALL()) {
			
			double am = 0;
			for (Induvidual a : indus) {
				am += e.get(a);
			}
			am /= (indus.length*e.max());
			spec.equipSet(e, am);
		}
		
		double exp = 0;
		for (StatTraining t : STATS.BATTLE().TRAINING_ALL) {
			double am = 0;
			for (Induvidual a : indus) {
				am += t.stat.indu().getD(a);
				exp += STATS.BATTLE().COMBAT_EXPERIENCE.indu().getD(a);
			}
			am /= (indus.length);
			spec.trainingSet(t, am);
		}
		
		spec.experienceSet(exp/indus.length);
		return spec;
	}

}