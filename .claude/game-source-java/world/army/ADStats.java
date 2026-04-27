package world.army;

import game.boosting.BSourceInfo;
import game.boosting.BoosterAbs;
import game.faction.FACTIONS;
import game.faction.Faction;
import game.time.TIME;
import init.sprite.UI.UI;
import init.value.GVALUES;
import settlement.stats.STATS;
import snake2d.util.misc.CLAMP;
import util.data.DOUBLE_O;
import util.data.DOUBLE_O.DOUBLE_OE;
import util.data.INT_O;
import util.data.INT_O.INT_OE;
import util.info.INFO;
import util.text.D;
import util.text.Dic;
import world.army.ADInit.Updater;
import world.battle.BattleListener;
import world.entity.army.WArmy;
import world.map.regions.Region;

public final class ADStats {

	private static CharSequence ¤¤Wins = "Victories";
	private static CharSequence ¤¤Defeats = "Defeats";
	private static CharSequence ¤¤kills = "Enemies Killed";
	private static CharSequence ¤¤losses = "Casualties";
	private static CharSequence ¤¤SiegeWon = "Sieges Won";
	private static CharSequence ¤¤WinsD = "Total amount of victories.";
	private static CharSequence ¤¤DefeatsD = "Total amount of defeats.";
	private static CharSequence ¤¤killsD = "Total amount of enemies killed.";
	private static CharSequence ¤¤lossesD = "Total amount of casualties sustained.";
	private static CharSequence ¤¤SiegeWonD = "Total amount of sieges won.";
	private static CharSequence ¤¤reputation = "Reputation";
	private static CharSequence ¤¤reputationD = "Based on previous and recent victories and defeats. Affects morale on the battlefield.";
	private static CharSequence ¤¤mercy = "Mercy";
	private static CharSequence ¤¤cruelty = "Cruelty";
	static {
		D.ts(ADStats.class);
	}
	
	public final ADStat wins;
	public final ADStat defeats;
	public final ADStat kills;
	public final ADStat losses;
	public final ADStat siegeWon;
	
	private final DOUBLE_OE<WArmy> scoreA;
	private final DOUBLE_OE<Faction> scoreF;
	private final DOUBLE_OE<Faction> mercy;
	private final DOUBLE_O<Faction> cruelty;
	
