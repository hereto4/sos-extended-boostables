package settlement.tilemap.terrain;

import settlement.tilemap.terrain.Terrain.TerrainTile;
import snake2d.util.datatypes.AREA;
import snake2d.util.sets.LIST;
import view.sett.IDebugPanelSett;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMessages;
import view.tool.PlacableMulti;

class TerrainPlacers {

	TerrainPlacers(Terrain t, LIST<TerrainTile> tiles){

		for (TerrainTile tt : tiles) {
			
			make(tt);
		}
		
		new Increaser(t.ROCK) {
			
			@Override
			public void place(int tx, int ty, AREA a, PLACER_TYPE type) {
				t.ROCK.amountIncrease(tx, ty);
			}

		};
		new Decreaser(t.ROCK) {
			
			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				t.ROCK.amountDecrease(tx, ty);
			}
		};


	
	}
	
	private void make(TerrainTile t) {
		
		PLACABLE p = new PlacableMulti(t.getClass().getSimpleName() + " " + t.name(), null, t.getIcon(), null) {
			
			@Override
			public void place(int tx, int ty, AREA a, PLACER_TYPE type) {
				t.placeFixed(tx, ty);
			}
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE type) {
				if (!t.isPlacable(tx, ty))
					return PlacableMessages.¤¤BLOCKED;
				return null;
			}
		};
		
		

		
//		PLACABLE p = new PLACABLE.TILE_RADIUS() {
//			
//			@Override
//			public CharSequence name() {
//				return t.name();
//			}
//			
//			@Override
//			public MEDIUM getIcon() {
//				return t.getIcon();
//			}
//			
//			@Override
//			protected void placeFixed(int tx, int ty) {
//				t.placeFixed(tx, ty);
//			}
//			
//			@Override
//			protected boolean isPlacable(int tx, int ty) {
//				return t.isPlacable(tx, ty);
//			}
//		};
		
		IDebugPanelSett.add("terrain", p);
		
	}
	
	private abstract class Increaser extends PlacableMulti {

		private TerrainTile t;

		
		
		Increaser(TerrainTile t){
			super(t.name() + " increase");
			this.t = t;
			IDebugPanelSett.add("terrain", this);
		}
		
		@Override
		public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE type) {
			return t.is(tx, ty) ? null : "";
		}
		
	}
	
	private abstract class Decreaser extends PlacableMulti {

		private TerrainTile t;
		
		
		Decreaser(TerrainTile t){
			super(t.name() + " decrease");
			this.t = t;
			IDebugPanelSett.add("terrain", this);
		}
		
		@Override
		public CharSequence isPlacable(int tx, int ty, AREA a, PLACER_TYPE type) {
			return t.is(tx, ty) ? null : "";
		}
		
	}
	
	
	
}
