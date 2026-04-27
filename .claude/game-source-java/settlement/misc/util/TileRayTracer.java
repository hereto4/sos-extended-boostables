package settlement.misc.util;

import init.constant.C;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.sets.LinkedList;

public final class TileRayTracer {

	private final int radius;
	private final Ray[] rays;
	private final Ray[][][] raysOnTile;
	private final COORDINATE[] allTiles;
	private final Ray[] empty = new Ray[0];
	private final short check[][];
	private short checkI = 0;
	
	public TileRayTracer(int radius){
		
		this.radius = radius;
		LinkedList<Ray> rays = new LinkedList<>();
		
		boolean[][] has = new boolean[radius*2+1][radius*2+1];
		raysOnTile = new Ray[radius*2+1][radius*2+1][0];
		for (int gy = -radius; gy <= radius; gy++) {
			rayTrace(-radius, gy, has, rays);
			rayTrace(radius, gy, has, rays);
		}
		
		for (int gx = -radius; gx <= radius; gx++) {
			rayTrace(gx, -radius, has, rays);
			rayTrace(gx, radius, has, rays);
		}
		
		this.rays = new Ray[rays.size()];
		int i = 0;
		while(!rays.isEmpty()) {
			this.rays[i] = new Ray(rays.removeFirst(), i);
			i++;
		}
		int[][] grid = new int[radius*2+1][radius*2+1];
		int all = 0;
		for (Ray r : this.rays) {
			for (COORDINATE c : r.coos) {
				
				if (grid[c.y()+radius][c.x()+radius] == 0)
					all++;
				grid[c.y()+radius][c.x()+radius] ++;
				
			}
		}
		
		
		allTiles = new COORDINATE[all];
		int tileI = 0;
		for (int y = 0; y < grid.length; y++) {
			for (int x = 0; x < grid[y].length; x++) {
				int c = grid[y][x];
				if (c > 0) {
					allTiles[tileI++] = new Coo(x-radius, y-radius);
					
					raysOnTile[y][x] = new Ray[grid[y][x]];
					grid[y][x] = 0;
				}
			}
		}
		
		for (Ray r : this.rays) {
			for (COORDINATE c : r.coos) {
				raysOnTile[c.y()+radius][c.x()+radius][grid[c.y()+radius][c.x()+radius]] = r;
				grid[c.y()+radius][c.x()+radius] ++;
			}
		}
		
		check = new short[radius*2+1][radius*2+1];
		
		
		
	}
	
	public void checkInit() {
		checkI++;
		if (checkI == 0) {
			for (int y = 0; y < check.length; y++) {
				for (int x = 0; x < check[y].length; x++) {
					check[y][x] = 0;
				}
			}
			checkI = 1;
		}
	}
	
	public boolean check(COORDINATE c) {
		if (check[c.y()+radius][c.x()+radius] != checkI) {
			check[c.y()+radius][c.x()+radius] = checkI;
			return true;
		}
		return false;
	}
	
	public boolean checked(COORDINATE c) {
		return check[c.y()+radius][c.x()+radius] == checkI;
	}
	
	public Ray[] rays(int dx, int dy) {
		dx += radius;
		dy += radius;
		if (dx < 0 || dy < 0 || dy >= raysOnTile.length || dx >= raysOnTile[dy].length)
			return empty;
		return raysOnTile[dy][dx];
	}
	
	public Ray[] rays() {
		return rays;
	}
	
	public int radius() {
		return radius;
	}
	
	public COORDINATE[]  tiles(){
		return allTiles;
	}
	

	private void rayTrace(int fromx, int fromy, boolean[][] has, LinkedList<Ray> rays) {

		double x = fromx;
		double y = fromy;
		double divider;		
		if (Math.abs(x) > Math.abs(y)) {
			divider = Math.abs(x);
		}else if(Math.abs(x) < Math.abs(y)) {
			divider = Math.abs(y);
		}else {
			divider = Math.abs(x);
		}
		
		double dx = (-x)/divider;
		double dy = (-y)/divider;
		

		
		for (int i = 0; i < divider; i++) {


			
			int tx = (int) x;
			int ty = (int) y;
			
			double r = Math.floor(Math.sqrt(x*x+y*y));
			
			if (r <= radius) {
				if (has[ty+radius][tx+radius]) {
					return;
				}
				has[ty+radius][tx+radius] = true;
				break;
			}
			
			x += dx;
			y += dy;
		}
		
		LinkedList<Coo> coos = new LinkedList<Coo>();
		LinkedList<Coo> offs = new LinkedList<Coo>();
		
		while(true) {
			
			int tx = (int) x;
			int ty = (int) y;
			if (tx == 0 && ty == 0) {
				
				Ray ray = new Ray(coos.size());
				for (int i = ray.coos.length-1; i >=0; i--) {
					
					ray.coos[i] = coos.removeFirst();
					ray.tileOff[i] = offs.removeFirst();
					ray.radius[i] = Math.sqrt(ray.coos[i].x()*ray.coos[i].x() + ray.coos[i].y()*ray.coos[i].y());
					ray.area[i] = ray.tileOff[i].x()*ray.tileOff[i].y();
					ray.area[i] /= C.TILE_SIZE*C.TILE_SIZE;
				}

				rays.add(ray);
				return;
			}
			coos.add(new Coo(tx, ty));
			offs.add(new Coo(x-tx, y-ty));
			
			x += dx;
			y += dy;
			
		}
		
	}
	
	public final static class Ray {
		
		private final COORDINATE[] coos;
		private final COORDINATE[] tileOff;
		private final double[] radius;
		private final double[] area;
		public final int index;
		
		Ray(Ray other, int index){
			this.index = index;
			coos = other.coos;
			tileOff = other.tileOff;
			radius = other.radius;
			area = other.area;
		}
		
		Ray(int size){
			coos = new COORDINATE[size];
			tileOff = new COORDINATE[size];
			radius = new double[size];
			area = new double[size];
			index = 0;
		}
		
		public COORDINATE first() {
			return coos[0];
		}
		
		public COORDINATE last() {
			return coos[coos.length-1];
		}
		
		public int size() {
			return coos.length;
		}
		
		public COORDINATE get(int i) {
			return coos[i];
		}
		
		public COORDINATE[] coos() {
			return coos;
		}

		public double radius(int i) {
			return radius[i];
		}
		
		public double traverseArea(int i) {
			return 1.0;
		}

		
	}
	
}
