package init.race.appearence;

import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.OPACITY;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.TILE_SHEET;

class RaceFrameRaw {

	public final TILE_SHEET sheet;
	public final int offY;
	public static final int WIDTH = RaceFrameMaker.TILES_X*RaceFrameMaker.TILE_SIZE;
	public static final int HEIGHT = RaceFrameMaker.TILES_Y*RaceFrameMaker.TILE_SIZE;
	private final int hh;
	private final RaceFrameMaker f;
	
	
	RaceFrameRaw(RaceFrameMaker f, TILE_SHEET sheet, int offY){
		this.sheet = sheet;
		this.offY = offY;
		this.f =  f;
		hh = sheet.tiles()/RaceFrameMaker.TILES_X;
	}

	public void render(SPRITE_RENDERER r, int X1, int Y1, int scale) {
		
		Y1 += offY*scale;
		
		int d = scale*RaceFrameMaker.TILE_SIZE;
		
		int i = 0;
		for (int y = 0; y < hh; y++) {
			for (int x = 0; x < RaceFrameMaker.TILES_X; x++) {
				sheet.render(r, i++, X1+x*d, X1+x*d+d, Y1+y*d, Y1+y*d+d);
			}
		}

	}

	public void renderOverlay(SPRITE_RENDERER r, int X1, int Y1, int scale, double blood, double grit, COLOR bloodC) {
		
		int bi = CLAMP.i((int) (blood*4), 0, 4)-1;
		int gi = CLAMP.i((int) (grit*4), 0, 4)-1;
		
		Y1 += offY*scale;
		
		int d = scale*RaceFrameMaker.TILE_SIZE;
		OPACITY.O99.bind();
		if (gi >= 0) {
			int i = 0;
			for (int y = 0; y < hh; y++) {
				for (int x = 0; x < RaceFrameMaker.TILES_X; x++) {
					sheet.renderTextured(f.grit.get(gi).sheet.getTexture(i), i++, X1+x*d, Y1+y*d, scale);
				}
			}
		}
		
		if (bi >= 0) {
			bloodC.bind();
			int i = 0;
			for (int y = 0; y < hh; y++) {
				for (int x = 0; x < RaceFrameMaker.TILES_X; x++) {
					sheet.renderTextured(f.blood.get(bi).sheet.getTexture(i), i++, X1+x*d, Y1+y*d, scale);
				}
			}
			COLOR.unbind();
		}
		OPACITY.unbind();
	}
	
}
