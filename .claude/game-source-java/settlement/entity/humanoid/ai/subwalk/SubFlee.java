package settlement.entity.humanoid.ai.subwalk;

import init.type.CAUSE_LEAVES;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.Humanoid.HumanoidResource;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISTATE;
import settlement.entity.humanoid.ai.main.AISUB;
import settlement.main.SETT;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

final class SubFlee extends AISUB.Simple{


	
	public SubFlee() {
		super("flee");
	}
	
	AISubActivation activate(Humanoid a, AIManager d, ENTITY other){
		a.speed.turn2(other.body(), a.body());
		return activate(a, d);
	}
	
	AISubActivation activate(Humanoid a, AIManager d, int iterations){
		d.subPathByte = (byte) (iterations+1);
		return activate(a, d);
	}
	
	@Override
	public AISubActivation activate(Humanoid a, AIManager d) {
		d.subPathByte = (byte) (2 + RND.rInt(5));
		d.subPathByte2 = (byte) (2 + RND.rInt(15));
		return super.activate(a, d);
	}
	
	@Override
	protected AISTATE resume(Humanoid a, AIManager d) {
		a.speed.turnWithAngel(RND.rFloat0(90));
		d.subPathByte--;
		if (SETT.TERRAIN().WATER.DEEP.is(a.tc())) {
			d.subPathByte2 --;
			if (d.subPathByte2 <= 0) {
				HumanoidResource.dead = CAUSE_LEAVES.DROWNED();
			}
		}
		
		if (d.subPathByte > 0) {
			if (RND.oneIn(3))
				return AI.STATES().jogCrazy.activate(a, d, 2f + RND.rFloat()*3);
			return AI.STATES().jog.activate(a, d, 2f + RND.rFloat()*3);
		}
		
		return null;
	}
	
	@Override
	protected AISTATE resumeInterrupted(Humanoid a, AIManager d, HEvent event) {
		return null;
	};
	
	@Override
	public boolean event(Humanoid a, AIManager d, HEventData e) {
		if (e.event == HEvent.COLLISION_UNREACHABLE) {
			DIR dd = a.speed.dir();
			if (!dd.isOrtho())
				dd = dd.next(1);
			for (int i = 0; i < 4; i++) {
				if (SETT.PATH().connectivity.is(a.tc(), dd)) {
					break;
				}
				dd = dd.next(2);
				//a.speed.turn90();
			}
			if (SETT.PATH().connectivity.is(a.tc(), dd)) {
				a.speed.setRaw(dd, 0.5);
			}
		}else if (e.event == HEvent.MEET_ENEMY) {
			a.speed.turn2(-e.norX, -e.norY);
			d.stateTimer = 10;
		}else if (e.event == HEvent.COLLISION_TILE) {
			double dx = e.norX;
			double dy = e.norY;
			if (RND.oneIn(4)) {
				for (int i = RND.rInt(4); i >= 1; i--) {
					double y = dy;
					dy = -dx;
					dx = y;
				}
			}
			a.speed.turn2(dx, dy);
			
			return true;
		}else if(e.event == HEvent.EXHAUST) {
			return super.event(a, d, e);
		}else if (e.event == HEvent.COLLISION_HARD) {
			return super.event(a, d, e);
		}
		return false;
	}

}
