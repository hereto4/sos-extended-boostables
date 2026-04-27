package util.spritecomposer;

import init.constant.C;
import snake2d.CORE;
import snake2d.LOG;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SnakeImage;
import snake2d.util.sprite.TILE_SHEET;

public final class ComposerDests {

	public final Tile s16;
	public final Tile s8;
	public final Tile s32;
	public final Tile s24;
	final DestChunk chunk;
	
	

	ComposerDests(int WIDTH) {
		s16 = new Tile(16, WIDTH);
		s8 = new Tile(8, WIDTH);
		s32 = new Tile(32, WIDTH);
		s24 = new Tile(24, WIDTH);
		chunk = new DestChunk(WIDTH - s24.tilesX*s24.size, 512);
		for (int y = 0; y < 16; y++) {
			for (int x = 0; x < 16; x++) {
				
				s16.diffuseSet(x, y, 255, 255, 255, 255);
				s16.normalSet(x, y, 0x80, 0x80, 0xFF, 0xFF);
			}
		}
		s16.skip(1);
	}
	
	private volatile boolean saved = false;
	
	void save(java.nio.file.Path deff, java.nio.file.Path nor, FilePutter p, int extraHeight) {
		
		int HEIGHT = height(extraHeight);
		
		SnakeImage diffuse = new SnakeImage(s16.width()*s16.tilesX, HEIGHT);
		SnakeImage normal = new SnakeImage(diffuse.width, HEIGHT);
		
		for (COORDINATE c : new Rec(diffuse.width, HEIGHT))
			normal.rgb.set(c.x(), c.y(), 127, 127, 255, 255);
		
		Tile[] tiles = new Tile[] {
			s8,s16,s32,s24
		};
		
		int ly = 0;
		int y1 = 0;
		for (Tile t : tiles) {
			int h = (int) Math.ceil((double)t.lastTile/t.tilesX);
			p.i(h);
			h*= t.size;
			for (int y = 0; y < h; y++) {
				for (int x = 0; x < t.destWidth(); x++) {
					diffuse.rgb.set(x, y1+y, t.diffuseGet(x, y));
					normal.rgb.set(x, y1+y, t.normalGet(x, y));
				}
			}
			y1 += h;
			ly = y1 -h;
			
			t.dispose();
		}
		
		for (int y = 0; y < chunk.diffuse.height; y++) {
			for (int x = 0; x < chunk.normal.width; x++) {
				int py = ly+y;					
				diffuse.rgb.set(x+s24.tilesX*24, py, chunk.diffuseGet(x, y));
				normal.rgb.set(x+s24.tilesX*24, py, chunk.normalGet(x, y));
			}
		}
		
		chunk.dispose();

		
		saved = false;
		
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				diffuse.save(""+deff.toAbsolutePath());
				normal.save(""+nor.toAbsolutePath());
				saved = true;
			}
		});
		t.setName("composer saver");
		t.start();
		
		while(!saved) {
			CORE.checkIn();
			try {
				Thread.sleep(16);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		
		
		diffuse.dispose();
		normal.dispose();
	}
	
	int height(int extra) {
		
		Tile[] tiles = new Tile[] {
			s8,s16,s32,s24
		};
		
		int h = 0;
		for (Tile t : tiles) {
			h += (int) Math.ceil((double)t.lastTile/t.tilesX)*t.size;
		}
		
		if ((int) Math.ceil((double)s24.lastTile/s24.tilesX)*s24.size < chunk.diffuse.height+extra) {
			h -= (int) Math.ceil((double)s24.lastTile/s24.tilesX)*s24.size;
			h += chunk.diffuse.height + extra;
			
		}
		
		int hh = h / 256;
		if (h%256 > 0)
			hh++;
		return hh*256;
		
		
	}
	
	
	void dispose() {
		s16.dispose();
		s8.dispose();
		s32.dispose();
		s24.dispose();
		chunk.dispose();
	}
	
	static abstract class Dest {
		
		abstract int x1();
		abstract int y1();
		abstract int width();
		abstract int height();
		public abstract void jump(int i);
		
		public void diffuseSet(int x, int y, int r, int g, int b, int a) {
			
			int res = r;
			res = res << 8;
			res |= g;
			res = res << 8;
			res |= b;
			res = res << 8;
			res |= a;
			diffuseSet(x, y, res);
		}
		public void normalSet(int x, int y, int r, int g, int b, int a) {
			
			int res = r;
			res = res << 8;
			res |= g;
			res = res << 8;
			res |= b;
			res = res << 8;
			res |= a;
			normalSet(x, y, res);
		}
		
		
		public abstract void diffuseSet(int x, int y, int c);
		public abstract int diffuseGet(int x, int y);
		public abstract void normalSet(int x, int y, int c);
		public abstract int normalGet(int x, int y);
		
		public abstract int destWidth();
		
		public abstract void dispose();
		abstract int size();
		
	}
	
	public final class Tile extends Dest{
		
		final int startY;
		final int tilesX;
		private int tx = 0;
		private int ty = 0;
		private int lastTile = 0;
		final int size;
		final int startX;
		private final int destWidth;
		
		private SnakeImage[] diffuses = new SnakeImage[0];
		private SnakeImage[] normals = new SnakeImage[0];

		Tile(int size, int width) {
			this.startY = 0;
			this.size = size;
			startX = 0;
			int s = 1;
			while (s * 2 * size <= width) {
				s *= 2;
			}
			tilesX = s;
			destWidth = tilesX*this.size;
		}
		

		
		@Override
		int y1() {
			return startY + ty * size;
		}
		
		@Override
		int x1() {
			return tx * size;
		}
		
		@Override
		int size() {
			return size;
		}

		@Override
		int width() {
			return size;
		}

		@Override
		int height() {
			return size;
		}

		@Override
		public void jump(int i) {
			tx+= i;
			while(tx >= tilesX) {
				ty++;
				tx -= tilesX;
			}
			while(tx < 0) {
				tx += tilesX;
				ty--;
			}
			
			if (tx < 0 || ty < 0)
				throw new RuntimeException(tx + " " + ty);
			
		}
		
		public void debug() {
			LOG.ln(x1() + " " + (y1()));
		}
		
		public TILE_SHEET saveGame() {
			return save(C.SCALE);
		}
		
		public TILE_SHEET saveNormal() {
			return save(C.SCALE_NORMAL);
		}
		
		public TILE_SHEET saveGui() {
			return save(C.SG);
		}
		
		public SpriteData saveSprite(int tilesX) {
			if (tilesX <= 0)
				throw new RuntimeException();
				
			jump(-tilesX);
			int x1 = tx*size;
			int y1 = ty*size + startY;
			skip(tilesX);
			return SpriteData.save(x1, y1, size*tilesX, size, size);
		}
		
		public TILE_SHEET save(int scale) {
			int end = tx + ty*tilesX;
			int nrOfTiles = end - lastTile;
			int startTile = lastTile;
			lastTile = end;
			return ComposerThings.ITileSheet.save(scale, size, startTile, nrOfTiles, tilesX);
		}
		
		public final SpriteData saveSprite() {
			return saveSprite(1);
		}
		
		public void skip(int i) {
			jump(i);
			lastTile = tx + ty*tilesX;
		}
		
		public void skipNPaint(int i) {
			while(i-- > 0) {
				setNewImage(x1(), y1());
				for (int y = 0; y < size; y++) {
					for (int x = 0; x < size; x++) {
						diffuseSet(x1()+x, y1()+y, -1);
					}
				}
				jump(1);
			}
		}
		
		@Override
		public void dispose() {
			for (int i = 0; i < normals.length; i++) {
				normals[i].dispose();
				diffuses[i].dispose();
			}
		}

		private void setNewImage(int x, int y) {
			int k = y/(size*32);
			
			if (k >= diffuses.length) {
				SnakeImage[] diffs = new SnakeImage[diffuses.length + 1];
				SnakeImage[] norms = new SnakeImage[normals.length + 1];
				
				for (int i = 0; i < diffuses.length; i++) {
					diffs[i] = diffuses[i];
					norms[i] = normals[i];
				}
				diffs[diffs.length-1] = new SnakeImage(tilesX*this.size, 32*this.size);
				norms[norms.length-1] = new SnakeImage(tilesX*this.size, 32*this.size);
				
				diffuses = diffs;
				normals = norms;
			}
			
		}

		@Override
		public void diffuseSet(int x, int y, int c) {
			setNewImage(x, y);
			int k = y/(size*32);
			y -= k*size*32;
			diffuses[k].rgb.set(x, y, c);
		}

		@Override
		public int diffuseGet(int x, int y) {
			setNewImage(x, y);
			int k = y/(size*32);
			y -= k*size*32;
			return diffuses[k].rgb.get(x, y);
		}

		@Override
		public void normalSet(int x, int y, int c) {
			setNewImage(x, y);
			int k = y/(size*32);
			y -= k*size*32;
			normals[k].rgb.set(x, y, c);
		}

		@Override
		public int normalGet(int x, int y) {
			setNewImage(x, y);
			int k = y/(size*32);
			y -= k*size*32;
			return normals[k].rgb.get(x, y);
		}



		@Override
		public int destWidth() {
			return destWidth;
		}

	}
	
	final static class DestChunk extends Dest {

		final Rec rec = new Rec();
		private SnakeImage diffuse;
		private SnakeImage normal;
		public final int width;
		
		private DestChunk(int width, int height) {
			diffuse = new SnakeImage(width, height);
			normal = new SnakeImage(width, height);
			this.width = width;
		}
		
		@Override
		int x1() {
			return rec.x1();
		}

		@Override
		int y1() {
			return rec.y1();
		}

		@Override
		int width() {
			return rec.width();
		}

		@Override
		int height() {
			return rec.height();
		}
		
		@Override
		public int destWidth() {
			return width;
		}

		@Override
		public void jump(int i) {
			
		}

		@Override
		public void dispose() {
			normal.dispose();
			diffuse.dispose();
		}

		@Override
		int size() {
			return rec.width();
		}

		@Override
		public void diffuseSet(int x, int y, int c) {
			diffuse.rgb.set(x, y, c);
			
		}

		@Override
		public int diffuseGet(int x, int y) {
			return diffuse.rgb.get(x, y);
		}

		@Override
		public void normalSet(int x, int y, int c) {
			normal.rgb.set(x, y, c);
		}

		@Override
		public int normalGet(int x, int y) {
			return normal.rgb.get(x, y);
		}
		
	}
	
}
