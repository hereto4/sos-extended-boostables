package world.battle.spec;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.sets.LIST;
import world.army.ADSupplies.ADArtillery;

public interface WBattleSide {

	public COORDINATE coo();
	public int men();
	public int losses();
	public int lossesRetreat();
	public LIST<WBattleUnit> units();
	public int artillery(ADArtillery a);
	public double powerBalance();

}
