package util.spritecomposer;

import snake2d.SPRITE_RENDERER;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TextureCoords;
import util.spritecomposer.Optimizer.Tile;

class TIleSheetPow2 implements TILE_SHEET{
	
	private final int startTile;
	private final int mask;
	private final int scrollY;
	private final int quadSize;
	private final int tiles;
	private final Tile t;
	private final int scale;

	private static final TextureCoords[] texs = new TextureCoords[] {
		new TextureCoords(),
		new TextureCoords()
	};
	private static int tI = 0;
	
	TIleSheetPow2(int scale, int tileSize, int startTile, int tilesX, int tiles) {

		t = Optimizer.get(tileSize);
		this.scale = scale;
		
		this.startTile = startTile;
		
		int m = 1;
		int scroll = 1;
		if (tilesX % 2 != 0)
			throw new RuntimeException();
		while ((tilesX /= 2) > 1) {
			m = m << 1;
			m |= 1;
			scroll++;
		}
		mask = m;
		scrollY = scroll;

		quadSize = tileSize * scale;
		this.tiles = tiles;
	}

	@Override
	public void render(SPRITE_RENDERER r, int tile, int x1, int y1) {
		t.render(r, tile+startTile, x1, y1, scale);
	}

	@Override
	public TextureCoords getTexture(int tile) {
		tile += startTile;
		int tx = tile & mask;
		int ty = tile >> scrollY;
			tI++;
		return texs[tI&1].get(
				(tx * quadSize/scale),
				t.startY + (ty * t.size),
				t.size,
				t.size
				);
	}

	@Override
	public void renderTextured(TextureCoords t, int tile, int x1, int y1) {
		if (tile < 0)
			return;

		this.t.renderTextured(t, tile+startTile, x1, y1, scale);
		
//		tile += startTile;
//		int tx = tile & mask;
//		int ty = tile >> scrollY;
//
//		int px = startPixelX + (tx * tileSize);
//		int py = startPixelY + (ty * tileSize);
//
//		CORE.renderer().renderTextured(
//				x1, x1 + quadSize, y1, y1 + quadSize, 
//				t,
//				TextureCoords.Normal.get(px, py, tileSize, tileSize)
//				);

	}
	
	@Override
	public void renderTextured(TextureCoords t, int tile, int x1, int y1, int scale) {
		this.t.renderTextured(t, tile+startTile, x1, y1, scale);
	}

	@Override
	public int size() {
		return quadSize;
	}
	
	@Override
	public int tiles(){
		return tiles;
	}

	@Override
	public void render(SPRITE_RENDERER r, int tile, int x1, int x2, int y1, int y2) {
		if (tile < 0)
			return;

		tile += startTile;
		int tx = tile & mask;
		int ty = tile >> scrollY;

		int px = (tx * t.size);
		int py = t.startY + (ty * t.size);

		r.renderSprite(x1, x2, y1, y2, TextureCoords.Normal.get(px, py, t.size, t.size));
	}




	
}