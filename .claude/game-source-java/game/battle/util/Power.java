package game.battle.util;

import java.nio.file.Path;

import game.GAME;
import game.battle.div.Div;
import game.battle.util.DIV_SPEC.DIV_SPECImp;
import game.boosting.BOOSTABLES;
import game.boosting.Boostable;
import game.faction.Faction;
import init.constant.C;
import init.race.RACES;
import init.race.Race;
import settlement.stats.STATS;
import settlement.stats.colls.StatsBattle.StatTraining;
import settlement.stats.equip.EquipBattle;
import settlement.stats.equip.EquipRange;
import snake2d.util.gui.GUI_BOX;
import snake2d.util.misc.ACTION.ACTION_O;
import util.gui.misc.GBox;
import util.gui.misc.GText;
import util.info.GFORMAT;
import util.text.D;
import util.text.Dic;
import world.entity.army.WArmy;

public final class Power {
	
	public static CharSequence ¤¤desc = "The overall power of a battle unit. Divided into different attack and defence types. The total power is an indication of how well the unit will perform in a fight, but in practice each type determines the outcome.";
	private static CharSequence ¤¤attack = "attack";
	private static CharSequence ¤¤defence = "defence";
	private static CharSequence ¤¤morale = "morale";
	private static CharSequence ¤¤mass = "mass";
	private static CharSequence ¤¤speed = "speed";
	private static CharSequence ¤¤charge = "charge";
	
	private static CharSequence ¤¤ranged = "ranged";
	
	
	
	static {
		D.ts(Power.class);
	}
	
	public final double HIGH_POWER = 5.0;
	
	private double minPower = -1;
	private double maxPI = -1.0;
	private double bestRanged = 1;
	
	Power(){
		GAME.saver().onAfterLoad(new ACTION_O<Path>() {

			@Override
			public void exe(Path t) {
				maxPI = -1;
			}
		});
	}

	public double get(DIV_SPEC div) {
		init();	
		
		double d = pget(div);
		d -= minPower;
		d *= maxPI;
		return div.men()*(1+d);
	}
	
	public void hover(GUI_BOX box, DIV_SPEC spec) {
		GBox b = (GBox) box;
		
		b.title(Dic.¤¤Power);
		b.text(¤¤desc);
		
		double att = attack(spec);
		double def = defence(spec);
		GText t;
		
		b.NL(8);
		b.textLL(¤¤attack);
		b.tab(6);
		t = b.text();
		t.add('+');
		b.add(GFORMAT.f(t, att));
		b.NL();
		
		b.textLL(¤¤defence);
		b.tab(6);
		t = b.text();
		t.add('+');
		b.add(GFORMAT.f(t, def));
		b.NL();
		
		b.textLL(¤¤morale);
		b.tab(6);
		t = b.text();
		t.add('*');
		b.add(GFORMAT.f(t, bo(spec, BOOSTABLES.BATTLE().MORALE)));
		b.NL();
		
		b.textLL(¤¤charge);
		b.tab(6);
		t = b.text();
		t.add('+');
		b.add(GFORMAT.f(t, att*bo(spec, BOOSTABLES.BATTLE().CHARGE)));
		b.NL();
		
		b.textLL(¤¤mass);
		b.tab(6);
		t = b.text();
		t.add('*');
		b.add(GFORMAT.f(t, (1 + 0.1*bo(spec, BOOSTABLES.PHYSICS().MASS))));
		b.NL();
		
		b.textLL(¤¤speed);
		b.tab(6);
		t = b.text();
		t.add('*');
		b.add(GFORMAT.f(t, (1 + 0.1*bo(spec, BOOSTABLES.PHYSICS().SPEED))));
		b.NL();
		
		b.textLL(¤¤ranged);
		b.tab(6);
		t = b.text();
		t.add('+');
		b.add(GFORMAT.f(t, range(spec)));
		b.NL();
		
		b.textLL(Dic.¤¤Soldiers);
		b.tab(6);
		t = b.text();
		t.add('*');
		b.add(GFORMAT.i(t, spec.men()));
		b.NL();
		
		b.tab(6);
		t = b.text();
		b.add(GFORMAT.f0(t, get(spec)));
		b.NL();
		
		
		
		b.NL();
		
	}
	
	private double pget(DIV_SPEC div) {
		
		double attack = attack(div);
		double defence = defence(div) + defenceDir(div)*0.5;
		
		double tot = attack + defence;
		tot *= bo(div, BOOSTABLES.BATTLE().MORALE);
		
		tot += attack*GAME.battle().boost(div, BOOSTABLES.BATTLE().CHARGE)/(1.0);
		
		tot *= 1 + 0.1*(bo(div, BOOSTABLES.PHYSICS().MASS)-1);
		tot *= 1 + 0.25*(bo(div, BOOSTABLES.PHYSICS().SPEED)-1);
		
		tot += range(div);
		
		return tot;
	
	}
	
	private double attack(DIV_SPEC div) {
		double base = bo(div, BOOSTABLES.BATTLE().OFFENCE);
		double blunt = bo(div, BOOSTABLES.BATTLE().BLUNT_ATTACK);
		double res = blunt;
		for (int di = 0; di < BOOSTABLES.BATTLE().DAMAGES.size(); di++) {
			res += blunt*bo(div, BOOSTABLES.BATTLE().DAMAGES.get(di).attack)/BOOSTABLES.BATTLE().DAMAGES.size();
		}
		res += base;
		
		return res;
	}
	
