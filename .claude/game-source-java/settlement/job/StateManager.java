package settlement.job;

import static settlement.main.SETT.IN_BOUNDS;
import static settlement.main.SETT.JOBS;
import static settlement.main.SETT.PATH;
import static settlement.main.SETT.TWIDTH;

import settlement.main.SETT;
import snake2d.util.datatypes.DIR;
import snake2d.util.map.MAP_OBJECT;
import snake2d.util.sets.Bitsmap1D;

final class StateManager implements MAP_OBJECT<StateManager.State> {

	enum State {

		DORMANT, RESERVABLE, RESERVED, BLOCKED;

		static State[] all = values();

	}

	private final Bitsmap1D bits;
	private final static int MAX_DISTANCE = 16;

	
	public StateManager(Bitsmap1D bits) {
		this.bits = bits;
	}

	@Override
	public State get(int tile) {
		return State.all[bits.get(tile)];
	}
	
	@Override
	public State get(int tx, int ty) {
		return get(tx + ty*TWIDTH);
	}

	public void set(State value, Job job) {
		
		int tx = job.coo.x();
		int ty = job.coo.y();
		
		if (get(job.jobCoo()) == State.DORMANT && value != State.RESERVABLE) {
			return;
		}
		
		if (value == State.BLOCKED)
			value = State.RESERVABLE;
		
		if (isBlocked(tx, ty))
			value = State.BLOCKED;
		
		set(value, tx, ty);
		
		for (int di = 0; di < DIR.ALL.size(); di++) {
			DIR d = DIR.ALL.get(di);
			int dx = tx+d.x();
			int dy = ty+d.y();
			if (JOBS().getter.is(dx, dy)) {
				if (isBlocked(dx, dy)) {
					set(State.BLOCKED, dx, dy);
				}else if (get(dx+dy*TWIDTH) == State.BLOCKED){
					set(State.RESERVABLE, dx, dy);
				}
			}
			
		}
		
		job.get(tx, ty);	
	}
	
	private void set(State state, int tx, int ty) {
		int tile = tx + ty*TWIDTH;
		State old = get(tile);
		
		if (old == state)
			return;
		
		if (old == State.RESERVABLE) {
			PATH().finders.job.report(JOBS().getter.get(tx, ty), -1);
		}
		
		bits.set(tile, state.ordinal());
		
		if (state == State.RESERVABLE) {
			PATH().finders.job.report(JOBS().getter.get(tx, ty), 1);
		}
	}

	
	private boolean isBlocked(int tx, int ty) {
		if (!isBlockingJob(tx, ty))
			return false;
		
		int depth = getDepth(tx, ty);
		if (depth == MAX_DISTANCE) {
			if (PATH().solidity.is(tx, ty, DIR.N))
				return false;
			
			if (getDepth(tx+DIR.N.x(), ty+DIR.N.y()) < MAX_DISTANCE)
				return false;
			
			return true;
		}
		
		for (int di = 0; di < DIR.ORTHO.size(); di++) {
			DIR d = DIR.ORTHO.get(di);
			int dx = tx+d.x();
			int dy = ty+d.y();
			if (canBeBlocked(dx, dy) && getDepth(dx, dy) > depth) {
				return true;
			}
		}
		
		return false;
	}
	
	public int getDepth(int tx, int ty) {
		
		int depth = getDepthOrtho(tx, ty);
		if (depth < MAX_DISTANCE)
			return depth;
		
		
		for (int di = 0; di < DIR.ORTHO.size(); di++) {
			DIR d = DIR.ORTHO.get(di);
			if (!IN_BOUNDS(tx, ty, d) || PATH().solidity.is(tx, ty, d))
				depth+=SETT.TWIDTH;
		}
		
		return depth + getDepthNortho(tx, ty);
	}
	
	public int getDepthOrtho(int tx, int ty) {
		
		int depth = MAX_DISTANCE;
		
		for (int di = 0; di < DIR.ORTHO.size() && depth > 0; di++) {
			DIR d = DIR.ORTHO.get(di);
			for (int i = 1; i < MAX_DISTANCE && i <= depth; i++) {
				int dx = tx+d.x()*i;
				int dy = ty+d.y()*i;
				if (!IN_BOUNDS(dx, dy) || PATH().solidity.is(dx, dy))
					break;
				if (!canBeBlocked(dx, dy)) {
					if (i-1 < depth) {
						depth = i-1;
						break;
					}
				}
			}
		}
		
		
		return depth;
	}
	
	public int getDepthNortho(int tx, int ty) {
		return tx;
	}
	
	private boolean isBlockingJob(int tx, int ty) {
		Job j = JOBS().getter.get(tx, ty);
		return j != null && get(tx, ty) != State.DORMANT && j.becomesSolidNext();
	}
	
	private boolean canBeBlocked(int tx, int ty) {
		Job j = JOBS().getter.get(tx, ty);
		return j != null && get(tx, ty) != State.DORMANT && j.becomesSolid();
	}
	

	
	void clear(int tx, int ty) {
		set(State.DORMANT, JOBS().getter.get(tx, ty));
		JOBS().set(Job.NOTHING, tx, ty);
	}

	public void activate(int tile, Job job) {

		if (get(tile) == State.DORMANT)
			set(State.RESERVABLE, job);
	}


}
