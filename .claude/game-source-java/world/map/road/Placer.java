package world.map.road;


import init.sprite.UI.Icon;
import init.sprite.UI.UI;
import snake2d.util.datatypes.AREA;
import snake2d.util.datatypes.DIR;
import snake2d.util.sets.LinkedList;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMulti;
import world.WORLD;

class Placer {

	public final LinkedList<PLACABLE> placers = new LinkedList<>();
	
	public Placer() {
		
		PlacableMulti undo = new PlacableMulti("remove", "", UI.icons().m.cancel) {
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				if (WORLD.ROADS().placable.is(tx, ty)) {
					return null;
				}
				return E;
			}

			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				WORLD.ROADS().set(tx, ty, false);
				
			}

		};
		
		
		
		PlacableMulti road = new PlacableMulti("road", "", WORLD.BUILDINGS().sprites.roads.makeSprite(0x0F)) {
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				if (WORLD.ROADS().placable.is(tx, ty)) {
					return null;
				}
				return E;
			}

			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				WORLD.ROADS().set(tx, ty, true);
				
			}
			
			@Override
			public PLACABLE getUndo() {
				return undo;
			};

		};
		
		PlacableMulti harbour = new PlacableMulti("bridge", "", WORLD.BUILDINGS().sprites.harbour.makeSprite(0)) {
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				
				if (!WORLD.ROADS().canBridge.is(tx, ty))
					return E;
				return null;
			}

			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				WORLD.ROADS().bridge.set(tx, ty, true);
				
			}
			
			@Override
			public PLACABLE getUndo() {
				return undo;
			};
	
		};
		
//		PlacableMulti waterway = new PlacableMulti("waterway", "", WORLD.WATER().OCEAN.icon) {
//			
//			@Override
//			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
//				if (WORLD.WATER().isBig.is(tx, ty))
//					return null;
//				return E;
//			}
//
//			@Override
//			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
//				WORLD.PATH().dirMap.set(tx, ty, 1);
//				
//			}
//			
//			@Override
//			public PLACABLE getUndo() {
//				return undo;
//			};
//			
//			@Override
//			public void updateRegardless(GameWindow window) {
//				ov.add();
//			};
//		};
		
		PlacableMulti roadMiniUndo = new PlacableMulti("magnify", "", WORLD.BUILDINGS().sprites.roads.makeSprite(0x0F).resized(Icon.L).twin(UI.icons().s.arrowUp, DIR.N, 0)) {
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				if (WORLD.ROADS().is(tx, ty)) {
					return null;
				}
				return E;
			}

			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				WORLD.ROADS().minified.set(tx, ty, false);
				
			}

		};
		
		PlacableMulti roadMini = new PlacableMulti("minify", "", WORLD.BUILDINGS().sprites.roads.makeSprite(0x0F).resized(Icon.L).twin(UI.icons().s.arrowDown, DIR.S, 0)) {
			
			@Override
			public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
				if (WORLD.ROADS().is(tx, ty)) {
					return null;
				}
				return E;
			}

			@Override
			public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
				WORLD.ROADS().minified.set(tx, ty, true);
				
			}
			
			@Override
			public PLACABLE getUndo() {
				return roadMiniUndo;
			}

		};
		
		placers.add(road);
		placers.add(harbour);
//		placers.add(waterway);
		placers.add(undo);
		placers.add(roadMini);
		placers.add(roadMiniUndo);
	}
	
	
}
