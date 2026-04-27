package snake2d.util.rnd;

import java.io.File;

import snake2d.Printer;
import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.datatypes.BODY_HOLDER;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.SnakeImage;
import snake2d.util.map.MAP_BOOLEAN;
import snake2d.util.map.MAP_BOOLEANE;
import snake2d.util.map.MAP_INT;

public class Polymap implements BODY_HOLDER{

	private final int width;
	private final int height;
	private final RECTANGLE bounds;
	private final int[] ids;
	private final double r;
	private final double ri;
	private int checkI;
	private int[] checkers;
	private final RECTANGLE body;
	public final MAP_BOOLEANE checker;
	
	public Polymap(int width, int height){
		this(width, height, 1.0);
	}
	
	public Polymap(int width, int height, double scale){
		this.width = width;
		this.height = height;
		ids = new int[height*width];
		bounds = new Rec(width, height);
		float[][] heights = new float[height][width];
		double a = scale*width*height/163;
		int id = 1;
		r = 64*scale;
		ri = 1.0/r;
		for (int i = 0; i < a; i++)
			polly(RND.rInt(width), RND.rInt(height), id++, heights);
		
		checkers = new int[id];
		body = new Rec(width, height);
		checker = new MAP_BOOLEANE.BooleanMapE(width, height) {
			
			@Override
			public MAP_BOOLEANE set(int tile, boolean value) {
				if (value)
					checkers[ids[tile]] = checkI;
				else
					checkers[ids[tile]] = checkI-1;
				return this;
			}

			@Override
			public boolean is(int tile) {
				return checkers[ids[tile]] == checkI;
			}
		};
	}
	
	public MAP_INT getter = new MAP_INT() {
		
		@Override
		public int get(int tx, int ty) {
			return get(tx+ty*width);
		}
		
		@Override
		public int get(int tile) {
			return ids[tile];
		}
	};
	
	public Polymap(int width, int height, int size, double relaxation){
		this.width = width;
		this.height = height;
		ids = new int[height*width];
		bounds = new Rec(width, height);
		float[][] heights = new float[height][width];
		int id = 1;
		
		r = width/size;
		ri = 1.0/r;
		
		double dx = (double)width/size;
		double dy = (double)height/size;
		
		for (int y = 0; y < size; y++) {
			for (int x = 0; x < size; x++) {
				double qx = dx*x;
				double qy = dy*y;
				qx += dx/2;
				qy += dy/2;
				qx += dx*Math.pow(RND.rFloat(), relaxation) * (RND.rBoolean() ? 0.5 : -0.5);
				qy += dy*Math.pow(RND.rFloat(), relaxation) * (RND.rBoolean() ? 0.5 : -0.5);
				polly((int)qx, (int)qy, id++, heights);
			}
		}

		
		checkers = new int[id];
		body = new Rec(width, height);
		checker = new MAP_BOOLEANE.BooleanMapE(width, height) {
			
			@Override
			public MAP_BOOLEANE set(int tile, boolean value) {
				if (value)
					checkers[ids[tile]] = checkI;
				else
					checkers[ids[tile]] = checkI-1;
				return this;
			}

			@Override
			public boolean is(int tile) {
				return checkers[ids[tile]] == checkI;
			}
		};
	}
	
	public Polymap(RECTANGLE bounds, int cellsize, double randomness){
		this.width = bounds.width();
		this.height = bounds.height();
		ids = new int[height*width];
		this.bounds = new Rec(width, height);
		float[][] heights = new float[height][width];
		int id = 1;
		
		r = cellsize;
		ri = 1.0/r;
		
		int d = cellsize/2;
		
		
		for (int y = -cellsize; y < height+cellsize; y+=cellsize) {
			for (int x = -cellsize; x < width+cellsize; x+=cellsize) {
				double qx = x;
				double qy = y;
				qx += d;
				qy += d;
				qx += RND.rSign()*RND.rFloat(d)*randomness;
				qy += RND.rSign()*RND.rFloat(d)*randomness;
				polly((int)qx, (int)qy, id++, heights);
			}
		}

		
		checkers = new int[id];
		body = new Rec(width, height);
		checker = new MAP_BOOLEANE.BooleanMapE(width, height) {
			
			@Override
			public MAP_BOOLEANE set(int tile, boolean value) {
				if (value)
					checkers[ids[tile]] = checkI;
				else
					checkers[ids[tile]] = checkI-1;
				return this;
			}

			@Override
			public boolean is(int tile) {
				return checkers[ids[tile]] == checkI;
			}
		};
	}
	
