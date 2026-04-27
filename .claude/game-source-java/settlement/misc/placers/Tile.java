package settlement.misc.placers;

import snake2d.util.sprite.SPRITE;

interface Tile {

	boolean placable(int tx, int ty, TileGrid grid, int rx, int ry);
	void place(int tx, int ty, TileGrid grid, int rx, int ry);
	SPRITE sprite(TileGrid grid, int rx, int ry, int mask);
}
