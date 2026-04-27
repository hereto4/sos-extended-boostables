package settlement.entity.humanoid.ai.battle;

import static settlement.main.SETT.PATH;

import game.GAME;
import game.battle.div.Div;
import init.type.CAUSE_LEAVES;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.HPoll;
import settlement.entity.humanoid.HPoll.HPollData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.stats.STATS;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

class PlanRout extends AIPLAN.PLANRES{
	
	public PlanRout(String key) {
		super(key);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		return run.set(a, d);
	}
	
	private final Resumer run = new Resumer("Routing") {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			STATS.BATTLE().ROUTING.indu().set(a.indu(), 1);
			Div div = a.division();
			if (div != null) {
				GAME.ARMIES().factors.reportRout(div);
				int di = RND.rInt(DIR.ALL.size());
				for (int i = 0; i < DIR.ALL.size(); i++) {
					DIR dir = DIR.ALL.getC(di+i);
					if (!div.status().threat(dir) && !div.status().threat(dir.next(1)) && !div.status().threat(dir.next(-1))) {
						a.setDivision(null);
						a.speed.turn2(dir);
						return AI.SUBS().walkTo.run_arround_crazy(a, d, 5);
					}
				}
				a.setDivision(null);
			}
			
			a.speed.turn90().turn90();
			a.speed.turnWithAngel(RND.rFloat0(20));
			return AI.SUBS().walkTo.run_arround_crazy(a, d, 5);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (RND.oneIn(5))
				return path.set(a, d);
			else
				return surrendered.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	
	private final Resumer path = new Resumer("Routing") {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			if (PATH().finders.entity.findExitNoEnemies(a, a.physics.tileC().x(), a.physics.tileC().y(), d.path, Integer.MAX_VALUE)) {
				return AI.SUBS().walkTo.pathRun(a, d);
			}
			return run.set(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			AIManager.dead = CAUSE_LEAVES.DESERTED();
			return AI.SUBS().STAND.activate(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
	};
	
	private final Resumer surrendered = new Resumer("Surrendered") {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			d.planByte1 = 0;
			return AI.SUBS().LAY.activateTime(a, d, 10);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			d.planByte1 ++;
			if (d.planByte1 >= 32) {
				AIManager.dead = CAUSE_LEAVES.DESERTED();
				return AI.SUBS().LAY.activateTime(a, d, 10);
			}
			
			if (a.indu().army() == GAME.ARMIES().player()) {
				if (GAME.ARMIES().enemy().men() == 0) {
					return path.set(a, d);
				}
			}
			return AI.SUBS().LAY.activateTime(a, d, 10);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
		}
		
		@Override
		public double poll(Humanoid a, AIManager d, HPollData e) {
	
			return 0;
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			return false;
		}
		
	};
	
	@Override
	public boolean event(Humanoid a, AIManager d, HEventData e) {
		if (e.event == HEvent.COLLISION_TILE && !SETT.TILE_BOUNDS.holdsPoint(e.tx, e.ty)) {
			a.helloMyNameIsInigoMontoyaYouKilledMyFatherPrepareToDie();
			return false;
		}
			
		return super.event(a, d, e);
	}
	
	@Override
	public double poll(Humanoid a, AIManager d, HPollData e) {
		if (e.type == HPoll.DEFENCE_SKILL)
			return 0;
		return super.poll(a, d, e);
	}
	
}
