package game.battle.thread.general;

import game.GAME;
import game.battle.Army;
import game.battle.formation.DivDeployer;
import settlement.main.SETT;
import settlement.room.main.throne.THRONE;
import snake2d.PathUtilOnline;
import snake2d.util.datatypes.COORDINATE;

public final class StrategosUtil{

	public final PathUtilOnline flooder = new PathUtilOnline(SETT.TWIDTH);
	public final DivDeployer deployer = new DivDeployer(flooder);
	public final UtilDeployer divDeployer = new UtilDeployer(this);
	
	public StrategosUtil() {

	}
	
	public COORDINATE getDestCoo() {
		return THRONE.coo();
	}

	public Army getArmy() {
		return GAME.ARMIES().enemy();
	}
	
}
