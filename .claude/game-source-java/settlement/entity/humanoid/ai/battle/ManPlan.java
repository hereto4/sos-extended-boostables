package settlement.entity.humanoid.ai.battle;

import game.GAME;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.path.finders.SFinderSoldierManning.FINDABLE_MANNING;
import settlement.stats.STATS;
import snake2d.util.rnd.RND;
import util.text.D;

final class ManPlan extends AIPLAN.PLANRES{

	ManPlan(String key) {
		super(key);
	}

	private static CharSequence ¤¤Name = "¤Manning defenses";
	
	static {
		D.ts(ManPlan.class);
	}

	boolean shouldMan(Humanoid a, AIManager d) {
		if (a.division() != null && a.division().settings().mustering())
			return false;
		if (d.plan() == this && getResumer(d) != wait)
			return true;
		
		if (!SETT.PATH().finders.manning(a.indu().army()).has(a.tc()))
			return false;
		return true;
		
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		return wait.set(a, d);
	}
	
	private Resumer wait = new Resumer(¤¤Name) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			AISubActivation s = AI.SUBS().walkTo.serviceInclude(a, d, SETT.PATH().finders.manning(a.indu().army()), 100);
			if (s != null) {
				walk.set(a, d);
				return s;
			}
			return AI.SUBS().STAND.activateTime(a, d, 5);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			AISubActivation s = AI.SUBS().walkTo.serviceInclude(a, d, SETT.PATH().finders.manning(a.indu().army()), Integer.MAX_VALUE);
			if (s != null) {
				walk.set(a, d);
				return s;
			}
			if (!a.indu().player())
				STATS.BATTLE().ROUTING.indu().set(a.indu(), 1);
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private Resumer walk = new Resumer(¤¤Name) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return null;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			FINDABLE_MANNING f = SETT.PATH().finders.manning(a.indu().army()).getReserved(d.path.destX(), d.path.destY());
			if (f == null)
				return null;
			a.speed.setDirCurrent(f.faceDIR());
			if (!AI.modules().battle.moduleCanContinue(a, d)) {
				can(a, d);
				return null;
			}
			return stand.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			FINDABLE_MANNING f = SETT.PATH().finders.manning(a.indu().army()).getReserved(d.path.destX(), d.path.destY());
			if (f != null)
				f.findableReserveCancel();
			
		}
	};
	
	private Resumer stand = new Resumer(¤¤Name) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().STAND.activateTime(a, d, (int) (2+RND.rFloat()*2));
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (!AI.modules().battle.moduleCanContinue(a, d)) {
				can(a, d);
				return null;
			}
			FINDABLE_MANNING f = SETT.PATH().finders.manning(a.indu().army()).getReserved(d.path.destX(), d.path.destY());
			if (f == null) {
				return null;
			}
			if (f.needsWork())
				return work.set(a, d);
			return AI.SUBS().STAND.activateTime(a, d, (int) (2+RND.rFloat()*2));
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			FINDABLE_MANNING f = SETT.PATH().finders.manning(a.indu().army()).getReserved(d.path.destX(), d.path.destY());
			if (f != null)
				f.findableReserveCancel();
			
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.CHECK_MORALE) {
				if (a.indu().army() == GAME.ARMIES().enemy() &&  GAME.ARMIES().enemy().morale() < 0.2) {
					STATS.BATTLE().ROUTING.indu().set(a.indu(), 1);
					d.overwrite(a, AI.modules().battle.dessert);
					return false;
				}
				
			}
			return super.event(a, d, e);
		}
	};
	
	private Resumer work = new Resumer(¤¤Name) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().WORK_HANDS.activate(a, d, 1);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			FINDABLE_MANNING f = SETT.PATH().finders.manning(a.indu().army()).getReserved(d.path.destX(), d.path.destY());
			if (f == null)
				return null;
			f.work(5, a);
			if (!AI.modules().battle.moduleCanContinue(a, d)) {
				can(a, d);
				return null;
			}
			if (a.division() != null && a.division().settings().mustering()) {
				can(a, d);
				return null;
			}
			return stand.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			FINDABLE_MANNING f = SETT.PATH().finders.manning(a.indu().army()).getReserved(d.path.destX(), d.path.destY());
			if (f != null)
				f.findableReserveCancel();
			
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.CHECK_MORALE) {
				if (a.indu().army() == GAME.ARMIES().enemy() && GAME.ARMIES().enemy().morale() < 0.2) {
					STATS.BATTLE().ROUTING.indu().set(a.indu(), 1);
					d.overwrite(a, AI.modules().battle.dessert);
					return false;
				}
				
			}
			return super.event(a, d, e);
		}
	};
	
	@Override
	protected AISubActivation resume(Humanoid a, AIManager d) {
		return super.resume(a, d);
	}
	
	@Override
	public boolean notifyIfSubFails() {
		return false;
	}

	
}
