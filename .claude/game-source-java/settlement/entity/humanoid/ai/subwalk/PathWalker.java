package settlement.entity.humanoid.ai.subwalk;

import game.GAME;
import init.constant.C;
import settlement.entity.humanoid.HEvent;
import settlement.entity.humanoid.Humanoid;
import settlement.entity.humanoid.ai.main.AI;
import settlement.entity.humanoid.ai.main.AIManager;
import settlement.entity.humanoid.ai.main.AISTATE;
import settlement.entity.humanoid.ai.main.AISTATES;
import settlement.entity.humanoid.ai.main.AISUB.Resumable;
import settlement.main.SETT;
import snake2d.util.datatypes.DIR;
import snake2d.util.rnd.RND;

abstract class PathWalker extends Resumable {
	
	protected final AISTATES.WALK_DEST state;
	
	PathWalker(String key, AISTATES.WALK_DEST state, CharSequence name) {
		super(key + "Walker", name);
		this.state = state;
	}
	
	protected PathWalker(String key, String name) {
		this(key + "Walker", AI.STATES().WALK2, name);
	}

	@Override
	public AISTATE init(Humanoid a, AIManager d) {
		if (a.speed.magnitude() > 0) {
			//This shouldn't happen
			//d.debug(a, "HERE");
			return stop.set(a, d);
		}
		if (!d.path.isSuccessful()) {
			GAME.Notify("here!" + d.path);
			return failure.set(a, d);
		}
		
		d.subPathByte2 = 0;
		AISTATE st = next.set(a, d);
		if (st == null)
			return stop.set(a, d);
		return st;
	}
	
	
	private final Resumer next = new Resumer() {

		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			if (d.path.isDest()) {
				a.speed.turn2(a, (d.path.destX()<<C.T_SCROLL)+C.TILE_SIZEH, (d.path.destY()<<C.T_SCROLL)+C.TILE_SIZEH);
				return setLast(a, d);
			}
			
			d.path.setNext();
			if (!d.path.isSuccessful()) {
				GAME.Notify("no " + a.physics.tileC() + " " + d.path.destX() + " " + d.path.destY());
				return failure.set(a, d); 
			}
			d.subPathByte2 ++;
			if (d.subPathByte2 > 4) {
				d.subPathByte2 = 0;
				if (hasFailed(a, d)) {
					a.speed.magnitudeInit(0);
					return failure.set(a, d);
				}
			}
			
			return state.path(a, d);
		}

		@Override
		public boolean success(Humanoid a, AIManager d) {
			return true;
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			
			abort(a, d);
		}

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			return res(a, d);
		};
		
		
		
	};
	
	final Resumer moveToEdge = new Resumer() {
		
		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			int dy = d.path.destY()*C.TILE_SIZE +C.TILE_SIZEH - a.body().cY();
			int dx = d.path.destX()*C.TILE_SIZE +C.TILE_SIZEH - a.body().cX();
			a.speed.setDirCurrent(DIR.get(dx, dy));
			return wait.set(a, d);
		}

		@Override
		public boolean success(Humanoid a, AIManager d) {
			return true;
		}

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			//GAME.Notify("here " + a.physics.tileC());
			int x2 = d.path.getSettCX();
			int y2 = d.path.getSettCY();
			int dd = ((C.TILE_SIZE - a.body().width())-2) / 2;
			if (d.path.isFull()) {
				if (dd > 3)
					dd = 3;
				x2 += RND.rInt0(dd);
				y2 += RND.rInt0(dd);
			} else {
				
				int dy = d.path.destY() - d.path.y();
				int dx = d.path.destX() - d.path.x();
				x2 += dx * dd;
				y2 += dy * dd;
				
			}
			return AI.STATES().WALK2.free(a, d, x2, y2);
		}
		
		@Override
		public void can(Humanoid a, AIManager d) {
			abort(a, d);
		}
	};
	
	final Resumer wait = new Resumer() {
		
		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			if (hasFailed(a, d) || !d.path.isSuccessful()) {
				return failure.set(a, d);
			}
			arrive(a, d);
			return null;
		}

		@Override
		public boolean success(Humanoid a, AIManager d) {
			return true;
		}

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			a.speed.magnitudeInit(0);
			return AI.STATES().STAND.activate(a, d, 0.5f);
		}
		@Override
		public void can(Humanoid a, AIManager d) {
			abort(a, d);
		}
	};
		
	private final Resumer stop = new Resumer() {
		
		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			if (hasFailed(a, d)) {
				return failure.set(a, d);
			}
			return next.set(a, d);
		}

		@Override
		public boolean success(Humanoid a, AIManager d) {
			return true;
		}

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			return AI.STATES().STOP.activate(a, d);
		}
		@Override
		public void can(Humanoid a, AIManager d) {
			abort(a, d);
		}
		
	};
	
	final Resumer failure = new Resumer() {
		
	
		
		@Override
		public AISTATE res(Humanoid a, AIManager d) {
			return null;
		}

		@Override
		public boolean success(Humanoid a, AIManager d) {
			return false;
		}

		@Override
		public AISTATE setAction(Humanoid a, AIManager d) {
			a.speed.magnitudeInit(0);
			abort(a, d);
			return AI.STATES().STAND.activate(a, d, 0.1f);
		}
		
	};
	
	@Override
	public void cancel(Humanoid a, AIManager d) {
		super.cancel(a, d);
	}

	
	@Override
	public final AISTATE resumeInterrupted(Humanoid a, AIManager d, HEvent event) {
		
		d.subPathByte2 = 0;

		if (event == HEvent.COLLISION_TILE)
			return null;
		
		if (d.path().isFull() && !SETT.PATH().connectivity.is(d.path().destX(), d.path().destY()))
			return null;
		
		if (hasFailed(a, d) || !d.path.resume(a.physics.tileC(), a.body())) {
			return null;
		}
		return init(a, d);
	}
	
	@Override
	public AISTATE resume(Humanoid a, AIManager d) {
		AISTATE s =  super.resume(a, d);
		return s;
	}
	
	protected AISTATE setLast(Humanoid a, AIManager d) {
		return moveToEdge.set(a, d);
	}
	
	protected abstract boolean hasFailed(Humanoid a, AIManager d);
	protected abstract void abort(Humanoid a, AIManager d);
	protected abstract void arrive(Humanoid a, AIManager d);
	
}