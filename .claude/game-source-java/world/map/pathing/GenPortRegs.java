package world.map.pathing;

import static world.WORLD.TBOUNDS;

import init.sprite.SPRITES;
import snake2d.PathTile;
import snake2d.PathUtilOnline.Flooder;
import snake2d.SPRITE_RENDERER;
import snake2d.util.color.COLOR;
import snake2d.util.datatypes.COORDINATE;
import snake2d.util.datatypes.DIR;
import snake2d.util.map.MAP_OBJECTE;
import snake2d.util.misc.ACTION;
import snake2d.util.rnd.Polymap;
import snake2d.util.sets.Bitmap1D;
import snake2d.util.sets.Bitsmap2D;
import util.rendering.RenderData.RenderIterator;
import util.GUTIL;
import util.rendering.ShadowBatch;
import world.WORLD;
import world.map.road.WTRAV;
import world.overlay.WorldOverlays;

final class GenPortRegs extends Bitsmap2D{

	public GenPortRegs(ACTION u) {
		super(-1, 6, WORLD.TBOUNDS());
		WORLD.OVERLAY().debug = new WorldOverlays.OverlayTile(true, false) {
			
			@Override
			protected void renderAbove(SPRITE_RENDERER r, ShadowBatch s, RenderIterator it) {
				if (WORLD.WATER().isBig.is(it.tile()) && get(it.tile()) >= 0) {
					COLOR.UNIQUE.getC(get(it.tile())).bind();
					SPRITES.cons().BIG.outline.render(r, 0, it.x(), it.y());
					COLOR.unbind();
				}
			}
		};
		
		Flooder f = GUTIL.flooder();
		f.init(f);
		
		
		Polymap p = new Polymap(TBOUNDS(), 16, 1);
		p.checkInit();
		int ma = 0;
		
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (WORLD.WATER().isBig.is(c) && WORLD.ROADS().harbour.is(c)) {
				if (p.checker.is(c)) {
					continue;
				}
				p.checker.set(c, true);
				f.pushSloppy(c.x(), c.y(), 0, null);
				ma = Math.max(ma, p.getter.get(c));
				f.setValue2(c, p.getter.get(c));
			}
		}
		
		TmpReg[] regs = new TmpReg[ma+1];
		
		int id = 0;
		
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			if (t.getParent() != null)
				t.setValue2(t.getParent().getValue2());
			
			int pi = (int) t.getValue2();
			if (regs[pi] == null) {
				regs[pi] = new TmpReg(id++, t);
			}
			map.set(t, regs[pi]);
			regs[pi].area ++;
			
			for (DIR d : DIR.ALL) {
				if (!WORLD.WATER().isBig.is(t, d))
					continue;
				if (!WTRAV.can(t.x(), t.y(), d, false))
					continue;
				
				double v = 1;
				if (pi != p.getter.get(t, d))
					v += 100;
				f.pushSmaller(t,d, t.getValue()+v*d.tileDistance(), t);
			}
			
		}
		
		f.done();
		
		Bitmap1D check = new Bitmap1D(id, false);
		id = 0;
		for (COORDINATE c : WORLD.TBOUNDS()) {
			if (process(c, id%max(), check)) {
				id ++;
				if (id % 5 == 0)
					u.exe();
			}
		}
	}
	
	private boolean process(COORDINATE start, int id, Bitmap1D check) {
		
		TmpReg home = map.get(start);
		if (home == null)
			return false;
		
		if (home.done)
			return false;
		check.clear();
		check.set(home.id, true);
		Flooder f = GUTIL.flooder();
		f.init(f);
		f.pushSloppy(start.x(), start.y(), 0);
		
		while(f.hasMore()) {
			PathTile t = f.pollSmallest();
			
			TmpReg r = map.get(t);
			if (r.done)
				continue;
			if (r != home && !check.get(r.id)) {
				if (r.area < 64) {
					check.set(r.id, true);
					home.area += r.area;
					r.done = true;
					r.area = 0;
				}else if (home.area < 64) {
					check.set(r.id, true);
					home.area += r.area;
					r.done = true;
					r.area = 0;
				}
			}
			
			
			if (!check.get(r.id)) {
				continue;
			}
			
			map.set(t, home);
			set(t, id);
			
			for (DIR d : DIR.ALL) {
				if (!WORLD.WATER().isBig.is(t, d))
					continue;
				if (!WTRAV.can(t.x(), t.y(), d, false))
					continue;
				
				double v = d.tileDistance();
				TmpReg to = map.get(t, d);
				if (!check.get(to.id)) {
					v += to.area*100;
				}
				f.pushSmaller(t,d, t.getValue()+v, t);
			}
			
		}
		
		f.done();
		home.done = true;
		
		return true;
	}



	private static class TmpReg {
		
		private final int id;
		int area = 0;
		boolean done = false;
		
		TmpReg(int id, COORDINATE c){
			this.id = id;
		}
		
	}
	
	private final MAP_OBJECTE<TmpReg> map = new MAP_OBJECTE<GenPortRegs.TmpReg>() {
		
		private final TmpReg[] rmap = new TmpReg[WORLD.TAREA()];
		
		@Override
		public TmpReg get(int tx, int ty) {
			if (!WORLD.IN_BOUNDS(tx, ty))
				return null;
			return get(tx+ty*WORLD.TWIDTH());
		}
		
		@Override
		public TmpReg get(int tile) {
			return rmap[tile];
		}
		
		@Override
		public void set(int tx, int ty, TmpReg object) {
			if (WORLD.IN_BOUNDS(tx, ty)) {
				set(tx+ty*WORLD.TWIDTH(), object);
			}
			
		}
		
		@Override
		public void set(int tile, TmpReg object) {
			rmap[tile] = object;
		}
	};
	
}