	public MAP_BOOLEANE getScaled(double scale) {
		return new MAP_BOOLEANE() {
			
			@Override
			public boolean is(int tile) {
				int x = tile%width;
				int y = tile/width;
				return is(x, y);
			}

			@Override
			public boolean is(int tx, int ty) {
				return checker.is((int)((tx*scale))%width, (int)((ty*scale))%height);
			}

			@Override
			public MAP_BOOLEANE set(int tile, boolean value) {
				int x = tile%width;
				int y = tile/width;
				return set(x, y, value);
			}

			@Override
			public MAP_BOOLEANE set(int tx, int ty, boolean value) {
				checker.set((int)((tx*scale))%width, (int)((ty*scale))%height, value);
				return this;
			}
			
		};
	}

	
	private void polly(int x, int y, int id, float[][] heights) {
		
		if (bounds.holdsPoint(x, y) && heights[y][x] == 1f)
			return;
		for (int y1 = (int) (-r); y1 < r; y1++) {
			int ty = y1 + y;
			if (ty < 0 || ty >= height)
				continue;
			for (int x1 = (int) (-r); x1 < r; x1++) {
				int tx = x + x1;
				if (tx < 0 || tx >= width)
					continue;
				//double d = Math.abs(x1) + Math.abs(y1);
				double d = Math.sqrt(x1*x1 + y1*y1);
				if (d > r)
					continue;
				double v = 1.0-ri*d;
				if (v > heights[ty][tx]) {
					heights[ty][tx] = (float) v;
					ids[tx + width*ty] = id;
				}
				
				
			}
		}
		
	}
	
	public MAP_BOOLEAN isEdge = new MAP_BOOLEAN() {
		
		@Override
		public boolean is(int tx, int ty) {
			if (!bounds.holdsPoint(tx, ty))
				return false;
			
			int id = ids[tx+ty*width];
			DIR d = DIR.E;
			
			for (int i = 0; i < 2; i++) {
				
				int x = tx+d.x();
				int y = ty+d.y();
				if (bounds.holdsPoint(x,y))
					if (id != ids[x+y*width])
						return true;
				
				d = d.next(2);
			}
			return false;
		}
		
		@Override
		public boolean is(int tile) {
			return is (tile% bounds.width(), tile/bounds.width());
		}
	};
	
	public boolean isEdge(int tx, int ty) {
		
		int id = ids[tx+ty*width];
		DIR d = DIR.E;
		
		for (int i = 0; i < 2; i++) {
			
			int x = tx+d.x();
			int y = ty+d.y();
			if (bounds.holdsPoint(x,y))
				if (id != ids[x+y*width])
					return true;
			
			d = d.next(2);
		}
		
		return false;
		
	}
	
	public void checkInit() {
		checkI ++;
	}

	public static void main(String[] args) {
		
		int width = 512;
		int height = 512;
		
		SnakeImage im = new SnakeImage(width, height);
		Polymap p = new Polymap(width, height, 2.0);
		
		int cols = 128;
		int colM = cols-1;
		final COLOR[] co = new COLOR[128];
		
		for (int i = 0; i < cols; i++)
			co[i] = new ColorImp(RND.rInt(255), RND.rInt(255), RND.rInt(255));
		
		for (COORDINATE coo :p.body()) {
			
			int id = p.ids[coo.x()+coo.y()*width];
			COLOR c = co[id&colM];
			
			im.rgb.set(coo.x(), coo.y(), c.red(), c.green(), c.red(), 255);
			
		}
		
		String path = new File("PollyTest.png").getAbsolutePath();
		Printer.ln(path);
		im.save(path);
		
	}
	
	@Override
	public RECTANGLE body() {
		return body;
	}

	public int get(int tx, int ty) {
		return ids[tx+ty*width];
	}
	
}
