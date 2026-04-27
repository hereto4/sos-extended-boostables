package settlement.tilemap.terrain;

import settlement.main.SETT;
import snake2d.util.datatypes.AREA;
import snake2d.util.map.MAP_DOUBLEE;
import snake2d.util.map.MAP_INTE;
import view.sett.IDebugPanelSett;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;

public abstract class TAmount implements MAP_INTE{
	
	public final int max;
	public final double maxI;
	
	TAmount(int max, CharSequence name){
		this.max = max;
		this.maxI = 1.0/max;
		PlacableMulti undo = new PlacableMulti(name + " decrease") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				increment(tx, ty, -1);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return get(tx, ty) > 0 ? null : E;
			}
		};
		
		PlacableMulti place = new PlacableMulti(name + " increase") {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				increment(tx, ty, 1);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				return get(tx, ty) < max ? null : E;
			}
			
			@Override
			public PLACABLE getUndo() {
				return undo;
			}
		};
		
		IDebugPanelSett.add(place);
		
	}
	
	@Override
	public int get(int tx, int ty) {
		if (SETT.IN_BOUNDS(tx, ty)) {
			return get(tx+ty*SETT.TWIDTH);
		}
		return 0;
	}

	@Override
	public MAP_INTE set(int tx, int ty, int value) {
		if (SETT.IN_BOUNDS(tx, ty)) {
			return set(tx+ty*SETT.TWIDTH, value);
		}
		return this;
	}
	
	public final MAP_DOUBLEE DM = new MAP_DOUBLEE() {
		
		@Override
		public double get(int tx, int ty) {
			return TAmount.this.get(tx, ty)*maxI;
		}
		
		@Override
		public double get(int tile) {
			return TAmount.this.get(tile)*maxI;
		}
		
		@Override
		public MAP_DOUBLEE set(int tx, int ty, double value) {
			TAmount.this.set(tx, ty, (int)(value*max));
			return this;
		}
		
		@Override
		public MAP_DOUBLEE set(int tile, double value) {
			TAmount.this.set(tile, (int)(value*max));
			return this;
		}
	};
	
}