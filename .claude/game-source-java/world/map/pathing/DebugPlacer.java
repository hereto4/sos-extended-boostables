package world.map.pathing;

import init.constant.C;
import init.sprite.SPRITES;
import snake2d.PathTile;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.Coo;
import view.main.VIEW;
import view.subview.GameWindow;
import view.tool.PlacableSimpleTile;
import world.WORLD;
import world.map.pathing.WRegFinder.Treaty;

public class DebugPlacer extends PlacableSimpleTile{

	private Coo clicked = new Coo();
	
	
	public DebugPlacer() {
		super("world path");
		
	}


	@Override
	public CharSequence isPlacable(int tx, int ty) {
		return WORLD.PATH().map.is.is(tx, ty) ? null : E;
	}


	@Override
	public void place(int tx, int ty) {
		clicked.set(tx, ty);
		
	}

	@Override
	public void renderOverlay(GameWindow window) {
		
		
	}
	
	
	@Override
	public void renderExtra(SPRITE_RENDERER r) {
		if (!WORLD.PATH().map.is.is(clicked))
			return;
		
		PathTile t = WORLD.PATH().path(clicked, VIEW.world().window.tile(), Treaty.DUMMY);
		
		GameWindow w = VIEW.world().window;

		while (t != null) {
			
			int x = (t.x()-w.tile().x())*C.TILE_SIZE + w.tile().rel().x();
			int y = (t.y()-w.tile().y())*C.TILE_SIZE + w.tile().rel().y();
			SPRITES.cons().BIG.line.render(r, 0, x, y);
			t = t.getParent();
		}
		
		
	}
	
}
