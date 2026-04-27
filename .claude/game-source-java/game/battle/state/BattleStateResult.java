package game.battle.state;

import game.GAME;
import game.battle.div.Div;
import init.constant.Config;
import init.race.RACES;
import init.type.HTYPES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import settlement.stats.Induvidual;
import settlement.stats.STATS;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import world.battle.WBattles;
import world.battle.spec.BATTLE_RESULT;

public final class BattleStateResult {

	public final Induvidual[][] playerSurvivors = new Induvidual[Config.battle().DIVISIONS_PER_ARMY][];
	public final int[] enemySurvivors = new int[Config.battle().DIVISIONS_PER_ARMY];
	public final int[] enemyCaptured = new int[RACES.all().size()];
	public final BATTLE_RESULT result;
	public final int playerLosses;
	public final int enemyLosses;
	
	BattleStateResult(BATTLE_RESULT result, int enemydead, int playerdead){
		this.result = result;
		int[] count = new int[Config.battle().DIVISIONS_PER_ARMY];
		this.playerLosses = playerdead;
		this.enemyLosses = enemydead;
		if (result != BATTLE_RESULT.VICTORY) {
			int losses = (int) Math.ceil(GAME.ARMIES().enemy().men()*WBattles.retreatPenalty);
			double dlosses = (double)losses/(GAME.ARMIES().player().men()+1.0);
			for (Div d : GAME.ARMIES().player().divisions()) {
				int am = (int) (STATS.BATTLE().DIV.stat().div().get(d)*dlosses);
				if (d.status().isFighting())
					am += (STATS.BATTLE().DIV.stat().div().get(d)*0.75);
				am = CLAMP.i(am, 0, STATS.BATTLE().DIV.stat().div().get(d));
				count[d.indexArmy()] = am;
			}
			for (Div d : GAME.ARMIES().player().divisions()) {
				playerSurvivors[d.indexArmy()] = new Induvidual[ STATS.BATTLE().DIV.stat().div().get(d) - count[d.indexArmy()]];
				count[d.indexArmy()] = 0;
			}
		}else {
			for (Div d : GAME.ARMIES().player().divisions()) {
				playerSurvivors[d.indexArmy()] = new Induvidual[STATS.BATTLE().DIV.stat().div().get(d)];
			}
		}
		
		
		
		ENTITY[] es = SETT.ENTITIES().getAllEnts();
		for (ENTITY e : es) {
			if (e instanceof Humanoid) {
				Humanoid h = (Humanoid) e;
				Div d = STATS.BATTLE().DIV.get(h);
				if (d == null) {
					if (h.indu().hType() == HTYPES.ENEMY())
						enemyCaptured[h.race().index]++;
					
				} else {
					
					if (d.index() >= Config.battle().DIVISIONS_PER_ARMY) {
						
						if (result == BATTLE_RESULT.VICTORY && RND.rBoolean()) {
							enemyCaptured[h.race().index]++;
						}else {
							enemySurvivors[d.indexArmy()] ++;
						}
						
					}else {
						if (count[d.indexArmy()] >= playerSurvivors[d.indexArmy()].length) {
							
						}else {
							playerSurvivors[d.indexArmy()][count[d.indexArmy()]++] = h.indu();
						}
						
					}
				}
			}
		}
		
		wash();
	}
	
	private void wash() {
		for (Div d : GAME.ARMIES().player().divisions()) {
			wash(d);
		}
	}
	
	
	private void wash(Div div) {
		Induvidual[] ins = playerSurvivors[div.indexArmy()];
		int am = 0;
		for (Induvidual ii : ins) {
			if (ii != null) {
				am++;
			}
		}
		if (am == ins.length)
			return;
		
		Induvidual[] nins = new Induvidual[ins.length];
		am = 0;
		System.err.println("BattleResult");
		System.err.println(result);
		System.err.println(div.indexArmy());
		System.err.println(STATS.BATTLE().DIV.stat().div().get(div));
		
		System.err.println(ins.length + " " + am);

		
		for (Induvidual ii : ins) {
			if (ii != null) {
				nins[am++] = ii;
			}
		}
		playerSurvivors[div.indexArmy()] = nins;
		
	}
	
	
}
