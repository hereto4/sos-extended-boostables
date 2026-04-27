package settlement.entity.humanoid.ai.types.prisoner;

import static settlement.main.SETT.PATH;

import game.time.TIME;
import init.type.CAUSE_ARRIVES;
import init.type.HTYPES;
import init.type.NEEDS;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISTATE;
import settlement.entity.humanoid.ai.main.AISUB;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.misc.util.FSERVICE;
import settlement.room.law.prison.ROOM_PRISON;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import util.text.Dic;

class Prison extends AIPLAN.PLANRES{

	private final ROOM_PRISON b = SETT.ROOMS().PRISON;
	
	public Prison() {
		super("prisPrison");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		if (setCell(a, d)) {
			PrisonerData.self.reportPunishment(a, d, LAW.process().prison);
			STATS.NEEDS().EXPOSURE.fix(a.indu());
			d.planByte1 = 8;
			AISubActivation s = init.set(a, d);
			if (s != null)
				return s;
			cancel(a, d);
			
		}
		return null;
	}

	private boolean setCell(Humanoid a, AIManager d) {
				
		COORDINATE c = b.registerPrisoner(AI.modules().coo(d), a.tc());
		if (c == null)
			return false;
		AI.modules().coo(d).set(c);
		return true;
	}
	
	private final Resumer init = new Resumer(LAW.process().prison.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			
			return AI.SUBS().STAND.activateRndDir(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			if (d.planByte1 <= 0) {
				if (!SETT.PATH().connectivity.is(a.tc())) {
					return unfuck.set(a, d);
				}
				if (AIModule_Prisoner.DATA().prisonTimeLeft.get(d) == 0) {
					return free.set(a, d);
				}
				
				return null;
			}
			d.planByte1 --;
			
			if (!b.isWithinCell(a.tc().x(), a.tc().y(), AI.modules().coo(d))) {
				return walkToDoor.set(a, d);
			}
			
			if (NEEDS.TYPES().HUNGER.stat().getPrio(a.indu()) > 0) {
				AISubActivation s = unfuck(eat, a, d);
				if (s != null)
					return s;
			}
			
			if (TIME.light().nightIs()) {
				
				AISubActivation s = sleep.set(a, d);
				if (s != null)
					return s;
			}
			
			//hunger, popo
			if (RND.oneIn(5)) {
				
				if (RND.oneIn(8)) {
					AISubActivation s = unfuck(poop, a, d);
					if (s != null)
						return s;
				}else {
					AISubActivation s = changeSpot.set(a, d);
					if (s != null)
						return s;
				}
			}
			
			
			
			return AI.SUBS().STAND.activateRndDir(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
			
		}
		
		private AISubActivation unfuck(Resumer res, Humanoid a, AIManager d) {
			if (!SETT.PATH().connectivity.is(a.tc())) {
				return unfuck.set(a, d);
			}
			return res.set(a, d);
		}
	};
	
	private final Resumer walkToDoor = new Resumer(LAW.process().prison.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().walkTo.cooFull(a, d, AI.modules().coo(d));
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return init.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
			
		}
	};
	

	

	
	private final Resumer unfuck = new Resumer(LAW.process().prison.verb) {
		
		final AISUB untrapp = new AISUB.Simple("prisontrapped") {

			@Override
			public AISTATE resume(Humanoid a, AIManager d) {
				d.subByte ++;
				if (d.subByte != 1)
					return null;
				for (int di = 0; di < DIR.ALL.size(); di++) {
					DIR dir = DIR.ALL.get(di);
					if (PATH().connectivity.is(a.tc(), dir) 
							&& b.isWithinCell(a.tc().x()+dir.x(), a.tc().y()+dir.y(), AI.modules().coo(d)) ) 
					{
						return AI.STATES().WALK2.dirTile(a, d, dir);
					}
				}
				return AI.STATES().STAND.activate(a, d, 1);
			}
			
		};
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return untrapp.activate(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return init.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
			
		}
	};
	
	private final Resumer sleep = new Resumer(LAW.process().prison.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().subSleep.activate(a, d);
			
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return init.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
			
		}
	};
	
	private final Resumer poop = new Resumer(LAW.process().prison.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().STAND.activateRndDir(a, d);
			
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			FSERVICE c = b.getLatrine(AI.modules().coo(d));
			if (c == null || !c.findableReservedCanBe())
				return null;
			c.findableReserve();
			c.consume();
			return init.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
			
		}
	};
	
	private final Resumer eat = new Resumer(LAW.process().prison.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			FSERVICE c = b.getFood(AI.modules().coo(d));
			if (c == null || !c.findableReservedCanBe())
				return null;
			c.findableReserve();
			return AI.SUBS().walkTo.coo(a, d, c);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			FSERVICE c = b.getFood(AI.modules().coo(d));
			if (c != null)
				c.consume();
			return eat2.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			FSERVICE c = b.getFood(AI.modules().coo(d));
			if (c != null)
				c.consume();

		}
	};
	
	private final Resumer eat2 = new Resumer(LAW.process().prison.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().single.activate(a, d, AI.STATES().anima.grab, 3);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			STATS.FOOD().eat(a, 0, 0);
			NEEDS.TYPES().HUNGER.stat().fix(a.indu());
			return init.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
			
		}
	};
	
	private final Resumer changeSpot = new Resumer(LAW.process().prison.verb) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			DIR dir = DIR.ORTHO.rnd();
			int dx = a.tc().x() + dir.x();
			int dy = a.tc().y() + dir.y();
			if (b.isWithinCell(dx, dy, AI.modules().coo(d))) {
				if (!SETT.ENTITIES().hasAtTile(dx, dy))
					return AI.SUBS().walkTo.cooFull(a, d, dx, dy);
			}
			return null;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			return init.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
			
		}
	};
	
	Resumer free = new Resumer(Dic.¤¤Free) {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {	
			return AI.SUBS().STAND.activateRndDir(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			a.HTypeSet(HTYPES.SUBJECT(), null, CAUSE_ARRIVES.PAROLE());
			STATS.LAW().EX_CON.indu().setD(a.indu(), 1.0);
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
	public boolean event(Humanoid a, AIManager d, HEventData e) {
		if (e.event == HEvent.ROOM_REMOVED && e.room.blueprintI() == b && e.room.is(AI.modules().coo(d))) {
			d.overwrite(a, AI.plans().NOP);
			AI.modules().coo(d).set(-1, -1);
			return true;
		}
		return false;
	}
	
	@Override
	protected void cancel(Humanoid a, AIManager d) {
		b.unregisterPrisoner(AI.modules().coo(d));
		AI.modules().coo(d).set(-1, -1);
		super.cancel(a, d);
	}
	
	@Override
	protected AISubActivation resume(Humanoid a, AIManager d) {
		AISubActivation s = super.resume(a, d);
		if (s == null) {
			b.unregisterPrisoner(AI.modules().coo(d));
			AI.modules().coo(d).set(-1, -1);
		}
		return s;
	}
	
	

}
