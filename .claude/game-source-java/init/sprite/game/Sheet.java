package init.sprite.game;

import settlement.main.SETT;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TextureCoords;
import util.rendering.RenderData.RenderIterator;
import util.rendering.ShadowBatch;

public abstract class Sheet {
	
	public final boolean hasRotation;
	public final boolean hasShadow;
	public final int tiles;
	
	public Sheet(int tiles, boolean rots, boolean sha) {
		this.tiles = tiles;
		this.hasRotation = rots;
		this.hasShadow = sha;
	}
	
	public abstract void render(SheetData da, int x, int y, RenderIterator it, SPRITE_RENDERER sr, int tile, int random, double degrade);
	public abstract void renderShadow(SheetData da, int x, int y, RenderIterator it, ShadowBatch shadow, int tile, int random);
	
	public abstract TextureCoords texture(int tile);
	
	public static class Imp extends Sheet {
		
		public final TILE_SHEET sheet;
		private final int varSize;

		
		public Imp(SheetType type, TILE_SHEET sheet, boolean rotates){
			super(sheet.tiles(), rotates&type.defRotates, shadow(sheet, type.sizeSize*((rotates&type.defRotates)  ? 4 :1)));
			this.sheet = sheet;
			varSize = type.sizeSize*((rotates&type.defRotates) ? 4 :1);
		}
		

		
		static boolean shadow(TILE_SHEET sheet, int size) {
			int i = sheet.tiles()/(size);
			return i > 1 && (i & 1) == 1;
		}

		
		@Override
		public void render(SheetData da, int x, int y, RenderIterator it, SPRITE_RENDERER sr, int tile, int random, double degrade) {
			sheet.render(sr, tile, x, y);
			if (degrade > 0.05) {
				OPACITY.O99.bind();
				sheet.renderTextured(SETT.ROOMS().util.filth.texture(degrade, it.ran()), tile, x, y);
				OPACITY.unbind();
			}
		}
		
		@Override
		public void renderShadow(SheetData da, int x, int y, RenderIterator it, ShadowBatch shadow, int tile, int random) {
			if (da.shadowLength > 0 || da.shadowHeight > 0) {
				shadow.setHeight(da.shadowLength).setDistance2Ground(da.shadowHeight);
				int t = tile;
				if (this.hasShadow) {
					t = sheet.tiles()-(varSize);
					t += tile%((varSize));
				}
				sheet.render(shadow, t, x, y);
			}
		}
		
		public TILE_SHEET sheet() {
			return sheet;
		}
		
		@Override
		public TextureCoords texture(int tile) {
			return sheet.getTexture(tile);
		}
	}
	
	static class Dummy extends Sheet {

		public Dummy(int tiles){
			super(tiles, true, false);
		}


		
		@Override
		public void render(SheetData da, int x, int y, RenderIterator it, SPRITE_RENDERER sr, int tile, int random, double degrade) {
			
		}
		
		@Override
		public void renderShadow(SheetData da, int x, int y, RenderIterator it, ShadowBatch shadow, int tile, int random) {
			
		}



		@Override
		public TextureCoords texture(int tile) {
			return COLOR.WHITE100.texture();
		}

	}
	


}
