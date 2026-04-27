package snake2d.util.rnd;

import snake2d.util.datatypes.COORDINATE;
import snake2d.util.map.MAP_DOUBLEE;

public class HeightMap implements MAP_DOUBLEE{

	private final float[][] spheric;
	
	public HeightMap(int width, int height, int startSize, int endSize) {
		
		OpenSimplexNoise noise = new OpenSimplexNoise(RND.rLong());
		double FEATURE_SIZE = startSize;
		spheric = new float[height][width];
		
		double max = 0;
		double min = 1;

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				double v = 0;
				double size = FEATURE_SIZE;
				int i = 1;
				while (size >= endSize) {
					v += noise.eval(x / size, y / size) / i;
					size /= 2.0;
					i++;
				}
				spheric[y][x] = (float) v;
				if (v > max)
					max = v;
				if (v < min)
					min = v;
			}
		}

		// NORMALIZE

		double range = max - min;
		double d = 1.0 / range;
		
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				double v = spheric[y][x];
				v -= min;
				v *= d;
				spheric[y][x] = (float) v;
			}
		}
		

		
	}
	
	@Override
	public double get(int tx, int ty) {
		return spheric[ty][tx];
	}
	
	@Override
	public HeightMap set(int tx, int ty, double value) {
		if (tx >= 0 && tx < spheric[0].length && ty >= 0 && ty < spheric.length)
			spheric[ty][tx] = (float) value;
		return this;
	}
	
	public void sink(int cx, int cy, int radius, double leaveBelow, double leaveAbove) {
		double ri = 1.0/radius;
		
		for (int y1 = (int) (-radius); y1 < radius; y1++) {
			int ty = y1 + cy;
			if (ty < 0 || ty >= spheric.length)
				continue;
			for (int x1 = (int) (-radius); x1 < radius; x1++) {
				int tx = cx + x1;
				if (tx < 0 || tx >= spheric[0].length)
					continue;
				double v = spheric[ty][tx];
				if (v < leaveBelow)
					continue;
				if (v > leaveAbove)
					continue;
				
				double d = Math.sqrt(x1*x1 + y1*y1);
				
				if (d < radius) {
					spheric[ty][tx] *= ri*d;
				}
				
				
			}
		}
	}
	
	public void sink(COORDINATE c, int radius, double leaveBelow, double leaveAbove) {
		sink(c.x(), c.y(), radius, leaveBelow, leaveAbove);
	}
	
	public void rise(int x, int y, double r, double bonus) {
	
		double ri = 1.0/r;
		
		for (int y1 = (int) (-r); y1 < r; y1++) {
			int ty = y1 + y;
			if (ty < 0 || ty >= spheric.length)
				continue;
			for (int x1 = (int) (-r); x1 < r; x1++) {
				int tx = x + x1;
				if (tx < 0 || tx >= spheric[0].length)
					continue;
				double d = Math.sqrt(x1*x1 + y1*y1);
				
				if (d < r) {
					spheric[ty][tx] += (1.0-spheric[ty][tx])*(1.0 - ri*d) + bonus*(1.0 - ri*d);
				}
				
				
			}
		}
			
	}

	@Override
	public double get(int tile) {
		return get(tile%spheric.length, tile/spheric.length);
	}

	@Override
	public MAP_DOUBLEE set(int tile, double value) {
		throw new RuntimeException();
	}

	
}
