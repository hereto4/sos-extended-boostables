package game.battle.util;

import game.GAME;
import game.boosting.Boostable;

public final class BattleUtil{

	public final Power power = new Power();
	private final Boosts boosts = new Boosts();
	public final DivTypes types = new DivTypes();
	public final FightingUtil fight = new FightingUtil();
	public final ArmyFormations formations = new ArmyFormations();
	
	public BattleUtil(GAME game) {
		super();
	}

	public double boost(DIV_SPEC div, Boostable bo) {
		return boosts.get(div, bo);
	}

	public double boostMax(Boostable bo) {
		return boosts.max(bo);
	}
	
}
