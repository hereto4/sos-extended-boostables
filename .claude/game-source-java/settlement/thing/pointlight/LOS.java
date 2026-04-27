package settlement.thing.pointlight;

import settlement.main.SETT;

public interface LOS {

	public abstract boolean passesToOtherFromThis(int fx, int fy, int tx, int ty);
	public abstract boolean passesFromOtherToThis(int fx, int fy, int tx, int ty);
	public boolean isLightBlocker(int tx, int ty);
	public boolean blocksEnv(int tx, int ty);
	
	public static final LOS OPEN = new LOS() {
		
		@Override
		public boolean passesToOtherFromThis(int fx, int fy, int tx, int ty) {
			return true;
		}
		
		@Override
		public boolean passesFromOtherToThis(int fx, int fy, int tx, int ty) {
			return true;
		}
		
		@Override
		public boolean blocksEnv(int tx, int ty) {
			return false;
		}

		@Override
		public boolean isLightBlocker(int tx, int ty) {
			// TODO Auto-generated method stub
			return false;
		}
		
	};
	
	public static final LOS SOLID = new LOS() {
		
		@Override
		public boolean passesToOtherFromThis(int fx, int fy, int tx, int ty) {
			return !SETT.PATH().solidity.is(tx, ty);
		}
		
		@Override
		public boolean passesFromOtherToThis(int fx, int fy, int tx, int ty) {
			return false;
		}

		@Override
		public boolean blocksEnv(int tx, int ty) {
			return true;
		}

		@Override
		public boolean isLightBlocker(int tx, int ty) {
			return true;
		}
		
	};
	
	public static final LOS CEILING = new LOS() {
		
		@Override
		public boolean passesToOtherFromThis(int fx, int fy, int tx, int ty) {
			return !SETT.LIGHTS().los().get(tx, ty).isLightBlocker(tx, ty);
		}
		
		@Override
		public boolean passesFromOtherToThis(int fx, int fy, int tx, int ty) {
			return true;
		}

		@Override
		public boolean blocksEnv(int tx, int ty) {
			return false;
		}

		@Override
		public boolean isLightBlocker(int tx, int ty) {
			// TODO Auto-generated method stub
			return false;
		}
		
	};
	
}
