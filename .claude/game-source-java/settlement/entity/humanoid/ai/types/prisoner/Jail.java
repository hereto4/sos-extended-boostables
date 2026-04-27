package settlement.entity.humanoid.ai.types.prisoner;

import game.time.TIME;
import init.type.NEEDS;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AIPLAN;
import settlement.entity.humanoid.ai.main.AISUB.AISubActivation;
import settlement.main.SETT;
import settlement.room.law.stockade.ROOM_STOCKADE;
import settlement.room.main.RoomInstance;
import settlement.stats.STATS;
import settlement.stats.law.LAW;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.text.Str;
import util.text.D;

class Jail extends AIPLAN.PLANRES{

	private final ROOM_STOCKADE b = SETT.ROOMS().STOCKADE;
	private static CharSequence ¤¤name = "In Stockade";
	static {
		D.ts(Jail.class);
	}
	public Jail() {
		super("prisJail");
	}

	@Override
	protected AISubActivation init(Humanoid a, AIManager d) {
		
		RoomInstance ins = b.registerPrisoner(a.tc());
		if (ins == null)
			return null;
		d.planTile.set(ins.mX(), ins.mY());
		STATS.NEEDS().EXPOSURE.fix(a.indu());
		d.planByte1 = 8;
		PrisonerData.self.reportPunishment(a, d, LAW.process().none);
		return init.set(a, d);
	}


	
	private final Resumer init = new Resumer() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().STAND.activateRndDir(a, d);
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			
			
			if (d.planByte1 <= 0) {
				return null;
			}
			d.planByte1 --;
			
			if (!b.isWithin(d.planTile.x(), d.planTile.y(), a.tc())) {
				return walkToDoor.set(a, d);
			}
			
			if (NEEDS.TYPES().HUNGER.stat().getPrio(a.indu()) > 0) {
				AISubActivation s = eat.set(a, d);
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
					AISubActivation s = poop.set(a, d);
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
	};
	
	private final Resumer walkToDoor = new Resumer() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			return AI.SUBS().walkTo.room(a, d, b.getter.get(d.planTile));
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
	
	private final Resumer sleep = new Resumer() {
		
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
	
	private final Resumer poop = new Resumer() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			COORDINATE c = b.latrineReserve(d.planTile);
			d.planByte2 = 0;
			if (c != null) {
				d.planTile.set(c);
				AISubActivation ss = AI.SUBS().walkTo.cooFull(a, d, c.x(), c.y());
				if (ss != null)
					return ss;
				b.latrineUse(d.planTile, false);
			}
			return null;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			if (d.planByte2 == 0) {
				d.planByte2 = 1;
				return AI.SUBS().STAND.activateRndDir(a, d);
			}
			b.latrineUse(d.planTile, true);
			return init.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			b.latrineUse(d.planTile, false);
		}
	};
	
	private final Resumer eat = new Resumer() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			COORDINATE c = b.foodReserve(d.planTile);
			d.planByte2 = 0;
			if (c != null) {
				d.planTile.set(c);
				AISubActivation ss = AI.SUBS().walkTo.cooFull(a, d, c.x(), c.y());
				if (ss != null)
					return ss;
				b.foodUse(d.planTile, false);
			}
			return null;
		}
		
		@Override
		protected AISubActivation res(Humanoid a, AIManager d) {
			STATS.FOOD().eat(a, 0, 0);
			NEEDS.TYPES().HUNGER.stat().fix(a.indu());
			if (d.planByte2 == 0) {
				d.planByte2 = 1;
				return AI.SUBS().single.activate(a, d, AI.STATES().anima.grab, 3);
			}
			b.foodUse(d.planTile, true);
			return init.set(a, d);
		}
		
		@Override
		public boolean con(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			b.foodUse(d.planTile, false);
		}
	};
	
	private final Resumer changeSpot = new Resumer() {
		
		@Override
		protected AISubActivation setAction(Humanoid a, AIManager d) {
			DIR dir = DIR.ORTHO.rnd();
			int dx = a.tc().x() + dir.x();
			int dy = a.tc().y() + dir.y();
			if (b.isWithin(dx, dy, a.tc())) {
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
	
	@Override
	public boolean event(Humanoid a, AIManager d, HEventData e) {
		if (e.event == HEvent.ROOM_REMOVED && e.room.blueprintI() == b && e.room.is(d.planTile)) {
			
			d.overwrite(a, AI.plans().NOP);
			return true;
		}
		return false;
	}
	
	@Override
	protected void cancel(Humanoid a, AIManager d) {
		b.unregisterPrisoner(d.planTile);
		super.cancel(a, d);
	}
	
	@Override
	protected AISubActivation resume(Humanoid a, AIManager d) {
		AISubActivation s = super.resume(a, d);
		if (s == null)
			b.unregisterPrisoner(d.planTile);
		return s;
	}
	
	@Override
	protected boolean shouldContinue(Humanoid a, AIManager d) {
		return super.shouldContinue(a, d);
	}
	
	@Override
	protected void name(Humanoid a, AIManager d, Str string) {
		string.add(¤¤name);
	}

}
