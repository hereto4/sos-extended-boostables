package game.battle.formation;

import game.battle.util.Copyable;
import init.constant.Config;

public class DivPositionCopyable extends DivPositionImp implements Copyable<DivPositionCopyable>{
	
	public DivPositionCopyable() {
		super(Config.battle().MEN_PER_DIVISION);
	}
	
	@Override
	public void copy(DivPositionCopyable pos) {
		copyposition(pos);
	}
	
}
