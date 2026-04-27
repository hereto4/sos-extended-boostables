package launcher;

import java.io.IOException;

import init.paths.PATHS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.file.Json;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.text.Font;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerFonter;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings.IFont;
import util.spritecomposer.ComposerThings.IInit;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerThings.ITileSheetL;
import util.spritecomposer.ComposerUtil;

final class RES {

	public final Font font;
	public final SPRITE[] clouds;
	public final SPRITE logo;
	public final SPRITE[] smallPanel = new SPRITE[3];
	public final SPRITE[] social;
	public final SPRITE[] arrowUpDown;
	public final SPRITE[] arrowLR;
	public final SPRITE[] langs;
	public final SPRITE bg;
	
	RES() throws IOException{
		
		Json json = new Json(PATHS.CONFIG().get("Charset"));
		CharSequence cs = json.text("CHARS");
		final int trail = json.i("SPACING", 0, 32, 0);
		
		Font.setCharset(cs);
		font = new IFont(PATHS.SPRITE().getFolder("font").get("Medium")) {
			@Override
			protected Font init(ComposerUtil c, ComposerFonter f) {
				
				return f.save(0, 0, trail);
			}
		}.get(trail);
		
		new IInit(PATHS.BASE().LAUNCHER.getFolder("assets").get("Sprites"), 952, 318);
		
		clouds = sprite(0, 4, 4, 6, 2);
		logo = sprite(70, 24, 4, 2);
		smallPanel[0] = sprite(140, 29, 2, 2);
		smallPanel[1] = sprite(178, 29, 1, 2);
		smallPanel[2] = sprite(200, 29, 1, 2);
		
		{
			TILE_SHEET ls = new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.init(0, 222, 1, 1, 10, 1, d.s24);
					s.singles.paste(true);
					return d.s24.save(1);
				}
				
			}.get();
			
			social = new SPRITE[] {
				ls.makeSprite(0),
				ls.makeSprite(1),
				ls.makeSprite(2),
				ls.makeSprite(3),
			};
			arrowUpDown = new SPRITE[] {
				ls.makeSprite(4),
				ls.makeSprite(5),
				
			};
			arrowLR = new SPRITE[] {
				ls.makeSprite(6),
				ls.makeSprite(7),
			};
		}
		{
			TILE_SHEET ls = new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.singles.init(0, 252, 1, 1, 14, 2, d.s24);
					s.singles.paste(true);
					return d.s24.save(1);
				}
				
			}.get();
			langs = new SPRITE[14*2];
			for (int i = 0; i < langs.length; i++) {
				langs[i] = ls.makeSprite(i);
			}
			
		}

		
		
		{
			TILE_SHEET sh = new ITileSheet(PATHS.BASE().LAUNCHER.getFolder("assets").get("BG"), 920, 236) {
	
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, 0, 1, 1, 28, 14, d.s16);
					s.full.paste(true);
					return d.s16.save(1);
				}
				
			}.get();
			
			bg = toSprite(sh, 28, 1);
		}
		
	}

	
	private SPRITE sprite(int y1, int tilesX, int tilesY, int scale) throws IOException {

		
		TILE_SHEET ls = new ITileSheet() {

			@Override
			protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(0, y1, 1, 1, tilesX, tilesY, d.s16);
				s.full.paste(true);
				return d.s16.save(1);
			}
			
		}.get();
		
		return toSprite(ls, tilesX, scale);
		
	}
	
	private SPRITE[] sprite(int y1, int tilesX, int tilesY, int amount, int scale) throws IOException {
		
		SPRITE[] res = new SPRITE[amount];
		
		LIST<TILE_SHEET> ls = new ITileSheetL() {

			@Override
			protected int init(ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.init(0, y1, amount, 1, tilesX, tilesY, d.s16);
				return amount;
			}

			@Override
			protected TILE_SHEET next(int i, ComposerUtil c, ComposerSources s, ComposerDests d) {
				s.full.setVar(i).paste(true);
				return d.s16.save(1);
			}
			
		}.get();
		
		for (int i = 0; i < res.length; i++) {
			res[i] = toSprite(ls.get(i), tilesX, scale);
		}
		
		return res;
		
	}
	
	private SPRITE toSprite(TILE_SHEET sh, int tilesX, int scale) {
		int w = tilesX*sh.size();
		int h = (int) (sh.size()*(Math.ceil(sh.tiles()/tilesX)));
		
		return new SPRITE.Imp(w*scale, h*scale) {
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				int d =(X2-X1)/tilesX;
				
				for (int i = 0; i < sh.tiles(); i++) {
					int x = X1 + d*(i%tilesX);
					int y = Y1 + d*(i/tilesX);
					sh.render(r, i, x, x+d, y, y+d);
				}
			}
		};
		
	}
	
}
