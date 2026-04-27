package settlement.entity;

import settlement.entity.humanoid.Humanoid;
import settlement.main.SETT;
import snake2d.util.MATH;

public abstract class EntityIterator {
	
	/**
	 * 
	 * @param e
	 * @return if to break
	 */
	protected abstract boolean processAndShouldBreak(ENTITY e, int ie);
	
	public final void iterate() {
		ENTITY[] es = SETT.ENTITIES().getAllEnts();
		int m = SETT.ENTITIES().Imax();
		for (int i = 0; i <= m; i++) {
			if (es[i] != null)
				if (processAndShouldBreak(es[i], i))
					return;
		}
	}
	
	public final void iterate(int off) {
		ENTITY[] es = SETT.ENTITIES().getAllEnts();
		int m = SETT.ENTITIES().Imax()+1;
		for (int i = 0; i < m; i++) {
			int k = MATH.mod(i+off, m);
			if (es[k] != null)
				if (processAndShouldBreak(es[k], k))
					return;
		}
	}
	
	public static abstract class Humans extends EntityIterator {
		
		@Override
		public final boolean processAndShouldBreak(ENTITY e, int ie) {
			if (e instanceof Humanoid) {
				return processAndShouldBreakH((Humanoid) e, ie);
			}
			return false;
		}
		
		protected abstract boolean processAndShouldBreakH(Humanoid h, int ie);
		
	}
}