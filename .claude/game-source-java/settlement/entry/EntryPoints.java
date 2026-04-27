package settlement.entry;

import java.io.IOException;

import init.constant.C;
import init.sprite.SPRITES;
import settlement.main.CapitolArea;
import settlement.main.SETT;
import snake2d.Renderer;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.Coo;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.datatypes.Rec;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.SAVABLE;
import snake2d.util.map.MAP_BOOLEANE;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.ArrayListGrower;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.INDEXED;
import snake2d.util.sets.LIST;
import util.colors.GCOLOR;
import util.rendering.RenderData;
import world.map.regions.centre.WCentre;

public final class EntryPoints {
	
	final int ww = Math.max(SETT.TWIDTH, SETT.THEIGHT);
	private final Bitmap1D ismap = new Bitmap1D(ww*4, false);
	private final LIST<EntryPoint> all;
	private final ArrayList<EntryPoint> active;
	private final ArrayList<EntryPoint> reachable;
	private boolean dirty = true;
	
	EntryPoints() {
	
		if (false)
			;
			//use this instead
		
//		Rec ww = new Rec(WCentre.TILE_DIM);
//		
//		ArrayListGrower<EntryPoint> all = new ArrayListGrower<>();
//		int index = 0;
//		for (COORDINATE c : ww) {
//			if (c.x() == 0) {
//				int x1 = 0;
//				int x2 = 1;
//				int y1 = c.y()*SETT.THEIGHT/WCentre.TILE_DIM;
//				int y2 = (c.y()+1)*SETT.THEIGHT/WCentre.TILE_DIM;
//				EntryPoint p = new EntryPoint(index++, x1, x2, y1, y2, DIR.W, c.x(), c.y());
//				all.add(p);
//				
//				if (c.y() == 0) {
//					p = new EntryPoint(index++, x1, x2, y1, y2, DIR.NW, c.x(), c.y());
//					all.add(p);
//					
//					y1 = 0;
//					y2 = 1;
//					x1 = c.x()*SETT.TWIDTH/WCentre.TILE_DIM;
//					x2 = (c.x()+1)*SETT.TWIDTH/WCentre.TILE_DIM;
//					
//					p = new EntryPoint(index++, x1, x2, y1, y2, DIR.NW, c.x(), c.y());
//					all.add(p);
//					p = new EntryPoint(index++, x1, x2, y1, y2, DIR.N, c.x(), c.y());
//					all.add(p);
//				}
//				
//				if (c.y() == WCentre.TILE_DIM-1) {
//					p = new EntryPoint(index++, x1, x2, y1, y2, DIR.SW, c.x(), c.y());
//					all.add(p);
//					
//					y1 = SETT.THEIGHT-1;
//					y2 = SETT.THEIGHT;
//					x1 = c.x()*SETT.TWIDTH/WCentre.TILE_DIM;
//					x2 = (c.x()+1)*SETT.TWIDTH/WCentre.TILE_DIM;
//					
//					p = new EntryPoint(index++, x1, x2, y1, y2, DIR.SW, c.x(), c.y());
//					all.add(p);
//					
//					p = new EntryPoint(index++, x1, x2, y1, y2, DIR.S, c.x(), c.y());
//					all.add(p);
//				}
//				
//			}else if (c.x() == WCentre.TILE_DIM-1) {
//				int x1 = SETT.TWIDTH-1;
//				int x2 = SETT.TWIDTH;
//				int y1 = c.y()*SETT.THEIGHT/WCentre.TILE_DIM;
//				int y2 = (c.y()+1)*SETT.THEIGHT/WCentre.TILE_DIM;
//				EntryPoint p = new EntryPoint(index++, x1, x2, y1, y2, DIR.E, c.x(), c.y());
//				all.add(p);
//				
//				if (c.y() == 0) {
//					p = new EntryPoint(index++, x1, x2, y1, y2, DIR.NE, c.x(), c.y());
//					all.add(p);
//					
//					y1 = 0;
//					y2 = 1;
//					x1 = c.x()*SETT.TWIDTH/WCentre.TILE_DIM;
//					x2 = (c.x()+1)*SETT.TWIDTH/WCentre.TILE_DIM;
//					
//					p = new EntryPoint(index++, x1, x2, y1, y2, DIR.NE, c.x(), c.y());
//					all.add(p);
//					
//					p = new EntryPoint(index++, x1, x2, y1, y2, DIR.N, c.x(), c.y());
//					all.add(p);
//				}
//				
//				if (c.y() == WCentre.TILE_DIM-1) {
//					p = new EntryPoint(index++, x1, x2, y1, y2, DIR.SE, c.x(), c.y());
//					all.add(p);
//					
//					y1 = SETT.THEIGHT-1;
//					y2 = SETT.THEIGHT;
//					x1 = c.x()*SETT.TWIDTH/WCentre.TILE_DIM;
//					x2 = (c.x()+1)*SETT.TWIDTH/WCentre.TILE_DIM;
//					
//					p = new EntryPoint(index++, x1, x2, y1, y2, DIR.SE, c.x(), c.y());
//					all.add(p);
//					
//					p = new EntryPoint(index++, x1, x2, y1, y2, DIR.S, c.x(), c.y());
//					all.add(p);
//				}
//			}else if (c.y() == 0) {
//				int y1 = 0;
//				int y2 = 1;
//				int x1 = c.x()*SETT.TWIDTH/WCentre.TILE_DIM;
//				int x2 = (c.x()+1)*SETT.TWIDTH/WCentre.TILE_DIM;
//				EntryPoint p = new EntryPoint(index++, x1, x2, y1, y2, DIR.N, c.x(), c.y());
//				all.add(p);
//			}else if (c.y() == WCentre.TILE_DIM-1) {
//				int y1 = SETT.THEIGHT-1;
//				int y2 = SETT.THEIGHT;
//				int x1 = c.x()*SETT.TWIDTH/WCentre.TILE_DIM;
//				int x2 = (c.x()+1)*SETT.TWIDTH/WCentre.TILE_DIM;
//				EntryPoint p = new EntryPoint(index++, x1, x2, y1, y2, DIR.S, c.x(), c.y());
//				all.add(p);
//			}
//			
//			
//			
//		}
		

		Rec ww = new Rec(WCentre.TILE_DIM);
		
		ArrayListGrower<EntryPoint> all = new ArrayListGrower<>();
		int index = 0;
		for (COORDINATE c : ww) {
			if (c.x() == 0) {
				int x1 = 0;
				int x2 = 1;
				int y1 = c.y()*SETT.THEIGHT/WCentre.TILE_DIM;
				int y2 = (c.y()+1)*SETT.THEIGHT/WCentre.TILE_DIM;
				EntryPoint p = new EntryPoint(index++, x1, x2, y1, y2, DIR.W, c.x(), c.y());
				all.add(p);
				
				if (c.y() == 0) {
					p = new EntryPoint(index++, x1, x2, y1, y2, DIR.NW, c.x(), c.y());
					all.add(p);
					p = new EntryPoint(index++, x1, x2, y1, y2, DIR.N, c.x(), c.y());
					all.add(p);
				}
				
				if (c.y() == WCentre.TILE_DIM-1) {
					p = new EntryPoint(index++, x1, x2, y1, y2, DIR.SW, c.x(), c.y());
					all.add(p);
					p = new EntryPoint(index++, x1, x2, y1, y2, DIR.S, c.x(), c.y());
					all.add(p);
				}
				
			}else if (c.x() == WCentre.TILE_DIM-1) {
				int x1 = SETT.TWIDTH-1;
				int x2 = SETT.TWIDTH;
				int y1 = c.y()*SETT.THEIGHT/WCentre.TILE_DIM;
				int y2 = (c.y()+1)*SETT.THEIGHT/WCentre.TILE_DIM;
				EntryPoint p = new EntryPoint(index++, x1, x2, y1, y2, DIR.E, c.x(), c.y());
				all.add(p);
				
				if (c.y() == 0) {
					p = new EntryPoint(index++, x1, x2, y1, y2, DIR.NE, c.x(), c.y());
					all.add(p);
					p = new EntryPoint(index++, x1, x2, y1, y2, DIR.N, c.x(), c.y());
					all.add(p);
				}
				
				if (c.y() == WCentre.TILE_DIM-1) {
					p = new EntryPoint(index++, x1, x2, y1, y2, DIR.SE, c.x(), c.y());
					all.add(p);
					p = new EntryPoint(index++, x1, x2, y1, y2, DIR.S, c.x(), c.y());
					all.add(p);
				}
			}else if (c.y() == 0) {
				int y1 = 0;
				int y2 = 1;
				int x1 = c.x()*SETT.TWIDTH/WCentre.TILE_DIM;
				int x2 = (c.x()+1)*SETT.TWIDTH/WCentre.TILE_DIM;
				EntryPoint p = new EntryPoint(index++, x1, x2, y1, y2, DIR.N, c.x(), c.y());
				all.add(p);
			}else if (c.y() == WCentre.TILE_DIM-1) {
				int y1 = SETT.THEIGHT-1;
				int y2 = SETT.THEIGHT;
				int x1 = c.x()*SETT.TWIDTH/WCentre.TILE_DIM;
				int x2 = (c.x()+1)*SETT.TWIDTH/WCentre.TILE_DIM;
				EntryPoint p = new EntryPoint(index++, x1, x2, y1, y2, DIR.S, c.x(), c.y());
				all.add(p);
			}
			
			
			
		}
		
		this.all = all;
		this.active = new ArrayList<>(all.size());
		this.reachable = new ArrayList<>(all.size());
		
	}
	