	private double defence(DIV_SPEC div) {
		double base = bo(div, BOOSTABLES.BATTLE().DEFENCE) + bo(div, BOOSTABLES.BATTLE().FORMATION)*0.5;
		double blunt = bo(div, BOOSTABLES.BATTLE().BLUNT_DEFENCE);
		double res = 1;
		for (int di = 0; di < BOOSTABLES.BATTLE().DAMAGES.size(); di++) {
			res += bo(div, BOOSTABLES.BATTLE().DAMAGES.get(di).defence)/BOOSTABLES.BATTLE().DAMAGES.size();
		}
		res *= blunt; 
		return res + base;
	}
	
	private double defenceDir(DIV_SPEC div) {
		double base = bo(div, BOOSTABLES.BATTLE().DEFENCE) + bo(div, BOOSTABLES.BATTLE().FORMATION)*0.5;
		double blunt = bo(div, BOOSTABLES.BATTLE().BLUNT_DEFENCE_DIR);
		double res = 1;
		for (int di = 0; di < BOOSTABLES.BATTLE().DAMAGES.size(); di++) {
			res += bo(div, BOOSTABLES.BATTLE().DAMAGES.get(di).defenceDir)/BOOSTABLES.BATTLE().DAMAGES.size();
		}
		res *= blunt; 
		return res + base;
	}
	
	
	private double range(DIV_SPEC div) {
		
		EquipRange rr = best(div);
		if (rr == null)
			return 0;
		
		double ref = rr.ref(div.equip(rr), GAME.battle().boost(div, rr.boostable));
		
		return range(rr, ref);

		
		
	}
	
	public double range(EquipRange rr, double ref) {
		
		double hits = rr.projectile.range(0, ref)/(rr.projectile.reloadSeconds(ref)*C.TILE_SIZE*(1.0+BOOSTABLES.PHYSICS().SPEED.baseValue*4));
		
		
		double base = hits;
		double blunt = rr.projectile.bluntDamage(ref)/(1+BOOSTABLES.BATTLE().BLUNT_ATTACK.baseValue);
		double res = blunt;

		for (int di = 0; di < BOOSTABLES.BATTLE().DAMAGES.size(); di++) {
			res += blunt*(rr.projectile.damage(di, ref)/((1+BOOSTABLES.BATTLE().DAMAGES.get(di).attack.baseValue)*BOOSTABLES.BATTLE().DAMAGES.size()));
		}
		res *= base;
		res *= 1 + rr.projectile.areaAttack(ref);
		res *=  0.2 + 0.8*rr.projectile.accuracy(ref);
		return res;
		
		
	}
	
	
	private double bo(DIV_SPEC div, Boostable b) {
		return  GAME.battle().boost(div, b)/(1.0+b.baseValue);
	}
	
	private EquipRange best(DIV_SPEC div) {
		double max = 0;
		EquipRange b = null;
		for (int ei = 0; ei < STATS.EQUIP().RANGED().size(); ei++) {
			EquipRange rr = STATS.EQUIP().RANGED().get(ei);
			if (div.equip(rr) > 0) {
				double ref = rr.ref(div.equip(rr), GAME.battle().boost(div, rr.boostable));
				double m = range(rr, ref);
				if (m > max) {
					max = m;
					b = rr;
				}
			}
				
		}
		return b;
	}
	
	public double bestRangedPower() {
		return bestRanged;
	}
	
	private void init() {
		if (maxPI >= 0)
			return;
		
		
		DIV_SPECImp spec = new DIV_SPECImp();
		
		double minAverage = 0;
		for (int ri = 0; ri < RACES.playable().size(); ri++) {
			
			Race r = RACES.playable().get(ri);
			spec.clear(r);
			spec.menSet(1);
			minAverage += pget(spec);
			
			
		}
		minAverage /= RACES.playable().size();
		minPower = minAverage;
		
		double highAverage = 0;
		for (int ri = 0; ri < RACES.playable().size(); ri++) {
			
			Race r = RACES.playable().get(ri);
			spec.clear(r);
			spec.menSet(1);
			spec.experienceSet(0.5);
			
			double am = 0;
			double pp = 0;
			for (int si = 0; si < GAME.battle().types.ALL().size(); si++) {
				DivType t = GAME.battle().types.ALL().get(si);
				if (t.valid(r)) {
					am += t.occurence;
					spec.copySettings(t);
					pp += pget(spec)*t.occurence;
				}
			}
			
			double m = minAverage;
			if (am > 0) {
				m = Math.max(m, pp/am);
			}
			
			highAverage += m;
		}
		highAverage /= RACES.playable().size();
		double delta = highAverage-minAverage;
		maxPI = HIGH_POWER/delta;
		
		
		
		bestRanged = 0;
		for (EquipRange r : STATS.EQUIP().RANGED()) {
			bestRanged = Math.max(bestRanged, range(r, r.ref(1.0, GAME.battle().boostMax(r.boostable))));
			
		}
		
	}

	
	private Div sDiv;
	private final DIV_SPEC dstats = new DIV_SPEC() {
		
		@Override
		public double training(StatTraining tr) {
			return tr.stat.div().getD(sDiv);
		}
		
		@Override
		public double equip(EquipBattle e) {
			return e.stat().div().getD(sDiv);
		}
		
		@Override
		public Race race() {
			return sDiv.info.race();
		}
		
		@Override
		public int men() {
			return sDiv.menNrOf();
		}
		
		@Override
		public Faction faction() {
			return sDiv.army().faction();
		}
		
		@Override
		public double experience() {
			return STATS.BATTLE().COMBAT_EXPERIENCE.div().getD(sDiv);
		}

		@Override
		public CharSequence name() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int bannerI() {
			// TODO Auto-generated method stub
			return 0;
		}
	};
	
	public double get(Div div) {
		sDiv = div;
		return get(dstats);
	}
	
	public double get(WArmy a) {
		int am = 0;
		for (int di = 0; di < a.divs().size(); di++) {
			am += get(a.divs().get(di));
		}
		return am;
	}
	
	
}
