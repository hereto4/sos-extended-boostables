package init.sprite.game;

import java.io.IOException;
import java.nio.file.Path;

import game.GAME;
import init.constant.C;
import init.paths.PATH;
import init.paths.PATHS;
import init.sprite.SPRITES;
import init.sprite.UI.UICons;
import settlement.path.AVAILABILITY;
import snake2d.Errors;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.DIR;
import snake2d.util.file.Json;
import snake2d.util.file.SnakeImage;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TextureCoords;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

public abstract class SheetType implements INDEXED{
	
	private static LinkedList<SheetType> tall = new LinkedList<>();
	public static final c1X1 s1x1 = new c1X1(); 
	public static final cXxX s2x2 = new cXxX(2); 
	public static final cXxX s3x3 = new cXxX(3); 
	public static final cCombo sCombo = new cCombo(); 
	public static final cBox sBox = new cBox(); 
	public static final cTex sTex = new cTex(); 
	public static final LIST<SheetType> ALL = new ArrayList<>(tall);
	static {
		tall = null;
	}
	
	
	
	final String path;
	private final int W,H;
	public final int sizeSize;
	private final int index;
	private final Sheet dummy;
	public final boolean defRotates;
	
	private SheetType(String path, int sizeSize, int W, int H, boolean rot){
		this.path = path;
		index = tall.add(this);
		this.W = W;
		this.H = H;
		this.sizeSize = sizeSize;
		dummy = new Sheet.Dummy(sizeSize*4);
		defRotates = rot;
	}
	
	public abstract TILE_SHEET make(int amount, int y1) throws IOException;
	
	protected LIST<TILE_SHEET> make(String key, Json error) throws IOException {
		
		PATH pp = PATHS.SPRITE_GAME().getFolder(path);
		
		if (!pp.exists(key)) {
			if (error != null) {
				GAME.WarnLight(error.path());
			}
			GAME.WarnLight("The sprite file: " + key + " does not exist in the folder: " + pp.get() + error.path());
			return new ArrayList<TILE_SHEET>(DUMMY);
		}
		
		Path p = pp.get(key);
		
		SnakeImage im = new SnakeImage(p);

		int w = im.width/2;
		int h = im.height;
		im.dispose();
		if (w % W != 0 || h % H != 0)
			throw new Errors.DataError("Image is width is not a multiple of: " + W + ", or image height is not a multiple of: " +  H, p);
		
		w /= W;
		h /= H;
		
		new ComposerThings.IInit(p, w*W*2, h*H);
		
		ArrayList<TILE_SHEET> res = new ArrayList<>(h);
		
		
		
		for (int yy = 0; yy < h; yy++) {
			TILE_SHEET s = make(w, yy*H);
			res.add(s);
		}
		
		return res;
	}
	
	public final int tile(SheetPair s, int data, int variation, int rotation) {
		return tile(s.s, s.d, data, variation, rotation);
	}
	public abstract int tile(Sheet s, SheetData da, int data, int variation, int rotation);
	
	public abstract void renderOverlay(int x, int y, SPRITE_RENDERER r, AVAILABILITY a, int data, int rotation, boolean single);
	
	protected final int getVar(int var, int vars, boolean circular) {

		var &= Integer.MAX_VALUE;
		if (circular) {
			var %= vars + vars-1;
			if (var >= vars) {
				var = vars - (var%vars)-1;
			}
			return var;
		}
		return var % vars;
	}
	
	@Override
	public int index() {
		return index;
	}

	public static class cTex extends SheetType {
		
		cTex(){
			super("texture", 1, 16+6, 16+6, true);
		}
		
