package settlement.tilemap.floor;

import static settlement.main.SETT.TERRAIN;

import java.io.IOException;

import init.constant.C;
import init.paths.PATHS;
import init.sprite.SPRITES;
import settlement.main.SETT;
import snake2d.CORE;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.color.OpacityImp;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TextureCoords;
import snake2d.util.sprite.TileTexture.TileTextureScroller;
import util.rendering.RenderData;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

final class GrassRenderer {
	
	private final TILE_SHEET sheet;
	@SuppressWarnings("unused")
	private final TILE_SHEET sheetMask;
	private final TILE_SHEET moss;
	
	private final Colors colors;
	private final TileTextureScroller dis2 = SPRITES.textures().dis_low.scroller(12.0*6, -12*5.5);
	private final Grass grass;
	private final static int SET = 16;
	private final int[] tts = {
		-1,0,0,1,1,2,2,3,3,4,4,5,5,6,7,7,
	};
	
	private final OPACITY[] op = new OPACITY[Grass.TYPES];
	{
		for (int i = 0; i < Grass.TYPES; i++) {
			int p = (int) (100*(i+1.0)/Grass.TYPES);
			op[i] = new OpacityImp(p);
 		}
	}
	
	public GrassRenderer(Grass grass) throws IOException{
		
		this.grass = grass;
		new ComposerThings.IInit(PATHS.SPRITE_SETTLEMENT_MAP().get("Grass"), 972, 390);
		
		colors = new Colors();
		
		sheetMask = (new ITileSheet() {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				ComposerDests.Tile t = d.s24;
				final ComposerSources.Full f = s.full;
				f.init(0, f.body().y2(), 1, 1, 4, 4, t);
				f.setVar(0).paste(true);
				return t.saveGame();

			}
		}).get();
		
