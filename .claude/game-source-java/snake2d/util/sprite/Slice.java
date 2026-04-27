package snake2d.util.sprite;

import snake2d.SPRITE_RENDERER;

final class Slice implements TILE_SHEET {

	final TILE_SHEET combo;
	final int start;
	final int size;
	
	
	Slice(TILE_SHEET combo, int start, int end){
		this.combo = combo;
		this.start = start;
		this.size = end-this.start;
	}
	
	@Override
	public void render(SPRITE_RENDERER r, int tile, int x1, int x2, int y1, int y2) {
		combo.render(r, tile+start, x1, x2, y1, y2);
		
	}

	@Override
	public void renderTextured(TextureCoords texture, int tile, int x1, int y1) {
		combo.renderTextured(texture, tile+start, x1, y1);
		
	}

	@Override
	public void renderTextured(TextureCoords texture, int tile, int x1, int x2, int scale) {
		combo.renderTextured(texture, tile+start, x1, x2, scale);
		
	}

	@Override
	public TextureCoords getTexture(int tile) {
		return combo.getTexture(tile+start);
	}

	@Override
	public int size() {
		return combo.size();
	}

	@Override
	public int tiles() {
		return size;
	}
	
}