		@Override
		protected LIST<TILE_SHEET> make(String key, Json error) throws IOException {
			PATH pp = PATHS.SPRITE_GAME().getFolder(path);
			
			if (!pp.exists(key)) {
				if (error != null) {
					GAME.WarnLight(error.path());
				}
				GAME.WarnLight("The sprite file: " + key + " does not exist in the folder: " + pp.get());
				return new ArrayList<TILE_SHEET>(DUMMY);
			}
			
			Path p = pp.get(key);
			
			SnakeImage im = new SnakeImage(p);

			final int iwidth = im.width/2;
			final int iheight = im.height;
			im.dispose();
			
			int tilesX = (iwidth-12)/16;
			
			if (tilesX*16 + 12 != iwidth)
				throw new Errors.DataError("Image width does not work. resize", p);
			
			int tilesY = iheight/28;
			if (tilesY*28 != iheight)
				throw new Errors.DataError("Image height does not work. resize", p);
		
			new ComposerThings.IInit(p, iwidth*2, iheight);
			
			ArrayList<TILE_SHEET> res = new ArrayList<>(tilesY);
			
			for (int i = 0; i < tilesY; i++) {
				final int k = i;
				TILE_SHEET s = new ITileSheet() {
					
					@Override
					protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
						s.full.init(0, 0, 1, tilesY, tilesX, 1, d.s16);
						s.full.setVar(k).paste(true);
						return d.s16.saveGame();
					}
				}.get();
				res.add(s);
			}
			return res;
		}
		

		@Override
		public int tile(Sheet sheet, SheetData da, int data, int variation, int rotation) {
			
			variation &= 0xFFFF;
			int am = sheet.tiles;
			if (sheet.hasShadow) {
				am -= 1;
			}
			variation = getVar(variation, am, da.circular);
			
			return variation%am;
		}

		@Override
		public void renderOverlay(int x, int y, SPRITE_RENDERER r, AVAILABILITY a, int data, int rotation, boolean single) {
			if (a.player < 0) {
				SPRITES.cons().BIG.filled.render(r, 0, x, y);
			}else if (a.from > 1  || a.player > AVAILABILITY.ROOM.player)
				SPRITES.cons().BIG.dashedThick.render(r, 0, x, y);
			else
				SPRITES.cons().BIG.outline.render(r, 0, x, y);
		}


