package settlement.entity.humanoid.ai.work;

import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISTATES.Animation;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.entity.humanoid.ai.util.AIPlanResourceMany;
import settlement.main.SETT;
import settlement.room.main.Room;
import settlement.room.spirit.temple.ROOM_TEMPLE;
import settlement.room.spirit.temple.TempleInstance;
import settlement.room.spirit.temple.TempleJob;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;

final class WorkTemple extends PlanBlueprint {

	private final ROOM_TEMPLE temple;
	
	WorkTemple(AIModule_Work module, ROOM_TEMPLE blueprint, PlanBlueprint[] map){
		super(module, blueprint, map);
		this.temple = blueprint;
	}
	
	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		if (RND.oneIn(8))
			return walkAround.set(a, d);
		
		TempleInstance ins = (TempleInstance) work(a);
		TempleJob j = ins.jobReservable(a.tc().x(), a.tc().y());
		if (j == null) {
			return walkAround.set(a, d);
		}
		
		d.planTile.set(j.coo());
		d.planByte1 = -1;
		if (j.jobResourceBitToFetch() != null) {
			AISubActivation s = fetch.activate(a, d, j.jobResourceBitToFetch(), maxCarry, 1000, true, true);
			if (s != null) {
				j = ins.job(d.planTile.x(), d.planTile.y());
				j.jobReserve();
				return s;
			}
			j = ins.job(d.planTile.x(), d.planTile.y());
			j.reportMissingResource();
		}
		
		
		
		AISubActivation s = walkToJob.set(a, d);
		if (s != null) {
			j = ins.job(d.planTile.x(), d.planTile.y());
			j.jobReserve();
			return s;
		}

		return walkAround.set(a, d);
	}
	
	private final AIPlanResourceMany fetch = new AIPlanResourceMany(this, 32) {
		
		@Override
		public AISubActivation next(Humanoid a, AIManager d) {
			return walkToJob.set(a, d);
		}
		
		@Override
		public void cancel(Humanoid a, AIManager d) {
			unreserve(a, d);
		}
	};
	
	private boolean reserved(Humanoid a, AIManager d) {
		if (work(a) != null) {
			TempleInstance ins = (TempleInstance) work(a);
			TempleJob j = ins.job(d.planTile.x(), d.planTile.y());
			return j != null && j.jobReservedIs();
		}
		return false;
	}
	
	private void unreserve(Humanoid a, AIManager d) {
		Room r = SETT.ROOMS().map.get(d.planTile);
		if (r != null && r instanceof TempleInstance) {
			TempleInstance ins = (TempleInstance) r;
			TempleJob j = ins.job(d.planTile.x(), d.planTile.y());
			if (j != null)
				j.jobReserveCancel();
		}
	}
	
	private final Res walkToJob = new Res() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().walkTo.cooFull(a, d, d.planTile);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			TempleInstance ins = (TempleInstance) work(a);
			TempleJob j = ins.job(d.planTile.x(), d.planTile.y());
			a.speed.setDirCurrent(DIR.get(a.tc(), j.faceCoo()));
			return work.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return reserved(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			unreserve(a, d);
		}
	};
	
	private final Res work = new Res() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			TempleInstance ins = (TempleInstance) work(a);
			TempleJob j = ins.job(d.planTile.x(), d.planTile.y());
			
			if(d.resourceCarried() != null) {
				j.jobPerform(a, d.resourceA());
				d.resourceCarriedSet(null);
				return null;
			}else if(j.shouldKill()) {
				return sacrifice.set(a, d);
			}else {
				d.planByte1 = 10;
				return res(a, d);
			}
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			d.planByte1 --;
			if (d.planByte1 <= 0) {
				unreserve(a, d);
				return null;
			}
			if (RND.oneIn(5))
				temple.employment().sound().rnd(a);
			a.speed.setDirCurrent(a.speed.dir().next(RND.rInt0(1)));
			return AI.SUBS().single.activate(a, d, preach[RND.rInt(preach.length)], 2 + RND.rFloat(4));
			
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return reserved(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			unreserve(a, d);
		}
	};
	
	private final Res sacrifice = new Res() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			return AI.SUBS().single.activate(a, d, AI.STATES().anima.stab, 10);
			
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			TempleInstance ins = (TempleInstance) work(a);
			TempleJob j = ins.job(d.planTile.x(), d.planTile.y());
			
			if(j.shouldKill()) {
				j.kill();
				
				if(j.shouldKill()) {
					return setAction(a, d);
				}
				temple.employment().sound().rnd(a);
				return AI.SUBS().single.activate(a, d, AI.STATES().anima.armsOut, 4);
			}
			
			unreserve(a, d);
			return null;
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return reserved(a, d);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			unreserve(a, d);
		}
	};
	
	private final Res walkAround = new Res() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().walkTo.room(a, d, work(a));
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return standAround.set(a, d);
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
	private final Res standAround = new Res() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			d.planByte1 = 10;
			return res(a, d);

		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			d.planByte1 --;
			if (d.planByte1 <= 0) {
				return null;
			}
			a.speed.setDirCurrent(a.speed.dir().next(RND.rInt0(1)));
			if (RND.oneIn(5))
				temple.employment().sound().rnd(a);
			return AI.SUBS().single.activate(a, d, preach[RND.rInt(preach.length)], 2 + RND.rFloat(4));
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return work(a) != null;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private final Animation[] preach = new Animation[] {
		AI.STATES().anima.carry,
		AI.STATES().anima.armsOut,
		AI.STATES().anima.fist,
		AI.STATES().anima.wave,
		AI.STATES().anima.stand,
		AI.STATES().anima.stand,
		AI.STATES().anima.stand,
	};

	

	

	
	private abstract class Res extends Resumer {

		protected Res() {
			super("");
		
		}
		
		@Override
		protected void name(Humanoid a, AIManager d, Str string) {
			string.add(temple.employment().verb);
		}
		
		
	}


}