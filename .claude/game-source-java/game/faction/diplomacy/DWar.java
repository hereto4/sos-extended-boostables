package game.faction.diplomacy;


import game.faction.FACTIONS;
import game.faction.FWorth;
import game.faction.Faction;
import game.faction.npc.FactionNPC;
import init.sprite.UI.UI;
import settlement.main.SETT;
import snake2d.LOG;
import snake2d.util.color.COLOR;
import snake2d.util.misc.CLAMP;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LISTE;
import util.text.D;
import world.WORLD;
import world.army.AD;
import world.entity.army.WArmy;
import world.map.regions.Region;
import world.region.RD;

public final class DWar extends DipStance{

	private static CharSequence ¤¤name = "Enemies";
	private static CharSequence ¤¤desc = "Enemies are at war and bent on destroying one another.";

	static {
		D.ts(DWar.class);
	}
	
	private final ArrayList<Faction> allies = new ArrayList<>(FACTIONS.MAX());
	private final ArrayList<Faction> enemies = new ArrayList<>(FACTIONS.MAX());
	private final Bitmap1D btmp = new Bitmap1D(FACTIONS.MAX(), false);
	
	DWar(LISTE<DipStance> all){
		super(all, "WAR", 0, 0, 0.8, false, false, false, ¤¤name, ¤¤desc, UI.icons().s.sword.createColored(COLOR.REDISH));
		new Debug();
	}
	
	@Override
	public boolean is(Faction faction, Faction other) {
		if (faction == other)
			return false;
		if (faction == null || other == null)
			return true;
		return super.is(faction, other);
	}
	
	public double offenseValue(Faction a) {
		return offenseValue(allies(a), enemies(a));
	}
	
//	public double offenseValue(LIST<Faction> newAllies, Faction newEnemy) {
//		LISTE<Faction> allies = enemies(newEnemy);
//		btmp.clear();
//		for (Faction f : allies)
//			btmp.set(f.index(), true);
//		for (Faction f : newAllies)
//			if (!btmp.get(f.index()))
//				allies.add(f);
//		
//		
//		return offenseValue(allies(a), enemies(a));
//	}
	
	public LISTE<Faction> allies(Faction faction){
		allies.clearSloppy();
		for (Faction a : FACTIONS.all()) {
			if (a == faction || DIP.get(faction, a).ally){
				allies.add(a);
			}
		}
		return allies;
	}
	
	public LISTE<Faction> enemies(Faction faction){
		enemies.clearSloppy();
		for (Faction f : all(faction))
			enemies.add(f);
		return enemies;
	}
	
	/**
	 * 
	 * @param team
	 * @param enemies
	 * @return values greater 
	 */
	public double offenseValue(LIST<? extends Faction> team, LIST<? extends Faction> enemies) {
		if (team.size() == 0)
			return 0;
		if (enemies.size() == 0)
			return 0;
		
		double offence = 1.0;
		double teamRegMin = Double.MAX_VALUE;
		
		for (Faction f : team) {
			offence += f.offensivePower();
			teamRegMin = Math.min(minRegDef(f), teamRegMin);
			if (SETT.INVADOR().invadingPending()) {
				offence -= FACTIONS.player().offensivePower();
			}
		}
		
		double eRegMin = Double.MAX_VALUE;
		double eOffence = 1;
		
		for (Faction f : enemies) {
			eOffence += f.offensivePower();
			eRegMin = Math.min(minRegDef(f), eRegMin);
			if (SETT.INVADOR().invadingPending()) {
				eOffence -= FACTIONS.player().offensivePower();
			}
		}

		if (log) {
			LOG.ln("team " + team.size() + " " + team.get(0).name);
			LOG.ln(AD.power().get( team.get(0)));
			LOG.ln(offence + " " + teamRegMin);
			LOG.ln("enemy " + enemies.size() + " " + enemies.get(0).name);
			LOG.ln(AD.power().get( enemies.get(0)) + " " + AD.power().get(FACTIONS.player()));
			LOG.ln(eOffence + " " + eRegMin);
		}
		
		if (offence > eOffence + eRegMin) {
			if (log) {
				LOG.ln("STRONG " + CLAMP.d((offence) / (eOffence+eRegMin) -1, 0, 1));
			}
			return CLAMP.d((offence) / (eOffence+eRegMin) -1, 0, 1);
		}else if (eOffence > offence + teamRegMin) {
			if (log) {
				LOG.ln("WEAK " + -CLAMP.d((eOffence) / (offence + teamRegMin) -1, 0, 1));
			}
			return -CLAMP.d((eOffence) / (offence + teamRegMin) -1, 0, 1);
		}
		return 0;
	}
	
	private double minRegDef(Faction f) {
		if (f.realm().regions() == 0)
			return 0;
		double def = Double.MAX_VALUE;
		for (int ri = 0; ri < f.realm().regions(); ri++) {
			Region reg = f.realm().region(ri);
			if (reg.faction() == FACTIONS.player())
				return 0;
			def = Math.min(def, RD.MILITARY().power.getD(reg));
		}
		return def;
		
	}
	
	/**
	 * 
	 * @param underAttack
	 * 0-1
	 * @return under how much pressure this faction currently is, measured in how much he will immediately loose if the war continues.
	 */
	public double distress(Faction underAttack) {
		
		double power = 0;
		double enemyPower = 0;
		btmp.clear();
		
		double totalWorth = 0;
		double lossesWorth = 0;
		
		enemies.clearSloppy();
		
		for (Faction a : FACTIONS.all()) {
			if (a == underAttack || DIP.get(underAttack, a).ally){
				btmp.set(a.index(), true);
				power += AD.power().get(a);
				enemies.add(a);
			}else if (a != underAttack && DIP.get(underAttack, a) == DIP.WAR()) {
				enemyPower += AD.power().get(a);
			}
			
		}
		
		double pd = (enemyPower + 1) / (power + 1);
		pd = CLAMP.d(pd, 0, 1);
				
		for (Faction f : enemies) {
			double loss = 0;
			totalWorth += FACTIONS.WORTH().regions.get(f);
			for (int ri = 0; ri < f.realm().regions(); ri++) {
				Region reg = f.realm().region(ri);
				
				double fort = RD.MILITARY().defensePower(reg);
				double def = RD.MILITARY().power.getD(reg);
				double pow = 0;
				double ee = 0;
				for (WArmy a : WORLD.ENTITIES().armies.fill(f.realm().region(ri))) {
					if (a.faction() != null && btmp.get(a.faction().index()))
						pow += AD.power().get(a);
					else if (DIP.WAR().is(f, a.faction())) {
						ee += AD.power().get(a);
					}
				}
				if (ee > pow + fort) {
					if (reg.capitol()) {
						loss = FACTIONS.WORTH().regions.get(f);
						break;
					}else {
						loss += FWorth.region(reg);
					}
				}else if (ee > pow + def) {
					loss += pd*0.5*FWorth.region(reg);
				}
				
			}
			lossesWorth += loss;
		}
		
		if (underAttack == FACTIONS.player() && SETT.INVADOR().invading())
			lossesWorth += FACTIONS.WORTH().faction();
		
		return CLAMP.d(lossesWorth/totalWorth, 0, 1);
	
	}
	
	private static boolean log = false;
	
	public double peaceValue(Faction a) {
		double o = offenseValue(a);
		if (o < 0)
			o *= 0.1;
		o -= distress(a);
		
		if (all(a).size() > 0) {
			o += distress(all(a).get(0));
		}
		return CLAMP.d(o, -1, 1);
	}
	
}
