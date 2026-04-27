package settlement.entity.humanoid.ai.types.prisoner;

import game.time.TIME;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.law.court.CourtStation;
import settlement.room.law.court.ROOM_COURT;
import settlement.stats.law.LAW;
import snake2d.util.rnd.RND;

class Judged extends AIPLAN.PLANRES{


	
	public Judged() {
		super("prisJudged");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		CourtStation s = SETT.ROOMS().COURT.exectuionReserve();

		if (s == null)
			return null;
		d.planTile.set(s.cooCriminal());
		return walk.set(a, d);
		
	}
	
	private final Resumer walk = new Resumer(LAW.process().judgement.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			AISubActivation s = AI.SUBS().walkTo.cooFull(a, d, d.planTile);
			if (s != null) {
				return s;
			}
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
	
	private final Resumer ready = new Resumer(LAW.process().judgement.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			CourtStation s = SETT.ROOMS().COURT.executionSpot(d.planTile);
			s.criminalUse();
			d.planByte1 = (byte) TIME.hours().bitCurrent();
			d.planByte2 = (byte) TIME.days().bitCurrent();
			a.speed.setDirCurrent(s.criminalDir());
			d.planByte3 = 8;
			return res(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			CourtStation s = SETT.ROOMS().COURT.executionSpot(d.planTile);
			if (s.criminalIsBeeingHeard()) {
				if (d.planByte3-- <= 0) {
					PrisonerData.self.judged.set(d, 1);
					LAW.process().judgement.inc(a.race(), true);
					cancel(a, d);
					
					if (RND.rFloat() < ROOM_COURT.freeRate) {
						return freed.set(a, d);
						
					}
					return null;
				}
					
				if (RND.oneIn(4)) {
					a.speed.setDirCurrent(s.criminalDir().next(RND.rInt0(2)));
					return AI.SUBS().STAND.activateTime(a, d, 1+RND.rInt(5));
				}else {
					return AI.SUBS().single.activate(a, d, RND.rBoolean() ? AI.STATES().anima.box : AI.STATES().anima.wave, 1 + RND.rFloat()*4);
				}
			}else {
				a.speed.setDirCurrent(a.speed.dir().next(RND.rInt0(2)));
				return AI.SUBS().STAND.activateTime(a, d, 1+RND.rInt(5));
			}
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
		
	};
	
	private final Resumer freed = ResFree.make(this);
	
	@Override
	protected void cancel(Humanoid a, AIManager d) {
		CourtStation s = SETT.ROOMS().COURT.executionSpot(d.planTile);
		if (s != null)
			s.criminalClear();
		super.cancel(a, d);
	}
	
	@Override
	protected boolean shouldContinue(Humanoid a, AIManager d) {
		if (getResumer(d) == freed)
			return true;
		CourtStation s = SETT.ROOMS().COURT.executionSpot(d.planTile);
		if (s != null && s.criminalReserved())
			return super.shouldContinue(a, d);
		return false;
	}

}
