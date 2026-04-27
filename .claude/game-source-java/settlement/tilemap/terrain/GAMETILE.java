package settlement.tilemap.terrain;

import snake2d.util.sprite.SPRITE;

interface GAMETILE {

	/**
	 * 
	 * @return true if a client can safely place this tile
	 */
	public abstract boolean isPlacable(int tx, int ty);

	/**
	 * place the tile on the map and fix surrounding tiles.
	 * 
	 * @param x
	 * @param y
	 */
	public abstract void placeFixed(int tx, int ty);
	
	/**
	 * 
	 * @return get an small image representing this tile
	 */
	public abstract SPRITE getIcon();

	/**
	 * 
	 * @return the name of the tile
	 */
	public abstract CharSequence name();
}