	final SAVABLE saver = new SAVABLE() {
		
		@Override
		public void save(FilePutter file) {
			for (EntryPoint p : all)
				p.save(file);
		}
		
		@Override
		public void load(FileGetter file) throws IOException {
			clear();

			for (EntryPoint p : all)
				p.load(file);
			for (EntryPoint p : all) {
				if (p.reachable)
					reachable.add(p);
				if (p.active) {
					active.add(p);
					ismap.set(imapi(p.coo().x(), p.coo().y()), true);
				}
			}
			
			dirty = true;
		}
		
		@Override
		public void clear() {
			dirty = true;
			ismap.clear();
			for (EntryPoint p : all)
				p.clear();
			active.clearSloppy();
			reachable.clearSloppy();
		}
	};

	private int imapi(int tx, int ty) {
		if (tx == 0) {
			return ty;
		}else if (tx == SETT.TWIDTH-1)
			return ww + ty;
		else if (ty == 0)
			return ww*2 + tx;
		else if (ty == SETT.THEIGHT-1)
			return ww*3 + tx;
		return -1;
	}
	
	public final MAP_BOOLEANE map = new MAP_BOOLEANE() {
		
		
		
		@Override
		public boolean is(int tx, int ty) {
			int i = imapi(tx, ty);
			if (i < 0)
				return false;
			return ismap.get(i);
			
		}
		

		
		@Override
		public boolean is(int tile) {
			return is(tile%SETT.TWIDTH, tile/SETT.TWIDTH);
		}

		@Override
		public MAP_BOOLEANE set(int tile, boolean value) {
			return set(tile%SETT.TWIDTH, tile/SETT.TWIDTH, value);
		}

		@Override
		public MAP_BOOLEANE set(int tx, int ty, boolean value) {
			int i = imapi(tx, ty);
			
			if (i < 0)
				return this;
			
			dirty = true;
			if (!value) {
				for (EntryPoint e : all) {
					if (e.coo().isSameAs(tx, ty)) {
						e.active = false;
						ismap.set(imapi(tx, ty), false);
						setActive();
						break;
					}
				}
				return this;
			}
			
			EntryPoint ee = null;
			
			for (EntryPoint e : all) {
				if (e.body.holdsPoint(tx, ty)) {
					ee = e;
					break;
				}
			}
			
			
			
			ismap.set(imapi(ee.coo().x(), ee.coo().y()), false);
			ismap.set(i, true);
			ee.sCoo.set(tx, ty);
			ee.active = true;
			setActive();
			return this;
		}
		
		private void setActive() {
			active.clearSloppy();
			for (EntryPoint e : all) {
				if (e.active)
					active.add(e);
			}
		}
		
	};
	

