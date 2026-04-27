package util.spritecomposer;

import static util.spritecomposer.Resources.c;
import static util.spritecomposer.Resources.dests;
import static util.spritecomposer.Resources.fonter;
import static util.spritecomposer.Resources.g;
import static util.spritecomposer.Resources.immi;
import static util.spritecomposer.Resources.p;
import static util.spritecomposer.Resources.sources;

import java.io.IOException;
import java.nio.file.Path;

import init.constant.C;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.file.FileGetter;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;
import snake2d.util.sprite.TextureCoords;
import snake2d.util.sprite.TileTexture;
import snake2d.util.sprite.text.Font;

public final class ComposerThings {


	
	private ComposerThings() {
		
	}
	
	public static class IInit {
		
		public IInit(Path path, int width, int height) throws IOException {
			c.setSource(path, width, height);
			if (g == null)
				init(c, sources, dests);
		}
		
		public IInit() throws IOException {
			if (g == null)
				init(c, sources, dests);
		}
		
		protected void init(ComposerUtil c, ComposerSources s, ComposerDests d) throws IOException{
			
		};
	}
	
	public static abstract class ITileSheet {
		
		protected ITileSheet() {
			
		}
		
		protected ITileSheet(Path path, int width, int height)  throws IOException{
			c.setSource(path, width, height);
		}
		
		static TILE_SHEET save(int scale, int tileSize, int startTile, int tiles, int tilesX) {
			p.mark("sheet");
			p.i(scale);
			p.i(tileSize);
			p.i(startTile);
			p.i(tiles);
			p.i(tilesX);
			return new TIleSheetPow2(scale, tileSize, startTile, tilesX, tiles);
		}
		
		private static TILE_SHEET read(FileGetter g) throws IOException {
			g.check("sheet");
			int scale = g.i();
			int tileSize = g.i();
			int startTile = g.i();
			int tiles = g.i();
			int tilesX = g.i();
			return new TIleSheetPow2(scale, tileSize, startTile, tilesX, tiles);
		}
		
		public TILE_SHEET get() throws IOException {
			if (g == null) {
				TILE_SHEET s = init(c, sources, dests);
				if (s == null) {
					save(1, 8, 0, 0, 0);
				}
				return s;
			}
			return read(g);
		}
		
