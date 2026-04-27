package settlement.misc.placers;

import init.sprite.SPRITES;
import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.sprite.SPRITE;
import view.tool.PlacableFixedImp;

class Placer extends PlacableFixedImp{

	private final CharSequence name;
	private final TileGrid grid;
	
	Placer(CharSequence name, TileGrid grid){
		super(name, 1, 1);
		this.name = name;
		this.grid = grid;
	}
	
	@Override
	public SPRITE getIcon() {
		return SPRITES.icons().m.cancel;
	}

	@Override
	public CharSequence name() {
		return name;
	}
	
	@Override
	public void renderPlaceHolder(SPRITE_RENDERER r, int mask, int x, int y, int tx, int ty, int rx, int ry,
			boolean isPlacable, boolean areaIsPlacable) {
		grid.get(rx, ry).sprite(grid, rx, ry, mask).render(r, x, y);
	}

	@Override
	public int width() {
		return grid.width();
	}

	@Override
	public int height() {
		return grid.height();
	}

	@Override
	public CharSequence placable(int tx, int ty, int rx, int ry) {
		return SETT.IN_BOUNDS(tx, ty) && grid.get(rx, ry).placable(tx, ty, grid, rx, ry) ? null : E;
	}

	@Override
	public void place(int tx, int ty, int rx, int ry) {
		grid.get(rx, ry).place(tx, ty, grid, rx, ry);
	}

}
