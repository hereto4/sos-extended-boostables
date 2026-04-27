package settlement.misc.placers;

import snake2d.util.datatypes.DIMENSION;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_OBJECT;

final class TileGrid implements MAP_OBJECT<Tile>, DIMENSION{

	private final Tile[][] tiles;
	final MAP_BOOLEAN isIn = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			if (tx < 0 || tx >= tiles[0].length)
				return false;
			if (ty < 0 || ty >= tiles.length)
				return false;
			return true;
		}
		
		@Override
		public boolean is(int tile) {
			throw new RuntimeException();
		}
	};
	
	public TileGrid(Tile[][] tiles) {
		this.tiles = tiles;
	}

	@Override
	public Tile get(int tile) {
		throw new RuntimeException();
	}

	@Override
	public Tile get(int tx, int ty) {
		if (tx < 0 || tx >= tiles[0].length)
			return null;
		if (ty < 0 || ty >= tiles.length)
			return null;
		return tiles[ty][tx];
	}

	@Override
	public int width() {
		return tiles[0].length;
	}

	@Override
	public int height() {
		return tiles.length;
	}
	
}