		protected abstract TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d);	
	}
	
	public static abstract class ITileSprite implements SPRITE{
		
		private final int width,height;
		private final TILE_SHEET sheet;
		
		protected ITileSprite(int width, int height, int scale) throws IOException{
			this.width = width*scale;
			this.height = height*scale;
			sheet = get();
		}
		
		protected ITileSprite(int width, int height, int scale, Path path, int w, int h) throws IOException{
			c.setSource(path, w, h);
			this.width = width*scale;
			this.height = height*scale;
			sheet = get();
		}
		
		public TILE_SHEET get() throws IOException {
			if (g == null) {
				return init(c, sources, dests);
			}
			return ITileSheet.read(g);
		}
		
		protected abstract TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d);

		@Override
		public int width() {
			return width;
		}

		@Override
		public int height() {
			return height;
		}

		@Override
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
			int i = 0;
			for (int y = 0; y < height; y+=sheet.size()) {
				for (int x = 0; x < width; x+=sheet.size()) {
					sheet.render(r, i, X1+x, Y1+y);
					i++;
				}
			}
		}

		@Override
		public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
			throw new RuntimeException("not supported!");
		}	
	}
	
	public abstract static class ITileSheetL{
		
		protected  ITileSheetL() {
			
		}
		
		protected  ITileSheetL(Path path, int width, int height) throws IOException {
			c.setSource(path, width, height);
		}
		
		public LIST<TILE_SHEET> get() throws IOException {
			if (g == null) {
				p.mark("list");
				int a = init(c, sources, dests);
				p.i(a);
				ArrayList<TILE_SHEET> res = new ArrayList<>(a);
				for (int i = 0; i < a; i++) {
					res.add(next(i, c, sources, dests));
				}
				p.mark("list");
				return res;
				
			}
			g.check("list");
			int a = g.i();
			ArrayList<TILE_SHEET> res = new ArrayList<>(a);
			for (int i = 0; i < a; i++) {
				res.add(ITileSheet.read(g));
			}
			g.check("list");
			return res;
		}
		
		protected abstract int init(ComposerUtil c, ComposerSources s, ComposerDests d);
		protected abstract TILE_SHEET next(int i, ComposerUtil c, ComposerSources s, ComposerDests d);
	}
	
	public abstract static class ITileSpriteL{
		
		protected  ITileSpriteL() {
			
		}
		
		public LIST<TILE_SHEET> get() throws IOException {
			if (g == null) {
				p.mark("list");
				int a = init(c, sources, dests);
				p.i(a);
				ArrayList<TILE_SHEET> res = new ArrayList<>(a);
				for (int i = 0; i < a; i++) {
					res.add(next(i, c, sources, dests));
				}
				p.mark("list");
				return res;
				
			}
			g.check("list");
			int a = g.i();
			ArrayList<TILE_SHEET> res = new ArrayList<>(a);
			for (int i = 0; i < a; i++) {
				res.add(ITileSheet.read(g));
			}
			g.check("list");
			return res;
		}
		
		protected abstract int init(ComposerUtil c, ComposerSources s, ComposerDests d);
		protected abstract TILE_SHEET next(int i, ComposerUtil c, ComposerSources s, ComposerDests d);
	}
	
	public static abstract class ITileTexture {
		
		private final int width,height;
		
		protected ITileTexture(int width, int height) {
			this.width = width;
			this.height = height;
		}
		
		protected ITileTexture(int width, int height, Path path, int w, int h) throws IOException {
			c.setSource(path, w, h);
			this.width = width;
			this.height = height;
		}
		
		public TileTexture get() throws IOException {
			SpriteData d;
			if (g == null) {
				d = init(c, sources, dests, immi);
			}else {
				d = SpriteData.read(g);
			}
			int dx = 0;
			if (Optimizer.get(24) != null)
				dx += Optimizer.get(24).tilesX*24;
			return new TileTexture(C.T_PIXELS, width, height, d.x1+dx, d.y1) ;
		}
		
		protected abstract SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d, ComposerTexturer t);
	}
	

	
	protected static void init(Path path, int width, int height) throws IOException {
		if (g == null) {
			c.setSource(path, width, height);
		}
	}
	
	public static abstract class ISpriteData {
		
		protected  ISpriteData() {
			
		}
		
		protected  ISpriteData(Path path, int width, int height) throws IOException {
			c.setSource(path, width, height);
		}
		
		public SpriteData get() throws IOException {
			if (g == null) {
				return init(c, sources, dests);
			}
			return SpriteData.read(g);
		}
		
		protected abstract SpriteData init(ComposerUtil c, ComposerSources s, ComposerDests d);	
		
	}
	
	public static abstract class INop {
		
		protected  INop(){
			if (g == null) {
				init(c, sources, dests);
			}
		}
		
		protected abstract void init(ComposerUtil c, ComposerSources s, ComposerDests d);	
		
	}
	
	public static abstract class ISpriteList {
		
		protected  ISpriteList() {
			
		}
		
		protected  ISpriteList(Path path, int width, int height) throws IOException{
			c.setSource(path, width, height);
		}
		
		public SpriteData[] get() throws IOException {
			if (g == null) {
				p.mark("list");
				int a = init(c, sources, dests);
				p.i(a);
				SpriteData[] res = new SpriteData[a];
				for (int i = 0; i < a; i++) {
					res[i] = next(i, c, sources, dests);
				}
				p.mark("list");
				return res;
			}
			g.check("list");
			int a = g.i();
			SpriteData[] res = new SpriteData[a];
			for (int i = 0; i < a; i++) {
				res[i] = SpriteData.read(g);
			}
			g.check("list");
			return res;
		}
		
		protected abstract int init(ComposerUtil c, ComposerSources s, ComposerDests d);
		protected abstract SpriteData next(int i, ComposerUtil c, ComposerSources s, ComposerDests d);
		
	}
	
	public static abstract class IColorSampler {
		
		protected  IColorSampler() {
			
		}
		
		protected  IColorSampler(Path path, int width, int height) throws IOException{
			c.setSource(path, width, height);
		}
		
		static COLOR save (int c) {
			int r = (c >> 24) & 0x0FF;
			int g = (c >> 16) & 0x0FF;
			int b = (c >> 8) & 0x0FF;
			p.i(r);
			p.i(g);
			p.i(b);
			return new ColorImp(r,g,b);
			
		}
		
		public LIST<COLOR> get() throws IOException {
			if (g == null) {
				p.mark("color list");
				int a = init(c, sources, dests);
				p.i(a);
				ArrayList<COLOR> res = new ArrayList<>(a);
				for (int i = 0; i < a; i++) {
					res.add(next(i, c, sources, dests));
				}
				p.mark("list");
				return res;
			}
			g.check("color list");
			int a = g.i();
			ArrayList<COLOR> res = new ArrayList<>(a);
			for (int i = 0; i < a; i++) {
				res.add(new ColorImp(g.i(), g.i(), g.i()));
			}
			g.check("list");
			return res;
		}
		
		public LIST<COLOR> getHalf() throws IOException {
			if (g == null) {
				p.mark("color list");
				int a = init(c, sources, dests);
				p.i(a);
				ArrayList<COLOR> res = new ArrayList<>(a);
				for (int i = 0; i < a; i++) {
					res.add(next(i, c, sources, dests).shade(0.5));
				}
				p.mark("list");
				return res;
			}
			g.check("color list");
			int a = g.i();
			ArrayList<COLOR> res = new ArrayList<>(a);
			for (int i = 0; i < a; i++) {
				res.add(new ColorImp(g.i()/2, g.i()/2, g.i()/2));
			}
			g.check("list");
			return res;
		}
		
		protected abstract int init(ComposerUtil c, ComposerSources s, ComposerDests d);
		protected abstract COLOR next(int i, ComposerUtil c, ComposerSources s, ComposerDests d);
		
	}
	
	
	public static abstract class IColorSamplerSingle {
		
		protected  IColorSamplerSingle() {
			
		}
		
		public COLOR get() throws IOException {
			if (g == null) {
				p.mark("color");
				return init(c, sources, dests);
			}
			g.check("color");
			return new ColorImp(g.i(), g.i(), g.i());
		}
		
		public COLOR getHalf() throws IOException {
			if (g == null) {
				p.mark("color");
				return init(c, sources, dests);
			}
			g.check("color");
			return new ColorImp(new ColorImp(g.i()/2, g.i()/2, g.i()/2));
		}
		
		protected abstract COLOR init(ComposerUtil c, ComposerSources s, ComposerDests d);

	}
	
	public static abstract class IFont {
		
		protected  IFont() {
			
		}
		
		protected  IFont(Path path)  throws IOException{
			c.setSource(path);
		}
		
		public Font get(int trail) throws IOException {
			if (g == null) {
				return init(c, fonter);
			}
			return ComposerFonter.get(trail);
		}
		
		protected abstract Font init(ComposerUtil c, ComposerFonter f);	
		
	}
	
	
	public static class ISprite{

		private static SPRITE.SpriteImp getSprite(int scale, SpriteData d) {
			
			short x1,x2,y1,y2,w,h;
			x1 = (short) d.x1;
			x2 = (short) (d.x1 + d.width);
			y1 = (short) d.y1;
			y2 = (short) (d.y1 + d.height);
			w = (short) (d.width*scale);
			h = (short) (d.height*scale);
			return new SPRITE.SpriteImp(x1, x2, y1, y2, w, h);
		}

		public static SPRITE game(SpriteData d) {
			return getSprite(C.SCALE, d);
		}
		
		public static SPRITE gui(SpriteData d) {
			return getSprite(C.SG, d);
		}
		
		public static SPRITE normal(SpriteData d) {
			return getSprite(C.SCALE_NORMAL, d);
		}
		
		public static SPRITE scaled(SpriteData d, int scale) {
			return getSprite(scale, d);
		}
		
		public static LIST<SPRITE> game(SpriteData[] data) {
			ArrayList<SPRITE> res = new ArrayList<>(data.length);
			for (int i = 0; i < data.length; i++)
				res.add(game(data[i]));
			return res;
		}
		
		public static LIST<SPRITE> gui(SpriteData[] data) {
			ArrayList<SPRITE> res = new ArrayList<>(data.length);
			for (int i = 0; i < data.length; i++)
				res.add(gui(data[i]));
			return res;
		}
		
		public static LIST<SPRITE> normal(SpriteData[] data) {
			ArrayList<SPRITE> res = new ArrayList<>(data.length);
			for (int i = 0; i < data.length; i++)
				res.add(normal(data[i]));
			return res;
		}
		
	}

	
}
