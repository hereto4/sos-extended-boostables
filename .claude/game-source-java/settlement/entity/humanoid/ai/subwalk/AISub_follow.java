package settlement.entity.humanoid.ai.subwalk;

import settlement.entity.ENTITY;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.HEvent.HEventData;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISTATE;
import settlement.entity.humanoid.ai.main.AISTATES;
import settlement.main.SETT;
import snake2d.util.rnd.RND;

final class AISub_follow extends PathWalker{
	
	
	AISub_follow(String key, AISTATES.WALK_DEST state, CharSequence name){
		super(key, state, name);
	}

	AISubActivation activate(Humanoid a, AIManager d, ENTITY other, byte tries) {
		d.planObject = other.id();
		d.subPathByte = (byte) tries;
		d.path.request(a.tc(), other.tc());
		if (d.path.isSuccessful())
			return activate(a, d);
		return activate(a, d, meet);
	}
	
	@Override
	protected boolean hasFailed(Humanoid a, AIManager d) {
		if (SETT.ENTITIES().getByID(d.planObject) == null) {
			
			return true;
		}
		return false;
	}
	
	@Override
	protected void arrive(Humanoid a, AIManager d) {
		
	}
	
	@Override
	protected void abort(Humanoid a, AIManager d) {
		
	}
	
	@Override
	protected AISTATE setLast(Humanoid a, AIManager d) {
		return last.set(a, d);
	}
	
	@Override
	public AISTATE resume(Humanoid a, AIManager d) {
		if (RND.oneIn(5)) {
			ENTITY prey = SETT.ENTITIES().getByID(d.planObject);
			if (prey == null) {
				return meet.set(a, d);
			}
		}
		return super.resume(a, d);
	}
	
	public boolean isSuccess(Humanoid a, AIManager d) {
		ENTITY prey = SETT.ENTITIES().getByID(d.planObject);
		
		if (prey == null) {
			
			return false;
		}
		int dx = prey.physics.tileC().x() - a.physics.tileC().x();
		int dy = prey.physics.tileC().y() - a.physics.tileC().y();
		int dist = Math.abs(dx) + Math.abs(dy);
		
		return dist <= 1;
	}

	
	final Resumer last = new Resumer() {
		
		@Override
		protected AISTATE setAction(Humanoid a, AIManager d) {
			return res(a, d);
		}
		
		@Override
		protected AISTATE res(Humanoid a, AIManager d) {
			double m = a.speed.magnitude();
			
			a.speed.magnitudeInit(0);
			ENTITY prey = SETT.ENTITIES().getByID(d.planObject);
			
			if (prey == null) {
				
				return meet.set(a, d);
			}
			int dx = prey.physics.tileC().x() - a.physics.tileC().x();
			int dy = prey.physics.tileC().y() - a.physics.tileC().y();
			int dist = Math.abs(dx) + Math.abs(dy);
			
			if (dist == 0) {
				return meet.set(a, d);
			}
			
			if(dist < 5) {
				AISTATE s = state.free(a, d, prey.body().cX(), prey.body().cY());
				a.speed.magnitudeInit(m);
				return s;
			}else {
				d.path.request(a.tc(), prey.tc());
				if (d.path.isSuccessful()) {
					AISTATE s = activate(a, d).state();
					a.speed.magnitudeInit(m);
					return s;
				}
			}
			return meet.set(a, d);
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event == HEvent.COLLISION_TILE) {
				ENTITY prey = SETT.ENTITIES().getByID(d.planObject);
				if (prey == null) {
					d.overwrite(a, meet.set(a, d));
					return false;
				}
				a.speed.magnitudeInit(0);
				a.speed.magnitudeTargetSet(0);
				d.path.request(a.tc(), prey.tc());
				if (d.path.isSuccessful()) {
					d.overwrite(a, activate(a, d));
					return true;
				}
				d.overwrite(a, meet.set(a, d));
				return true;
			}
			return super.event(a, d, e);
		}
	};
	
	final Resumer collide = new Resumer() {
		
		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			ENTITY prey = SETT.ENTITIES().getByID(d.planObject);
			if (prey == null) {
				return meet.set(a, d);
			}
			d.path.request(a.tc(), prey.tc());
			if (d.path.isSuccessful()) {
				
				AISTATE s = activate(a, d).state();
				return s;
			}
			return meet.set(a, d);
		}

		@Override
		public boolean success(Humanoid a, AIManager d) {
			return true;
		}

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			a.speed.magnitudeInit(0);
			return AI.STATES().STAND.activate(a, d, 0.05);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			abort(a, d);
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event != HEvent.COLLISION_SOFT)
				super.event(a, d, e);
			return false;
		}
	};
	
	final Resumer meet = new Resumer() {
		
		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			return null;
		}

		@Override
		public boolean success(Humanoid a, AIManager d) {
			return true;
		}

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			a.speed.magnitudeInit(0);
			return AI.STATES().STAND.activate(a, d, 0.1);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			abort(a, d);
		}
		
		@Override
		public boolean event(Humanoid a, AIManager d, HEventData e) {
			if (e.event != HEvent.MEET_HARMLESS) {
				super.event(a, d, e);
			}
			return false;
		}
	};
	
	@Override
	public boolean event(Humanoid a, AIManager d, HEventData e) {
		if (e.event == HEvent.MEET_HARMLESS) {
			ENTITY target = SETT.ENTITIES().getByID(d.planObject);
			if (target != null && target == e.other) {
				d.overwrite(a, meet.set(a, d));
			}
			
		}else if (e.event == HEvent.COLLISION_SOFT) {
			
			d.overwrite(a, collide.set(a, d));
			
		}else {
			return super.event(a, d, e);
		}
		return false;
		
	}

	
}