	void generate(CapitolArea area) {
		
	}
	
	public LIST<EntryPoint> all(){
		return all;
	}
	
	public LIST<EntryPoint> active(){
		return active;
	}
	
	public LIST<EntryPoint> reachable(){
		return reachable;
	}
	
	public EntryPoint all(int wx, int wy){
		return find(wx, wy, all);
	}
	
	public EntryPoint active(int wx, int wy){
		return find(wx, wy, active);
	}
	
	public EntryPoint reachable(int wx, int wy){
		return find(wx, wy, reachable);
	}
	
	private final Coo ctmp = new Coo();
	
	public COORDINATE randomReachable() {
		return randomReachable(RND.rInt());
	}
	
	public COORDINATE randomReachable(int rr) {
		if (reachable.size() <= 0)
			return null;
		EntryPoint p = reachable.getC(rr);
		ctmp.set(p.coo());
		if (!RND.oneIn(3)) {
			DIR d = p.dirOut.next(-2 + RND.rInt(2)*4);
			if (SETT.PATH().connectivity.is(ctmp, d)) {
				ctmp.increment(d.x(), d.y());
			}
		}
		if (SETT.PATH().connectivity.is(ctmp))
			return ctmp;
		return null;
	}
	
	private EntryPoint find(int wx, int wy, LIST<EntryPoint> all){
		
		if (all.size() == 0)
			return null;
		
		EntryPoint best = all.get(0);
		double bestD = Double.MAX_VALUE;
		
		for (int ei = 1; ei < all.size(); ei++) {
			EntryPoint ee = all.get(ei);
			double dist = ee.distanceValue(wx, wy);
			if (dist < bestD) {
				bestD = dist;
				best = ee;
			}
			
		}
		
		return best;
	}
	
	
	public boolean hasAny() {
		return reachable.size() > 0;
	}
	
