package util.updating;

import java.io.IOException;

import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.rnd.RND;

public abstract class TileUpdater implements SAVABLE{

	private final static int randomizerSize = 64;
	private final static int randomizerMask = randomizerSize-1;
	private static byte[][] randomizerX = new byte[64][64];
	private static byte[][] randomizerY = new byte[64][64];
	
	static {
		for (int y = 0; y < randomizerSize; y++) {
			for (int x = 0; x < randomizerSize; x++) {
				randomizerX[y][x] = (byte) x;
				randomizerY[y][x] = (byte) y;
			}
		}
		
		for (int y = 0; y < randomizerSize; y++) {
			for (int x = 0; x < randomizerSize; x++) {
				
				byte ax = randomizerX[y][x];
				byte ay = randomizerY[y][x];
				
				int x2 = RND.rInt(randomizerSize);
				int y2 = RND.rInt(randomizerSize);
				
				randomizerX[y][x] = randomizerX[y2][x2];
				randomizerY[y][x] = randomizerY[y2][x2];
				
				randomizerX[y2][x2] = ax;
				randomizerY[y2][x2] = ay;
			}
		}
		
	}
	
	private final int width;
	private final int height;
	private int x,y,i;
	private final double secondsBetween;
	private final double tilesPerSecond;
	private double acc = 0;
	
	
	public TileUpdater(int width, int height, double secondsBetween) {
		this.width = width;
		this.height = height;
		this.secondsBetween = secondsBetween;
		tilesPerSecond = ((width*height)/secondsBetween);
	}
	
	public void update(double ds) {
		
		acc += ds*tilesPerSecond;
		
		int a = (int) acc;
		acc -= a;
		while(a > 0) {
			a--;
			update(x, y, i, secondsBetween);
			
			i++;
			x++;
			if (x >= width) {
				x = 0;
				y++;
			}
			if (y >= height) {
				y = 0;
				x = 0;
				i = 0;
			}
		}
	}

	public void updateRandom(double ds) {
		
		acc += ds*tilesPerSecond;
		
		int a = (int) acc;
		acc -= a;
		
		final int qw = width/64;
		final int qh = height/64;
		
		final int divI = (width/64)*(height/64);
		
		while(a > 0) {
			a--;
			
			
			
			int di = (i%divI);
			int qx = di%qw;
			int qy = (di/qw)%qh;
			qx*= 64;
			qy*= 64;
			
			int ri = i/divI;
			int rx = ri&randomizerMask;
			int ry = (ri/64);
			

			
			qx += randomizerX[ry][rx];
			qy += randomizerY[ry][rx];
			
			

			int ui = qx+qy*width;
			update(qx, qy, ui, secondsBetween);
			
			i++;
			
			x++;
			if (x >= width) {
				x = 0;
				y++;
			}

			
			if (y >= height) {
				y = 0;
				x = 0;
				i = 0;
			}
		}
	}
	
	protected abstract void update(int tx, int ty, int i, double timeSinceLast);
	
	@Override
	public final void save(FilePutter fp) {
		fp.writeInt(i);
		fp.writeInt(x);
		fp.writeInt(y);
		fp.d(acc);
	}
	
	@Override
	public final void load(FileGetter fp) throws IOException {
		i = fp.i();
		x = fp.i();
		y = fp.i();
		acc = fp.d();
	}
	
	@Override
	public void clear() {
		i = 0;
		x = 0;
		y = 0;
		acc = 0;
	}
	
}