		sheet = (new ITileSheet() {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				ComposerDests.Tile t = d.s24;
				final ComposerSources.Singles f = s.singles;
				f.init(0, s.full.body().y2(), 1, 1, 16, 8, t);
				f.setVar(0).paste(true);
				return t.saveGame();

			}
		}).get();
		
		moss = (new ITileSheet(PATHS.SPRITE_SETTLEMENT_MAP().get("Moss"), 792, 108) {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				ComposerDests.Tile t = d.s24;
				final ComposerSources.Full f = s.full;
				f.init(0, 0, 1, 1, 16, 4, t);
				f.setVar(0).paste(true);
				return t.saveGame();

			}
		}).get();
		
	}
	void update(double ds) {
		colors.update(ds);
		double w = Math.pow(SETT.WEATHER().wind.getD(), 1.5)*0.5;
		if (w > 0.1)
			dis2.update(ds*w);
	}
	
	
	public void render(double ds, Renderer r, RenderData data) {
		
		RenderData.RenderIterator it = data.onScreenTiles(1,1,1,1);
		
		while(it.has()) {
			
			render(it, r);
			it.next();
		}
		COLOR.unbind();
		
	}
	
	public void render(RenderData.RenderIterator it, Renderer r) {
		int ran = it.ran();
		
		int colC = grass.currentI.get(it.tile());
		int c = tts[colC];
		
		if (colC == 1)
			c -= (ran&0b011);
		if (colC == 2)
			c -= (ran&0b001);
		if (colC == 3)
			c -= (ran&0b01);
		
		if (colC == 5)
			c -= (ran&0b0001);
		
		
		ran = ran>>2;
		
		
		
		
		if (c >= 0) {
			
			int m = SETT.MINERALS().amountInt.get(it.tile())>>2;
			c = CLAMP.i(c-m, 0, c);
			
			
			int d = (int) (((ran&0x07)-7)*C.SCALE);
			ran = ran >> 3;
			int x = it.x() + d;
			d = (int) (((ran&0x07)-7)*C.SCALE);
			ran = ran >> 3;
			int y = it.y() + d;
			if (TERRAIN().get(it.tile()).roofIs()) {
				COLOR.unbind();
				c = (c+1)>>3;
				moss.render(r, c*SET+(it.ran()&0x0F), x, y);
			}else {
//				if (SETT.FLOOR().getter.is(it.tile()))
//					c = CLAMP.i(c, 0, 1);
				
				if (!SETT.ROOMS().map.is(it.tile())) {
//					c = CLAMP.i((int) ((c+(ran&0b011))*growth), 0, c);
//					ran = ran >> 2;
				}
//				
//				int mm = 0;
//				if (CORE.renderer().getZoomout() <= 1 && !SETT.ROOMS().map.is(it.tile())) {
//					if (grass.currentI.get(it.tx(), it.ty(), DIR.W) == 0) {
//						mm |= 1;
//						x = it.x()-8*C.SCALE;
//					}else if (grass.currentI.get(it.tx(), it.ty(), DIR.E) == 0)
//						x = it.x()-8*C.SCALE;
//					if (grass.currentI.get(it.tx(), it.ty(), DIR.N) == 0) {
//						mm |= 2;
//						y = it.y()-8*C.SCALE;
//					}else if(grass.currentI.get(it.tx(), it.ty(), DIR.S) == 0)
//						y = it.y()-8*C.SCALE;
//					
//					mm += 4*(ran&3);
//					ran = ran >> 2;
//				}
				
				
				
				colors.get(colC-1, it.ran()).bind();
				//colorMask.bind();
				sheet.render(r, c*SET+(ran&0x0F), x, y);
				//sheetMask.renderTextured(, mm, x, y);
				
				
				op[colC-1].bind();
				TextureCoords t = SPRITES.textures().dots.get(it.tx(), it.ty(), 0, 0);
				
				CORE.renderer().renderDisplaced(it.x(), it.x()+C.TILE_SIZE, it.y(), it.y()+C.TILE_SIZE, 0.4, dis2.get(it.tx(), it.ty()), t);
				OPACITY.unbind();
//				if (c >= 4 && mm == 0)
//					i.hiddenSet();
			}
			
		}
	}
	

	
	public COLOR color(int ran) {
		return colors.get(4, ran);
	}
	
	private static final class Colors {
		
		private final static int RAN = 4;
		
		private final COLOR[][] c_base = new COLOR[Grass.TYPES][RAN];
		private final ColorImp[][] c_current = new ColorImp[Grass.TYPES][RAN];
		
		private final COLOR dry;
		private final COLOR winter;

		
		Colors() throws IOException{
			COLOR fertile = new ComposerThings.IColorSamplerSingle() {
				
				@Override
				protected COLOR init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, 0, 1, 1, 8, 1, d.s24);
					return s.full.sample();
				}
			}.getHalf();
			
			COLOR infertile = new ComposerThings.IColorSamplerSingle() {
				
				@Override
				protected COLOR init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					
					s.full.setSkip(1, 1);
					return s.full.sample();
				}
			}.getHalf();
			
			dry = new ComposerThings.IColorSamplerSingle() {
				
				@Override
				protected COLOR init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					
					s.full.setSkip(1, 2);
					return s.full.sample();
				}
			}.getHalf();
			
			winter = new ComposerThings.IColorSamplerSingle() {
				
				@Override
				protected COLOR init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					
					s.full.setSkip(1, 3);
					return s.full.sample();
				}
			}.getHalf();
			
			int skip = 5;
			
			COLOR[] cols = COLOR.interpolate(infertile, fertile, Grass.TYPES-4);
			
			
			
			for (int i = 0; i < Grass.TYPES; i++) {
				COLOR c = cols[CLAMP.i(i-skip, 0, i)];
				
				c_base[i][0] = c;
				c_current[i][0] = new ColorImp(c_base[i][0]);
				for (int k = 1; k < RAN; k++) {
					c_base[i][k] = c.shade(RND.rFloat1(0.05));
					c_current[i][k] = new ColorImp(c_base[i][k]);
				}
				
			}
		}
		
		private COLOR get(int c, int ran) {
			return c_current[c][ran & (RAN-1)];
		}
		
		void update(double ds) {
			double m = SETT.WEATHER().moisture.getD();
			if (m < 0.25) {
				m = 0.05;
			}else if (m < 0.5) {
				m -= 0.25;
				m /= 0.25;
				m = CLAMP.d(m, 0.05, 1);
			}else {
				m = 1;
			}
			set(m, 1.0-SETT.WEATHER().growth.getD());
		}
		
		private final ColorImp tmp = new ColorImp();
		
		private void set(double moist, double winter) {
			for (int a = 0; a < Grass.TYPES; a++) {
				for (int b = 0; b < RAN; b++) {
					tmp.interpolate(dry, c_base[a][b], moist);
					c_current[a][b].interpolate(tmp, this.winter, winter*0.75);
				}
			}
		}
		
	}
	
}