	void update() {
		
		if (!dirty)
			return;
		

		reachable.clearSloppy();
		for (EntryPoint e : active) {
			
			e.reachable = SETT.PATH().connectivity.is(e.sCoo.x(), e.sCoo.y());
			if (e.reachable) {
				reachable.add(e);
			}
		}
		dirty = false;
	}
	
	public void updateAvailability() {
		dirty = true;
		
	}

	public void render(Renderer r, RenderData renData) {
		
		for (EntryPoint b : active) {
			if (renData.tBounds().holdsPoint(b.coo())) {
				if (b.reachable) {
					GCOLOR.MAP().BETTER.bind();
					SPRITES.cons().ICO.scratch.render(r, b.coo().x()*C.TILE_SIZE-renData.offX1(), b.coo().y()*C.TILE_SIZE-renData.offY1());
					GCOLOR.MAP().BEST.bind();
				}else {
					GCOLOR.MAP().BAD.bind();
					SPRITES.cons().ICO.scratch.render(r, b.coo().x()*C.TILE_SIZE-renData.offX1(), b.coo().y()*C.TILE_SIZE-renData.offY1());
					GCOLOR.MAP().SOSO.bind();
				}
				if (map.is(b.coo())) {
					COLOR.BLUE100.render(r, b.coo().x()*C.TILE_SIZE-renData.offX1(), b.coo().y()*C.TILE_SIZE-renData.offY1());
				}else {
					COLOR.MEDIUM_BROWN.render(r, b.coo().x()*C.TILE_SIZE-renData.offX1(), b.coo().y()*C.TILE_SIZE-renData.offY1());
				}
				
				for (DIR d : DIR.ORTHO) {
					int dx = b.coo().x()+d.x();
					int dy = b.coo().y()+d.y();
					if (SETT.IN_BOUNDS(dx,dy) && renData.tBounds().holdsPoint(dx, dy)) {
						SPRITES.cons().ICO.scratch.render(r, dx*C.TILE_SIZE-renData.offX1(), dy*C.TILE_SIZE-renData.offY1());
					}
				}
			}	
		}
		COLOR.unbind();
		
	}

	public static class EntryPoint implements INDEXED{
		
		public final int DIM;
		
		public final RECTANGLE body;
		private final Coo sCoo = new Coo();
		private final COORDINATE wCoo;
		public final DIR dirOut;
		private final int index;
		
		private boolean reachable = false;
		private boolean active = false;
		
		void save(FilePutter file) {
			sCoo.save(file);
			file.bool(active);
			file.bool(reachable);
		}

		void load(FileGetter file) throws IOException {
			sCoo.load(file);
			active = file.bool();
			reachable = file.bool();
		}

		void clear() {
			sCoo.set(body.cX(), body.cY());
			active = false;
			reachable = false;
		}
		
		EntryPoint(int index, int x1, int x2, int y1, int y2, DIR dir, int wdx, int wdy){
			this.body = new Rec(x1, x2, y1, y2);
			DIM = Math.max(body.width(), body.height());
			this.dirOut = dir;
			sCoo.set(body.cX(), body.cY());
			wCoo = new Coo(wdx, wdy);
			this.index = index;
		}

		@Override
		public int index() {
			return index;
		}
		
		public boolean reachable() {
			return reachable;
		}
		
		public boolean active() {
			return active;
		}
		
		public COORDINATE coo() {
			return sCoo;
		}
		
		public COORDINATE wCooD() {
			return wCoo;
		}
		
		public int wx() {
			return wCoo.x()+SETT.WORLD_AREA().tiles().x1();
		}
		
		public int wy() {
			return wCoo.y()+SETT.WORLD_AREA().tiles().y1();
		}
		
		public double distanceValue(int wx, int wy) {
			

			
			double ox = wx + 0.5;
			double oy = wy + 0.5;
			
			double x = wCoo.x()+SETT.WORLD_AREA().tiles().x1() + 0.5;
			double y = wCoo.y()+SETT.WORLD_AREA().tiles().y1() + 0.5;
			x += dirOut.x()*0.5;
			y += dirOut.y()*0.5;
			
			x-= ox;
			y-= oy;
			
			double dist = x*x + y*y;
			
			return dist;
		}


		
	}
	

	
}
