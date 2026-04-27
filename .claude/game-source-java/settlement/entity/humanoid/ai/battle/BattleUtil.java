package settlement.entity.humanoid.ai.battle;

import game.battle.div.Div;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AIManager;
import snake2d.util.datatypes.COORDINATE;

public final class BattleUtil {
	
	static double getAttackPause(Humanoid a, AIManager d) {
		//double de = (BOOSTABLES.BATTLE().ATTACK_RATE.get(a.indu())+1.0)/(BOOSTABLES.BATTLE().ATTACK_RATE.max(Induvidual.class)+1.0); 
		return 1.0;
	}
	
	static boolean isInPosition(COORDINATE dest, Humanoid a, AIManager d) {
		return dest.isSameAs(a.physics.body().cX(), a.physics.body().cY());
	}
	
	static boolean hasSpot(Humanoid a, AIManager d) {
		Div div = a.division();
		return div != null && div.reporter.posHas(a);
	}
	
	static boolean shouldMoveIntoDivPosition(Humanoid a, AIManager d) {
		if (a.division() == null)
			return false;
		if (!a.division().settings().mustering())
			return false;
		if (a.division().settings().moppingUp())
			return false;
		if (!a.division().reporter.posHas(a))
			return false;
		return true;
	}
	
	

	
}
