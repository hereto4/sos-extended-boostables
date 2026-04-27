package settlement.entity.humanoid.ai.types.prisoner;

import game.GAME;
import game.time.TIME;
import init.type.CAUSE_LEAVES;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.law.execution.ExecutionStation;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import snake2d.util.rnd.RND;

class Executed extends AIPLAN.PLANRES{

	
	public Executed() {
		super("prisExe");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		ExecutionStation s = SETT.ROOMS().EXECUTION.exectuionReserve();
		if (s == null)
			return null;
		d.planTile.set(s.coo());
		return walk.set(a, d);
		
	}
	
	private final Resumer walk = new Resumer(LAW.process().execution.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			AISubActivation s = AI.SUBS().walkTo.cooFull(a, d, d.planTile);
			if (s != null)
				return s;
			cancel(a, d);
			return null;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return ready.set(a, d);
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
	
	private final Resumer ready = new Resumer(LAW.process().execution.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			ExecutionStation s = SETT.ROOMS().EXECUTION.executionSpot(d.planTile);
			s.clientUse();
			d.planByte1 = (byte) TIME.hours().bitCurrent();
			d.planByte2 = (byte) TIME.days().bitCurrent();
			return res(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			ExecutionStation s = SETT.ROOMS().EXECUTION.executionSpot(d.planTile);
			if (s.isHang()) {
				if (s.clientExecuted()) {
					
				}
				return AI.SUBS().STAND.activateRndDir(a, d);
			}else if (s.isChop()){
				a.speed.setDirCurrent(s.clientGetTurn());
				return AI.SUBS().LAY.activateTime(a, d, 5);
			}
			cancel(a, d);
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			if (d.planByte2 != TIME.days().bitCurrent() && TIME.hours().bitCurrent() > d.planByte1) {
				return false;
			}
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.PRISON_EXECUTE) {
				ExecutionStation s = SETT.ROOMS().EXECUTION.executionSpot(d.planTile);
				if (s.isChop()) {
					chop(a, d);
				}else {
					d.overwrite(a, strangled.set(a, d));
				}
				return false;
			}
			return super.event(a, d, e);
		}
		
		private void chop(Humanoid a, AIManager d) {
			STATS.NEEDS().INJURIES.COUNT.indu().incD(a.indu(), 0.2+RND.rFloat());
			SETT.THINGS().gore.cloud(a, a.race().appearance().colors.blood);
			SETT.THINGS().gore.flesh(a, a.race().appearance().colors.blood);
			if (STATS.NEEDS().INJURIES.COUNT.indu().getD(a.indu()) > 0.75) {
				ExecutionStation s = SETT.ROOMS().EXECUTION.executionSpot(d.planTile);
				if (s != null)
					s.clientClear();
				PrisonerData.self.reportPunishment(a, d, LAW.process().execution);
			
				GAME.count().EXECUTIONS.inc(1);
				STATS.NEEDS().INJURIES.COUNT.indu().setD(a.indu(), 1.0);
				a.kill(false, CAUSE_LEAVES.EXECUTED());
				
				
			}
			
		}
	};
	

	
	private final Resumer strangled = new Resumer(LAW.process().execution.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().single.activate(a, d, AI.STATES().anima.box, 4);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			PrisonerData.self.reportPunishment(a, d, LAW.process().execution);
			GAME.count().EXECUTIONS.inc(1);
			AIManager.dead = CAUSE_LEAVES.EXECUTED();
			cancel(a, d);
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
	
	@Override
	protected void cancel(Humanoid a, AIManager d) {
		ExecutionStation s = SETT.ROOMS().EXECUTION.executionSpot(d.planTile);
		if (s != null)
			s.clientClear();
		super.cancel(a, d);
	}
	
	@Override
	protected boolean shouldContinue(Humanoid a, AIManager d) {
		ExecutionStation s = SETT.ROOMS().EXECUTION.executionSpot(d.planTile);
		if (s != null && s.clientReserved())
			return super.shouldContinue(a, d);
		return false;
	}

}