	ADStats(ADInit init){
		
		scoreA = init.dataA.new DataDouble("STATS_MORALE", new INFO(¤¤reputation, ¤¤reputationD));
		scoreF = init.dataT.new DataDouble("STATS_MORALE", new INFO(¤¤reputation, ¤¤reputationD));
		mercy = init.dataT.new DataDouble("BATTLE_MERCY", new INFO(¤¤mercy, ¤¤mercy)) {
			@Override
			public DOUBLE_OE<Faction> setD(Faction t, double d) {
				d = CLAMP.d(d, -1, 1);
				return super.setD(t, d);
			}
		};
		
		GVALUES.FACTION.push("BATTLES_MERCY", mercy.info().name, UI.icons().s.heart, mercy);
		
		cruelty = new DOUBLE_O<Faction>() {
			
			INFO info = new INFO(¤¤cruelty, ¤¤cruelty);
			
			@Override
			public double getD(Faction t) {
				return -mercy.getD(t);
			}
			
			@Override
			public INFO info() {
				return info;
			}
		};
		
		
		GVALUES.FACTION.push("BATTLES_REPUTATION", ¤¤reputation, UI.icons().s.arrowUp, scoreF);
		
		wins = new ADStat(init, "BATTLES_WON", ¤¤Wins, ¤¤WinsD);
		defeats = new ADStat(init, "BATTLES_LOST", ¤¤Defeats, ¤¤DefeatsD);
		kills = new ADStat(init, "BATTLES_ENEMIES_KILLED", ¤¤kills, ¤¤killsD);
		losses = new ADStat(init, "BATTLES_CASUALTIES", ¤¤losses, ¤¤lossesD);
		siegeWon = new ADStat(init, "BATTLES_SIEGES_WON", ¤¤SiegeWon, ¤¤SiegeWonD);
		
		AD.moraleFactors().add(new BoosterAbs<WArmy>(new BSourceInfo(¤¤reputation, UI.icons().s.crown), false) {
			
			@Override
			public double to() {
				return 1;
			}
			
			@Override
			protected double pget(WArmy o) {
				return rep().getD(o);
			}
			
			@Override
			public double from() {
				return 0;
			}

			@Override
			public double getValue(double input) {
				return input;
			}
		});
		
		AD.moraleFactors().add(new BoosterAbs<WArmy>(new BSourceInfo(¤¤reputation, Dic.¤¤global, UI.icons().s.crown), false) {
			
			@Override
			public double to() {
				return 1;
			}
			
			@Override
			protected double pget(WArmy o) {
				return repF().getD(o.faction());
			}
			
			@Override
			public double getValue(double input) {
				return input;
			}
			
			@Override
			public double from() {
				return 0;
			}
		});
		
		init.updaters.add(new Updater() {
			
			@Override
			public void update(Faction f, double ds) {
				double d = scoreF.getD(f);
				if (d < 0) {
					d += ds/(TIME.secondsPerDay()*10.0);
					d = CLAMP.d(d, d, 0);
				}else if (d > 1) {
					d -= ds/(TIME.secondsPerDay()*20);
					d = CLAMP.d(d, 0, d);
				}
				scoreF.setD(f, d);
			}
			
			@Override
			public void update(WArmy a, double ds) {
				double d = scoreA.getD(a);
				if (d < 0) {
					d += ds/(TIME.secondsPerDay()*5.0);
					d = CLAMP.d(d, d, 0);
				}else if (d > 1) {
					d -= ds/(TIME.secondsPerDay()*10);
					d = CLAMP.d(d, 0, d);
				}
				scoreA.setD(a, d);
			}
		});
		
		new BattleListener() {
			
			@Override
			public void siege(Faction attacker, Region reg) {
				siegeWon.f.inc(attacker, 1);
			}
			
			@Override
			public void siege(WArmy attacker, Region reg) {
				siegeWon.a.inc(attacker, 1);
			}
			
			@Override
			public void battle(WArmy a, boolean victory, int losses, int kills, Faction againsts) {
				ADStats.this.kills.a.inc(a, kills);
				ADStats.this.losses.a.inc(a, losses);
				if (victory) {
					double d = 0.25*(double)kills/(AD.men(null).get(a)+1);
					double s = scoreA.getD(a);
					s += d;
					s = CLAMP.d(s, 0, 1);
					scoreA.setD(a, s);
					if (kills > 0)
						ADStats.this.wins.a.inc(a, 1);
				}else {
					double d = (double)losses/(AD.men(null).get(a)+1);
					double s = scoreA.getD(a);
					s -= d;
					s = CLAMP.d(s, 0, 1);
					scoreA.setD(a, s);
					ADStats.this.defeats.a.inc(a, 1);
				}
			}
			
			@Override
			public void battle(Faction a, boolean victory, int losses, int kills, Faction againsts) {
				report(a, victory, losses, kills);
			}
		};
	}
	
	public void report(Faction a, boolean victory, int losses, int kills) {
		ADStats.this.kills.f.inc(a, kills);
		ADStats.this.losses.f.inc(a, losses);
		double men = 1+AD.men(null).faction(a) + losses;
		if (a == FACTIONS.player())
			men += STATS.BATTLE().DIV.stat().data().get(null);
		
		if (victory) {
			double d = 0.25*(double)kills/men;
			double s = scoreF.getD(a);
			s += d;
			s = CLAMP.d(s, -1, 1);
			scoreF.setD(a, s);
			ADStats.this.wins.f.inc(a, 1);
		}else {
			double d = (double)losses/men;
			double s = scoreF.getD(a);
			s -= d;
			s = CLAMP.d(s, -1, 1);
			scoreF.setD(a, s);
			ADStats.this.defeats.f.inc(a, 1);
		}
	}
	
	public DOUBLE_OE<WArmy> rep() {
		return scoreA;
	}
	
	public DOUBLE_OE<Faction> repF() {
		return scoreF;
	}
	
	public DOUBLE_OE<Faction> mercy() {
		return mercy;
	}
	
	public DOUBLE_O<Faction> cruelty() {
		return cruelty;
	}
	
	public static class ADStat extends INFO{
		
		private INT_OE<WArmy> a;
		private INT_OE<Faction> f;
		
		ADStat(ADInit init, String key, CharSequence name, CharSequence desc){
			super(name, desc);
			this.a = init.dataA.new DataInt(key, null, 10000);
			f = init.dataT.new DataInt(key);
			GVALUES.FACTION.pushI(key, name, UI.icons().s.sword, f);
		}
		
		public INT_O<WArmy> a(){
			return a;
		}
		
		public INT_O<Faction> f(){
			return f;
		}
		
	}
	
}
