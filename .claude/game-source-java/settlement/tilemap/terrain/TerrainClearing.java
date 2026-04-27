package settlement.tilemap.terrain;

import game.audio.SoundRace;
import init.constant.C;
import init.resources.RESOURCE;
import settlement.main.SETT;

public abstract class TerrainClearing {
	
	protected TerrainClearing() {
		
	}
	
	/**
	 * 
	 * @return true for all structures and for mountain
	 */
	public boolean isStructure() {
		return false;
	}
	
	
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @return true for all except deep ocean and mountain.
	 */
	public abstract boolean can();
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @return false if cant be cleared and if nothings is here.
	 */
	public boolean needs() {
		return true;
	}

	public abstract RESOURCE clear1(int tx, int ty);
	/**
	 * 
	 * @param tx
	 * @param ty
	 * @return the amount of resources yeilded, specified by {@link #resource()}
	 */
	public abstract int clearAll(int tx, int ty);

	public boolean canDestroy(int tx, int ty) {
		return true;
	}

	public void destroy(int tx, int ty) {
		SETT.TERRAIN().NADA.placeFixed(tx, ty);
	}

	public double strength() {
		return 500*C.TILE_SIZE;
	}
	
//	public abstract boolean canDestroy(int tx, int ty);
//	public abstract void destroy(int tx, int ty);
//	public abstract double strength();

	public abstract SoundRace sound(int tx, int ty);
	/**
	 * true for flowers, bush, etc
	 * @return
	 */
	public boolean isEasilyCleared() {
		return false;
	}
	
	static final TerrainClearing dummy = new TerrainClearing() {

		@Override
		public boolean can() {
			return true;
		}

		@Override
		public boolean needs() {
			return false;
		}

		@Override
		public RESOURCE clear1(int tx, int ty) {
			return null;
		}
		
		@Override
		public boolean canDestroy(int tx, int ty) {
			return false;
		}
		
		@Override
		public void destroy(int tx, int ty) {
			
			
		}
		
		@Override
		public double strength() {
			return 0;
		}



		@Override
		public int clearAll(int tx, int ty) {
			return 0;
		}

		@Override
		public SoundRace sound(int tx, int ty) {
			return null;
		}

		@Override
		public boolean isStructure() {
			return false;
		}
		
		@Override
		public boolean isEasilyCleared() {
			return true;
		}
		
	};


	
}