		@Override
		public TILE_SHEET make(int amount, int y1) throws IOException {
			// TODO Auto-generated method stub
			return null;
		}
		
	}
	
	public static class c1X1 extends SheetType {
		
		c1X1(){
			super("1x1", 1, 16+6, 16+6, true);
		}
		
		
		@Override
		public LIST<TILE_SHEET> make(String key, Json error) throws IOException {
			
			PATH pp = PATHS.SPRITE_GAME().getFolder(path);
			
			if (!pp.exists(key)) {
				if (error != null) {
					GAME.WarnLight(error.path());
				}
				GAME.WarnLight("The sprite file: " + key + " does not exist in the folder: " + pp.get() + error.path());
				return new ArrayList<TILE_SHEET>(DUMMY);
			}
			
			Path p = pp.get(key);
			
			SnakeImage im = new SnakeImage(p);

			final int iwidth = im.width/2;
			final int iheight = im.height;
			im.dispose();
			
			int tilesX = iwidth/((16+6));
			
			if (tilesX*(16+6) != iwidth)
				throw new Errors.DataError("Image is width is not a multiple of: " + (16+6), p);
			
			
			int houseYs = size(iheight, p);
			int houseHeight = 6 + houseYs*16;
			
			
			
			int houses = houseYs*iheight/houseHeight;
		
			new ComposerThings.IInit(p, iwidth*2, iheight);
			
			ArrayList<TILE_SHEET> res = new ArrayList<>(houses);
			
			if (houseYs == 1) {
				for (int yy = 0; yy < houses; yy++) {
					TILE_SHEET s = make(tilesX, yy*houseHeight);
					res.add(s);
				}				
			}else {
				for (int yy = 0; yy < houses; yy++) {
					int y = houseHeight*(yy/houseYs);
					if (yy % houseYs == 0) {
						TILE_SHEET s = makeFirst(tilesX, y);
						res.add(s);
					}else {
						y += 16*(yy % houseYs);
						TILE_SHEET s = make(tilesX, y);
						res.add(s);
					}
				}
			}
			
			
			
			
			return res;
		}
		
		private int size(int h, Path p) {
			for (int i = 1; i < 9; i++) {
				if (h%(16*i + 6) == 0) {
					return i;
				}
			}
			throw new Errors.DataError("Image has wrong dimensions. Image height must be a multiple of x*16+6 pixels. x = how many tiles. Yeah, I can't explain it better...", p);
		}
		
		private TILE_SHEET makeFirst(int w, int y1) throws IOException {
			return new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full2.init(0, y1, w, 1, 1, 1, d.s16);
					for (int i = 0; i < w; i++) {
						s.full2.setVar(i).pasteRotated(2, true);
						s.full2.setVar(i).pasteRotated(3, true);
						s.full2.setVar(i).pasteRotated(0, true);
						s.full2.setVar(i).pasteRotated(1, true);
					}
					return d.s16.saveGame();
				}
			}.get();
		}
		
		@Override
		public TILE_SHEET make(int w, int y1) throws IOException {
			return new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full2.init(0, y1, w, 1, 1, 1, d.s16);
					for (int i = 0; i < w; i++) {
						s.full2.setVar(i).paste(3, true);
					}
					return d.s16.saveGame();
				}
			}.get();
		}
		

		@Override
		public int tile(Sheet sheet, SheetData da, int data, int variation, int rotation) {
			
			variation &= 0xFFFF;
			int am = sheet.tiles;
			int tt = sheet.hasRotation ? 4 : 1;
			if (sheet.hasShadow) {
				am -= tt;
			}
			variation = getVar(variation, am/tt, da.circular);
			
			if (rotation >= 0 && sheet.hasRotation) {
				return (((variation*tt)%am)&~0b011) + (rotation&0b011);
			}else {
				return variation%am;
			}
		}

		@Override
		public void renderOverlay(int x, int y, SPRITE_RENDERER r, AVAILABILITY a, int data, int rotation, boolean single) {
			if (rotation < 0) {
				
				if (a.player < 0) {
					SPRITES.cons().BIG.filled.render(r, 0, x, y);
				}else if (a.from > 1 || a.player > AVAILABILITY.ROOM.player)
					SPRITES.cons().BIG.dashedThick.render(r, 0, x, y);
				else
					SPRITES.cons().BIG.outline.render(r, 0, x, y);
				
			}else {
				int var = 0;
				
				
				if (a.player < 0) {
					;
				}else if (a.from > 1)
					var = 1;
				else
					var = 2;
				if (single) {
					var += 3;
					
				}
				Sheet ss = SPRITES.GAME().overlay(this);
				int t = tile(ss, SheetData.DUMMY, 0, var, rotation);
				ss.render(null, x, y, null, r, t, 0, 0);
			}
		}
		
	}
	
	public static class cXxX extends SheetType {
		
		public final int size;
		
		cXxX(int size){
			super(size + "x" + size, size*size, size*16+12, size*16+12, true);
			this.size = size;
		}
		
		@Override
		public TILE_SHEET make(int w, int y1) throws IOException {
			return new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.combo.init(0, y1, w, 1, size, d.s16);
					for (int i = 0; i < w; i++) {
						s.combo.setVar(i).paste(3, true);
					}
						
					return d.s16.saveGame();
				}
			}.get();
		}
		
		@Override
		public int tile(Sheet sheet, SheetData da, int data, int variation, int rotation) {
			
			int t = data;
			int rot = rotation;
			
			int sizeSizeRot = sizeSize*(sheet.hasRotation ? 4 : 1);
			int vars = (sheet.tiles/sizeSizeRot)-(sheet.hasShadow ? 1 :0);
			
			variation = getVar(variation, vars, da.circular);
			
		
			t += (variation)*sizeSizeRot;
			
			
			if (rotation >= 0 && sheet.hasRotation) {
				return t + rot*sizeSize;
			}
			return t;
		}

		@Override
		public void renderOverlay(int x, int y, SPRITE_RENDERER r, AVAILABILITY a, int data, int rotation,
				boolean single) {
			UICons c = SPRITES.cons().BIG.filled;
			if (a.player < 0) {
				;
			}else if (a.from > 1  || a.player > AVAILABILITY.ROOM.player)
				SPRITES.cons().BIG.dashedThick.render(r, 0, x, y);
			else
				SPRITES.cons().BIG.outline.render(r, 0, x, y);
			
			int m = 0;
			int dx = dx(data);
			int dy = dy(data);

			for (DIR d : DIR.ORTHO) {
				if (dx + d.x() >= 0 && dx + d.x() < size && dy + d.y() >= 0 && dy + d.y() < size)
					m |= d.mask();
			}
			
			c.render(r, m, x, y);
			
			if (rotation >= 0) {
				
				COLOR.WHITE50.bind();
				Sheet ss = SPRITES.GAME().overlay(this);
				int t = tile(ss, SheetData.DUMMY, data&0b0111111, 0, rotation);
				ss.render(null, x, y, null, r, t, t, 0);
				COLOR.unbind();
			}
			
		}
		
		public int dx(int data) {
			return (data&0b0111111)%size;
		}
		
		public int dy(int data) {
			return (data&0b0111111)/size;
		}
		
	}
	
	public static final class cCombo extends SheetType {
		
		private cCombo() {
			super("combo", 16, 72, 72, false);
		}

		@Override
		public TILE_SHEET make(int amount, int y1) throws IOException {
			TILE_SHEET s =  new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					
					s.house.init(0, y1, amount, 1, d.s16);
					for (int i = 0; i < amount; i++)
						s.house.setVar(i).paste(true);
					return d.s16.saveGame();
				}
			}.get();
			
			
			
			return s;
		}

		@Override
		public int tile(Sheet s, SheetData da, int data, int variation, int rotation) {
			int vars = s.tiles/16;
			if (s.hasShadow)
				vars -= 1;
			variation = getVar(variation, vars, da.circular);
			return data + (variation)*16;
		}

		@Override
		public void renderOverlay(int x, int y, SPRITE_RENDERER r, AVAILABILITY a, int data, int rotation,
				boolean single) {
			if (a.player < 0) {
				SPRITES.cons().BIG.filled.render(r, data&0x0F, x, y);
			}else if (a.from > 1  || a.player > AVAILABILITY.ROOM.player)
				SPRITES.cons().BIG.dashedThick.render(r, data&0x0F, x, y);
			else
				SPRITES.cons().BIG.outline.render(r, data&0x0F, x, y);

		}
		
	}
	
	public static final class cBox extends SheetType {
		
		private final int[][] boxI = new int[16][];
		
		private cBox() {
			super("box", 16, 76, 76,true);
			boxI[DIR.E.mask() | DIR.S.mask()] = new int[] {0};
			boxI[DIR.S.mask() | DIR.E.mask() | DIR.W.mask()] = new int[] {1,2};
			boxI[DIR.W.mask() | DIR.S.mask()] = new int[] {3};
			boxI[DIR.E.mask() | DIR.N.mask() | DIR.S.mask()] = new int[] {4,8};
			boxI[0x0F] = new int[] {5,6,9,10};;
			boxI[DIR.W.mask() | DIR.N.mask() | DIR.S.mask()] = new int[] {7,11};
			boxI[DIR.N.mask() | DIR.E.mask()] = new int[] {12};
			boxI[DIR.N.mask() | DIR.W.mask() | DIR.E.mask()] = new int[] {13,14};;
			boxI[DIR.N.mask() | DIR.W.mask()] = new int[] {15};
		}

		@Override
		public TILE_SHEET make(int amount, int y1) throws IOException {
			return new ITileSheet() {
				
				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.combo.init(0, y1, amount, 1, 4, d.s16);
					for (int i = 0; i < amount; i++)
						s.combo.setVar(i).paste(1, true);
					return d.s16.saveGame();
				}
			}.get();
		}

		@Override
		public int tile(Sheet s, SheetData da, int data, int variation, int rotation) {
			int[] ids = boxI[data];
			if(ids == null)
				return 0;
			
			data = ids[variation%ids.length];
			variation = variation >> 4;
			rotation &= 1;
			int ss = (16* ((s.hasRotation ? 2 : 1)));
			int vars = s.tiles/ss;
			variation = getVar(variation, vars, da.circular);
			return data + (variation)*ss + rotation*16;
		}

		@Override
		public void renderOverlay(int x, int y, SPRITE_RENDERER r, AVAILABILITY a, int data, int rotation,
				boolean single) {
			if (a.player < 0) {
				SPRITES.cons().BIG.filled.render(r, data&0x0F, x, y);
			}else if (a.from > 1  || a.player > AVAILABILITY.ROOM.player)
				SPRITES.cons().BIG.dashedThick.render(r, 0, x, y);
			else
				SPRITES.cons().BIG.outline.render(r, 0, x, y);

		}
		
	}

	public Sheet dummy() {
		return dummy;
	}

	public static final TILE_SHEET DUMMY = new TILE_SHEET() {
		
		@Override
		public int tiles() {
			return 16;
		}
		
		@Override
		public int size() {
			return C.TILE_SIZE;
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
			COLOR.WHITE100.render(r, x1, x2, y1, y2);
			
		}
		
		@Override
		public TextureCoords getTexture(int tile) {
			return COLOR.WHITE100.texture();
		}
	};
	
}