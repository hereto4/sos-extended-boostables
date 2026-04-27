package game.battle.state;

import game.save.GameLoader;
import init.paths.PATHS;
import world.battle.spec.BATTLE_RESULT;

public abstract class BattleStateExiter{
	
	public abstract void afterExit(BattleStateResult res);
	
	public void exit(BATTLE_RESULT res, int plosses, int elosses) {
		final BattleStateResult r = new BattleStateResult(res, elosses, plosses);
		new GameLoader(PATHS.local().save().get("__beforeBattle")){
			
			@Override
			public void doAfterSet() {
				
				afterExit(r);

			}
			
		}.set();
	}
}