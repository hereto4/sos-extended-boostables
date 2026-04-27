package init.sprite.UI;

import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TextureCoords;
import util.spritecomposer.SpriteData;

public class Icon implements SPRITE {

	public static final int S = 16;
	public static final int M = 24;
	public static final int L = 32;
	public static final int HUGE = 64;
	
	
	public final int size;
	
	private final SPRITE sprite;
	public final SPRITE huge;
	public final SPRITE big;
	public final SPRITE small;
	public final SPRITE medium;
	

	public Icon(int size, SPRITE s){
		this.size = size;
		this.sprite = s;
		big = size == L ? this : new SPRITE.Scaled(this, L, L);
		small = size == S ? this : new SPRITE.Scaled(this, S, S);
		medium = size == M ? this : new SPRITE.Scaled(this, M, M);
		huge = size == HUGE ? this : new SPRITE.Scaled(this, HUGE, HUGE);
	}
	
	public Icon(SpriteData data){
		this(data.width, new SPRITE.SpriteImp(data.x1, data.x1+data.width, data.y1, data.y1+data.height, data.width, data.height));
	}
	
	public Icon(SPRITE sprite){
		this(sprite.width(), sprite);
	}
	
	public Icon twin(SPRITE b) {
		return new Icon(size, new SPRITE.Twin(this, b));
	}
	
	@Override
	public Icon createColored(COLOR color) {
		return new Icon(SPRITE.super.createColored(color));
	}
	
	@Override
	public Icon twin(SPRITE b, DIR align, int shadow) {
		return new Icon(size, SPRITE.super.twin(b, align, shadow));
	}
	
	@Override
	public int width() {
		return size;
	}

	@Override
	public int height() {
		return size;
	}

	@Override
	public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
		sprite.render(r, X1, X2, Y1, Y2);
	}

	@Override
	public void renderTextured(TextureCoords other, int X1, int X2, int Y1, int Y2) {
		sprite.renderTextured(other, X1, X2, Y1, Y2);
	}
	
	static class IconSheet extends Icon {
		
		private final TILE_SHEET sheet;
		private final int tile;
		
		IconSheet(int size, TILE_SHEET sheet, int tile){
			super(size, new SPRITE.Imp(size) {
				
				@Override
				public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
					sheet.render(r, tile, X1, X2, Y1, Y2);
				}
			});
			this.sheet = sheet;
			this.tile = tile;
		}
		
		@Override
		public TextureCoords texture() {
			return sheet.getTexture(tile);
		}
		
	}
	
}