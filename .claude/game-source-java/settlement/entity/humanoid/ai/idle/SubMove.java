package settlement.entity.humanoid.ai.idle;

import static settlement.main.SETT.PATH;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISTATE;
import settlement.entity.humanoid.ai.main.AISUB;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

class SubMove extends AISUB.Simple{
	
	public SubMove(String key) {
		super(key);
	}
	
	@Override
	public AISTATE resume(Humanoid a, AIManager d) {
		
		
		
		if (!a.speed.isZero()) {
			return AI.STATES().STOP.instant(a, d);
		}
		
		
		switch(d.subByte) {
		case 0: 
			d.subByte = (1);
			return AI.STATES().WALK2.cTile(a, d);
		case 1:
			DIR di = DIR.ALL.rnd();
			int x1 = a.physics.tileC().x();
			int y1 = a.physics.tileC().y();
			d.subByte = 2;
			double c = PATH().coster.player.getCost(x1, y1, x1+di.x(), y1+di.y());
			if (c > 0 && c <= 1 && PATH().finders.isGoodTileToStandOn(x1+di.x(), y1+di.y(), a)) {
				return AI.STATES().WALK2.dirTile(a, d, di);
			}else if(RND.rBoolean()) {
				return AI.STATES().STAND.activate(a, d, 1.0f + RND.rFloat(5.0));
			}
		case 2:
			d.subByte = 100;
			return AI.STATES().STOP.activate(a, d);
		default:
			return null;
		}

	}
	
}
