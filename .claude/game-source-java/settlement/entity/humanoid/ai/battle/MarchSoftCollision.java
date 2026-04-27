package settlement.entity.humanoid.ai.battle;

import game.GAME;
import game.boosting.BOOSTABLES;
import game.time.TIME;
import init.constant.C;
import settlement.entity.ENTITY;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISTATE;
import settlement.entity.humanoid.ai.main.AISUB;
import settlement.entity.humanoid.spirte.HSprites;
import settlement.stats.STATS;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.rnd.RND;

final class MarchSoftCollision extends AISUB.Resumable{
	
	AISubActivation initReady(AIManager d, Humanoid a, ENTITY other, double norX, double norY, double faceDot,
			double momentum) {
		d.subPathByte = 5;
		if (other instanceof Humanoid) {
			Humanoid o = (Humanoid) other;
			if (o.indu().hostile() != a.indu().hostile()) {
				d.otherEntitySet((Humanoid) other);
			}
		}
	
		return activate(a, d, gg(a,d));
		
	}
	
	
	
	AISubActivation initCoo(AIManager d, Humanoid a, ENTITY other, int px, int py) {
		d.subPathByte = 5;
		if (other instanceof Humanoid) {
			Humanoid o = (Humanoid) other;
			if (o.indu().hostile() != a.indu().hostile()) {
				d.otherEntitySet((Humanoid) other);
			}
		}
		return activate(a, d, gg(a,d));
		
	}
	
	protected MarchSoftCollision() {
		super("MarchSoftColl");
	}

	@Override
	protected AISTATE init(Humanoid a, AIManager d) {
		
		return null;
	}

	private Resumer gg(Humanoid a, AIManager d) {
		if (a.division() != null)
			a.speed.setDirCurrent(a.division().dir());
		if (BattleUtil.shouldMoveIntoDivPosition(a, d)) {
			double m = COORDINATE.tileDistance(a.body().cX(), a.body().cY(), a.division().reporter.getPixel(a));
			if (m > C.TILE_SIZEH) {
				return push;
			}
			//retreat needs to do path as well, and collision with friends is probably not needed.
		}
		return brakes;
		
	}
	

	
	private final Resumer push = new ResumerB() {
		
		@Override
		protected AISTATE setAction(Humanoid a, AIManager d) {
			d.subPathByte --;
			return AI.STATES().PUSH_TO.move(a, d, a.division().reporter.getPixel(a).x(), a.division().reporter.getPixel(a).y(), 1.0, 0.75);
			
		}
		
		@Override
		protected AISTATE res(Humanoid a, AIManager d) {
			if (d.subPathByte < 0 && d.subByte >= 0) {
				AIPLAN p = AI.modules().battle.escape.plan(a, d);
				if (p != null)
					return d.resumeOtherPlanState(a, p);
			}
				
			return strike.set(a, d);
		}
		
	};
	
	private final Resumer brakes = new ResumerB() {
		
		@Override
		protected AISTATE setAction(Humanoid a, AIManager d) {
			d.subByte = 0;
			if (a.division() != null)
				a.speed.setDirCurrent(a.division().dir());
			return AI.STATES().STAND_SWORD.activate(a, d, RND.rFloat0(0.5));
		}
		
		@Override
		protected AISTATE res(Humanoid a, AIManager d) {
			return strike.set(a, d);
		}
		
	};
	
	private final Resumer strike = new ResumerB() {
		private final AISTATE state = AI.STATES().SWORD.strike;
		private final float time = (float) HSprites.SWORD_OUT.time;

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			a.spriteTimer = 0;
			d.stateTimer = time;
			a.speed.magnitudeTargetSet(0);
			
			
			
			return state;
		}

		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			return strike2.set(a, d);
		}

	};

	private final Resumer strike2 = new ResumerB() {

		private final AISTATE state = AI.STATES().SWORD.strikeIn;
		private final float time = (float) HSprites.SWORD_IN.time;

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			
			if (d.otherEntity() != null) {
				int dist = a.body().getDistance(d.otherEntity().body());
				if (dist <= (a.body().width() + d.otherEntity().body().width()) / 2 + 24) {
					Humanoid enemy = d.otherEntity();
					GAME.battle().fight.attack(a, enemy);
					
					
				}
			}

			a.spriteTimer = 0;
			d.stateTimer = time;
			a.speed.magnitudeTargetSet(0);
		
			return state;
		}



		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			return null;
		}

	};
	
	private abstract class ResumerB extends Resumer {
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			switch(e.event) {
			case COLLISION_SOFT:
				
				a.speed.setPrevDir();
				
//				if (a.division() != null)
//					a.speed.setDirCurrent(a.division().dir());

				Resumer ss = gg(a, d);
				if (getResumer(a, d) != push && ss != push)
					d.overwrite(a, ss.set(a, d));
				else if (a.division() != null)
					a.speed.setDirCurrent(a.division().dir());
				else
					a.speed.setPrevDir();
				
				return true;
			case EXHAUST:
				if (RND.oneIn(BOOSTABLES.PHYSICS().STAMINA.get(a.indu())*8)) {
					
					STATS.NEEDS().EXHASTION.indu().inc(a.indu(), 1);
				}
				return false;
			case MEET_ENEMY:
				a.speed.setPrevDir();
				d.otherEntitySet((Humanoid) e.other);
				ss = gg(a, d);
				if (getResumer(a, d) != push && ss != push)
					d.overwrite(a, ss.set(a, d));
				else if (a.division() != null)
					a.speed.setDirCurrent(a.division().dir());
				else
					a.speed.setPrevDir();
				
				return false;
			case MEET_HARMLESS:
				
				return false;
				
			default :
				return InterBattle.listener.event(a, d, e);
			}
			
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
			switch(e.type) {
			case COLLIDES:
				return 1;
			case WILL_COLLIDE_WITH:{
				if (e.other instanceof Humanoid && ((Humanoid) e.other).indu().hostile() != a.indu().hostile()) {
					return 1;
				}
				if (TIME.currentSecond()- d.lastCollision < 3)
					return 1;
				
//				if (InterBattle.listener.poll(a, d, e) == 0)
//					return STATS.BATTLE().DIV.get(a) != null && STATS.BATTLE().DIV.get(a).status().isFighting() && !STATS.BATTLE().DIV.get(a).settings().moppingUp() ? 1 :0;
				return 0;
			}
				
			
			default :
				return InterBattle.listener.poll(a, d, e);
			}
		}
		
	}
	
}
