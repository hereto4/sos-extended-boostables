package snake2d;

import snake2d.util.sprite.TextureCoords;

public interface SPRITE_RENDERER{
	
	/**
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @param tx1
	 * @param tx2
	 * @param ty1
	 * @param ty2
	 */
	public default void renderSprite(int x1, int x2, int y1, int y2, TextureCoords texture) {
		renderSprite(x1, x2, y1, y2, texture);
	}
	
	
	public final SPRITE_RENDERER DUMMY = new SPRITE_RENDERER() {

		@Override
		public void renderSprite(int x1, int x2, int y1, int y2, TextureCoords texture) {
			// TODO Auto-generated method stub
			
		}
	};
	
}

