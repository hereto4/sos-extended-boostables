package world.map.buildings;

import static world.WORLD.BUILDINGS;
import static world.WORLD.IN_BOUNDS;
import static world.WORLD.TWIDTH;

import init.sprite.UI.UI;
import snake2d.util.datatypes.AREA;
import util.text.D;
import util.text.Dic;
import view.subview.GameWindow;
import view.tool.PLACABLE;
import view.tool.PLACER_TYPE;
import view.tool.PlacableMessages;
import view.tool.PlacableMulti;
import world.WORLD;

class Placer extends PlacableMulti{

	private static CharSequence ¤¤name = "village";
	
	private final PlacableMulti undo = new PlacableMulti(Dic.¤¤remove + ": " + ¤¤name, "", UI.icons().m.building.twin(UI.icons().m.anti)) {
		
		@Override
		public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
			BUILDINGS().village.set(tx, ty, false);
		}
		
		@Override
		public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
			return BUILDINGS().village.is(tx, ty) ? null : E;
		}
		
		@Override
		public void updateRegardless(GameWindow window, AREA selected) {
			BUILDINGS().debugVisible = true;
		}
	};
	
	static {
		D.ts(Placer.class);
	}
	
	Placer(){
		super(¤¤name, "", UI.icons().m.building);
	}
	
	@Override
	public void place(int tx, int ty, AREA area, PLACER_TYPE type) {
		int tile = tx + ty*TWIDTH();
		BUILDINGS().village.set(tile, true);
		
		
	}
	
	@Override
	public CharSequence isPlacable(int tx, int ty, AREA area, PLACER_TYPE type) {
		if (!IN_BOUNDS(tx, ty))
			return PlacableMessages.¤¤IN_MAP;
		if (WORLD.REGIONS().isCentre.is(tx, ty))
			return PlacableMessages.¤¤BLOCKED;
		return null;
	}
	
	@Override
	public void updateRegardless(GameWindow window, AREA selected) {
		BUILDINGS().debugVisible = true;
	}
	
	@Override
	public PLACABLE getUndo() {
		return undo;
	}
		
	
}
