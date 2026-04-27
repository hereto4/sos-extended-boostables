package snake2d.util.sprite;

import snake2d.SPRITE_RENDERER;

public interface TILE_SHEET {

	public default void renderC(SPRITE_RENDERER r, int tile, int cx, int cy) {
		render(r, tile, cx-size()/2, cy-size()/2);
	}
	public default void render(SPRITE_RENDERER r, int tile, int x1, int y1) {
		render(r, tile, x1, x1+size(), y1, y1+size());
	}
	
	
	public void render(SPRITE_RENDERER r, int tile, int x1, int x2, int y1, int y2);
	public  void renderTextured(TextureCoords texture, int tile, int x1, int y1);
	public void renderTextured(TextureCoords texture, int tile, int x1, int x2, int scale);
	public TextureCoords getTexture(int tile);
	public int size();
	public int tiles();
	
	public default SPRITE makeSprite(int tile) {
		return new SPRITE.SpriteFromSheet(this, tile);
	}

	public default TILE_SHEET slice(int from, int to) {
		if (from == 0 && to == 1)
			return this;
		return new Slice(this, from, to);
	}
	
	public static final TILE_SHEET DUMMY = new TILE_SHEET() {
		
		@Override
		public int tiles() {
			// TODO Auto-generated method stub
			return 16;
		}
		
		@Override
		public int size() {
			// TODO Auto-generated method stub
			return 0;
		}
		
		@Override
		public void renderTextured(TextureCoords texture, int tile, int x1, int x2, int scale) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void renderTextured(TextureCoords texture, int tile, int x1, int y1) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public void render(SPRITE_RENDERER r, int tile, int x1, int x2, int y1, int y2) {
			// TODO Auto-generated method stub
			
		}
		
		@Override
		public TextureCoords getTexture(int tile) {
			// TODO Auto-generated method stub
			return null;
		}
	};
	
}
